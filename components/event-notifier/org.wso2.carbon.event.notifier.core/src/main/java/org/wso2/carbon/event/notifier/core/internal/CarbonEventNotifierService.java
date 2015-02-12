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
package org.wso2.carbon.event.notifier.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.core.EventNotifierService;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.config.EventNotifier;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConfiguration;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;
import org.wso2.carbon.event.notifier.core.internal.util.EventNotifierConfigurationBuilder;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConfigurationFile;
import org.wso2.carbon.event.notifier.core.internal.util.helper.EventNotifierConfigurationFilesystemInvoker;
import org.wso2.carbon.event.notifier.core.internal.util.helper.EventNotifierConfigurationHelper;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventNotifierService
        implements EventNotifierService {

    private static final Log log = LogFactory.getLog(CarbonEventNotifierService.class);
    private Map<Integer, Map<String, EventNotifier>> tenantSpecificEventNotifierConfigurationMap;
    private Map<Integer, List<EventNotifierConfigurationFile>> eventNotifierConfigurationFileMap;


    public CarbonEventNotifierService() {
        tenantSpecificEventNotifierConfigurationMap = new ConcurrentHashMap<Integer, Map<String, EventNotifier>>();
        eventNotifierConfigurationFileMap = new ConcurrentHashMap<Integer, List<EventNotifierConfigurationFile>>();
    }

    public Map<Integer, Map<String, EventNotifier>> getTenantSpecificEventNotifierConfigurationMap() {
        return tenantSpecificEventNotifierConfigurationMap;
    }

    @Override
    public void deployEventNotifierConfiguration(
            EventNotifierConfiguration eventNotifierConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        String eventFormatterName = eventNotifierConfiguration.getEventNotifierName();

        OMElement omElement = EventNotifierConfigurationBuilder.eventNotifierConfigurationToOM(eventNotifierConfiguration);
        EventNotifierConfigurationHelper.validateEventNotifierConfiguration(omElement);
        if (EventNotifierConfigurationHelper.getOutputMappingType(omElement) != null) {
            String repoPath = axisConfiguration.getRepository().getPath();
            File directory = new File(repoPath);
            if (!directory.exists()) {
                synchronized (repoPath.intern()) {
                    if (!directory.mkdir()) {
                        throw new EventNotifierConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
                    }
                }
            }

            String eventFormatterConfigPath = directory.getAbsolutePath() + File.separator + EventNotifierConstants.TM_ELE_DIRECTORY;
            directory = new File(eventFormatterConfigPath);
            if (!directory.exists()) {
                synchronized (eventFormatterConfigPath.intern()) {
                    if (!directory.mkdir()) {
                        throw new EventNotifierConfigurationException("Cannot create directory " + EventNotifierConstants.TM_ELE_DIRECTORY + " to add tenant specific event adaptor :" + eventFormatterName);
                    }
                }

            }
            validateToRemoveInactiveEventNotifierConfiguration(eventFormatterName, axisConfiguration);
            EventNotifierConfigurationFilesystemInvoker.save(omElement, eventFormatterName + ".xml", axisConfiguration);

        } else {
            throw new EventNotifierConfigurationException("Mapping type of the Event Formatter " + eventFormatterName + " cannot be null");
        }

    }

    @Override
    public void deployEventNotifierConfiguration(
            String eventNotifierConfigXml,
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        OMElement omElement = null;
        try {
            omElement = AXIOMUtil.stringToOM(eventNotifierConfigXml);
        } catch (XMLStreamException e) {
            throw new EventNotifierConfigurationException("Error in parsing XML configuration of event notifier.");
        }
        EventNotifierConfigurationHelper.validateEventNotifierConfiguration(omElement);
        String eventFormatterName = EventNotifierConfigurationHelper.getEventNotifierName(omElement);
        if (EventNotifierConfigurationHelper.getOutputMappingType(omElement) != null) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new EventNotifierConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + EventNotifierConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new EventNotifierConfigurationException("Cannot create directory " + EventNotifierConstants.TM_ELE_DIRECTORY + " to add tenant specific event adaptor :" + eventFormatterName);
                }
            }
            validateToRemoveInactiveEventNotifierConfiguration(eventFormatterName, axisConfiguration);
            EventNotifierConfigurationFilesystemInvoker.save(omElement, eventFormatterName + ".xml", axisConfiguration);
        } else {
            throw new EventNotifierConfigurationException("Mapping type of the Event Formatter " + eventFormatterName + " cannot be null");
        }

    }

    @Override
    public void undeployActiveEventNotifierConfiguration(String eventNotifierName,
                                                         AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventNotifierName);
        if (fileName != null) {
            EventNotifierConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        } else {
            throw new EventNotifierConfigurationException("Couldn't undeploy the Event Formatter configuration : " + eventNotifierName);
        }

    }

    @Override
    public void undeployInactiveEventNotifierConfiguration(String filename,
                                                           AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        EventNotifierConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
    }

    @Override
    public void editInactiveEventNotifierConfiguration(
            String eventNotifierConfiguration,
            String filename,
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        editEventNotifierConfiguration(filename, axisConfiguration, eventNotifierConfiguration, null);
    }

    @Override
    public void editActiveEventNotifierConfiguration(String eventNotifierConfiguration,
                                                     String eventNotifierName,
                                                     AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventNotifierName);
        if (fileName == null) {
            fileName = eventNotifierName + EventNotifierConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        }
        editEventNotifierConfiguration(fileName, axisConfiguration, eventNotifierConfiguration, eventNotifierName);

    }

    @Override
    public EventNotifierConfiguration getActiveEventNotifierConfiguration(
            String eventNotifierName,
            int tenantId)
            throws EventNotifierConfigurationException {

        EventNotifierConfiguration eventNotifierConfiguration = null;

        Map<String, EventNotifier> tenantSpecificEventFormatterMap = tenantSpecificEventNotifierConfigurationMap.get(tenantId);
        if (tenantSpecificEventFormatterMap != null && tenantSpecificEventFormatterMap.size() > 0) {
            eventNotifierConfiguration = tenantSpecificEventFormatterMap.get(eventNotifierName).getEventNotifierConfiguration();
        }
        return eventNotifierConfiguration;
    }

    @Override
    public List<EventNotifierConfiguration> getAllActiveEventNotifierConfiguration(
            AxisConfiguration axisConfiguration) throws EventNotifierConfigurationException {
        List<EventNotifierConfiguration> eventNotifierConfigurations = new ArrayList<EventNotifierConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventNotifierConfigurationMap.get(tenantId) != null) {
            for (EventNotifier eventNotifier : tenantSpecificEventNotifierConfigurationMap.get(tenantId).values()) {
                eventNotifierConfigurations.add(eventNotifier.getEventNotifierConfiguration());
            }
        }
        return eventNotifierConfigurations;
    }

    @Override
    public List<EventNotifierConfiguration> getAllActiveEventNotifierConfiguration(
            AxisConfiguration axisConfiguration, String streamId)
            throws EventNotifierConfigurationException {
        List<EventNotifierConfiguration> eventNotifierConfigurations = new ArrayList<EventNotifierConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventNotifierConfigurationMap.get(tenantId) != null) {
            for (EventNotifier eventNotifier : tenantSpecificEventNotifierConfigurationMap.get(tenantId).values()) {
                if (eventNotifier.getStreamId().equals(streamId)) {
                    eventNotifierConfigurations.add(eventNotifier.getEventNotifierConfiguration());
                }
            }
        }
        return eventNotifierConfigurations;
    }

    @Override
    public List<EventNotifierConfigurationFile> getAllInactiveEventNotifierConfiguration(
            AxisConfiguration axisConfiguration) {

        List<EventNotifierConfigurationFile> undeployedEventFormatterFileList = new ArrayList<EventNotifierConfigurationFile>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (eventNotifierConfigurationFileMap.get(tenantId) != null) {
            for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileMap.get(tenantId)) {
                if (!eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.DEPLOYED)) {
                    undeployedEventFormatterFileList.add(eventNotifierConfigurationFile);
                }
            }
        }
        return undeployedEventFormatterFileList;
    }

    @Override
    public String getInactiveEventNotifierConfigurationContent(String filename,
                                                               AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        return EventNotifierConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(filename, axisConfiguration);
    }

    @Override
    public String getActiveEventNotifierConfigurationContent(String eventNotifierName,
                                                             AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventNotifierName);
        return EventNotifierConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(fileName, axisConfiguration);
    }

    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        List<String> streamList = new ArrayList<String>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventStreamService eventStreamService = EventNotifierServiceValueHolder.getEventStreamService();
        Collection<StreamDefinition> eventStreamDefinitionList;
        try {
            eventStreamDefinitionList = eventStreamService.getAllStreamDefinitions(tenantId);
            if (eventStreamDefinitionList != null) {
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    streamList.add(streamDefinition.getStreamId());
                }
            }

        } catch (EventStreamConfigurationException e) {
            throw new EventNotifierConfigurationException("Error while retrieving stream definition from store", e);
        }

        return streamList;
    }

    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventStreamService eventStreamService = EventNotifierServiceValueHolder.getEventStreamService();
        try {
            return eventStreamService.getStreamDefinition(streamNameWithVersion, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventNotifierConfigurationException("Error while getting stream definition from store : " + e.getMessage(), e);
        }
    }

    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventNotifierConfigurationException {
        RegistryService registryService = EventNotifierServiceValueHolder.getRegistryService();

        String registryData;
        Resource registryResource = null;
        try {
            String pathPrefix = resourcePath.substring(0, resourcePath.indexOf(':') + 2);
            if (pathPrefix.equalsIgnoreCase(EventNotifierConstants.REGISTRY_CONF_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getConfigSystemRegistry().get(resourcePath);
            } else if (pathPrefix.equalsIgnoreCase(EventNotifierConstants.REGISTRY_GOVERNANCE_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getGovernanceSystemRegistry().get(resourcePath);
            }

            if (registryResource != null) {
                Object registryContent = registryResource.getContent();
                if (registryContent != null) {
                    registryData = (RegistryUtils.decodeBytes((byte[]) registryContent));
                } else {
                    throw new EventNotifierConfigurationException("There is no registry resource content available at " + resourcePath);
                }

            } else {
                throw new EventNotifierConfigurationException("Resource couldn't found from registry at " + resourcePath);
            }

        } catch (RegistryException e) {
            throw new EventNotifierConfigurationException("Error while retrieving the resource from registry at " + resourcePath, e);
        } catch (ClassCastException e) {
            throw new EventNotifierConfigurationException("Invalid mapping content found in " + resourcePath, e);
        }
        return registryData;
    }

    @Override
    public void setStatisticsEnabled(String eventNotifierName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventNotifierConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventNotifierConfiguration eventNotifierConfiguration = getActiveEventNotifierConfiguration(eventNotifierName, tenantId);
        eventNotifierConfiguration.setEnableStatistics(flag);
        editTracingStatistics(eventNotifierConfiguration, eventNotifierName, tenantId, axisConfiguration);
    }

    @Override
    public void setTraceEnabled(String eventNotifierName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventNotifierConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventNotifierConfiguration eventNotifierConfiguration = getActiveEventNotifierConfiguration(eventNotifierName, tenantId);
        eventNotifierConfiguration.setEnableTracing(flag);
        editTracingStatistics(eventNotifierConfiguration, eventNotifierName, tenantId, axisConfiguration);
    }

    @Override
    public String getEventNotifierStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);
        if (eventNotifierConfigurationFileList != null) {
            for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                if (filename != null && filename.equals(eventNotifierConfigurationFile.getFileName())) {
                    String statusMsg = eventNotifierConfigurationFile.getDeploymentStatusMessage();
                    if (eventNotifierConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + eventNotifierConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }

        return EventNotifierConstants.NO_DEPENDENCY_INFO_MSG;
    }

    @Override
    public void deployDefaultEventSender(String streamId, AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        //TODO fix this properly

//        OutputEventAdaptorManagerService outputEventAdaptorManagerService = EventNotifierServiceValueHolder.getOutputEventAdaptorManagerService();
//        try {
//            String defaultFormatterFilename = streamId.replaceAll(EventNotifierConstants.STREAM_ID_SEPERATOR, EventNotifierConstants.NORMALIZATION_STRING)
//                                              + EventNotifierConstants.DEFAULT_EVENT_NOTIFIER_POSTFIX + EventNotifierConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
//            if (!EventNotifierConfigurationFilesystemInvoker.isEventFormatterConfigurationFileExists(defaultFormatterFilename, axisConfiguration)) {
//                String eventAdaptorName = outputEventAdaptorManagerService.getDefaultLoggerEventAdaptor(axisConfiguration);
//                EventNotifierConfiguration defaultEventNotifierConfiguration =
//                        EventFormatterUtil.createDefaultEventFormatter(streamId, eventAdaptorName);
//                deployEventNotifierConfiguration(defaultEventNotifierConfiguration, axisConfiguration);
//            }
//        } catch (OutputEventAdaptorManagerConfigurationException e) {
//            throw new EventProducerException("Error retrieving default logger event output adaptor : " + e.getMessage(), e);
//        } catch (EventNotifierConfigurationException e) {
//            throw new EventProducerException("Error creating a default event formatter : " + e.getMessage(), e);
//        }
    }

    //Non-Interface public methods

    public boolean checkEventNotifierValidity(int tenantId, String eventNotifierName) {

        if (eventNotifierConfigurationFileMap.size() > 0) {
            List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);
            if (eventNotifierConfigurationFileList != null) {
                for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                    if ((eventNotifierConfigurationFile.getEventNotifierName().equals(eventNotifierName)) && (eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.DEPLOYED))) {
                        log.error("Event Notifier " + eventNotifierName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void addEventEventNotifierConfigurationFile(int tenantId,
                                                       EventNotifierConfigurationFile eventNotifierConfigurationFile) {

        List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);

        if (eventNotifierConfigurationFileList == null) {
            eventNotifierConfigurationFileList = new ArrayList<EventNotifierConfigurationFile>();
        } else {
            for (EventNotifierConfigurationFile anEventNotifierConfigurationFileList : eventNotifierConfigurationFileList) {
                if (anEventNotifierConfigurationFileList.getFileName().equals(eventNotifierConfigurationFile.getFileName())) {
                    return;
                }
            }
        }
        eventNotifierConfigurationFileList.add(eventNotifierConfigurationFile);
        eventNotifierConfigurationFileMap.put(tenantId, eventNotifierConfigurationFileList);
    }

    public void addEventNotifierConfiguration(
            EventNotifierConfiguration eventNotifierConfiguration)
            throws EventNotifierConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        Map<String, EventNotifier> eventFormatterConfigurationMap
                = tenantSpecificEventNotifierConfigurationMap.get(tenantId);

        if (eventFormatterConfigurationMap == null) {
            eventFormatterConfigurationMap = new ConcurrentHashMap<String, EventNotifier>();
        }

        EventNotifier eventNotifier = new EventNotifier(eventNotifierConfiguration);
        eventFormatterConfigurationMap.put(eventNotifierConfiguration.getEventNotifierName(), eventNotifier);

        tenantSpecificEventNotifierConfigurationMap.put(tenantId, eventFormatterConfigurationMap);
    }

    public void removeEventFormatterConfigurationFromMap(String fileName, int tenantId) {
        List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);
        if (eventNotifierConfigurationFileList != null) {
            for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                if ((eventNotifierConfigurationFile.getFileName().equals(fileName))) {
                    if (eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.DEPLOYED)) {
                        String eventFormatterName = eventNotifierConfigurationFile.getEventNotifierName();
                        if (tenantSpecificEventNotifierConfigurationMap.get(tenantId) != null) {
                            EventNotifier eventNotifier = tenantSpecificEventNotifierConfigurationMap.get(tenantId).get(eventFormatterName);
                            EventNotifierServiceValueHolder.getEventStreamService().unsubscribe(eventNotifier, tenantId);
                            tenantSpecificEventNotifierConfigurationMap.get(tenantId).remove(eventFormatterName);
                        }
                    }
                    eventNotifierConfigurationFileList.remove(eventNotifierConfigurationFile);
                    return;
                }
            }
        }
    }

    public void activateInactiveEventFormatterConfigurationForAdaptor(String dependency)
            throws EventNotifierConfigurationException {

        List<EventNotifierConfigurationFile> fileList = new ArrayList<EventNotifierConfigurationFile>();

        if (eventNotifierConfigurationFileMap != null && eventNotifierConfigurationFileMap.size() > 0) {

            Iterator<List<EventNotifierConfigurationFile>> eventNotifierConfigurationFileIterator = eventNotifierConfigurationFileMap.values().iterator();
            while (eventNotifierConfigurationFileIterator.hasNext()) {
                List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileIterator.next();
                if (eventNotifierConfigurationFileList != null) {
                    for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                        if ((eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && eventNotifierConfigurationFile.getDependency().equalsIgnoreCase(dependency)) {
                            fileList.add(eventNotifierConfigurationFile);
                        }
                    }
                }
            }
        }

        for (EventNotifierConfigurationFile eventNotifierConfigurationFile : fileList) {
            try {
                EventNotifierConfigurationFilesystemInvoker.reload(eventNotifierConfigurationFile.getFilePath(), eventNotifierConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event notifier configuration file : " + eventNotifierConfigurationFile.getFileName(), e);
            }
        }
    }

    public void activateInactiveEventFormatterConfigurationForStream(int tenantId, String streamId)
            throws EventNotifierConfigurationException {

        List<EventNotifierConfigurationFile> fileList = new ArrayList<EventNotifierConfigurationFile>();

        if (eventNotifierConfigurationFileMap != null && eventNotifierConfigurationFileMap.size() > 0) {
            List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);

            if (eventNotifierConfigurationFileList != null) {
                for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                    if ((eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.WAITING_FOR_STREAM_DEPENDENCY)) && eventNotifierConfigurationFile.getDependency().equalsIgnoreCase(streamId)) {
                        fileList.add(eventNotifierConfigurationFile);
                    }
                }
            }
        }
        for (EventNotifierConfigurationFile eventNotifierConfigurationFile : fileList) {
            try {
                EventNotifierConfigurationFilesystemInvoker.reload(eventNotifierConfigurationFile.getFilePath(), eventNotifierConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event notifier configuration file : " + new File(eventNotifierConfigurationFile.getFileName()).getName(), e);
            }
        }
    }

    public void deactivateActiveEventFormatterConfigurationForAdaptor(int tenantId,
                                                                      String dependency)
            throws EventNotifierConfigurationException {
        OutputEventAdaptorService eventAdaptorService = EventNotifierServiceValueHolder.getOutputEventAdaptorService();
        List<EventNotifierConfigurationFile> fileList = new ArrayList<EventNotifierConfigurationFile>();
        if (tenantSpecificEventNotifierConfigurationMap != null && tenantSpecificEventNotifierConfigurationMap.size() > 0) {
            Map<String, EventNotifier> eventFormatterMap = tenantSpecificEventNotifierConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                for (EventNotifier eventNotifier : eventFormatterMap.values()) {
                    String eventAdaptorType = eventNotifier.getEventNotifierConfiguration().getEndpointAdaptorConfiguration().getEndpointType();
                    if (eventAdaptorType.equals(dependency)) {
                        EventNotifierConfigurationFile eventNotifierConfigurationFile = getEventNotifierConfigurationFile(eventNotifier.getEventNotifierConfiguration().getEventNotifierName(), tenantId);
                        if (eventNotifierConfigurationFile != null) {
                            fileList.add(eventNotifierConfigurationFile);
                            eventAdaptorService.removeConnectionInfo(eventNotifier.getEventNotifierConfiguration().getEndpointAdaptorConfiguration(), tenantId);
                        }
                    }
                }
            }
        }

        for (EventNotifierConfigurationFile eventNotifierConfigurationFile : fileList) {
            EventNotifierConfigurationFilesystemInvoker.reload(eventNotifierConfigurationFile.getFilePath(), eventNotifierConfigurationFile.getAxisConfiguration());
            log.info("Event notifier : " + eventNotifierConfigurationFile.getEventNotifierName() + "  is in inactive state because dependency could not be found : " + dependency);
        }
    }

    public void deactivateActiveEventFormatterConfigurationForStream(int tenantId, String streamId)
            throws EventNotifierConfigurationException {
        OutputEventAdaptorService eventAdaptorService = EventNotifierServiceValueHolder.getOutputEventAdaptorService();
        List<EventNotifierConfigurationFile> fileList = new ArrayList<EventNotifierConfigurationFile>();
        if (tenantSpecificEventNotifierConfigurationMap != null && tenantSpecificEventNotifierConfigurationMap.size() > 0) {
            Map<String, EventNotifier> eventFormatterMap = tenantSpecificEventNotifierConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                for (EventNotifier eventNotifier : eventFormatterMap.values()) {
                    String streamNameWithVersion = eventNotifier.getEventNotifierConfiguration().getFromStreamName() + ":" + eventNotifier.getEventNotifierConfiguration().getFromStreamVersion();
                    if (streamNameWithVersion.equals(streamId)) {
                        EventNotifierConfigurationFile eventNotifierConfigurationFile = getEventNotifierConfigurationFile(eventNotifier.getEventNotifierConfiguration().getEventNotifierName(), tenantId);
                        if (eventNotifierConfigurationFile != null) {
                            fileList.add(eventNotifierConfigurationFile);
                            eventAdaptorService.removeConnectionInfo(eventNotifier.getEventNotifierConfiguration().getEndpointAdaptorConfiguration(), tenantId);
                        }
                    }
                }
            }
        }

        for (EventNotifierConfigurationFile eventNotifierConfigurationFile : fileList) {
            EventNotifierConfigurationFilesystemInvoker.reload(eventNotifierConfigurationFile.getFilePath(), eventNotifierConfigurationFile.getAxisConfiguration());
            log.info("Event notifier : " + eventNotifierConfigurationFile.getEventNotifierName() + "  is in inactive state because stream dependency could not be found : " + streamId);
        }
    }

    //Private Methods are below

    private void editTracingStatistics(
            EventNotifierConfiguration eventNotifierConfiguration,
            String eventNotifierName, int tenantId, AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        String fileName = getFileName(tenantId, eventNotifierName);
        undeployActiveEventNotifierConfiguration(eventNotifierName, axisConfiguration);
        OMElement omElement = EventNotifierConfigurationBuilder.eventNotifierConfigurationToOM(eventNotifierConfiguration);
        EventNotifierConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        EventNotifierConfigurationFilesystemInvoker.save(omElement, fileName, axisConfiguration);
    }

    private String getFileName(int tenantId, String eventNotifierName) {

        if (eventNotifierConfigurationFileMap.size() > 0) {
            List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);
            if (eventNotifierConfigurationFileList != null) {
                for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                    if ((eventNotifierConfigurationFile.getEventNotifierName().equals(eventNotifierName)) && (eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.DEPLOYED))) {
                        return eventNotifierConfigurationFile.getFileName();
                    }
                }
            }
        }
        return null;
    }

    private void editEventNotifierConfiguration(String filename,
                                                AxisConfiguration axisConfiguration,
                                                String eventNotifierConfiguration,
                                                String originalEventNotifierName)
            throws EventNotifierConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventNotifierConfiguration);
            omElement.toString();
            EventNotifierConfigurationHelper.validateEventNotifierConfiguration(omElement);
            String mappingType = EventNotifierConfigurationHelper.getOutputMappingType(omElement);
            if (mappingType != null) {
                EventNotifierConfiguration eventNotifierConfigurationObject = EventNotifierConfigurationBuilder.getEventNotifierConfiguration(omElement, tenantId, mappingType);
                if (!(eventNotifierConfigurationObject.getEventNotifierName().equals(originalEventNotifierName))) {
                    if (checkEventNotifierValidity(tenantId, eventNotifierConfigurationObject.getEventNotifierName())) {
                        EventNotifierConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
                        EventNotifierConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
                    } else {
                        throw new EventNotifierConfigurationException("There is a Event Notifier " + eventNotifierConfigurationObject.getEventNotifierName() + " with the same name");
                    }
                } else {
                    EventNotifierConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
                    EventNotifierConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
                }
            } else {
                throw new EventNotifierConfigurationException("Mapping type of the Event Notifier " + originalEventNotifierName + " cannot be null");

            }

        } catch (XMLStreamException e) {
            throw new EventNotifierConfigurationException("Not a valid xml object : " + e.getMessage(), e);
        }

    }

    private EventNotifierConfigurationFile getEventNotifierConfigurationFile(
            String eventNotifierName, int tenantId) {
        List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierConfigurationFileMap.get(tenantId);

        if (eventNotifierConfigurationFileList != null) {
            for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFileList) {
                if (eventNotifierConfigurationFile.getEventNotifierName().equals(eventNotifierName)) {
                    return eventNotifierConfigurationFile;
                }
            }
        }
        return null;

    }

    private void validateToRemoveInactiveEventNotifierConfiguration(String eventNotifierName,
                                                                    AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = eventNotifierName + EventNotifierConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<EventNotifierConfigurationFile> eventNotifierConfigurationFiles = eventNotifierConfigurationFileMap.get(tenantId);
        if (eventNotifierConfigurationFiles != null) {
            for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFiles) {
                if ((eventNotifierConfigurationFile.getFileName().equals(fileName))) {
                    if (!(eventNotifierConfigurationFile.getStatus().equals(EventNotifierConfigurationFile.Status.DEPLOYED))) {
                        EventNotifierConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

    public boolean isEventNotifierFileAlreadyExist(String eventNotifierFileName, int tenantId) {
        if (eventNotifierConfigurationFileMap.size() > 0) {
            List<EventNotifierConfigurationFile> eventNotifierConfigurationFiles = eventNotifierConfigurationFileMap.get(tenantId);
            if (eventNotifierConfigurationFiles != null) {
                for (EventNotifierConfigurationFile eventNotifierConfigurationFile : eventNotifierConfigurationFiles) {
                    if ((eventNotifierConfigurationFile.getFileName().equals(eventNotifierFileName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}