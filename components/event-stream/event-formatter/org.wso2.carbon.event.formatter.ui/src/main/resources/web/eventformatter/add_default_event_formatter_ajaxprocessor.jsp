<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%


    String msg = null;
    try {
        EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);

        String streamNameWithVersion = request.getParameter("eventStreamId");
        stub.deployDefaultEventSender(streamNameWithVersion);
        msg = "true";


    } catch (Exception e) {
        msg = e.getMessage();
    }

%>
<%=msg%>
