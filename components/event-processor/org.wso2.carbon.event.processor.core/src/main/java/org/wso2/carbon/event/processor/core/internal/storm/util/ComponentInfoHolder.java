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

import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.HashMap;
import java.util.Map;

public class ComponentInfoHolder {



    public enum ComponentType {EVENT_RECEIVER_SPOUT, SIDDHI_BOLT, EVENT_PUBLISHER_BOLT;}

    private ComponentType componentType;
    private String componentName = null;
    private Object declarer;
    private Map<String, StreamDefinition> inputStreams = new HashMap<String, StreamDefinition>();
    private Map<String, String> inputStreamPartitoningFields = new HashMap<String, String>();
    private Map<String, StreamDefinition> outputStreams = new HashMap<String, StreamDefinition>();
    private int parallelism = 1;
    private String query;

    public ComponentInfoHolder(String componentName, ComponentType componentType) {
        this.componentName = componentName;
        this.componentType = componentType;
    }

    public void addSiddhiQuery(String query){
        this.query = query;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public void addInputStream(String streamDefinition) {
        StreamDefinition siddhiStreamDefinition = SiddhiCompiler.parseStreamDefinition(streamDefinition);
        inputStreams.put(siddhiStreamDefinition.getId(), siddhiStreamDefinition);
    }

    public void addStreamPartitioningField(String streamId, String partitioningField){
        if (this.componentType != ComponentType.EVENT_RECEIVER_SPOUT){
            inputStreamPartitoningFields.put(streamId, partitioningField);
        }
    }

    public void addOutputStream(String streamDefinition) {
        StreamDefinition siddhiStreamDefinition = SiddhiCompiler.parseStreamDefinition(streamDefinition);
        outputStreams.put(siddhiStreamDefinition.getId(), siddhiStreamDefinition);
    }

    public String[] getInputStreamIds() {
        return inputStreams.keySet().toArray(new String[inputStreams.size()]);
    }

    public String[] getOutputStreamIds() {
        return outputStreams.keySet().toArray(new String[inputStreams.size()]);
    }

    public String getPartionenedField(String streamId){
        return inputStreamPartitoningFields.get(streamId);
    }

    public int getParallelism() {
        return parallelism;
    }

    public String getComponentName() {
        return componentName;
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
