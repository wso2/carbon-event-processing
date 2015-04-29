<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.configuration.xsd.TemplateConfigurationInfoDTO" %>
<%@ page import="org.apache.axis2.AxisFault" %>

<fmt:bundle basename="org.wso2.carbon.event.execution.manager.ui.i18n.Resources">
    <html>
    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
        <script type="text/javascript" src="../admin/js/jquery-1.6.3.min.js"></script>
        <script src="../dialog/js/jqueryui/jquery-ui.min.js" type="text/javascript"></script>
        <link href="../admin/css/global.css" rel="stylesheet" type="text/css" media="all"/>
        <link href="css/execution_manager.css" rel="stylesheet" type="text/css" media="all"/>
        <script type="text/javascript" src="js/domain_config_update.js"></script>
        <script type="text/javascript" src="../dialog/js/dialog.js"></script>
        <link media="all" type="text/css" rel="stylesheet" href="../dialog/css/dialog.css"/>
        <link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css" media="all"/>
        <meta charset="UTF-8">
    </head>
    <body>
    <div id="container">
        <div id="headerArea">
            <fmt:message key='application.name'/>
        </div>


        <div id="middle">
            <div id="dcontainer"></div>
            <div id="workArea">
                <h2><fmt:message key='domain.header.text'/></h2>


                <%
                    if (request.getParameter("domainName") != null) {

                %>
                <a href="domains_ajaxprocessor.jsp"><fmt:message key='application.name'/></a>
                <fmt:message key='common.navigation.seperator'/> <%=(request.getParameter("domainName"))%> <br/><br/>
                <%

                    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils
                            .getExecutionManagerAdminService(config, session, request);

                    try {

                        TemplateConfigurationInfoDTO[] configurations = proxy
                                .getConfigurationsInfo(request.getParameter("domainName"));


                %>

                <a href="template_configurations_ajaxprocessor.jsp?ordinal=1&domainName=<%=request.getParameter("domainName")%>"
                   style="background-image:url(images/add.gif);" class="icon-link">
                    Add Configuration
                </a>

                <%
                    if (configurations != null && configurations.length > 0) {
                %>

                <table class="styledLeft" id="tblConfigs">

                    <thead>
                    <tr>
                        <th><fmt:message key='domain.table.column.name'/></th>
                        <th><fmt:message key='domain.table.column.description'/></th>
                        <th><fmt:message key='domain.table.column.type'/></th>
                        <th width="420px"><fmt:message key='common.table.column.action'/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <%
                        for (TemplateConfigurationInfoDTO templateConfigurationDTO : configurations) {
                    %>

                    <tr>
                        <td>
                            <%=templateConfigurationDTO.getName()%>
                        </td>
                        <td><%=templateConfigurationDTO.getDescription()%>
                        </td>
                        <td><%=templateConfigurationDTO.getType()%>
                        </td>
                        <td>

                            <a onclick="deleteConfiguration('<%=templateConfigurationDTO.getFrom()%>','<%=templateConfigurationDTO.getName()%>',this, 'tblConfigs')"
                               style="background-image: url(images/delete.gif);" class="icon-link">
                                <font color="#4682b4"><fmt:message key='common.button.delete'/></font></a>

                            <a style="background-image: url(images/edit.gif);" class="icon-link"
                               href="template_configurations_ajaxprocessor.jsp?configurationName=<%=templateConfigurationDTO.getName()%>&domainName=<%=templateConfigurationDTO.getFrom()%>">
                                <font color="#4682b4"><fmt:message key='common.button.edit'/></font></a>
                        </td>
                    </tr>
                    <%
                        }
                    } else {

                    %>

                    <table class="styledLeft">
                        <tbody>
                        <tr>
                            <td class="formRaw">
                                <table id="noEventReceiverInputTable" class="normal-nopadding" style="width:100%">
                                    <tbody>
                                    <tr>
                                        <td class="leftCol-med" colspan="2"><fmt:message key='domain.empty.text'/>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        </tbody>


                    </table>
                    <%
                                }

                            } catch (AxisFault e) {
                                response.sendRedirect("../admin/login.jsp");
                            }

                        }
                    %>
                    </tbody>
                </table>
            </div>
        </div>


        <div id="footerArea">
            <div class="copyright">© 2015 WSO2 Inc. All Rights Reserved.</div>
        </div>
    </div>


    </body>
    </html>
</fmt:bundle>
