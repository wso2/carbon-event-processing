package org.wso2.carbon.event.stream.manager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.stream.manager.core.*;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.event.stream.manager.core.internal.stream.EventJunction;
import org.wso2.carbon.event.stream.manager.core.internal.util.EventStreamConstants;
import org.wso2.carbon.event.stream.manager.core.internal.util.SampleEventGenerator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class CarbonEventStreamService implements EventStreamService {

    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private List<EventStreamListener> eventStreamListenerList = new ArrayList<EventStreamListener>();
    private Map<Integer, Map<String, EventJunction>> tenantSpecificEventJunctions = new HashMap<Integer, Map<String, EventJunction>>();


    /**
     * @param name
     * @param version
     * @return
     */
    @Override
    public StreamDefinition getStreamDefinition(String name, String version, int tenantId) throws EventStreamConfigurationException {
        return getStreamDefinition(DataBridgeCommonsUtils.generateStreamId(name, version), tenantId);
    }

    /**
     * @param streamId
     * @return StreamDefinition and returns null if ont exist
     */
    @Override
    public StreamDefinition getStreamDefinition(String streamId, int tenantId) throws EventStreamConfigurationException {
        try {
            AbstractStreamDefinitionStore streamDefinitionStore = EventStreamServiceValueHolder.getStreamDefinitionStore();
            return streamDefinitionStore.getStreamDefinition(streamId, tenantId);
        } catch (StreamDefinitionStoreException e) {
            log.error("Error in getting Stream Definition " + streamId, e);
            throw new EventStreamConfigurationException("Error in getting Stream Definition " + streamId, e);
        }
    }

    /**
     * @return
     * @throws org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException
     */
    @Override
    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId) throws EventStreamConfigurationException {
        AbstractStreamDefinitionStore streamDefinitionStore = EventStreamServiceValueHolder.getStreamDefinitionStore();
        return streamDefinitionStore.getAllStreamDefinitions(tenantId);
    }

    @Override
    public void addEventStreamDefinition(StreamDefinition streamDefinition, int tenantId)
            throws EventStreamConfigurationException {

        StreamDefinition existingDefinition;
        existingDefinition = getStreamDefinition(streamDefinition.getName(), streamDefinition.getVersion(), tenantId);

        if (existingDefinition == null) {
            saveStreamDefinitionToStore(streamDefinition, tenantId);
            log.info("Stream definition - " + streamDefinition.getStreamId() + " added to registry successfully");
            return;
        }
        if (!existingDefinition.equals(streamDefinition)) {
            throw new EventStreamConfigurationException("Another Stream with same name and version exist : "
                    + EventDefinitionConverterUtils.convertToJson(existingDefinition));
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


    @Override
    public void removeEventStreamDefinition(String streamName, String streamVersion, int tenantId)
            throws EventStreamConfigurationException {

        if (removeStreamDefinitionFromStore(streamName, streamVersion, tenantId)) {
            log.info("Stream definition - " + streamName + ":" + streamVersion + " removed from registry successfully");
        }

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
        Collection<StreamDefinition> streamDefinitions = getAllStreamDefinitions(tenantId);
        List<String> streamDefinitionsIds = new ArrayList<String>(streamDefinitions.size());
        for (StreamDefinition streamDefinition : streamDefinitions) {
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

    private EventJunction getOrConstructEventJunction(int tenantId, String streamId) throws EventStreamConfigurationException {
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }
        EventJunction eventJunction = eventJunctionMap.get(streamId);
        if (eventJunction == null) {
            StreamDefinition streamdefinition;
            try {
                streamdefinition = EventStreamServiceValueHolder.getStreamDefinitionStore().getStreamDefinition(streamId, tenantId);
            } catch (StreamDefinitionStoreException e) {
                throw new EventStreamConfigurationException("Cannot retrieve Stream " + streamId + " for tenant " + tenantId);
            }
            if (streamdefinition == null) {
                throw new EventStreamConfigurationException("Stream " + streamId + " is not configured to tenant " + tenantId);
            }
            eventJunction = new EventJunction(streamdefinition);
            eventJunctionMap.put(streamdefinition.getStreamId(), eventJunction);
        }
        return eventJunction;
    }


    private void saveStreamDefinitionToStore(StreamDefinition streamDefinition, int tenantId)
            throws EventStreamConfigurationException {
        try {
            AbstractStreamDefinitionStore streamDefinitionStore = EventStreamServiceValueHolder.getStreamDefinitionStore();
            streamDefinitionStore.saveStreamDefinition(streamDefinition, tenantId);
        } catch (DifferentStreamDefinitionAlreadyDefinedException ex) {
            log.error(ex.getMessage());
            throw new EventStreamConfigurationException("Error in saving Stream Definition " + streamDefinition, ex);
        } catch (StreamDefinitionStoreException e) {
            log.error("Error in saving Stream Definition " + streamDefinition);
            throw new EventStreamConfigurationException("Error in saving Stream Definition " + streamDefinition, e);
        }
    }

    private boolean removeStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException {
        AbstractStreamDefinitionStore streamDefinitionStore = EventStreamServiceValueHolder.getStreamDefinitionStore();
        return streamDefinitionStore.deleteStreamDefinition(name, version, tenantId);
    }

}