/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.processor.common.storm.component;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.util.AsyncEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.*;

/**
 * Publish events processed by Siddhi engine to CEP publisher
 */
public class EventPublisherBolt extends BaseBasicBolt {
    private transient Logger log = Logger.getLogger(EventPublisherBolt.class);
    /**
     * All stream definitions processed
     */
    private List<String> inputStreamDefinitions;
    private List<String> outputStreamDefinitions;
    private String query;
    /**
     * Keep track of relevant data bridge stream id for a given Siddhi stream id
     */
    private transient Map<String, StreamDefinition> streamIdToDefinitionMap;

    private transient AsyncEventPublisher asyncEventPublisher;
    private BasicOutputCollector collector;

    private String executionPlanName;

    private String logPrefix;

    private int tenantId = -1234;
    private DistributedConfiguration stormDeploymentConfig;
    private Boolean initialized = false;

    private transient SiddhiManager siddhiManager;
    private transient ExecutionPlanRuntime executionPlanRuntime;

    private int eventCount;
    private long batchStartTime;

    public EventPublisherBolt(DistributedConfiguration stormDeploymentConfig, List<String> inputStreamDefinitions,
                              List<String> outputStreamDefinitions, String query, String executionPlanName, int tenantId) {
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.inputStreamDefinitions = inputStreamDefinitions;
        this.outputStreamDefinitions = outputStreamDefinitions;
        this.query = query;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.logPrefix = "[" + tenantId + ":" + executionPlanName + ":" + "Event Publisher Bolt" + "] ";

    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        if (log.isDebugEnabled()) {
            log.debug(logPrefix + "Received Event: " + tuple.getSourceStreamId() + ":" + Arrays.deepToString(tuple
                    .getValues().toArray()));
        }
        this.collector = basicOutputCollector;
        if (!initialized) {
            init();
        }

        Object[] dataArray = tuple.getValues().toArray();
        long timestamp = (Long) dataArray[dataArray.length - 1];
        dataArray = ArrayUtils.remove(dataArray, dataArray.length - 1);

        StreamDefinition streamDefinition = streamIdToDefinitionMap.get(tuple.getSourceStreamId());
        if (streamDefinition != null) {
            asyncEventPublisher.sendEvent(dataArray, timestamp, tuple.getSourceStreamId());
        } else {
            log.warn(logPrefix + "Tuple received for unknown stream " + tuple.getSourceStreamId() + ". Discarding " +
                    "Event: " + tuple.getSourceStreamId() + ":" + Arrays.deepToString(dataArray) + "@" + timestamp);
        }
        if (log.isDebugEnabled()) {
            log.debug(logPrefix + "Emitted Event: " + tuple.getSourceStreamId() + ":" + Arrays.deepToString(dataArray) + "@" + timestamp);
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
            initialized = true;
            //Adding functionality to support query execution at publisher level for future use cases.
            if (query != null && (!query.isEmpty())) {
                siddhiManager = new SiddhiManager();
                eventCount = 0;
                batchStartTime = System.currentTimeMillis();
                log = Logger.getLogger(SiddhiBolt.class);

                String fullQueryExpression = Utils.constructQueryExpression(inputStreamDefinitions, outputStreamDefinitions,
                        query);
                executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(fullQueryExpression);

                for (String outputStreamDefinition : outputStreamDefinitions) {
                    final StreamDefinition outputSiddhiDefinition = SiddhiCompiler.parseStreamDefinition
                            (outputStreamDefinition);
                    log.info(logPrefix + "Adding callback for stream:" + outputSiddhiDefinition.getId());
                    executionPlanRuntime.addCallback(outputSiddhiDefinition.getId(), new StreamCallback() {

                        @Override
                        public void receive(Event[] events) {
                            for (Event event : events) {
                                Object[] eventData = Arrays.copyOf(event.getData(), event.getData().length + 1);
                                eventData[event.getData().length] = event.getTimestamp();
                                collector.emit(outputSiddhiDefinition.getId(), Arrays.asList(eventData));
                                if (log.isDebugEnabled()) {
                                    if (++eventCount % 10000 == 0) {
                                        double timeSpentInSecs = (System.currentTimeMillis() - batchStartTime) / 1000.0D;
                                        double throughput = 10000 / timeSpentInSecs;
                                        log.debug(logPrefix + "Processed 10000 events in " + timeSpentInSecs + " " +
                                                "seconds, throughput : " + throughput + " events/sec. Stream : " +
                                                outputSiddhiDefinition.getId());
                                        eventCount = 0;
                                        batchStartTime = System.currentTimeMillis();
                                    }
                                    log.debug(logPrefix + "Emitted Event:" + outputSiddhiDefinition.getId() +
                                            ":" + Arrays.deepToString(eventData) + "@" + event.getTimestamp());
                                }
                            }
                        }
                    });

                }
                executionPlanRuntime.start();
            }
            streamIdToDefinitionMap = new HashMap<String, StreamDefinition>();
            StreamDefinition siddhiDefinition;
            for (String outputStreamDefinition : outputStreamDefinitions) {
                siddhiDefinition = SiddhiCompiler.parseStreamDefinition(outputStreamDefinition);
                streamIdToDefinitionMap.put(siddhiDefinition.getId(), siddhiDefinition);
            }

            asyncEventPublisher = new AsyncEventPublisher(AsyncEventPublisher.DestinationType.CEP_PUBLISHER,
                    new HashSet<StreamDefinition>(streamIdToDefinitionMap.values()),
                    stormDeploymentConfig.getManagers(), executionPlanName, tenantId, stormDeploymentConfig);

            asyncEventPublisher.initializeConnection(false);
        } catch (Throwable e) {
            log.error(logPrefix + "Error starting event publisher bolt: " + e.getMessage(), e);
        }
    }
}
