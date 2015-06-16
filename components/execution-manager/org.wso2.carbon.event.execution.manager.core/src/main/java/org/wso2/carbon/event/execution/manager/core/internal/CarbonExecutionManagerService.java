/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.execution.manager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerHelper;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.*;


/**
 * Class consist of the implementations of interface ExecutionManagerService
 */
public class CarbonExecutionManagerService implements ExecutionManagerService {
    private static final Log log = LogFactory.getLog(CarbonExecutionManagerService.class);

    private Map<String, TemplateDomain> domains;
    private Registry registry;

    public CarbonExecutionManagerService() throws ExecutionManagerException {

        domains = new HashMap<>();

        try {
            registry = ExecutionManagerValueHolder.getRegistryService().getConfigSystemRegistry();

        } catch (RegistryException e) {
            throw new ExecutionManagerException("Registry exception occurred when getting system registry ", e);
        }

        domains = ExecutionManagerHelper.loadDomains();
    }


    @Override
    public void saveConfiguration(TemplateConfiguration configuration) throws ExecutionManagerException {
        try {
            StringWriter fileContent = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, ExecutionManagerConstants.DEFAULT_CHARSET);

            jaxbMarshaller.marshal(configuration, fileContent);
            Resource resource = registry.newResource();
            resource.setContent(fileContent.toString());
            String resourceCollectionPath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                    + File.separator + configuration.getFrom();

            String resourcePath = resourceCollectionPath + File.separator
                    + configuration.getName() + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;

            //Collection directory will be created if it is not exist in the registry
            if (!registry.resourceExists(resourceCollectionPath)) {
                registry.put(resourceCollectionPath, registry.newCollection());
            }

            ExecutionManagerHelper.deployStreams(domains.get(configuration.getFrom()));
            ExecutionManagerHelper.deployExecutionPlans(configuration, domains);

            if (registry.resourceExists(resourcePath)) {
                registry.delete(resourcePath);
            }
            resource.setMediaType("application/xml");
            registry.put(resourcePath, resource);

        } catch (RegistryException e) {
            throw new ExecutionManagerException("Registry exception occurred when creating " + configuration.getName()
                    + " configurations", e);

        } catch (JAXBException e) {
            throw new ExecutionManagerException("JAXB Exception when marshalling file at " + configuration.getName()
                    + " configurations", e);
        }
    }

    @Override
    public Collection<TemplateDomain> getAllDomains() {
        return domains.values();
    }

    @Override
    public Collection<TemplateConfiguration> getConfigurations(String domainName) {
        Collection<TemplateConfiguration> templateConfigurations = new ArrayList<TemplateConfiguration>();

        String domainFilePath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                + File.separator + domainName;
        try {
            if (registry.resourceExists(domainFilePath)) {
                Resource resource = registry.get(domainFilePath);
                //All the resources of collection will be loaded
                if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                    loadConfigurations(((org.wso2.carbon.registry.core.Collection) resource).getChildren(),
                            templateConfigurations);
                }
            }
        } catch (RegistryException e) {
            log.error("Registry exception occurred when accessing files at "
                    + ExecutionManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }
        return templateConfigurations;

    }

    /**
     * Load all the configurations of given list of file paths
     *
     * @param filePaths              where configuration files are located
     * @param templateConfigurations TemplateConfiguration collection which needs to be loaded
     */
    private void loadConfigurations(String[] filePaths, Collection<TemplateConfiguration> templateConfigurations) {
        for (String filePath : filePaths) {
            templateConfigurations.add(ExecutionManagerHelper.getConfiguration(filePath, registry));
        }
    }

    @Override
    public TemplateDomain getDomain(String domainName) {
        return domains.get(domainName);
    }

    @Override
    public TemplateConfiguration getConfiguration(String domainName, String configName) {
        return ExecutionManagerHelper.getConfiguration(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                + File.separator + configName
                + ExecutionManagerConstants.CONFIG_FILE_EXTENSION, registry);
    }

    @Override
    public void deleteConfiguration(String domainName, String configName) throws ExecutionManagerException {
        /*
            First try to delete from registry if any exception occur, it will be logged.
            Then try to un deploy execution plan and log errors occur.
            So even one operation failed other operation will be executed
         */
        try {

            registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + File.separator
                    + domainName + File.separator + configName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (RegistryException e) {
            log.error("Configuration exception when deleting registry configuration file "
                    + configName + " of Domain " + domainName, e);
        }

        try {
            ExecutionManagerHelper.unDeployExistingExecutionPlan(domainName
                    + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + configName);
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Configuration exception when un deploying Execution Plan "
                    + configName + " of Domain " + domainName, e);
        }
    }

}