package org.wso2.carbon.event.processor.storm.topology.util;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.TopologyBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.storm.common.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.storm.topology.component.EventPublisherBolt;
import org.wso2.carbon.event.processor.storm.topology.component.EventReceiverSpout;
import org.wso2.carbon.event.processor.storm.topology.component.SiddhiBolt;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
                componentInfoHolder.addInputStream(streamDefinition.getStreamId(), streamDefinition);
                componentInfoHolder.addOutputStream(streamDefinition.getStreamId(), streamDefinition);
            }

            componentInfoHolder.setDeclarer(builder.setSpout(name, new EventReceiverSpout(stormDeploymentConfig, streamDefinitions, executionPlanName, tenantId), Integer.parseInt(parallel)));
            topologyInfoHolder.addComponent(componentInfoHolder);
        }

        //******** Processor ************
        iterator = queryPlanElement.getChildrenWithName(new QName("event-processor"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String name = eventProcessorElement.getAttributeValue(new QName("name"));
            String parallel = eventProcessorElement.getAttributeValue(new QName("parallel"));
            ComponentInfoHolder componentInfoHolder = new ComponentInfoHolder(name, ComponentInfoHolder.ComponentType.SIDDHI_BOLT);

            List<StreamDefinition> inputStreamDefinitions = getStreamDefinitions(eventProcessorElement.getFirstChildWithName(new QName("input-streams")));
            for (StreamDefinition streamDefinition : inputStreamDefinitions) {
                componentInfoHolder.addInputStream(streamDefinition.getStreamId(), streamDefinition);
            }

            List<ExecutionPlan> executionPlans = new ArrayList<ExecutionPlan>(inputStreamDefinitions);
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

            List<StreamDefinition> inputStreamDefinitions = getStreamDefinitions(eventProcessorElement.getFirstChildWithName(new QName("input-streams")));
            for (StreamDefinition streamDefinition : inputStreamDefinitions) {
                componentInfoHolder.addInputStream(streamDefinition.getStreamId(), streamDefinition);
            }

            List<ExecutionPlan> executionPlans = new ArrayList<ExecutionPlan>(inputStreamDefinitions);
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

        for (ComponentInfoHolder componentInfoHolder : topologyInfoHolder.getComponents()) {
            if (componentInfoHolder.getComponentType() != ComponentInfoHolder.ComponentType.EVENT_RECEIVER_SPOUT) {
                BoltDeclarer boltDeclarer = (BoltDeclarer) componentInfoHolder.getDeclarer();
                for (String streamId : componentInfoHolder.getInputStreamIds()) {
                    for (ComponentInfoHolder pubComponent : topologyInfoHolder.getPublishingComponents(streamId)) {
                        if (!pubComponent.getComponentName().equals(componentInfoHolder.getComponentName())) {
                            boltDeclarer.shuffleGrouping(pubComponent.getComponentName(), streamId);
                        }
                    }
                }
            }
        }

        return builder;
    }

    private static List<StreamDefinition> getStreamDefinitions(OMElement streamsElement) {
        List<StreamDefinition> inputStreamDefinitions = new ArrayList<StreamDefinition>();
        Iterator<OMElement> inputStreamIterator = streamsElement.getChildrenWithName(new QName("stream"));
        while (inputStreamIterator.hasNext()) {
            OMElement inputStreamElement = inputStreamIterator.next();
            StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition(inputStreamElement.getText());
            inputStreamDefinitions.add(streamDefinition);
        }
        return inputStreamDefinitions;
    }
}