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

package org.wso2.carbon.event.input.adaptor.manager.core.internal.util.helper;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.event.input.adaptor.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorDeployer;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.ds.InputEventAdaptorManagerValueHolder;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.InputEventAdaptorManagerConstants;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class used to do the file system related tasks
 */
public final class InputEventAdaptorConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(InputEventAdaptorConfigurationFilesystemInvoker.class);

    private InputEventAdaptorConfigurationFilesystemInvoker() {
    }

    public static void encryptAndSave(OMElement eventAdaptorElement,
                                      String eventAdaptorName, String fileName,
                                      AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        String adaptorType = eventAdaptorElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_TYPE));
        List<String> encryptedProperties = InputEventAdaptorManagerValueHolder.getCarbonEventAdaptorManagerService().getEncryptedProperties(adaptorType);

        Iterator propertyIter = eventAdaptorElement.getChildrenWithName(
                new QName(InputEventAdaptorManagerConstants.IEA_CONF_NS, InputEventAdaptorManagerConstants.IEA_ELE_PROPERTY));
        InternalInputEventAdaptorConfiguration outputEventAdaptorPropertyConfiguration = new InternalInputEventAdaptorConfiguration();
        if (propertyIter.hasNext()) {
            while (propertyIter.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(InputEventAdaptorManagerConstants.IEA_ATTR_NAME));

                if (encryptedProperties.contains(name.trim())) {
                    OMAttribute encryptedAttribute = propertyOMElement.getAttribute(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_ENCRYPTED));

                    if (encryptedAttribute == null || (!"true".equals(encryptedAttribute.getAttributeValue()))) {
                        String value = propertyOMElement.getText();

                        try {
                            value = new String(CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(value.getBytes()));
                            propertyOMElement.setText(value);
                            propertyOMElement.addAttribute(InputEventAdaptorManagerConstants.IEA_ATTR_ENCRYPTED, "true", null);
                        } catch (Exception e) {
                            log.error("Unable to decrypt the encrypted field: " + name + " for adaptor type: " + adaptorType);
                            propertyOMElement.setText("");
                        }
                    }
                }
            }
        }

        InputEventAdaptorConfigurationFilesystemInvoker.save(eventAdaptorElement.toString(), eventAdaptorName, fileName, axisConfiguration);
    }

    private static void save(String eventAdaptorConfiguration, String eventAdaptorName,
                             String fileName, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        InputEventAdaptorDeployer deployer = (InputEventAdaptorDeployer) getDeployer(axisConfiguration, InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);
        String filePath = getfilePathFromFilename(fileName, axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = new XmlFormatter().format(eventAdaptorConfiguration);
            deployer.getDeployedEventAdaptorFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Input Event Adaptor configuration saved in th filesystem : " + eventAdaptorName);
            deployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            deployer.getDeployedEventAdaptorFilePaths().remove(filePath);
            log.error("Could not save Input Event Adaptor configuration " + eventAdaptorName, e);
            throw new InputEventAdaptorManagerConfigurationException("Error while saving : " + e.getMessage(), e);
        }
    }

    public static boolean isFileExists(String filename, AxisConfiguration axisConfiguration) {
        String filePath = getfilePathFromFilename(filename, axisConfiguration);
        File file = new File(filePath);
        return file.exists();
    }

    public static void delete(String fileName, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        try {
            String filePath = getfilePathFromFilename(fileName, axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                InputEventAdaptorDeployer deployer = (InputEventAdaptorDeployer) getDeployer(axisConfiguration, InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);
                deployer.getUndeployedEventAdaptorFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Input Event Adaptor configuration : " + fileName);
                    deployer.getUndeployedEventAdaptorFilePaths().remove(filePath);
                } else {
                    log.info("Input Event Adaptor configuration deleted from file system : " + fileName);
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new InputEventAdaptorManagerConfigurationException("Error while deleting the Input Event Adaptor " + e.getMessage(), e);
        }
    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    public static void reload(String filePath, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        InputEventAdaptorDeployer deployer = (InputEventAdaptorDeployer) getDeployer(axisConfiguration, InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);

        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
        try {
            deployer.processUndeploy(filePath);
            deployer.processDeploy(deploymentFileData);
        } catch (DeploymentException e) {
            throw new InputEventAdaptorManagerConfigurationException(e);
        }
    }

    public static String readEventAdaptorConfigurationFile(String fileName,
                                                           AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String filePath = getfilePathFromFilename(fileName, axisConfiguration);
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new InputEventAdaptorManagerConfigurationException("Input Event Adaptor file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new InputEventAdaptorManagerConfigurationException("Cannot read the Input Event Adaptor file : " + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new InputEventAdaptorManagerConfigurationException("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }

        return stringBuilder.toString().trim();
    }

    private static String getfilePathFromFilename(String filename,
                                                  AxisConfiguration axisConfiguration) {
        return new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY + File.separator + filename;
    }
}
