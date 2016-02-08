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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.event.processor.ui.executionPlan.flow.siddhi.visitor.SiddhiFlowCompiler;

import java.util.ArrayList;
import java.util.List;

public class ExtractJsonValueImpl {
    private String executionPlan_Text;
    //stream
    private List<String> streamId = new ArrayList<String>();
    private List<String> streamAnno = new ArrayList<String>();
    private List<String> streamElement = new ArrayList<String>();
    private List<String> stream_Text = new ArrayList<String>();

    //table
    private List<String> tableId = new ArrayList<String>();
    private List<String> tableAnno = new ArrayList<String>();
    private List<String> tableElement = new ArrayList<String>();
    private List<String> table_Text = new ArrayList<String>();

    //query
    private List<JsonArray> inputStream = new ArrayList<JsonArray>();
    private List<JsonArray> innerStream_in = new ArrayList<JsonArray>();
    private List<JsonArray> outputStream = new ArrayList<JsonArray>();
    private List<JsonArray> innerStream_out = new ArrayList<JsonArray>();
    private List<String> queryID = new ArrayList<String>();
    private List<String> queryName = new ArrayList<String>();
    private List<String> query_Text = new ArrayList<String>();
    private List<String> query_withPartition = new ArrayList<String>();

    //partition
    private List<String> partition_with_Text = new ArrayList<String>();
    private List<String> partition_Text = new ArrayList<String>();
    private List<String> partition_with_stream = new ArrayList<String>();
    private List<JsonArray> partition_with_attribute = new ArrayList<JsonArray>();
    private List<JsonArray> partition_with_condition = new ArrayList<JsonArray>();
    private List<List<String>> partitionAttribute_toolTip = new ArrayList<List<String>>();


    public void set_jsonValues(String executionPlan) {

        StringBuilder executionPlan_String = SiddhiFlowCompiler.parseString(executionPlan);

        JsonElement jsonElement = new JsonParser().parse(executionPlan_String.toString());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray exeplanArray = jsonObject.getAsJsonArray("ExecutionPlan");

        for (int k = 0; k < exeplanArray.size(); k++) {
            if (exeplanArray.get(k).getAsJsonObject().get("Stream") != null) {
                JsonArray streamArray = exeplanArray.get(k).getAsJsonObject().getAsJsonArray("Stream");
                set_streamJsonValue(streamArray);
            }

            if (exeplanArray.get(k).getAsJsonObject().get("Table") != null) {
                JsonArray tableArray = exeplanArray.get(k).getAsJsonObject().getAsJsonArray("Table");
                set_tableJsonValue(tableArray);
            }

            if (exeplanArray.get(k).getAsJsonObject().get("Query") != null) {
                JsonArray queryArray = exeplanArray.get(k).getAsJsonObject().getAsJsonArray("Query");
                set_queryJsonValue(queryArray, null);
            }

            if (exeplanArray.get(k).getAsJsonObject().get("Partition") != null) {
                JsonArray partitionArray = exeplanArray.get(k).getAsJsonObject().getAsJsonArray("Partition");
                set_partitionJsonValue(partitionArray);
            }

            if (exeplanArray.get(k).getAsJsonObject().get("executionPlan_Text") != null) {
                executionPlan_Text = exeplanArray.get(k).getAsJsonObject().get("executionPlan_Text").toString();
            }
        }
    }

    //Stream
    private void set_streamJsonValue(JsonArray streamArray) {
        for (int i = 0; i < streamArray.size(); i++) {
            JsonObject streamObj = streamArray.get(i).getAsJsonObject();
            stream_Text.add(streamObj.get("stream_Text").toString());
            streamId.add(streamObj.get("streamId").toString());
            streamAnno.add(streamObj.get("annoName").toString());
            streamElement.add(streamObj.get("annoElement").toString());
        }
    }

    //table
    private void set_tableJsonValue(JsonArray tableArray) {
        for (int i = 0; i < tableArray.size(); i++) {
            JsonObject streamObj = tableArray.get(i).getAsJsonObject();
            table_Text.add(streamObj.get("table_Text").toString());
            tableId.add(streamObj.get("tableId").toString());
            tableAnno.add(streamObj.get("annoName").toString());
            tableElement.add(streamObj.get("annoElement").toString());
        }
    }

    //query
    private void set_queryJsonValue(JsonArray queryArray, String partitionWith) {
        for (int i = 0; i < queryArray.size(); i++) {
            JsonObject queryObj = queryArray.get(i).getAsJsonObject();
            queryName.add(queryObj.get("annotationElement").toString());
            query_Text.add(queryObj.get("query_Text").toString().replaceAll("\'", "\\\\'"));
            query_withPartition.add(partitionWith);

            if (queryObj.get("inputStream").isJsonObject()) {
                JsonObject inputStreamObj = queryObj.getAsJsonObject("inputStream");
                inputStream.add(inputStreamObj.get("streamId").getAsJsonArray());
                innerStream_in.add(inputStreamObj.get("innerStream").getAsJsonArray());
            } else {
                JsonArray inputArray = queryObj.get("inputStream").getAsJsonArray();
                StringBuilder stream = new StringBuilder("[");
                StringBuilder inner = new StringBuilder("[");
                for (int l = 0; l < inputArray.size(); l++) {
                    JsonObject jObj = inputArray.get(l).getAsJsonObject();
                    stream.append(jObj.get("streamId").getAsString()).append(",");
                    inner.append(jObj.get("innerStream").getAsString()).append(",");
                }
                stream = new StringBuilder(stream.substring(0, stream.length() - 1));
                stream.append("]");
                inner = new StringBuilder(inner.substring(0, inner.length() - 1));
                inner.append("]");
                JsonElement streamElement = new JsonParser().parse(stream.toString());
                JsonArray streamArray = streamElement.getAsJsonArray();
                JsonElement innerElement = new JsonParser().parse(inner.toString());
                JsonArray innerArray = innerElement.getAsJsonArray();
                inputStream.add(streamArray);
                innerStream_in.add(innerArray);
            }
            JsonObject outputStreamObj = queryObj.getAsJsonObject("outputStream");
            outputStream.add(outputStreamObj.get("streamId").getAsJsonArray());
            innerStream_out.add(outputStreamObj.get("innerStream").getAsJsonArray());
        }
    }

    //partition
    private void set_partitionJsonValue(JsonArray partitionArray) {
        for (int m = 0; m < partitionArray.size(); m++) {
            JsonObject partitionObj = partitionArray.get(m).getAsJsonObject();
            partition_with_Text.add(partitionObj.get("Partition_with_Text").toString());
            partition_Text.add(partitionObj.get("Partition_Text").toString());

            JsonArray partitionWith = partitionObj.getAsJsonArray("PartitionWith");
            JsonObject partitionWithObj = partitionWith.get(0).getAsJsonObject();
            partition_with_stream.add(partitionWithObj.get("Partition_Stream").toString());
            partition_with_attribute.add(partitionWithObj.get("attribute").getAsJsonArray());
            partition_with_condition.add(partitionWithObj.get("condition").getAsJsonArray());

            for (int j = 0; j < partitionObj.get("Query_size").getAsInt(); j++) {
                JsonArray partitionQuery_Array = partitionObj.getAsJsonArray("Query_" + j);
                JsonObject partitionQuery_Obj = partitionQuery_Array.get(0).getAsJsonObject();
                JsonArray queryArray = partitionQuery_Obj.getAsJsonObject().getAsJsonArray("Query");

                set_queryJsonValue(queryArray, partitionObj.get("Partition_with_Text").toString());
            }
        }
    }

    public List<List<String>> getPartitionAttribute_toolTip() {
        for (int i = 0; i < partition_with_attribute.size(); i++) {
            List<String> attribute_toolTip = new ArrayList<String>();
            for (int j = 0; j < partition_with_attribute.get(i).size(); j++) {
                if (!partition_with_condition.get(i).toString().equals("[null]")) {
                    JsonArray attribute = partition_with_attribute.get(i);
                    JsonArray condition = partition_with_condition.get(i);
                    if (j == 0) {
                        attribute_toolTip.add("\"" + attribute.get(j).getAsString() + " : " + condition.get(j).getAsString() + "\"");
                    } else {
                        attribute_toolTip.add("\"\\n" + attribute.get(j).getAsString() + " : " + condition.get(j).getAsString() + "\"");
                    }
                } else {
                    attribute_toolTip.add(null);
                }
            }
            partitionAttribute_toolTip.add(attribute_toolTip);
        }
        return partitionAttribute_toolTip;
    }

    public List<String> get_queryID() {
        for (int i = 0; i < queryName.size(); i++) {
            queryID.add("\"Q_" + i + "\"");
        }
        return queryID;
    }

    public String get_executionPlan_Text() {
        return executionPlan_Text;
    }

    public List<String> get_streamId() {
        return streamId;
    }

    public List<String> get_streamAnno() {
        return streamAnno;
    }

    public List<String> get_streamElement() {
        return streamElement;
    }

    public List<String> get_streamText() {
        return stream_Text;
    }

    public List<String> get_tableId() {
        return tableId;
    }

    public List<String> get_tableText() {
        return table_Text;
    }

    public List<String> get_tableAnno() {
        return tableAnno;
    }

    public List<String> get_tableElement() {
        return tableElement;
    }

    public List<JsonArray> get_inputStream() {
        return inputStream;
    }

    public List<JsonArray> get_innerStreamIn() {
        return innerStream_in;
    }

    public List<JsonArray> get_innerStreamOut() {
        return innerStream_out;
    }

    public List<JsonArray> get_outputStream() {
        return outputStream;
    }

    public List<String> get_queryText() {
        return query_Text;
    }

    public List<String> get_queryName() {
        return queryName;
    }

    public List<String> get_partitonWith_query() {
        return query_withPartition;
    }

    public List<String> get_partitionWithText() {
        return partition_with_Text;
    }

    public List<String> get_partitionText() {
        return partition_Text;
    }

    public List<JsonArray> get_partitionWithAttribute() {
        return partition_with_attribute;
    }

    public List<String> get_partitionWithStream() {
        return partition_with_stream;
    }
}



