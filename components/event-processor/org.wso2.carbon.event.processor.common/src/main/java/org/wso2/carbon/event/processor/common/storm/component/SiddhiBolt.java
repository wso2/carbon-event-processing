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
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.util.ThroughputProbe;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Bold which runs Siddhi engine
 */

public class SiddhiBolt extends BaseBasicBolt {
    private final String name;
    private transient Logger log = Logger.getLogger(SiddhiBolt.class);
    private transient SiddhiManager siddhiManager;

    /**
     * Exported stream IDs. Must declare output filed for each exported stream
     */
    private List<String> outputStreamDefinitions;
    /**
     * All stream inputStreamDefinitions and partition inputStreamDefinitions(if any)
     */
    private List<String> inputStreamDefinitions;
    /**
     * Queries to be executed in Siddhi.
     */
    private String query;

    private BasicOutputCollector collector;
    private String logPrefix;

    private transient ExecutionPlanRuntime executionPlanRuntime;

    private transient ThroughputProbe inputThroughputProbe;
    private transient ThroughputProbe emitThroughputProbe;

    /**
     * Bolt which runs the Siddhi engine.
     *
     * @param inputStreamDefinitions  - All stream and partition inputStreamDefinitions
     * @param query                   - Siddhi query
     * @param outputSiddhiDefinitions - The names of streams that will be output from this particular bolt
     */
    public SiddhiBolt(String name, List<String> inputStreamDefinitions, String query,
                      List<String> outputSiddhiDefinitions, String executionPlanName, int tenantId) {
        this.inputStreamDefinitions = inputStreamDefinitions;
        this.query = query;
        this.outputStreamDefinitions = outputSiddhiDefinitions;
        this.name = name;
        this.logPrefix = "[" + tenantId + ":" + executionPlanName + ":" + name + "] ";
        init();
    }

    /**
     * Bolt get saved and reloaded, this to redo the configurations.
     */
    private void init() {
        log = Logger.getLogger(SiddhiBolt.class);

        inputThroughputProbe = new ThroughputProbe(logPrefix + "-IN", 10);
        emitThroughputProbe = new ThroughputProbe(logPrefix + " -EMIT", 10);

        inputThroughputProbe.startSampling();
        emitThroughputProbe.startSampling();

        siddhiManager = new SiddhiManager();
        String fullQueryExpression = Utils.constructQueryExpression(inputStreamDefinitions, outputStreamDefinitions,
                query);
        executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(fullQueryExpression);

        for (String outputStreamDefinition : outputStreamDefinitions) {
            final StreamDefinition outputSiddhiDefinition = SiddhiCompiler.parseStreamDefinition(outputStreamDefinition);
            if (log.isDebugEnabled()) {
                log.debug(logPrefix + " Adding callback for stream: " + outputSiddhiDefinition.getId());
            }
            executionPlanRuntime.addCallback(outputSiddhiDefinition.getId(), new StreamCallback() {

                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        Object[] eventData = Arrays.copyOf(event.getData(), event.getData().length + 1);
                        eventData[event.getData().length] = event.getTimestamp();
                        collector.emit(outputSiddhiDefinition.getId(), Arrays.asList(eventData));

                        if (log.isDebugEnabled()) {
                            log.debug(logPrefix + "Emitted Event:" + outputSiddhiDefinition.getId() +
                                    ":" + Arrays.deepToString(eventData) + "@" + event.getTimestamp());
                        }

                        emitThroughputProbe.update();
                    }
                }
            });
        }
        executionPlanRuntime.start();
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
        inputThroughputProbe.update();

        try {
            this.collector = collector;
            InputHandler inputHandler = executionPlanRuntime.getInputHandler(tuple.getSourceStreamId());
            Object[] dataArray = tuple.getValues().toArray();
            long timestamp = (Long) dataArray[dataArray.length - 1];
            dataArray = ArrayUtils.remove(dataArray, dataArray.length - 1);

            if (log.isDebugEnabled()) {
                log.debug(logPrefix + "Received Event: " + tuple.getSourceStreamId() + ":" + Arrays.deepToString(dataArray) + "@" + timestamp);
            }

            if (inputHandler != null) {
                inputHandler.send(timestamp, dataArray);
            } else {
                log.warn(logPrefix + "Event received for unknown stream " + tuple.getSourceStreamId() + ". Discarding" +
                        " the Event: " + tuple.getSourceStreamId() + ":" + Arrays.deepToString(dataArray) + "@" + timestamp);
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

        // Declaring output fields for each exported stream ID
        for (String outputStreamDefinition : outputStreamDefinitions) {
            StreamDefinition siddhiOutputDefinition = SiddhiCompiler.parseStreamDefinition(outputStreamDefinition);
            if (outputStreamDefinition == null) {
                throw new RuntimeException(logPrefix + "Cannot find exported stream : " + siddhiOutputDefinition.getId());
            }
            List<String> list = new ArrayList<String>();
            list.add(0,"_timestamp");
            for (Attribute attribute : siddhiOutputDefinition.getAttributeList()) {
                list.add(attribute.getName());
            }
            Fields fields = new Fields(list);
            declarer.declareStream(siddhiOutputDefinition.getId(), fields);
            log.info(logPrefix + "Declaring output field for stream :" + siddhiOutputDefinition.getId());
        }
    }
}
