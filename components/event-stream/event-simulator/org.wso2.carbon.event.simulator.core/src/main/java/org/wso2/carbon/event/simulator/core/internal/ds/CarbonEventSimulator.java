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

package org.wso2.carbon.event.simulator.core.internal.ds;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.core.*;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class CarbonEventSimulator implements EventSimulator {

    private static final Log log = LogFactory.getLog(CarbonEventSimulator.class);
    private HashMap<String, EventStreamProducer> eventProducerMap;
    private HashMap<Integer, HashMap<String, CSVFileInfo>> tenantSpecificCSVFileInfoMap;

    public CarbonEventSimulator() {

        eventProducerMap = new HashMap<String, EventStreamProducer>();
        tenantSpecificCSVFileInfoMap = new HashMap<Integer, HashMap<String, CSVFileInfo>>();
    }

    public Collection<StreamDefinition> getAllEventStreamDefinitions() {
        try {
            EventStreamService eventStreamService = EventSimulatorValueHolder.getEventStreamService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return eventStreamService.getAllStreamDefinitions(tenantId);

        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public void sendEvent(Event eventDetail) throws AxisFault {

        EventStreamService eventstreamservice = EventSimulatorValueHolder.getEventStreamService();

        StreamDefinition streamDefinition = eventDetail.getStreamDefinition();
        String[] attributeValues = eventDetail.getAttributeValues();

        Object[] dataObjects = new Object[attributeValues.length];

        List<Attribute> streamAttributeList = getStreamAttributeList(eventDetail.getStreamDefinition());

        if (validateAttributeValues(streamAttributeList, attributeValues)) {

            for (int i = 0; i < dataObjects.length; i++) {
                Attribute attribute = streamAttributeList.get(i);
                String attributeType = attribute.getType().toString();


                if (attributeType.equals(EventSimulatorConstant.STRING)) {
                    dataObjects[i] = attributeValues[i];
                } else if (attributeType.equals(EventSimulatorConstant.INT)) {
                    try {
                        int val = Integer.parseInt(attributeValues[i]);
                        dataObjects[i] = val;
                    } catch (NumberFormatException e) {
                        throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected " + attribute.getType().toString() + " : " + e.getMessage(), e);
                    }
                } else if (attributeType.equals(EventSimulatorConstant.LONG)) {
                    try {
                        long val = Long.parseLong(attributeValues[i]);
                        dataObjects[i] = val;

                    } catch (NumberFormatException e) {
                        throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected  " + attribute.getType().toString() + " : " + e.getMessage(), e);
                    }
                } else if (attributeType.equals(EventSimulatorConstant.DOUBLE)) {
                    try {
                        double val = Double.parseDouble(attributeValues[i]);
                        dataObjects[i] = val;

                    } catch (NumberFormatException e) {
                        throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected  " + attribute.getType().toString() + " : " + e.getMessage(), e);
                    }
                } else if (attributeType.equals(EventSimulatorConstant.FLOAT)) {
                    try {
                        float val = Float.parseFloat(attributeValues[i]);
                        dataObjects[i] = val;

                    } catch (NumberFormatException e) {
                        throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected  " + attribute.getType().toString() + " : " + e.getMessage(), e);
                    }
                } else if (attributeType.equals(EventSimulatorConstant.BOOLEAN)) {
                    if (!Boolean.parseBoolean(attributeValues[i])) {
                        throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected " + attribute.getType().toString());
                    } else {
                        boolean val = Boolean.parseBoolean(attributeValues[i]);
                        dataObjects[i] = val;
                    }
                }

            }
        }

        if (eventProducerMap.get(streamDefinition.getStreamId()) != null) {

            EventStreamProducer eventProducer = eventProducerMap.get(streamDefinition.getStreamId());
            eventProducer.sendData(dataObjects);

        } else {
            EventStreamProducer eventStreamProducer = new EventStreamProducer();

            eventStreamProducer.setStreamID(streamDefinition.getStreamId());

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            try {
                eventstreamservice.subscribe(eventStreamProducer, tenantId);

            } catch (EventStreamConfigurationException e) {
                log.error(e);
            }

            eventProducerMap.put(streamDefinition.getStreamId(), eventStreamProducer);
            eventStreamProducer.sendData(dataObjects);

        }
    }


    @Override
    public List<CSVFileInfo> getAllCSVFileInfo() {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);

        List<CSVFileInfo> CSVFileInfoList = new ArrayList<CSVFileInfo>(csvFileInfoMap.values());

        return CSVFileInfoList;
    }


    public void addCSVFileInfo(CSVFileInfo csvFileInfo) {


        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificCSVFileInfoMap.containsKey(tenantID)) {
            HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);
            csvFileInfoMap.put(csvFileInfo.getFileName(), csvFileInfo);

        } else {
            HashMap<String, CSVFileInfo> csvFileMap = new HashMap<String, CSVFileInfo>();
            csvFileMap.put(csvFileInfo.getFileName(), csvFileInfo);
            tenantSpecificCSVFileInfoMap.put(tenantID, csvFileMap);

        }
    }

    public void addEventMappingConfiguration(String fileName, String streamId, String separateChar) {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);

        CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
        csvFileInfo.setStreamID(streamId);
        csvFileInfo.setSeparateCharacter(separateChar);
    }

    @Override
    public void createConfigurationXML(String fileName, String streamId, String separateChar, AxisConfiguration axisConfiguration) {

        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(EventSimulatorConstant.ROOT_ELEMENT_NAME);
            doc.appendChild(rootElement);

            Element csvFileName = doc.createElement(EventSimulatorConstant.FILE_ELEMENT);
            csvFileName.appendChild(doc.createTextNode(fileName));
            rootElement.appendChild(csvFileName);

            Element streamID = doc.createElement(EventSimulatorConstant.STREAM_ID_ELEMENT);
            streamID.appendChild(doc.createTextNode(streamId));
            rootElement.appendChild(streamID);

            Element separateCharacter = doc.createElement(EventSimulatorConstant.SEPARATE_CHAR_ELEMENT);
            separateCharacter.appendChild(doc.createTextNode(separateChar));
            rootElement.appendChild(separateCharacter);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            String absolutePath = path + File.separator + fileName.substring(0, fileName.length() - 4) + EventSimulatorConstant.CONFIGURATION_XML_PREFIX;
            StreamResult result = new StreamResult(new File(absolutePath));

            transformer.transform(source, result);
            addEventMappingConfiguration(fileName, streamId, separateChar);

        } catch (ParserConfigurationException e) {
            log.error(e);
        } catch (TransformerException e) {
            log.error(e);
        }


    }

    @Override
    public void deleteFile(String fileName, AxisConfiguration axisConfiguration) throws AxisFault {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);

        CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;

        String xmlFileName = csvFileInfo.getFileName().substring(0, csvFileInfo.getFileName().length() - 4) + EventSimulatorConstant.CONFIGURATION_XML_PREFIX;
        String xmlFilePath = path + File.separator + xmlFileName;

        File file = new File(csvFileInfo.getFilePath());
        File xmlFile = new File(xmlFilePath);

        if (file.delete()) {
            csvFileInfoMap.remove(fileName);
        } else {
            throw new AxisFault("Failed to delete the file .." + csvFileInfo.getFileName());
        }

        if (xmlFile.exists()) {
            xmlFile.delete();
        }


    }

    @Override
    public void sendEvents(String fileName) throws AxisFault {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Thread eventCreator = new Thread(new EventCreation(fileName, tenantId));
        eventCreator.start();


    }

    @Override
    public void uploadService(UploadedFileItem[] fileItems, AxisConfiguration axisConfiguration) throws AxisFault {

        String repo = axisConfiguration.getRepository().getPath();

        if (CarbonUtils.isURL(repo)) {
            throw new AxisFault("URL Repositories are not supported: " + repo);
        }

        String csvDirectory = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;
        String csvTemp = CarbonUtils.getCarbonHome() + EventSimulatorConstant.TEMP_DIR_PATH;


        File csvTempDir = new File(csvTemp);

        if (!csvTempDir.exists() && !csvTempDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + csvTempDir.getAbsolutePath());
        }

        File csvDir = new File(csvDirectory);

        if (!csvDir.exists() && !csvDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + csvDir.getAbsolutePath());
        }

        for (UploadedFileItem uploadedFile : fileItems) {
            String fileName = uploadedFile.getFileName();

            if (fileName == null || fileName.equals("")) {
                throw new AxisFault("Invalid file name. File name is not available");
            }

            if (uploadedFile.getFileType().equals("csv")) {
                try {
                    writeResource(uploadedFile.getDataHandler(), csvTemp, fileName, csvDir);
                } catch (IOException e) {
                    throw new AxisFault("IOError: Writing resource failed.", e);
                }
            } else {
                throw new AxisFault("Invalid file type : " + uploadedFile.getFileType() + " ." +
                        "csv" +
                        " file type is expected");
            }
        }

    }


    private void writeResource(DataHandler dataHandler, String destPath, String fileName,
                               File csvDest) throws IOException {
        File tempDestFile = new File(destPath, fileName);
        FileOutputStream fos = null;
        File destFile = new File(csvDest, fileName);
        try {
            fos = new FileOutputStream(tempDestFile);
            /* File stream is copied to a temp directory in order handle hot deployment issue
               occurred in windows */
            dataHandler.writeTo(fos);
            FileUtils.copyFile(tempDestFile, destFile);

        } catch (FileNotFoundException e) {
            log.error("Cannot find the file", e);
            throw e;
        } catch (IOException e) {
            log.error("IO error.");
            throw e;
        } finally {
            close(fos);
        }

        boolean isDeleted = tempDestFile.delete();
        if (!isDeleted) {
            log.warn("temp file: " + tempDestFile.getAbsolutePath() +
                    " deletion failed, scheduled deletion on server exit.");
            tempDestFile.deleteOnExit();
        }
    }

    public static void close(Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            log.warn("Can't close file streams.", e);
        }
    }

    private List<Attribute> getStreamAttributeList(StreamDefinition streamDefinition) {

        List<Attribute> attributeList = new ArrayList<Attribute>();

        if (streamDefinition != null) {
            int i = 0;
            if (streamDefinition.getMetaData() != null) {
                for (Attribute attribute : streamDefinition.getMetaData()) {
                    attributeList.add(attribute);
                    i++;
                }
            }

            int j = 0;
            if (streamDefinition.getCorrelationData() != null) {
                for (Attribute attribute : streamDefinition.getCorrelationData()) {
                    attributeList.add(attribute);
                    j++;
                }
            }

            int k = 0;
            if (streamDefinition.getPayloadData() != null) {
                for (Attribute attribute : streamDefinition.getPayloadData()) {
                    attributeList.add(attribute);
                    k++;
                }
            }
        }


        return attributeList;
    }

    private StreamDefinition getStreamDefinition(String streamId) {

        EventStreamService eventStreamService = EventSimulatorValueHolder.getEventStreamService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        StreamDefinition streamDefinition = null;

        try {
            Collection<StreamDefinition> streamDefinitions = eventStreamService.getAllStreamDefinitions(tenantId);

            int index = 0;
            for (StreamDefinition streamDefinition1 : streamDefinitions) {
                if (streamDefinition1.getStreamId().equals(streamId)) {
                    streamDefinition = streamDefinition1;
                    break;
                }
                index++;
            }

        } catch (Exception e) {
            log.error(e);
        }


        return streamDefinition;
    }

    private boolean validateAttributeValues(List<Attribute> attributeList, String[] valueArray) throws AxisFault {

        if (attributeList.size() != valueArray.length) {
            throw new AxisFault("Failed configuration of event stream in this file or file is corrupted ");

        }

        return true;
    }


    private class EventCreation implements Runnable {

        String fileName = null;
        int tenantId;

        public EventCreation(String fileName, int tenantId) {
            this.fileName = fileName;
            this.tenantId = tenantId;
        }

        @Override
        public void run() {

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId, true);

                HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantId);

                CSVFileInfo fileInfo = csvFileInfoMap.get(fileName);
                String path = fileInfo.getFilePath();


                File file = new File(path);
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                DataInputStream dis = null;

                StreamDefinition streamDefinition = getStreamDefinition(fileInfo.getStreamID());

                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    dis = new DataInputStream(bis);
                    int rowNumber = 0;
                    while (dis.available() != 0) {

                        String eventValues = dis.readLine();


                        try {
                            String[] attributeValueList = eventValues.split(fileInfo.getSeparateCharacter());
                            Event event = new Event();

                            event.setStreamDefinition(streamDefinition);
                            event.setAttributeValues(attributeValueList);

                            sendEvent(event);
                        } catch (Exception e) {
                            log.error("Error in row " + rowNumber + "-failed to create an event " + e);
                            rowNumber++;
                            continue;
                        }
                        rowNumber++;
                    }

                } catch (IOException e) {
                    log.error(e);
                } finally {
                    try {
                        fis.close();
                        bis.close();
                        dis.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }
}
