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
<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>

<%
    // get Event Stream Definition
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    EventFormatterConfigurationInfoDto[] eventFormatters = stub.getAllActiveEventFormatterConfiguration();
    String[] eventFormatterNames = null;
    String responseText = "";
    if (eventFormatters != null && eventFormatters.length > 0) {
        eventFormatterNames = new String[eventFormatters.length];
        int i = 0;
        for (EventFormatterConfigurationInfoDto eventFormatter : eventFormatters) {
            eventFormatterNames[i++] = eventFormatter.getEventFormatterName();
        }
        responseText = new Gson().toJson(eventFormatterNames);
    }
%>
<%=responseText%>
