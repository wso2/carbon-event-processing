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
package org.wso2.carbon.event.simulator.core.internal.ds;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.core.*;
import org.wso2.carbon.event.stream.manager.core.EventStreamConfig;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;

import javax.activation.DataHandler;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CarbonEventSimulator implements EventSimulator {

    private static final Log log = LogFactory.getLog(CarbonEventSimulator.class);
    private HashMap<String, EventStreamProducer> eventProducerMap;
    private HashMap<Integer, HashMap<String, CSVFileInfo>> tenantSpecificCSVFileInfoMap;
    private HashMap<Integer, HashMap<String, DataSourceTableAndStreamInfo>> tenantSpecificDataSourceInfoMap;

    public CarbonEventSimulator() {
        eventProducerMap = new HashMap<String, EventStreamProducer>();
        tenantSpecificCSVFileInfoMap = new HashMap<Integer, HashMap<String, CSVFileInfo>>();
        tenantSpecificDataSourceInfoMap = new HashMap<Integer, HashMap<String, DataSourceTableAndStreamInfo>>();
    }

    public Collection<StreamDefinition> getAllEventStreamDefinitions() {
        try {
            EventStreamService eventStreamService = EventSimulatorValueHolder.getEventStreamService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Collection<StreamDefinition> collection = new ArrayList<StreamDefinition>();
            for(StreamDefinition streamDefinition: eventStreamService.getAllStreamDefinitions(tenantId)) {
                collection.add(streamDefinition);
            }
            return collection;
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
        if (csvFileInfoMap != null) {
            return new ArrayList<CSVFileInfo>(csvFileInfoMap.values());
        }else {
            return null;
        }

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

    public void addEventMappingConfiguration(String fileName, String streamId,
                                             String separateChar) {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);

        CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
        csvFileInfo.setStreamID(streamId);
        csvFileInfo.setSeparateCharacter(separateChar);
    }

    @Override
    public void createConfigurationXML(String fileName, String streamId, String separateChar,
                                       AxisConfiguration axisConfiguration) {

        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(EventSimulatorConstant.ROOT_ELEMENT_NAME);
            rootElement.setAttribute("type", "csv");
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
    public void uploadService(UploadedFileItem[] fileItems, AxisConfiguration axisConfiguration)
            throws AxisFault {

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

    private boolean validateAttributeValues(List<Attribute> attributeList, String[] valueArray)
            throws AxisFault {

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
                        if(fis!=null) {
                            fis.close();
                        }
                        if (bis != null) {
                            bis.close();
                        }
                        if (dis != null) {
                            dis.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    public void addDataSourceTableAndStreamInfo(DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo) {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificDataSourceInfoMap.containsKey(tenantID)) {
            HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap.get(tenantID);
            dataSourceTableAndStreamInfoMap.put(dataSourceTableAndStreamInfo.getConfigurationName(), dataSourceTableAndStreamInfo);
        } else {
            HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = new HashMap<String, DataSourceTableAndStreamInfo>();
            dataSourceTableAndStreamInfoMap.put(dataSourceTableAndStreamInfo.getConfigurationName(), dataSourceTableAndStreamInfo);
            tenantSpecificDataSourceInfoMap.put(tenantID, dataSourceTableAndStreamInfoMap);

        }


    }

    @Override
    public List<DataSourceTableAndStreamInfo> getAllDataSourceInfo() {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, DataSourceTableAndStreamInfo> DataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap.get(tenantID);
        if (DataSourceTableAndStreamInfoMap != null) {
            return new ArrayList<DataSourceTableAndStreamInfo>(DataSourceTableAndStreamInfoMap.values());
        }else {
            return null;
        }

    }

    @Override
    public void createConfigurationXMLForDataSource(String dataSourceConfigAndEventStreamInfo,AxisConfiguration axisConfiguration) throws AxisFault {

        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DATA_SOURCE_DEPLOY_DIRECTORY_PATH;

        try {
            JSONObject jsonConvertedInfo = new JSONObject(dataSourceConfigAndEventStreamInfo);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            String fileName= jsonConvertedInfo.getString(EventSimulatorConstant.CONFIGURATION_NAME);


            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(EventSimulatorConstant.ROOT_ELEMENT_NAME);
            rootElement.setAttribute("type", "database");
            rootElement.setAttribute("name", jsonConvertedInfo.getString(EventSimulatorConstant.CONFIGURATION_NAME));
            doc.appendChild(rootElement);


            Element dataSourceName = doc.createElement(EventSimulatorConstant.DATA_SOURCE_NAME);
            dataSourceName.appendChild(doc.createTextNode(jsonConvertedInfo.getString(EventSimulatorConstant.DATA_SOURCE_NAME)));
            rootElement.appendChild(dataSourceName);

            Element tableName = doc.createElement(EventSimulatorConstant.TABLE_NAME);
            tableName.appendChild(doc.createTextNode(jsonConvertedInfo.getString(EventSimulatorConstant.TABLE_NAME)));
            rootElement.appendChild(tableName);

            Element streamNameID = doc.createElement(EventSimulatorConstant.EVENT_STREAM_ID);
            streamNameID.appendChild(doc.createTextNode(jsonConvertedInfo.getString(EventSimulatorConstant.EVENT_STREAM_ID)));
            rootElement.appendChild(streamNameID);

            Element columnMappings = doc.createElement("columnMappings");

            JSONArray databaseColumnAndStreamAttributeInfo1 = jsonConvertedInfo.getJSONArray(EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO);
            for(int i=0; i< databaseColumnAndStreamAttributeInfo1.length(); i++){
                JSONObject temp = databaseColumnAndStreamAttributeInfo1.getJSONObject(i);

                Element columnMapping = doc.createElement("columnMapping");
                columnMapping.setAttribute(EventSimulatorConstant.COLUMN_NAME, temp.getString(EventSimulatorConstant.COLUMN_NAME));
                columnMapping.setAttribute(EventSimulatorConstant.STREAM_ATTRIBUTE_NAME, temp.getString(EventSimulatorConstant.STREAM_ATTRIBUTE_NAME));

                columnMappings.appendChild(columnMapping);
            }

            rootElement.appendChild(columnMappings);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            String absolutePath = path + File.separator + fileName + EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_PREFIX;

            StreamResult result = new StreamResult(new File(absolutePath));



            uploadXMLFile(axisConfiguration);

            transformer.transform(source, result);

            //addEventMappingDBConfiguration(fileName, streamId, separateChar);


        } catch (ParserConfigurationException e) {
            log.error(e);
        } catch (TransformerException e) {
            log.error(e);
        } catch (JSONException e) {
            log.error(e);
            e.printStackTrace();
        }


    }

    //@Override
    public void uploadXMLFile(AxisConfiguration axisConfiguration)
            throws AxisFault {

        String repo = axisConfiguration.getRepository().getPath();

        if (CarbonUtils.isURL(repo)) {
            throw new AxisFault("URL Repositories are not supported: " + repo);
        }

        String xmlDirectory = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;

        File csvDir = new File(xmlDirectory);

        if (!csvDir.exists() && !csvDir.mkdirs()) {
            throw new AxisFault("Fail to create the directory: " + csvDir.getAbsolutePath());
        }

        /*

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
        }*/

    }



    @Override
    public void deleteDBConfigFile(String fileName, AxisConfiguration axisConfiguration) throws AxisFault {



        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap.get(tenantID);

        fileName = fileName.replace(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_PREFIX,"");

        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoMap.get(fileName);
        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;

        String xmlFilePath = path + File.separator + dataSourceTableAndStreamInfo.getFileName();

        File xmlFile = new File(xmlFilePath);

        if (xmlFile.exists()) {
            dataSourceTableAndStreamInfoMap.remove(fileName);
            xmlFile.delete();
        }
    }

    @Override
    public String getEventStreamInfo(String fileName) throws AxisFault {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);

        fileName = fileName.replace(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_PREFIX,"");
        HashMap<String, DataSourceTableAndStreamInfo> dataSourceInfoMap = tenantSpecificDataSourceInfoMap.get(tenantId);

        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceInfoMap.get(fileName);

        String jsonFormattedAllInfo = "{\""+ EventSimulatorConstant.EVENT_STREAM_ID+"\":\"" + dataSourceTableAndStreamInfo.getEventStreamID() + "\",\""+EventSimulatorConstant.DATA_SOURCE_NAME+"\":\"" + dataSourceTableAndStreamInfo.getDataSourceName() + "\",\""+EventSimulatorConstant.TABLE_NAME+"\":\"" + dataSourceTableAndStreamInfo.getTableName() + "\", \""+EventSimulatorConstant.CONFIGURATION_NAME+"\":\"" + dataSourceTableAndStreamInfo.getConfigurationName() + "\",\""+EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO+"\":[";
        String jsonAttribute = "";

        String[][] columnAndStreamAttributeNames = dataSourceTableAndStreamInfo.getDataSourceColumnsAndTypes();

        StreamDefinition streamDefinition = getStreamDefinition(dataSourceTableAndStreamInfo.getEventStreamID());

        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();
        int q=0, r=0;

        //columnAndStreamAttributeNames[0] includes column names
        //columnAndStreamAttributeNames[1] includes mapping attribute names for columns
        for(int i=0; i<columnAndStreamAttributeNames[0].length; i++){
            if(i<metaAttributeList.size()){
                for(int j=0; j<columnAndStreamAttributeNames[0].length; j++){
                    if(metaAttributeList.get(i).getName().equalsIgnoreCase(columnAndStreamAttributeNames[1][j])){
                        jsonAttribute = jsonAttribute + "{\""+EventSimulatorConstant.STREAM_ATTRIBUTE_NAME+"\":\"" + metaAttributeList.get(i).getName() + "\",\""+EventSimulatorConstant.COLUMN_NAME+"\":\"" + columnAndStreamAttributeNames[0][j] + "\",\""+EventSimulatorConstant.COLUMN_TYPE+"\":\"" + metaAttributeList.get(i).getType() + "\"},";
                    }
                }

                q=0;
            }else if(i>=metaAttributeList.size() && q<correlationAttributeList.size()){

                for(int j=0; j<columnAndStreamAttributeNames[0].length; j++){
                    if(correlationAttributeList.get(q).getName().equalsIgnoreCase(columnAndStreamAttributeNames[1][j])){
                        jsonAttribute = jsonAttribute + "{\""+EventSimulatorConstant.STREAM_ATTRIBUTE_NAME+"\":\"" + correlationAttributeList.get(q).getName() + "\",\""+EventSimulatorConstant.COLUMN_NAME+"\":\"" + columnAndStreamAttributeNames[0][j] + "\",\""+EventSimulatorConstant.COLUMN_TYPE+"\":\"" + correlationAttributeList.get(q).getType() + "\"},";
                    }
                }
                q++;
                r=0;
            }else{
                for(int j=0; j<columnAndStreamAttributeNames[0].length; j++){
                    if(payloadAttributeList.get(r).getName().equalsIgnoreCase(columnAndStreamAttributeNames[1][j])){
                        jsonAttribute = jsonAttribute + "{\""+EventSimulatorConstant.STREAM_ATTRIBUTE_NAME+"\":\"" + payloadAttributeList.get(r).getName() + "\",\""+EventSimulatorConstant.COLUMN_NAME+"\":\"" + columnAndStreamAttributeNames[0][j] + "\",\""+EventSimulatorConstant.COLUMN_TYPE+"\":\"" + payloadAttributeList.get(r).getType() + "\"},";
                    }
                }
                r++;
            }
        }
        jsonFormattedAllInfo = jsonFormattedAllInfo + jsonAttribute + "]}";
        return jsonFormattedAllInfo;
    }

    @Override
    public void sendEventsViaDB(JSONObject allInfo, String getPreparedSelectStatement) throws AxisFault{

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Thread eventCreator = new Thread(new EventCreationForDB(tenantId, allInfo, getPreparedSelectStatement));
        eventCreator.start();


    }

    private class EventCreationForDB implements Runnable {

        private ResultSet resultSet = null;
        private int tenantId;
        private JSONObject allInfo;
        private DataSource datasource;
        private JSONArray columnAndAttributeMapping;

        public EventCreationForDB(int tenantId, JSONObject allInfo, String getPreparedSelectStatement) throws AxisFault {
            this.tenantId = tenantId;
            this.allInfo = allInfo;

            CarbonDataSource carbonDataSource;
            String dataSourceName;
            try {
                dataSourceName = allInfo.getString(EventSimulatorConstant.DATA_SOURCE_NAME);

                try {
                    carbonDataSource = EventSimulatorValueHolder.getDataSourceService().getDataSource(dataSourceName);
                    datasource = (DataSource) carbonDataSource.getDSObject();
                    Connection con;
                    Statement stmt;

                    try {
                        con = datasource.getConnection();
                        stmt = con.createStatement();
                        resultSet = stmt.executeQuery(getPreparedSelectStatement);

                    } catch (SQLException e) {
                        log.error(EventSimulatorConstant.CONNECTION_STRING_NOT_FOUND + dataSourceName, e);
                        throw new AxisFault(EventSimulatorConstant.CONNECTION_STRING_NOT_FOUND + dataSourceName, e);
                    }
                } catch (DataSourceException e) {
                    log.error(EventSimulatorConstant.DATA_SOURCE_NOT_FOUND_FOR_DATA_SOURCE_NAME + dataSourceName, e);
                    throw new AxisFault(EventSimulatorConstant.DATA_SOURCE_NOT_FOUND_FOR_DATA_SOURCE_NAME + dataSourceName, e);

                }


            } catch (JSONException e) {
                log.error(EventSimulatorConstant.JSON_EXCEPTION, e);
                throw new AxisFault(EventSimulatorConstant.JSON_EXCEPTION, e);

            }
        }

        @Override
        public void run(){

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId, true);

                columnAndAttributeMapping = allInfo.getJSONArray(EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO);
                StreamDefinition streamDefinition = getStreamDefinition(allInfo.getString(EventSimulatorConstant.EVENT_STREAM_ID));

                List<Attribute> metaAttributeList = streamDefinition.getMetaData();
                List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
                List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();


                //columnAndStreamAttributeNames[0] includes column names
                //columnAndStreamAttributeNames[1] includes mapping attribute names for columns

                while (resultSet.next()) {

                    Event event = new Event();
                    event.setStreamDefinition(streamDefinition);

                    String[] attributeValues = new String[columnAndAttributeMapping.length()];
                    int noOfAttributes = 0;

                    int j=0;

                    String columnName = columnAndAttributeMapping.getJSONObject(j).getString(EventSimulatorConstant.COLUMN_NAME);

                    if(metaAttributeList != null){
                        for (int i = 0; i < metaAttributeList.size(); i++) {
                            if (metaAttributeList.get(i).getType() == AttributeType.INT) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getInt(columnName));
                            } else if (metaAttributeList.get(i).getType() == AttributeType.LONG) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getLong(columnName));
                            } else if (metaAttributeList.get(i).getType() == AttributeType.FLOAT) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getFloat(columnName));
                            } else if (metaAttributeList.get(i).getType() == AttributeType.DOUBLE) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getDouble(columnName));
                            } else if (metaAttributeList.get(i).getType() == AttributeType.STRING) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getString(columnName));
                            } else if (metaAttributeList.get(i).getType() == AttributeType.BOOL) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getBoolean(columnName));
                            }

                            if(j<columnAndAttributeMapping.length()-1){
                                noOfAttributes++;
                                j++;
                                columnName = columnAndAttributeMapping.getJSONObject(j).getString(EventSimulatorConstant.COLUMN_NAME);

                            }

                        }

                    }
                    if(correlationAttributeList!=null){//noOfAttributes-metaAttributeList.size()<correlationAttributeList.size()){
                        for (int i = 0; i< correlationAttributeList.size(); i++) {
                            if (correlationAttributeList.get(i).getType() == AttributeType.INT) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getInt(columnName));
                            } else if (correlationAttributeList.get(i).getType() == AttributeType.LONG) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getLong(columnName));
                            } else if (correlationAttributeList.get(i).getType() == AttributeType.FLOAT) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getFloat(columnName));
                            } else if (correlationAttributeList.get(i).getType() == AttributeType.DOUBLE) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getDouble(columnName));
                            } else if (correlationAttributeList.get(i).getType() == AttributeType.STRING) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getString(columnName));
                            } else if (correlationAttributeList.get(i).getType() == AttributeType.BOOL) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getBoolean(columnName));
                            }
                            if(j<columnAndAttributeMapping.length()-1){
                                noOfAttributes++;
                                j++;
                                columnName = columnAndAttributeMapping.getJSONObject(j).getString(EventSimulatorConstant.COLUMN_NAME);

                            }
                        }

                    }
                    if(payloadAttributeList != null){ //noOfAttributes -metaAttributeList.size()-correlationAttributeList.size()<payloadAttributeList.size()){
                        for (int i = 0; i< payloadAttributeList.size(); i++) {
                            if (payloadAttributeList.get(i).getType() == AttributeType.INT) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getInt(columnName));
                            } else if (payloadAttributeList.get(i).getType() == AttributeType.LONG) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getLong(columnName));
                            } else if (payloadAttributeList.get(i).getType() == AttributeType.FLOAT) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getFloat(columnName));
                            } else if (payloadAttributeList.get(i).getType() == AttributeType.DOUBLE) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getDouble(columnName));
                            } else if (payloadAttributeList.get(i).getType() == AttributeType.STRING) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getString(columnName));
                            } else if (payloadAttributeList.get(i).getType() == AttributeType.BOOL) {
                                attributeValues[noOfAttributes] = String.valueOf(resultSet.getBoolean(columnName));
                            }
                            if(j<columnAndAttributeMapping.length()-1){
                                noOfAttributes++;
                                j++;
                                columnName = columnAndAttributeMapping.getJSONObject(j).getString(EventSimulatorConstant.COLUMN_NAME);

                            }
                        }
                    }
                    event.setAttributeValues(attributeValues);
                    sendEvent(event);
                }
            } catch (SQLException e) {
                log.error("database exception occurred: "+ e.getMessage(), e);
            } catch (JSONException e) {
                log.error(EventSimulatorConstant.JSON_EXCEPTION, e);
                //throw new AxisFault(EventSimulatorConstant.JSON_EXCEPTION, e);
            } catch (AxisFault axisFault) {
                log.error(axisFault.getMessage(), axisFault);
            }
        }
    }
}
