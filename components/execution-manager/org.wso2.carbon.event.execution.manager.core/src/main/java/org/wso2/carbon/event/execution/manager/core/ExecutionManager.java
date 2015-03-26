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
package org.wso2.carbon.event.execution.manager.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class ExecutionManager {

    private Registry registry;

    /**
     * constructor
     */
    public ExecutionManager() {
        CarbonContext cCtx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        registry = (Registry) cCtx.getRegistry(RegistryType.SYSTEM_CONFIGURATION);
        String registryType = RegistryType.SYSTEM_CONFIGURATION.toString();
        if (registryType != null) {
            registry = (Registry) cCtx.getRegistry(RegistryType.valueOf(registryType));
        }
    }

    /**
     * save template configuration xml files in wso2 carbon registry
     *
     * @param fileName    template configuration name
     * @param fileContent xml content
     * @param description template configuration description
     * @param type        template configuration type
     */
    public void saveTemplateConfig(String fileName, String fileContent, String description, String type) {
        final Log log = LogFactory.getLog(ExecutionManager.class);
        try {
            Resource resource = registry.newResource();
            resource.setContent(fileContent);
            resource.setProperty("name", fileName);
            resource.setProperty("description", description);
            resource.setProperty("type", type);
            String resourcePath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                    + ExecutionManagerConstants.PATH_SEPARATOR + fileName
                    + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
            registry.put(resourcePath, resource);
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * get template configuration content when the configuration name is given
     *
     * @param configName template configuration name
     * @return template configuration xml content as a string value
     */
    public String getTemplateConfig(String configName) {

        Resource config_file;
        String configContent = "";
        try {
            config_file = registry.get(
                    ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + ExecutionManagerConstants.PATH_SEPARATOR
                            + configName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
            configContent = new String((byte[]) config_file.getContent());

        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            e.printStackTrace();
        }
        return configContent;
    }

    /**
     * get all template configurations' details as a DomainConfigInfoDTO objects array
     *
     * @return DomainConfigInfoDTO objects array
     */
    public DomainConfigInfoDTO[] getAllTemplateConfig() {
        DomainConfigInfoDTO[] domainConfigInfo = null;

        try {

            Resource resp = registry.get(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH);
            org.wso2.carbon.registry.api.Collection collection = (org.wso2.carbon.registry.api.Collection) resp;
            String[] resources = collection.getChildren();
            domainConfigInfo = new DomainConfigInfoDTO[resources.length];
            Resource config_file;
            for (int i = 0; i < resources.length; i++) {
                config_file = registry.get(resources[i]);


                DomainConfigInfoDTO domainConfigObj = new DomainConfigInfoDTO(config_file.getProperty("name"),
                        config_file.getProperty("type"), config_file.getProperty("description"));
                domainConfigInfo[i] = domainConfigObj;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainConfigInfo;
    }

    /**
     * delete template configuration when the name of configuration is given
     *
     * @param configName template configuration name
     */
    public void deleteTemplateConfig(String configName) {

        try {
            registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + ExecutionManagerConstants.PATH_SEPARATOR
                    + configName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
