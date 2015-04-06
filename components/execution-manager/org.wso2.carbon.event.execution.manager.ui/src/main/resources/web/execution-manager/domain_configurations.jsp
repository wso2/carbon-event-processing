<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.config.xsd.TemplateConfigDTO" %>

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
                if (request.getParameter("domainName") != null) {

                    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session, request);
                    TemplateConfigDTO[] configurations = proxy.getTemplateConfigurations(request.getParameter("domainName"));
            %>

            <a href="template_configurations.jsp?ordinal=1"
               style="background-image:url(images/add.gif);" class="icon-link">
                Add Configuration
            </a>

            <table class="styledLeft">

                <thead>
                <tr>
                    <th>Template Name</th>
                    <th>Description</th>
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
                    <td>
                        <a style="background-image: url(images/edit.gif);" class="icon-link"
                           href="template_configurations.jsp?ordinal=1&configurationName=<%out.print(templateConfig.getName());%>&domainName=<%out.print(templateConfig.getFrom());%>">
                            <font color="#4682b4">Edit Configuration</font></a>
                    </td>
                </tr>
                <%
                        }

                    }
                %>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>

</body>
</html>

