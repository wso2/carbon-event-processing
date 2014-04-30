<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.admin.EventStreamInfoDto" %>

<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<script type="text/javascript"
        src="../eventsimulator/js/eventstreamProperty_load.js"></script>
<link type="text/css" href="css/eventSimulator.css" rel="stylesheet"/>

<%
    EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

    org.wso2.carbon.event.simulator.stub.types.EventStreamInfoDto[] eventInfoArray = stub.getAllEventStreamInfoDto();

%>

<fmt:bundle basename="org.wso2.carbon.event.simulator.ui.i18n.Resources">
    <carbon:breadcrumb
            label="eventformatter.list"
            resourceBundle="org.wso2.carbon.event.simulator.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>


    <div id="middle">
        <div id="workArea">
            <h2>Event Stream Simulator</h2>
            <br>

            <form name="eventStreams" id="eventStreams" method="post">
                <table id="eventStreamtable" class="styledLeft" style="width:100%">
                    <thead>
                    <tr>
                        <th>Event Stream details</th>

                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table id="inputEventDetailTable" class="normal-nopadding" style="width:100%">
                                <tbody>
                                <tr>
                                    <td class="leftCol-med">Select the Event Stream<span class="required">*</span></td>
                                    <td>
                                        <select name="EventStreamID" id="EventStreamID" onload="showEventProperties()"
                                                onchange="showEventProperties()">

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
                                </tbody>
                            </table>
                        </td>


                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="Send"
                                   onclick="sendEvent(document.getElementById('eventStreams'))">
                                <%--<input type="button" value="Upload a file" onclick="uploadCSV()">--%>
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
