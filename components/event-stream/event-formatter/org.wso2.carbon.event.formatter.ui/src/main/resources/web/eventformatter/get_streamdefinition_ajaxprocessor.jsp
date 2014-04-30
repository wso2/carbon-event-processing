<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIConstants" %>
<%@ page
        import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamDefinitionDto" %>

<%
    // get Event Stream Definition
    EventStreamAdminServiceStub stub = EventFormatterUIUtils.getEventStreamAdminService(config, session, request);
    String streamName = request.getParameter("streamName");

%>

<%

    if (streamName != null) {
        EventStreamDefinitionDto streamDefinitionDto = stub.getStreamDefinitionDto(streamName);
        EventStreamAttributeDto[] metaAttributeList = streamDefinitionDto.getMetaData();
        EventStreamAttributeDto[] correlationAttributeList = streamDefinitionDto.getCorrelationData();
        EventStreamAttributeDto[] payloadAttributeList = streamDefinitionDto.getPayloadData();

        String attributes = "";

        if (metaAttributeList != null && metaAttributeList.length > 0) {
            for (EventStreamAttributeDto attribute : metaAttributeList) {
                attributes += EventFormatterUIConstants.PROPERTY_META_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (EventStreamAttributeDto attribute : correlationAttributeList) {
                attributes += EventFormatterUIConstants.PROPERTY_CORRELATION_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (EventStreamAttributeDto attribute : payloadAttributeList) {
                attributes += attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            attributes = attributes.substring(0, attributes.lastIndexOf(","));
        }

        String streamDefinition = attributes;

%>
<%=streamDefinition%>
<%
    }

%>
