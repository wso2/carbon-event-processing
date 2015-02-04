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
package org.wso2.carbon.event.processor.core.internal.storm.util;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.storm.component.EventPublisherBolt;
import org.wso2.carbon.event.processor.common.storm.component.EventReceiverSpout;
import org.wso2.carbon.event.processor.common.storm.component.SiddhiBolt;
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Reads the execution plan xml file and construct the Storm topology
 */
public class StormTopologyConstructor {

    private static Logger log = Logger.getLogger(StormTopologyConstructor.class);

    public static TopologyBuilder constructTopologyBuilder(String queryPlanString, String executionPlanName, int tenantId, StormDeploymentConfig stormDeploymentConfig) throws XMLStreamException {

        OMElement queryPlanElement = AXIOMUtil.stringToOM(queryPlanString);

        TopologyInfoHolder topologyInfoHolder = new TopologyInfoHolder();
        TopologyBuilder builder = new TopologyBuilder();

        //******** Receiver ************
        Iterator<OMElement> iterator = queryPlanElement.getChildrenWithName(new QName("event-receiver"));
        while (iterator.hasNext()) {
            OMElement eventReceiverElement = iterator.next();
            String name = eventReceiverElement.getAttributeValue(new QName("name"));
            String parallel = eventReceiverElement.getAttributeValue(new QName("parallel"));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.EVENT_RECEIVER_SPOUT);

            List<StreamDefinition> streamDefinitions = getStreamDefinitions(eventReceiverElement.getFirstChildWithName(new QName("streams")));
            for (StreamDefinition streamDefinition : streamDefinitions) {
                //Receiver only passes through incoming events. Therefore, input all input streams are output streams
                componentInfoHolder.addInputStream(streamDefinition.getStreamId(), streamDefinition);
                componentInfoHolder.addOutputStream(streamDefinition.getStreamId(), streamDefinition);
            }

            componentInfoHolder.setDeclarer(builder.setSpout(name, new EventReceiverSpout(stormDeploymentConfig, streamDefinitions, executionPlanName, tenantId, stormDeploymentConfig.getHeartbeatInterval()), Integer.parseInt(parallel)));
            topologyInfoHolder.addComponent(componentInfoHolder);
        }

        //******** Processor ************
        iterator = queryPlanElement.getChildrenWithName(new QName("event-processor"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String name = eventProcessorElement.getAttributeValue(new QName("name"));
            String parallel = eventProcessorElement.getAttributeValue(new QName("parallel"));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.SIDDHI_BOLT);

            OMElement inputStreamsElement = eventProcessorElement.getFirstChildWithName(new QName("input-streams"));
            List<StreamDefinition> inputStreamDefinitions = getStreamDefinitions(inputStreamsElement);
            for (StreamDefinition streamDefinition : inputStreamDefinitions) {
                componentInfoHolder.addInputStream(streamDefinition.getStreamId(), streamDefinition);
            }
            // Adding partitioning fields of input streams
            addPartitionFields(inputStreamsElement, componentInfoHolder);

            List<ExecutionPlan> executionPlans = new ArrayList<ExecutionPlan>();
            OMElement queryElement = eventProcessorElement.getFirstChildWithName(new QName("queries"));
            executionPlans.addAll(SiddhiCompiler.parse(queryElement.getText()));
            componentInfoHolder.addSiddhiQueries(executionPlans);

            List<StreamDefinition> outputStreamDefinitions = getStreamDefinitions(eventProcessorElement.getFirstChildWithName(new QName("output-streams")));
            for (StreamDefinition streamDefinition : outputStreamDefinitions) {
                componentInfoHolder.addOutputStream(streamDefinition.getStreamId(), streamDefinition);
            }
            componentInfoHolder.setDeclarer(builder.setBolt(name, new SiddhiBolt(inputStreamDefinitions, executionPlans, outputStreamDefinitions), Integer.parseInt(parallel)));
            topologyInfoHolder.addComponent(componentInfoHolder);
        }


        //******** Publisher ************
        iterator = queryPlanElement.getChildrenWithName(new QName("event-publisher"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String name = eventProcessorElement.getAttributeValue(new QName("name"));
            String parallel = eventProcessorElement.getAttributeValue(new QName("parallel"));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.SIDDHI_BOLT);

            OMElement inputStreamsElement = eventProcessorElement.getFirstChildWithName(new QName("input-streams"));
            List<StreamDefinition> inputStreamDefinitions = getStreamDefinitions(inputStreamsElement);
            for (StreamDefinition streamDefinition : inputStreamDefinitions) {
                componentInfoHolder.addInputStream(streamDefinition.getStreamId(), streamDefinition);
            }
            // Publisher might also partition the output. Adding partitioning fields of input streams.
            addPartitionFields(inputStreamsElement, componentInfoHolder);

            List<ExecutionPlan> executionPlans = new ArrayList<ExecutionPlan>();
            OMElement queryElement = eventProcessorElement.getFirstChildWithName(new QName("queries"));
            if (queryElement != null) {
                executionPlans.addAll(SiddhiCompiler.parse(queryElement.getText()));
                componentInfoHolder.addSiddhiQueries(executionPlans);
            }

            List<StreamDefinition> outputStreamDefinitions = getStreamDefinitions(eventProcessorElement.getFirstChildWithName(new QName("output-streams")));
            for (StreamDefinition streamDefinition : outputStreamDefinitions) {
                componentInfoHolder.addOutputStream(streamDefinition.getStreamId(), streamDefinition);
            }
            componentInfoHolder.setDeclarer(builder.setBolt(name, new EventPublisherBolt(stormDeploymentConfig, inputStreamDefinitions, executionPlans, outputStreamDefinitions, executionPlanName, tenantId), Integer.parseInt(parallel)));
            topologyInfoHolder.addComponent(componentInfoHolder);
        }

        topologyInfoHolder.indexComponents();

        /**
         * Connecting components together.
         *  1) Get each component
         *  2) Get input streams of that component
         *  3) Find publishers who publishes each input stream
         *  4) Connect with producer of input stream
         */
        for (ComponentInfoHolder componentInfoHolder : topologyInfoHolder.getComponents()) {

            if (componentInfoHolder.getComponentType() != ComponentInfoHolder.ComponentType.EVENT_RECEIVER_SPOUT) {
                BoltDeclarer boltDeclarer = (BoltDeclarer) componentInfoHolder.getDeclarer();

                for (String inputStreamId : componentInfoHolder.getInputStreamIds()) {
                    for (ComponentInfoHolder pubComponent : topologyInfoHolder.getPublishingComponents(inputStreamId)) {

                        if (!pubComponent.getComponentName().equals(componentInfoHolder.getComponentName())) {
                            String partitionedField = componentInfoHolder.getPartionenedField(inputStreamId);
                            String groupingType = "AllGrouping";

                            if (partitionedField == null){
                                boltDeclarer.allGrouping(pubComponent.getComponentName(), inputStreamId);
                            }else{
                                groupingType = "FieldGrouping";
                                boltDeclarer.fieldsGrouping(pubComponent.getComponentName(), inputStreamId, new Fields(partitionedField));
                            }

                            if (log.isDebugEnabled()){
                                log.debug("Connecting storm components [Consumer:" + componentInfoHolder.getComponentName()
                                        + ", Stream:" + inputStreamId
                                        + ", Publisher:" + pubComponent.getComponentName()
                                        + ", Grouping:" + groupingType + "]"); ;
                            }
                        }
                    }
                }
            }
        }

        return builder;
    }

    private static List<StreamDefinition> getStreamDefinitions(OMElement streamsElement) {
        List<StreamDefinition> streamDefinitions = new ArrayList<StreamDefinition>();
        Iterator<OMElement> streamIterator = streamsElement.getChildrenWithName(new QName("stream"));
        while (streamIterator.hasNext()) {
            OMElement streamElement = streamIterator.next();
            StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition(streamElement.getText());
            streamDefinitions.add(streamDefinition);
        }
        return streamDefinitions;
    }

    /**
     * Adding stream partitioned fields
     */
    private static void addPartitionFields(OMElement streamsElement, ComponentInfoHolder componentInfoHolder){
        Iterator<OMElement> streamIterator = streamsElement.getChildrenWithName(new QName("stream"));
        while (streamIterator.hasNext()) {
            OMElement streamElement = streamIterator.next();
            OMAttribute partitionAttribute = streamElement.getAttribute(new QName("partition"));

            if (partitionAttribute != null){
                StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition(streamElement.getText());
                componentInfoHolder.addStreamPartitioningField(streamDefinition.getStreamId(), partitionAttribute.getAttributeValue());
            }
        }
    }
}