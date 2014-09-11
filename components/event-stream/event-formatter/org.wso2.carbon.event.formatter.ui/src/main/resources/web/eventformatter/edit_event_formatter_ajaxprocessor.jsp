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
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%
    // get required parameters to add a event formatter to back end.
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    String eventFormatterName = request.getParameter("eventFormatterName");
    String eventFormatterPath = request.getParameter("eventFormatterPath");
    String eventFormatterConfiguration = request.getParameter("eventFormatterConfiguration");
    String msg = null;
    if (eventFormatterName != null) {
        try {
            // add event formatter via admin service
            stub.editActiveEventFormatterConfiguration(eventFormatterConfiguration, eventFormatterName);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    } else if (eventFormatterPath != null) {
        try {
            // add event formatter via admin service
            stub.editInactiveEventFormatterConfiguration(eventFormatterConfiguration, eventFormatterPath);
            msg = "true";
        } catch (Exception e) {
            msg = e.getMessage();

        }
    }
    // Since JSP faithfully replicates all spaces, new lines encountered to HTML,
    // and since msg is output as a response flag, please take care in editing
    // the snippet surrounding print of msg.
%><%=msg%><%%>
