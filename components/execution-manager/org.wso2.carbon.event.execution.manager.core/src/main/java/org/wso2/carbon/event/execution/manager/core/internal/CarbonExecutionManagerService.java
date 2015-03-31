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
                    for (int i = 0; i < templateDomain.getTemplates().length; i++) {

                        String registryPath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                                + ExecutionManagerConstants.PATH_SEPARATOR
                                + templateDomain.getTemplates()[i].getName()
                                + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
                        try {
                            if (registry.resourceExists(registryPath)) {
                                Resource configFile = registry.get(registryPath);

                                if (configFile != null) {
                                    StringReader reader = new StringReader(new String((byte[]) configFile.getContent()));
                                    jaxbContext = JAXBContext.newInstance(TemplateConfig.class);
                                    jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                                    TemplateConfig templateConfig =
                                            (TemplateConfig) jaxbUnmarshaller.unmarshal(reader);
                                    configurations.put(templateConfig.getName(), templateConfig);

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

            jaxbMarshaller.marshal(configuration, fileContent);
            Resource resource = registry.newResource();
            resource.setContent(fileContent.toString());
            resource.setProperty("name", configuration.getName());
            resource.setProperty("description", configuration.getDescription());
            resource.setProperty("type", "");
            String resourcePath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                    + ExecutionManagerConstants.PATH_SEPARATOR + configuration.getName()
                    + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
            registry.put(resourcePath, resource);

            configurations.put(configuration.getName(), configuration);
        } catch (RegistryException e) {
            log.error("Registry exception occurred when writing " + configuration.getName() + " configurations", e);
        } catch (JAXBException e) {
            log.error("JAXB Exception when marshalling file at " + configuration.getName() + " configurations", e);
        }
    }

    @Override
    public List<TemplateDomain> getAllDomains() {
        Iterator it = domains.entrySet().iterator();
        List<TemplateDomain> domainAll = new ArrayList<TemplateDomain>();

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {
                domainAll.add((TemplateDomain) pair.getValue());
            }
        }
        return domainAll;
    }

    @Override
    public List<TemplateConfig> getAllConfigurations() {
        Iterator it = configurations.entrySet().iterator();
        List<TemplateConfig> configAll = new ArrayList<TemplateConfig>();

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {
                configAll.add((TemplateConfig) pair.getValue());
            }
        }
        return configAll;
    }

    @Override
    public TemplateDomain getDomain(String domainName) {
        return domains.get(domainName);
    }


    @Override
    public TemplateConfig getConfiguration(String configName) {
        return configurations.get(configName);
    }

    @Override
    public void deleteConfig(String configName) {
        try {
            registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + ExecutionManagerConstants.PATH_SEPARATOR
                    + configName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (Exception e) {
            log.error("Exception when deleting file at " + configName + " configurations", e);
        }
    }

}