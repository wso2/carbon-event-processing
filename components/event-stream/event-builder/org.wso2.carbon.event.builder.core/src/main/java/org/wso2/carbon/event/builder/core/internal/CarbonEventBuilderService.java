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
package org.wso2.carbon.event.builder.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfigurationFile;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderStreamValidationException;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigurationFileSystemInvoker;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorDto;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventBuilderService implements EventBuilderService {

    private static final Log log = LogFactory.getLog(CarbonEventBuilderService.class);
    private Map<Integer, Map<String, List<EventBuilder>>> tenantSpecificEventBuilderMap;
    private Map<Integer, List<EventBuilderConfigurationFile>> tenantSpecificEventBuilderConfigFileMap;

    public CarbonEventBuilderService() {
        tenantSpecificEventBuilderMap = new ConcurrentHashMap<Integer, Map<String, List<EventBuilder>>>();
        tenantSpecificEventBuilderConfigFileMap = new ConcurrentHashMap<Integer, List<EventBuilderConfigurationFile>>();
    }

    @Override
    public void undeployActiveEventBuilderConfiguration(String eventBuilderName,
                                                        AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventBuilderName);
        if (fileName != null) {
            EventBuilderConfigurationFileSystemInvoker.delete(fileName, axisConfiguration);
        } else {
            throw new EventBuilderConfigurationException("Couldn't undeploy the Event Builder configuration : " + eventBuilderName);
        }

    }

    @Override
    public void undeployInactiveEventBuilderConfiguration(String filename,
                                                          AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        EventBuilderConfigurationFileSystemInvoker.delete(filename, axisConfiguration);

    }

    public void removeEventBuilder(String eventBuilderName,
                                   int tenantId)
            throws EventBuilderConfigurationException {
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);

        EventBuilderConfiguration eventBuilderConfiguration = getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        if (eventBuilderListMap != null) {
            int removedCount = 0;
            String exportedStreamDefinitionId = EventBuilderUtil.getExportedStreamIdFrom(eventBuilderConfiguration);
            List<EventBuilder> eventBuilderList = eventBuilderListMap.get(exportedStreamDefinitionId);
            Iterator<EventBuilder> eventBuilderIterator = eventBuilderList.iterator();
            while (eventBuilderIterator.hasNext()) {
                EventBuilder eventBuilder = eventBuilderIterator.next();
                if (eventBuilder.getEventBuilderConfiguration().getEventBuilderName().equals(eventBuilderConfiguration.getEventBuilderName())) {
                    eventBuilder.unsubscribeFromEventAdaptor(null);
                    EventBuilderServiceValueHolder.getEventStreamService().unsubscribe(eventBuilder, tenantId);
                    eventBuilderIterator.remove();
                    removedCount++;
                }
            }
            if (removedCount == 0) {
                throw new EventBuilderConfigurationException("Could not find the specified event builder '"
                        + eventBuilderConfiguration.getEventBuilderName() + "' for removal for the given axis configuration");
            }
        }
    }

    public void addEventBuilder(EventBuilderConfiguration eventBuilderConfiguration,
                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        // Start: Checking preconditions to add the event builder
        StreamDefinition exportedStreamDefinition = null;
        try {
            exportedStreamDefinition = EventBuilderServiceValueHolder.getEventStreamService().getStreamDefinition(
                    eventBuilderConfiguration.getToStreamName(), eventBuilderConfiguration.getToStreamVersion(), tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventBuilderConfigurationException("Error while retrieving stream definition for stream " + eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion() + " from store", e);
        }

        if (exportedStreamDefinition == null) {
            throw new EventBuilderStreamValidationException("Stream " + eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion() + " does not exist",
                    eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion()
            );
        }
        Map<String, List<EventBuilder>> eventBuilderListMap
                = tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap == null) {
            eventBuilderListMap = new ConcurrentHashMap<String, List<EventBuilder>>();
            tenantSpecificEventBuilderMap.put(tenantId, eventBuilderListMap);
        }

        List<EventBuilder> eventBuilderList = eventBuilderListMap.get(exportedStreamDefinition.getStreamId());
        if (eventBuilderList == null) {
            eventBuilderList = new ArrayList<EventBuilder>();
            eventBuilderListMap.put(exportedStreamDefinition.getStreamId(), eventBuilderList);
        }
        // End; Checking preconditions to add the event builder
        EventBuilder eventBuilder = new EventBuilder(eventBuilderConfiguration, exportedStreamDefinition, axisConfiguration);
        eventBuilder.subscribeToEventAdaptor();
        try {
            EventBuilderServiceValueHolder.getEventStreamService().subscribe(eventBuilder, tenantId);
        } catch (EventStreamConfigurationException e) {
            //ignored as this is already checked
        }

        eventBuilderList.add(eventBuilder);
    }

    public void addEventBuilderConfigurationFile(String eventBuilderName,
                                                 File file,
                                                 EventBuilderConfigurationFile.DeploymentStatus status,
                                                 String deploymentStatusMessage, String dependency,
                                                 String streamNameWithVersion,
                                                 OMElement ebConfigElement,
                                                 AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFiles == null) {
            eventBuilderConfigurationFiles = new ArrayList<EventBuilderConfigurationFile>();
            tenantSpecificEventBuilderConfigFileMap.put(tenantId, eventBuilderConfigurationFiles);
        }
        EventBuilderConfigurationFile eventBuilderConfigurationFile = createEventBuilderConfigurationFile(
                eventBuilderName, file, status, axisConfiguration, deploymentStatusMessage, dependency,
                streamNameWithVersion, ebConfigElement);
        eventBuilderConfigurationFiles.add(eventBuilderConfigurationFile);
    }

    public void removeEventBuilderConfigurationFile(String fileName, int tenantId)
            throws EventBuilderConfigurationException {

        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList =
                tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileList != null) {
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileList) {
                if ((eventBuilderConfigurationFile.getFileName().equals(fileName))) {
                    if (eventBuilderConfigurationFile.getDeploymentStatus().
                            equals(EventBuilderConfigurationFile.DeploymentStatus.DEPLOYED)) {
                        String eventBuilderName = eventBuilderConfigurationFile.getEventBuilderName();
                        removeEventBuilder(eventBuilderName, tenantId);
                    }
                    eventBuilderConfigurationFileList.remove(eventBuilderConfigurationFile);
                    return;
                }
            }
        }

    }

    @Override
    public List<EventBuilderConfiguration> getAllActiveEventBuilderConfigurations(int tenantId) {
        List<EventBuilderConfiguration> eventBuilderConfigurations = new ArrayList<EventBuilderConfiguration>();
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap != null) {
            for (List<EventBuilder> eventBuilderList : eventBuilderListMap.values()) {
                for (EventBuilder eventBuilder : eventBuilderList) {
                    eventBuilderConfigurations.add(eventBuilder.getEventBuilderConfiguration());
                }
            }
        }
        return eventBuilderConfigurations;
    }

    @Override
    public List<EventBuilderConfiguration> getAllStreamSpecificActiveEventBuilderConfigurations(
            String streamId, int tenantId) {
        List<EventBuilderConfiguration> eventBuilderConfigurations = new ArrayList<EventBuilderConfiguration>();
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap != null) {
            for (List<EventBuilder> eventBuilderList : eventBuilderListMap.values()) {
                for (EventBuilder eventBuilder : eventBuilderList) {
                    String streamWithVersion = eventBuilder.getExportedStreamDefinition().getStreamId();
                    if (streamWithVersion.equals(streamId)) {
                        eventBuilderConfigurations.add(eventBuilder.getEventBuilderConfiguration());
                    }
                }
            }
        }
        return eventBuilderConfigurations;
    }

    @Override
    public EventBuilderConfiguration getActiveEventBuilderConfiguration(String eventBuilderName,
                                                                        int tenantId) {
        EventBuilderConfiguration eventBuilderConfiguration = null;
        Map<String, List<EventBuilder>> eventBuilderListMap = this.tenantSpecificEventBuilderMap.get(tenantId);
        if (eventBuilderListMap != null) {
            boolean foundEventBuilder = false;
            Iterator<List<EventBuilder>> eventBuilderListIterator = eventBuilderListMap.values().iterator();
            while (eventBuilderListIterator.hasNext() && !foundEventBuilder) {
                List<EventBuilder> eventBuilderList = eventBuilderListIterator.next();
                for (EventBuilder eventBuilder : eventBuilderList) {
                    if (eventBuilder.getEventBuilderConfiguration().getEventBuilderName().equals(eventBuilderName)) {
                        eventBuilderConfiguration = eventBuilder.getEventBuilderConfiguration();
                        foundEventBuilder = true;
                        break;
                    }
                }
            }
        }

        return eventBuilderConfiguration;
    }

    @Override
    public List<String> getSupportedInputMappingTypes(String eventAdaptorName, int tenantId) {
        List<String> supportedInputMappingTypes = new ArrayList<String>();
        InputEventAdaptorService InputEventAdaptorService = EventBuilderServiceValueHolder.getInputEventAdaptorService();
        InputEventAdaptorManagerService InputEventAdaptorManagerService = EventBuilderServiceValueHolder.getInputEventAdaptorManagerService();
        String eventAdaptorType = null;
        try {
            InputEventAdaptorConfiguration InputEventAdaptorConfiguration =
                    InputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(eventAdaptorName, tenantId);
            eventAdaptorType = InputEventAdaptorConfiguration.getType();
        } catch (InputEventAdaptorManagerConfigurationException e) {
            log.error("Error while trying to retrieve supported input mapping types.", e);
        }
        if (eventAdaptorType != null) {
            InputEventAdaptorDto InputEventAdaptorDto = InputEventAdaptorService.getEventAdaptorDto(eventAdaptorType);
            if (InputEventAdaptorDto != null) {
                for (String messageType : InputEventAdaptorDto.getSupportedMessageTypes()) {
                    supportedInputMappingTypes.add(EventBuilderConstants.MESSAGE_TYPE_STRING_MAP.get(messageType));
                }
            }
        }
        return supportedInputMappingTypes;
    }

    @Override
    public void deployEventBuilderConfiguration(EventBuilderConfiguration eventBuilderConfiguration,
                                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {

        String filePath = EventBuilderUtil.generateFilePath(eventBuilderConfiguration, axisConfiguration);
        OMElement omElement = EventBuilderConfigBuilder.eventBuilderConfigurationToOM(eventBuilderConfiguration);
        ConfigurationValidator.validateEventBuilderConfiguration(omElement);
        String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            validateToRemoveInactiveEventBuilderConfiguration(eventBuilderConfiguration.getEventBuilderName(), axisConfiguration);
            EventBuilderConfigurationFileSystemInvoker.save(omElement, new File(filePath).getName(), axisConfiguration);
        } else {
            throw new EventBuilderConfigurationException("Mapping type of the Event Builder " + eventBuilderConfiguration.getEventBuilderName() + " cannot be null");
        }

    }

    @Override
    public void deployEventBuilderConfiguration(String eventBuilderConfigXml, AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(eventBuilderConfigXml);
        } catch (XMLStreamException e) {
            throw new EventBuilderConfigurationException("Error parsing XML for event builder configuration.");
        }
        ConfigurationValidator.validateEventBuilderConfiguration(omElement);
        String eventBuilderName = EventBuilderConfigHelper.getEventBuilderName(omElement);
        String filePath = EventBuilderUtil.generateFilePath(eventBuilderName);
        String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            validateToRemoveInactiveEventBuilderConfiguration(eventBuilderName, axisConfiguration);
            EventBuilderConfigurationFileSystemInvoker.save(omElement, new File(filePath).getName(), axisConfiguration);
        } else {
            throw new EventBuilderConfigurationException("Mapping type of the Event Builder " + eventBuilderName + " cannot be null");
        }

    }

    @Override
    public void deployEventBuilderConfiguration(EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderConfigurationException {

        String filePath = EventBuilderUtil.generateFilePath(eventBuilderConfiguration);
        OMElement omElement = EventBuilderConfigBuilder.eventBuilderConfigurationToOM(eventBuilderConfiguration);
        ConfigurationValidator.validateEventBuilderConfiguration(omElement);
        String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            AxisConfiguration axisConfiguration;
            if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
                axisConfiguration = EventBuilderServiceValueHolder.getConfigurationContextService().
                        getServerConfigContext().getAxisConfiguration();
            } else {
                axisConfiguration = TenantAxisUtils.getTenantAxisConfiguration(CarbonContext.
                                getThreadLocalCarbonContext().getTenantDomain(),
                        EventBuilderServiceValueHolder.getConfigurationContextService().
                                getServerConfigContext());
            }
            EventBuilderConfigurationFileSystemInvoker.saveAndDeploy(omElement.toString(),
                    new File(filePath).getName(), axisConfiguration);
        } else {
            throw new EventBuilderConfigurationException("Mapping type of the Event Builder " + eventBuilderConfiguration.getEventBuilderName() + " cannot be null");
        }

    }

    @Override
    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled,
                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderConfiguration eventBuilderConfiguration = getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        eventBuilderConfiguration.setTraceEnabled(traceEnabled);
        String ebConfigXml = EventBuilderConfigBuilder.eventBuilderConfigurationToOM(eventBuilderConfiguration).toString();
        editActiveEventBuilderConfiguration(ebConfigXml, eventBuilderName, axisConfiguration);
    }

    @Override
    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled,
                                     AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderConfiguration eventBuilderConfiguration = getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        eventBuilderConfiguration.setStatisticsEnabled(statisticsEnabled);
        String ebConfigXml = EventBuilderConfigBuilder.eventBuilderConfigurationToOM(eventBuilderConfiguration).toString();
        editActiveEventBuilderConfiguration(ebConfigXml, eventBuilderName, axisConfiguration);
    }

    @Override
    public String getEventBuilderStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFileList != null) {
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileList) {
                if (filename != null && filename.equals(eventBuilderConfigurationFile.getFileName())) {
                    String statusMsg = eventBuilderConfigurationFile.getDeploymentStatusMessage();
                    if (eventBuilderConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + eventBuilderConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }

        return EventBuilderConstants.NO_DEPENDENCY_INFO_MSG;
    }

    @Override
    public void deployDefaultEventBuilder(String streamId,
                                          AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        try {
            InputEventAdaptorManagerService inputEventAdaptorManagerService = EventBuilderServiceValueHolder.getInputEventAdaptorManagerService();
            String transportAdaptorName = inputEventAdaptorManagerService.getDefaultWso2EventAdaptor(axisConfiguration);
            EventBuilderConfiguration defaultEventBuilderConfiguration =
                    EventBuilderUtil.createDefaultEventBuilder(streamId, transportAdaptorName);
            String filename = defaultEventBuilderConfiguration.getEventBuilderName() + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
            if (!EventBuilderConfigurationFileSystemInvoker.isFileExists(filename)) {
                deployEventBuilderConfiguration(defaultEventBuilderConfiguration, axisConfiguration);
            }
        } catch (InputEventAdaptorManagerConfigurationException e) {
            throw new EventBuilderConfigurationException("Error retrieving default WSO2 event adaptor :" + e.getMessage(), e);
        } catch (EventBuilderConfigurationException e) {
            throw new EventBuilderConfigurationException("Error deploying default event builder for stream :" + streamId, e);
        }
    }

    public void saveDefaultEventBuilder(String streamId, int tenantId) throws EventBuilderConfigurationException {
        try {
            InputEventAdaptorManagerService inputEventAdaptorManagerService = EventBuilderServiceValueHolder.getInputEventAdaptorManagerService();
            String defaultWso2EventAdaptorName = inputEventAdaptorManagerService.getDefaultWso2EventAdaptor();
            InputEventAdaptorConfiguration defaultInputEventAdaptor = inputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(defaultWso2EventAdaptorName, tenantId);
            if (defaultInputEventAdaptor != null) {
                EventBuilderConfiguration defaultEventBuilderConfiguration =
                        EventBuilderUtil.createDefaultEventBuilder(streamId, defaultWso2EventAdaptorName);

                String filename = defaultEventBuilderConfiguration.getEventBuilderName() + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;

                if (!EventBuilderConfigurationFileSystemInvoker.isFileExists(filename)) {
                    for (EventBuilderConfiguration eventBuilderConfiguration : getAllActiveEventBuilderConfigurations(tenantId)) {

                        //TODO - We have to decide about default builder handeling -- If there is a builder which send events to the specific stream then there is no default builder created
                        //eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName().equals(defaultWso2EventAdaptorName) &&
                        if ((eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion()).equals(streamId)) {
                            log.info("Skipping defining default event builder " + defaultEventBuilderConfiguration.getEventBuilderName() + " as " + eventBuilderConfiguration.getEventBuilderName() + " already exist");
                            return;
                        }
                    }
                    deployEventBuilderConfiguration(defaultEventBuilderConfiguration);
                }
            }

        } catch (InputEventAdaptorManagerConfigurationException e) {
            throw new EventBuilderConfigurationException("Error retrieving default WSO2 event adaptor :" + e.getMessage(), e);
        } catch (EventBuilderConfigurationException e) {
            throw new EventBuilderConfigurationException("Error deploying default event builder for stream :" + streamId, e);
        }
    }

    @Override
    public String getActiveEventBuilderConfigurationContent(String eventBuilderName,
                                                            AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventBuilderName);
        return EventBuilderConfigurationFileSystemInvoker.readEventBuilderConfigurationFile(fileName, axisConfiguration);

    }

    @Override
    public List<EventBuilderConfigurationFile> getAllInactiveEventBuilderConfigurations(
            AxisConfiguration axisConfiguration) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = this.tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFiles != null) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = new ArrayList<EventBuilderConfigurationFile>();
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                if (eventBuilderConfigurationFile.getDeploymentStatus() != EventBuilderConfigurationFile.DeploymentStatus.DEPLOYED) {
                    eventBuilderConfigurationFileList.add(eventBuilderConfigurationFile);
                }
            }
            return eventBuilderConfigurationFileList;
        }

        return null;
    }

    @Override
    public String getInactiveEventBuilderConfigurationContent(String fileName,
                                                              AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        return EventBuilderConfigurationFileSystemInvoker.readEventBuilderConfigurationFile(fileName, axisConfiguration);
    }

    @Override
    public void editInactiveEventBuilderConfiguration(
            String eventBuilderConfiguration,
            String filename,
            AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {

        editEventBuilderConfiguration(filename, axisConfiguration, eventBuilderConfiguration, null);
    }

    @Override
    public void editActiveEventBuilderConfiguration(String eventBuilderConfiguration,
                                                    String eventBuilderName,
                                                    AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventBuilderName);
        if (fileName == null) {
            fileName = eventBuilderName + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
        }
        editEventBuilderConfiguration(fileName, axisConfiguration, eventBuilderConfiguration, eventBuilderName);

    }

    private void editEventBuilderConfiguration(String filename,
                                               AxisConfiguration axisConfiguration,
                                               String eventBuilderConfigXml,
                                               String originalEventBuilderName)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventBuilderConfigXml);
            if (getActiveEventBuilderConfiguration(originalEventBuilderName, tenantId) != null) {
                ConfigurationValidator.validateEventBuilderConfiguration(omElement);
            }
            String mappingType = EventBuilderConfigHelper.getInputMappingType(omElement);
            if (mappingType != null) {
                EventBuilderConfiguration eventBuilderConfigurationObject = EventBuilderConfigBuilder.getEventBuilderConfiguration(omElement, mappingType, true, tenantId);
                if (!(eventBuilderConfigurationObject.getEventBuilderName().equals(originalEventBuilderName))) {
                    if (!isEventBuilderAlreadyExists(tenantId, eventBuilderConfigurationObject.getEventBuilderName())) {
                        EventBuilderConfigurationFileSystemInvoker.delete(filename, axisConfiguration);
                        EventBuilderConfigurationFileSystemInvoker.save(omElement, filename, axisConfiguration);
                    } else {
                        throw new EventBuilderConfigurationException("There is already an Event Builder " + eventBuilderConfigurationObject.getEventBuilderName() + " with the same name");
                    }
                } else {
                    EventBuilderConfigurationFileSystemInvoker.delete(filename, axisConfiguration);
                    EventBuilderConfigurationFileSystemInvoker.save(omElement, filename, axisConfiguration);
                }
            } else {
                throw new EventBuilderConfigurationException("Mapping type of the Event Builder " + originalEventBuilderName + " cannot be null");
            }
        } catch (XMLStreamException e) {
            String errMsg = "Error while creating the XML object";
            log.error(errMsg);
            throw new EventBuilderConfigurationException(errMsg + ":" + e.getMessage(), e);
        }
    }

    private EventBuilderConfigurationFile createEventBuilderConfigurationFile(
            String eventBuilderName, File file,
            EventBuilderConfigurationFile.DeploymentStatus status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage, String dependency, String streamNameWithVersion,
            OMElement ebConfigElement) {
        EventBuilderConfigurationFile eventBuilderConfigurationFile = new EventBuilderConfigurationFile(file.getName());
        eventBuilderConfigurationFile.setFilePath(file.getAbsolutePath());
        eventBuilderConfigurationFile.setEventBuilderName(eventBuilderName);
        eventBuilderConfigurationFile.setDeploymentStatus(status);
        eventBuilderConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventBuilderConfigurationFile.setDependency(dependency);
        eventBuilderConfigurationFile.setAxisConfiguration(axisConfiguration);
        eventBuilderConfigurationFile.setStreamWithVersion(streamNameWithVersion);
        eventBuilderConfigurationFile.setEbConfigOmElement(ebConfigElement);
        return eventBuilderConfigurationFile;
    }


    public void activateInactiveEventBuilderConfigurationsForAdaptor(String eventAdaptorName,
                                                                     int tenantId)
            throws EventBuilderConfigurationException {
        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();

        if (tenantSpecificEventBuilderConfigFileMap != null && tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);

            if (eventBuilderConfigurationFiles != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                    if ((eventBuilderConfigurationFile.getDeploymentStatus().equals(EventBuilderConfigurationFile.DeploymentStatus.WAITING_FOR_DEPENDENCY)) && eventBuilderConfigurationFile.getDependency().equalsIgnoreCase(eventAdaptorName)) {
                        fileList.add(eventBuilderConfigurationFile);
                    }
                }
            }
        }
        for (EventBuilderConfigurationFile builderConfigurationFile : fileList) {
            try {
                EventBuilderConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event Builder configuration file : " + builderConfigurationFile.getFileName(), e);
            }
        }
    }

    public void activateInactiveEventBuilderConfigurationsForStream(String streamNameWithVersion,
                                                                    int tenantId) throws EventBuilderConfigurationException {
        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();

        if (tenantSpecificEventBuilderConfigFileMap != null && tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);

            if (eventBuilderConfigurationFiles != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                    if (EventBuilderConfigurationFile.DeploymentStatus.WAITING_FOR_STREAM_DEPENDENCY.equals(eventBuilderConfigurationFile.getDeploymentStatus())
                            && streamNameWithVersion.equalsIgnoreCase(eventBuilderConfigurationFile.getDependency())) {
                        fileList.add(eventBuilderConfigurationFile);
                    }
                }
            }
        }
        for (EventBuilderConfigurationFile builderConfigurationFile : fileList) {
            try {
                EventBuilderConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event Builder configuration file : " + builderConfigurationFile.getFileName(), e);
            }
        }
    }

    public void deactivateActiveEventBuilderConfigurationsForAdaptor(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration, int tenantId)
            throws EventBuilderConfigurationException {

        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();
        if (tenantSpecificEventBuilderMap != null && tenantSpecificEventBuilderMap.size() > 0) {
            Map<String, List<EventBuilder>> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
            if (eventBuilderMap != null) {
                for (List<EventBuilder> eventBuilderList : eventBuilderMap.values()) {
                    for (EventBuilder eventBuilder : eventBuilderList) {
                        if (eventBuilder.getEventBuilderConfiguration().getInputStreamConfiguration().getInputEventAdaptorName().equals(inputEventAdaptorConfiguration.getName())) {
                            EventBuilderConfigurationFile builderConfigurationFile = getEventBuilderConfigurationFile(eventBuilder.getEventBuilderConfiguration().getEventBuilderName(), tenantId);
                            if (builderConfigurationFile != null) {
                                fileList.add(builderConfigurationFile);
                                // We unsubscribe here since carrying the inputAdaptorConfiguration through
                                // numerous methods would not be clean. Add the remove process, when event builder
                                // attempts to unsubscribe, the call would actually be avoided since
                                // event builder keeps track of the subscription id which will be set to null
                                // upon unsubscribing.
                                eventBuilder.unsubscribeFromEventAdaptor(inputEventAdaptorConfiguration);
                            }
                        }
                    }
                }
            }
        }

        for (EventBuilderConfigurationFile builderConfigurationFile : fileList) {
            EventBuilderConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            log.info("Event builder : " + builderConfigurationFile.getEventBuilderName() + " in inactive state because dependency could not be found : " + inputEventAdaptorConfiguration.getName());
        }
    }

    public void deactivateActiveEventBuilderConfigurationsForStream(
            String streamNameWithVersion, int tenantId)
            throws EventBuilderConfigurationException {

        List<EventBuilderConfigurationFile> fileList = new ArrayList<EventBuilderConfigurationFile>();
        if (tenantSpecificEventBuilderMap != null && tenantSpecificEventBuilderMap.size() > 0) {
            Map<String, List<EventBuilder>> eventBuilderMap = tenantSpecificEventBuilderMap.get(tenantId);
            if (eventBuilderMap != null) {
                for (List<EventBuilder> eventBuilderList : eventBuilderMap.values()) {
                    for (EventBuilder eventBuilder : eventBuilderList) {
                        EventBuilderConfiguration eventBuilderConfiguration = eventBuilder.getEventBuilderConfiguration();
                        String stream = EventBuilderUtil.getExportedStreamIdFrom(eventBuilderConfiguration);
                        if (streamNameWithVersion.equals(stream)) {
                            EventBuilderConfigurationFile builderConfigurationFile =
                                    getEventBuilderConfigurationFile(eventBuilder.getEventBuilderConfiguration().getEventBuilderName(), tenantId);
                            if (builderConfigurationFile != null) {
                                fileList.add(builderConfigurationFile);
                            }
                        }
                    }
                }
            }
        }
        for (EventBuilderConfigurationFile builderConfigurationFile : fileList) {
            EventBuilderConfigurationFileSystemInvoker.reload(builderConfigurationFile.getFilePath(), builderConfigurationFile.getAxisConfiguration());
            log.info("Event builder : " + builderConfigurationFile.getEventBuilderName() + " in inactive state because event stream dependency  could not be found : " + streamNameWithVersion);
        }
    }

    private String getFileName(int tenantId, String eventBuilderName) {

        if (tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderConfigurationFiles != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                    if ((eventBuilderConfigurationFile.getEventBuilderName().equals(eventBuilderName))
                            && eventBuilderConfigurationFile.getDeploymentStatus().equals(EventBuilderConfigurationFile.DeploymentStatus.DEPLOYED)) {
                        return new File(eventBuilderConfigurationFile.getFileName()).getName();
                    }
                }
            }
        }
        return null;
    }

    public boolean isEventBuilderAlreadyExists(int tenantId, String eventBuilderName) {

        if (tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderConfigurationFiles != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                    if ((eventBuilderConfigurationFile.getEventBuilderName().equals(eventBuilderName))
                            && (eventBuilderConfigurationFile.getDeploymentStatus().equals(EventBuilderConfigurationFile.DeploymentStatus.DEPLOYED))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private EventBuilderConfigurationFile getEventBuilderConfigurationFile(
            String eventBuilderName, int tenantId) {
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFiles != null) {
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                if (eventBuilderConfigurationFile.getEventBuilderName().equals(eventBuilderName)) {
                    return eventBuilderConfigurationFile;
                }
            }
        }
        return null;

    }

    private void validateToRemoveInactiveEventBuilderConfiguration(String eventBuilderName,
                                                                   AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = eventBuilderName + EventBuilderConstants.EB_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
        if (eventBuilderConfigurationFiles != null) {
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                if ((eventBuilderConfigurationFile.getFileName().equals(fileName))) {
                    if (!(eventBuilderConfigurationFile.getDeploymentStatus().equals(EventBuilderConfigurationFile.DeploymentStatus.DEPLOYED))) {
                        EventBuilderConfigurationFileSystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

    public boolean isEventBuilderFileAlreadyExist(String eventBuilderFileName, int tenantId) {
        if (tenantSpecificEventBuilderConfigFileMap.size() > 0) {
            List<EventBuilderConfigurationFile> eventBuilderConfigurationFiles = tenantSpecificEventBuilderConfigFileMap.get(tenantId);
            if (eventBuilderConfigurationFiles != null) {
                for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFiles) {
                    if ((eventBuilderConfigurationFile.getFileName().equals(eventBuilderFileName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
