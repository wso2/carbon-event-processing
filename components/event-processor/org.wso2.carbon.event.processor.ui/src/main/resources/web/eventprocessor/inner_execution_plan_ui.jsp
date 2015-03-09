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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>

<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
<script type="text/javascript"
        src="../eventprocessor/js/create_execution_plan_helper.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%--code mirror code--%>

<link rel="stylesheet" href="../eventprocessor/css/codemirror.css"/>
<link rel="stylesheet" href="../eventprocessor/css/event-processor.css"/>
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
            autofocus: true
        });
    };
</script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        init();
    });
</script>

<%--Code mirror code end--%>

<%
    EventStreamAdminServiceStub streamAdminServiceStub = EventProcessorUIUtils.getEventStreamAdminService(config, session, request);
    String[] streamNames = streamAdminServiceStub.getStreamNames();
%>
<tr>
<td>
<table width="100%">
<tbody>
<tr>

    <td class="leftCol-med">Execution Plan Name<span class="required">*</span></td>
    <td><input type="text" name="executionPlanName" id="executionPlanId"
               class="initE"
               style="width:75%"/>

        <div class="sectionHelp">
            Please Enter the Execution Plan Name.
        </div>
    </td>
</tr>
<tr>
    <td class="leftCol-med">
        Description
    </td>
    <td>
        <textarea name="executionPlanDescription" id="executionPlanDescId"
                  class="initE"
                  style="width:75%"></textarea>

        <div class="sectionHelp">
            Please Enter the Execution Plan Description (optional).
        </div>
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
        <input type="text" name="siddhiSnapshotTime" id="siddhiSnapshotTime"
               class="initE" value="0"
               style="width:75%"/>

        <div class="sectionHelp">
            Enter the snapshot time in minutes. Entering zero disables snapshots. (Cassandra should be configured and start)
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med">
        Distributed processing
    </td>
    <td>
        <select name="distributedProcessing" id="distributedProcessing">
            <option value="RedundantNode">Redundant Node</option>
            <option value="Distributed">Distributed</option>
            <option value="false" selected="selected">Disabled</option>
        </select>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="wso2query.expressions"/></b>
    </td>
</tr>

<%-- imported stream definitions--%>


<tr>
    <td class="leftCol-med">
        <fmt:message key="import.stream"/><span class="required">*</span>
    </td>
    <td>
        <table>
            <td class="col-small"><fmt:message key="import.stream"/> :
            </td>
            <td><select id="importedStreamId" onfocus="this.selectedIndex = 0;" onchange="createImportedStreamDefinition(this)"
                        onclick="importedStreamDefSelectClick(this)">
                <%
                    if (streamNames != null) {
                        for (String streamName : streamNames) {

                %>
                <option value= <%= "\"" + streamName + "\""%>><%= streamName %>
                </option>
                <%
                        }
                    }
                %>
                <option value="createStreamDef">-- Create Stream Definition --</option>
            </select>
            </td>
            <td class="col-small"><fmt:message key="property.as"/> :
            </td>
            <td>
                <input type="text" id="importedStreamAs"/>
            </td>

            <td><input type="button" class="button"
                       value="<fmt:message key="import"/>"
                       onclick="addImportedStreamDefinition()"/>
            </td>
            <td id="addEventStreamTD"></td>
        </table>
    </td>
</tr>


    <%--query expressions--%>


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
                        </tbody>
                    </table>
                </td>
            </tr>
            <tr>
                <td>
                    <textarea class="queryExpressionsTextArea" style="width:100%; height: 150px"
                              id="queryExpressions"
                              name="queryExpressions" onblur="window.queryEditor.save()"></textarea>
                </td>
            </tr>

            <tr>
                <td>
                    <input type="button" class="button"
                           value="<fmt:message key="validate.queries"/>"
                           onclick="validateQueries()"/>
                </td>
            </tr>


        </table>
    </td>
</tr>
<tr></tr>
<tr>
    <td class="leftCol-med">
        <fmt:message key="export.stream"/>
    </td>
    <td>
        <table id="addExportedStreams" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="property.value.of"/> :
                </td>
                <td>
                    <input type="text" id="exportedStreamValueOf"/>
                </td>
                <td class="col-small"><fmt:message key="property.stream.id"/> :
                </td>
                <td><select id="exportedStreamId" onfocus="this.selectedIndex = 0;" onchange="createExportedStreamDefinition(this)"
                            onclick="exportedStreamDefSelectClick(this)">
                    <%
                        if (streamNames != null && streamNames.length > 0) {
                            for (String streamName : streamNames) {

                    %>
                    <option value= <%= "\"" + streamName + "\""%>><%= streamName %>
                    </option>
                    <%
                            }
                        }
                    %>
                    <option value="createStreamDef">-- Create Stream Definition --</option>
                </select></td>

                <td>

                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addExportedStreamDefinition()"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
</table>
</td>
</tr>
</fmt:bundle>