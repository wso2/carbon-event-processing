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
        src="js/eventstreamProperty_load.js"></script>
<script type="text/javascript"
        src="js/stream_configuration.js"></script>
<script type="text/javascript"
        src="js/sendFileDetail.js"></script>
<script type="text/javascript"
        src="js/sendDBConfigFileDetails.js"></script>
<link type="text/css" href="css/eventSimulator.css" rel="stylesheet"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

    org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto[] eventInfoArray = stub.getAllEventStreamInfoDto();

    org.wso2.carbon.event.simulator.stub.types.CSVFileInfoDto[] csvFileInfoDtosArray=stub.getAllCSVFileInfo();
    org.wso2.carbon.event.simulator.stub.types.DataSourceTableAndStreamInfoDto[] dataSourceTableAndStreamInfoDtoArray = stub.getAllDataSourceTableAndStreamInfo();
%>

<script type="text/javascript">
    jQuery(document).ready(function() {
        document.getElementById("fileArea").style.display = "inline";
        document.getElementById("dataSourceArea").style.display = "none";
        document.getElementById("fileRadioButton").checked = true;
    });
    function changeView(view) {
        var plain = "fileArea";
        if (plain.localeCompare(view) == 0) {
            document.getElementById("fileArea").style.display = "none";
            document.getElementById("dataSourceArea").style.display = "inline";
        } else {
            document.getElementById("dataSourceArea").style.display = "none";
            document.getElementById("fileArea").style.display = "inline";
        }
    }
</script>

<script type="text/javascript">

    function saveDataSourceConfiguration() {
        var selectDataSourceId = document.getElementById( "dataSourceTypeID" );
        if(selectDataSourceId.options[selectDataSourceId.selectedIndex ].value.localeCompare("RDBMS") == 0){
            saveDBConfiguration("RDBMS");
        }else{
            saveDBConfiguration("Cassandra");
        }

    }
</script>

<script type="text/javascript">

    function testConnection() {
        var selectDataSourceId = document.getElementById( "dataSourceTypeID" );
        if(selectDataSourceId.options[selectDataSourceId.selectedIndex ].value.localeCompare("RDBMS") == 0){
            testRDBMConnection("RDBMS");
        }else{
            testRDBMConnection("Cassandra");
        }

    }
</script>

<fmt:bundle basename="org.wso2.carbon.event.simulator.ui.i18n.Resources">
<carbon:breadcrumb
        label="eventformatter.list"
        resourceBundle="org.wso2.carbon.event.simulator.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<div id="custom_dcontainer" style="display:none"></div>

<div id="middle">
    <div id="workArea">
    <h2><fmt:message key="event.stream.simulator"/> </h2>
    <br>
    <h4 style="color: #0D4d79"><fmt:message key="send.multiple.events"/>
    </h4>
        <div id="fileArea">
            <form name="csvFileForm" id="csvFileForm" method="post" action="../../fileupload/csv" enctype="multipart/form-data"
                  target="_self"  >

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="3" class="middle-header">
                                            <span
                                                    style="float: left; position: relative; margin-top: 2px;">
                                                    <fmt:message key="input.by.file" />
                                            </span>
                            <a href="#" onclick="changeView('fileArea');" class="icon-link"
                               style="background-image: url(images/design-view.gif); font-weight: normal">
                                switch to add configuration for simulate by database</a>
                        </th>
                    </tr>
                    <tr>
                        <th><fmt:message key="file"/> </th>
                        <th><fmt:message key="stream.configuration"/> </th>
                        <th><fmt:message key="action"/> </th>
                    </tr>
                    </thead>
                    <tbody>
                    <%if(csvFileInfoDtosArray==null){%>
                    <tr>
                        <td><fmt:message key="no.file.has.been.uploaded"/> </td>
                        <td></td>
                        <td></td>
                    </tr>
                    <%}
                    else{
                        for(int k=0;k<csvFileInfoDtosArray.length;k++){
                    %>

                    <tr>
                        <td><strong><%=csvFileInfoDtosArray[k].getFileName()%>
                        </strong></td>
                        <% if (csvFileInfoDtosArray[k].getStreamID() != null) {%>
                        <td><%=csvFileInfoDtosArray[k].getStreamID()%></td>
                        <%} else {
                        %>
                        <td>
                            <fmt:message key="click.configuration"/>
                        </td>
                        <%}%>
                        <td>

                            <%if(csvFileInfoDtosArray[k].getStreamID()!=null){%>
                            <input type="button" value="Play" onclick="sendFileDetails('<%=csvFileInfoDtosArray[k].getFileName()%>')">
                            <%}%>
                            <input type="button" value="Configure" onclick="createPopupStreamConfigUI('<%=csvFileInfoDtosArray[k].getFileName()%>')">
                            <input type="button" value="Delete" onclick="deleteFile('<%=csvFileInfoDtosArray[k].getFileName()%>')">
                        </td>
                    </tr>
                    <%
                            }
                        }

                    %>

                    </tbody>

                    <tr>
                        <td class="buttonRow" colspan="3">
                            <input type="file" name="csvFileName" id="csvFile" size="50"/>
                            <input name="upload" class="button registryWriteOperation" type="button"
                                   value="upload" onclick="validateUpload()"/>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
        <div id="dataSourceArea">

                <form name="inputFormDB" action="#" method="post" id="inputFormDB">
                    <table class="styledLeft noBorders" cellspacing="0" cellpadding="0" border="0" style="border-bottom:1px solid #cccccc">
                        <thead>

                    <tr>
                        <th colspan="3" class="middle-header">
                                                <span
                                                        style="float: left; position: relative; margin-top: 2px;">
                                                        <fmt:message key="input.by.ds" />
                                                </span>
                            <a href="#" onclick="changeView('dataSourceArea');" class="icon-link"
                               style="background-image: url(images/design-view.gif); font-weight: normal">
                                switch to upload configuration file for simulate</a>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <br>
                    <tr>
                        <td colspan="3">
                            <table class="styledLeft" id = "dataSourceConfigInfoTableID" cellspacing="0" cellpadding="0" style="border-bottom:none">
                                <thead>
                                <tr>
                                    <th><fmt:message key="configuration.name"/> </th>
                                    <th><fmt:message key="data.source.name"/> </th>
                                    <th><fmt:message key="table.name"/> </th>
                                    <th><fmt:message key="column.names"/> </th>
                                    <th><fmt:message key="stream.id"/> </th>
                                    <th><fmt:message key="stream.attributes"/> </th>
                                    <th><fmt:message key="action"/> </th>
                                </tr>
                                </thead>
                                <tbody>
                                <%if(dataSourceTableAndStreamInfoDtoArray==null){%>
                                <tr>
                                    <td><fmt:message key="no.configuration"/> </td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </tr>
                                <%}
                                else{
                                    for(int k=0;k<dataSourceTableAndStreamInfoDtoArray.length;k++){
                                %>

                                <tr>

                                    <td><%=dataSourceTableAndStreamInfoDtoArray[k].getConfigurationName()%></td>
                                    <td><%=dataSourceTableAndStreamInfoDtoArray[k].getDataSourceName()%></td>
                                    <td><%=dataSourceTableAndStreamInfoDtoArray[k].getTableName()%></td>
                                    <%
                                        String columns = "", streamAttributes= "";

                                        boolean addedFirstColumn = false;
                                        boolean addedFirstAttribute = false;
                                        String[] columnArray = dataSourceTableAndStreamInfoDtoArray[k].getColumnNames();
                                        String[] streamAttributesArray = dataSourceTableAndStreamInfoDtoArray[k].getStreamAtrributeNames();

                                        for(int i=0; i<columnArray.length;i++) {
                                            if(addedFirstColumn){
                                                columns = columns + ",";
                                            }
                                            if(addedFirstAttribute){
                                                streamAttributes = streamAttributes + ",";
                                            }
                                            addedFirstColumn = true;
                                            addedFirstAttribute = true;
                                            columns = columns + columnArray[i];
                                            streamAttributes = streamAttributes + streamAttributesArray[i];
                                        }
                                    %>
                                    <td><%=columns%></td>
                                    <td><%=dataSourceTableAndStreamInfoDtoArray[k].getEventStreamID()%></td>
                                    <td><%=streamAttributes%></td>


                                    <td>

                                        <input type="button" value="Play" onclick="sendDBConfigFileNameToSimulate('<%=dataSourceTableAndStreamInfoDtoArray[k].getFileName()%>')">
                                        <input type="button" value="Delete" onclick="deleteDBConfigFile('<%=dataSourceTableAndStreamInfoDtoArray[k].getFileName()%>')">
                                    </td>
                                </tr>
                                <%}

                                }%>

                                </tbody>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td class="formRow" colspan="2">
                            <table id="inputEventDetailTable3" class="normal-nopadding" style="width:100%">
                                <tbody>
                                <tr>
                                    <td style="padding-left:10px">Configuration Name<span class="required">*</span></td>
                                    <td colspan="2" style="padding-left:10px">
                                        <div class="outputFields">
                                            <input style="width:75%" type="text" id="configurationNameId2" name="configuration.name" value="" class="initE">
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding-left:10px">Data Source Type<span class="required">*</span></td>
                                    <td colspan="2" style="padding-left:10px">
                                        <select name="dataSourceTypeID" id="dataSourceTypeID">

                                            <option value="RDBMS">RDBMS
                                            </option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding-left:10px" >Data Source Name<span class="required">*</span></td>
                                    <td colspan="2" style="padding-left:10px">
                                        <div class="outputFields">
                                            <input style="width:75%" type="text" id="dataSourceNameId2" name="datasource.name" value="" class="initE">
                                        </div>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-med" style="padding-left:10px">Table Name
                                        <span class="required">*</span>
                                    </td>
                                    <td style="padding-left:10px">
                                        <div class="outputFields">
                                            <input type="text" name="table.name" id="tableNameId2" class="initE" style="width:75%" value="">
                                        </div>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>


                    <tr>
                        <td class="formRow" colspan="2">
                            <table id="inputEventDetailTable2" class="normal-nopadding" style="width:100%">
                                <tbody>
                                <tr>
                                    <td class="leftCol-med"><fmt:message key="select.the.event.stream"/> <span class="required">*</span></td>
                                    <td>
                                        <select name="EventStreamID" id="EventStreamID2" onload="showEventPropertiesForSimulator()"
                                                onchange="showEventPropertiesForSimulator()">

                                            <%
                                                if (eventInfoArray == null) {
                                            %>

                                            <option value="No Event Stream Definitions"><fmt:message key="no.event.stream.definition"/>
                                            </option>
                                            <%
                                            } else {%>

                                                <option value="select">select event stream
                                                </option>
                                                <%for (int i = 0; i < eventInfoArray.length; i++) {
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

                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow formRow" colspan="2">
                            <input type="button" value="Test Connection" onclick = "testConnection()">
                            <input type="button" value="Save" onclick = "saveDataSourceConfiguration()">
                        </td>

                    </tr>
                    </tbody>

                </table>

            </form>
        </div>


        <br/>
        <h4 style="color: #0D4d79"><fmt:message key="send.single.event"/> </h4>

        <div id="workArea">
            <form name="eventStreams" id="eventStreams" method="post" >
                <table id="eventStreamtable" class="styledLeft">
                    <thead>
                    <tr>
                        <th></th>

                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table id="inputEventDetailTable" class="normal-nopadding">
                                <tbody>
                                <tr>
                                    <td class="leftCol-med"><fmt:message key="select.the.event.stream"/> <span class="required">*</span></td>
                                    <td>
                                        <select name="EventStreamID" id="EventStreamID" onload="showEventProperties()"
                                                onchange="showEventProperties()">

                                            <%
                                                if (eventInfoArray == null) {
                                            %>

                                            <option value="No Event Stream Definitions"><fmt:message key="no.event.stream.definition"/>
                                            </option>
                                            <%
                                            } else {%>

                                            <option value="select">select event stream
                                            </option>

                                                <%for (int i = 0; i < eventInfoArray.length; i++) {
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
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="Send"
                                   onclick="sendEvent(document.getElementById('eventStreams'))">
                        </td>
                    </tr>
                    </tbody>
                </table>

            </form>

        </div>


    </div>

</div>
</fmt:bundle>