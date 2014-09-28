package org.wso2.carbon.event.processor.storm.topology.util;

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.*;

public class ComponentInfoHolder {

    public static int GROUPING_TYPE_SHUFFLE_GROUPING = 1;

    public enum ComponentType {EVENT_RECEIVER_SPOUT, SIDDHI_BOLT, EVENT_PUBLISHER_BOLT;}

    private ComponentType componentType;
    private String componentName = null;
    private Object declarer;
    private HashMap<String, StreamDefinition> inputStreams = new HashMap<String, StreamDefinition>();
    private ArrayList<ExecutionPlan> siddhiQueries = new ArrayList<ExecutionPlan>();
    private HashMap<String, StreamDefinition> outputStreams = new HashMap<String, StreamDefinition>();
    private int parallelism = 1;
    private int groupingType = GROUPING_TYPE_SHUFFLE_GROUPING;
    private HashMap<String, String> groupingParameters = new HashMap<String, String>();

    public ComponentInfoHolder(String componentName, ComponentType componentType) {
        this.componentName = componentName;
        this.componentType = componentType;
    }

    public void addSiddhiQueries(List<ExecutionPlan> siddhiQueries) {
        this.siddhiQueries.addAll(siddhiQueries);
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public void setGroupingType(int groupingType) {
        this.groupingType = groupingType;
    }

    public void addGroupingParameter(String key, String value) {
        groupingParameters.put(key, value);
    }

    public void addInputStream(String streamId, StreamDefinition streamDefinition) {
        inputStreams.put(streamId, streamDefinition);
    }

    public void addOutputStream(String streamId, StreamDefinition streamDefinition) {
        outputStreams.put(streamId, streamDefinition);
    }

    public String[] getInputStreamIds() {
        return inputStreams.keySet().toArray(new String[inputStreams.size()]);
    }

    public String[] getOutputStreamIds() {
        return outputStreams.keySet().toArray(new String[inputStreams.size()]);
    }

//    public StreamDefinition[] getAllStreamDefinitions() {
//        Set<StreamDefinition> allStreamDefinitions = new HashSet<StreamDefinition>();
//
//        for (Map.Entry<String, StreamDefinition> entry : inputStreams.entrySet()) {
//            if (entry.getValue() != null) {
//                allStreamDefinitions.add(entry.getValue());
//            }
//        }
//
//        for (Map.Entry<String, StreamDefinition> entry : outputStreams.entrySet()) {
//            if (entry.getValue() != null) {
//                allStreamDefinitions.add(entry.getValue());
//            }
//        }
//
//        return allStreamDefinitions.toArray(new StreamDefinition[inputStreams.size()]);
//    }

    public int getParallelism() {
        return parallelism;
    }

    public int getGroupingType() {
        return groupingType;
    }

    public String getGroupingParameter(String key) {
        return groupingParameters.get(key);
    }

    public String getComponentName() {
        return componentName;
    }

    public List<ExecutionPlan> getSiddhiQuery() {
        return siddhiQueries;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setDeclarer(Object declarer) {
        this.declarer = declarer;
    }

    public Object getDeclarer() {
        return declarer;
    }
//    @Override
//    public String toString() {
//        StringBuilder message = new StringBuilder();
//        message.append("Component Name: ").append(componentName).append("\n");
//        message.append("Parallelism: ").append(parallelism).append("\n");
//        message.append("Grouping Type: ").append(groupingType).append("\n");
//        message.append("Grouping Parameters: ").append(groupingParameters.toString()).append("\n");
//
//        message.append("Input Streams:\n");
//        for (String inputStream : getInputStreamIds()) {
//            message.append("->").append(inputStream).append("\n");
//        }
//
//        message.append("Output Streams:\n");
//        for (String outputStream : getOutputStreamIds()) {
//            message.append("<-").append(outputStream).append("\n");
//        }
//
//        return message.toString();
//    }

}
