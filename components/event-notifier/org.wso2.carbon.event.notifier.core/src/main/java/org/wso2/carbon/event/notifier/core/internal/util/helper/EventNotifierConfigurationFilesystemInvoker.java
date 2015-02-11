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
package org.wso2.carbon.event.notifier.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.notifier.core.EventNotifierDeployer;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;

import java.io.*;

/**
 * This class used to do the file system related tasks
 */
public class EventNotifierConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(EventNotifierConfigurationFilesystemInvoker.class);

    public static void save(OMElement eventFormatterOMElement,
                            String fileName,
                            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {

        EventNotifierConfigurationFilesystemInvoker.save(eventFormatterOMElement.toString(), fileName, axisConfiguration);
    }

    public static void save(String eventFormatter,
                            String fileName, AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        EventNotifierDeployer eventNotifierDeployer = (EventNotifierDeployer) getDeployer(axisConfiguration, EventNotifierConstants.TM_ELE_DIRECTORY);
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = new XmlFormatter().format(eventFormatter);
            eventNotifierDeployer.getDeployedEventNotifierFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Event Formatter configuration saved in the filesystem : " + new File(filePath).getName());
            eventNotifierDeployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            eventNotifierDeployer.getDeployedEventNotifierFilePaths().remove(filePath);
            log.error("Could not save Event Formatter configuration : " + fileName, e);
            throw new EventNotifierConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String fileName,
                              AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                EventNotifierDeployer deployer = (EventNotifierDeployer) getDeployer(axisConfiguration, EventNotifierConstants.TM_ELE_DIRECTORY);
                deployer.getUndeployedEventNotifierFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Event Formatter configuration : " + fileName);
                    deployer.getUndeployedEventNotifierFilePaths().remove(filePath);
                } else {
                    log.info("Event Formatter configuration deleted from the file system : " + fileName);
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventNotifierConfigurationException("Error while deleting the Event Formatter : " + e.getMessage(), e);
        }
    }

    public static boolean isEventFormatterConfigurationFileExists(String fileName,
                                                                  AxisConfiguration axisConfiguration) {
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        File file = new File(filePath);
        return file.exists();
    }

    public static void reload(String filePath, AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        EventNotifierDeployer deployer = (EventNotifierDeployer) getDeployer(axisConfiguration, EventNotifierConstants.TM_ELE_DIRECTORY);
        try {
            deployer.processUndeploy(filePath);
            deployer.processDeploy(new DeploymentFileData(new File(filePath)));
        } catch (DeploymentException e) {
            throw new EventNotifierConfigurationException(e);
        }

    }

    public static String readEventFormatterConfigurationFile(String fileName,
                                                             AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventNotifierConfigurationException("Event notifier file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventNotifierConfigurationException("Cannot read the Event Formatter file : " + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }
        return stringBuilder.toString().trim();
    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    private static String getFilePathFromFilename(String filename,
                                                  AxisConfiguration axisConfiguration) {
        return new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + EventNotifierConstants.TM_ELE_DIRECTORY + File.separator + filename;
    }

}
