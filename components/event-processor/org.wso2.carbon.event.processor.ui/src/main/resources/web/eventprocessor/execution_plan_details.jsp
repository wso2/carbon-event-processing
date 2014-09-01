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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.SiddhiConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.StreamConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>


<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<carbon:breadcrumb
        label="details"
        resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>

<link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>

<%--<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">--%>

<div id="middle">
<h2>Event Processor Details</h2>

<div id="workArea">
<table style="width:100%" id="eventProcessorDetails" class="styledLeft noBorders spacer-bot">
<tbody>
<tr>
<td>
        <%

            String execPlanName = request.getParameter("execPlan");
            EventProcessorAdminServiceStub processorAdminServiceStub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
            EventStreamAdminServiceStub eventStreamAdminServiceStub = EventProcessorUIUtils.getEventStreamAdminService(config, session, request);
            ExecutionPlanConfigurationDto configurationDto = processorAdminServiceStub.getActiveExecutionPlanConfiguration(execPlanName);
        %>
<table width="100%">

<tr>
    <td class="leftCol-med">Execution Plan Name<span class="required">*</span></td>
    <td><input type="text" name="executionPlanName" id="executionPlanId"
               class="initE"
               style="width:100%"
               value=<%= "\"" + execPlanName + "\"" %>
                       readonly/>
    </td>
</tr>


<tr>
    <td class="leftCol-med">
        Description
    </td>

    <td>
        <input type="text" name="executionPlanDescription" id="executionPlanDescId"
               class="initE"
               style="width:100%"
               value=<%= "\"" +((configurationDto.getDescription()!= null)? configurationDto.getDescription().trim():"")+ "\"" %>
                       readonly/>
    </td>
</tr>


<tr name="siddhiConfigsHeader">
    <td colspan="2">
        <b>Siddhi Configurations</b>
    </td>
</tr>
<tr>
    <td class="leftCol-med">
        Snapshot time interval
    </td>

    <td>

        <%
            String snapshotTime = "0";
            if (configurationDto.getSiddhiConfigurations() != null) {
                for (SiddhiConfigurationDto siddhiConfigurationDto : configurationDto.getSiddhiConfigurations()) {
                    if (UIConstants.SIDDHI_SNAPSHOT_INTERVAL.equalsIgnoreCase(siddhiConfigurationDto.getKey())) {
                        snapshotTime = siddhiConfigurationDto.getValue();
                    }
                }
            }
        %>

        <input type="text" name="siddhiSnapshotTime" id="siddhiSnapshotTime"
               class="initE"
               style="width:100%"
               value=<%= "\"" +snapshotTime + "\"" %>
                       readonly/>

    </td>
</tr>

<tr>
    <td class="leftCol-med">
        Distributed processing
    </td>
    <td>
        <%
            String distributedProcessingStatus = "N/A";
            if (configurationDto.getSiddhiConfigurations() != null) {
                for (SiddhiConfigurationDto siddhiConfig : configurationDto.getSiddhiConfigurations()) {
                    if (UIConstants.SIDDHI_DISTRIBUTED_PROCESSING.equalsIgnoreCase(siddhiConfig.getKey())) {
                        if ("true".equals(siddhiConfig.getValue()) || "DistributedCache".equals(siddhiConfig.getValue())) {
                            distributedProcessingStatus = "Distributed Cache";
                        } else if ("RedundantNode".equals(siddhiConfig.getValue())) {
                            distributedProcessingStatus = "Redundant Node";
                        } else {
                            distributedProcessingStatus = "Disabled";
                        }
                    }
                }
            }
        %>
        <input type="text" name="siddhiDistrProcessing" id="siddhiDistrProcessing"
               class="initE"
               style="width:100%"
               value=<%= "\"" +distributedProcessingStatus + "\"" %>
                       readonly/>

    </td>
</tr>

    <%--code mirror code--%>

<link rel="stylesheet" href="../eventprocessor/css/codemirror.css"/>
<script src="../eventprocessor/js/codemirror.js"></script>
<script src="../eventprocessor/js/sql.js"></script>

<style>
    .CodeMirror {
        border-top: 1px solid #cccccc;
        border-bottom: 1px solid black;
    }
</style>


<script>
    var init = function () {
        var mime = 'text/siddhi-sql-db';

        // get mime type
        if (window.location.href.indexOf('mime=') > -1) {
            mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
        }

        window.queryEditor = CodeMirror.fromTextArea(document.getElementById('queryExpressions'), {
            mode: mime,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            readOnly: true
        });
    };
</script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        init();
    });
</script>

    <%--Code mirror code end--%>

<tr>
    <td colspan="2">
        <b><fmt:message key="wso2query.expressions"/></b>
    </td>
</tr>

<!--imported stream mappings-->

<tr>
    <td colspan="2">
        <style>
            div#workArea table#streamDefinitionsTable tbody tr td {
                padding-left: 45px !important;
            }
        </style>
        <table width="100%" style="border: 1px solid #cccccc">
            <tr>
                <td>
                    <table id="streamDefinitionsTable" width="100%">
                        <tbody>
                        <%
                            if (configurationDto.getImportedStreams() != null) {
                                for (StreamConfigurationDto dto : configurationDto.getImportedStreams()) {
                                    String paramDefinitions = eventStreamAdminServiceStub.getStreamDefinitionAsString(dto.getStreamId());
                                    String query = "define stream <b>"  + dto.getSiddhiStreamName() + "</b> (" + paramDefinitions+" )";

                        %>
                        <tr>
                            <td>
                                <font color="green"> <i><%="// Imported from " + dto.getStreamId()%>
                                </i>   </font>
                                <br>
                                <%=query %>
                            </td>
                        </tr>

                        <%
                                }
                            }
                        %>

                        <%
                            if (configurationDto.getExportedStreams() != null) {
                                for (StreamConfigurationDto dto : configurationDto.getExportedStreams()) {
                                    String paramDefinitions = eventStreamAdminServiceStub.getStreamDefinitionAsString(dto.getStreamId());
                                    String query = "define stream <b>" + dto.getSiddhiStreamName() + "</b> ( " + paramDefinitions +" )";

                        %>
                        <tr>
                            <td>
                                <font color="blue"> <i><%="// Exported to " + dto.getStreamId()%>
                                </i> </font>
                                <br>
                                <%=query %>
                            </td>
                        </tr>

                        <%
                                }
                            }
                        %>

                        </tbody>
                    </table>
                </td>
            </tr>
                <%--query expressions--%>
            <tr>
                <td>
                    <textarea class="queryExpressionsTextArea" style="width:100%; height: 150px"
                              id="queryExpressions"
                              name="queryExpressions" readonly><%= configurationDto.getQueryExpressions() %>
                    </textarea>
                </td>
            </tr>
        </table>
    </td>
</tr>

</table>
</tbody>
</table>

</div>
</div>

</fmt:bundle>