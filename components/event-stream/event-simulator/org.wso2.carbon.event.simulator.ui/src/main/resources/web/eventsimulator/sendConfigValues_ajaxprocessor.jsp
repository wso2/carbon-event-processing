<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.json.JSONObject" %><%
    String msg=null;
    try{
        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);
        String jsonData = request.getParameter("jsonData");

        JSONObject fileConfigData=new JSONObject(jsonData);

        String fileName=fileConfigData.getString("FileName");
        String streamID=fileConfigData.getString("streamID");
        String seperateChar=fileConfigData.getString("seperateChar");

        stub.sendConfigDetails(fileName,streamID,seperateChar);
        msg="Sent";
    }catch(Exception e){
        msg=e.getMessage();
    }
%>
<%=msg%>