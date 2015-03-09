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
package org.wso2.carbon.event.stream.manager.core.internal;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.stream.manager.core.*;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.exception.StreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.event.stream.manager.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.event.stream.manager.core.internal.stream.EventJunction;
import org.wso2.carbon.event.stream.manager.core.internal.util.EventStreamConstants;
import org.wso2.carbon.event.stream.manager.core.internal.util.SampleEventGenerator;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventStreamService implements EventStreamService {

    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private List<EventStreamListener> eventStreamListenerList = new ArrayList<EventStreamListener>();
    private Map<Integer, Map<String, EventJunction>> tenantSpecificEventJunctions = new HashMap<Integer, Map<String, EventJunction>>();
    private Map<Integer, Map<String, EventStreamConfig>> tenantSpecificEventStreamConfigs = new ConcurrentHashMap<Integer, Map<String, EventStreamConfig>>();


    public void removeEventStreamConfigurationFromMap(String fileName, int tenantId) {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        String streamId = null;
        if(eventStreamConfigs != null ) {
            for( EventStreamConfig eventStreamConfig: eventStreamConfigs.values()) {
                if(eventStreamConfig.getFileName().equals(fileName)) {
                    streamId = eventStreamConfig.getStreamDefinition().getStreamId();
                    break;
                }
            }
        }
        if(streamId != null) {
            eventStreamConfigs.remove(streamId);
        }
    }

    /**
     * @param name
     * @param version
     * @return
     */
    @Override
    public StreamDefinition getStreamDefinition(String name, String version, int tenantId) throws EventStreamConfigurationException {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if(eventStreamConfigs != null && eventStreamConfigs.containsKey(name+":"+version)) {
            return eventStreamConfigs.get(name+":"+version).getStreamDefinition();
        }
        return null;
    }

    /**
     * @param streamId
     * @return StreamDefinition and returns null if ont exist
     */
    @Override
    public StreamDefinition getStreamDefinition(String streamId, int tenantId) throws EventStreamConfigurationException {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if( eventStreamConfigs != null && eventStreamConfigs.containsKey(streamId)) {
            return eventStreamConfigs.get(streamId).getStreamDefinition();
        }
        return null;
    }

    public EventStreamConfig getEventStreamConfig(String streamId, int tenantId) {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if( eventStreamConfigs != null && eventStreamConfigs.containsKey(streamId)) {
            return eventStreamConfigs.get(streamId);
        }
        return null;
    }

    @Override
    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId) throws EventStreamConfigurationException {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        Collection<StreamDefinition> collection = new ArrayList<StreamDefinition>();
        if(eventStreamConfigs == null) {
            return collection;
        }
        for(EventStreamConfig eventStreamConfig: eventStreamConfigs.values()) {
            collection.add(eventStreamConfig.getStreamDefinition());
        }
        return collection;
    }

    @Override
    public Collection<EventStreamConfig> getAllEventStreamConfigs(int tenantId) throws EventStreamConfigurationException {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if(eventStreamConfigs == null) {
            return new ArrayList<EventStreamConfig>();
        }
        return eventStreamConfigs.values();
    }

    public void addEventStreamConfig(EventStreamConfig eventStreamConfig, int tenantId)
            throws EventStreamConfigurationException {
        Map<String, EventStreamConfig> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs == null) {
            eventStreamConfigs = new HashMap<String, EventStreamConfig>();
            tenantSpecificEventStreamConfigs.put(tenantId, eventStreamConfigs);
        }
        eventStreamConfigs.put(eventStreamConfig.getStreamDefinition().getStreamId(), eventStreamConfig);
    }

    @Override
    public void addEventStreamDefinition(StreamDefinition streamDefinition, int tenantId) throws
            EventStreamConfigurationException {
        AxisConfiguration axisConfig;
        if(tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfig = EventStreamServiceValueHolder.getConfigurationContextService().getServerConfigContext().getAxisConfiguration();
        } else {
            axisConfig = TenantAxisUtils.getTenantAxisConfiguration(Integer.toString(tenantId),
                    EventStreamServiceValueHolder.getConfigurationContextService().getServerConfigContext());
        }
        String directoryPath =  new File(axisConfig.getRepository().getPath())
                .getAbsolutePath() + File.separator + "eventstreams";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new EventStreamConfigurationException("Cannot create directory to add tenant specific Event Stream : " + streamDefinition.getStreamId());
            }
        }
        String filePath =  directoryPath + File.separator +
                streamDefinition.getName() + "_"+ streamDefinition.getVersion() + ".json";
        StreamDefinition streamDefinitionOld = getStreamDefinition(streamDefinition.getStreamId(), tenantId);
        if(streamDefinitionOld != null) {
            if(!(streamDefinitionOld.equals(streamDefinition))) {
                throw new StreamDefinitionAlreadyDefinedException("Different stream definition with same stream id "
                        + streamDefinition.getStreamId() +" already exist " + streamDefinitionOld.toString() +", cannot add stream definition " + streamDefinition.toString() );
            } else {
                return;
            }
        }
        saveStreamDefinitionToFileSystem(streamDefinition,filePath);
        EventStreamDeployer eventStreamDeployer = (EventStreamDeployer)((DeploymentEngine) axisConfig.getConfigurator()).getDeployer("eventstreams", "json");
        try {
            eventStreamDeployer.processDeployment(new DeploymentFileData(new File(filePath)));
            eventStreamDeployer.getDeployedEventStreamFilePaths().add(filePath);
        } catch (Exception e) {
            log.error("Event stream deploment failed " + streamDefinition.getStreamId(), e);
        }
    }

    private void saveStreamDefinitionToFileSystem(StreamDefinition streamDefinition, String filePath) {
        OutputStreamWriter writer = null;
        File file = new File(filePath);
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(streamDefinition.toString());

        } catch (Exception e) {
            log.error("Writing the stream definition " + streamDefinition.getStreamId() + " is failed ", e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    log.info("Stream definition configuration for " + streamDefinition.getStreamId() + " saved in the filesystem");
                } catch (IOException e) {
                    log.error("Writing the stream definition " + streamDefinition.getStreamId() + "is failed ", e);
                };
            }
        }
    }

    private void removeStreamDefinitionFromFileSystem(String filePath) {
        File file = new File(filePath);
        if(!(file.exists() && file.delete())) {
            throw new RuntimeException("File deleting failed " + filePath);
        }
    }

    public void loadEventStream(String streamId, int tenantId)
            throws EventStreamConfigurationException {

        StreamDefinition streamDefinition = getStreamDefinition(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId), DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), tenantId);

        if (streamDefinition != null) {
            Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
            if (eventJunctionMap == null) {
                eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
                tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
            }
            EventJunction junction = new EventJunction(streamDefinition);
            eventJunctionMap.put(streamDefinition.getStreamId(), junction);

            for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                eventStreamListener.addedEventStream(tenantId, streamDefinition.getName(), streamDefinition.getVersion());
            }
        }
    }

    public void removeEventStreamDefinition(String streamId, int tenantId) throws EventStreamConfigurationException {
        String name = null, version = null;
        if(streamId != null && streamId.contains(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)) {
            name = streamId.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)[0];
            version = streamId.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)[1];
        }
        removeEventStreamDefinition(name,version,tenantId);
    }

    @Override
    public void removeEventStreamDefinition(String streamName, String streamVersion, int tenantId)
            throws EventStreamConfigurationException {
        AxisConfiguration axisConfig;
        if(tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfig = EventStreamServiceValueHolder.getConfigurationContextService().getServerConfigContext().getAxisConfiguration();
        } else {
            axisConfig = TenantAxisUtils.getTenantAxisConfiguration(Integer.toString(tenantId),
                    EventStreamServiceValueHolder.getConfigurationContextService().getServerConfigContext());
        }
        String directoryPath =  new File(axisConfig.getRepository().getPath())
                .getAbsolutePath() + File.separator + "eventstreams";
        String filePath =  directoryPath + File.separator +
                streamName + "_"+ streamVersion + ".json";
        try {
            removeStreamDefinitionFromFileSystem(filePath);
            EventStreamDeployer eventStreamDeployer = (EventStreamDeployer)((DeploymentEngine) axisConfig.getConfigurator()).getDeployer("eventstreams", "json");
            eventStreamDeployer.processUndeployment(new File(filePath).getName());
            eventStreamDeployer.getUnDeployedEventStreamFilePaths().add(filePath);
        } catch (Exception e) {
            log.error("Event stream undeployment failed " + streamName + ":" + streamVersion, e);
        }
        log.info("Stream definition - " + streamName + ":" + streamVersion + " removed successfully");
    }

    public void unloadEventStream(String streamId, int tenantId)
            throws EventStreamConfigurationException {

        StreamDefinition streamDefinition = getStreamDefinition(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId), DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), tenantId);

        if (streamDefinition == null) {
            Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
            if (eventJunctionMap != null) {
                eventJunctionMap.remove(streamId);
            }

            for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                eventStreamListener.removedEventStream(tenantId, DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId), DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId));
            }
        }

    }

    @Override
    public void registerEventStreamListener(EventStreamListener eventStreamListener) {
        if (eventStreamListener != null) {
            eventStreamListenerList.add(eventStreamListener);
        }
    }

    @Override
    public List<String> getStreamIds(int tenantId) throws EventStreamConfigurationException {
        Collection<StreamDefinition> eventStreamConfigs = getAllStreamDefinitions(tenantId);
        List<String> streamDefinitionsIds = new ArrayList<String>(eventStreamConfigs.size());
        for (StreamDefinition streamDefinition : eventStreamConfigs) {
            streamDefinitionsIds.add(streamDefinition.getStreamId());
        }

        return streamDefinitionsIds;
    }


    @Override
    public String generateSampleEvent(String streamId, String eventType, int tenantId)
            throws EventStreamConfigurationException {

        StreamDefinition streamDefinition = getStreamDefinition(streamId, tenantId);

        if (eventType.equals(EventStreamConstants.XML_EVENT)) {
            return SampleEventGenerator.generateXMLEvent(streamDefinition);
        } else if (eventType.equals(EventStreamConstants.JSON_EVENT)) {
            return SampleEventGenerator.generateJSONEvent(streamDefinition);
        } else if (eventType.equals(EventStreamConstants.TEXT_EVENT)) {
            return SampleEventGenerator.generateTextEvent(streamDefinition);
        }
        return null;
    }

    @Override
    public void subscribe(SiddhiEventConsumer siddhiEventConsumer, int tenantId) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(tenantId, siddhiEventConsumer.getStreamId());
        eventJunction.addConsumer(siddhiEventConsumer);

    }

    @Override
    public void subscribe(RawEventConsumer rawEventConsumer, int tenantId) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(tenantId, rawEventConsumer.getStreamId());
        eventJunction.addConsumer(rawEventConsumer);
    }

    @Override
    public void subscribe(EventProducer eventProducer, int tenantId) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(tenantId, eventProducer.getStreamId());
        eventJunction.addProducer(eventProducer);
    }

    @Override
    public void subscribe(WSO2EventConsumer wso2EventConsumer, int tenantId) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(tenantId, wso2EventConsumer.getStreamId());
        eventJunction.addConsumer(wso2EventConsumer);
    }

    @Override
    public void subscribe(WSO2EventListConsumer wso2EventListConsumer, int tenantId) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(tenantId, wso2EventListConsumer.getStreamId());
        eventJunction.addConsumer(wso2EventListConsumer);
    }

    @Override
    public void unsubscribe(SiddhiEventConsumer siddhiEventConsumer, int tenantId) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(siddhiEventConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(siddhiEventConsumer);
            }
        }
    }

    @Override
    public void unsubscribe(RawEventConsumer rawEventConsumer, int tenantId) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(rawEventConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(rawEventConsumer);
            }
        }
    }

    @Override
    public void unsubscribe(EventProducer eventProducer, int tenantId) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(eventProducer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeProducer(eventProducer);
            }
        }
    }

    @Override
    public void unsubscribe(WSO2EventConsumer wso2EventConsumer, int tenantId) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(wso2EventConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(wso2EventConsumer);
            }
        }
    }

    @Override
    public void unsubscribe(WSO2EventListConsumer wso2EventListConsumer, int tenantId) {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(wso2EventListConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(wso2EventListConsumer);
            }
        }
    }

    private EventJunction getOrConstructEventJunction(int tenantId, String streamId) throws EventStreamConfigurationException {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }
        EventJunction eventJunction = eventJunctionMap.get(streamId);
        if (eventJunction == null) {
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = getStreamDefinition(streamId, tenantId);
            } catch (Exception e) {
                throw new EventStreamConfigurationException("Cannot retrieve Stream " + streamId + " for tenant " + tenantId);
            }
            if (streamDefinition == null) {
                throw new EventStreamConfigurationException("Stream " + streamId + " is not configured to tenant " + tenantId);
            }
            eventJunction = new EventJunction(streamDefinition);
            eventJunctionMap.put(streamDefinition.getStreamId(), eventJunction);
        }
        return eventJunction;
    }
}