<!--
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
-->
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.event.input.adaptor.manager.stub.types.InputEventAdaptorFileDto" %>
<%@ page import="org.wso2.carbon.event.input.adaptor.manager.ui.InputEventAdaptorUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="details"
            resourceBundle="org.wso2.carbon.event.input.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">
        function doDelete(filePath) {
            var theform = document.getElementById('deleteForm');
            theform.filePath.value = filePath;
            theform.submit();
        }
    </script>
    <%
        String filePath = request.getParameter("filePath");
        if (filePath != null) {
            InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
            stub.undeployInactiveInputEventAdaptorConfiguration(filePath);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event File successfully deleted.');</script>
    <%
        }
    %>


    <div id="middle">
        <h2><fmt:message key="notdeployed.input.event.adaptors"/></h2>

        <div id="workArea">

            <table class="styledLeft">

                <%
                    InputEventAdaptorManagerAdminServiceStub stub = InputEventAdaptorUIUtils.getInputEventManagerAdminService(config, session, request);
                    InputEventAdaptorFileDto[] eventDetailsArray = stub.getAllInactiveInputEventAdaptorConfigurationFile();
                    if (eventDetailsArray != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="file.name"/></th>
                    <th><fmt:message key="event.adaptor.name"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    for (InputEventAdaptorFileDto inputEventAdaptorFile : eventDetailsArray) {

                %>

                <tbody>
                <tr>
                    <td>
                        <%=inputEventAdaptorFile.getFileName()%>
                    </td>
                    <td><%=inputEventAdaptorFile.getEventAdaptorName()%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=inputEventAdaptorFile.getFileName()%>')"><font
                                color="#4682b4">Delete</font></a>
                        <a style="background-image: url(../admin/images/edit.gif);"
                           class="icon-link"
                           href="edit_event_details.jsp?ordinal=1&eventPath=<%=inputEventAdaptorFile.getFileName()%>"><font
                                color="#4682b4">Source View</font></a>
                    </td>

                </tr>
                <%
                        }
                    }
                %>
                </tbody>
            </table>

            <div>
                <form id="deleteForm" name="input" action="" method="post"><input type="HIDDEN"
                                                                                 name="filePath"
                                                                                 value=""/></form>
            </div>
        </div>


        <script type="text/javascript">
            alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
        </script>
</fmt:bundle>