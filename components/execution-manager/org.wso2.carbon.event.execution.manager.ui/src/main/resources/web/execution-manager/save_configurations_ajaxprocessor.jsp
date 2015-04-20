<%@ page import="org.wso2.carbon.event.execution.manager.stub.ExecutionManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.execution.manager.ui.ExecutionManagerUIUtils" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.config.xsd.TemplateConfigDTO" %>
<%@ page import="org.wso2.carbon.event.execution.manager.admin.dto.config.xsd.ParameterDTO" %>
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

<%

    String domainName = request.getParameter("domainName");
    String configuration = request.getParameter("configurationName");
    String saveType = request.getParameter("saveType");
    String description = request.getParameter("description");
    String parametersJson = request.getParameter("parameters");
    String templateType = request.getParameter("templateType");

    ParameterDTO[] parameters;

    ExecutionManagerAdminServiceStub proxy = ExecutionManagerUIUtils.getExecutionManagerAdminService(config, session,
            request);

    if (saveType.equals("delete")) {
        proxy.deleteTemplateConfig(domainName, configuration);
    } else {

        TemplateConfigDTO templateConfigDTO = new TemplateConfigDTO();

        templateConfigDTO.setName(configuration);
        templateConfigDTO.setFrom(domainName);
        templateConfigDTO.setDescription(description);
        templateConfigDTO.setType(templateType);

        String[] parameterStrings = parametersJson.split(",");
        parameters = new ParameterDTO[parameterStrings.length];
        int index = 0;

        for (String parameterString : parameterStrings) {
            ParameterDTO parameterDTO = new ParameterDTO();
            parameterDTO.setName(parameterString.split(":")[0]);
            parameterDTO.setValue(parameterString.split(":")[1]);
            parameters[index] = parameterDTO;
            index++;
        }

        templateConfigDTO.setParameterDTOs(parameters);
        proxy.saveTemplateConfig(templateConfigDTO);
    }


%>