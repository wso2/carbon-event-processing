/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.geo.event.fuser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.window.WindowProcessor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventFusionProcessor extends WindowProcessor {
    private final String[] statesArray = new String[]{"OFFLINE", "NORMAL", "WARNING", "ALERTED"};
    private ConcurrentMap<String, List<StreamEvent>> eventsBuffer = new ConcurrentHashMap<String, List<StreamEvent>>();
    private int eventIdPosition;
    private int statePosition;
    private int informationPosition;

    /**
     * Method called when initialising the extension.
     *
     * @param attributeExpressionExecutors Array of {@link ExpressionExecutor}
     * @param executionPlanContext         {@link ExecutionPlanContext}
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext, boolean outputExpectsExpiredEvents) {
        if (attributeExpressionExecutors.length != 3) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to geo:eventsFusion(<string> " +
                    "eventId, <string> finalState, <string> information) function, required 3 arguments, but " +
                    "found " + attributeExpressionExecutors.length);
        }
        Attribute eventIdAttr = ((VariableExpressionExecutor) attributeExpressionExecutors[0]).getAttribute();
        eventIdPosition = inputDefinition.getAttributePosition(eventIdAttr.getName());
        Attribute finalStateAttr = ((VariableExpressionExecutor) attributeExpressionExecutors[1]).getAttribute();
        statePosition = inputDefinition.getAttributePosition(finalStateAttr.getName());
        Attribute informationAttr = ((VariableExpressionExecutor) attributeExpressionExecutors[2]).getAttribute();
        informationPosition = inputDefinition.getAttributePosition(informationAttr.getName());
    }

    /**
     * This method called when processing an event list.
     *
     * @param streamEventChunk  {@link ComplexEventChunk<StreamEvent>}
     * @param nextProcessor     {@link Processor}
     * @param streamEventCloner {@link StreamEventCloner}
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner) {
        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            String eventId = (String) streamEvent.getOutputData()[eventIdPosition];
            if (eventsBuffer.containsKey(eventId)) {
                eventsBuffer.get(eventId).add(streamEvent);
                if (eventsBuffer.get(eventId).size() == getDeployedExecutionPlansCount()) {
                    // Do the fusion here and return combined event.
                    fuseEvent(streamEvent);
                    eventsBuffer.remove(eventId);
                } else {
                    streamEventChunk.remove();
                }
            } else if (getDeployedExecutionPlansCount().equals(1)) {
                // Here we do not need to fuse(combine) multiple events.
                // Since we don't get multiple events, just pass through.
                nextProcessor.process(streamEventChunk);
            } else {
                List<StreamEvent> buffer = new ArrayList<StreamEvent>();
                buffer.add(streamEvent);
                eventsBuffer.put(eventId, buffer);
                streamEventChunk.remove();
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    /**
     * Get number of deployed execution plans.
     *
     * @return number of deployed execution plans.
     */
    public Integer getDeployedExecutionPlansCount() {
        return ExecutionPlansCount.getNumberOfExecutionPlans();
    }

    /**
     * Fuse given {@link StreamEvent}.
     * -Precedence of states when fusing-
     * goes low to high from LHS to RHS
     * OFFLINE < NORMAL < WARNING < ALERT
     * States will be in all caps to mimics
     * that states not get on the way.
     *
     * @param event {@link StreamEvent} to fuse
     */
    public void fuseEvent(StreamEvent event) {
        String eventId = (String) event.getOutputData()[eventIdPosition];
        List<StreamEvent> receivedEvents = eventsBuffer.get(eventId);
        List<String> states = Arrays.asList(statesArray);
        Object[] outputData = event.getOutputData();
        String finalState = "";
        String information = "";
        String alertStrings = "";
        String warningStrings = "";

        Integer currentStateIndex = -1;
        for (StreamEvent receivedEvent : receivedEvents) {
            String eventState = (String) receivedEvent.getOutputData()[statePosition];
            Integer eventStateIndex = states.indexOf(eventState);
            if (eventStateIndex > currentStateIndex) {
                finalState = eventState;
                currentStateIndex = eventStateIndex;
            }
            if ("ALERTED".equals(eventState)) {
                alertStrings += "," + receivedEvent.getOutputData()[informationPosition];
            } else if ("WARNING".equals(eventState)) {
                warningStrings += "," + receivedEvent.getOutputData()[informationPosition];
            }
        }

        if ("NORMAL".equals(finalState)) {
            information = "Normal driving pattern";
        } else {
            if (!alertStrings.isEmpty()) {
                information = "Alerts: " + alertStrings;
            }
            if (!warningStrings.isEmpty()) {
                information += " | " + "Warnings: " + warningStrings;
            }
        }
        outputData[statePosition] = finalState;
        outputData[informationPosition] = information;
        event.setOutputData(outputData);
    }

    @Override
    public void start() {
        // Do nothing.
    }

    @Override
    public void stop() {
        // Do nothing.
    }

    @Override
    public Object[] currentState() {
        return new Object[]{eventsBuffer};
    }

    @Override
    public void restoreState(Object[] state) {
        // Do nothing.
    }

}