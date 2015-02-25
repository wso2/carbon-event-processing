/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.receiver.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigHelper;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationFileSystemInvoker;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventReceiverService implements EventReceiverService {

    private static final Log log = LogFactory.getLog(CarbonEventReceiverService.class);
    private Map<Integer, Map<String, List<EventReceiver>>> tenantSpecificEventReceiverMap;
    private Map<Integer, List<EventReceiverConfigurationFile>> tenantSpecificEventReceiverConfigFileMap;

    public CarbonEventReceiverService() {
        tenantSpecificEventReceiverMap = new ConcurrentHashMap<Integer, Map<String, List<EventReceiver>>>();
        tenantSpecificEventReceiverConfigFileMap = new ConcurrentHashMap<Integer, List<EventReceiverConfigurationFile>>();
    }

    @Override
    public void undeployActiveEventReceiverConfiguration(String eventReceiverName,
                                                        AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventReceiverName);
        if (fileName != null) {
            EventReceiverConfigurationFileSystemInvoker.delete(fileName, axisConfiguration);
        } else {
            throw new EventReceiverConfigurationException("Couldn't undeploy the Event Builder configuration : " + eventReceiverName);
        }

    }

    @Override
    public void undeployInactiveEventReceiverConfiguration(String filename,
                                                          AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        EventReceiverConfigurationFileSystemInvoker.delete(filename, axisConfiguration);

    }

    public void removeEventReceiver(String eventReceiverName,
                                   int tenantId)
            throws EventReceiverConfigurationException {
        Map<String, List<EventReceiver>> eventReceiverListMap = this.tenantSpecificEventReceiverMap.get(tenantId);

        EventReceiverConfiguration eventReceiverConfiguration = getActiveEventReceiverConfiguration(eventReceiverName, tenantId);
        if (eventReceiverListMap != null) {
            int removedCount = 0;
            String exportedStreamDefinitionId = EventReceiverUtil.getExportedStreamIdFrom(eventReceiverConfiguration);
            List<EventReceiver> eventReceiverList = eventReceiverListMap.get(exportedStreamDefinitionId);
            Iterator<EventReceiver> eventReceiverIterator = eventReceiverList.iterator();
            while (eventReceiverIterator.hasNext()) {
                EventReceiver eventReceiver = eventReceiverIterator.next();
                if (eventReceiver.getEventReceiverConfiguration().getEventReceiverName().equals(eventReceiverConfiguration.getEventReceiverName())) {
                    eventReceiver.unsubscribeFromEventAdaptor(null);
                    EventReceiverServiceValueHolder.getEventStreamService().unsubscribe(eventReceiver, tenantId);
                    eventReceiverIterator.remove();
                    removedCount++;
                }
            }
            if (removedCount == 0) {
                throw new EventReceiverConfigurationException("Could not find the specified event builder '"
                        + eventReceiverConfiguration.getEventReceiverName() + "' for removal for the given axis configuration");
            }
        }
    }

    public void addEventReceiver(EventReceiverConfiguration eventReceiverConfiguration,
                                AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        // Start: Checking preconditions to add the event builder
        StreamDefinition exportedStreamDefinition = null;
        try {
            exportedStreamDefinition = EventReceiverServiceValueHolder.getEventStreamService().getStreamDefinition(
                    eventReceiverConfiguration.getToStreamName(), eventReceiverConfiguration.getToStreamVersion(), tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventReceiverConfigurationException("Error while retrieving stream definition for stream " + eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion() + " from store", e);
        }

        if (exportedStreamDefinition == null) {
            throw new EventReceiverStreamValidationException("Stream " + eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion() + " does not exist",
                    eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion()
            );
        }
        Map<String, List<EventReceiver>> eventReceiverListMap
                = tenantSpecificEventReceiverMap.get(tenantId);
        if (eventReceiverListMap == null) {
            eventReceiverListMap = new ConcurrentHashMap<String, List<EventReceiver>>();
            tenantSpecificEventReceiverMap.put(tenantId, eventReceiverListMap);
        }

        List<EventReceiver> eventReceiverList = eventReceiverListMap.get(exportedStreamDefinition.getStreamId());
        if (eventReceiverList == null) {
            eventReceiverList = new ArrayList<EventReceiver>();
            eventReceiverListMap.put(exportedStreamDefinition.getStreamId(), eventReceiverList);
        }
        // End; Checking preconditions to add the event builder
        EventReceiver eventReceiver = new EventReceiver(eventReceiverConfiguration, exportedStreamDefinition, axisConfiguration);
        eventReceiver.subscribeToEventAdaptor();
        try {
            EventReceiverServiceValueHolder.getEventStreamService().subscribe(eventReceiver, tenantId);
        } catch (EventStreamConfigurationException e) {
            //ignored as this is already checked
        }

        eventReceiverList.add(eventReceiver);
    }

    public void addEventReceiverConfigurationFile(String eventReceiverName,
                                                 File file,
                                                 EventReceiverConfigurationFile.DeploymentStatus status,
                                                 String deploymentStatusMessage, String dependency,
                                                 String streamNameWithVersion,
                                                 OMElement ebConfigElement,
                                                 AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles == null) {
            eventReceiverConfigurationFiles = new ArrayList<EventReceiverConfigurationFile>();
            tenantSpecificEventReceiverConfigFileMap.put(tenantId, eventReceiverConfigurationFiles);
        }
        EventReceiverConfigurationFile eventReceiverConfigurationFile = createEventReceiverConfigurationFile(
                eventReceiverName, file, status, axisConfiguration, deploymentStatusMessage, dependency,
                streamNameWithVersion, ebConfigElement);
        eventReceiverConfigurationFiles.add(eventReceiverConfigurationFile);
    }

    public void removeEventReceiverConfigurationFile(String fileName, int tenantId)
            throws EventReceiverConfigurationException {

        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList =
                tenantSpecificEventReceiverConfigFileMap.get(tenantId);
        if (eventReceiverConfigurationFileList != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFileList) {
                if ((eventReceiverConfigurationFile.getFileName().equals(fileName))) {
                    if (eventReceiverConfigurationFile.getDeploymentStatus().
                            equals(EventReceiverConfigurationFile.DeploymentStatus.DEPLOYED)) {
                        String eventReceiverName = eventReceiverConfigurationFile.getEventReceiverName();
                        removeEventReceiver(eventReceiverName, tenantId);
                    }
                    eventReceiverConfigurationFileList.remove(eventReceiverConfigurationFile);
                    return;
                }
            }
        }

    }

    @Override
    public List<EventReceiverConfiguration> getAllActiveEventReceiverConfigurations(int tenantId) {
        List<EventReceiverConfiguration> eventReceiverConfigurations = new ArrayList<EventReceiverConfiguration>();
        Map<String, List<EventReceiver>> eventReceiverListMap = this.tenantSpecificEventReceiverMap.get(tenantId);
        if (eventReceiverListMap != null) {
            for (List<EventReceiver> eventReceiverList : eventReceiverListMap.values()) {
                for (EventReceiver eventReceiver : eventReceiverList) {
                    eventReceiverConfigurations.add(eventReceiver.getEventReceiverConfiguration());
                }
            }
        }
        return eventReceiverConfigurations;
    }

    @Override
    public List<EventReceiverConfiguration> getAllStreamSpecificActiveEventReceiverConfigurations(
            String streamId, int tenantId) {
        List<EventReceiverConfiguration> eventReceiverConfigurations = new ArrayList<EventReceiverConfiguration>();
        Map<String, List<EventReceiver>> eventReceiverListMap = this.tenantSpecificEventReceiverMap.get(tenantId);
        if (eventReceiverListMap != null) {
            for (List<EventReceiver> eventReceiverList : eventReceiverListMap.values()) {
                for (EventReceiver eventReceiver : eventReceiverList) {
                    String streamWithVersion = eventReceiver.getExportedStreamDefinition().getStreamId();
                    if (streamWithVersion.equals(streamId)) {
                        eventReceiverConfigurations.add(eventReceiver.getEventReceiverConfiguration());
                    }
                }
            }
        }
        return eventReceiverConfigurations;
    }

    @Override
    public EventReceiverConfiguration getActiveEventReceiverConfiguration(String eventReceiverName,
                                                                        int tenantId) {
        EventReceiverConfiguration eventReceiverConfiguration = null;
        Map<String, List<EventReceiver>> eventReceiverListMap = this.tenantSpecificEventReceiverMap.get(tenantId);
        if (eventReceiverListMap != null) {
            boolean foundEventReceiver = false;
            Iterator<List<EventReceiver>> eventReceiverListIterator = eventReceiverListMap.values().iterator();
            while (eventReceiverListIterator.hasNext() && !foundEventReceiver) {
                List<EventReceiver> eventReceiverList = eventReceiverListIterator.next();
                for (EventReceiver eventReceiver : eventReceiverList) {
                    if (eventReceiver.getEventReceiverConfiguration().getEventReceiverName().equals(eventReceiverName)) {
                        eventReceiverConfiguration = eventReceiver.getEventReceiverConfiguration();
                        foundEventReceiver = true;
                        break;
                    }
                }
            }
        }

        return eventReceiverConfiguration;
    }

//    @Override
//    public List<String> getSupportedInputMappingTypes(String eventAdaptorName, int tenantId) {
//        List<String> supportedInputMappingTypes = new ArrayList<String>();
//        InputEventAdaptorService InputEventAdaptorService = EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService();
//        String eventAdaptorType = null;
//        try {
//            InputEventAdaptorConfiguration InputEventAdaptorConfiguration =
//                    InputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(eventAdaptorName, tenantId);
//            eventAdaptorType = InputEventAdaptorConfiguration.getType();
//        } catch (InputEventAdaptorManagerConfigurationException e) {
//            log.error("Error while trying to retrieve supported input mapping types.", e);
//        }
//        if (eventAdaptorType != null) {
//            InputEventAdaptorDto InputEventAdaptorDto = InputEventAdaptorService.getEventAdaptorDto(eventAdaptorType);
//            if (InputEventAdaptorDto != null) {
//                for (String messageType : InputEventAdaptorDto.getSupportedMessageTypes()) {
//                    supportedInputMappingTypes.add(EventReceiverConstants.MESSAGE_TYPE_STRING_MAP.get(messageType));
//                }
//            }
//        }
//        return supportedInputMappingTypes;
//    }

    @Override
    public void deployEventReceiverConfiguration(EventReceiverConfiguration eventReceiverConfiguration,
                                                AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {

        String filePath = EventReceiverUtil.generateFilePath(eventReceiverConfiguration, axisConfiguration);
        OMElement omElement = EventReceiverConfigBuilder.eventReceiverConfigurationToOM(eventReceiverConfiguration);
        ConfigurationValidator.validateEventReceiverConfiguration(omElement);
        String mappingType = EventReceiverConfigHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            validateToRemoveInactiveEventReceiverConfiguration(eventReceiverConfiguration.getEventReceiverName(), axisConfiguration);
            EventReceiverConfigurationFileSystemInvoker.save(omElement, new File(filePath).getName(), axisConfiguration);
        } else {
            throw new EventReceiverConfigurationException("Mapping type of the Event Builder " + eventReceiverConfiguration.getEventReceiverName() + " cannot be null");
        }

    }

    @Override
    public void deployEventReceiverConfiguration(String eventReceiverConfigXml, AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(eventReceiverConfigXml);
        } catch (XMLStreamException e) {
            throw new EventReceiverConfigurationException("Error parsing XML for event builder configuration.");
        }
        ConfigurationValidator.validateEventReceiverConfiguration(omElement);
        String eventReceiverName = EventReceiverConfigHelper.getEventReceiverName(omElement);
        String filePath = EventReceiverUtil.generateFilePath(eventReceiverName);
        String mappingType = EventReceiverConfigHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            validateToRemoveInactiveEventReceiverConfiguration(eventReceiverName, axisConfiguration);
            EventReceiverConfigurationFileSystemInvoker.save(omElement, new File(filePath).getName(), axisConfiguration);
        } else {
            throw new EventReceiverConfigurationException("Mapping type of the Event Builder " + eventReceiverName + " cannot be null");
        }

    }

    @Override
    public void deployEventReceiverConfiguration(EventReceiverConfiguration eventReceiverConfiguration)
            throws EventReceiverConfigurationException {

        String filePath = EventReceiverUtil.generateFilePath(eventReceiverConfiguration);
        OMElement omElement = EventReceiverConfigBuilder.eventReceiverConfigurationToOM(eventReceiverConfiguration);
        ConfigurationValidator.validateEventReceiverConfiguration(omElement);
        String mappingType = EventReceiverConfigHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            AxisConfiguration axisConfiguration;
            if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
                axisConfiguration = EventReceiverServiceValueHolder.getConfigurationContextService().
                        getServerConfigContext().getAxisConfiguration();
            } else {
                axisConfiguration = TenantAxisUtils.getTenantAxisConfiguration(CarbonContext.
                                getThreadLocalCarbonContext().getTenantDomain(),
                        EventReceiverServiceValueHolder.getConfigurationContextService().
                                getServerConfigContext());
            }
            EventReceiverConfigurationFileSystemInvoker.saveAndDeploy(omElement.toString(),
                    new File(filePath).getName(), axisConfiguration);
        } else {
            throw new EventReceiverConfigurationException("Mapping type of the Event Builder " + eventReceiverConfiguration.getEventReceiverName() + " cannot be null");
        }

    }

    @Override
    public void setTraceEnabled(String eventReceiverName, boolean traceEnabled,
                                AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverConfiguration eventReceiverConfiguration = getActiveEventReceiverConfiguration(eventReceiverName, tenantId);
        eventReceiverConfiguration.setTraceEnabled(traceEnabled);
        String ebConfigXml = EventReceiverConfigBuilder.eventReceiverConfigurationToOM(eventReceiverConfiguration).toString();
        editActiveEventReceiverConfiguration(ebConfigXml, eventReceiverName, axisConfiguration);
    }

    @Override
    public void setStatisticsEnabled(String eventReceiverName, boolean statisticsEnabled,
                                     AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverConfiguration eventReceiverConfiguration = getActiveEventReceiverConfiguration(eventReceiverName, tenantId);
        eventReceiverConfiguration.setStatisticsEnabled(statisticsEnabled);
        String ebConfigXml = EventReceiverConfigBuilder.eventReceiverConfigurationToOM(eventReceiverConfiguration).toString();
        editActiveEventReceiverConfiguration(ebConfigXml, eventReceiverName, axisConfiguration);
    }

    @Override
    public String getEventReceiverStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
        if (eventReceiverConfigurationFileList != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFileList) {
                if (filename != null && filename.equals(eventReceiverConfigurationFile.getFileName())) {
                    String statusMsg = eventReceiverConfigurationFile.getDeploymentStatusMessage();
                    if (eventReceiverConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + eventReceiverConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }

        return EventReceiverConstants.NO_DEPENDENCY_INFO_MSG;
    }


    //TODO Fix this properly
    @Override
    public void deployDefaultEventReceiver(String streamId,
                                          AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
//        try {
//            InputEventAdaptorManagerService inputEventAdaptorManagerService = EventReceiverServiceValueHolder.getInputEventAdaptorManagerService();
//            String transportAdaptorName = inputEventAdaptorManagerService.getDefaultWso2EventAdaptor(axisConfiguration);
//            EventReceiverConfiguration defaultEventReceiverConfiguration =
//                    EventReceiverUtil.createDefaultEventReceiver(streamId, transportAdaptorName);
//            String filename = defaultEventReceiverConfiguration.getEventReceiverName() + EventReceiverConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
//            if (!EventReceiverConfigurationFileSystemInvoker.isFileExists(filename)) {
//                deployEventReceiverConfiguration(defaultEventReceiverConfiguration, axisConfiguration);
//            }
//        } catch (InputEventAdaptorManagerConfigurationException e) {
//            throw new EventReceiverConfigurationException("Error retrieving default WSO2 event adaptor :" + e.getMessage(), e);
//        } catch (EventReceiverConfigurationException e) {
//            throw new EventReceiverConfigurationException("Error deploying default event builder for stream :" + streamId, e);
//        }
    }

//    public void saveDefaultEventReceiver(String streamId, int tenantId) throws EventReceiverConfigurationException {
//        try {
//            InputEventAdaptorManagerService inputEventAdaptorManagerService = EventReceiverServiceValueHolder.getInputEventAdaptorManagerService();
//            String defaultWso2EventAdaptorName = inputEventAdaptorManagerService.getDefaultWso2EventAdaptor();
//            InputEventAdaptorConfiguration defaultInputEventAdaptor = inputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(defaultWso2EventAdaptorName, tenantId);
//            if (defaultInputEventAdaptor != null) {
//                EventReceiverConfiguration defaultEventReceiverConfiguration =
//                        EventReceiverUtil.createDefaultEventReceiver(streamId, defaultWso2EventAdaptorName);
//
//                String filename = defaultEventReceiverConfiguration.getEventReceiverName() + EventReceiverConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
//
//                if (!EventReceiverConfigurationFileSystemInvoker.isFileExists(filename)) {
//                    for (EventReceiverConfiguration eventReceiverConfiguration : getAllActiveEventReceiverConfigurations(tenantId)) {
//
//                        //TODO - We have to decide about default builder handeling -- If there is a builder which send events to the specific stream then there is no default builder created
//                        //eventReceiverConfiguration.getInputEventAdaptorConfiguration().getInputEventAdaptorName().equals(defaultWso2EventAdaptorName) &&
//                        if ((eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion()).equals(streamId)) {
//                            log.info("Skipping defining default event builder " + defaultEventReceiverConfiguration.getEventReceiverName() + " as " + eventReceiverConfiguration.getEventReceiverName() + " already exist");
//                            return;
//                        }
//                    }
//                    deployEventReceiverConfiguration(defaultEventReceiverConfiguration);
//                }
//            }
//
//        } catch (InputEventAdaptorManagerConfigurationException e) {
//            throw new EventReceiverConfigurationException("Error retrieving default WSO2 event adaptor :" + e.getMessage(), e);
//        } catch (EventReceiverConfigurationException e) {
//            throw new EventReceiverConfigurationException("Error deploying default event builder for stream :" + streamId, e);
//        }
//    }

    @Override
    public String getActiveEventReceiverConfigurationContent(String eventReceiverName,
                                                            AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventReceiverName);
        return EventReceiverConfigurationFileSystemInvoker.readEventReceiverConfigurationFile(fileName, axisConfiguration);

    }

    @Override
    public List<EventReceiverConfigurationFile> getAllInactiveEventReceiverConfigurations(
            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = this.tenantSpecificEventReceiverConfigFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles != null) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = new ArrayList<EventReceiverConfigurationFile>();
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                if (eventReceiverConfigurationFile.getDeploymentStatus() != EventReceiverConfigurationFile.DeploymentStatus.DEPLOYED) {
                    eventReceiverConfigurationFileList.add(eventReceiverConfigurationFile);
                }
            }
            return eventReceiverConfigurationFileList;
        }

        return null;
    }

    @Override
    public String getInactiveEventReceiverConfigurationContent(String fileName,
                                                              AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        return EventReceiverConfigurationFileSystemInvoker.readEventReceiverConfigurationFile(fileName, axisConfiguration);
    }

    @Override
    public void editInactiveEventReceiverConfiguration(
            String eventReceiverConfiguration,
            String filename,
            AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {

        editEventReceiverConfiguration(filename, axisConfiguration, eventReceiverConfiguration, null);
    }

    @Override
    public void editActiveEventReceiverConfiguration(String eventReceiverConfiguration,
                                                    String eventReceiverName,
                                                    AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventReceiverName);
        if (fileName == null) {
            fileName = eventReceiverName + EventReceiverConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
        }
        editEventReceiverConfiguration(fileName, axisConfiguration, eventReceiverConfiguration, eventReceiverName);

    }

    private void editEventReceiverConfiguration(String filename,
                                               AxisConfiguration axisConfiguration,
                                               String eventReceiverConfigXml,
                                               String originalEventReceiverName)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventReceiverConfigXml);
            if (getActiveEventReceiverConfiguration(originalEventReceiverName, tenantId) != null) {
                ConfigurationValidator.validateEventReceiverConfiguration(omElement);
            }
            String mappingType = EventReceiverConfigHelper.getInputMappingType(omElement);
            if (mappingType != null) {
                EventReceiverConfiguration eventReceiverConfigurationObject = EventReceiverConfigBuilder.getEventReceiverConfiguration(omElement, mappingType, tenantId);
                if (!(eventReceiverConfigurationObject.getEventReceiverName().equals(originalEventReceiverName))) {
                    if (!isEventReceiverAlreadyExists(tenantId, eventReceiverConfigurationObject.getEventReceiverName())) {
                        EventReceiverConfigurationFileSystemInvoker.delete(filename, axisConfiguration);
                        EventReceiverConfigurationFileSystemInvoker.save(omElement, filename, axisConfiguration);
                    } else {
                        throw new EventReceiverConfigurationException("There is already an Event Builder " + eventReceiverConfigurationObject.getEventReceiverName() + " with the same name");
                    }
                } else {
                    EventReceiverConfigurationFileSystemInvoker.delete(filename, axisConfiguration);
                    EventReceiverConfigurationFileSystemInvoker.save(omElement, filename, axisConfiguration);
                }
            } else {
                throw new EventReceiverConfigurationException("Mapping type of the Event Builder " + originalEventReceiverName + " cannot be null");
            }
        } catch (XMLStreamException e) {
            String errMsg = "Error while creating the XML object";
            log.error(errMsg);
            throw new EventReceiverConfigurationException(errMsg + ":" + e.getMessage(), e);
        }
    }

    private EventReceiverConfigurationFile createEventReceiverConfigurationFile(
            String eventReceiverName, File file,
            EventReceiverConfigurationFile.DeploymentStatus status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage, String dependency, String streamNameWithVersion,
            OMElement ebConfigElement) {
        EventReceiverConfigurationFile eventReceiverConfigurationFile = new EventReceiverConfigurationFile(file.getName());
        eventReceiverConfigurationFile.setFilePath(file.getAbsolutePath());
        eventReceiverConfigurationFile.setEventReceiverName(eventReceiverName);
        eventReceiverConfigurationFile.setDeploymentStatus(status);
        eventReceiverConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventReceiverConfigurationFile.setDependency(dependency);
        eventReceiverConfigurationFile.setAxisConfiguration(axisConfiguration);
        eventReceiverConfigurationFile.setStreamWithVersion(streamNameWithVersion);
        eventReceiverConfigurationFile.setEbConfigOmElement(ebConfigElement);
        return eventReceiverConfigurationFile;
    }


    public void activateInactiveEventReceiverConfigurationsForAdaptor(String eventAdaptorType)
            throws EventReceiverConfigurationException {
        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();

        if (tenantSpecificEventReceiverConfigFileMap != null && tenantSpecificEventReceiverConfigFileMap.size() > 0) {

            Iterator<List<EventReceiverConfigurationFile>> eventReceiverConfigurationFileIterator = tenantSpecificEventReceiverConfigFileMap.values().iterator();
            while (eventReceiverConfigurationFileIterator.hasNext()) {
                List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = eventReceiverConfigurationFileIterator.next();
                if (eventReceiverConfigurationFileList != null) {
                    for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFileList) {
                        if ((eventReceiverConfigurationFile.getDeploymentStatus().equals(EventReceiverConfigurationFile.DeploymentStatus.WAITING_FOR_DEPENDENCY)) && eventReceiverConfigurationFile.getDependency().equalsIgnoreCase(eventAdaptorType)) {
                            fileList.add(eventReceiverConfigurationFile);
                        }
                    }
                }
            }
        }

        for (EventReceiverConfigurationFile builderConfigurationFile : fileList) {
            try {
                EventReceiverConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event Builder configuration file : " + builderConfigurationFile.getFileName(), e);
            }
        }
    }

    public void activateInactiveEventReceiverConfigurationsForStream(String streamNameWithVersion,
                                                                    int tenantId) throws EventReceiverConfigurationException {
        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();

        if (tenantSpecificEventReceiverConfigFileMap != null && tenantSpecificEventReceiverConfigFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);

            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if (EventReceiverConfigurationFile.DeploymentStatus.WAITING_FOR_STREAM_DEPENDENCY.equals(eventReceiverConfigurationFile.getDeploymentStatus())
                            && streamNameWithVersion.equalsIgnoreCase(eventReceiverConfigurationFile.getDependency())) {
                        fileList.add(eventReceiverConfigurationFile);
                    }
                }
            }
        }
        for (EventReceiverConfigurationFile builderConfigurationFile : fileList) {
            try {
                EventReceiverConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event Builder configuration file : " + builderConfigurationFile.getFileName(), e);
            }
        }
    }


    //TODO this code segment is not required.

//    public void deactivateActiveEventReceiverConfigurationsForAdaptor(
//            InputEventAdaptorConfiguration inputEventAdaptorConfiguration, int tenantId)
//            throws EventReceiverConfigurationException {
//
//        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();
//        if (tenantSpecificEventReceiverMap != null && tenantSpecificEventReceiverMap.size() > 0) {
//            Map<String, List<EventReceiver>> eventReceiverMap = tenantSpecificEventReceiverMap.get(tenantId);
//            if (eventReceiverMap != null) {
//                for (List<EventReceiver> eventReceiverList : eventReceiverMap.values()) {
//                    for (EventReceiver eventReceiver : eventReceiverList) {
//                        if (eventReceiver.getEventReceiverConfiguration().getInputEventAdaptorConfiguration().getInputEventAdaptorType().equals(inputEventAdaptorConfiguration.getName())) {
//                            EventReceiverConfigurationFile builderConfigurationFile = getEventReceiverConfigurationFile(eventReceiver.getEventReceiverConfiguration().getEventReceiverName(), tenantId);
//                            if (builderConfigurationFile != null) {
//                                fileList.add(builderConfigurationFile);
//                                // We unsubscribe here since carrying the inputAdaptorConfiguration through
//                                // numerous methods would not be clean. Add the remove process, when event builder
//                                // attempts to unsubscribe, the call would actually be avoided since
//                                // event builder keeps track of the subscription id which will be set to null
//                                // upon unsubscribing.
//                                eventReceiver.unsubscribeFromEventAdaptor(inputEventAdaptorConfiguration);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        for (EventReceiverConfigurationFile builderConfigurationFile : fileList) {
//            EventReceiverConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
//            log.info("Event builder : " + builderConfigurationFile.getEventReceiverName() + " in inactive state because dependency could not be found : " + inputEventAdaptorConfiguration.getName());
//        }
//    }

    public void deactivateActiveEventReceiverConfigurationsForStream(
            String streamNameWithVersion, int tenantId)
            throws EventReceiverConfigurationException {

        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();
        if (tenantSpecificEventReceiverMap != null && tenantSpecificEventReceiverMap.size() > 0) {
            Map<String, List<EventReceiver>> eventReceiverMap = tenantSpecificEventReceiverMap.get(tenantId);
            if (eventReceiverMap != null) {
                for (List<EventReceiver> eventReceiverList : eventReceiverMap.values()) {
                    for (EventReceiver eventReceiver : eventReceiverList) {
                        EventReceiverConfiguration eventReceiverConfiguration = eventReceiver.getEventReceiverConfiguration();
                        String stream = EventReceiverUtil.getExportedStreamIdFrom(eventReceiverConfiguration);
                        if (streamNameWithVersion.equals(stream)) {
                            EventReceiverConfigurationFile builderConfigurationFile =
                                    getEventReceiverConfigurationFile(eventReceiver.getEventReceiverConfiguration().getEventReceiverName(), tenantId);
                            if (builderConfigurationFile != null) {
                                fileList.add(builderConfigurationFile);
                            }
                        }
                    }
                }
            }
        }
        for (EventReceiverConfigurationFile builderConfigurationFile : fileList) {
            EventReceiverConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            log.info("Event builder : " + builderConfigurationFile.getEventReceiverName() + " in inactive state because event stream dependency  could not be found : " + streamNameWithVersion);
        }
    }

    private String getFileName(int tenantId, String eventReceiverName) {

        if (tenantSpecificEventReceiverConfigFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if ((eventReceiverConfigurationFile.getEventReceiverName().equals(eventReceiverName))
                            && eventReceiverConfigurationFile.getDeploymentStatus().equals(EventReceiverConfigurationFile.DeploymentStatus.DEPLOYED)) {
                        return new File(eventReceiverConfigurationFile.getFileName()).getName();
                    }
                }
            }
        }
        return null;
    }

    public boolean isEventReceiverAlreadyExists(int tenantId, String eventReceiverName) {

        if (tenantSpecificEventReceiverConfigFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if ((eventReceiverConfigurationFile.getEventReceiverName().equals(eventReceiverName))
                            && (eventReceiverConfigurationFile.getDeploymentStatus().equals(EventReceiverConfigurationFile.DeploymentStatus.DEPLOYED))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private EventReceiverConfigurationFile getEventReceiverConfigurationFile(
            String eventReceiverName, int tenantId) {
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                if (eventReceiverConfigurationFile.getEventReceiverName().equals(eventReceiverName)) {
                    return eventReceiverConfigurationFile;
                }
            }
        }
        return null;

    }

    private void validateToRemoveInactiveEventReceiverConfiguration(String eventReceiverName,
                                                                   AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = eventReceiverName + EventReceiverConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                if ((eventReceiverConfigurationFile.getFileName().equals(fileName))) {
                    if (!(eventReceiverConfigurationFile.getDeploymentStatus().equals(EventReceiverConfigurationFile.DeploymentStatus.DEPLOYED))) {
                        EventReceiverConfigurationFileSystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

    public boolean isEventReceiverFileAlreadyExist(String eventReceiverFileName, int tenantId) {
        if (tenantSpecificEventReceiverConfigFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigFileMap.get(tenantId);
            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if ((eventReceiverConfigurationFile.getFileName().equals(eventReceiverFileName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
