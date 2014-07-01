/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.simulator.core;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.event.simulator.core.internal.ds.CarbonEventSimulator;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

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

        } catch (Exception e) {
            throw new DeploymentException("CSV file not deployed and in inactive state :  " + new File(path).getName(), e);
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

        File csvFile = deploymentFileData.getFile();
        CarbonEventSimulator eventSimulator = EventSimulatorValueHolder.getEventSimulator();
        CSVFileInfo csvFileInfo = new CSVFileInfo();

        csvFileInfo.setFileName(csvFile.getName());
        csvFileInfo.setFilePath(csvFile.getAbsolutePath());

        String[] streamIdSepChar = getEventMappingConfiguration(csvFile.getName());
        if (streamIdSepChar[0] != null) {
            csvFileInfo.setStreamID(streamIdSepChar[0]);
        }
        if (streamIdSepChar[1] != null) {
            csvFileInfo.setSeparateCharacter(streamIdSepChar[1]);
        }
        eventSimulator.addCSVFileInfo(csvFileInfo);

    }

    public String[] getEventMappingConfiguration(String fileName) {

        String xmlName = fileName.substring(0, fileName.length() - 4) + EventSimulatorConstant.CONFIGURATION_XML_PREFIX;
        String repo = configurationContext.getAxisConfiguration().getRepository().getPath();
        String dirPath = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;
        String absolutePath = dirPath + File.separator + xmlName;
        String streamID = null;
        String separateChar;
        String[] streamIdSepChar = new String[2];
        try {
            File xmlFile = new File(absolutePath);

            if (xmlFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);

                Element element = doc.getDocumentElement();
                streamID = element.getElementsByTagName(EventSimulatorConstant.STREAM_ID_ELEMENT).item(0).getTextContent();
                separateChar = element.getElementsByTagName(EventSimulatorConstant.SEPARATE_CHAR_ELEMENT).item(0).getTextContent();

                streamIdSepChar[0] = streamID;
                streamIdSepChar[1] = separateChar;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return streamIdSepChar;
    }
}
