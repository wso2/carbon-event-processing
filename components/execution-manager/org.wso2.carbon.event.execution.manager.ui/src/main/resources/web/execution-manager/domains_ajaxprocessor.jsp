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
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainDTO" %>
<%@ page import="org.apache.axis2.AxisFault" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <link href="../admin/css/global.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="css/execution_manager.css" rel="stylesheet" type="text/css" media="all"/>
</head>
<body>
<div id="container">
    <div id="headerArea">
        Execution Manager
    </div>

    <div id="middle">
        <div id="workArea">
            <h2>Domains</h2>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>Domain Name</th>
                    <th>Description</th>
                    <th width="420px">Actions</th>
                </tr>
                </thead>
                <tbody>
                <%
                    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session, request);
                    try {
                        TemplateDomainDTO[] domainDTOs = proxy.getAllTemplateDomains();

                        if (domainDTOs != null && domainDTOs.length > 0) {

                            for (TemplateDomainDTO domainDTO : domainDTOs) {
                %>
                <tr>
                    <td>
                        <%out.print(domainDTO.getName());%>
                    </td>
                    <td><%out.print(domainDTO.getDescription());%></td>
                    <td>
                        <a style="background-image: url(images/edit.gif);" class="icon-link"
                           href="domain_configurations_ajaxprocessor.jsp?ordinal=1&domainName=<%out.print(domainDTO.getName());%>">
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
                                    <td class="leftCol-med" colspan="2">No Domains to be listed
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

