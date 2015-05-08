<%--
~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
~ use this file except in compliance with the License. You may obtain a copy
~ of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software distributed
~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
~ CONDITIONS OF ANY KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations under the License.
--%>

<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.wso2.carbon.siddhi.tryit.ui.UIConstants" %>

<%
    Date date = new Date();
    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String EXECUTION_PLAN_BASIC_TEMPLATE = UIConstants.EXECUTION_PLAN_BASIC_TEMPLATE;
%>

<meta charset="utf-8">
<link rel="stylesheet" href="../siddhitryit/css/siddhi-tryit.css">
<script type="text/javascript" src="js/sendInputData.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<div id="middle">
    <h2>Siddhi Try It</h2>

    <div id="workArea">
        <table class="styledLeft" id="userTable">
            <thead>
            <tr>
                <th>Event Stream</th>
                <th>Execution Plan</th>
                <th class="js_resultCol" style="display:none">Result</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <textarea rows="50" cols="50" name="eventstream"
                              id="eventStreamId" style="margin-top:29px;">Enter stream</textarea>
                </td>
                <td>
                    Begin Time: <input type="text" name="datetime" id="dateTimeID" value="<%=dataFormat.format(date)%>">
                    <input type="button" value="Submit" onclick="sendAjaxRequestToSiddhiProcessor()"/>
                    <br>
                    <textarea rows="50" cols="50" name="executionplan"
                              id="executionPlanId"
                              style="margin-top:10px;"><%=EXECUTION_PLAN_BASIC_TEMPLATE%></textarea>
                </td>
                <td id="tabTable" class="js_resultCol" style="display:none;">
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

