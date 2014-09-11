<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.types.EventDto" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>

<%

    String msg = null;
    try {
        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

        String jsonData = request.getParameter("jsonData");


        JSONObject eventDetail = new JSONObject(jsonData);
        String eventStreamId = eventDetail.getString("EventStreamName");

        JSONArray attributes = eventDetail.getJSONArray("attributes");


        EventDto event = new EventDto();

        String[] attributeValues=new String[attributes.length()];

        for (int i = 0; i < attributes.length(); i++) {

            attributeValues[i]=attributes.getJSONObject(i).getString("value");

        }

        event.setEventStreamId(eventStreamId);
        event.setAttributeValues(attributeValues);

        stub.sendEvent(event);

        msg = "Success";


    } catch (Exception e) {
        msg = e.getMessage();
    }

%>
<%=msg%>
