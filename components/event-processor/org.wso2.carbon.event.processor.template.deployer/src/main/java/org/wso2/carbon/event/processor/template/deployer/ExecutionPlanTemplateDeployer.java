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
package org.wso2.carbon.event.processor.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.template.deployer.internal.ExecutionPlanDeployerConstants;
import org.wso2.carbon.event.processor.template.deployer.internal.ExecutionPlanDeployerValueHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ExecutionPlanTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(ExecutionPlanTemplateDeployer.class);

    @Override
    public String getType() {
        return "realtime";
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String planName = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }
            planName = template.getArtifactId();

            // configuring plan name etc
            String updatedExecutionPlan = template.getArtifact();
            String executionPlanNameDefinition = ExecutionPlanDeployerConstants.EXECUTION_PLAN_NAME_ANNOTATION + "('"
                    + planName + "')";

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

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            saveExecutionPlan(planName, updatedExecutionPlan, tenantId);

        } catch (SiddhiParserException e) {
            throw new TemplateDeploymentException(
                    "Validation exception occurred when parsing Execution Plan of Template "
                            + template.getConfiguration().getName() + " of Domain " + template.getConfiguration().getDomain(), e);
        } catch (IOException e) {
            throw new TemplateDeploymentException("Could not save Execution Plan: " + planName + " for Tenant: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true), e);
        }
    }


    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template) throws TemplateDeploymentException{
        if (template == null) {
            throw new TemplateDeploymentException("No artifact received to be deployed.");
        }
        String planName = template.getArtifactId();
        if (ExecutionPlanDeployerValueHolder.getEventProcessorService().
                getAllActiveExecutionConfigurations().get(planName) == null) {
            deployArtifact(template);
        } else {
            if(log.isDebugEnabled()) {
                log.debug("Common Artifact: " + planName + " of Domain " + template.getConfiguration().getDomain()
                          + " was not deployed as it is already being deployed.");
            }
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        deleteExecutionPlan(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), artifactId);
    }

    private void saveExecutionPlan(String executionPlanName, String executionPlan, int tenantId)
            throws IOException {
        OutputStreamWriter writer = null;
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                          EventProcessorConstants.EP_ELE_DIRECTORY + File.separator + executionPlanName +
                          EventProcessorConstants.SIDDHIQL_EXTENSION;
        try {
            /* save contents to .xml file */
            File file = new File(filePath);

            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            // get the content in bytes
            writer.write(executionPlan);
            log.info("Execution plan: " + executionPlanName + " saved in the filesystem");
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    private void deleteExecutionPlan(int tenantId, String artifactId)
            throws TemplateDeploymentException {
        File executionPlanFile = new File(MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                                      EventProcessorConstants.EP_ELE_DIRECTORY + File.separator + artifactId +
                                      EventProcessorConstants.SIDDHIQL_EXTENSION);
        if (executionPlanFile.exists()) {
            if (!executionPlanFile.delete()) {
                throw new TemplateDeploymentException("Unable to successfully delete Execution Plan File : " + executionPlanFile.getName() + " from File System, for tenant id : "
                                                      + tenantId);
            }
        }
    }

}
