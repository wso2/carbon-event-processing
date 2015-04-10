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
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTOE" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.config.xsd.ParameterDTO" %>
<%@ page import="org.apache.axis2.AxisFault" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <script type="text/javascript" src="../admin/js/jquery-1.6.3.min.js"></script>
    <link href="../admin/css/global.css" rel="stylesheet" type="text/css"/>
    <link rel="stylesheet" href="../admin/css/carbonFormStyles.css">
    <link href="css/execution_manager.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="js/domain_config_update.js"></script>
</head>
<body>
<div id="container">
<div id="headerArea">
    Execution Manager
</div>

<div id="middle">


<div id="workArea">

<h2>Edit Configurations</h2>


<%
    if (request.getParameter("domainName") != null) {

        String domainName = request.getParameter("domainName");
        String configurationName = request.getParameter("configurationName");

%>
<a href="domains_ajaxprocessor.jsp">Execution Manager</a> >> <a
        href="domain_configurations_ajaxprocessor.jsp?domainName=<%out.print(domainName);%>">Edit
    Configurations</a><br/><br/>
<%

    try {

        ExecutionManagerAdminServiceStub proxy =
                ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session, request);
        TemplateDomainDTO domain =
                proxy.getTemplateDomain(domainName);
        TemplateDTO currentTemplate = null;
        String saveButtonText = "Add Configuration";
        String parameterString = "";

%>


<table style="width:100%" id="configParameters" class="styledLeft">
    <thead>
    <tr>
        <th>Configuration Parameters</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRaw">
            <table class="styledLeft noBorders spacer-bot" style="width: 100%">
                <tbody>
                <%
                    for (TemplateDTO template : domain.getTemplateDTOs()) {
                        if (configurationName == null || template.getName().equals(configurationName)) {
                            currentTemplate = template;
                            configurationName = template.getName();
                            break;
                        }
                    }

                    if (currentTemplate != null) {

                        TemplateConfigDTO configDTO = proxy.getTemplateConfiguration(domainName,
                                configurationName);
                        String parameterValue = "";
                        String description = "";
                %>

                <tr>
                    <td><h6>Template Name</h6></td>
                    <td>
                        <select id="cBoxTemplates"
                                onchange="document.location.href=document.getElementById('cBoxTemplates').options[document.getElementById('cBoxTemplates').selectedIndex].value">
                            <%

                                for (TemplateDTO templateDTO : domain.getTemplateDTOs()) {

                                    String selectedValue = "";

                                    if (description.equals("")
                                            || templateDTO.getName().trim().equals(configurationName)) {
                                        description = templateDTO.getDescription().trim();
                                    }

                                    if (templateDTO.getName().trim().equals(configurationName)) {
                                        selectedValue = "selected=true";
                                    }
                            %>
                            <option <%out.print(selectedValue);%>
                                    value="template_configurations_ajaxprocessor.jsp?configurationName=<%out.print(templateDTO.getName());%>&domainName=<%out.print(domainName);%>">
                                <%out.print(templateDTO.getName());%>
                            </option>
                            <%}%>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><h6>Description</h6></td>
                    <td>
                        <%
                            if (configDTO != null) {
                                description = configDTO.getDescription().trim();
                                saveButtonText = "Update Configuration";
                            }
                        %>

                        <input type="text" id="txtDescription"
                               value="<%out.print(description);%>">
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <b>Parameter Configurations</b>
                    </td>
                </tr>


                <%
                    int indexParam = 0;

                    for (ParameterDTOE parameter : currentTemplate.getParameterDTOs()) {

                        if (configDTO == null) {
                            parameterValue = parameter.getDefaultValue().trim();
                        } else if (configDTO.getParameterDTOs() != null) {

                            for (ParameterDTO param : configDTO.getParameterDTOs()) {
                                if (param.getName().equals(parameter.getName())) {
                                    parameterValue = param.getValue().trim();
                                    break;
                                }
                            }
                        }
                %>


                <tr>
                    <td><h6><%
                        if (parameter.getDisplayName() == null) {
                            out.print(parameter.getName());
                        } else {
                            out.print(parameter.getDisplayName());
                        }
                    %>

                    </h6></td>
                    <td>

                        <%
                            if (parameter.getOptions() != null && !parameter.getOptions().trim().equals("")) {
                        %>

                        <select id="<% out.print(parameter.getName());%>">
                            <%
                                String[] options = parameter.getOptions().split(",");
                                for (String option : options) {

                                    String selectedValue = "";

                                    if (option.trim().equals(parameterValue)) {
                                        selectedValue = "selected=true";
                                    }
                            %>
                            <option <%out.print(selectedValue);%> value=<%out.print(option);%>>
                                <%out.print(option);%>
                            </option>
                            <%}%>
                        </select>

                        <%
                        } else {
                        %>
                        <input type="text" id="<% out.print(parameter.getName());%>"
                               value="<%out.print(parameterValue);%>">
                        <%
                            }

                            if (!parameter.getDescription().equals("")) {
                        %>
                        <div class="sectionHelp"><%out.print(parameter.getDescription());%></div>
                        <%}%>
                    </td>
                </tr>
                <%
                            parameterString += "'" + parameter.getName() +
                                    ":' + document.getElementById('"
                                    + parameter.getName() + "').value";

                            indexParam++;
                            if (indexParam < currentTemplate.getParameterDTOs().length) {
                                parameterString += "+ ',' +";
                            }

                        }
                    }
                %>
                </tbody>
            </table>

        </td>
    </tr>
    <tr>
        <td class="buttonRow">
            <input type="button" value="<%out.print(saveButtonText);%>"
                   onclick="saveConfiguration('<%out.print(domainName);%>','<%out.print(configurationName);%>', document.getElementById('txtDescription').value, <% out.print(parameterString);%>)">
        </td>
    </tr>
    </tbody>

</table>
<%
        } catch (AxisFault e) {
            response.sendRedirect("../admin/login.jsp");
        }
    }
%>
</div>
</div>

<div id="footerArea">
    <div class="copyright">© 2015 WSO2 Inc. All Rights Reserved.</div>
</div>
</div>

</body>
</html>

