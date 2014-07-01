<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.wso2.carbon.event.simulator.stub.types.EventDto" %>

<%

    String msg = null;
    try {
        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

        String jsonData = request.getParameter("jsonData");


        JSONObject eventDetail = new JSONObject(jsonData);
        String eventStreamId = eventDetail.getString("EventStreamName");

        JSONArray attributes = eventDetail.getJSONArray("attributes");


        EventDto event = new EventDto();

        String[] attributeValues=new String[attributes.length()];

        for (int i = 0; i < attributes.length(); i++) {

            attributeValues[i]=attributes.getJSONObject(i).getString("value");

        }

        event.setEventStreamId(eventStreamId);
        event.setAttributeValues(attributeValues);

        stub.sendEvent(event);

        msg = "Success";


    } catch (Exception e) {
        msg = e.getMessage();
    }

%>
<%=msg%>
