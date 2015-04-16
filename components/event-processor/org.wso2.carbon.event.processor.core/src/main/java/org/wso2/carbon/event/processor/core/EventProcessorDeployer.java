/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.application.deployer.EventProcessingDeployer;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.exception.ServiceDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorHelper;

import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy query plans as axis2 service
 */
@SuppressWarnings("unused")
public class EventProcessorDeployer extends AbstractDeployer implements EventProcessingDeployer {

    private static Log log = LogFactory.getLog(org.wso2.carbon.event.processor.core.EventProcessorDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedExecutionPlanFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> unDeployedExecutionPlanFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Reads the query-plan.siddhiql and deploys it.
     *
     * @param deploymentFileData information about query plan
     * @throws org.apache.axis2.deployment.DeploymentException
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {
            String path = deploymentFileData.getAbsolutePath();

            if (!deployedExecutionPlanFilePaths.contains(path)) {
                try {
                    processDeploy(deploymentFileData);
                } catch (ExecutionPlanConfigurationException e) {
                    throw new DeploymentException("Execution plan not deployed properly.", e);
                }
            } else {
                log.debug("Execution plan file is already deployed :" + path);
                deployedExecutionPlanFilePaths.remove(path);
            }
        } catch (Throwable t) {
            log.error("Can't deploy the execution plan: " + deploymentFileData.getName(), t);
            throw new DeploymentException("Can't deploy the execution plan: " + deploymentFileData.getName(), t);
        }
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed bucket
     *
     * @param filePath the path to the bucket to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     */
    public void undeploy(String filePath) throws DeploymentException {
        try {
            if (!unDeployedExecutionPlanFilePaths.contains(filePath)) {
                processUndeploy(filePath);
            } else {
                log.debug("Execution plan file is already undeployed :" + filePath);
                unDeployedExecutionPlanFilePaths.remove(filePath);
            }
        } catch (Throwable t) {
            log.error("Can't undeploy the execution plan: " + filePath, t);
            throw new DeploymentException("Can't undeploy the execution plan: " + filePath, t);
        }

    }

    public synchronized void processDeploy(DeploymentFileData deploymentFileData)
            throws ExecutionPlanConfigurationException {
        // can't be null at this point
        CarbonEventProcessorService carbonEventProcessorService = EventProcessorValueHolder.getEventProcessorService();

        File executionPlanFile = deploymentFileData.getFile();
        boolean isEditable = !executionPlanFile.getAbsolutePath().contains(File.separator + "carbonapps" + File.separator);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ExecutionPlanConfigurationFile executionPlanConfigurationFile = new ExecutionPlanConfigurationFile();
        if (!carbonEventProcessorService.isExecutionPlanFileAlreadyExist(executionPlanFile.getName(), tenantId)) {
            String executionPlanName = "";
            try {
                String executionPlan = readFile(deploymentFileData.getAbsolutePath());
                EventProcessorHelper.validateExecutionPlan(executionPlan, tenantId);

                executionPlanName = EventProcessorHelper.getExecutionPlanName(executionPlan);
                carbonEventProcessorService.addExecutionPlan(executionPlan, isEditable);
                executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.DEPLOYED);
                executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
                executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
                executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
                executionPlanConfigurationFile.setFilePath(deploymentFileData.getAbsolutePath());
                carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

                log.info("Execution plan is deployed successfully and in active state  : " + executionPlanName);

            } catch (ServiceDependencyValidationException ex) {
                executionPlanConfigurationFile.setDependency(ex.getDependency());
                executionPlanConfigurationFile.setDeploymentStatusMessage(ex.getMessage());
                executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_OSGI_SERVICE);
                executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
                executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
                executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
                executionPlanConfigurationFile.setFilePath(deploymentFileData.getAbsolutePath());
                carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

                log.info("Execution plan deployment held back and in inactive state : " + executionPlanName + ", waiting for dependency : " + ex.getDependency());

            } catch (ExecutionPlanDependencyValidationException ex) {
                executionPlanConfigurationFile.setDependency(ex.getDependency());
                executionPlanConfigurationFile.setDeploymentStatusMessage(ex.getMessage());
                executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.WAITING_FOR_DEPENDENCY);
                executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
                executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
                executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
                executionPlanConfigurationFile.setFilePath(deploymentFileData.getAbsolutePath());
                carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

                log.info("Execution plan deployment held back and in inactive state : " + executionPlanName + ", waiting for dependency : " + ex.getDependency());

            } catch (ExecutionPlanConfigurationException ex) {
                executionPlanConfigurationFile.setDeploymentStatusMessage(ex.getMessage());
                executionPlanConfigurationFile.setStatus(ExecutionPlanConfigurationFile.Status.ERROR);
                executionPlanConfigurationFile.setExecutionPlanName(executionPlanName);
                executionPlanConfigurationFile.setAxisConfiguration(configurationContext.getAxisConfiguration());
                executionPlanConfigurationFile.setFileName(deploymentFileData.getName());
                executionPlanConfigurationFile.setFilePath(deploymentFileData.getAbsolutePath());
                carbonEventProcessorService.addExecutionPlanConfigurationFile(executionPlanConfigurationFile, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

                log.error("Execution plan is not deployed and in inactive state : " + executionPlanFile.getName(), ex);
                throw new ExecutionPlanConfigurationException(ex.getMessage(), ex);
            }
        } else {
            log.info("Execution plan " + executionPlanFile.getName() + " is already registered with this tenant (" + tenantId + "), hence ignoring redeployment");
        }

    }

    public synchronized void processUndeploy(String filePath) {

        String fileName = new File(filePath).getName();
        log.info("Execution Plan was undeployed successfully : " + fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventProcessorService carbonEventProcessorService = EventProcessorValueHolder.getEventProcessorService();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        carbonEventProcessorService.removeExecutionPlanConfigurationFile(fileName, tenantId);
    }

    public void setDirectory(String directory) {

    }

    public void executeManualDeployment(String filePath) throws DeploymentException, ExecutionPlanConfigurationException {
        processDeploy(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) {
        processUndeploy(filePath);
    }

    private String readFile(String path) throws ExecutionPlanConfigurationException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new ExecutionPlanConfigurationException("File '" + path + "' not found." + e.getMessage(), e);
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException("Could not read from file " + path + ", " + e.getMessage(), e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                throw new ExecutionPlanConfigurationException("Could not close the file " + path + ", " + e.getMessage(), e);
            }
        }
    }


    public Set<String> getDeployedExecutionPlanFilePaths() {
        return deployedExecutionPlanFilePaths;
    }

    public Set<String> getUnDeployedExecutionPlanFilePaths() {
        return unDeployedExecutionPlanFilePaths;
    }

    @Override
    public void processDeployment(DeploymentFileData deploymentFileData) throws Exception {
        processDeploy(deploymentFileData);
    }

    @Override
    public void processUndeployment(String filePath) throws Exception {
        processUndeploy(filePath);
    }
}