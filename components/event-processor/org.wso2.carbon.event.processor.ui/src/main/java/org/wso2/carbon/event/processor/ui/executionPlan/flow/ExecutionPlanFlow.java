/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.ui.executionPlan.flow;

import com.google.gson.JsonArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class ExecutionPlanFlow {

    private static final Log log = LogFactory.getLog(ExecutionPlanFlow.class);

    public String getExecutionPlanFlow(String executionPlanString) {

        try {
            ExtractJsonValueImpl converter = new ExtractJsonValueImpl();

            converter.setJsonValues(executionPlanString);
            StringBuilder executionPlanFlow = new StringBuilder(" '{ \"nodes\": [ ");

            List<String> streamId = converter.getStreamId();
            List<String> streamText = converter.getStreamText();
            List<String> streamAnno = converter.getStreamAnno();
            List<String> streamElement = converter.getStreamElement();
            List<String> streamMapId = converter.getStreamMapId();
            List<String> streamDefinition = converter.getStreamDefinition();

            List<String> tableId = converter.getTableId();
            List<String> tableText = converter.getTableText();

            List<String> triggerId = converter.getTriggerId();
            List<String> triggerText = converter.getTriggerText();

            List<String> functionId = converter.getFunctionId();
            List<String> functionText = converter.getFunctionText();

            List<String> query_text = converter.getQueryText();
            List<String> queryName = converter.getQueryName();
            List<String> queryId = converter.getQueryID();
            List<JsonArray> inputStream = converter.getInputStream();
            List<JsonArray> innerInputStream = converter.getInnerInputStream();
            List<JsonArray> outputStream = converter.getOutputStream();
            List<JsonArray> innerOutputStream = converter.getInnerOutputStream();
            List<String> queryPartitionWith = converter.getPartitionWithQuery();

            List<String> partitionWithText = converter.getPartitionWithText();
            List<String> partitionText = converter.getPartitionText();
            List<JsonArray> attribute = converter.getPartitionWithAttribute();
            List<List<String>> partitionAttributeToolTip = converter.getPartitionAttributeToolTip();

            //stream def node
            for (int i = 0; i < streamId.size(); i++) {
                if (!streamAnno.get(i).equals("null")) {
                    if (streamElement.get(i).equals("null")) {
                        executionPlanFlow.append("{ \"id\": \"").append(i).append("\", \"label\":").append(streamId.get(i)).append(",");
                    } else {
                        executionPlanFlow.append("{ \"id\": ").append(streamElement.get(i).toUpperCase()).append(", \"label\":").append(streamElement.get(i)).append(",");
                    }
                    executionPlanFlow.append("\"toolTip\": ").append(streamText.get(i)).append(",");
                    if (streamAnno.get(i).equals("\"Import\"")) {
                        executionPlanFlow.append("\"nodeclass\": \"I\"").append("},");
                    } else if (streamAnno.get(i).equals("\"Export\"")) {
                        executionPlanFlow.append("\"nodeclass\": \"E\"").append("},");
                    }
                }
            }

            //table def node
            for (int i = 0; i < tableId.size(); i++) {
                executionPlanFlow.append("{ \"id\": ").append(tableId.get(i).toUpperCase()).append(", \"label\":").append(tableId.get(i)).append(",");
                executionPlanFlow.append("\"toolTip\": ").append(tableText.get(i)).append(",");
                executionPlanFlow.append("\"nodeTable\": \"T\"").append("},");
            }

            //trigger def node
            for (int i = 0; i < triggerId.size(); i++) {
                executionPlanFlow.append("{ \"id\": ").append(triggerId.get(i).toUpperCase()).append(", \"label\":").append(triggerId.get(i)).append(",");
                executionPlanFlow.append("\"toolTip\": ").append(triggerText.get(i)).append(",");
                executionPlanFlow.append("\"nodeTable\": \"TR\"").append("},");
            }

            //fuction def node
            for (int i = 0; i < functionId.size(); i++) {
                executionPlanFlow.append("{ \"id\": ").append(functionId.get(i).toUpperCase()).append(", \"label\":").append(functionId.get(i)).append(",");
                executionPlanFlow.append("\"toolTip\": ").append(functionText.get(i)).append(",");
                executionPlanFlow.append("\"nodeTable\": \"TR\"").append("},");
            }

            //inputStream node
            for (int i = 0; i < inputStream.size(); i++) {
                JsonArray stream = inputStream.get(i);
                for (int j = 0; j < stream.size(); j++) {
                    executionPlanFlow.append("{ \"id\":\"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"label\":\"").append(stream.get(j).getAsString()).append("\",");
                    JsonArray inner = innerInputStream.get(i);
                    String isInner = ("true");
                    if (inner.get(j).getAsString().equals(isInner)) {
                        executionPlanFlow.append("\"parent\": ").append(queryPartitionWith.get(i).toUpperCase()).append(",");
                    }
                    for (int k = 0; k < tableId.size(); k++) {
                        String inId = ("\"" + stream.get(j).getAsString() + "\"");
                        if (tableId.get(k).equals(inId)) {
                            executionPlanFlow.append("\"nodeTable\": \"T\"").append(",");
                            executionPlanFlow.append("\"toolTip\": ").append(tableText.get(k)).append(",");
                        }
                    }
                    for (int l = 0; l < streamMapId.size(); l++) {
                        String inId = (stream.get(j).getAsString());
                        if (streamMapId.get(l).equals(inId)) {
                            executionPlanFlow.append("\"toolTip\": ").append(streamDefinition.get(l)).append(",");
                        }
                    }
                    executionPlanFlow.append("\"nodeclass\": \"S\"").append("},");
                }
            }

            //outputStream Node
            for (int i = 0; i < outputStream.size(); i++) {
                if (!outputStream.get(i).getAsString().equals("null")) {
                    JsonArray stream = outputStream.get(i);
                    for (int j = 0; j < stream.size(); j++) {
                        executionPlanFlow.append("{ \"id\":\"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"label\":\"").append(stream.get(j).getAsString()).append("\",");
                        JsonArray inner = innerOutputStream.get(i);
                        if (inner.get(j).getAsString().equals("\"true\"")) {
                            executionPlanFlow.append("\"parent\": ").append(queryPartitionWith.get(i).toUpperCase()).append(",");
                        }
                        for (int k = 0; k < tableId.size(); k++) {
                            String outId = ("\"" + stream.get(j).getAsString() + "\"");
                            if (tableId.get(k).equals(outId)) {
                                executionPlanFlow.append("\"nodeTable\": \"T\"").append(",");
                                executionPlanFlow.append("\"toolTip\": ").append(tableText.get(k)).append(",");
                            }
                        }
                        for (int l = 0; l < streamMapId.size(); l++) {
                            String outId = (stream.get(j).getAsString());
                            if (streamMapId.get(l).equals(outId)) {
                                executionPlanFlow.append("\"toolTip\": ").append(streamDefinition.get(l)).append(",");
                            }
                        }
                        executionPlanFlow.append("\"nodeclass\": \"S\"").append("},");
                    }
                }
            }

            //query Node
            for (int i = 0; i < queryName.size(); i++) {
                executionPlanFlow.append("{ \"id\": ").append(queryId.get(i)).append(", \"label\":").append(queryName.get(i)).append(",");
                if (queryPartitionWith.get(i) != null) {
                    executionPlanFlow.append("\"parent\": ").append(queryPartitionWith.get(i).toUpperCase()).append(",");
                }
                executionPlanFlow.append("\"toolTip\": ").append(query_text.get(i)).append(",\"nodeclass\": \"Q\"").append("},");
            }

            //partition with attribute node
            for (int i = 0; i < attribute.size(); i++) {
                executionPlanFlow.append("{ \"id\": ").append(attribute.get(i).toString().toUpperCase()).append(", \"label\":").append(attribute.get(i).toString()).append(",\"nodeclass\": \"P\"").append(",\"parent\": ").append(partitionWithText.get(i).toUpperCase()).append(",\"toolTip\": ").append(partitionAttributeToolTip.get(i).toString()).append("},");
            }
            //partition with as node
            executionPlanFlow = new StringBuilder(executionPlanFlow.substring(0, executionPlanFlow.length() - 1));
            executionPlanFlow.append("], \"partitionWith\": [ ");

            for (int i = 0; i < partitionWithText.size(); i++) {
                executionPlanFlow.append("{ \"id\": ").append(partitionWithText.get(i).toUpperCase()).append(", \"label\":").append("\"*\"").append(",\"nodeclass\": \"PW\"").append(",\"toolTip\": ").append(partitionText.get(i)).append("},");
            }

            //edges
            executionPlanFlow = new StringBuilder(executionPlanFlow.substring(0, executionPlanFlow.length() - 1));
            executionPlanFlow.append("], \"edges\": [ ");

            // query and outputStream
            for (int i = 0; i < outputStream.size(); i++) {
                if (!outputStream.get(i).getAsString().equals("null")) {
                    JsonArray stream = outputStream.get(i);
                    for (int j = 0; j < stream.size(); j++) {
                        executionPlanFlow.append("  { \"from\": ").append(queryId.get(i)).append(", \"to\":\"").append(stream.get(j).getAsString().toUpperCase()).append("\" },");
                    }
                }
            }

            // query and inputStream
            for (int i = 0; i < inputStream.size(); i++) {
                JsonArray stream = inputStream.get(i);
                for (int j = 0; j < stream.size(); j++) {
                    JsonArray inner = innerInputStream.get(i);
                    if (!inner.get(j).getAsString().equals("true")) {
                        if (queryPartitionWith.get(i) == null) {
                            executionPlanFlow.append("  { \"from\": \"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"to\":").append(queryId.get(i)).append(" },");
                        } else {
                            for (int k = 0; k < partitionWithText.size(); k++) {
                                if (queryPartitionWith.get(i).equals(partitionWithText.get(k))) {
                                    executionPlanFlow.append("  { \"from\": \"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"to\":").append(attribute.get(k).toString().toUpperCase()).append(" },");
                                    executionPlanFlow.append("  { \"from\": ").append(attribute.get(k).toString().toUpperCase()).append(", \"to\":").append(queryId.get(i)).append(" },");
                                }
                            }
                        }
                    } else {
                        executionPlanFlow.append("  { \"from\": \"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"to\":").append(queryId.get(i)).append(" },");
                    }
                }
            }

            //import stream
            for (int i = 0; i < inputStream.size(); i++) {
                JsonArray stream = inputStream.get(i);
                for (int j = 0; j < stream.size(); j++) {
                    String inStream = ("\"" + stream.get(j).getAsString() + "\"");
                    for (int k = 0; k < streamId.size(); k++) {
                        if (streamId.get(k).equals(inStream) && !streamElement.get(k).equals("null")) {
                            executionPlanFlow.append("  { \"from\": ").append(streamElement.get(k).toUpperCase()).append(", \"to\":\"").append(stream.get(j).getAsString().toUpperCase()).append("\" },");
                        } else if (streamId.get(k).equals(inStream) && streamAnno.get(k).equals("\"Import\"")) {
                            executionPlanFlow.append("  { \"from\": \"").append(k).append("\", \"to\":\"").append(stream.get(j).getAsString().toUpperCase()).append("\" },");
                        }
                    }
                }
            }

            //export stream
            for (int i = 0; i < outputStream.size(); i++) {
                if (!outputStream.get(i).getAsString().equals("null")) {
                    JsonArray stream = outputStream.get(i);
                    for (int j = 0; j < stream.size(); j++) {
                        String outStream = ("\"" + stream.get(j).getAsString() + "\"");
                        for (int k = 0; k < streamId.size(); k++) {
                            if (streamId.get(k).equals(outStream) && !streamElement.get(k).equals("null")) {
                                executionPlanFlow.append("  { \"from\": \"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"to\":").append(streamElement.get(k).toUpperCase()).append(" },");
                            } else if (streamId.get(k).equals(outStream) && streamAnno.get(k).equals("\"Export\"")) {
                                executionPlanFlow.append("  { \"from\": \"").append(stream.get(j).getAsString().toUpperCase()).append("\", \"to\":\"").append(k).append("\" },");
                            }
                        }
                    }
                }
            }
            executionPlanFlow = new StringBuilder(executionPlanFlow.substring(0, executionPlanFlow.length() - 1));
            executionPlanFlow.append("]}'");

            return executionPlanFlow.toString();
        } catch (RuntimeException e) {
            log.error("Error in visualizing execution plan '" + executionPlanString + "', " + e.getMessage(), e);
            return null;
        }
    }
}

