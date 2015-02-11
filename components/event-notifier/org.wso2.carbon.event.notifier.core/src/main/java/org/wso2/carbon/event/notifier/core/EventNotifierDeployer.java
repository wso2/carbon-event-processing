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
package org.wso2.carbon.event.notifier.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierStreamValidationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierValidationException;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;
import org.wso2.carbon.event.notifier.core.internal.util.EventNotifierConfigurationBuilder;
import org.wso2.carbon.event.notifier.core.internal.util.helper.EventNotifierConfigurationHelper;
import org.wso2.carbon.event.processing.application.deployer.EventProcessingDeployer;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConfiguration;
import org.wso2.carbon.event.notifier.core.internal.CarbonEventNotifierService;
import org.wso2.carbon.event.notifier.core.internal.util.EventNotifierConfigurationFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy event notifier as axis2 service
 */
public class EventNotifierDeployer extends AbstractDeployer implements EventProcessingDeployer {

    private static Log log = LogFactory.getLog(EventNotifierDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventNotifierFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> undeployedEventNotifierFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event notifier file, create it and deploy it
     *
     * @param deploymentFileData information about the event notifier
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventNotifierFilePaths.contains(path)) {
            try {
                processDeploy(deploymentFileData);
            } catch (EventNotifierConfigurationException e) {
                throw new DeploymentException("Event notifier file " + deploymentFileData.getName() + " is not deployed ", e);
            }
        } else {
            deployedEventNotifierFilePaths.remove(path);
        }
    }

    private OMElement getEventNotifierOMElement(File eventNotifierFile)
            throws DeploymentException {
        String fileName = eventNotifierFile.getName();
        OMElement eventNotifierOMElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(eventNotifierFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            eventNotifierOMElement = builder.getDocumentElement();
            eventNotifierOMElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " file cannot be found with name : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for file " + eventNotifierFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed in " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
        return eventNotifierOMElement;
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event notifier configuration
     *
     * @param filePath the path to the event notifier artifact to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {


        if (!undeployedEventNotifierFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            undeployedEventNotifierFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData)
            throws DeploymentException, EventNotifierConfigurationException {

        File eventNotifierFile = deploymentFileData.getFile();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        CarbonEventNotifierService carbonEventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventNotifierName = "";

        if (!carbonEventNotifierService.isEventNotifierFileAlreadyExist(eventNotifierFile.getName(), tenantId)) {
            try {
                OMElement eventNotifierOMElement = getEventNotifierOMElement(eventNotifierFile);
                if (!(eventNotifierOMElement.getQName().getLocalPart()).equals(EventNotifierConstants.EF_ELE_ROOT_ELEMENT)) {
                    throw new DeploymentException("Wrong event notifier configuration file, Invalid root element " + eventNotifierOMElement.getQName() + " in " + eventNotifierFile.getName());
                }

                EventNotifierConfigurationHelper.validateEventNotifierConfiguration(eventNotifierOMElement);
                String mappingType = EventNotifierConfigurationHelper.getOutputMappingType(eventNotifierOMElement);
                if (mappingType != null) {
                    mappingType = mappingType.toLowerCase();
                    EventNotifierConfiguration eventNotifierConfiguration = EventNotifierConfigurationBuilder.getEventNotifierConfiguration(eventNotifierOMElement, tenantId, mappingType);
                    eventNotifierName = eventNotifierConfiguration.getEventNotifierName();
                    if (carbonEventNotifierService.checkEventNotifierValidity(tenantId, eventNotifierName)) {
                        carbonEventNotifierService.addEventNotifierConfiguration(eventNotifierConfiguration);
                        carbonEventNotifierService.addEventEventNotifierConfigurationFile(tenantId, createEventNotifierConfigurationFile(eventNotifierName,
                                deploymentFileData.getFile(), EventNotifierConfigurationFile.Status.DEPLOYED, axisConfiguration, null, null));
                        log.info("Event Notifier configuration successfully deployed and in active state : " + eventNotifierName);
                    } else {
                        throw new EventNotifierConfigurationException("Event Notifier not deployed and in inactive state," +
                                " since there is a event notifier registered with the same name in this tenant :" + eventNotifierFile.getName());
                    }
                } else {
                    throw new EventNotifierConfigurationException("Event Ntifier not deployed and in inactive state, " +
                            "since it does not contain a proper mapping type : " + eventNotifierFile.getName());
                }
            } catch (EventNotifierConfigurationException ex) {
                log.error("Event Notifier not deployed and in inactive state, " + ex.getMessage(), ex);
                carbonEventNotifierService.addEventEventNotifierConfigurationFile(tenantId,
                        createEventNotifierConfigurationFile(eventNotifierName, deploymentFileData.getFile(), EventNotifierConfigurationFile.Status.ERROR, null, null, null));
                throw new EventNotifierConfigurationException(ex.getMessage(), ex);
            } catch (EventNotifierValidationException ex) {
                carbonEventNotifierService.addEventEventNotifierConfigurationFile(tenantId,
                        createEventNotifierConfigurationFile(eventNotifierName, deploymentFileData.getFile(),
                                EventNotifierConfigurationFile.Status.WAITING_FOR_DEPENDENCY, axisConfiguration, ex.getMessage(), ex.getDependency())
                );
                log.info("Event Notifier deployment held back and in inactive state : " + eventNotifierFile.getName() + ", waiting for dependency : " + ex.getDependency());
            } catch (EventNotifierStreamValidationException e) {
                carbonEventNotifierService.addEventEventNotifierConfigurationFile(tenantId,
                        createEventNotifierConfigurationFile(eventNotifierName, deploymentFileData.getFile(),
                                EventNotifierConfigurationFile.Status.WAITING_FOR_STREAM_DEPENDENCY, axisConfiguration, e.getMessage(), e.getDependency())
                );
                log.info("Event Notifier deployment held back and in inactive state :" + eventNotifierFile.getName() + ", Stream validation exception : " + e.getMessage());
            } catch (DeploymentException e) {
                log.error("Event Notifier not deployed and in inactive state : " + eventNotifierFile.getName() + " , " + e.getMessage(), e);
                carbonEventNotifierService.addEventEventNotifierConfigurationFile(tenantId, createEventNotifierConfigurationFile(eventNotifierName,
                        deploymentFileData.getFile(), EventNotifierConfigurationFile.Status.ERROR, null, "Deployment exception occurred", null));
                throw new EventNotifierConfigurationException(e.getMessage(), e);
            }
        } else {
            log.info("Event Notifier " + eventNotifierName + " is already registered with this tenant (" + tenantId + "), hence ignoring redeployment");
        }
    }

    public void processUndeploy(String filePath) {

        String fileName = new File(filePath).getName();
        log.info("Event Formatter undeployed successfully : " + fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventNotifierService carbonEventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();
        carbonEventNotifierService.removeEventFormatterConfigurationFromMap(fileName, tenantId);
    }

    public void setDirectory(String directory) {

    }

    public void executeManualDeployment(String filePath)
            throws DeploymentException, EventNotifierConfigurationException {
        processDeploy(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) {
        processUndeploy(filePath);
    }

    private EventNotifierConfigurationFile createEventNotifierConfigurationFile(
            String eventFormatterName,
            File file,
            EventNotifierConfigurationFile.Status status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage,
            String dependency) {
        EventNotifierConfigurationFile eventNotifierConfigurationFile = new EventNotifierConfigurationFile();
        eventNotifierConfigurationFile.setFileName(file.getName());
        eventNotifierConfigurationFile.setFilePath(file.getAbsolutePath());
        eventNotifierConfigurationFile.setEventNotifierName(eventFormatterName);
        eventNotifierConfigurationFile.setStatus(status);
        eventNotifierConfigurationFile.setDependency(dependency);
        eventNotifierConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventNotifierConfigurationFile.setAxisConfiguration(axisConfiguration);

        return eventNotifierConfigurationFile;
    }

    public Set<String> getDeployedEventNotifierFilePaths() {
        return deployedEventNotifierFilePaths;
    }

    public Set<String> getUndeployedEventNotifierFilePaths() {
        return undeployedEventNotifierFilePaths;
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


