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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%
    String eventBuilderName = request.getParameter("eventBuilderName");
    String attribute = request.getParameter("attribute");
    String value = request.getParameter("value");

    if (eventBuilderName != null && attribute != null && value != null) {
        EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
        if ("stat".equals(attribute)) {
            if ("true".equalsIgnoreCase(value)) {
                stub.setStatisticsEnabled(eventBuilderName, true);
            } else {
                stub.setStatisticsEnabled(eventBuilderName, false);
            }
        } else if ("trace".equals(attribute)) {
            if ("true".equalsIgnoreCase(value)) {
                stub.setTraceEnabled(eventBuilderName, true);
            } else {
                stub.setTraceEnabled(eventBuilderName, false);
            }
        }
    }

%>