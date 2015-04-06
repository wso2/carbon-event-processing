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
import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.structure.config.TemplateConfig;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;
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
        //get template domain file path
        String filePath = ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH;
        File folder = new File(filePath);
        domains = new HashMap<String, TemplateDomain>();
        configurations = new HashMap<String, TemplateConfig>();

        try {
            registry = ExecutionManagerValueHolder.getRegistryService().getConfigSystemRegistry();
        } catch (RegistryException e) {
            log.error("Registry exception occurred when getting system registry of service ", e);
        }

        //Load domains
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                try {
                    JAXBContext jaxbContext = JAXBContext.newInstance(TemplateDomain.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    TemplateDomain templateDomain = (TemplateDomain) jaxbUnmarshaller.unmarshal(fileEntry);
                    //Load configurations of each domain
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
                                    jaxbContext = JAXBContext.newInstance(TemplateConfig.class);
                                    jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                                    TemplateConfig templateConfig =
                                            (TemplateConfig) jaxbUnmarshaller.unmarshal(reader);
                                    configurations.put(templateDomain.getName()
                                            + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR
                                            + templateConfig.getName(), templateConfig);

                                }
                            }
                        } catch (RegistryException e) {
                            log.error("Registry exception occurred when accessing file at " + registryPath, e);
                        }
                    }
                    domains.put(templateDomain.getName(), templateDomain);
                } catch (JAXBException e) {
                    log.error("JAXB Exception when unmarshalling file at " + fileEntry.getPath());
                }

            }
        }
    }

    @Override
    public void saveTemplateConfig(TemplateConfig configuration) {

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
            registry.put(resourcePath, resource);

            configurations.put(configFullName, configuration);

        } catch (RegistryException e) {
            log.error("Registry exception occurred when writing " + configuration.getName() + " configurations", e);
        } catch (JAXBException e) {
            log.error("JAXB Exception when marshalling file at " + configuration.getName() + " configurations", e);
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
        TemplateConfig[] configAll;
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
    public void deleteConfig(String domainName, String configName) {
        try {

            String configFullName = domainName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + configName;

            registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + ExecutionManagerConstants.PATH_SEPARATOR
                    + configFullName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
            configurations.remove(configFullName);

        } catch (Exception e) {
            log.error("Exception when deleting file at " + configName + " configurations", e);
        }
    }

}