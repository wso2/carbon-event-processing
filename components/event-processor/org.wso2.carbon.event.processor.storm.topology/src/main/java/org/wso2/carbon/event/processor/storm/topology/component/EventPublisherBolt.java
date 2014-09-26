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
package org.wso2.carbon.event.processor.storm.topology.component;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.storm.common.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.storm.common.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.storm.common.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.storm.topology.util.SiddhiUtils;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Publish events processed by Siddhi engine to CEP publisher
 */
public class EventPublisherBolt extends BaseBasicBolt {
    private transient Logger log = Logger.getLogger(EventPublisherBolt.class);
    /**
     * Exported stream IDs. Must declare output filed for each exported stream
     */
    private String[] exportedStreamIDs;
    /**
     * All stream definitions processed
     */
    private String[] streamDefinitions;
    /**
     * Queries processed by Siddhi engine. Required to extract field definitions of implicitly declared stream
     * definitions
     */
    private String[] queries;
    /**
     * Keep track of relevant data bridge stream id for a given Siddhi stream id
     */
    private Map<String, org.wso2.carbon.databridge.commons.StreamDefinition> siddhiStreamIdToDataBridgeStreamMap
            = new HashMap<String, org.wso2.carbon.databridge.commons.StreamDefinition>();

    private transient TCPEventPublisher tcpEventPublisher = null;

    private String executionPlanName;

    private String logPrefix;

    private int tenantId = -1234;
    private int eventCount;
    private long batchStartTime;
    private SiddhiManager siddhiManager;
    private StormDeploymentConfig stormDeploymentConfig;
    private String thisHostIp;

    public EventPublisherBolt(int tenantId, String[] streamDefinitions, String[] queries, String[] exportedStreamIDs, String executionPlanName, StormDeploymentConfig stormDeploymentConfig) {
        this.tenantId = tenantId;
        this.exportedStreamIDs = exportedStreamIDs;
        this.streamDefinitions = streamDefinitions;
        this.queries = queries;
        this.executionPlanName = executionPlanName;
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.logPrefix = "{" + executionPlanName + ":" + tenantId + "} - ";
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        if (tcpEventPublisher == null) {
            if (siddhiManager != null) {
                siddhiManager.shutdown();
            }
            init(); // TODO : Understand why this init is required
        }

        if (tcpEventPublisher != null) {
            //TODO Do we need to keep databridge stream definitions inside the bolt??
            org.wso2.carbon.databridge.commons.StreamDefinition databridgeStream = siddhiStreamIdToDataBridgeStreamMap.get(tuple.getSourceStreamId());

            if (databridgeStream != null) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug(logPrefix + "Event published to CEP Publisher =>" + tuple.toString());
                    }
                    if (++eventCount % 10000 == 0) {
                        double timeSpentInSecs = (System.currentTimeMillis() - batchStartTime) / 1000.0D;
                        double throughput = 10000 / timeSpentInSecs;
                        log.info("Processed 10000 events in " + timeSpentInSecs + " seconds, throughput : " + throughput + " events/sec");
                        eventCount = 0;
                        batchStartTime = System.currentTimeMillis();
                    }

                    tcpEventPublisher.sendEvent(tuple.getSourceStreamId(), tuple.getValues().toArray());
                } catch (IOException e) {
                    log.error(logPrefix + "Error while publishing event to CEP publisher", e);
                }
            } else {
                log.warn(logPrefix + "Tuple received for unknown stream " + tuple.getSourceStreamId() + ". Discarding event : " + tuple.toString());
            }
        } else {
            log.warn("Dropping the event since the data publisher is not yet initialized for " + executionPlanName + ":" + tenantId);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        init();
    }

    private void init() {
        // TODO : remove siddhi related stream definitions. Use only exported streams
        try {
            log = Logger.getLogger(EventPublisherBolt.class);
            siddhiManager = new SiddhiManager(new SiddhiConfiguration());
            eventCount = 0;
            batchStartTime = System.currentTimeMillis();

            thisHostIp = HostAddressFinder.findAddress("localhost");

            if (streamDefinitions != null) {
                for (String definition : streamDefinitions) {
                    if (definition.contains("define stream")) {
                        siddhiManager.defineStream(definition);
                    } else if (definition.contains("define partition")) {
                        siddhiManager.definePartition(definition);
                    } else {
                        throw new RuntimeException("Invalid definition : " + definition);
                    }
                }
            }

            if (queries != null) {
                for (String query : queries) {
                    siddhiManager.addQuery(query);
                }
            }

            for (String streamDefinitionId : exportedStreamIDs) {
                StreamDefinition siddhiStreamDefinition = siddhiManager.getStreamDefinition(streamDefinitionId);
                org.wso2.carbon.databridge.commons.StreamDefinition databridgeStreamDefinition = SiddhiUtils.toFlatDataBridgeStreamDefinition(siddhiStreamDefinition);
                siddhiStreamIdToDataBridgeStreamMap.put(siddhiStreamDefinition.getStreamId(), databridgeStreamDefinition);
            }

            Thread thread = new Thread(new Registrar());
            thread.start();
        } catch (Throwable e) {
            log.error(logPrefix + "Error starting event publisher bolt: " + e.getMessage(), e);

        }

//        ManagerServiceClient client = new ManagerServiceClient(cepManagerHost, cepManagerPort, this);
//        client.getCepPublisher(executionPlanName, tenantId, 30, thisHostIp);
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
            log.info(logPrefix + "Getting CEP publisher for " + thisHostIp);
            while (!configureStormReceiverFromStormMangerService()){
                log.info(logPrefix + "Retry getting CEP publisher in 30 sec");
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
                String cepPublisherHostPort = client.getCEPPublisher(tenantId, executionPlanName, thisHostIp);
                log.info(logPrefix + "Successfully got CEP publisher with " + cepPublisherHostPort);

                tcpEventPublisher = new TCPEventPublisher(cepPublisherHostPort);
                for (String siddhiStreamId : exportedStreamIDs) {
                    if (log.isDebugEnabled()) {
                        log.debug(logPrefix + "EventPublisherBolt adding stream definition to client for exported Siddhi stream: " + siddhiStreamId);
                    }
                    tcpEventPublisher.addStreamDefinition(siddhiManager.getStreamDefinition(siddhiStreamId));
                }
                log.info(logPrefix + "connected to CEP publisher at " + cepPublisherHostPort);
                return true;
            } catch (Exception e) {
                log.error(logPrefix + "Error in getting CEP publisher", e);
                return false;
            } finally {
                if (transport != null) {
                    transport.close();
                }
            }
        }
    }
}
