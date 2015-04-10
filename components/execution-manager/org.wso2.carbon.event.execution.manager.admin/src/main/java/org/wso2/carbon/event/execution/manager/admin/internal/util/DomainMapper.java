/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.execution.manager.admin.internal.util;

import org.wso2.carbon.event.execution.manager.admin.dto.domain.ParameterDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDomainDTO;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Parameter;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;

public class DomainMapper {

    public static TemplateDomainDTO[] mapDomains(TemplateDomain[] templateDomains) {
        TemplateDomainDTO[] templateDomainDTOs = null;

        if (templateDomains != null) {
            templateDomainDTOs = new TemplateDomainDTO[templateDomains.length];

            for (int i = 0; i < templateDomainDTOs.length; i++) {
                templateDomainDTOs[i] = mapDomain(templateDomains[i]);
            }
        }
        return templateDomainDTOs;
    }

    public static TemplateDomainDTO mapDomain(TemplateDomain templateDomain) {
        TemplateDomainDTO templateDomainDTO = null;

        if (templateDomain != null) {
            templateDomainDTO = new TemplateDomainDTO();
            templateDomainDTO.setName(templateDomain.getName());
            templateDomainDTO.setDescription(templateDomain.getName());
            templateDomainDTO.setStreams(templateDomain.getStreams());
            templateDomainDTO.setTemplateDTOs(mapTemplates(templateDomain.getTemplates()));
        }

        return templateDomainDTO;
    }

    public static TemplateDTO[] mapTemplates(Template[] templates) {
        TemplateDTO[] templateDTOs = null;

        if (templates != null) {
            templateDTOs = new TemplateDTO[templates.length];
            for (int i = 0; i < templateDTOs.length; i++) {
                templateDTOs[i] = mapTemplate(templates[i]);
            }
        }
        return templateDTOs;
    }

    public static TemplateDTO mapTemplate(Template template) {
        TemplateDTO templateDTO = null;

        if (template != null) {
            templateDTO = new TemplateDTO();
            templateDTO.setName(template.getName());
            templateDTO.setDescription(template.getDescription());
            templateDTO.setExecutionPlan(template.getExecutionPlan());
            templateDTO.setParameterDTOs(mapParameters(template.getParameters()));
        }
        return templateDTO;
    }

    public static ParameterDTO[] mapParameters(Parameter[] parameters) {
        ParameterDTO[] parameterDTOs = null;

        if (parameters != null) {
            parameterDTOs = new ParameterDTO[parameters.length];
            for (int i = 0; i < parameterDTOs.length; i++) {
                parameterDTOs[i] = mapParameter(parameters[i]);
            }
        }
        return parameterDTOs;
    }

    public static ParameterDTO mapParameter(Parameter parameter) {
        ParameterDTO parameterDTO = null;

        if (parameter != null) {
            parameterDTO = new ParameterDTO();
            parameterDTO.setName(parameter.getName());
            parameterDTO.setDescription(parameter.getDescription());
            parameterDTO.setDefaultValue(parameter.getDefaultValue());
            parameterDTO.setDisplayName(parameter.getDisplayName());
            parameterDTO.setType(parameter.getType());
            parameterDTO.setOptions(parameter.getOptions());
        }
        return parameterDTO;
    }

}
