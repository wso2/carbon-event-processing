/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.event.simulator.core.CSVFileInfo;
import org.wso2.carbon.event.simulator.core.DataSourceTableAndStreamInfo;
import org.wso2.carbon.event.simulator.core.EventSimulatorConstant;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class DeploymentHelper {

    private static final Log log = LogFactory.getLog(DeploymentHelper.class);

    /**
     * Returns CSVFileInfo configuration object from stream configuration XML file
     *
     * @param streamConfigXMLFile stream configuration XML file
     * @return CSVFileInfo configuration object
     * @throws DeploymentException when the given XML file cannot be parsed; missing required info; or when the contents are inconsistent with filename.
     */
    public static CSVFileInfo getCSVFileInfo(File streamConfigXMLFile,
                                             AxisConfiguration axisConfiguration)
            throws DeploymentException {
        CSVFileInfo csvFileInfo = new CSVFileInfo();

        String csvFileName = streamConfigXMLFile.getName().replace(EventSimulatorConstant.CONFIGURATION_XML_SUFFIX, EventSimulatorConstant.CSV_EXTENSION);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(streamConfigXMLFile);
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Failed to load configuration from file: " + streamConfigXMLFile.getName(), e);
        } catch (SAXException e) {
            throw new DeploymentException("Failed to load configuration from file: " + streamConfigXMLFile.getName(), e);
        } catch (IOException e) {
            throw new DeploymentException("Failed to load configuration from file: " + streamConfigXMLFile.getName(), e);
        }

        Element element = doc.getDocumentElement();
        Node fileNode = element.getElementsByTagName(EventSimulatorConstant.FILE_ELEMENT).item(0);
        if (fileNode == null) {
            throw new DeploymentException(EventSimulatorConstant.FILE_ELEMENT + " absent in stream configuration file: " + streamConfigXMLFile.getName());
        }
        String extractedCSVFileName = fileNode.getTextContent();
        if (!extractedCSVFileName.equals(csvFileName)) {
            throw new DeploymentException("Wrong file name: " + extractedCSVFileName + " given in stream configuration file: " + streamConfigXMLFile.getName());
        }
        csvFileInfo.setFileName(extractedCSVFileName);

        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;
        String csvAbsolutePath = path + File.separator + extractedCSVFileName;
        File csvFile = new File(csvAbsolutePath);

        if (!csvFile.exists()) {
            throw new DeploymentException("CSV file: " + extractedCSVFileName + " does not exist for stream configuration: " + streamConfigXMLFile.getName());
        }

        csvFileInfo.setFilePath(csvAbsolutePath);

        Node streamIdNode = element.getElementsByTagName(EventSimulatorConstant.STREAM_ID_ELEMENT).item(0);
        if (streamIdNode == null) {
            throw new DeploymentException(EventSimulatorConstant.STREAM_ID_ELEMENT + " absent in stream configuration file: " + streamConfigXMLFile.getName());
        }
        csvFileInfo.setStreamID(streamIdNode.getTextContent());

        Node eventSendingDelayNode = element.getElementsByTagName(EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES).item(0);
        if (eventSendingDelayNode == null) {
            throw new DeploymentException(EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES + " absent in stream configuration file: " + streamConfigXMLFile.getName());
        }
        csvFileInfo.setDelayBetweenEventsInMillis(Long.valueOf(eventSendingDelayNode.getTextContent()));

        Node separateCharNode = element.getElementsByTagName(EventSimulatorConstant.SEPARATE_CHAR_ELEMENT).item(0);
        if (separateCharNode == null) {
            throw new DeploymentException(EventSimulatorConstant.SEPARATE_CHAR_ELEMENT + " absent in stream configuration file: " + streamConfigXMLFile.getName());
        }
        csvFileInfo.setSeparateCharacter(separateCharNode.getTextContent());

        return csvFileInfo;
    }

    /**
     * Returns DataSourceTableAndStreamInfo configuration object from datasource configuration XML file
     *
     * @param datasourceConfigXMLFile Datasource configuration XML file
     * @return DataSourceTableAndStreamInfo configuration object
     */
    public static DataSourceTableAndStreamInfo getEventMappingConfiguration(
            File datasourceConfigXMLFile) {
        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = new DataSourceTableAndStreamInfo();
        try {
            if (datasourceConfigXMLFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(datasourceConfigXMLFile);

                Element element = doc.getDocumentElement();
                if (element.getAttribute(EventSimulatorConstant.STREAM_CONFIGURATION_TYPE).equalsIgnoreCase(EventSimulatorConstant.STREAM_CONFIGURATION_TYPE_DATABASE)) {
                    dataSourceTableAndStreamInfo.setConfigurationName(element.getAttribute(EventSimulatorConstant.STREAM_CONFIGURATION_NAME));
                    dataSourceTableAndStreamInfo.setDataSourceName(
                            element.getElementsByTagName(EventSimulatorConstant.DATA_SOURCE_NAME).item(0)
                                    .getTextContent());
                    dataSourceTableAndStreamInfo.setTableName(
                            element.getElementsByTagName(EventSimulatorConstant.TABLE_NAME).item(0).getTextContent());
                    dataSourceTableAndStreamInfo.setEventStreamID(
                            element.getElementsByTagName(EventSimulatorConstant.EVENT_STREAM_ID).item(0)
                                    .getTextContent());
                    dataSourceTableAndStreamInfo.setDelayBetweenEventsInMillis(Long.parseLong(
                            element.getElementsByTagName(EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES).item(0)
                                    .getTextContent()));
                    NodeList columnMappings = element.getElementsByTagName(EventSimulatorConstant.COLUMN_MAPPING);
                    String dataSourceColumnsAndTypes[][] = new String[2][columnMappings.getLength()];

                    for (int i = 0; i < columnMappings.getLength(); i++) {
                        dataSourceColumnsAndTypes[0][i] = columnMappings.item(i).getAttributes().item(0).getNodeValue();
                        dataSourceColumnsAndTypes[1][i] = columnMappings.item(i).getAttributes().item(1).getNodeValue();
                    }
                    dataSourceTableAndStreamInfo.setDataSourceColumnsAndTypes(dataSourceColumnsAndTypes);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred when converting the datasource configuration file ", e);
        }
        return dataSourceTableAndStreamInfo;
    }
}
