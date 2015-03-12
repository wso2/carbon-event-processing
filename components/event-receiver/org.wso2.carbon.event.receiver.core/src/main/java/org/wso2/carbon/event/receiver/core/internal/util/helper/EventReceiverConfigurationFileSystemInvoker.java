/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.receiver.core.EventReceiverDeployer;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.util.XmlFormatter;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.*;

public class EventReceiverConfigurationFileSystemInvoker {
    private static final Log log = LogFactory.getLog(EventReceiverConfigurationFileSystemInvoker.class);

    public static void save(OMElement eventReceiverConfigOMElement, String fileName)
            throws EventReceiverConfigurationException {
        saveAndDeploy(eventReceiverConfigOMElement.toString(), fileName);
    }

    public static void saveAndDeploy(String eventReceiverConfigXml, String fileName)
            throws EventReceiverConfigurationException {
        EventReceiverDeployer eventReceiverDeployer = EventReceiverConfigurationHelper.getEventReceiverDeployer(EventAdapterUtil.getAxisConfiguration());
        String filePath = getFilePathFromFilename(fileName);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = XmlFormatter.format(eventReceiverConfigXml);
            eventReceiverDeployer.getDeployedEventReceiverFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Event receiver configuration saved to the filesystem :" + fileName);
            DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
            eventReceiverDeployer.executeManualDeployment(deploymentFileData);
        } catch (IOException e) {
            eventReceiverDeployer.getDeployedEventReceiverFilePaths().remove(filePath);
            log.error("Error while saving event receiver configuration: " + fileName, e);
        }
    }

    public static void delete(String fileName)
            throws EventReceiverConfigurationException {

        try {
            String filePath = getFilePathFromFilename(fileName);
            File file = new File(filePath);
            String filename = file.getName();
            if (file.exists()) {
                EventReceiverDeployer eventReceiverDeployer = EventReceiverConfigurationHelper.getEventReceiverDeployer(EventAdapterUtil.getAxisConfiguration());
                eventReceiverDeployer.getUndeployedEventReceiverFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + filename);
                    eventReceiverDeployer.getUndeployedEventReceiverFilePaths().remove(filePath);
                } else {
                    log.info("Event receiver configuration deleted from the file system :" + filename);
                    eventReceiverDeployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventReceiverConfigurationException("Error while deleting the event receiver :" + e.getMessage(), e);
        }
    }

    public static boolean isFileExists(String fileName) {
        String filePath = getFilePathFromFilename(fileName);
        File file = new File(filePath);
        return file.exists();
    }

    public static String readEventReceiverConfigurationFile(String fileName)
            throws EventReceiverConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(getFilePathFromFilename(fileName)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventReceiverConfigurationException("Event receiver file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventReceiverConfigurationException("Cannot read the event receiver file : " + e.getMessage(), e);
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

    public static void reload(EventReceiverConfigurationFile eventReceiverConfigurationFile)
            throws EventReceiverConfigurationException {
        String filePath = eventReceiverConfigurationFile.getFilePath();
        AxisConfiguration axisConfiguration = EventAdapterUtil.getAxisConfiguration();
        EventReceiverDeployer deployer = EventReceiverConfigurationHelper.getEventReceiverDeployer(axisConfiguration);
        try {
            deployer.processUndeployment(filePath);
            deployer.processDeployment(new DeploymentFileData(new File(filePath)));
        } catch (DeploymentException e) {
            throw new EventReceiverConfigurationException(e);
        }
    }

    /**
     * Returns the full path. IMPORTANT: This method uses {@link MultitenantUtils} to get the Axis2RepositoryPath and might
     * give incorrect values if the axis2 repository path has been modified at startup (i.e. for samples)
     *
     * @param filename filename of the config file
     * @return the full path
     */
    private static String getFilePathFromFilename(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repositoryPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        return new File(repositoryPath).getAbsolutePath() + File.separator + EventReceiverConstants.ER_CONFIG_DIRECTORY + File.separator + filename;
    }
}
