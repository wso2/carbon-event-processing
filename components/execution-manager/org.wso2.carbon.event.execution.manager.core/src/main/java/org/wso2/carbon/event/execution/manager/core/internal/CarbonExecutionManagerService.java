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
package org.wso2.carbon.event.execution.manager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.structure.config.TemplateConfig;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class CarbonExecutionManagerService implements ExecutionManagerService {
    private static final Log log = LogFactory.getLog(CarbonExecutionManagerService.class);

    private HashMap<String, TemplateDomain> domains;
    private HashMap<String, TemplateConfig> configurations;
    private Registry registry;

    public CarbonExecutionManagerService() {

        domains = new HashMap<String, TemplateDomain>();
        configurations = new HashMap<String, TemplateConfig>();

        try {
            registry = ExecutionManagerValueHolder.getRegistryService().getConfigSystemRegistry();

        } catch (RegistryException e) {
            log.error("Registry exception occurred when getting system registry of service ", e);
        }

        this.loadDomainConfigurations();
    }

    /**
     * Load All domains and configurations
     */
    private void loadDomainConfigurations() {
        //Get domain template folder and load all the domain template files
        File folder = new File(ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH);

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(TemplateDomain.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    TemplateDomain templateDomain = (TemplateDomain) jaxbUnmarshaller.unmarshal(fileEntry);

                    this.deployStreams(templateDomain);
                    this.loadConfigurations(templateDomain);

                    domains.put(templateDomain.getName(), templateDomain);
                } catch (JAXBException e) {
                    log.error("JAXB Exception when unmarshalling domain template file at " + fileEntry.getPath());
                }
            }
        }
    }

    /**
     * Load available configurations for given template domain
     *
     * @param templateDomain template domain object
     */
    private void loadConfigurations(TemplateDomain templateDomain) {
            for (Template template : templateDomain.getTemplates()) {
                String registryPath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                        + ExecutionManagerConstants.PATH_SEPARATOR
                        + templateDomain.getName()
                        + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + template.getName()
                        + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
                try {
                    if (registry.resourceExists(registryPath)) {
                        Resource configFile = registry.get(registryPath);

                        if (configFile != null) {
                            StringReader reader = new StringReader(new String(
                                    (byte[]) configFile.getContent()));
                            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateConfig.class);
                            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                            TemplateConfig templateConfig =
                                    (TemplateConfig) jaxbUnmarshaller.unmarshal(reader);
                            configurations.put(templateDomain.getName()
                                    + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR
                                    + templateConfig.getName(), templateConfig);

                        }
                    }
                } catch (RegistryException e) {
                    log.error("Registry exception occurred when accessing file at " + registryPath, e);
                } catch (JAXBException e) {
                    log.error("JAXB Exception when unmarshalling configuration file at " + registryPath);
                }
            }
    }


    /**
     * Deploy Streams of given template domain
     *
     * @param templateDomain template domain object
     */
    private void deployStreams(TemplateDomain templateDomain) {
        for (String stream : templateDomain.getStreams()) {
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = EventDefinitionConverterUtils.convertFromJson(stream);
                ExecutionManagerValueHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
            } catch (MalformedStreamDefinitionException e) {
                log.error("Stream definition is incorrect in domain template " + templateDomain.getName(), e);
            } catch (EventStreamConfigurationException e) {
                log.error("Exception occurred when configuring stream " + streamDefinition.getName());
            }
        }
    }

    /**
     * Deploy given configurations template Execution Plan
     *
     * @param configuration configuration object
     */
    private void deployExecutionPlan(TemplateConfig configuration) {
//        for (Template template : domains.get(configuration.getFrom()).getTemplates()) {
//            if (template.getName().equals(configuration.getName())) {
//                try {
//                    //Get Template Execution plan, Tenant Id and deploy Execution Plan
//                    ExecutionManagerValueHolder.getEventProcessorService()
//                            .deployExecutionPlanConfiguration(template.getExecutionPlan(), new AxisConfiguration());
//                    break;
//                } catch (ExecutionPlanConfigurationException e) {
//                    log.error("Configuration exception when adding Execution Plan of Template "
//                            + configuration.getName() + " of Domain " + configuration.getFrom(), e);
//
//                } catch (ExecutionPlanDependencyValidationException e) {
//                    log.error("Dependency validation exception when adding Execution Plan of Template "
//                            + configuration.getName() + " of Domain " + configuration.getFrom(), e);
//                }
//            }
//        }
    }

    @Override
    public void saveTemplateConfig(TemplateConfig configuration) throws ExecutionManagerException {

        try {
            StringWriter fileContent = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateConfig.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, ExecutionManagerConstants.DEFAULT_CHARSET);
            String configFullName = configuration.getFrom() + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR
                    + configuration.getName();

            jaxbMarshaller.marshal(configuration, fileContent);
            Resource resource = registry.newResource();
            resource.setContent(fileContent.toString());
            resource.setProperty("name", configuration.getName());
            resource.setProperty("description", configuration.getDescription());
            resource.setProperty("type", "");
            String resourcePath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                    + ExecutionManagerConstants.PATH_SEPARATOR + configFullName
                    + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;

            this.deployExecutionPlan(configuration);
            registry.put(resourcePath, resource);
            configurations.put(configFullName, configuration);

        } catch (RegistryException e) {
            log.error("Registry exception occurred when creating " + configuration.getName() + " configurations", e);
            throw new ExecutionManagerException("Exception occurred when creating " + configuration.getName()
                    + " configurations", e);

        } catch (JAXBException e) {
            log.error("JAXB Exception when marshalling file at " + configuration.getName() + " configurations", e);
            throw new ExecutionManagerException("Exception occurred when creating " + configuration.getName()
                    + " configurations", e);
        }
    }


    @Override
    public TemplateDomain[] getAllDomains() {
        Iterator it = domains.entrySet().iterator();
        TemplateDomain[] domainAll = new TemplateDomain[domains.size()];
        int index = 0;

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {
                domainAll[index] = (TemplateDomain) pair.getValue();
                index++;
            }
        }
        return domainAll;
    }

    @Override
    public TemplateConfig[] getAllConfigurations() {
        Iterator it = configurations.entrySet().iterator();
        TemplateConfig[] configAll = new TemplateConfig[configurations.size()];
        int index = 0;

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {
                configAll[index] = (TemplateConfig) pair.getValue();
                index++;
            }
        }
        return configAll;
    }

    @Override
    public TemplateConfig[] getConfigurations(String domainName) {
        Iterator it = configurations.entrySet().iterator();
        List<TemplateConfig> configAllList = new ArrayList<TemplateConfig>();

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {

                TemplateConfig config = (TemplateConfig) pair.getValue();

                //check configuration from same domain
                if (config.getFrom().equals(domainName)) {
                    configAllList.add((TemplateConfig) pair.getValue());
                }
            }
        }
        //Convert array list to array and return
        return configAllList.toArray(new TemplateConfig[configAllList.size()]);
    }

    @Override
    public TemplateDomain getDomain(String domainName) {
        return domains.get(domainName);
    }

    @Override
    public TemplateConfig getConfiguration(String domainName, String configName) {
        return configurations.get(domainName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + configName);
    }

    @Override
    public void deleteConfig(String domainName, String configName) throws ExecutionManagerException {
        try {

            String configFullName = domainName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + configName;

            registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + ExecutionManagerConstants.PATH_SEPARATOR
                    + configFullName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
            configurations.remove(configFullName);

        } catch (Exception e) {
            log.error("Exception when deleting file at " + configName + " configurations", e);
            throw new ExecutionManagerException("Exception when deleting file at " + configName + " configurations", e);
        }
    }

}