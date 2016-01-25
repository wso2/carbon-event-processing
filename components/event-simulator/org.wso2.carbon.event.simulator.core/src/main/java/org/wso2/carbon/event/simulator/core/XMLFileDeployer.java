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
import org.wso2.carbon.event.simulator.core.internal.CarbonEventSimulator;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;
import org.wso2.carbon.event.simulator.core.internal.util.DeploymentHelper;

import java.io.File;

public class XMLFileDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(XMLFileDeployer.class);
    private ConfigurationContext configurationContext;

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String path = deploymentFileData.getAbsolutePath();
        try {
            if (deploymentFileData.getName().contains(EventSimulatorConstant.CONFIGURATION_XML_SUFFIX)) {
                processDeployStreamConfig(deploymentFileData);
            } else if (deploymentFileData.getName().contains(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX)) {
                processDeployDatasourceConfig(deploymentFileData);
            } else {
                log.warn("XML file : " + deploymentFileData.getName() + " not deployed. Name should contain either "
                        + EventSimulatorConstant.CONFIGURATION_XML_SUFFIX + " or " + EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX);
            }
        } catch (Throwable t) {
            File file = new File(path);
            log.error("Could not deploy XML file : " + file.getName(), t);
            throw new DeploymentException("XML file not deployed and in inactive state :  " + new File(path).getName(), t);
        }
    }

    public void undeploy(String filePath) throws DeploymentException {
        try {
            File file = new File(filePath);
            if (file.getName().contains(EventSimulatorConstant.CONFIGURATION_XML_SUFFIX)) {
                processUndeployStreamConfig(file);
            } else if (file.getName().contains(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX)) {
                processUndeployDatasourceConfig(file);
            } //else, no need to bother since it has never being deployed.
        } catch (Throwable t) {
            File file = new File(filePath);
            log.error("Could not deploy XML file : " + file.getName(), t);
            throw new DeploymentException("XML file could not be undeployed :  " + file.getName(), t);
        }
    }

    @Override
    public void setDirectory(String s) {

    }

    @Override
    public void setExtension(String s) {

    }

    /*
    Deploying datasource configuration
     */
    public void processDeployDatasourceConfig(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        File xmlFile = deploymentFileData.getFile();
        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = DeploymentHelper.getEventMappingConfiguration(xmlFile);
        dataSourceTableAndStreamInfo.setFileName(xmlFile.getName());
        dataSourceTableAndStreamInfo.setFilePath(xmlFile.getAbsolutePath());

        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        if (dataSourceTableAndStreamInfo != null) {
            eventSimulator.addDataSourceTableAndStreamInfo(dataSourceTableAndStreamInfo);
        }
    }

    /*
    Undeploying datasource configuration
     */
    public void processUndeployDatasourceConfig(File datasourceConfigFile) {
        String datasourceConfigFileName = datasourceConfigFile.getName().replace(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX, "");
        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        eventSimulator.removeDataSourceTableAndStreamInfo(datasourceConfigFileName);
    }

    /*
    Deploying CSV configuration
     */
    public void processDeployStreamConfig(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        CSVFileInfo csvFileInfo = DeploymentHelper.getCSVFileInfo(deploymentFileData.getFile(), configurationContext.getAxisConfiguration());
        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        eventSimulator.addCSVFileInfo(csvFileInfo);
    }

    /*
    Undeploying CSV configuration
     */
    public void processUndeployStreamConfig(File csvFile) {
        String csvFileName = csvFile.getName().replace(EventSimulatorConstant.CONFIGURATION_XML_SUFFIX, EventSimulatorConstant.CSV_EXTENSION);
        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        eventSimulator.removeCSVFileInfo(csvFileName);
    }
}
