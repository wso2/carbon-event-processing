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

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentInfoHolder {

    public static int GROUPING_TYPE_SHUFFLE_GROUPING = 1;

    public enum ComponentType {EVENT_RECEIVER_SPOUT, SIDDHI_BOLT, EVENT_PUBLISHER_BOLT;}

    private ComponentType componentType;
    private String componentName = null;
    private Object declarer;
    private Map<String, StreamDefinition> inputStreams = new HashMap<String, StreamDefinition>();
    private List<ExecutionPlan> siddhiQueries = new ArrayList<ExecutionPlan>();
    private Map<String, StreamDefinition> outputStreams = new HashMap<String, StreamDefinition>();
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

    public List<ExecutionPlan> getSiddhiQueries() {
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

}
