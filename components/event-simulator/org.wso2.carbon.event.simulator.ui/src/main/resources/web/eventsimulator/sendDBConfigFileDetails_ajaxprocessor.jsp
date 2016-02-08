<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%
    String msg = null;

    try {

        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);
        String fileName = request.getParameter("fileName");
        String mode = request.getParameter("mode");

        if ("send".equals(mode)) {
            stub.sendDBConfigFileNameToSimulate(fileName);
            msg = "sent";
        } else if ("pause".equals(mode)) {
            stub.pauseDBConfigFileNameToSimulate(fileName);
            msg = "paused";
        } else if ("resume".equals(mode)) {
            stub.resumeDBConfigFileNameToSimulate(fileName);
            msg = "resumed";
        } else if ("stop".equals(mode)) {
            stub.stopDBConfigFileNameToSimulate(fileName);
            msg = "stopped";
        }
    } catch (Exception e) {
        msg = e.getMessage();
    }
%>
<%=msg%>