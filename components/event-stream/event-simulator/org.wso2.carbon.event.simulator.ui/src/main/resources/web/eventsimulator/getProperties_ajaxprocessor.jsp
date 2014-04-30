<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.admin.EventStreamInfoDto" %>
<%@ page import="com.google.gson.Gson" %>
<%

    EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);
    org.wso2.carbon.event.simulator.stub.types.EventStreamInfoDto[] eventInfoArray = stub.getAllEventStreamInfoDto();

    String eventName = request.getParameter("eventName");
    org.wso2.carbon.event.simulator.stub.types.EventStreamInfoDto selctedEvent = new org.wso2.carbon.event.simulator.stub.types.EventStreamInfoDto();

    for (int i = 0; i < eventInfoArray.length; i++) {
        if (eventInfoArray[i].getStreamName().equals(eventName)) {
            selctedEvent = eventInfoArray[i];
            break;
        }

    }


    String eventPropertyString = "";

    if (selctedEvent != null) {
        eventPropertyString = new Gson().toJson(selctedEvent);


%>

<%=eventPropertyString%>
<%
    }
%>
