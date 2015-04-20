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
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.config.xsd.TemplateConfigDTO" %>
<%@ page import="org.apache.axis2.AxisFault" %>

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
        Execution Manager
    </div>


    <div id="middle">
        <div id="dcontainer"></div>
        <div id="workArea">
            <h2>Configurations</h2>


            <%
                if (request.getParameter("domainName") != null) {

                    %>
            <a href="domains_ajaxprocessor.jsp">Execution Manager</a>
            >  <%out.print(request.getParameter("domainName"));%> <br/><br/>
            <%

                    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils
                            .getExecutionManagerAdminService(config, session, request);

                    try {

                        TemplateConfigDTO[] configurations = proxy
                                .getTemplateConfigurations(request.getParameter("domainName"));


            %>

            <a href="template_configurations_ajaxprocessor.jsp?ordinal=1&domainName=<%out.print(request.getParameter("domainName"));%>"
               style="background-image:url(images/add.gif);" class="icon-link">
                Add Configuration
            </a>

            <%
                if (configurations != null && configurations.length > 0) {
            %>

            <table class="styledLeft" id="tblConfigs">

                <thead>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Template Type</th>
                    <th width="420px">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%


                    for (TemplateConfigDTO templateConfig : configurations) {
                %>

                <tr>
                    <td>
                        <%out.print(templateConfig.getName());%>
                    </td>
                    <td><%out.print(templateConfig.getDescription());%></td>
                    <td><%out.print(templateConfig.getType());%></td>
                    <td>

                        <a onclick="deleteConfiguration('<%out.print(templateConfig.getFrom());%>','<%out.print(templateConfig.getName());%>',this, 'tblConfigs')"
                           style="background-image: url(images/delete.gif);" class="icon-link">
                            <font color="#4682b4">Delete</font></a>

                        <a href="#" class="modal" id="modal-one" aria-hidden="true">
                        </a>

                        <div class="modal-dialog">
                            <div class="modal-header">
                                <h2>Warning</h2>
                                <a href="#" class="btn-close" aria-hidden="true">x</a>
                            </div>
                            <div class="modal-body">
                                <p>Are you sure want to delete?</p>
                            </div>
                            <div class="modal-footer">
                                <a href="#" class="btn"
                                   onclick="">Yes</a>
                            </div>
                        </div>

                        <a style="background-image: url(images/edit.gif);" class="icon-link"
                           href="template_configurations_ajaxprocessor.jsp?configurationName=<%out.print(templateConfig.getName());%>&domainName=<%out.print(templateConfig.getFrom());%>">
                            <font color="#4682b4">Edit</font></a>
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
                                    <td class="leftCol-med" colspan="2">No Configurations to be listed
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

