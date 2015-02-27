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
package org.wso2.carbon.event.receiver.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processing.application.deployer.EventProcessingDeployer;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigBuilder;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy event builders as axis2 service
 */
public class EventReceiverDeployer extends AbstractDeployer implements EventProcessingDeployer {

    private static Log log = LogFactory.getLog(EventReceiverDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventReceiverFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> undeployedEventReceiverFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event receiver configuration file, create it and deploy it
     *
     * @param deploymentFileData information about the event receiver
     * @throws org.apache.axis2.deployment.DeploymentException for any errors
     */
    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventReceiverFilePaths.contains(path)) {
            try {
                processDeployment(deploymentFileData);
            } catch (Throwable e) {
                String errorMsg = "Event receiver not deployed and in inactive state :'" + deploymentFileData.getFile().getName();
                log.error(errorMsg, e);
                throw new DeploymentException(errorMsg, e);
            }
        } else {
            deployedEventReceiverFilePaths.remove(path);
        }
    }

    private OMElement getEbConfigOMElement(File ebConfigFile) throws DeploymentException {
        OMElement ebConfigElement = null;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(ebConfigFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            ebConfigElement = builder.getDocumentElement();
            ebConfigElement.build();
        } catch (FileNotFoundException e) {
            String errorMessage = ebConfigFile.getName() + " cannot be found";
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ebConfigFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Error parsing configuration syntax : " + ebConfigFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Cannot close the input stream";
                log.error(errorMessage, e);
            }
        }

        return ebConfigElement;
    }

    @Override
    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event builder configuration file
     *
     * @param filePath the filePath to the EventReceiverConfiguration file to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     */
    @Override
    public void undeploy(String filePath) throws DeploymentException {
        if (!undeployedEventReceiverFilePaths.contains(filePath)) {
            try {
                processUndeployment(filePath);
            } catch (Throwable e) {
                String errorMsg = "Event receiver file is not deployed : " + new File(filePath).getName();
                log.error(errorMsg + ":" + e.getMessage(), e);
                throw new DeploymentException(errorMsg, e);
            }
        } else {
            undeployedEventReceiverFilePaths.remove(filePath);
        }
    }

    public void setDirectory(String directory) {

    }

    public void processDeployment(DeploymentFileData deploymentFileData)
            throws DeploymentException, EventReceiverConfigurationException {

        File ebConfigXmlFile = deploymentFileData.getFile();
        String fileName = deploymentFileData.getName();
        CarbonEventReceiverService carbonEventReceiverService = EventReceiverServiceValueHolder.getCarbonEventReceiverService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventReceiverName = "";
        String streamNameWithVersion = null;
        AxisConfiguration currentAxisConfiguration = configurationContext.getAxisConfiguration();
        OMElement ebConfigOMElement = null;
        String filePath = deploymentFileData.getFile().getPath();
        if(!carbonEventReceiverService.isEventReceiverFileAlreadyExist(ebConfigXmlFile.getName(),tenantId)) {
            try {
                ebConfigOMElement = getEbConfigOMElement(ebConfigXmlFile);
                if (!ebConfigOMElement.getLocalName().equals(EventReceiverConstants.ER_ELEMENT_ROOT_ELEMENT)) {
                    throw new EventReceiverConfigurationException("Wrong event receiver configuration file, Invalid root element " + ebConfigOMElement.getQName() + " in " + ebConfigXmlFile.getName());
                }
                eventReceiverName = ebConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
                String inputMappingType = EventReceiverConfigHelper.getInputMappingType(ebConfigOMElement);

                if (eventReceiverName == null || eventReceiverName.trim().isEmpty()) {
                    throw new EventReceiverConfigurationException(ebConfigXmlFile.getName() + " is not a valid event receiver configuration file, does not contain a valid event-receiver name");
                }

                EventReceiverConfiguration eventReceiverConfiguration = EventReceiverConfigBuilder.getEventReceiverConfiguration(ebConfigOMElement, inputMappingType, tenantId);
                if (eventReceiverConfiguration != null && (!carbonEventReceiverService.isEventReceiverAlreadyExists(tenantId, eventReceiverName))) {
                    streamNameWithVersion = eventReceiverConfiguration.getToStreamName() + EventReceiverConstants.STREAM_NAME_VER_DELIMITER + eventReceiverConfiguration.getToStreamVersion();
                    carbonEventReceiverService.addEventReceiver(eventReceiverConfiguration, configurationContext.getAxisConfiguration());
                    carbonEventReceiverService.addEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(), EventReceiverConfigurationFile.DeploymentStatus.DEPLOYED,
                            eventReceiverName + " successfully deployed.", null, streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                    log.info("Event Receiver deployed successfully and in active state : " + eventReceiverName);
                } else {
                    throw new EventReceiverConfigurationException("Event Receiver not deployed and in inactive state, since there is a event receiver registered with the same name in this tenant :" + eventReceiverName);
                }
            } catch (EventReceiverValidationException e) {
                carbonEventReceiverService.addEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(), EventReceiverConfigurationFile.DeploymentStatus.WAITING_FOR_DEPENDENCY,
                        e.getMessage(), e.getDependency(), streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                log.info("Event receiver deployment held back and in inactive state :" + eventReceiverName + ", Waiting for Input Event Adaptor dependency :" + e.getDependency());
            } catch (EventReceiverStreamValidationException e) {
                carbonEventReceiverService.addEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(), EventReceiverConfigurationFile.DeploymentStatus.WAITING_FOR_STREAM_DEPENDENCY,
                        e.getMessage(), e.getDependency(), streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                log.info("Event receiver deployment held back and in inactive state :" + eventReceiverName + ", Stream validation exception : " + e.getMessage());
            } catch (EventReceiverConfigurationException e) {
                carbonEventReceiverService.addEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(), EventReceiverConfigurationFile.DeploymentStatus.ERROR,
                        "Exception when deploying event receiver configuration file:\n" + e.getMessage(), null, streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                throw new EventReceiverConfigurationException(e.getMessage(), e);
            } catch (Throwable e) {
                log.error("Invalid configuration in event receiver configuration file :" + ebConfigXmlFile.getName(), e);
                carbonEventReceiverService.addEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(), EventReceiverConfigurationFile.DeploymentStatus.ERROR,
                        "Deployment exception: " + e.getMessage(), null, streamNameWithVersion, ebConfigOMElement, currentAxisConfiguration);
                throw new DeploymentException(e);
            }
        } else {
            log.info("Event receiver " + eventReceiverName + " is already registered with this tenant (" + tenantId + "), hence ignoring redeployment");
        }
    }

    public void processUndeployment(String filePath) throws EventReceiverConfigurationException {
        String fileName = new File(filePath).getName();
        log.info("Event Receiver undeployed successfully : " + fileName);
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventReceiverService carbonEventReceiverService = EventReceiverServiceValueHolder.getCarbonEventReceiverService();
        carbonEventReceiverService.removeEventReceiverConfigurationFile(fileName, tenantID);
    }

    public void executeManualDeployment(DeploymentFileData deploymentFileData)
            throws EventReceiverConfigurationException {
        try {
            processDeployment(deploymentFileData);
        } catch (DeploymentException e) {
            throw new EventReceiverConfigurationException("Error while attempting manual deployment :" + e.getMessage(), e);
        }
    }

    public void executeManualUndeployment(String filePath)
            throws EventReceiverConfigurationException {
        processUndeployment(filePath);
    }

    public Set<String> getDeployedEventReceiverFilePaths() {
        return deployedEventReceiverFilePaths;
    }

    public Set<String> getUndeployedEventReceiverFilePaths() {
        return undeployedEventReceiverFilePaths;
    }
}


