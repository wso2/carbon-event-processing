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
package org.wso2.carbon.event.execution.manager.core.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.Parameter;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Class consist of the helper methods which are required to deal with domain templates stored in the file directory,
 * configurations stored as resources in the registry, deploy execution plans and deploy streams
 */
public class ExecutionManagerHelper {

    private static final Log log = LogFactory.getLog(ExecutionManagerHelper.class);

    /**
     * To avoid instantiating
     */
    private ExecutionManagerHelper() {
    }

    /**
     * Load All domains templates available in the file directory
     */
    public static Map<String, TemplateDomain> loadDomains() {
        //Get domain template folder and load all the domain template files
        File folder = new File(ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH);
        Map<String, TemplateDomain> domains = new HashMap<>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isFile()) {
                    TemplateDomain templateDomain = unmarshalDomain(fileEntry);
                    domains.put(templateDomain.getName(), templateDomain);
                }
            }
        }

        return domains;
    }

    /**
     * Unmarshalling TemplateDomain object by given file
     *
     * @param fileEntry file for unmarshalling
     * @return templateDomain object
     */
    private static TemplateDomain unmarshalDomain(File fileEntry) {
        TemplateDomain templateDomain = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateDomain.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            templateDomain = (TemplateDomain) jaxbUnmarshaller.unmarshal(fileEntry);

        } catch (JAXBException e) {
            log.error("JAXB Exception when unmarshalling domain template file at "
                    + fileEntry.getPath(), e);
        }

        return templateDomain;

    }

    /**
     * Provide template configurations available in the given registry and given path
     *
     * @param path     where configurations are stored
     * @return available configurations
     */
    public static TemplateConfiguration getConfiguration(String path) {


        TemplateConfiguration templateConfiguration = null;
        try {
            Registry registry = ExecutionManagerValueHolder.getRegistryService().getConfigSystemRegistry(PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getTenantId());

            if (registry.resourceExists(path)) {
                Resource configFile = registry.get(path);
                if (configFile != null) {
                    templateConfiguration = unmarshalConfiguration(configFile.getContent());
                }
            }
        } catch (RegistryException e) {
            log.error("Registry exception occurred when accessing files at "
                    + ExecutionManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }

        return templateConfiguration;
    }

    /**
     * Unmarshalling TemplateDomain object by given file content object
     *
     * @param configFileContent file for unmarshalling
     * @return templateConfiguration object
     */
    private static TemplateConfiguration unmarshalConfiguration(Object configFileContent) {
        TemplateConfiguration templateConfiguration = null;
        try {

            StringReader reader = new StringReader(new String((byte[]) configFileContent));
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            templateConfiguration = (TemplateConfiguration) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("JAXB Exception occurred when unmarshalling configuration ", e);
        }

        return templateConfiguration;
    }

    /**
     * Deploy Streams of given template domain
     *
     * @param templateDomain template domain object
     */
    public static void deployStreams(TemplateDomain templateDomain) {
        for (String stream : templateDomain.getStreams()) {
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = EventDefinitionConverterUtils.convertFromJson(stream);
                ExecutionManagerValueHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
            } catch (MalformedStreamDefinitionException e) {
                log.error("Stream definition is incorrect in domain template " + templateDomain.getName(), e);
            } catch (EventStreamConfigurationException e) {
                log.error("Exception occurred when configuring stream " + streamDefinition.getName(), e);
            } catch (StreamDefinitionAlreadyDefinedException e) {
                log.error("Same template stream name "+ streamDefinition.getName()
                        + " has been defined for another definition ", e);
                throw e;
            }
        }
    }

    /**
     * Deploy given configurations template Execution Plans
     *
     * @param configuration configuration object
     */
    public static void deployExecutionPlans(TemplateConfiguration configuration, Map<String, TemplateDomain> domains)
            throws ExecutionManagerException {

        if (domains.get(configuration.getFrom()) == null) {
            throw new ExecutionManagerException("The " + configuration.getFrom() + " domain of"
                    + configuration.getName() + " configuration" + " is not available in the domain list.");
        } else if (domains.get(configuration.getFrom()).getTemplates() == null) {
            throw new ExecutionManagerException("There are no templates in the domain " + configuration.getFrom()
                    + " of " + configuration.getName() + " configuration");
        } else {
            for (Template template : domains.get(configuration.getFrom()).getTemplates()) {
                if (template.getName().equals(configuration.getType())
                        && deployExecutionPlan(configuration, template.getExecutionPlan())) {
                    break;
                }
            }
        }
    }

    private static boolean deployExecutionPlan(TemplateConfiguration configuration, String templateExecutionPlan)
            throws ExecutionManagerException {
        try {
            String executionPlan = ExecutionManagerHelper.updateExecutionPlanParameters(configuration,
                    templateExecutionPlan);

            ExecutionManagerHelper.unDeployExistingExecutionPlan(AnnotationHelper.getAnnotationElement(
                    EventProcessorConstants.ANNOTATION_NAME_NAME, null,
                    SiddhiCompiler.parse(executionPlan).getAnnotations()).getValue());

            //Get Template Execution plan, Tenant Id and deploy Execution Plan
            ExecutionManagerValueHolder.getEventProcessorService()
                    .deployExecutionPlan(executionPlan);
            return true;
        } catch (ExecutionPlanConfigurationException e) {
            throw new ExecutionManagerException(
                    "Configuration exception occurred when adding Execution Plan of Template "
                            + configuration.getName() + " of Domain " + configuration.getFrom(), e);

        } catch (ExecutionPlanDependencyValidationException e) {
            throw new ExecutionManagerException(
                    "Validation exception occurred when adding Execution Plan of Template "
                            + configuration.getName() + " of Domain " + configuration.getFrom(), e);
        } catch (SiddhiParserException e) {
            throw new ExecutionManagerException(
                    "Validation exception occurred when parsing Execution Plan of Template "
                            + configuration.getName() + " of Domain " + configuration.getFrom(), e);
        }
    }


    /**
     * Update given execution plan by replacing undefined parameter values with configured parameter values
     *
     * @param config        configurations which consists of parameters which will replace
     * @param executionPlan execution plan which needs to be updated
     * @return updated execution plan
     */
    private static String updateExecutionPlanParameters(TemplateConfiguration config, String executionPlan) {

        String updatedExecutionPlan = executionPlan;
        String executionPlanNameDefinition = ExecutionManagerConstants.EXECUTION_PLAN_NAME_ANNOTATION + "('"
                + config.getFrom() + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + config.getName() + "')";

        //Execution plan parameters will be replaced with given configuration parameters
        for (Parameter parameter : config.getParameters()) {
            updatedExecutionPlan = updatedExecutionPlan.replaceAll(ExecutionManagerConstants.REGEX_NAME_VALUE
                    + parameter.getName(), parameter.getValue());
        }

        if (AnnotationHelper.getAnnotationElement(
                EventProcessorConstants.ANNOTATION_NAME_NAME, null,
                SiddhiCompiler.parse(updatedExecutionPlan).getAnnotations()) == null
                || !updatedExecutionPlan.contains(ExecutionManagerConstants.EXECUTION_PLAN_NAME_ANNOTATION)) {
            updatedExecutionPlan = executionPlanNameDefinition + updatedExecutionPlan;
        } else {
            //@Plan:name will be updated with given configuration name and uncomment in case if it is commented
            updatedExecutionPlan = updatedExecutionPlan.replaceAll(
                    ExecutionManagerConstants.EXECUTION_PLAN_NAME_ANNOTATION
                            + ExecutionManagerConstants.REGEX_NAME_COMMENTED_VALUE,
                    executionPlanNameDefinition);
        }

        return updatedExecutionPlan;
    }

    /**
     * Check weather given execution plan is already exists and un deploy it
     *
     * @param executionPlanName name of the execution plan
     * @throws ExecutionPlanConfigurationException
     */
    public static void unDeployExistingExecutionPlan(String executionPlanName)
            throws ExecutionPlanConfigurationException {
        if (ExecutionManagerValueHolder.getEventProcessorService()
                .getAllActiveExecutionConfigurations().get(executionPlanName) != null) {
            ExecutionManagerValueHolder.getEventProcessorService().undeployActiveExecutionPlan(executionPlanName);
        }
    }


}
