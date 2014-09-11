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
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorPropertyDto" %>
<%@ page
        import="org.wso2.carbon.event.output.adaptor.manager.ui.OutputEventAdaptorUIUtils" %>

<%
    // get required parameters to add a event adaptor to back end.
    OutputEventAdaptorManagerAdminServiceStub stub = OutputEventAdaptorUIUtils.getOutputEventManagerAdminService(config, session, request);
    String eventName = request.getParameter("eventName");
    String testConnection = request.getParameter("testConnection");
    String msg = null;

    String eventType = request.getParameter("eventType");

    String outputPropertySet = request.getParameter("outputPropertySet");
    OutputEventAdaptorPropertyDto[] inputEventProperties = null;

    if (outputPropertySet != null && (!outputPropertySet.isEmpty())) {
        String[] properties = outputPropertySet.split("\\|=");
        if (properties != null) {
            // construct event adaptor property array for each event adaptor property
            inputEventProperties = new OutputEventAdaptorPropertyDto[properties.length];
            int index = 0;
            for (String property : properties) {
                String[] propertyNameAndValue = property.split("\\$=");
                if (propertyNameAndValue != null) {
                    inputEventProperties[index] = new OutputEventAdaptorPropertyDto();
                    inputEventProperties[index].setKey(propertyNameAndValue[0].trim());
                    inputEventProperties[index].setValue(propertyNameAndValue[1].trim());
                    index++;
                }
            }

        }
    }


    try {
        if (testConnection != null) {
            stub.testConnection(eventName, eventType, inputEventProperties);
        } else {
            // add event adaptor via admin service
            stub.deployOutputEventAdaptorConfiguration(eventName, eventType, inputEventProperties);
        }
        msg = "true";
    } catch (Exception e) {
        msg = e.getMessage();

    }

%><%=msg%><%

%>
