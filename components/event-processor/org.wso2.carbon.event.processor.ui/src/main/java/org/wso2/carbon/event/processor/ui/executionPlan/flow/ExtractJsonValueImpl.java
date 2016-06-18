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
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.ui.EventProcessorUIUtils;
import org.wso2.carbon.event.processor.ui.executionPlan.flow.siddhi.visitor.SiddhiFlowCompiler;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.common.spi.DataSourceReader;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminDataSourceException;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminStub;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtractJsonValueImpl {
    private static final Log log = LogFactory.getLog(ExtractJsonValueImpl.class);

    private String executionPlanText;
    //stream
    private List<String> streamId = new ArrayList<String>();
    private List<String> streamAnno = new ArrayList<String>();
    private List<String> streamElement = new ArrayList<String>();
    private List<String> streamText = new ArrayList<String>();
    private List<String> streamMapId = new ArrayList<String>();
    private List<String> streamDefinition = new ArrayList<String>();

    //trigger
    private List<String> triggerName = new ArrayList<String>();
    private List<String> triggerText = new ArrayList<String>();

    //table
    private List<String> tableId = new ArrayList<String>();
    private List<String> tableText = new ArrayList<String>();

    //function
    private List<String> functionName = new ArrayList<String>();
    private List<String> functionText = new ArrayList<String>();

    //query
    private List<JsonArray> inputStream = new ArrayList<JsonArray>();
    private List<JsonArray> innerStream_in = new ArrayList<JsonArray>();
    private List<JsonArray> outputStream = new ArrayList<JsonArray>();
    private List<JsonArray> innerStream_out = new ArrayList<JsonArray>();
    private List<String> queryID = new ArrayList<String>();
    private List<String> queryName = new ArrayList<String>();
    private List<String> queryText = new ArrayList<String>();
    private List<String> queryWithPartition = new ArrayList<String>();

    //partition
    private List<String> partitionWithText = new ArrayList<String>();
    private List<String> partitionText = new ArrayList<String>();
    private List<String> partitionWithStream = new ArrayList<String>();
    private List<JsonArray> partitionWithAttribute = new ArrayList<JsonArray>();
    private List<JsonArray> partitionWithCondition = new ArrayList<JsonArray>();
    private List<List<String>> partitionAttributeToolTip = new ArrayList<List<String>>();

    public void setJsonValues(String executionPlan, ServletConfig config, HttpSession session,
                              HttpServletRequest request) {

        StringBuilder executionPlan_String = SiddhiFlowCompiler.parseString(executionPlan);
        createStreamDefinition(executionPlan, config, session, request);

        JsonElement jsonElement = new JsonParser().parse(executionPlan_String.toString());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray executionPlanArray = jsonObject.getAsJsonArray("ExecutionPlan");

        for (int k = 0; k < executionPlanArray.size(); k++) {
            if (executionPlanArray.get(k).getAsJsonObject().get("Stream") != null) {
                JsonArray streamArray = executionPlanArray.get(k).getAsJsonObject().getAsJsonArray("Stream");
                setStreamJsonValue(streamArray);
            }

            if (executionPlanArray.get(k).getAsJsonObject().get("Table") != null) {
                JsonArray tableArray = executionPlanArray.get(k).getAsJsonObject().getAsJsonArray("Table");
                setTableJsonValue(tableArray);
            }

            if (executionPlanArray.get(k).getAsJsonObject().get("Query") != null) {
                JsonArray queryArray = executionPlanArray.get(k).getAsJsonObject().getAsJsonArray("Query");
                setQueryJsonValue(queryArray, null);
            }

            if (executionPlanArray.get(k).getAsJsonObject().get("Trigger") != null) {
                JsonArray triggerArray = executionPlanArray.get(k).getAsJsonObject().getAsJsonArray("Trigger");
                setTriggerJsonValue(triggerArray);
            }

            if (executionPlanArray.get(k).getAsJsonObject().get("Function") != null) {
                JsonArray functionArray = executionPlanArray.get(k).getAsJsonObject().getAsJsonArray("Function");
                setTriggerJsonValue(functionArray);
            }

            if (executionPlanArray.get(k).getAsJsonObject().get("Partition") != null) {
                JsonArray partitionArray = executionPlanArray.get(k).getAsJsonObject().getAsJsonArray("Partition");
                setPartitionJsonValue(partitionArray);
            }

            if (executionPlanArray.get(k).getAsJsonObject().get("executionPlan_Text") != null) {
                executionPlanText = executionPlanArray.get(k).getAsJsonObject().get("executionPlan_Text").toString();
            }
        }
    }

    //create stream definition
    private void createStreamDefinition(String executionPlan, ServletConfig config, HttpSession session,
                                        HttpServletRequest request) {

        SiddhiManager manager = new SiddhiManager();
        try {
            NDataSourceAdminStub stub = EventProcessorUIUtils.getNDataSourceAdminStub(config, session, request);
            WSDataSourceInfo[] allDataSources = stub.getAllDataSources();
            DataSourceReader dsReader = new RDBMSDataSourceReader();
            for (WSDataSourceInfo info : allDataSources) {
                WSDataSourceMetaInfo metaInfo = info.getDsMetaInfo();
                Object dsObject = dsReader.createDataSource(metaInfo.getDefinition().getDsXMLConfiguration(), false);
                if (dsObject instanceof javax.sql.DataSource) {
                    manager.setDataSource(metaInfo.getName(), (javax.sql.DataSource) dsObject);
                }
            }
        } catch (AxisFault axisFault) {
            log.error("Error in getting data sources from NDataSourceAdminService", axisFault);
        } catch (RemoteException e) {
            log.error("Error in getting data sources from NDataSourceAdminService", e);
        } catch (NDataSourceAdminDataSourceException e) {
            log.error("Error in getting data sources from NDataSourceAdminService", e);
        } catch (DataSourceException e) {
            log.error("Error in getting data sources from NDataSourceAdminService", e);
        }

        //Specially handle for events tables
        String executionPlanCleaned = executionPlan.replaceAll("@from\\(.*?\\)", "");
        ExecutionPlanRuntime executionPlanRuntime = manager.createExecutionPlanRuntime(executionPlanCleaned);

        Map<String, AbstractDefinition> streamDefinitionMap = executionPlanRuntime.getStreamDefinitionMap();
        List<List<Attribute>> attributeList = new ArrayList<List<Attribute>>();

        for (AbstractDefinition ab : streamDefinitionMap.values()) {
            streamMapId.add(ab.getId());
            attributeList.add(ab.getAttributeList());
        }

        for (int j = 0; j < attributeList.size(); j++) {
            StringBuilder stream = new StringBuilder();
            stream.append("\"define stream ").append(streamMapId.get(j)).append(" (");
            for (int i = 0; i < attributeList.get(j).size(); i++) {
                stream.append(" ").append(attributeList.get(j).get(i).getName()).append(" ").append(attributeList.get(j).get(i).getType().toString().toLowerCase()).append(",");
            }
            stream = new StringBuilder(stream.substring(0, stream.length() - 1));
            stream.append(")\"");
            streamDefinition.add(stream.toString());
        }
    }

    //Stream
    private void setStreamJsonValue(JsonArray streamArray) {
        for (int i = 0; i < streamArray.size(); i++) {
            JsonObject streamObj = streamArray.get(i).getAsJsonObject();
            streamText.add(streamObj.get("stream_Text").toString());
            streamId.add(streamObj.get("streamId").toString());
            streamAnno.add(streamObj.get("annoName").toString());
            streamElement.add(streamObj.get("annoElement").toString());
        }
    }

    //trigger
    private void setTriggerJsonValue(JsonArray trigerArray) {
        for (int i = 0; i < trigerArray.size(); i++) {
            JsonObject streamObj = trigerArray.get(i).getAsJsonObject();
            triggerName.add(streamObj.get("triggerName").toString());
            triggerText.add(streamObj.get("triggerText").toString());
        }
    }

    //function
    private void setFunctionJsonValue(JsonArray trigerArray) {
        for (int i = 0; i < trigerArray.size(); i++) {
            JsonObject streamObj = trigerArray.get(i).getAsJsonObject();
            functionName.add(streamObj.get("functionName").toString());
            functionText.add(streamObj.get("functionText").toString());
        }
    }

    //table
    private void setTableJsonValue(JsonArray tableArray) {
        for (int i = 0; i < tableArray.size(); i++) {
            JsonObject streamObj = tableArray.get(i).getAsJsonObject();
            tableText.add(streamObj.get("table_Text").toString());
            tableId.add(streamObj.get("tableId").toString());
        }
    }

    //query
    private void setQueryJsonValue(JsonArray queryArray, String partitionWith) {
        for (int i = 0; i < queryArray.size(); i++) {
            JsonObject queryObj = queryArray.get(i).getAsJsonObject();
            queryName.add(queryObj.get("annotationElement").toString());
            queryText.add(queryObj.get("query_Text").toString().replaceAll("\'", "\\\\'"));
            queryWithPartition.add(partitionWith);

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
    private void setPartitionJsonValue(JsonArray partitionArray) {
        for (int m = 0; m < partitionArray.size(); m++) {
            JsonObject partitionObj = partitionArray.get(m).getAsJsonObject();
            partitionWithText.add(partitionObj.get("Partition_with_Text").toString());
            partitionText.add(partitionObj.get("Partition_Text").toString());

            JsonArray partitionWith = partitionObj.getAsJsonArray("PartitionWith");
            JsonObject partitionWithObj = partitionWith.get(0).getAsJsonObject();
            partitionWithStream.add(partitionWithObj.get("Partition_Stream").toString());
            partitionWithAttribute.add(partitionWithObj.get("attribute").getAsJsonArray());
            partitionWithCondition.add(partitionWithObj.get("condition").getAsJsonArray());

            for (int j = 0; j < partitionObj.get("Query_size").getAsInt(); j++) {
                JsonArray partitionQuery_Array = partitionObj.getAsJsonArray("Query_" + j);
                JsonObject partitionQuery_Obj = partitionQuery_Array.get(0).getAsJsonObject();
                JsonArray queryArray = partitionQuery_Obj.getAsJsonObject().getAsJsonArray("Query");

                setQueryJsonValue(queryArray, partitionObj.get("Partition_with_Text").toString());
            }
        }
    }

    public List<List<String>> getPartitionAttributeToolTip() {
        for (int i = 0; i < partitionWithAttribute.size(); i++) {
            List<String> attribute_toolTip = new ArrayList<String>();
            for (int j = 0; j < partitionWithAttribute.get(i).size(); j++) {
                if (!partitionWithCondition.get(i).toString().equals("[null]")) {
                    JsonArray attribute = partitionWithAttribute.get(i);
                    JsonArray condition = partitionWithCondition.get(i);
                    if (j == 0) {
                        attribute_toolTip.add("\"" + attribute.get(j).getAsString() + " : " + condition.get(j).getAsString() + "\"");
                    } else {
                        attribute_toolTip.add("\"\\n" + attribute.get(j).getAsString() + " : " + condition.get(j).getAsString() + "\"");
                    }
                } else {
                    attribute_toolTip.add(null);
                }
            }
            partitionAttributeToolTip.add(attribute_toolTip);
        }
        return partitionAttributeToolTip;
    }

    public List<String> getQueryID() {
        for (int i = 0; i < queryName.size(); i++) {
            queryID.add("\"Q_" + i + "\"");
        }
        return queryID;
    }

    public String getExecutionPlanText() {
        return executionPlanText;
    }

    public List<String> getStreamId() {
        return streamId;
    }

    public List<String> getStreamAnno() {
        return streamAnno;
    }

    public List<String> getStreamElement() {
        return streamElement;
    }

    public List<String> getStreamText() {
        return streamText;
    }

    public List<String> getTableId() {
        return tableId;
    }

    public List<String> getTableText() {
        return tableText;
    }

    public List<String> getTriggerId() {
        return triggerName;
    }

    public List<String> getTriggerText() {
        return triggerText;
    }

    public List<String> getFunctionId() {
        return functionName;
    }

    public List<String> getFunctionText() {
        return functionText;
    }

    public List<JsonArray> getInputStream() {
        return inputStream;
    }

    public List<JsonArray> getInnerInputStream() {
        return innerStream_in;
    }

    public List<JsonArray> getInnerOutputStream() {
        return innerStream_out;
    }

    public List<JsonArray> getOutputStream() {
        return outputStream;
    }

    public List<String> getQueryText() {
        return queryText;
    }

    public List<String> getQueryName() {
        return queryName;
    }

    public List<String> getPartitionWithQuery() {
        return queryWithPartition;
    }

    public List<String> getPartitionWithText() {
        return partitionWithText;
    }

    public List<String> getPartitionText() {
        return partitionText;
    }

    public List<JsonArray> getPartitionWithAttribute() {
        return partitionWithAttribute;
    }

    public List<String> getPartitionWithStream() {
        return partitionWithStream;
    }

    public List<String> getStreamMapId() {
        return streamMapId;
    }

    public List<String> getStreamDefinition() {
        return streamDefinition;
    }
}
