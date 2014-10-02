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
package org.wso2.carbon.event.processor.common.storm.component;

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
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.common.util.Utils;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.query.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Publish events processed by Siddhi engine to CEP publisher
 */
public class EventPublisherBolt extends BaseBasicBolt {
    private transient Logger log = Logger.getLogger(EventPublisherBolt.class);
    private final List<ExecutionPlan> queries;
    /**
     * All stream definitions processed
     */
    private List<StreamDefinition> inputStreamDefinitions;
    private List<StreamDefinition> outputStreamDefinitions;
    /**
     * Keep track of relevant data bridge stream id for a given Siddhi stream id
     */
    private Map<String, StreamDefinition> streamIdToDefinitionMap = new HashMap<String, StreamDefinition>();

    private transient TCPEventPublisher tcpEventPublisher = null;

    private String executionPlanName;

    private String logPrefix;

    private int tenantId = -1234;
    private int eventCount;
    private long batchStartTime;
    private SiddhiManager siddhiManager;
    private StormDeploymentConfig stormDeploymentConfig;
    private String thisHostIp;

    public EventPublisherBolt(StormDeploymentConfig stormDeploymentConfig, List<StreamDefinition> inputStreamDefinitions, List<ExecutionPlan> queries, List<StreamDefinition> outputStreamDefinitions, String executionPlanName, int tenantId) {

        this.stormDeploymentConfig = stormDeploymentConfig;
        this.inputStreamDefinitions = inputStreamDefinitions;
        this.queries = queries;
        this.outputStreamDefinitions = outputStreamDefinitions;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
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
//            org.wso2.carbon.databridge.commons.StreamDefinition databridgeStream = streamIdToDefinitionMap.get(tuple.getSourceStreamId());
          StreamDefinition streamDefinition=  streamIdToDefinitionMap.get(tuple.getSourceStreamId());
            if (streamDefinition != null) {
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

            thisHostIp = Utils.findAddress("localhost");

            if (inputStreamDefinitions != null) {
                for (StreamDefinition definition : inputStreamDefinitions) {
                    siddhiManager.defineStream(definition);
                }
            }

            if (queries != null) {
                for (ExecutionPlan executionPlan : queries) {
                    if (executionPlan instanceof Query) {
                        siddhiManager.addQuery((Query) executionPlan);
                    } else if (executionPlan instanceof StreamDefinition) {
                        siddhiManager.defineStream((StreamDefinition) executionPlan);
                    } else if (executionPlan instanceof PartitionDefinition) {
                        siddhiManager.definePartition((PartitionDefinition) executionPlan);
                    } else if (executionPlan instanceof TableDefinition) {
                        siddhiManager.defineTable((TableDefinition) executionPlan);
                    }
                }
            }

            for (StreamDefinition outputStreamDefinition : outputStreamDefinitions) {
                streamIdToDefinitionMap.put(outputStreamDefinition.getStreamId(), outputStreamDefinition);
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
            while (!configureStormReceiverFromStormMangerService()) {
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
                for (StreamDefinition outputStreamDefinition : outputStreamDefinitions) {
                    if (log.isDebugEnabled()) {
                        log.debug(logPrefix + "EventPublisherBolt adding stream definition to client for exported Siddhi stream: " + outputStreamDefinition.getStreamId());
                    }
                    tcpEventPublisher.addStreamDefinition(outputStreamDefinition);
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
