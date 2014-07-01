<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>

<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto" %>
<%

    EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);
    org.wso2.carbon.event.simulator.stub.types.StreamDefinitionInfoDto[] eventInfoArray = stub.getAllEventStreamInfoDto();

    String eventName = request.getParameter("eventName");
    StreamDefinitionInfoDto selectedEvent =new StreamDefinitionInfoDto();

    for (int i = 0; i < eventInfoArray.length; i++) {
        if (eventInfoArray[i].getStreamName().equals(eventName)) {
            selectedEvent = eventInfoArray[i];
            break;
        }

    }


    String eventPropertyString = "";

    if (selectedEvent != null) {
        eventPropertyString = new Gson().toJson(selectedEvent);


%>

<%=eventPropertyString%>
<%
    }
%>
