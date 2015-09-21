<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.wso2.carbon.siddhi.tryit.ui.SiddhiTryItClient" %>
<%@ page import="java.util.Map" %>

<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.JsonArray" %>
<%@ page import="com.google.gson.JsonObject" %>

<%
    String executionplan = request.getParameter("executionplan");
    String eventStream = request.getParameter("eventstream");
    String dateTime = request.getParameter("datetime");

    SiddhiTryItClient siddhiTryItClientObject = new SiddhiTryItClient();
    JsonObject jsonObject = new JsonObject();

    try {
        Map<String, StringBuilder> map = siddhiTryItClientObject.processData(executionplan, eventStream, dateTime);
        Object[] resultMapKeysArray = map.keySet().toArray();
        Object[] resultMapValuesArray = map.values().toArray();

        //Create JSON array
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < map.size(); i++) {
            JsonObject object = new JsonObject();
            object.addProperty("key", resultMapKeysArray[i].toString());
            object.addProperty("jsonValue", resultMapValuesArray[i].toString());
            jsonArray.add(object);
        }

        String toStringJsonArray = new Gson().toJson(jsonArray);
        jsonObject.addProperty("success", "true");
        jsonObject.addProperty("jsonValue", toStringJsonArray);
    } catch (Exception e) {
        jsonObject.addProperty("success", "false");
        jsonObject.addProperty("jsonValue", e.getMessage());
    }
%>
<%=jsonObject%>