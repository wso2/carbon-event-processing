<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%

    String msg=null;

    try{
        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

        String fileName=request.getParameter("fileName");
        stub.deleteFile(fileName);

        msg="deleted";
    }catch(Exception e){
        msg=e.getMessage();
    }
%>

<%=msg%>