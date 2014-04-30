
<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminService" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%


    String msg = null;
    try {
        EventBuilderAdminService stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);

        String streamNameWithVersion = request.getParameter("eventStreamId");
        stub.deployDefaultEventReceiver(streamNameWithVersion);
        msg = "true";


    } catch (Exception e) {
        msg = e.getMessage();
    }

%>
<%=msg%>
