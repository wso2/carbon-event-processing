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
<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>


<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<carbon:breadcrumb
        label="edit"
        resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript" src="global-params.js"></script>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<%--Yahoo includes for dom event handling--%>
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>

<%--end of newly added--%>


<%
    String status = request.getParameter("status");
    ResourceBundle bundle = ResourceBundle.getBundle(
            "org.wso2.carbon.event.processor.ui.i18n.Resources", request.getLocale());

    if ("updated".equals(status)) {
%>
<script type="text/javascript">
    jQuery(document).ready(function () {
        CARBON.showInfoDialog('<%=bundle.getString("activated.configuration")%>');
    });
</script>
<%

    }
%>


<%
    String executionPlanName = request.getParameter("execPlanName");
    String executionPlanPath = request.getParameter("execPlanPath");

    String executionPlan = "";
    if (executionPlanName != null) {
        EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
        executionPlan = stub.getActiveExecutionPlan(executionPlanName);

    } else if (executionPlanPath != null) {
        EventProcessorAdminServiceStub stub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
        executionPlan = stub.getInactiveExecutionPlan(executionPlanPath);

    }

    EventStreamAdminServiceStub streamAdminServiceStub = EventProcessorUIUtils.getEventStreamAdminService(config, session, request);
    String[] streamNames = streamAdminServiceStub.getStreamNames();

    Boolean loadEditArea = true;

%>

<% if (loadEditArea) { %>

<%--code mirror code--%>

<link rel="stylesheet" href="../eventprocessor/css/codemirror.css"/>
<link rel="stylesheet" href="../eventprocessor/css/event-processor.css"/>
<script src="../eventprocessor/js/codemirror.js"></script>
<script src="../eventprocessor/js/sql.js"></script>
<script type="text/javascript"
        src="../eventprocessor/js/create_execution_plan_helper.js"></script>

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

        window.queryEditor = CodeMirror.fromTextArea(document.getElementById('rawConfig'), {
            mode: mime,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true
        });

        //window.queryEditor.setValue();
        //window.queryEditor.save();
    };
</script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        init();
    });
</script>

<% } %>

<script type="text/javascript">
    function updateConfiguration(form, executionPlanName) {
        var newExecutionPlan = "";

        if (document.getElementById("rawConfig") != null) {
            newExecutionPlan = editAreaLoader.getValue("rawConfig");
        }

        var parameters = "?execPlanName=" + executionPlanName + "&execPlan=" + newExecutionPlan;

        new Ajax.Request('../eventprocessor/edit_execution_plan_ajaxprocessor.jsp', {
            method: 'POST',
            asynchronous: false,
            parameters: {execPlanName: executionPlanName, execPlan: newExecutionPlan },
            onSuccess: function (transport) {
                if ("true" == transport.responseText.trim()) {
                    form.submit();
                } else {
                    if(transport.responseText.trim().indexOf("The input stream for an incoming message is null") != -1){
                        CARBON.showErrorDialog("Possible session time out, redirecting to index page",function(){
                            window.location.href = "../admin/index.jsp?ordinal=1";
                        });
                    }else{
                        CARBON.showErrorDialog("Exception: " + transport.responseText.trim());
                    }
                }
            }
        });

    }

    function updateNotDeployedConfiguration(form, executionPlanPath) {
        var newExecutionPlan = "";

        if (document.getElementById("rawConfig") != null) {
            newExecutionPlan = editAreaLoader.getValue("rawConfig");
        }

        new Ajax.Request('../eventprocessor/edit_execution_plan_ajaxprocessor.jsp', {
            method: 'POST',
            asynchronous: false,
            parameters: {execPlanPath: executionPlanPath, execPlanConfig: newExecutionPlan },
            onSuccess: function (transport) {
                if ("true" == transport.responseText.trim()) {
                    form.submit();
                } else {
                    if(transport.responseText.trim().indexOf("The input stream for an incoming message is null") != -1){
                        CARBON.showErrorDialog("Possible session time out, redirecting to index page",function(){
                            window.location.href = "../admin/index.jsp?ordinal=1";
                        });
                    }else{
                        CARBON.showErrorDialog("Exception: " + transport.responseText.trim());
                    }
                }
            }
        });

    }

    function resetConfiguration(form) {

        CARBON.showConfirmationDialog(
                "Are you sure you want to reset?", function () {
                    editAreaLoader.setValue("rawConfig", document.getElementById("rawConfig").value.trim());
                });

    }

</script>

<div id="middle">
    <h2><fmt:message key="edit.execution.plan.configuration"/></h2>

    <div id="workArea">
        <form name="configform" id="configform" action="index.jsp?ordinal=1" method="post">
            <div id="saveConfiguration">
                            <span style="margin-top:10px;margin-bottom:10px; display:block;_margin-top:0px;">
                                <fmt:message key="save.advice"/>
                            </span>
            </div>
            <table class="styledLeft noBorders spacer-bot" style="width:100%">
                <thead>
                <tr>
                    <th colspan="2">
                        <fmt:message key="execution.plan.configuration"/>
                    </th>
                </tr>
                </thead>
                <tbody>

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
                <tr>
                    <td class="formRow" colspan="2">
                        <table class="normal" style="width:100%">
                            <tr>
                                <td id="rawConfigTD">
                                    <textarea name="rawConfig" id="rawConfig" onblur="window.queryEditor.save()"
                                              style="border:solid 1px #cccccc; width: 99%; height: 400px; margin-top:5px;"><%=executionPlan%>
                                    </textarea>
                                    <% if (!loadEditArea) { %>
                                    <div style="padding:10px;color:#444;">
                                        <fmt:message key="syntax.disabled"/>
                                    </div>
                                    <% } %>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow">
                        <%
                            if (executionPlanName != null) {
                        %>

                        <button class="button"
                                onclick="updateConfiguration(document.getElementById('configform'),'<%=executionPlanName%>'); return false;">
                            <fmt:message
                                    key="update"/></button>

                        <%
                        } else if (executionPlanPath != null) {
                        %>
                        <button class="button"
                                onclick="updateNotDeployedConfiguration(document.getElementById('configform'),'<%=executionPlanPath%>'); return false;">
                            <fmt:message
                                    key="update"/></button>

                        <%
                            }
                        %>
                        <button class="button"
                                onclick="resetConfiguration(document.getElementById('configform')); return false;">
                            <fmt:message
                                    key="reset"/></button>
                    </td>
                </tr>
                </tbody>

            </table>

        </form>

    </div>
</div>
</fmt:bundle>