<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%
    String msg = null;

    try{

        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);
        String fileName=request.getParameter("fileName");

        stub.sendEventsViaFile(fileName);

        msg="sent";
    }catch(Exception e){
        msg=e.getMessage();
    }
%>
<%=msg%>