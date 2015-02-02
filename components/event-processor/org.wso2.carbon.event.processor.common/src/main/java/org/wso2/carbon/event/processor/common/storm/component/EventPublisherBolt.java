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
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.common.util.AsyncEventPublisher;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.query.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Publish events processed by Siddhi engine to CEP publisher
 */
public class EventPublisherBolt extends BaseBasicBolt{
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

    private transient AsyncEventPublisher asyncEventPublisher;

    private String executionPlanName;

    private String logPrefix;

    private int tenantId = -1234;
    private SiddhiManager siddhiManager;
    private StormDeploymentConfig stormDeploymentConfig;

    public EventPublisherBolt(StormDeploymentConfig stormDeploymentConfig, List<StreamDefinition> inputStreamDefinitions, List<ExecutionPlan> queries, List<StreamDefinition> outputStreamDefinitions, String executionPlanName, int tenantId) {
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.inputStreamDefinitions = inputStreamDefinitions;
        this.queries = queries;
        this.outputStreamDefinitions = outputStreamDefinitions;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.logPrefix = "[" + executionPlanName + ":" + tenantId + "] - ";

    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        if (siddhiManager == null) {
            init();
         }

        StreamDefinition streamDefinition =  streamIdToDefinitionMap.get(tuple.getSourceStreamId());
        if (streamDefinition != null) {
            asyncEventPublisher.sendEvent(tuple.getValues().toArray(), tuple.getSourceStreamId());
        } else {
            log.warn(logPrefix + "Tuple received for unknown stream " + tuple.getSourceStreamId() + ". Discarding event : " + tuple.toString());
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
        try {
            log = Logger.getLogger(EventPublisherBolt.class);
            siddhiManager = new SiddhiManager(new SiddhiConfiguration());

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

            asyncEventPublisher = new AsyncEventPublisher(AsyncEventPublisher.DestinationType.CEP_PUBLISHER,
                                                          new HashSet<StreamDefinition>(outputStreamDefinitions),
                                                          stormDeploymentConfig.getManagers().get(0).getHostName(),
                                                          stormDeploymentConfig.getManagers().get(0).getPort(),
                                                          executionPlanName,
                                                          tenantId,
                                                          stormDeploymentConfig);

            asyncEventPublisher.initializeConnection(false);
        } catch (Throwable e) {
            log.error(logPrefix + "Error starting event publisher bolt: " + e.getMessage(), e);

        }
    }
}
