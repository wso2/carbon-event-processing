<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.StreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>

<%

    EventProcessorAdminServiceStub eventProcessorAdminServiceStub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
    String strInputStreamDefinitions = request.getParameter("siddhiStreamDefinitions");
    String strQueryExpressions = request.getParameter("queries");
    String strTargetStream = request.getParameter("targetStream");

    String resultString = "";
    if (strInputStreamDefinitions != null) {
        String[] definitions = strInputStreamDefinitions.split("(?<=;)");
        try {
            StreamDefinitionDto[] siddhiStreams = eventProcessorAdminServiceStub.getSiddhiStreams(definitions, strQueryExpressions);
            StreamDefinitionDto targetDto = null;
            for (StreamDefinitionDto dto : siddhiStreams) {
                if (strTargetStream.equals(dto.getName())) {
                    targetDto = dto;
                    break;
                }
            }

            resultString = EventProcessorUIUtils.getJsonStreamDefinition(targetDto);

        } catch (Exception e) {
            e.printStackTrace();
        }


%>
<%=resultString%>
<%
    }

%>
