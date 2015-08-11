<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>


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
<script type="text/javascript" src="../eventprocessor/js/eventprocessor_constants.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>

<script type="text/javascript">
    function doDeleteExecutionPlan(executionPlan) {
        alert(executionPlan);
        CARBON.showConfirmationDialog("!. Are you sure want to delete execution plan:" + executionPlan,
                function () {
                    new Ajax.Request('../eventprocessor/delete_execution_plan_ajaxprocessor.jsp', {
                        method: 'POST',
                        asynchronous: false,
                        parameters: {
                            executionPlan: executionPlan
                        }, onSuccess: function (msg) {
                            if ("success" == msg.responseText.trim()) {
                                window.location.href = "../eventprocessor/index.jsp?region=region1&item=execution_plan_menu.jsp";
                            } else {
                                CARBON.showErrorDialog("Failed to delete execution plan, Exception: " + msg.responseText.trim());
                            }
                        }
                    })
                }, null, null);
    }

</script>

<%--<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">--%>
<%

    String execPlanName = request.getParameter("execPlan");
    EventProcessorAdminServiceStub processorAdminServiceStub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
    ExecutionPlanConfigurationDto configurationDto = processorAdminServiceStub.getActiveExecutionPlanConfiguration(execPlanName);
%>

<div id="middle">
<h2 style="padding-bottom: 7px">Event Processor Details
    <span style="float: right; font-size:75%">
        <% if (configurationDto.getEditable()) { %>
            <% if (configurationDto.getStatisticsEnabled()) {%>
            <div style="display: inline-block">
                <div id="disableStat<%= configurationDto.getName()%>">
                    <a href="#"
                       onclick="disableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
                <div id="enableStat<%= configurationDto.getName()%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div style="display: inline-block">
                <div id="enableStat<%= configurationDto.getName()%>">
                    <a href="#"
                       onclick="enableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
                <div id="disableStat<%= configurationDto.getName()%>"
                     style="display:none">
                    <a href="#"
                       onclick="disableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
            </div>
            <% }
                if (configurationDto.getTracingEnabled()) {%>
            <div style="display: inline-block">
                <div id="disableTracing<%= configurationDto.getName()%>">
                    <a href="#"
                       onclick="disableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
                <div id="enableTracing<%= configurationDto.getName()%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div style="display: inline-block">
                <div id="enableTracing<%= configurationDto.getName() %>">
                    <a href="#"
                       onclick="enableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
                <div id="disableTracing<%= configurationDto.getName() %>"
                     style="display:none">
                    <a href="#"
                       onclick="disableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
            </div>

            <% } %>

            <div style="display: inline-block">
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="doDeleteExecutionPlan('<%=configurationDto.getName()%>')"><font
                        color="#4682b4">Delete</font></a>
            </div>
            <div style="display: inline-block">
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="../eventprocessor/edit_execution_plan.jsp?ordinal=1&execPlanName=<%=configurationDto.getName()%>"><font
                        color="#4682b4">Edit</font></a>
            </div>

            <% } else { %>
            <div style="display: inline-block">
                <div id="cappArtifact<%= configurationDto.getName()%>">
                    <div style="background-image: url(images/capp.gif);" class="icon-nolink-nofloat">
                        <fmt:message key="capp.artifact.message"/></div>
                </div>
            </div>

            <% } %>
    </span>
</h2>

<div id="workArea">
<table style="width:100%" id="eventProcessorDetails" class="styledLeft noBorders spacer-bot">
<tbody>
<tr>
<td>

<table width="100%">

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
        var mime = MIME_TYPE_SIDDHI_QL;

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
        <b><fmt:message key="execution.plan"/></b>
    </td>
</tr>

<%--imported stream mappings--%>

<tr>
    <td colspan="2">
        <style>
            div#workArea table#streamDefinitionsTable tbody tr td {
                padding-left: 45px !important;
            }
        </style>
        <table width="100%" style="border: 1px solid #cccccc">
                <%--query expressions--%>
            <tr>
                <td>
                    <textarea class="queryExpressionsTextArea" style="width:100%; height: 150px"
                              id="queryExpressions"
                              name="queryExpressions" readonly><%= configurationDto.getExecutionPlan() %>
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