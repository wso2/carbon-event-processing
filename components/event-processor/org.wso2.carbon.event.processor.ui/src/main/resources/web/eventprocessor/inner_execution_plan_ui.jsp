<%--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
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
<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="org.wso2.carbon.event.processor.ui.UIConstants" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>

<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
<script type="text/javascript"
        src="../eventprocessor/js/create_execution_plan_helper.js"></script>
<script type="text/javascript"
        src="../eventprocessor/js/eventprocessor_constants.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%--code mirror code--%>

<link rel="stylesheet" href="../eventprocessor/css/codemirror.css"/>
<link rel="stylesheet" href="../eventprocessor/css/show-hint.css">
<link rel="stylesheet" href="../eventprocessor/css/event-processor.css"/>
<script src="../eventprocessor/js/codemirror.js"></script>
<script type="text/javascript" src="../eventprocessor/js/show-hint.js"></script>
<script type="text/javascript" src="../eventprocessor/js/annotation-hint.js"></script>
<script type="text/javascript" src="js/any-word-hint.js"></script>
<script type="text/javascript" src="../eventprocessor/js/sql-hint.js"></script>
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
            extraKeys: {
                "Shift-2": function(cm) {
                    insertStr(cm, cm.getCursor(), '@');
                    CodeMirror.showHint(cm, getAnnotationHints);
                },
                "Ctrl-Space": "autocomplete"
            }
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
    String executionPlan = "";
    String mode;     //Either ADD or UPDATE

    String executionPlanName = request.getParameter("execPlanName");
    String executionPlanPath = request.getParameter("execPlanPath");

    if(executionPlanName == null && executionPlanPath == null){
        mode = "ADD";
    } else {
        mode = "UPDATE";
    }
%>
<%
    if (mode.equals("ADD")) {
        executionPlan = UIConstants.EXECUTION_PLAN_BASIC_TEMPLATE;

    } else {
        if (executionPlanName != null) {
            EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
            executionPlan = stub.getActiveExecutionPlan(executionPlanName);

        } else if (executionPlanPath != null) {
            EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
            executionPlan = stub.getInactiveExecutionPlan(executionPlanPath);

        }
    }

    EventStreamAdminServiceStub streamAdminServiceStub = EventProcessorUIUtils.getEventStreamAdminService(config, session, request);
    String[] streamNames = streamAdminServiceStub.getStreamNames();
%>
<tr>
    <td>
        <table width="100%">
            <tbody>

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

                <%-- exported stream definitions--%>

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
                            <td><input type="button" class="button"
                                       value="<fmt:message key="export"/>"
                                       onclick="addExportedStreamDefinition()"/>
                            </td>
                        </tr>
                        </tbody>
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
                                <textarea class="queryExpressionsTextArea" style="width:100%; height: 150px"
                                          id="queryExpressions"
                                          name="queryExpressions" onblur="window.queryEditor.save()"><%=executionPlan%></textarea>
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
            </tbody>
        </table>
    </td>
</tr>

</fmt:bundle>