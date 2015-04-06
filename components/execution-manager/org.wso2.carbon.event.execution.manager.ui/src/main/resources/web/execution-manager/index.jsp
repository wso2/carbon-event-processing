<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.domain.xsd.TemplateDomainDTO" %>

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
        <h2>Domain Manager</h2>

            <div id="workArea">

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
                        TemplateDomainDTO[] domainDTOs = proxy.getAllTemplateDomains();

                        for (TemplateDomainDTO domainDTO : domainDTOs) {
                    %>

                    <tr>
                        <td>
                            <%out.print(domainDTO.getName());%>
                        </td>
                        <td><%out.print(domainDTO.getDescription());%></td>
                        <td>
                            <a style="background-image: url(images/edit.gif);" class="icon-link"
                               href="domain_configurations.jsp?ordinal=1&domainName=<%out.print(domainDTO.getName());%>">
                                <font color="#4682b4">Edit Domain Configurations</font></a>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </div>
    </div>
</fmt:bundle>

</body>
</html>

