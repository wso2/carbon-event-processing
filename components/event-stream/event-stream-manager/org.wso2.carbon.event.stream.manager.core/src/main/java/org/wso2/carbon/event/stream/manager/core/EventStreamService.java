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

    public EventStreamConfig getEventStreamConfig(String streamId, int tenantId);

    /**
     * @return
     * @throws EventStreamConfigurationException
     */
    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId)
            throws EventStreamConfigurationException;


    public Collection<EventStreamConfig> getAllEventStreamConfigs(int tenantId)
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

    public void subscribe(WSO2EventListConsumer wso2EventListConsumer, int tenantId) throws EventStreamConfigurationException;

    public void unsubscribe(SiddhiEventConsumer siddhiEventConsumer, int tenantId);

    public void unsubscribe(RawEventConsumer rawEventConsumer, int tenantId);

    public void unsubscribe(EventProducer eventProducer, int tenantId);

    public void unsubscribe(WSO2EventConsumer wso2EventConsumer, int tenantId);

    public void unsubscribe(WSO2EventListConsumer wso2EventConsumer, int tenantId);

}
