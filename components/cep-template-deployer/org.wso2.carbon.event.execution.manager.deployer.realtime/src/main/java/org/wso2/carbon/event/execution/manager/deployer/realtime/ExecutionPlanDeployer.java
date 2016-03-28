/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.execution.manager.deployer.realtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.deployer.realtime.internal.ExecutionPlanDeployerConstants;
import org.wso2.carbon.event.execution.manager.deployer.realtime.internal.ExecutionPlanDeployerValueHolder;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class ExecutionPlanDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(ExecutionPlanDeployer.class);

    @Override
    public String getType() {
        return "realtime";
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {

        try {

            String artifactId = template.getConfiguration().getFrom()
                    + ExecutionPlanDeployerConstants.CONFIG_NAME_SEPARATOR + template.getConfiguration().getName();

            undeployArtifact(artifactId);

            deployStreams(template);

            // configuring plan name etc
            String updatedExecutionPlan = template.getScript();
            String executionPlanNameDefinition = ExecutionPlanDeployerConstants.EXECUTION_PLAN_NAME_ANNOTATION + "('"
                    + template.getConfiguration().getFrom() + ExecutionPlanDeployerConstants.CONFIG_NAME_SEPARATOR + template.getConfiguration().getName() + "')";

            if (AnnotationHelper.getAnnotationElement(
                    EventProcessorConstants.ANNOTATION_NAME_NAME, null,
                    SiddhiCompiler.parse(updatedExecutionPlan).getAnnotations()) == null
                    || !updatedExecutionPlan.contains(ExecutionPlanDeployerConstants.EXECUTION_PLAN_NAME_ANNOTATION)) {
                updatedExecutionPlan = executionPlanNameDefinition + updatedExecutionPlan;
            } else {
                //@Plan:name will be updated with given configuration name and uncomment in case if it is commented
                updatedExecutionPlan = updatedExecutionPlan.replaceAll(
                        ExecutionPlanDeployerConstants.EXECUTION_PLAN_NAME_ANNOTATION
                                + ExecutionPlanDeployerConstants.REGEX_NAME_COMMENTED_VALUE,
                        executionPlanNameDefinition);
            }


            //Get Template Execution plan, Tenant Id and deploy Execution Plan
            ExecutionPlanDeployerValueHolder.getEventProcessorService()
                    .deployExecutionPlan(updatedExecutionPlan);

        } catch (ExecutionPlanConfigurationException e) {
            throw new TemplateDeploymentException(
                    "Configuration exception occurred when adding Execution Plan of Template "
                            + template.getConfiguration().getName() + " of Domain " + template.getConfiguration().getFrom(), e);

        } catch (ExecutionPlanDependencyValidationException e) {
            throw new TemplateDeploymentException(
                    "Validation exception occurred when adding Execution Plan of Template "
                            + template.getConfiguration().getName() + " of Domain " + template.getConfiguration().getFrom(), e);
        } catch (SiddhiParserException e) {
            throw new TemplateDeploymentException(
                    "Validation exception occurred when parsing Execution Plan of Template "
                            + template.getConfiguration().getName() + " of Domain " + template.getConfiguration().getFrom(), e);
        }
    }


    public static void deployStreams(DeployableTemplate template) {
        if (template.getStreams() != null) {
            for (String stream : template.getStreams()) {
                StreamDefinition streamDefinition = null;
                try {
                    streamDefinition = EventDefinitionConverterUtils.convertFromJson(stream);
                    ExecutionPlanDeployerValueHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
                } catch (MalformedStreamDefinitionException e) {
                    log.error("Stream definition is incorrect in domain template " + stream, e);
                } catch (EventStreamConfigurationException e) {
                    log.error("Exception occurred when configuring stream " + streamDefinition.getName(), e);
                } catch (StreamDefinitionAlreadyDefinedException e) {
                    log.error("Same template stream name " + streamDefinition.getName()
                            + " has been defined for another definition ", e);
                    throw e;
                }
            }
        }
    }

    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {

        if (ExecutionPlanDeployerValueHolder.getEventProcessorService()
                .getAllActiveExecutionConfigurations().get(artifactId) != null) {
            try {
                ExecutionPlanDeployerValueHolder.getEventProcessorService().undeployActiveExecutionPlan(artifactId);
            } catch (ExecutionPlanConfigurationException e) {
                throw new TemplateDeploymentException("Couldn't undeploy the template " + artifactId);
            }
        }
    }

}
