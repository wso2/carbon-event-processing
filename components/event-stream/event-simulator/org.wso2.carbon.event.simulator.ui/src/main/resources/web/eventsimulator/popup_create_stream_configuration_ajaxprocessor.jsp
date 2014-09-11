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
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<script type="text/javascript"
        src="../eventsimulator/js/eventstreamProperty_load.js"></script>
<script type="text/javascript"
        src="../eventsimulator/js/stream_configuration.js"></script>
<link type="text/css" href="css/eventSimulator.css" rel="stylesheet"/>

<div id="middle">


    <div id="workArea">
    <form name="configForm" id="configForm" method="post" action="#">
        <table style="width:100%" class="styledLeft noBorders spacer-bot" id="configTable" >
<%
    EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

    org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto[] eventInfoArray = stub.getAllEventStreamInfoDto();

    String fileName=request.getParameter("fileName");

%>
        <br/>
        <thead>
        <%--<tr>Configuration settings</tr>--%>
        </thead>
        <tbody>
        <tr>
            <td class="leftCol-med">File name</td>
            <td id="filename"><%=fileName%></td>
        </tr>
        <tr>
            <td class="leftCol-med">Select the target event stream<span class="required">*</span></td>
            <td>
                <select name="eventStreamSelect" id="eventStreamSelect">
                    <%
                        if (eventInfoArray == null) {
                    %>

                    <option value="No Event Stream Definitions">No Event Stream Definitions
                    </option>
                    <%
                    } else {

                        for (int i = 0; i < eventInfoArray.length; i++) {
                    %>
                    <option value="<%=eventInfoArray[i].getStreamName()%>"><%=eventInfoArray[i].getStreamName()+":"+eventInfoArray[i].getStreamVersion()%>
                    </option>


                    <%
                            }
                        }
                    %>
                </select>
            </td>

        </tr>
        <tr>
            <td class="leftCol-med">Field delimiter<span class="required">*</span></td>
            <td ><input type="text" id="seperateChar" class="initE" style="width:75%"></td>
        </tr>
        <tr>
            <td class="buttonRow" colspan="2">
                <input type="button" value="Configure" onclick="sendConfiguration(document.getElementById('configForm'))">
            </td>
        </tr>
        </tbody>



        </table>


    </form>

    </div>
</div>