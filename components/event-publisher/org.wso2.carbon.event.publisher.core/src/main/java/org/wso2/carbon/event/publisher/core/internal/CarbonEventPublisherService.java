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
package org.wso2.carbon.event.publisher.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorService;
import org.wso2.carbon.event.publisher.core.config.EventPublisher;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfigurationFile;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherConfigurationBuilder;
import org.wso2.carbon.event.publisher.core.internal.util.helper.EventPublisherConfigurationFilesystemInvoker;
import org.wso2.carbon.event.publisher.core.internal.util.helper.EventPublisherConfigurationHelper;
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

public class CarbonEventPublisherService implements EventPublisherService {

    private static final Log log = LogFactory.getLog(CarbonEventPublisherService.class);
    private Map<Integer, Map<String, EventPublisher>> tenantSpecificEventPublisherConfigurationMap;
    private Map<Integer, List<EventPublisherConfigurationFile>> eventPublisherConfigurationFileMap;


    public CarbonEventPublisherService() {
        tenantSpecificEventPublisherConfigurationMap = new ConcurrentHashMap<Integer, Map<String, EventPublisher>>();
        eventPublisherConfigurationFileMap = new ConcurrentHashMap<Integer, List<EventPublisherConfigurationFile>>();
    }

    public Map<Integer, Map<String, EventPublisher>> getTenantSpecificEventPublisherConfigurationMap() {
        return tenantSpecificEventPublisherConfigurationMap;
    }

    @Override
    public void deployEventPublisherConfiguration(
            EventPublisherConfiguration eventPublisherConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        String eventFormatterName = eventPublisherConfiguration.getEventPublisherName();

        OMElement omElement = EventPublisherConfigurationBuilder.eventPublisherConfigurationToOM(eventPublisherConfiguration);
        EventPublisherConfigurationHelper.validateEventPublisherConfiguration(omElement);
        if (EventPublisherConfigurationHelper.getOutputMappingType(omElement) != null) {
            String repoPath = axisConfiguration.getRepository().getPath();
            File directory = new File(repoPath);
            if (!directory.exists()) {
                synchronized (repoPath.intern()) {
                    if (!directory.mkdir()) {
                        throw new EventPublisherConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
                    }
                }
            }

            String eventFormatterConfigPath = directory.getAbsolutePath() + File.separator + EventPublisherConstants.TM_ELE_DIRECTORY;
            directory = new File(eventFormatterConfigPath);
            if (!directory.exists()) {
                synchronized (eventFormatterConfigPath.intern()) {
                    if (!directory.mkdir()) {
                        throw new EventPublisherConfigurationException("Cannot create directory " + EventPublisherConstants.TM_ELE_DIRECTORY + " to add tenant specific event adaptor :" + eventFormatterName);
                    }
                }

            }
            validateToRemoveInactiveEventPublisherConfiguration(eventFormatterName, axisConfiguration);
            EventPublisherConfigurationFilesystemInvoker.save(omElement, eventFormatterName + ".xml", axisConfiguration);

        } else {
            throw new EventPublisherConfigurationException("Mapping type of the Event Formatter " + eventFormatterName + " cannot be null");
        }

    }

    @Override
    public void deployEventPublisherConfiguration(
            String eventPublisherConfigXml,
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        OMElement omElement = null;
        try {
            omElement = AXIOMUtil.stringToOM(eventPublisherConfigXml);
        } catch (XMLStreamException e) {
            throw new EventPublisherConfigurationException("Error in parsing XML configuration of event publisher.");
        }
        EventPublisherConfigurationHelper.validateEventPublisherConfiguration(omElement);
        String eventFormatterName = EventPublisherConfigurationHelper.getEventPublisherName(omElement);
        if (EventPublisherConfigurationHelper.getOutputMappingType(omElement) != null) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new EventPublisherConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + EventPublisherConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new EventPublisherConfigurationException("Cannot create directory " + EventPublisherConstants.TM_ELE_DIRECTORY + " to add tenant specific event adaptor :" + eventFormatterName);
                }
            }
            validateToRemoveInactiveEventPublisherConfiguration(eventFormatterName, axisConfiguration);
            EventPublisherConfigurationFilesystemInvoker.save(omElement, eventFormatterName + ".xml", axisConfiguration);
        } else {
            throw new EventPublisherConfigurationException("Mapping type of the Event Formatter " + eventFormatterName + " cannot be null");
        }

    }

    @Override
    public void undeployActiveEventPublisherConfiguration(String eventPublisherName,
                                                          AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventPublisherName);
        if (fileName != null) {
            EventPublisherConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        } else {
            throw new EventPublisherConfigurationException("Couldn't undeploy the Event Formatter configuration : " + eventPublisherName);
        }

    }

    @Override
    public void undeployInactiveEventPublisherConfiguration(String filename,
                                                            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        EventPublisherConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
    }

    @Override
    public void editInactiveEventPublisherConfiguration(
            String eventPublisherConfigXml,
            String filename,
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        editEventPublisherConfiguration(filename, axisConfiguration, eventPublisherConfigXml, null);
    }

    @Override
    public void editActiveEventPublisherConfiguration(String eventPublisherConfigXml,
                                                      String eventPublisherName,
                                                      AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventPublisherName);
        if (fileName == null) {
            fileName = eventPublisherName + EventPublisherConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        }
        editEventPublisherConfiguration(fileName, axisConfiguration, eventPublisherConfigXml, eventPublisherName);

    }

    @Override
    public EventPublisherConfiguration getActiveEventPublisherConfiguration(
            String eventPublisherName,
            int tenantId)
            throws EventPublisherConfigurationException {

        EventPublisherConfiguration eventPublisherConfiguration = null;

        Map<String, EventPublisher> tenantSpecificEventFormatterMap = tenantSpecificEventPublisherConfigurationMap.get(tenantId);
        if (tenantSpecificEventFormatterMap != null && tenantSpecificEventFormatterMap.size() > 0) {
            eventPublisherConfiguration = tenantSpecificEventFormatterMap.get(eventPublisherName).getEventPublisherConfiguration();
        }
        return eventPublisherConfiguration;
    }

    @Override
    public List<EventPublisherConfiguration> getAllActiveEventPublisherConfiguration(
            AxisConfiguration axisConfiguration) throws EventPublisherConfigurationException {
        List<EventPublisherConfiguration> eventPublisherConfigurations = new ArrayList<EventPublisherConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventPublisherConfigurationMap.get(tenantId) != null) {
            for (EventPublisher eventPublisher : tenantSpecificEventPublisherConfigurationMap.get(tenantId).values()) {
                eventPublisherConfigurations.add(eventPublisher.getEventPublisherConfiguration());
            }
        }
        return eventPublisherConfigurations;
    }

    @Override
    public List<EventPublisherConfiguration> getAllActiveEventPublisherConfiguration(
            String streamId, AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        List<EventPublisherConfiguration> eventPublisherConfigurations = new ArrayList<EventPublisherConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventPublisherConfigurationMap.get(tenantId) != null) {
            for (EventPublisher eventPublisher : tenantSpecificEventPublisherConfigurationMap.get(tenantId).values()) {
                if (eventPublisher.getStreamId().equals(streamId)) {
                    eventPublisherConfigurations.add(eventPublisher.getEventPublisherConfiguration());
                }
            }
        }
        return eventPublisherConfigurations;
    }

    @Override
    public List<EventPublisherConfigurationFile> getAllInactiveEventPublisherConfiguration(
            AxisConfiguration axisConfiguration) {

        List<EventPublisherConfigurationFile> undeployedEventFormatterFileList = new ArrayList<EventPublisherConfigurationFile>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (eventPublisherConfigurationFileMap.get(tenantId) != null) {
            for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileMap.get(tenantId)) {
                if (!eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.DEPLOYED)) {
                    undeployedEventFormatterFileList.add(eventPublisherConfigurationFile);
                }
            }
        }
        return undeployedEventFormatterFileList;
    }

    @Override
    public String getInactiveEventPublisherConfigurationContent(String filename,
                                                                AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        return EventPublisherConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(filename, axisConfiguration);
    }

    @Override
    public String getActiveEventPublisherConfigurationContent(String eventPublisherName,
                                                              AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventPublisherName);
        return EventPublisherConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(fileName, axisConfiguration);
    }

    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        List<String> streamList = new ArrayList<String>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventStreamService eventStreamService = EventPublisherServiceValueHolder.getEventStreamService();
        Collection<StreamDefinition> eventStreamDefinitionList;
        try {
            eventStreamDefinitionList = eventStreamService.getAllStreamDefinitions(tenantId);
            if (eventStreamDefinitionList != null) {
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    streamList.add(streamDefinition.getStreamId());
                }
            }

        } catch (EventStreamConfigurationException e) {
            throw new EventPublisherConfigurationException("Error while retrieving stream definition from store", e);
        }

        return streamList;
    }

    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventStreamService eventStreamService = EventPublisherServiceValueHolder.getEventStreamService();
        try {
            return eventStreamService.getStreamDefinition(streamNameWithVersion, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventPublisherConfigurationException("Error while getting stream definition from store : " + e.getMessage(), e);
        }
    }

    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventPublisherConfigurationException {
        RegistryService registryService = EventPublisherServiceValueHolder.getRegistryService();

        String registryData;
        Resource registryResource = null;
        try {
            String pathPrefix = resourcePath.substring(0, resourcePath.indexOf(':') + 2);
            if (pathPrefix.equalsIgnoreCase(EventPublisherConstants.REGISTRY_CONF_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getConfigSystemRegistry().get(resourcePath);
            } else if (pathPrefix.equalsIgnoreCase(EventPublisherConstants.REGISTRY_GOVERNANCE_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getGovernanceSystemRegistry().get(resourcePath);
            }

            if (registryResource != null) {
                Object registryContent = registryResource.getContent();
                if (registryContent != null) {
                    registryData = (RegistryUtils.decodeBytes((byte[]) registryContent));
                } else {
                    throw new EventPublisherConfigurationException("There is no registry resource content available at " + resourcePath);
                }

            } else {
                throw new EventPublisherConfigurationException("Resource couldn't found from registry at " + resourcePath);
            }

        } catch (RegistryException e) {
            throw new EventPublisherConfigurationException("Error while retrieving the resource from registry at " + resourcePath, e);
        } catch (ClassCastException e) {
            throw new EventPublisherConfigurationException("Invalid mapping content found in " + resourcePath, e);
        }
        return registryData;
    }

    @Override
    public void setStatisticsEnabled(String eventPublisherName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventPublisherConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventPublisherConfiguration eventPublisherConfiguration = getActiveEventPublisherConfiguration(eventPublisherName, tenantId);
        eventPublisherConfiguration.setEnableStatistics(flag);
        editTracingStatistics(eventPublisherConfiguration, eventPublisherName, tenantId, axisConfiguration);
    }

    @Override
    public void setTraceEnabled(String eventPublisherName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventPublisherConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventPublisherConfiguration eventPublisherConfiguration = getActiveEventPublisherConfiguration(eventPublisherName, tenantId);
        eventPublisherConfiguration.setEnableTracing(flag);
        editTracingStatistics(eventPublisherConfiguration, eventPublisherName, tenantId, axisConfiguration);
    }

    @Override
    public String getEventPublisherStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);
        if (eventPublisherConfigurationFileList != null) {
            for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                if (filename != null && filename.equals(eventPublisherConfigurationFile.getFileName())) {
                    String statusMsg = eventPublisherConfigurationFile.getDeploymentStatusMessage();
                    if (eventPublisherConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + eventPublisherConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }

        return EventPublisherConstants.NO_DEPENDENCY_INFO_MSG;
    }

    @Override
    public void deployDefaultEventSender(String streamId, AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        //TODO fix this properly

//        OutputEventAdaptorManagerService outputEventAdaptorManagerService = EventPublisherServiceValueHolder.getOutputEventAdaptorManagerService();
//        try {
//            String defaultFormatterFilename = streamId.replaceAll(EventPublisherConstants.STREAM_ID_SEPERATOR, EventPublisherConstants.NORMALIZATION_STRING)
//                                              + EventPublisherConstants.DEFAULT_EVENT_PUBLISHER_POSTFIX + EventPublisherConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
//            if (!EventPublisherConfigurationFilesystemInvoker.isEventFormatterConfigurationFileExists(defaultFormatterFilename, axisConfiguration)) {
//                String eventAdaptorName = outputEventAdaptorManagerService.getDefaultLoggerEventAdaptor(axisConfiguration);
//                EventPublisherConfiguration defaultEventPublisherConfiguration =
//                        EventFormatterUtil.createDefaultEventFormatter(streamId, eventAdaptorName);
//                deployEventPublisherConfiguration(defaultEventPublisherConfiguration, axisConfiguration);
//            }
//        } catch (OutputEventAdaptorManagerConfigurationException e) {
//            throw new EventProducerException("Error retrieving default logger event output adaptor : " + e.getMessage(), e);
//        } catch (EventPublisherConfigurationException e) {
//            throw new EventProducerException("Error creating a default event formatter : " + e.getMessage(), e);
//        }
    }

    //Non-Interface public methods

    public boolean checkEventPublisherValidity(int tenantId, String eventPublisherName) {

        if (eventPublisherConfigurationFileMap.size() > 0) {
            List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);
            if (eventPublisherConfigurationFileList != null) {
                for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                    if ((eventPublisherConfigurationFile.getEventPublisherName().equals(eventPublisherName)) && (eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.DEPLOYED))) {
                        log.error("Event Publisher " + eventPublisherName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void addEventEventPublisherConfigurationFile(int tenantId,
                                                        EventPublisherConfigurationFile eventPublisherConfigurationFile) {

        List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);

        if (eventPublisherConfigurationFileList == null) {
            eventPublisherConfigurationFileList = new ArrayList<EventPublisherConfigurationFile>();
        } else {
            for (EventPublisherConfigurationFile anEventPublisherConfigurationFileList : eventPublisherConfigurationFileList) {
                if (anEventPublisherConfigurationFileList.getFileName().equals(eventPublisherConfigurationFile.getFileName())) {
                    return;
                }
            }
        }
        eventPublisherConfigurationFileList.add(eventPublisherConfigurationFile);
        eventPublisherConfigurationFileMap.put(tenantId, eventPublisherConfigurationFileList);
    }

    public void addEventPublisherConfiguration(
            EventPublisherConfiguration eventPublisherConfiguration)
            throws EventPublisherConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        Map<String, EventPublisher> eventFormatterConfigurationMap
                = tenantSpecificEventPublisherConfigurationMap.get(tenantId);

        if (eventFormatterConfigurationMap == null) {
            eventFormatterConfigurationMap = new ConcurrentHashMap<String, EventPublisher>();
        }

        EventPublisher eventPublisher = new EventPublisher(eventPublisherConfiguration);
        eventFormatterConfigurationMap.put(eventPublisherConfiguration.getEventPublisherName(), eventPublisher);

        tenantSpecificEventPublisherConfigurationMap.put(tenantId, eventFormatterConfigurationMap);
    }

    public void removeEventFormatterConfigurationFromMap(String fileName, int tenantId) {
        List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);
        if (eventPublisherConfigurationFileList != null) {
            for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                if ((eventPublisherConfigurationFile.getFileName().equals(fileName))) {
                    if (eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.DEPLOYED)) {
                        String eventFormatterName = eventPublisherConfigurationFile.getEventPublisherName();
                        if (tenantSpecificEventPublisherConfigurationMap.get(tenantId) != null) {
                            EventPublisher eventPublisher = tenantSpecificEventPublisherConfigurationMap.get(tenantId).get(eventFormatterName);
                            EventPublisherServiceValueHolder.getEventStreamService().unsubscribe(eventPublisher, tenantId);
                            tenantSpecificEventPublisherConfigurationMap.get(tenantId).remove(eventFormatterName);
                        }
                    }
                    eventPublisherConfigurationFileList.remove(eventPublisherConfigurationFile);
                    return;
                }
            }
        }
    }

    public void activateInactiveEventFormatterConfigurationForAdaptor(String eventAdaptorType)
            throws EventPublisherConfigurationException {

        List<EventPublisherConfigurationFile> fileList = new ArrayList<EventPublisherConfigurationFile>();

        if (eventPublisherConfigurationFileMap != null && eventPublisherConfigurationFileMap.size() > 0) {

            Iterator<List<EventPublisherConfigurationFile>> eventPublisherConfigurationFileIterator = eventPublisherConfigurationFileMap.values().iterator();
            while (eventPublisherConfigurationFileIterator.hasNext()) {
                List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileIterator.next();
                if (eventPublisherConfigurationFileList != null) {
                    for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                        if ((eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && eventPublisherConfigurationFile.getDependency().equalsIgnoreCase(eventAdaptorType)) {
                            fileList.add(eventPublisherConfigurationFile);
                        }
                    }
                }
            }
        }

        for (EventPublisherConfigurationFile eventPublisherConfigurationFile : fileList) {
            try {
                EventPublisherConfigurationFilesystemInvoker.reload(eventPublisherConfigurationFile.getFilePath(), eventPublisherConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event publisher configuration file : " + eventPublisherConfigurationFile.getFileName(), e);
            }
        }
    }

    public void activateInactiveEventFormatterConfigurationForStream(int tenantId, String streamId)
            throws EventPublisherConfigurationException {

        List<EventPublisherConfigurationFile> fileList = new ArrayList<EventPublisherConfigurationFile>();

        if (eventPublisherConfigurationFileMap != null && eventPublisherConfigurationFileMap.size() > 0) {
            List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);

            if (eventPublisherConfigurationFileList != null) {
                for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                    if ((eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.WAITING_FOR_STREAM_DEPENDENCY)) && eventPublisherConfigurationFile.getDependency().equalsIgnoreCase(streamId)) {
                        fileList.add(eventPublisherConfigurationFile);
                    }
                }
            }
        }
        for (EventPublisherConfigurationFile eventPublisherConfigurationFile : fileList) {
            try {
                EventPublisherConfigurationFilesystemInvoker.reload(eventPublisherConfigurationFile.getFilePath(), eventPublisherConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event publisher configuration file : " + new File(eventPublisherConfigurationFile.getFileName()).getName(), e);
            }
        }
    }

    public void deactivateActiveEventFormatterConfigurationForAdaptor(int tenantId,
                                                                      String dependency)
            throws EventPublisherConfigurationException {
        OutputEventAdaptorService eventAdaptorService = EventPublisherServiceValueHolder.getOutputEventAdaptorService();
        List<EventPublisherConfigurationFile> fileList = new ArrayList<EventPublisherConfigurationFile>();
        if (tenantSpecificEventPublisherConfigurationMap != null && tenantSpecificEventPublisherConfigurationMap.size() > 0) {
            Map<String, EventPublisher> eventFormatterMap = tenantSpecificEventPublisherConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                for (EventPublisher eventPublisher : eventFormatterMap.values()) {
                    String eventAdaptorType = eventPublisher.getEventPublisherConfiguration().getOutputAdaptorConfiguration().getEndpointType();
                    if (eventAdaptorType.equals(dependency)) {
                        EventPublisherConfigurationFile eventPublisherConfigurationFile = getEventPublisherConfigurationFile(eventPublisher.getEventPublisherConfiguration().getEventPublisherName(), tenantId);
                        if (eventPublisherConfigurationFile != null) {
                            fileList.add(eventPublisherConfigurationFile);
                            eventAdaptorService.removeConnectionInfo(eventPublisher.getEventPublisherConfiguration().getOutputAdaptorConfiguration(), tenantId);
                        }
                    }
                }
            }
        }

        for (EventPublisherConfigurationFile eventPublisherConfigurationFile : fileList) {
            EventPublisherConfigurationFilesystemInvoker.reload(eventPublisherConfigurationFile.getFilePath(), eventPublisherConfigurationFile.getAxisConfiguration());
            log.info("Event publisher : " + eventPublisherConfigurationFile.getEventPublisherName() + "  is in inactive state because dependency could not be found : " + dependency);
        }
    }

    public void deactivateActiveEventFormatterConfigurationForStream(int tenantId, String streamId)
            throws EventPublisherConfigurationException {
        OutputEventAdaptorService eventAdaptorService = EventPublisherServiceValueHolder.getOutputEventAdaptorService();
        List<EventPublisherConfigurationFile> fileList = new ArrayList<EventPublisherConfigurationFile>();
        if (tenantSpecificEventPublisherConfigurationMap != null && tenantSpecificEventPublisherConfigurationMap.size() > 0) {
            Map<String, EventPublisher> eventFormatterMap = tenantSpecificEventPublisherConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                for (EventPublisher eventPublisher : eventFormatterMap.values()) {
                    String streamNameWithVersion = eventPublisher.getEventPublisherConfiguration().getFromStreamName() + ":" + eventPublisher.getEventPublisherConfiguration().getFromStreamVersion();
                    if (streamNameWithVersion.equals(streamId)) {
                        EventPublisherConfigurationFile eventPublisherConfigurationFile = getEventPublisherConfigurationFile(eventPublisher.getEventPublisherConfiguration().getEventPublisherName(), tenantId);
                        if (eventPublisherConfigurationFile != null) {
                            fileList.add(eventPublisherConfigurationFile);
                            eventAdaptorService.removeConnectionInfo(eventPublisher.getEventPublisherConfiguration().getOutputAdaptorConfiguration(), tenantId);
                        }
                    }
                }
            }
        }

        for (EventPublisherConfigurationFile eventPublisherConfigurationFile : fileList) {
            EventPublisherConfigurationFilesystemInvoker.reload(eventPublisherConfigurationFile.getFilePath(), eventPublisherConfigurationFile.getAxisConfiguration());
            log.info("Event publisher : " + eventPublisherConfigurationFile.getEventPublisherName() + "  is in inactive state because stream dependency could not be found : " + streamId);
        }
    }

    //Private Methods are below

    private void editTracingStatistics(
            EventPublisherConfiguration eventPublisherConfiguration,
            String eventPublisherName, int tenantId, AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {

        String fileName = getFileName(tenantId, eventPublisherName);
        undeployActiveEventPublisherConfiguration(eventPublisherName, axisConfiguration);
        OMElement omElement = EventPublisherConfigurationBuilder.eventPublisherConfigurationToOM(eventPublisherConfiguration);
        EventPublisherConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        EventPublisherConfigurationFilesystemInvoker.save(omElement, fileName, axisConfiguration);
    }

    private String getFileName(int tenantId, String eventPublisherName) {

        if (eventPublisherConfigurationFileMap.size() > 0) {
            List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);
            if (eventPublisherConfigurationFileList != null) {
                for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                    if ((eventPublisherConfigurationFile.getEventPublisherName().equals(eventPublisherName)) && (eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.DEPLOYED))) {
                        return eventPublisherConfigurationFile.getFileName();
                    }
                }
            }
        }
        return null;
    }

    private void editEventPublisherConfiguration(String filename,
                                                 AxisConfiguration axisConfiguration,
                                                 String eventPublisherConfiguration,
                                                 String originalEventPublisherName)
            throws EventPublisherConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventPublisherConfiguration);
            omElement.toString();
            EventPublisherConfigurationHelper.validateEventPublisherConfiguration(omElement);
            String mappingType = EventPublisherConfigurationHelper.getOutputMappingType(omElement);
            if (mappingType != null) {
                EventPublisherConfiguration eventPublisherConfigurationObject = EventPublisherConfigurationBuilder.getEventPublisherConfiguration(omElement, tenantId, mappingType);
                if (!(eventPublisherConfigurationObject.getEventPublisherName().equals(originalEventPublisherName))) {
                    if (checkEventPublisherValidity(tenantId, eventPublisherConfigurationObject.getEventPublisherName())) {
                        EventPublisherConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
                        EventPublisherConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
                    } else {
                        throw new EventPublisherConfigurationException("There is a Event Publisher " + eventPublisherConfigurationObject.getEventPublisherName() + " with the same name");
                    }
                } else {
                    EventPublisherConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
                    EventPublisherConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
                }
            } else {
                throw new EventPublisherConfigurationException("Mapping type of the Event Publisher " + originalEventPublisherName + " cannot be null");

            }

        } catch (XMLStreamException e) {
            throw new EventPublisherConfigurationException("Not a valid xml object : " + e.getMessage(), e);
        }

    }

    private EventPublisherConfigurationFile getEventPublisherConfigurationFile(
            String eventPublisherName, int tenantId) {
        List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherConfigurationFileMap.get(tenantId);

        if (eventPublisherConfigurationFileList != null) {
            for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFileList) {
                if (eventPublisherConfigurationFile.getEventPublisherName().equals(eventPublisherName)) {
                    return eventPublisherConfigurationFile;
                }
            }
        }
        return null;

    }

    private void validateToRemoveInactiveEventPublisherConfiguration(String eventPublisherName,
                                                                     AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = eventPublisherName + EventPublisherConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<EventPublisherConfigurationFile> eventPublisherConfigurationFiles = eventPublisherConfigurationFileMap.get(tenantId);
        if (eventPublisherConfigurationFiles != null) {
            for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFiles) {
                if ((eventPublisherConfigurationFile.getFileName().equals(fileName))) {
                    if (!(eventPublisherConfigurationFile.getStatus().equals(EventPublisherConfigurationFile.Status.DEPLOYED))) {
                        EventPublisherConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

    public boolean isEventPublisherFileAlreadyExist(String eventPublisherFileName, int tenantId) {
        if (eventPublisherConfigurationFileMap.size() > 0) {
            List<EventPublisherConfigurationFile> eventPublisherConfigurationFiles = eventPublisherConfigurationFileMap.get(tenantId);
            if (eventPublisherConfigurationFiles != null) {
                for (EventPublisherConfigurationFile eventPublisherConfigurationFile : eventPublisherConfigurationFiles) {
                    if ((eventPublisherConfigurationFile.getFileName().equals(eventPublisherFileName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}