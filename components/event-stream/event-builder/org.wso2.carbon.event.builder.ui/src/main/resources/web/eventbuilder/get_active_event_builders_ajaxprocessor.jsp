<!--
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
-->
<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>

<%
    // get Event Stream Definition
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    EventBuilderConfigurationInfoDto[] eventBuilders = stub.getAllActiveEventBuilderConfigurations();
    String[] eventBuilderNames = null;
    String responseText = "";
    if (eventBuilders != null && eventBuilders.length > 0) {
        eventBuilderNames = new String[eventBuilders.length];
        int i = 0;
        for (EventBuilderConfigurationInfoDto eventBuilder : eventBuilders) {
            eventBuilderNames[i++] = eventBuilder.getEventBuilderName();
        }
        responseText = new Gson().toJson(eventBuilderNames);
    }
%>
<%=responseText%>
