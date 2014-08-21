/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.core.internal.storm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.AgentHolder;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.ha.server.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.core.internal.listener.AbstractSiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceClient;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceClientCallback;
import org.wso2.carbon.event.processor.storm.common.helper.StormDeploymentConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.util.collection.Pair;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Publishes events of a stream to the event receiver spout running on Storm. There will be SiddhiStormInputEventDispatcher
 * instance for each imported stream of execution plan
 */
public class SiddhiStormInputEventDispatcher extends AbstractSiddhiInputEventDispatcher implements ManagerServiceClientCallback {
    private static final Log log = LogFactory.getLog(SiddhiStormInputEventDispatcher.class);

    private AsyncDataPublisher dataPublisher = null;
    private String streamVersion;
    private String cepManagerHost = StormDeploymentConfiguration.getCepManagerHost();
    private int cepManagerPort = StormDeploymentConfiguration.getCepManagerPort();
    private StreamDefinition flattenedStreamDefinition;

    public SiddhiStormInputEventDispatcher(StreamDefinition streamDefinition,
                                           String siddhiStreamName,
                                           ExecutionPlanConfiguration executionPlanConfiguration,
                                           int tenantId) {
        super(streamDefinition.getStreamId(), siddhiStreamName, executionPlanConfiguration, tenantId);
        init(streamDefinition);
    }

    private void init(StreamDefinition streamDefinition){
        String thisHostIp = null;
        try {
            thisHostIp =  HostAddressFinder.findAddress("localhost");
        } catch (SocketException e) {
            log.error("Cannot find IP address of the host");
        }
        // Creating a data bridge stream equivalent to the Siddhi stream handled by this input event dispatcher
        // by creating a data bridge stream which has name as the Siddhi stream name, and all fields as payload
        // fields just like in Siddhi streams.
        org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition =
                EventProcessorUtil.convertToSiddhiStreamDefinition(streamDefinition, new StreamConfiguration(streamDefinition.getName(), streamDefinition.getVersion()));

        List<Attribute> streamFields = new ArrayList<Attribute>();
        for (org.wso2.siddhi.query.api.definition.Attribute siddhiAttribute : siddhiStreamDefinition.getAttributeList()){
            streamFields.add(EventProcessorUtil.convertToDatabridgeAttribute(siddhiAttribute, null));
        }
        flattenedStreamDefinition = new StreamDefinition(super.siddhiStreamId);
        flattenedStreamDefinition.setPayloadData(streamFields);
        streamVersion = flattenedStreamDefinition.getVersion();

        ManagerServiceClient client = new ManagerServiceClient(cepManagerHost, cepManagerPort, this);
        client.getStormReceiver(super.getExecutionPlanName(), super.tenantId, StormDeploymentConfiguration.getReconnectInterval(), thisHostIp);
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        sendEvent(event.getData());
    }

    @Override
    public void sendEvent(Object[] eventData) throws InterruptedException {
        try {
            if (dataPublisher != null){
                dataPublisher.publish(super.siddhiStreamId, streamVersion, null, null, eventData);
            }else{
                log.warn("Dropping the event since the data publisher is not yet initialized for " + super.getExecutionPlanName() + ":" + super.tenantId);
            }
        } catch (AgentException e) {
            log.error("Error while publishing data", e);
        }
    }

    @Override
    public void onResponseReceived(Pair<String, Integer> endpoint) {
        synchronized (this){
            dataPublisher = new AsyncDataPublisher("tcp://" + endpoint.getOne() + ":" + endpoint.getTwo(), "admin", "admin", AgentHolder.getOrCreateAgent());
            dataPublisher.addStreamDefinition(flattenedStreamDefinition);
        }

        log.info("[CEP Receiver]Storm input dispatcher connecting to Storm event receiver at " + endpoint.getOne() + ":" + endpoint.getTwo()
                + " for the Stream '" + super.siddhiStreamId + "' of ExecutionPlan '" + super.getExecutionPlanName()
                + "' (TenantID=" + super.tenantId +")");
    }
}
