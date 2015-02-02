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
<%@ page import="org.wso2.carbon.event.simulator.stub.EventSimulatorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.simulator.ui.EventSimulatorUIUtils" %>
<%
    String msg = null;

    try{

        EventSimulatorAdminServiceStub stub = EventSimulatorUIUtils.getEventSimulatorAdminService(config, session, request);

        String eventStreamAndDataSourceColumnNamesAndTypes=request.getParameter("eventStreamAndDataSourceColumnNamesAndTypes");

        String result = stub.testSimulateRDBMSDataSourceConnection(eventStreamAndDataSourceColumnNamesAndTypes);

        char         c = 0;
        int          i;
        int          len = result.length();
        StringBuilder escapeString = new StringBuilder(len + 4);
        String       t;

        escapeString.append('"');
        for (i = 0; i < len; i += 1) {
            c = result.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    escapeString.append('\\');
                    escapeString.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    escapeString.append('\\');
                    //                }
                    escapeString.append(c);
                    break;
                case '\b':
                    escapeString.append("\\b");
                    break;
                case '\t':
                    escapeString.append("\\t");
                    break;
                case '\n':
                    escapeString.append("\\n");
                    break;
                case '\f':
                    escapeString.append("\\f");
                    break;
                case '\r':
                    escapeString.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        escapeString.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        escapeString.append(c);
                    }
            }
        }
        escapeString.append('"');

        msg="{\"success\":\"success\",\"message\":" +escapeString+ "}";
    }catch(Exception e){
        msg="{\"success\":\"fail\",\"message\":\"" +e.getMessage()+ "\"}";
    }
%>
<%=msg%>