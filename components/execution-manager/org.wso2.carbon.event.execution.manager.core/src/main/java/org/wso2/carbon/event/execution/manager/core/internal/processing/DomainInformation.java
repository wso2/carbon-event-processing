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
package org.wso2.carbon.event.execution.manager.core.internal.processing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.execution.manager.core.dto.ConfigDTO;
import org.wso2.carbon.event.execution.manager.core.dto.DomainConfigInfoDTO;
import org.wso2.carbon.event.execution.manager.core.dto.ParameterDTO;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.internal.structure.config.Parameter;
import org.wso2.carbon.event.execution.manager.core.internal.structure.config.TemplateConfig;
import org.wso2.carbon.event.execution.manager.core.internal.structure.domain.TemplateDomain;
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

public class DomainInformation {
    private static final Log log = LogFactory.getLog(DomainInformation.class);

    private HashMap<String, DomainConfigInfoDTO> domainInfoList;
    private HashMap<String, TemplateDomain> domains;
    private HashMap<String, TemplateConfig> configurations;
    private Registry registry;

    public DomainInformation() {
        //get template domain file path
        String filePath = ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH;
        File folder = new File(filePath);
        domainInfoList = new HashMap<String, DomainConfigInfoDTO>();
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
                    DomainConfigInfoDTO domainConfigInfoDTO = new DomainConfigInfoDTO();
                    List<ConfigDTO> configDTOs = new ArrayList<ConfigDTO>();

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

                                    //Map Unmarshalled configuration to configuration DTO and add to list
                                    configDTOs.add(this.mapTemplateConfig(templateConfig));
                                }
                            }
                        } catch (RegistryException e) {
                            log.error("Registry exception occurred when accessing file at " + registryPath, e);
                        }
                    }

                    domainConfigInfoDTO.setName(templateDomain.getName());
                    domainConfigInfoDTO.setDescription(templateDomain.getName());
                    domainConfigInfoDTO.setStreams(templateDomain.getStreams());

                    //Convert array list to array of ConfigDTO
                    ConfigDTO[] allDomainInfo = new ConfigDTO[configDTOs.size()];
                    allDomainInfo = configDTOs.toArray(allDomainInfo);
                    domainConfigInfoDTO.setConfigurations(allDomainInfo);

                    domains.put(templateDomain.getName(), templateDomain);
                    domainInfoList.put(domainConfigInfoDTO.getName(), domainConfigInfoDTO);
                } catch (JAXBException e) {
                    log.error("JAXB Exception when unmarshalling file at " + fileEntry.getPath());
                }

            }
        }
    }

    /**
     * save template configuration xml files in wso2 carbon registry
     *
     * @param configuration configuration object which needs to be saved
     */
    public void saveTemplateConfig(String domainName, ConfigDTO configuration) {

        try {
            StringWriter fileContent = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateConfig.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, ExecutionManagerConstants.DEFAULT_CHARSET);

            TemplateConfig configTemplate = this.mapConfigDTO(domainName, configuration);

            jaxbMarshaller.marshal(configTemplate, fileContent);
            Resource resource = registry.newResource();
            resource.setContent(fileContent.toString());
            resource.setProperty("name", configuration.getName());
            resource.setProperty("description", configuration.getDescription());
            resource.setProperty("type", "");
            String resourcePath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                    + ExecutionManagerConstants.PATH_SEPARATOR + configuration.getName()
                    + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
            registry.put(resourcePath, resource);

            configurations.put(configuration.getName(), configTemplate);
            DomainConfigInfoDTO domainConfigInfoDTO = domainInfoList.get(domainName);
            domainConfigInfoDTO = this.updateConfig(domainConfigInfoDTO, configuration);
            domainInfoList.put(domainName, domainConfigInfoDTO);


        } catch (RegistryException e) {
            log.error("Registry exception occurred when writing " + configuration.getName() + " configurations", e);
        } catch (JAXBException e) {
            log.error("JAXB Exception when marshalling file at " + configuration.getName() + " configurations", e);
        }
    }

    /**
     * read all TemplateDomains and returns the name and its description as an array of the domain objects
     *
     * @return DomainInfoDTO object array
     */
    public DomainConfigInfoDTO[] getAllDomainInfo() {
        Iterator it = domainInfoList.entrySet().iterator();
        List<DomainConfigInfoDTO> configurationList = new ArrayList<DomainConfigInfoDTO>();
        DomainConfigInfoDTO[] configurationArray;

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {
                configurationList.add((DomainConfigInfoDTO) pair.getValue());
            }
        }

        configurationArray = new DomainConfigInfoDTO[configurationList.size()];
        configurationArray = configurationList.toArray(configurationArray);
        return configurationArray;
    }


    /**
     * get information of a specific domain
     *
     * @param domainName template domain name
     * @return content of the TemplateDomain file
     */
    public DomainConfigInfoDTO getSpecificDomainInfo(String domainName) {
        return domainInfoList.get(domainName);
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
            log.error("Exception when deleting file at " + configName + " configurations", e);
        }
    }

    /**
     * Map Configuration DTO to Template Configuration for unmarshalling and marshalling by JAXB
     *
     * @param domainName    Name of the domain which configuration belongs
     * @param configuration configuration DTO which needs to be mapped
     * @return mapped Template Configuration object
     */
    private TemplateConfig mapConfigDTO(String domainName, ConfigDTO configuration) {

        TemplateConfig templateConfig = new TemplateConfig();

        templateConfig.setName(configuration.getName());
        templateConfig.setDescription(configuration.getDescription());
        templateConfig.setFrom(domainName);

        Parameter[] parameters = new Parameter[configuration.getParameters().length];
        for (int i = 0; i < configuration.getParameters().length; i++) {
            parameters[i] = new Parameter();
            parameters[i].setName(configuration.getParameters()[i].getName());
            parameters[i].setValue(configuration.getParameters()[i].getValue());
        }
        templateConfig.setParameters(parameters);

        return templateConfig;
    }

    /**
     * Map Template Configuration to Configuration DTO
     *
     * @param configuration template configuration object needs to be mapped
     * @returnmapped Configuration DTO
     */
    private ConfigDTO mapTemplateConfig(TemplateConfig configuration) {

        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setName(configuration.getName());
        configDTO.setDescription(configuration.getDescription());

        ParameterDTO[] parameters = new ParameterDTO[configuration.getParameters().length];
        for (int i = 0; i < configuration.getParameters().length; i++) {
            parameters[i] = new ParameterDTO();
            parameters[i].setName(configuration.getParameters()[i].getName());
            parameters[i].setValue(configuration.getParameters()[i].getValue());
        }
        configDTO.setParameters(parameters);

        return configDTO;
    }

    /**
     * Update template domain by updating given configuration
     *
     * @param domainInfo     domain object which needs to updated
     * @param configTemplate updated configuration
     * @return updated template domain object
     */
    private DomainConfigInfoDTO updateConfig(DomainConfigInfoDTO domainInfo, ConfigDTO configTemplate) {

        ConfigDTO[] configs = domainInfo.getConfigurations();

        for (int i = 0; i < configs.length; i++) {
            if (configs[i].getName().equals(configTemplate.getName())) {
                configs[i] = configTemplate;
            }
        }

        return domainInfo;
    }
}