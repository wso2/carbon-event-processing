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
import backtype.storm.topology.SpoutDeclarer;
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
import org.wso2.carbon.event.processor.core.exception.StormDeploymentException;
import org.wso2.carbon.event.processor.core.exception.StormQueryConstructionException;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Reads the execution plan xml file and construct the Storm topology
 */
public class StormTopologyConstructor {

    private static Logger log = Logger.getLogger(StormTopologyConstructor.class);

    public static TopologyBuilder constructTopologyBuilder(String queryPlanString, String executionPlanName,
                                                           int tenantId, StormDeploymentConfig stormDeploymentConfig)
            throws XMLStreamException, StormQueryConstructionException {

        OMElement queryPlanElement = AXIOMUtil.stringToOM(queryPlanString);
        TopologyInfoHolder topologyInfoHolder = new TopologyInfoHolder();
        TopologyBuilder builder = new TopologyBuilder();

        /*
        Receiver section
         */
        Iterator<OMElement> iterator = queryPlanElement.getChildrenWithName(new QName("event-receiver"));
        while (iterator.hasNext()) {
            OMElement eventReceiverElement = iterator.next();
            String name = eventReceiverElement.getAttributeValue(new QName("name"));
            String parallel = eventReceiverElement.getAttributeValue(new QName("parallel"));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.EVENT_RECEIVER_SPOUT);

            List<String> streamDefinitions = getStreamDefinitions(eventReceiverElement.getFirstChildWithName(new QName
                    ("streams")));
            for (String streamDefinition : streamDefinitions) {
                //Receiver only passes through incoming events. Therefore, input all input streams are output streams
                componentInfoHolder.addInputStream(streamDefinition);
                componentInfoHolder.addOutputStream(streamDefinition);
            }

            componentInfoHolder.setDeclarer(builder.setSpout(name, new EventReceiverSpout(stormDeploymentConfig,
                            streamDefinitions, executionPlanName, tenantId, stormDeploymentConfig.getHeartbeatInterval()),
                    Integer.parseInt(parallel)));
            topologyInfoHolder.addComponent(componentInfoHolder);
        }

        /*
        Processor Section
         */
        iterator = queryPlanElement.getChildrenWithName(new QName("event-processor"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String name = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants.NAME));
            String parallel = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants.PARALLEL));
            String isEnforced = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants
                    .ENFORCE_PARALLELISM));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.SIDDHI_BOLT);

            OMElement inputStreamsElement = eventProcessorElement.getFirstChildWithName(new QName("input-streams"));
            List<String> inputStreamDefinitions = getStreamDefinitions(inputStreamsElement);
            for (String streamDefinition : inputStreamDefinitions) {
                componentInfoHolder.addInputStream(streamDefinition);
            }
            // Adding partitioning fields of input streams
            addPartitionFields(inputStreamsElement, componentInfoHolder);

            OMElement queryElement = eventProcessorElement.getFirstChildWithName(new QName("queries"));
            componentInfoHolder.addSiddhiQuery(queryElement.getText());

            List<String> outputStreamDefinitions = getStreamDefinitions(eventProcessorElement.getFirstChildWithName(new
                    QName("output-streams")));
            for (String streamDefinition : outputStreamDefinitions) {
                componentInfoHolder.addOutputStream(streamDefinition);
            }
            BoltDeclarer declarer = builder.setBolt(name, new SiddhiBolt(inputStreamDefinitions,
                    queryElement.getText(), outputStreamDefinitions), Integer.parseInt(parallel));
            //enforcing parallelism
            if (isEnforced.equals("true")) {
                declarer.setMaxTaskParallelism(Integer.parseInt(parallel));     //todo add enforce property
            }
            componentInfoHolder.setDeclarer(declarer);
            topologyInfoHolder.addComponent(componentInfoHolder);
        }


        /*
        Publisher Section
         */
        iterator = queryPlanElement.getChildrenWithName(new QName("event-publisher"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String name = eventProcessorElement.getAttributeValue(new QName("name"));
            String parallel = eventProcessorElement.getAttributeValue(new QName("parallel"));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.SIDDHI_BOLT);

            OMElement inputStreamsElement = eventProcessorElement.getFirstChildWithName(new QName("input-streams"));
            List<String> inputStreamDefinitions = getStreamDefinitions(inputStreamsElement);
            for (String streamDefinition : inputStreamDefinitions) {
                componentInfoHolder.addInputStream(streamDefinition);
            }
            // Publisher might also partition the output. Adding partitioning fields of input streams.
            addPartitionFields(inputStreamsElement, componentInfoHolder);

            OMElement queryElement = eventProcessorElement.getFirstChildWithName(new QName("queries"));
            String query = null;
            if (queryElement != null) {
                query = queryElement.getText();
                componentInfoHolder.addSiddhiQuery(query);
            }

            List<String> outputStreamDefinitions = getStreamDefinitions(eventProcessorElement.getFirstChildWithName(new
                    QName("output-streams")));
            for (String streamDefinition : outputStreamDefinitions) {
                componentInfoHolder.addOutputStream(streamDefinition);
            }
            componentInfoHolder.setDeclarer(builder.setBolt(name, new EventPublisherBolt(stormDeploymentConfig,
                    inputStreamDefinitions, outputStreamDefinitions, query, executionPlanName, tenantId),
                    Integer.parseInt(parallel)));
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
                            String groupingType = "ShuffleGrouping";
                            if (partitionedField == null){
                                boltDeclarer.shuffleGrouping(pubComponent.getComponentName(), inputStreamId);
                            }else{
                                groupingType = "FieldGrouping";
                                boltDeclarer.fieldsGrouping(pubComponent.getComponentName(), inputStreamId, new Fields(partitionedField));
                            }

                            if (log.isDebugEnabled()){
                                log.debug("Connecting storm components [Consumer:" + componentInfoHolder.getComponentName()
                                        + ", Stream:" + inputStreamId
                                        + ", Publisher:" + pubComponent.getComponentName()
                                        + ", Grouping:" + groupingType + "]");
                            }
                        }
                    }
                }
            }
        }

        return builder;
    }

    private static List<String> getStreamDefinitions(OMElement streamsElement) {
        List<String> streamDefinitions = new ArrayList<String>();
        Iterator<OMElement> streamIterator = streamsElement.getChildrenWithName(new QName("stream"));
        while (streamIterator.hasNext()) {
            OMElement streamElement = streamIterator.next();
            String streamDefinition = streamElement.getText();
            streamDefinitions.add(streamDefinition);
        }
        return streamDefinitions;
    }

    /**
     * Adding stream partitioned fields
     */
    private static void addPartitionFields(OMElement streamsElement, ComponentInfoHolder componentInfoHolder) throws
            StormQueryConstructionException {
        Iterator<OMElement> streamIterator = streamsElement.getChildrenWithName(new QName(EventProcessorConstants.STREAM));
        while (streamIterator.hasNext()) {
            OMElement streamElement = streamIterator.next();
            OMAttribute partitionAttribute = streamElement.getAttribute(new QName("partition"));
            if (partitionAttribute != null) {
                StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition(streamElement.getText());
                if (!Arrays.asList(streamDefinition.getAttributeNameArray()).contains(partitionAttribute
                        .getAttributeValue())) {
                    throw new StormQueryConstructionException("All input streams of the partition should have the " +
                            "partitioning attribute.");
                }
                componentInfoHolder.addStreamPartitioningField(streamDefinition.getId(), partitionAttribute.getAttributeValue());
            }
        }
    }
}