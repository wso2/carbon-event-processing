package org.wso2.carbon.event.stream.manager.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.Collection;
import java.util.List;

public interface EventStreamService {


    /**
     * @param name
     * @param version
     * @return
     */
    public StreamDefinition getStreamDefinition(String name, String version, int tenantId)
            throws EventStreamConfigurationException;

    /**
     * @param streamId
     * @return
     */
    public StreamDefinition getStreamDefinition(String streamId, int tenantId)
            throws EventStreamConfigurationException;

    /**
     * @return
     * @throws EventStreamConfigurationException
     */
    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId)
            throws EventStreamConfigurationException;

    /**
     * @param streamDefinition
     * @throws EventStreamConfigurationException
     */
    public void addEventStreamDefinition(StreamDefinition streamDefinition, int tenantId) throws
            EventStreamConfigurationException;

    /**
     * @param streamName
     * @param streamVersion
     * @throws EventStreamConfigurationException
     */
    public void removeEventStreamDefinition(String streamName, String streamVersion, int tenantId)
            throws EventStreamConfigurationException;

    /**
     * @param eventStreamListener
     */
    public void registerEventStreamListener(EventStreamListener eventStreamListener);

    /**
     * @return
     * @throws EventStreamConfigurationException
     */
    public List<String> getStreamIds(int tenantId) throws EventStreamConfigurationException;


    public String generateSampleEvent(String streamId, String eventType, int tenantId)
            throws EventStreamConfigurationException;


    public void subscribe(SiddhiEventConsumer siddhiEventConsumer, int tenantId) throws EventStreamConfigurationException;

    public void subscribe(RawEventConsumer rawEventConsumer, int tenantId) throws EventStreamConfigurationException;

    public void subscribe(EventProducer eventProducer, int tenantId) throws EventStreamConfigurationException;

    public void subscribe(WSO2EventConsumer wso2EventConsumer, int tenantId) throws EventStreamConfigurationException;

    public void unsubscribe(SiddhiEventConsumer siddhiEventConsumer, int tenantId);

    public void unsubscribe(RawEventConsumer rawEventConsumer, int tenantId);

    public void unsubscribe(EventProducer eventProducer, int tenantId);

    public void unsubscribe(WSO2EventConsumer wso2EventConsumer, int tenantId);

}
