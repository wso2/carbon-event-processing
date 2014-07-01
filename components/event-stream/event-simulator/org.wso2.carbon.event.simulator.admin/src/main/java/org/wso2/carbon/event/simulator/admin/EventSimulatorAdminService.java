/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.simulator.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.wso2.carbon.core.AbstractAdmin;

import org.wso2.carbon.databridge.commons.StreamDefinition;

import org.wso2.carbon.event.simulator.admin.internal.util.EventSimulatorAdminvalueHolder;
import org.wso2.carbon.event.simulator.core.*;
import org.wso2.carbon.databridge.commons.Attribute;


//import org.wso2.carbon.eventsimulator.core.EventSimulator;

import org.w3c.dom.Document;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collection;
import java.util.List;


public class EventSimulatorAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventSimulatorAdminService.class);

    public StreamDefinitionInfoDto[] getAllEventStreamInfoDto() {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        try {
            Collection<StreamDefinition> eventStreamDefinitionList = eventSimulator.getAllEventStreamDefinitions();

            if (eventStreamDefinitionList != null) {

                StreamDefinitionInfoDto[] streamDefinitionInfoDtos = new StreamDefinitionInfoDto[eventStreamDefinitionList.size()];
                int index = 0;
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    streamDefinitionInfoDtos[index] = new StreamDefinitionInfoDto();
                    streamDefinitionInfoDtos[index].setStreamName(streamDefinition.getName());
                    streamDefinitionInfoDtos[index].setStreamVersion(streamDefinition.getVersion());
                    streamDefinitionInfoDtos[index].setStreamDefinition(streamDefinition.toString());
                    streamDefinitionInfoDtos[index].setStreamDescription(streamDefinition.getDescription());

                    // Set Meta attributes to EventStreamInfoDtos
                    List<Attribute> meataDataAttributeList = streamDefinition.getMetaData();


                    if (meataDataAttributeList != null) {
                        StreamAttributeDto[] metaDataAttributeArray = new StreamAttributeDto[meataDataAttributeList.size()];
                        for (int i = 0; i < metaDataAttributeArray.length; i++) {

                            metaDataAttributeArray[i] = new StreamAttributeDto();
                            metaDataAttributeArray[i].setAttributeName(meataDataAttributeList.get(i).getName());
                            metaDataAttributeArray[i].setAttributeType(meataDataAttributeList.get(i).getType().toString());

                        }

                        streamDefinitionInfoDtos[index].setMetaAttributes(metaDataAttributeArray);
                    }
                    //Set correlation attributes to EventStreamInfoDtos
                    List<Attribute> correlationDataAttributeList = streamDefinition.getCorrelationData();


                    if (correlationDataAttributeList != null) {
                        StreamAttributeDto[] correlationDataAttributeArray = new StreamAttributeDto[correlationDataAttributeList.size()];

                        for (int j = 0; j < correlationDataAttributeArray.length; j++) {
                            correlationDataAttributeArray[j] = new StreamAttributeDto();
                            correlationDataAttributeArray[j].setAttributeName(correlationDataAttributeList.get(j).getName());
                            correlationDataAttributeArray[j].setAttributeType(correlationDataAttributeList.get(j).getType().toString());
                        }

                        streamDefinitionInfoDtos[index].setCorrelationAttributes(correlationDataAttributeArray);
                    }
                    //Set payload data attributes to EventStreamInfoDtos

                    List<Attribute> payloadDataAttributeList = streamDefinition.getPayloadData();


                    if (payloadDataAttributeList != null) {
                        StreamAttributeDto[] payloadDataAttributesArray = new StreamAttributeDto[payloadDataAttributeList.size()];
                        for (int k = 0; k < payloadDataAttributesArray.length; k++) {
                            payloadDataAttributesArray[k] = new StreamAttributeDto();
                            payloadDataAttributesArray[k].setAttributeName(payloadDataAttributeList.get(k).getName());
                            payloadDataAttributesArray[k].setAttributeType(payloadDataAttributeList.get(k).getType().toString());
                        }

                        streamDefinitionInfoDtos[index].setPayloadAttributes(payloadDataAttributesArray);
                    }
                    index++;
                }
                return streamDefinitionInfoDtos;

            } else {
                return new StreamDefinitionInfoDto[0];
            }

        } catch (Exception e) {
            log.error(e);
        }

        return new StreamDefinitionInfoDto[0];
    }

    public void sendEvent(EventDto eventDto) throws AxisFault {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();
        StreamDefinition streamDefinition = null;
        String streamID = eventDto.getEventStreamId();
        String[] attributeValues = eventDto.getAttributeValues();

        try {
            Collection<StreamDefinition> streamDefinitionList = eventSimulator.getAllEventStreamDefinitions();

            int index = 0;
            if (streamDefinitionList != null) {
                for (StreamDefinition streamDefinition1 : streamDefinitionList) {
                    if (streamDefinition1.getStreamId().equals(streamID)) {
                        streamDefinition = streamDefinition1;
                        break;
                    }
                }
            }

            Event event = new Event();

            if (streamDefinition != null) {
                event.setStreamDefinition(streamDefinition);
                event.setAttributeValues(attributeValues);

                eventSimulator.sendEvent(event);
            } else {
                throw new AxisFault("Relevant stream not found");
            }

        } catch (Exception e) {
            log.error(e);
            throw new AxisFault(e.getMessage(), e);
        }

    }

    public void uploadService(UploadedFileItemDto[] fileItems) throws AxisFault {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        ConfigurationContext configurationContext = getConfigContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();

        UploadedFileItem[] uploadedFileItems = new UploadedFileItem[fileItems.length];

        int index = 0;
        for (UploadedFileItemDto uploadedFileItemDto : fileItems) {
            uploadedFileItems[index] = new UploadedFileItem();

            uploadedFileItems[index].setFileName(uploadedFileItemDto.getFileName());
            uploadedFileItems[index].setFileType(uploadedFileItemDto.getFileType());
            uploadedFileItems[index].setDataHandler(uploadedFileItemDto.getDataHandler());

            index++;
        }

        eventSimulator.uploadService(uploadedFileItems, axisConfiguration);

    }


    public void sendConfigDetails(String fileName, String streamId, String separateChar) {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        ConfigurationContext configurationContext = getConfigContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();

        eventSimulator.createConfigurationXML(fileName, streamId, separateChar, axisConfiguration);

    }

    public CSVFileInfoDto[] getAllCSVFileInfo() {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        try {
            List<CSVFileInfo> CSVFileInfoList = eventSimulator.getAllCSVFileInfo();

            if (CSVFileInfoList != null) {

                CSVFileInfoDto[] CSVFileInfoDtoArray = new CSVFileInfoDto[CSVFileInfoList.size()];

                int index = 0;
                for (CSVFileInfo csvFileInfo : CSVFileInfoList) {

                    CSVFileInfoDtoArray[index] = new CSVFileInfoDto();
                    CSVFileInfoDtoArray[index].setFileName(csvFileInfo.getFileName());
                    CSVFileInfoDtoArray[index].setFilePath(csvFileInfo.getFilePath());
                    if (csvFileInfo.getStreamID() != null) {
                        CSVFileInfoDtoArray[index].setStreamID(csvFileInfo.getStreamID());
                    }
                    index++;
                }

                return CSVFileInfoDtoArray;
            } else {
                return new CSVFileInfoDto[0];
            }

        } catch (Exception e) {
            log.error(e);
        }

        return new CSVFileInfoDto[0];
    }

    public void sendEventsViaFile(String fileName) throws AxisFault {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        eventSimulator.sendEvents(fileName);
    }

    public void deleteFile(String fileName) throws AxisFault {
        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        ConfigurationContext configurationContext = getConfigContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        eventSimulator.deleteFile(fileName, axisConfiguration);
    }

}
