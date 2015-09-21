<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.wso2.carbon.siddhi.tryit.ui.UIConstants" %>

<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.siddhi.tryit.ui.i18n.Resources">
    <carbon:breadcrumb
            label="siddhi.tryit.breadcrumb"
            resourceBundle="org.wso2.carbon.siddhi.tryit.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <%
        Date date = new Date();
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String EXECUTION_PLAN_BASIC_TEMPLATE = UIConstants.EXECUTION_PLAN_BASIC_TEMPLATE;
        String EXECUTION_PLAN_SAMPLE = UIConstants.EXECUTION_PLAN_SAMPLE;
        String EVENT_STREAM_SAMPLE = UIConstants.EVENT_STREAM_SAMPLE;
    %>

    <meta charset="utf-8">
    <link rel="stylesheet" href="../siddhitryit/css/siddhi-tryit.css">
    <script type="text/javascript" src="js/sendInputData.js"></script>
    <script type="text/javascript" src="../siddhitryit/js/siddhitryit_constants.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <%--code mirror css and script files--%>
    <link rel="stylesheet" href="../siddhitryit/css/codemirror.css"/>
    <link rel="stylesheet" href="../siddhitryit/css/show-hint.css">
    <script src="../siddhitryit/js/codemirror.js"></script>
    <script type="text/javascript" src="../siddhitryit/js/show-hint.js"></script>
    <script type="text/javascript" src="../siddhitryit/js/annotation-hint.js"></script>
    <script type="text/javascript" src="js/any-word-hint.js"></script>
    <script type="text/javascript" src="../siddhitryit/js/sql-hint.js"></script>
    <script src="../siddhitryit/js/sql.js"></script>

    <div id="middle">
        <h2>Siddhi Try It</h2>

        <div id="workArea">
            <table class="styledLeft" id="userTable">
                <thead>
                <tr>
                    <th><fmt:message key="execution.plan"/></th>
                    <th><fmt:message key="event.stream"/></th>
                    <th class="js_resultCol" style="display:none"><fmt:message key="result"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                    <textarea rows="50" cols="50" name="executionplan"
                              id="executionPlanId"
                              style="margin-top:10px;"
                              onblur="window.queryEditor.save()"><%=EXECUTION_PLAN_BASIC_TEMPLATE +
                            EXECUTION_PLAN_SAMPLE%></textarea>
                    </td>
                    <td>
                        <fmt:message key="begin.time"/>&nbsp<input type="text" name="datetime"
                                                                   id="dateTimeID"
                                                                   value="<%=dataFormat.format(date)%>">
                        <input type="button" value="Submit"
                               onclick="sendAjaxRequestToSiddhiProcessor()"/>
                        <br>
                    <textarea rows="50" cols="50" name="eventstream" id="eventStreamId"
                              style="margin-top:29px;"><%=EVENT_STREAM_SAMPLE%></textarea>
                    </td>
                    <td id="resultsId" class="js_resultCol" style="display:none;">
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <%--code mirror script--%>
    <script>
        var mime = MIME_TYPE_SIDDHI_QL;
        // get mime type
        if (window.location.href.indexOf('mime=') > -1) {
            mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
        }
        window.queryEditor = CodeMirror.fromTextArea(document.getElementById('executionPlanId'), {
            mode: mime,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            extraKeys: {
                "Shift-2": function (cm) {
                    insertStr(cm, cm.getCursor(), '@');
                    CodeMirror.showHint(cm, getAnnotationHints);
                },
                "Ctrl-Space": "autocomplete"
            }
        });
    </script>

</fmt:bundle>