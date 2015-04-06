<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.config.xsd.TemplateConfigDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.ParameterDTOE" %>

<html>
<head>
</head>
<body>

<fmt:bundle basename="org.wso2.carbon.event.execution.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="event.execution.manager.details"
            resourceBundle="org.wso2.carbon.event.execution.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2>Edit Configurations</h2>

        <div id="workArea">


            <%
                if (request.getParameter("configurationName") != null && request.getParameter("domainName") != null) {

            %>
            <table style="width:100%" id="eventPublisherAdd" class="styledLeft">
                <thead>
                <tr>
                    <th>Domain Template Configuration Parameters</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRaw">
                        <table class="styledLeft noBorders spacer-bot" style="width: 100%">
                            <tbody>
                            <%

                                ExecutionManagerAdminServiceStub proxy =
                                        ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session, request);
                                TemplateConfigDTO configuration =
                                        proxy.getTemplateConfiguration(request.getParameter("domainName"),
                                                request.getParameter("configurationName"));

                                TemplateDTO[] templates = proxy.getTemplateDomain(configuration.getFrom()).getTemplateDTOs();
                                TemplateDTO currentTemplate = null;

                                for (TemplateDTO template : templates) {
                                    if (template.getName().equals(request.getParameter("configurationName"))) {
                                        currentTemplate = template;
                                        break;
                                    }
                                }

                                if (currentTemplate != null) {
                                    for (ParameterDTOE parameter : currentTemplate.getParameterDTOs()) {


                            %>

                            <tr>
                                <td><h6><% out.print(parameter.getDisplayName());%></h6></td>
                                <td>

                                    <%
                                        if (parameter.getOptions() != null && !parameter.getOptions().trim().equals("")) {
                                    %>

                                    <select id="cBoxOptions">
                                        <%
                                            String[] options = parameter.getOptions().split(",");
                                            for (String option : options) {

                                                String selectedValue = "";

                                                if (option.trim().equals(parameter.getDefaultValue().trim())) {
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
                                           value="<%out.print(parameter.getDefaultValue());%>">
                                    <%
                                        }

                                        if (!parameter.getDescription().equals("")) {
                                    %>
                                    <div class="sectionHelp"><%out.print(parameter.getDescription());%></div>
                                    <%}%>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                            </tbody>
                        </table>

                    </td>
                </tbody>

            </table>
            <%


                }
            %>
        </div>
    </div>
</fmt:bundle>

</body>
</html>

