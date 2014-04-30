<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%@ page import="org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub" %>
<%
    OutputEventAdaptorManagerAdminServiceStub stub = EventFormatterUIUtils.getOutputEventManagerAdminService(config, session, request);
    String eventAdaptorName = request.getParameter("eventAdaptorName");
%>
<%
    if (eventAdaptorName != null) {
        String supportedMappings = "";
        String[] mappingTypes = stub.getSupportedMappingTypes(eventAdaptorName);
        for (String mappingType : mappingTypes) {
            supportedMappings = supportedMappings + "|=" + mappingType;
        }
%>
<%=supportedMappings%>
<%
    }
%>
