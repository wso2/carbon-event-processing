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
<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>


<%
    String executionPlanName = request.getParameter("execPlanName");
    String action = request.getParameter("action");

    if (executionPlanName != null && action != null) {
        EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
        if ("enableStat".equals(action)) {
            stub.setStatisticsEnabled(executionPlanName, true);
        } else if ("disableStat".equals(action)) {
            stub.setStatisticsEnabled(executionPlanName, false);
        } else if ("enableTracing".equals(action)) {
            stub.setTracingEnabled(executionPlanName, true);
        } else if ("disableTracing".equals(action)) {
            stub.setTracingEnabled(executionPlanName, false);
        }
    }

%>