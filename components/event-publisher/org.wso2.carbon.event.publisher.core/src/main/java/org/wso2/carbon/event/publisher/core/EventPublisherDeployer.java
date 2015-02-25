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
package org.wso2.carbon.event.publisher.core;

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
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherStreamValidationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherConfigurationBuilder;
import org.wso2.carbon.event.publisher.core.internal.util.helper.EventPublisherConfigurationHelper;
import org.wso2.carbon.event.processing.application.deployer.EventProcessingDeployer;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherService;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfigurationFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy event publisher as axis2 service
 */
public class EventPublisherDeployer extends AbstractDeployer implements EventProcessingDeployer {

    private static Log log = LogFactory.getLog(EventPublisherDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventPublisherFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> undeployedEventPublisherFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event publisher file, create it and deploy it
     *
     * @param deploymentFileData information about the event publisher
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventPublisherFilePaths.contains(path)) {
            try {
                processDeploy(deploymentFileData);
            } catch (EventPublisherConfigurationException e) {
                throw new DeploymentException("Event publisher file " + deploymentFileData.getName() + " is not deployed ", e);
            }
        } else {
            deployedEventPublisherFilePaths.remove(path);
        }
    }

    private OMElement getEventPublisherOMElement(File eventPublisherFile)
            throws DeploymentException {
        String fileName = eventPublisherFile.getName();
        OMElement eventPublisherOMElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(eventPublisherFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            eventPublisherOMElement = builder.getDocumentElement();
            eventPublisherOMElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " file cannot be found with name : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for file " + eventPublisherFile.getName();
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
        return eventPublisherOMElement;
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event publisher configuration
     *
     * @param filePath the path to the event publisher artifact to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {


        if (!undeployedEventPublisherFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            undeployedEventPublisherFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData)
            throws DeploymentException, EventPublisherConfigurationException {

        File eventPublisherFile = deploymentFileData.getFile();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        CarbonEventPublisherService carbonEventPublisherService = EventPublisherServiceValueHolder.getCarbonEventPublisherService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventPublisherName = "";

        if (!carbonEventPublisherService.isEventPublisherFileAlreadyExist(eventPublisherFile.getName(), tenantId)) {
            try {
                OMElement eventPublisherOMElement = getEventPublisherOMElement(eventPublisherFile);
                if (!(eventPublisherOMElement.getQName().getLocalPart()).equals(EventPublisherConstants.EF_ELE_ROOT_ELEMENT)) {
                    throw new DeploymentException("Wrong event publisher configuration file, Invalid root element " + eventPublisherOMElement.getQName() + " in " + eventPublisherFile.getName());
                }

                EventPublisherConfigurationHelper.validateEventPublisherConfiguration(eventPublisherOMElement);
                String mappingType = EventPublisherConfigurationHelper.getOutputMappingType(eventPublisherOMElement);
                if (mappingType != null) {
                    mappingType = mappingType.toLowerCase();
                    EventPublisherConfiguration eventPublisherConfiguration = EventPublisherConfigurationBuilder.getEventPublisherConfiguration(eventPublisherOMElement, tenantId, mappingType);
                    eventPublisherName = eventPublisherConfiguration.getEventPublisherName();
                    if (carbonEventPublisherService.checkEventPublisherValidity(tenantId, eventPublisherName)) {
                        carbonEventPublisherService.addEventPublisherConfiguration(eventPublisherConfiguration);
                        carbonEventPublisherService.addEventEventPublisherConfigurationFile(tenantId, createEventPublisherConfigurationFile(eventPublisherName,
                                deploymentFileData.getFile(), EventPublisherConfigurationFile.Status.DEPLOYED, axisConfiguration, null, null));
                        log.info("Event Publisher configuration successfully deployed and in active state : " + eventPublisherName);
                    } else {
                        throw new EventPublisherConfigurationException("Event Publisher not deployed and in inactive state," +
                                " since there is a event publisher registered with the same name in this tenant :" + eventPublisherFile.getName());
                    }
                } else {
                    throw new EventPublisherConfigurationException("Event Ntifier not deployed and in inactive state, " +
                            "since it does not contain a proper mapping type : " + eventPublisherFile.getName());
                }
            } catch (EventPublisherConfigurationException ex) {
                log.error("Event Publisher not deployed and in inactive state, " + ex.getMessage(), ex);
                carbonEventPublisherService.addEventEventPublisherConfigurationFile(tenantId,
                        createEventPublisherConfigurationFile(eventPublisherName, deploymentFileData.getFile(), EventPublisherConfigurationFile.Status.ERROR, null, null, null));
                throw new EventPublisherConfigurationException(ex.getMessage(), ex);
            } catch (EventPublisherValidationException ex) {
                carbonEventPublisherService.addEventEventPublisherConfigurationFile(tenantId,
                        createEventPublisherConfigurationFile(eventPublisherName, deploymentFileData.getFile(),
                                EventPublisherConfigurationFile.Status.WAITING_FOR_DEPENDENCY, axisConfiguration, ex.getMessage(), ex.getDependency())
                );
                log.info("Event Publisher deployment held back and in inactive state : " + eventPublisherFile.getName() + ", waiting for dependency : " + ex.getDependency());
            } catch (EventPublisherStreamValidationException e) {
                carbonEventPublisherService.addEventEventPublisherConfigurationFile(tenantId,
                        createEventPublisherConfigurationFile(eventPublisherName, deploymentFileData.getFile(),
                                EventPublisherConfigurationFile.Status.WAITING_FOR_STREAM_DEPENDENCY, axisConfiguration, e.getMessage(), e.getDependency())
                );
                log.info("Event Publisher deployment held back and in inactive state :" + eventPublisherFile.getName() + ", Stream validation exception : " + e.getMessage());
            } catch (DeploymentException e) {
                log.error("Event Publisher not deployed and in inactive state : " + eventPublisherFile.getName() + " , " + e.getMessage(), e);
                carbonEventPublisherService.addEventEventPublisherConfigurationFile(tenantId, createEventPublisherConfigurationFile(eventPublisherName,
                        deploymentFileData.getFile(), EventPublisherConfigurationFile.Status.ERROR, null, "Deployment exception occurred", null));
                throw new EventPublisherConfigurationException(e.getMessage(), e);
            }
        } else {
            log.info("Event Publisher " + eventPublisherName + " is already registered with this tenant (" + tenantId + "), hence ignoring redeployment");
        }
    }

    public void processUndeploy(String filePath) {

        String fileName = new File(filePath).getName();
        log.info("Event Formatter undeployed successfully : " + fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventPublisherService carbonEventPublisherService = EventPublisherServiceValueHolder.getCarbonEventPublisherService();
        carbonEventPublisherService.removeEventFormatterConfigurationFromMap(fileName, tenantId);
    }

    public void setDirectory(String directory) {

    }

    public void executeManualDeployment(String filePath)
            throws DeploymentException, EventPublisherConfigurationException {
        processDeploy(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) {
        processUndeploy(filePath);
    }

    private EventPublisherConfigurationFile createEventPublisherConfigurationFile(
            String eventFormatterName,
            File file,
            EventPublisherConfigurationFile.Status status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage,
            String dependency) {
        EventPublisherConfigurationFile eventPublisherConfigurationFile = new EventPublisherConfigurationFile();
        eventPublisherConfigurationFile.setFileName(file.getName());
        eventPublisherConfigurationFile.setFilePath(file.getAbsolutePath());
        eventPublisherConfigurationFile.setEventPublisherName(eventFormatterName);
        eventPublisherConfigurationFile.setStatus(status);
        eventPublisherConfigurationFile.setDependency(dependency);
        eventPublisherConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventPublisherConfigurationFile.setAxisConfiguration(axisConfiguration);

        return eventPublisherConfigurationFile;
    }

    public Set<String> getDeployedEventPublisherFilePaths() {
        return deployedEventPublisherFilePaths;
    }

    public Set<String> getUndeployedEventPublisherFilePaths() {
        return undeployedEventPublisherFilePaths;
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


