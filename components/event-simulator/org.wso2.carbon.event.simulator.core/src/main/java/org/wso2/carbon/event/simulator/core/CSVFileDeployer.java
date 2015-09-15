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
package org.wso2.carbon.event.simulator.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.simulator.core.internal.CarbonEventSimulator;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;
import org.wso2.carbon.event.simulator.core.internal.util.DeployerHelper;

import java.io.File;
import java.util.HashMap;

public class CSVFileDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(CSVFileDeployer.class);
    private ConfigurationContext configurationContext;

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String path = deploymentFileData.getAbsolutePath();
        try {
            processDeploy(deploymentFileData);
        } catch (Throwable t) {
            File file = new File(path);
            log.error("Could not deploy CSV file : " + file.getName(), t);
            throw new DeploymentException("CSV file not deployed and in inactive state :  " + file.getName(), t);
        }
    }

    @Override
    public void undeploy(String filePath) throws DeploymentException {
        try {
            processUndeploy(filePath);
        } catch (Throwable t) {
            File file = new File(filePath);
            log.error("Could not undeploy CSV file : " + file.getName(), t);
            throw new DeploymentException("CSV file could not be undeployed :  " + file.getName(), t);
        }
    }

    @Override
    public void setDirectory(String s) {
    }

    @Override
    public void setExtension(String s) {
    }

    public void processDeploy(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        CSVFileInfo csvFileInfo = null;
        File csvFile = deploymentFileData.getFile();
        String xmlName = csvFile.getName().substring(0, csvFile.getName().length() - 4) + EventSimulatorConstant.CONFIGURATION_XML_SUFFIX;
        String repo = configurationContext.getAxisConfiguration().getRepository().getPath();
        String dirPath = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;
        String absolutePath = dirPath + File.separator + xmlName;

        File xmlFile = new File(absolutePath);

        if (xmlFile.exists()) {
            csvFileInfo = DeployerHelper.getCSVFileInfo(xmlFile, configurationContext.getAxisConfiguration());
        }

        if (csvFileInfo == null) {
            csvFileInfo = new CSVFileInfo();
        }

        csvFileInfo.setFileName(csvFile.getName());
        csvFileInfo.setFilePath(csvFile.getAbsolutePath());

        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        eventSimulator.addCSVFileInfo(csvFileInfo);
        log.info("CSV file " + csvFile.getName() + " deployed successfully.");
    }

    public void processUndeploy(String filePath) throws DeploymentException {
        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        HashMap<String, CSVFileInfo> csvFileInfoMap = eventSimulator.getCSVFileInfoMap();

        File csvFile = new File(filePath);
        CSVFileInfo csvFileInfo = csvFileInfoMap.get(csvFile.getName());

        String repo = configurationContext.getAxisConfiguration().getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;

        String xmlFileName = csvFileInfo.getFileName().substring(0, csvFileInfo.getFileName().length() - 4) + EventSimulatorConstant.CONFIGURATION_XML_SUFFIX;
        String xmlFilePath = path + File.separator + xmlFileName;

        File xmlFile = new File(xmlFilePath);

        csvFileInfoMap.remove(csvFile.getName());

        if (xmlFile.exists()) {
            if (!xmlFile.delete()) {
                int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                throw new DeploymentException(xmlFileName + " could not be deleted for tenant ID : " + tenantID);
            }
        }
        log.info("CSV file " + csvFile.getName() + " undeployed successfully.");
    }
}
