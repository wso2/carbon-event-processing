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
package org.wso2.carbon.event.execution.manager.admin;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.execution.manager.admin.dto.config.TemplateConfigDTO;
import org.wso2.carbon.event.execution.manager.admin.dto.domain.TemplateDomainDTO;
import org.wso2.carbon.event.execution.manager.admin.internal.ds.ExecutionAdminManagerValueHolder;
import org.wso2.carbon.event.execution.manager.admin.internal.util.ConfigMapper;
import org.wso2.carbon.event.execution.manager.admin.internal.util.DomainMapper;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;

public class ExecutionManagerAdminService extends AbstractAdmin {

    /**
     * Default Constructor
     */
    public ExecutionManagerAdminService() {

    }

    /**
     * return all available template domain information
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public TemplateDomainDTO[] getAllTemplateDomains() throws AxisFault {
        return DomainMapper.mapDomains(ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService()
                .getAllDomains());
    }

    /**
     * return details for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public TemplateDomainDTO getTemplateDomain(String domainName) throws AxisFault {
        return DomainMapper.mapDomain(ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService()
                .getDomain(domainName));
    }

    /**
     * return all available template configurations
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public TemplateConfigDTO[] getAllTemplateConfigurations() throws AxisFault {
        return ConfigMapper.mapConfigurations(ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService()
                .getAllConfigurations());
    }

    /**
     * return details for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain configuration details
     * @throws AxisFault
     */
    public TemplateConfigDTO[] getTemplateConfigurations(String domainName) throws AxisFault {
        return ConfigMapper.mapConfigurations(ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService()
                .getConfigurations(domainName));
    }


    /**
     * return details for a given template configuration name
     *
     * @param configName template configuration name
     * @return template domain configuration details
     * @throws AxisFault
     */
    public TemplateConfigDTO getTemplateConfiguration(String domainName, String configName) throws AxisFault {
        return ConfigMapper.mapConfiguration(ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService()
                .getConfiguration(domainName, configName));
    }

    /**
     * Delete specified configuration
     *
     * @param configName configuration name which needs to be deleted
     */
    public void deleteTemplateConfig(String domainName, String configName) throws AxisFault{
        try {
            ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService().deleteConfig(domainName, configName);
        } catch (ExecutionManagerException e) {
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * Create or update specified configuration
     *
     * @param configuration configuration data transfer object
     */
    public void saveTemplateConfig(TemplateConfigDTO configuration) throws AxisFault{
        try {
            ExecutionAdminManagerValueHolder.getCarbonExecutorManagerService().saveTemplateConfig(
                    ConfigMapper.mapConfiguration(configuration));
        } catch (ExecutionManagerException e) {
            throw new AxisFault(e.getMessage());
        }
    }
}
