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
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Bold which runs Siddhi engine
 */

public class SiddhiBolt extends BaseBasicBolt {
    private transient Logger log = Logger.getLogger(SiddhiBolt.class);
    private transient SiddhiManager siddhiManager;

    /**
     * Exported stream IDs. Must declare output filed for each exported stream
     */
    private List<StreamDefinition> outputStreamDefinitions;
    /**
     * All stream inputStreamDefinitions and partition inputStreamDefinitions(if any)
     */
    private List<StreamDefinition> inputStreamDefinitions;
    /**
     * Queries to be executed in Siddhi.
     */
    private List<ExecutionPlan> queries;

    private BasicOutputCollector collector;
    private int eventCount;
    private long batchStartTime;


    /**
     * Bolt which runs the Siddhi engine.
     *
     * @param inputStreamDefinitions  - All stream and partition inputStreamDefinitions
     * @param queries                 - Siddhi queries
     * @param outputSiddhiDefinitions
     */
    public SiddhiBolt(List<StreamDefinition> inputStreamDefinitions, List<ExecutionPlan> queries, List<StreamDefinition> outputSiddhiDefinitions) {
        this.inputStreamDefinitions = inputStreamDefinitions;
        this.queries = queries;
        this.outputStreamDefinitions = outputSiddhiDefinitions;
        init();
    }

    /**
     * Bolt get saved and reloaded, this to redo the configurations.
     */
    private void init() {
        siddhiManager = new SiddhiManager(new SiddhiConfiguration());
        eventCount = 0;
        batchStartTime = System.currentTimeMillis();
        log = Logger.getLogger(SiddhiBolt.class);

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

        for (final StreamDefinition outputStreamDefinition : outputStreamDefinitions) {
            log.info("Siddhi Bolt adding callback for stream: " + outputStreamDefinition.getStreamId());
            siddhiManager.addCallback(outputStreamDefinition.getStreamId(), new StreamCallback() {
                @Override
                public void receive(Event[] events) {

                    for (Event event : events) {
                        if (++eventCount % 10000 == 0) {
                            double timeSpentInSecs = (System.currentTimeMillis() - batchStartTime) / 1000.0D;
                            double throughput = 10000 / timeSpentInSecs;
                            log.info("Processed 10000 events in " + timeSpentInSecs + " seconds, throughput : " + throughput + " events/sec");
                            eventCount = 0;
                            batchStartTime = System.currentTimeMillis();
                        }

                        collector.emit(event.getStreamId(), Arrays.asList(event.getData()));
                    }
                }
            });
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        if (siddhiManager == null) {
            init();
        }

        try {
            this.collector = collector;
            InputHandler inputHandler = siddhiManager.getInputHandler(tuple.getSourceStreamId());

            if (inputHandler != null) {
                inputHandler.send(tuple.getValues().toArray());
            } else {
                log.warn("Event received for unknown stream " + tuple.getSourceStreamId() + ". Discarding the event :" + tuple.toString());
            }
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (siddhiManager == null) {
            init();
        }

        // Declaring output fileds for each exported stream ID
        for (StreamDefinition outputStreamDefinition : outputStreamDefinitions) {

            if (outputStreamDefinition == null) {
                throw new RuntimeException("Cannot find exported stream - " + outputStreamDefinition.getStreamId());
            }
            List<String> list = new ArrayList<String>();

            for (Attribute attribute : outputStreamDefinition.getAttributeList()) {
                list.add(attribute.getName());
            }
            Fields fields = new Fields(list);
            declarer.declareStream(outputStreamDefinition.getStreamId(), fields);
            log.info("Declaring output field for stream -" + outputStreamDefinition.getStreamId());
        }
    }
}
