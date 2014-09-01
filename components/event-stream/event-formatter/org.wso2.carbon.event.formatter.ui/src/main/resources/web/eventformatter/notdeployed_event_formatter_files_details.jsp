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
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationFileDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">

    <carbon:breadcrumb
            label="inactive.event.formatters.breadcrumb"
            resourceBundle="org.wso2.carbon.event.formatter.ui.i18n.Resources"
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
            EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
            stub.undeployInactiveEventFormatterConfiguration(filePath);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event Formatter file successfully deleted.');</script>
    <%
        }
    %>


    <div id="middle">
        <h2><fmt:message key="not.deployed.event.formatters"/></h2>

        <div id="workArea">

            <table class="styledLeft">

                <%
                    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
                    EventFormatterConfigurationFileDto[] eventFormatterDetailsArray = stub.getAllInactiveEventFormatterConfiguration();
                    if (eventFormatterDetailsArray != null) {
                %>
                <thead>
                <tr>
                    <th><fmt:message key="file.name"/></th>
                    <th><fmt:message key="inactive.reason.header"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                </thead>
                <%
                    for (EventFormatterConfigurationFileDto eventFormatterFile : eventFormatterDetailsArray) {

                %>

                <tbody>
                <tr>
                    <td>
                        <%=eventFormatterFile.getFileName()%>
                    </td>
                    <td><%=eventFormatterFile.getDeploymentStatusMsg()%>
                    </td>
                    <td>
                        <a style="background-image: url(../admin/images/delete.gif);"
                           class="icon-link"
                           onclick="doDelete('<%=eventFormatterFile.getFileName()%>')"><font
                                color="#4682b4">Delete</font></a>
                        <a style="background-image: url(../admin/images/edit.gif);"
                           class="icon-link"
                           href="edit_event_formatter_details.jsp?ordinal=1&eventFormatterPath=<%=eventFormatterFile.getFileName()%>"><font
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