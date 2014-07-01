<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.admin.StreamDefinitionInfoDto" %>
<%@ page import="org.wso2.carbon.event.simulator.admin.CSVFileInfoDto" %>

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
<script type="text/javascript"
        src="../eventsimulator/js/sendFileDetail.js"></script>
<link type="text/css" href="css/eventSimulator.css" rel="stylesheet"/>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
    EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

    org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto[] eventInfoArray = stub.getAllEventStreamInfoDto();

    org.wso2.carbon.event.simulator.stub.types.CSVFileInfoDto[] csvFileInfoDtosArray=stub.getAllCSVFileInfo();
%>

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
            <h4 style="color: #0D4d79"><fmt:message key="send.multiple.events"/> </h4>

            <form name="csvFileForm" id="csvFileForm" method="post" action="../../fileupload/csv" enctype="multipart/form-data"
                  target="_self"  >

                <table class="styledLeft">
                    <thead>
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
            <br>
            <h4 style="color: #0D4d79"><fmt:message key="send.single.event"/> </h4>

            <form name="eventStreams" id="eventStreams" method="post" >


                <table id="eventStreamtable" class="styledLeft" style="width:100%">
                    <thead>
                    <tr>
                        <th></th>

                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table id="inputEventDetailTable" class="normal-nopadding" style="width:100%">
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

                <% if (eventInfoArray != null) {
                %>
                <body onload="showEventProperties()"></body>
                <% } %>
            </form>

        </div>

    </div>
</fmt:bundle>
