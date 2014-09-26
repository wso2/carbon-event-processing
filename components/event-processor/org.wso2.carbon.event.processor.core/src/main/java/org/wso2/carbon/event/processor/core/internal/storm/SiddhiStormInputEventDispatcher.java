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
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.ha.server.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.core.internal.listener.AbstractSiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.processor.storm.common.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.storm.common.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.storm.common.transport.client.TCPEventPublisher;
import org.wso2.siddhi.core.event.Event;

import java.io.IOException;

/**
 * Publishes events of a stream to the event receiver spout running on Storm. There will be SiddhiStormInputEventDispatcher
 * instance for each imported stream of execution plan
 */
public class SiddhiStormInputEventDispatcher extends AbstractSiddhiInputEventDispatcher {
    private static final Log log = LogFactory.getLog(SiddhiStormInputEventDispatcher.class);
    private final StormDeploymentConfig stormDeploymentConfig;
    private final ExecutionPlanConfiguration executionPlanConfiguration;

    private TCPEventPublisher tcpEventPublisher = null;
    private org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition;
    private String logPrefix;
    private String thisHostIp;

    public SiddhiStormInputEventDispatcher(StreamDefinition streamDefinition,
                                           String siddhiStreamId, String siddhiStreamName,
                                           ExecutionPlanConfiguration executionPlanConfiguration,
                                           int tenantId, StormDeploymentConfig stormDeploymentConfig) {
        super(streamDefinition.getStreamId(), siddhiStreamId, executionPlanConfiguration, tenantId);
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.stormDeploymentConfig = stormDeploymentConfig;
        init(streamDefinition, siddhiStreamName, executionPlanConfiguration);
    }

    private void init(StreamDefinition streamDefinition, String siddhiStreamName, ExecutionPlanConfiguration executionPlanConfiguration) {
        logPrefix = "[CEP Receiver] for execution plan '" + executionPlanConfiguration.getName() + "'" + "(TenantID=" + tenantId + ") ";

        try {
            thisHostIp = HostAddressFinder.findAddress("localhost");

            // Creating a Siddhi stream handled by this input event dispatcher
            // by using a data bridge stream which has name as the Siddhi stream name, and all fields as payload
            // fields just like in Siddhi streams.
            this.siddhiStreamDefinition = EventProcessorUtil.convertToSiddhiStreamDefinition(streamDefinition, new StreamConfiguration(siddhiStreamName, streamDefinition.getVersion()));
            Thread thread = new Thread(new Registrar());
            thread.start();
        } catch (Exception e) {
            log.error(logPrefix + "Failed to start event listener", e);
        }
    }


    @Override
    public void sendEvent(Event event) throws InterruptedException {
        sendEvent(event.getData());
    }

    @Override
    public void sendEvent(Object[] eventData) throws InterruptedException {
        try {
            if (tcpEventPublisher != null) {
                tcpEventPublisher.sendEvent(this.siddhiStreamDefinition.getStreamId(), eventData);
            } else {
                log.warn(logPrefix + "Dropping the event since the data publisher is not yet initialized");
            }
        } catch (IOException e) {
            log.error(logPrefix + "Error while publishing data", e);
        }
    }

    class Registrar implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            log.info(logPrefix + "Getting Storm receiver for " + thisHostIp);
            while (!configureStormReceiverFromStormMangerService()) {
                log.info(logPrefix + "Retry getting Storm receiver in 30 sec");
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    //ignore
                }
            }

        }

        private boolean configureStormReceiverFromStormMangerService() {

            TTransport transport = null;
            try {
                transport = new TSocket(stormDeploymentConfig.getManagers().get(0).getHostName(), stormDeploymentConfig.getManagers().get(0).getPort());
                TProtocol protocol = new TBinaryProtocol(transport);
                transport.open();

                StormManagerService.Client client = new StormManagerService.Client(protocol);
                String stormReceiverHostPort = client.getStormReceiver(tenantId, executionPlanConfiguration.getName(), thisHostIp);
                log.info(logPrefix + "Successfully got Storm receiver with " + stormReceiverHostPort);

                tcpEventPublisher = new TCPEventPublisher(stormReceiverHostPort);
                tcpEventPublisher.addStreamDefinition(siddhiStreamDefinition);
                log.info(logPrefix + "connected to Storm event receiver at " + stormReceiverHostPort
                        + " for the Stream '" + siddhiStreamDefinition.getStreamId());
                return true;
            } catch (Exception e) {
                log.error(logPrefix + "Error in getting Storm receiver ", e);
                return false;
            } finally {
                if (transport != null) {
                    transport.close();
                }
            }
        }
    }
}
