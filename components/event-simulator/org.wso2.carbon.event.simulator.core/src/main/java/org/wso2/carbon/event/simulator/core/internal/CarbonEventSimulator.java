/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.simulator.core.internal;

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
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.Mode;
import org.wso2.carbon.event.simulator.core.*;
import org.wso2.carbon.event.simulator.core.exception.EventSimulatorRuntimeException;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorUtil;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.utils.CarbonUtils;

import javax.activation.DataHandler;
import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.StringWriter;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.regex.Pattern;

public class CarbonEventSimulator implements EventSimulator {

    private static final Log log = LogFactory.getLog(CarbonEventSimulator.class);
    private HashMap<Integer, HashMap<String, CSVFileInfo>> tenantSpecificCSVFileInfoMap;
    private HashMap<Integer, HashMap<String, DataSourceTableAndStreamInfo>> tenantSpecificDataSourceInfoMap;
    private Map<Integer, Map<String, EventCreator>> tenantSpecificFileEventSimulatorMap;
    private Map<Integer, Map<String, EventCreatorForDB>> tenantSpecificDBEventSimulatorMap;
    private boolean isWorkerNode = true;
    private Mode mode;

    public CarbonEventSimulator() {
        tenantSpecificCSVFileInfoMap = new HashMap<Integer, HashMap<String, CSVFileInfo>>();
        tenantSpecificDataSourceInfoMap = new HashMap<Integer, HashMap<String, DataSourceTableAndStreamInfo>>();
        tenantSpecificFileEventSimulatorMap = new HashMap<>();
        tenantSpecificDBEventSimulatorMap = new HashMap<>();

        // EventManagementService has a cardinality of 1..1 with EventSimulatorService.
        // Therefore when event simulator is activated, EventManagementService will also be active.

        EventManagementService eventManagementService = EventSimulatorValueHolder.getEventManagementService();
        if (eventManagementService != null) {
            mode = eventManagementService.getManagementModeInfo().getMode();
            DistributedConfiguration distributedConfiguration = eventManagementService.getManagementModeInfo().getDistributedConfiguration();
            if (mode == Mode.Distributed) {
                if (distributedConfiguration != null) {
                    isWorkerNode = distributedConfiguration.isWorkerNode();
                }
            }
        }
    }

    @Override
    public Collection<StreamDefinition> getAllEventStreamDefinitions() {
        try {
            EventStreamService eventStreamService = EventSimulatorValueHolder.getEventStreamService();
            Collection<StreamDefinition> collection = new ArrayList<StreamDefinition>();
            for (StreamDefinition streamDefinition : eventStreamService.getAllStreamDefinitions()) {
                collection.add(streamDefinition);
            }
            return collection;
        } catch (EventStreamConfigurationException e) {
            log.error("Exception when retrieving event stream definitions", e);
        }
        return null;
    }

    @Override
    public void sendEvent(Event eventDetail) throws AxisFault {

        if (mode == Mode.Distributed && !isWorkerNode) {
            log.warn("Sending events via manager node in distributed mode is not allowed. " +
                    "Dropping event for stream: " + eventDetail.getStreamDefinition().getStreamId());
            return;
        }

        EventStreamService eventstreamservice = EventSimulatorValueHolder.getEventStreamService();
        StreamDefinition streamDefinition = eventDetail.getStreamDefinition();
        String[] attributeValues = eventDetail.getAttributeValues();
        Object[] dataObjects = new Object[attributeValues.length];
        List<Attribute> streamAttributeList = getStreamAttributeList(eventDetail.getStreamDefinition());

        if (validateAttributeValues(streamAttributeList, attributeValues)) {
            for (int i = 0; i < dataObjects.length; i++) {
                Attribute attribute = streamAttributeList.get(i);
                String attributeType = attribute.getType().toString();
                String attributeValue = attributeValues[i].trim();
                switch (attributeType) {
                    case EventSimulatorConstant.STRING:
                        dataObjects[i] = attributeValue;
                        break;
                    case EventSimulatorConstant.INT:
                        try {
                            int val = Integer.parseInt(attributeValue);
                            dataObjects[i] = val;
                        } catch (NumberFormatException e) {
                            throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected " + attribute.getType().toString() + " : " + e.getMessage(), e);
                        }
                        break;
                    case EventSimulatorConstant.LONG:
                        try {
                            long val = Long.parseLong(attributeValue);
                            dataObjects[i] = val;
                        } catch (NumberFormatException e) {
                            throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected  " + attribute.getType().toString() + " : " + e.getMessage(), e);
                        }
                        break;
                    case EventSimulatorConstant.DOUBLE:
                        try {
                            double val = Double.parseDouble(attributeValue);
                            dataObjects[i] = val;
                        } catch (NumberFormatException e) {
                            throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected  " + attribute.getType().toString() + " : " + e.getMessage(), e);
                        }
                        break;
                    case EventSimulatorConstant.FLOAT:
                        try {
                            float val = Float.parseFloat(attributeValue);
                            dataObjects[i] = val;
                        } catch (NumberFormatException e) {
                            throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected  " + attribute.getType().toString() + " : " + e.getMessage(), e);
                        }
                        break;
                    case EventSimulatorConstant.BOOLEAN:
                        if (attributeValue.equalsIgnoreCase("true") || attributeValue.equalsIgnoreCase("false")) {
                            boolean val = Boolean.parseBoolean(attributeValue);
                            dataObjects[i] = val;
                        } else {
                            throw new AxisFault("Incorrect value types for the attribute - " + attribute.getName() + ", expected " + attribute.getType().toString());
                        }
                        break;
                }
            }
        }
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if(null == EventSimulatorValueHolder.getEventProducerMap(tenantID)) {
            EventSimulatorValueHolder.createEventProducerMapForTenant(tenantID);
        }
        EventStreamProducer existingEventStreamProducer = EventSimulatorValueHolder.getEventProducerMap(tenantID).get(streamDefinition.getStreamId());
        if (existingEventStreamProducer != null ) {
            EventStreamProducer eventProducer = EventSimulatorValueHolder.getEventProducerMap(tenantID).get(streamDefinition.getStreamId());
            eventProducer.sendData(dataObjects);
        } else {
            EventStreamProducer eventStreamProducer = new EventStreamProducer();
            try {
                eventStreamProducer.setStreamID(streamDefinition.getStreamId());
                eventstreamservice.subscribe(eventStreamProducer);
            } catch (EventStreamConfigurationException e) {
                log.error("Exception occurred when subscribing to Event Stream service", e);
            }

            EventSimulatorValueHolder.getEventProducerMap(tenantID).put(streamDefinition.getStreamId(), eventStreamProducer);
            eventStreamProducer.sendData(dataObjects);
        }
    }


    @Override
    public List<CSVFileInfo> getAllCSVFileInfo() {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);
        if (csvFileInfoMap != null) {
            return new ArrayList<CSVFileInfo>(csvFileInfoMap.values());
        } else {
            return null;
        }
    }


    public void addCSVFileInfo(CSVFileInfo csvFileInfo) {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        csvFileInfo.setStatus(CSVFileInfo.Status.STOPPED);
        if (tenantSpecificCSVFileInfoMap.containsKey(tenantID)) {
            HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);
            csvFileInfoMap.put(csvFileInfo.getFileName(), csvFileInfo);
        } else {
            HashMap<String, CSVFileInfo> csvFileMap = new HashMap<String, CSVFileInfo>();
            csvFileMap.put(csvFileInfo.getFileName(), csvFileInfo);
            tenantSpecificCSVFileInfoMap.put(tenantID, csvFileMap);
        }
    }

    public void removeCSVFileInfo(String fileName) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificCSVFileInfoMap.containsKey(tenantID)) {
            HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);
            if (csvFileInfoMap != null) {
                CSVFileInfo oldCSVFileInfo = csvFileInfoMap.get(fileName);
                if (oldCSVFileInfo != null) {
                    CSVFileInfo newCSVFileInfo = new CSVFileInfo();
                    newCSVFileInfo.setFileName(oldCSVFileInfo.getFileName());
                    newCSVFileInfo.setFilePath(oldCSVFileInfo.getFilePath());
                    newCSVFileInfo.setStatus(CSVFileInfo.Status.STOPPED);
                    csvFileInfoMap.put(fileName, newCSVFileInfo);
                }
            }
        }
    }

    public void addEventMappingConfiguration(String fileName, String streamId, String separateChar,
                                             long delayBetweenEventsInMillis) {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);
        CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
        csvFileInfo.setStreamID(streamId);
        csvFileInfo.setSeparateCharacter(separateChar);
        csvFileInfo.setDelayBetweenEventsInMillis(delayBetweenEventsInMillis);
    }

    @Override
    public void createConfigurationXML(String fileName, String streamId, String separateChar,
                                       long delayBetweenEventsInMillis,
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

            Element eventSendingDelayElement = doc.createElement(EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES);
            eventSendingDelayElement.appendChild(doc.createTextNode(String.valueOf(delayBetweenEventsInMillis)));
            rootElement.appendChild(eventSendingDelayElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, EventSimulatorConstant.TRANSFORMER_OUTPUT_PROPERTY);
            DOMSource source = new DOMSource(doc);

            String configFileName = fileName.substring(0, fileName.length() - 4) + EventSimulatorConstant.CONFIGURATION_XML_SUFFIX;
            String absolutePath = path + File.separator + configFileName;
            EventSimulatorUtil.validatePath(fileName); //validating, because this fileName is a user-input.
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            transformer.transform(source, result);
            saveConfigurationXML(sw.toString(), configFileName, absolutePath);
            addEventMappingConfiguration(fileName, streamId, separateChar, delayBetweenEventsInMillis);

        } catch (ParserConfigurationException e) {
            log.error("Exception when parsing file event simulator configuration file", e);
        } catch (TransformerException e) {
            log.error("Exception when transforming file event simulator configuration file", e);
        }

    }

    @Override
    public void deleteFile(String fileName, AxisConfiguration axisConfiguration) throws AxisFault {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantID);
        CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
        File file = new File(csvFileInfo.getFilePath());
        if (!file.delete()) {
            throw new AxisFault("Failed to delete the file : " + csvFileInfo.getFileName() + " for tenant ID : " + tenantID);
        }
        Map<String, EventCreator> fileEventSimulatorMap = tenantSpecificFileEventSimulatorMap.get(tenantID);
        if (fileEventSimulatorMap != null) {
            fileEventSimulatorMap.remove(fileName);
        }

    }

    public HashMap<String, CSVFileInfo> getCSVFileInfoMap() {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return tenantSpecificCSVFileInfoMap.get(tenantID);
    }

    @Override
    public void sendEvents(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventCreator eventCreator = new EventCreator(fileName, tenantId);
        Thread eventCreatorThread = new Thread(eventCreator);
        Map<String, EventCreator> fileEventSimulatorMap = tenantSpecificFileEventSimulatorMap.get(tenantId);
        if (fileEventSimulatorMap == null) {
            fileEventSimulatorMap = new HashMap<>();
            tenantSpecificFileEventSimulatorMap.put(tenantId, fileEventSimulatorMap);
        }
        fileEventSimulatorMap.put(fileName, eventCreator);
        eventCreatorThread.start();
        Map<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantId);
        if (csvFileInfoMap != null) {
            CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
            if (csvFileInfo != null) {
                csvFileInfo.setStatus(CSVFileInfo.Status.STARTED);
            }
        }
    }

    @Override
    public void pauseEvents(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventCreator> fileEventSimulatorMap = tenantSpecificFileEventSimulatorMap.get(tenantId);
        EventCreator eventCreator = fileEventSimulatorMap.get(fileName);
        eventCreator.pause();
        Map<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantId);
        if (csvFileInfoMap != null) {
            CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
            if (csvFileInfo != null) {
                csvFileInfo.setStatus(CSVFileInfo.Status.PAUSED);
            }
        }
    }

    @Override
    public void resumeEvents(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventCreator> fileEventSimulatorMap = tenantSpecificFileEventSimulatorMap.get(tenantId);
        EventCreator eventCreator = fileEventSimulatorMap.get(fileName);
        eventCreator.resume();
        Map<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantId);
        if (csvFileInfoMap != null) {
            CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
            if (csvFileInfo != null) {
                csvFileInfo.setStatus(CSVFileInfo.Status.RESUMED);
            }
        }
    }

    @Override
    public void stopEvents(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventCreator> fileEventSimulatorMap = tenantSpecificFileEventSimulatorMap.get(tenantId);
        EventCreator eventCreator = fileEventSimulatorMap.get(fileName);
        eventCreator.stop();
        Map<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantId);
        if (csvFileInfoMap != null) {
            CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
            if (csvFileInfo != null) {
                csvFileInfo.setStatus(CSVFileInfo.Status.STOPPED);
            }
        }
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
            
            try {
                // validate file name
                EventSimulatorUtil.validateCSVFile(fileName, csvDirectory);
            } catch (EventSimulatorRuntimeException e) {
                throw new AxisFault(e.getMessage(), e);
            }

            if (uploadedFile.getFileType().equals("csv")) {
                try {
                    writeResource(uploadedFile.getDataHandler(), csvTemp, fileName, csvDir);
                } catch (IOException e) {
                    throw new AxisFault("IOError: Writing resource failed.", e);
                }
            } else {
                throw new AxisFault("Invalid file type : " + uploadedFile.getFileType() +
                        " .csv file type is expected");
            }
        }
    }


    private void writeResource(DataHandler dataHandler, String destPath, String fileName,
                               File csvDest) throws IOException {
        File tempDestinationFile = new File(destPath, fileName);
        FileOutputStream fos = null;
        File destinationFile = new File(csvDest, fileName);
        try {
            fos = new FileOutputStream(tempDestinationFile);
            /* File stream is copied to a temp directory in order handle hot deployment issue
               occurred in windows */
            dataHandler.writeTo(fos);
            FileUtils.copyFile(tempDestinationFile, destinationFile);

        } catch (FileNotFoundException e) {
            log.error("Cannot find the file that specified", e);
            throw e;
        } catch (IOException e) {
            log.error("Exception when reading the file", e);
            throw e;
        } finally {
            if (fos == null) {
                return;
            }
            try {
                fos.close();
            } catch (IOException e) {
                log.warn("Can't close file streams.", e);
            }
        }

        boolean isDeleted = tempDestinationFile.delete();
        if (!isDeleted) {
            log.warn("temp file: " + tempDestinationFile.getAbsolutePath() +
                    " deletion failed, scheduled deletion on server exit.");
            tempDestinationFile.deleteOnExit();
        }
    }

    private List<Attribute> getStreamAttributeList(StreamDefinition streamDefinition) {

        List<Attribute> attributeList = new ArrayList<Attribute>();

        if (streamDefinition != null) {
            if (streamDefinition.getMetaData() != null) {
                for (Attribute attribute : streamDefinition.getMetaData()) {
                    attributeList.add(attribute);
                }
            }

            if (streamDefinition.getCorrelationData() != null) {
                for (Attribute attribute : streamDefinition.getCorrelationData()) {
                    attributeList.add(attribute);
                }
            }

            if (streamDefinition.getPayloadData() != null) {
                for (Attribute attribute : streamDefinition.getPayloadData()) {
                    attributeList.add(attribute);
                }
            }
        }


        return attributeList;
    }

    private StreamDefinition getStreamDefinition(String streamId) {

        EventStreamService eventStreamService = EventSimulatorValueHolder.getEventStreamService();
        StreamDefinition streamDefinition = null;
        try {
            Collection<StreamDefinition> streamDefinitions = eventStreamService.getAllStreamDefinitions();
            for (StreamDefinition streamdef : streamDefinitions) {
                if (streamdef.getStreamId().equals(streamId)) {
                    streamDefinition = streamdef;
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Exception when retrieving the stream definition", e);
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

    private void saveConfigurationXML(String xmlContent, String configFileName,
                                             String filePath) {
        try {
            OutputStreamWriter writer = null;
            try {
                /* saveConfigurationXML contents to .xml file */
                File file = new File(filePath);

                writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

                // get the content in bytes
                writer.write(xmlContent);
                log.info("Event Simulator configuration file: " + configFileName + " saved in the filesystem");
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
        } catch (IOException e) {
            log.error("Error while saving " + configFileName, e);          //todo: throw proper exception
        }
    }


    class EventCreator implements Runnable {
        String fileName;
        int tenantId;
        private final Object lock = new Object();
        private volatile boolean isPaused = false;
        private volatile boolean isStopped = false;

        public EventCreator(String fileName, int tenantId) {
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
                long delayBetweenEventsInMillis = fileInfo.getDelayBetweenEventsInMillis();
                if (delayBetweenEventsInMillis <= 0) {
                    log.warn("Events will be sent continuously since the delay between events are set to "
                            + delayBetweenEventsInMillis + "milliseconds");
                    delayBetweenEventsInMillis = 0;
                }

                File file = new File(path);
                FileInputStream fileInputStream = null;
                BufferedInputStream bufferedInputStream = null;
                DataInputStream dataInputStream = null;
                StreamDefinition streamDefinition = getStreamDefinition(fileInfo.getStreamID());

                try {
                    fileInputStream = new FileInputStream(file);
                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                    dataInputStream = new DataInputStream(bufferedInputStream);
                    int rowNumber = 0;
                    while (dataInputStream.available() != 0) {
                        if (!isPaused) {
                            String eventValues = dataInputStream.readLine();
                            try {
                                /*
                                 * Pattern.quote() returns a literal pattern String for the specified String. Therefore
                                 * metacharacters or escape sequences in the input sequence will be given no special
                                 * meaning.
                                 */
                                String[] attributeValueList = eventValues
                                        .split(Pattern.quote(fileInfo.getSeparateCharacter()));
                                Event event = new Event();
                                event.setStreamDefinition(streamDefinition);
                                event.setAttributeValues(attributeValueList);
                                sendEvent(event);
                                if (delayBetweenEventsInMillis > 0) {
                                    Thread.sleep(delayBetweenEventsInMillis);
                                }
                            } catch (Exception e) {
                                log.error("Error in row " + rowNumber + "-failed to create an event " + e);
                                rowNumber++;
                                continue;
                            }
                            rowNumber++;
                        } else if (isStopped) {
                            break;
                        } else {
                            synchronized (lock) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    continue;
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    log.error("Exception occurred while reading the data file: " + path, e);
                } finally {
                    closedQuietly(path, dataInputStream, bufferedInputStream, fileInputStream);
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
                Map<String, CSVFileInfo> csvFileInfoMap = tenantSpecificCSVFileInfoMap.get(tenantId);
                if (csvFileInfoMap != null) {
                    CSVFileInfo csvFileInfo = csvFileInfoMap.get(fileName);
                    if (csvFileInfo != null) {
                        csvFileInfo.setStatus(CSVFileInfo.Status.STOPPED);
                    }
                }
            }
        }

        private void closedQuietly(String closingFile, Closeable... closeables) {
            if (closeables == null) {
                return;
            }
            for (Closeable closeable : closeables) {
                try {
                    if (closeable != null) {
                        closeable.close();
                    }
                } catch (IOException e) {
                    log.error("Exception occurred while closing the stream related to data file: " + closingFile, e);
                }
            }
        }

        public void pause() {
            isPaused = true;
        }

        public void resume() {
            isPaused = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        public void stop() {
            isPaused = true;
            isStopped = true;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

    }

    public void addDataSourceTableAndStreamInfo(
            DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificDataSourceInfoMap.containsKey(tenantID)) {
            HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap.get(
                    tenantID);
            dataSourceTableAndStreamInfoMap.put(dataSourceTableAndStreamInfo.getConfigurationName(),
                    dataSourceTableAndStreamInfo);
        } else {
            HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = new HashMap<String, DataSourceTableAndStreamInfo>();
            dataSourceTableAndStreamInfoMap.put(dataSourceTableAndStreamInfo.getConfigurationName(),
                    dataSourceTableAndStreamInfo);
            tenantSpecificDataSourceInfoMap.put(tenantID, dataSourceTableAndStreamInfoMap);
        }
    }

    public void removeDataSourceTableAndStreamInfo(String datasourceConfigFileName) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificDataSourceInfoMap.containsKey(tenantID)) {
            HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap.get(
                    tenantID);
            if (dataSourceTableAndStreamInfoMap != null) {
                dataSourceTableAndStreamInfoMap.remove(datasourceConfigFileName);
            }
        }
    }

    @Override
    public List<DataSourceTableAndStreamInfo> getAllDataSourceInfo() {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap
                .get(tenantID);
        if (dataSourceTableAndStreamInfoMap != null) {
            return new ArrayList<DataSourceTableAndStreamInfo>(dataSourceTableAndStreamInfoMap.values());
        } else {
            return null;
        }
    }

    @Override
    public void createConfigurationXMLForDataSource(String tableAndAttributeMappingInfo,
                                                    AxisConfiguration axisConfiguration)
            throws AxisFault {

        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DATA_SOURCE_DEPLOY_DIRECTORY_PATH;

        try {
            JSONObject tableAndAttributeMappingJsonObj = new JSONObject(tableAndAttributeMappingInfo);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            String fileName = tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.CONFIGURATION_NAME);
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(EventSimulatorConstant.ROOT_ELEMENT_NAME);
            rootElement.setAttribute("type", "database");
            rootElement.setAttribute("name", tableAndAttributeMappingJsonObj.getString(
                    EventSimulatorConstant.CONFIGURATION_NAME));
            doc.appendChild(rootElement);

            Element dataSourceName = doc.createElement(EventSimulatorConstant.DATA_SOURCE_NAME);
            dataSourceName.appendChild(doc.createTextNode(
                    tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.DATA_SOURCE_NAME)));
            rootElement.appendChild(dataSourceName);

            Element tableName = doc.createElement(EventSimulatorConstant.TABLE_NAME);
            tableName.appendChild(
                    doc.createTextNode(tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.TABLE_NAME)));
            rootElement.appendChild(tableName);

            Element streamNameID = doc.createElement(EventSimulatorConstant.EVENT_STREAM_ID);
            streamNameID.appendChild(doc.createTextNode(
                    tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.EVENT_STREAM_ID)));
            rootElement.appendChild(streamNameID);

            Element delayBetweenEventsInMilies = doc.createElement(
                    EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES);
            delayBetweenEventsInMilies.appendChild(doc.createTextNode(String.valueOf(
                    tableAndAttributeMappingJsonObj.getLong(EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES))));
            rootElement.appendChild(delayBetweenEventsInMilies);

            Element columnMappings = doc.createElement("columnMappings");

            JSONArray databaseColumnAndStreamAttributeInfo1 = tableAndAttributeMappingJsonObj.getJSONArray(
                    EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO);
            for (int i = 0; i < databaseColumnAndStreamAttributeInfo1.length(); i++) {
                JSONObject temp = databaseColumnAndStreamAttributeInfo1.getJSONObject(i);

                Element columnMapping = doc.createElement("columnMapping");
                columnMapping.setAttribute(EventSimulatorConstant.COLUMN_NAME,
                        temp.getString(EventSimulatorConstant.COLUMN_NAME));
                columnMapping.setAttribute(EventSimulatorConstant.STREAM_ATTRIBUTE_NAME,
                        temp.getString(EventSimulatorConstant.STREAM_ATTRIBUTE_NAME));

                columnMappings.appendChild(columnMapping);
            }

            rootElement.appendChild(columnMappings);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, EventSimulatorConstant.TRANSFORMER_OUTPUT_PROPERTY);
            DOMSource source = new DOMSource(doc);

            String absolutePath = path + File.separator + fileName + EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX;
            EventSimulatorUtil.validatePath(fileName);
            StreamResult result = new StreamResult(new File(absolutePath));
            uploadXMLFile(axisConfiguration);
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            log.error("Exception when parsing the DB event simulator configuration file", e);
        } catch (TransformerException e) {
            log.error("Exception when transforming the DB event simulator configuration file", e);
        } catch (JSONException e) {
            log.error("Exception occurred when manipulating JSON DB configuration", e);
        }
    }

    private void uploadXMLFile(AxisConfiguration axisConfiguration)
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
    }


    @Override
    public void deleteDBConfigFile(String fileName, AxisConfiguration axisConfiguration)
            throws AxisFault {

        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        HashMap<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoMap = tenantSpecificDataSourceInfoMap.get(
                tenantID);

        fileName = fileName.replace(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX, "");
        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoMap.get(fileName);
        String repo = axisConfiguration.getRepository().getPath();
        String path = repo + EventSimulatorConstant.DEPLOY_DIRECTORY_PATH;
        String xmlFilePath = path + File.separator + dataSourceTableAndStreamInfo.getFileName();
        EventSimulatorUtil.validatePath(fileName);

        File xmlFile = new File(xmlFilePath);
        if (xmlFile.exists()) {
            dataSourceTableAndStreamInfoMap.remove(fileName);
            xmlFile.delete();
            Map<String, EventCreatorForDB> dbEventSimulatorMap = tenantSpecificDBEventSimulatorMap.get(tenantID);
            if (dbEventSimulatorMap != null) {
                dbEventSimulatorMap.remove(fileName);
            }
        }
    }

    @Override
    public String createTableAndAttributeMappingInfo(String fileName) throws AxisFault {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
        fileName = fileName.replace(EventSimulatorConstant.DATA_SOURCE_CONFIGURATION_XML_SUFFIX, "");
        HashMap<String, DataSourceTableAndStreamInfo> dataSourceInfoMap = tenantSpecificDataSourceInfoMap.get(tenantId);
        DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceInfoMap.get(fileName);

        String jsonFormattedAllInfo = "{\"" +
                EventSimulatorConstant.EVENT_STREAM_ID + "\":\"" + dataSourceTableAndStreamInfo.getEventStreamID() +
                "\",\"" + EventSimulatorConstant.DATA_SOURCE_NAME + "\":\"" + dataSourceTableAndStreamInfo
                .getDataSourceName() +
                "\",\"" + EventSimulatorConstant.TABLE_NAME + "\":\"" + dataSourceTableAndStreamInfo.getTableName() +
                "\", \"" + EventSimulatorConstant.CONFIGURATION_NAME + "\":\"" + dataSourceTableAndStreamInfo
                .getConfigurationName() +
                "\", \"" + EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES + "\":" + dataSourceTableAndStreamInfo
                .getDelayBetweenEventsInMillis() +
                ",\"" + EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO + "\":[";

        String jsonAttribute = "";
        String[][] columnAndStreamAttributeNames = dataSourceTableAndStreamInfo.getDataSourceColumnsAndTypes();
        StreamDefinition streamDefinition = getStreamDefinition(dataSourceTableAndStreamInfo.getEventStreamID());
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();
        if (metaAttributeList == null) {
            metaAttributeList = new ArrayList<>(0);
        }
        if (correlationAttributeList == null) {
            correlationAttributeList = new ArrayList<>(0);
        }
        if (payloadAttributeList == null) {
            payloadAttributeList = new ArrayList<>(0);
        }

        int q = 0, r = 0;

        //columnAndStreamAttributeNames[0] includes column names
        //columnAndStreamAttributeNames[1] includes mapping attribute names for columns
        for (int i = 0; i < columnAndStreamAttributeNames[0].length; i++) {
            if (i < metaAttributeList.size()) {
                for (int j = 0; j < columnAndStreamAttributeNames[0].length; j++) {
                    if (metaAttributeList.get(i).getName().equalsIgnoreCase(columnAndStreamAttributeNames[1][j])) {
                        jsonAttribute = jsonAttribute + "{\"" + EventSimulatorConstant.STREAM_ATTRIBUTE_NAME + "\":\"" +
                                metaAttributeList.get(i).getName() + "\",\"" + EventSimulatorConstant.COLUMN_NAME + "\":\"" +
                                columnAndStreamAttributeNames[0][j] + "\",\"" + EventSimulatorConstant.COLUMN_TYPE + "\":\"" +
                                metaAttributeList.get(i).getType() + "\"},";
                    }
                }

                q = 0;
            } else if (i >= metaAttributeList.size() && q < correlationAttributeList.size()) {

                for (int j = 0; j < columnAndStreamAttributeNames[0].length; j++) {
                    if (correlationAttributeList.get(q).getName().equalsIgnoreCase(columnAndStreamAttributeNames[1][j])) {
                        jsonAttribute = jsonAttribute + "{\"" + EventSimulatorConstant.STREAM_ATTRIBUTE_NAME + "\":\"" +
                                correlationAttributeList.get(q).getName() + "\",\"" + EventSimulatorConstant.COLUMN_NAME + "\":\"" +
                                columnAndStreamAttributeNames[0][j] + "\",\"" + EventSimulatorConstant.COLUMN_TYPE + "\":\"" +
                                correlationAttributeList.get(q).getType() + "\"},";
                    }
                }
                q++;
                r = 0;
            } else {
                for (int j = 0; j < columnAndStreamAttributeNames[0].length; j++) {
                    if (payloadAttributeList.get(r).getName().equalsIgnoreCase(columnAndStreamAttributeNames[1][j])) {
                        jsonAttribute = jsonAttribute + "{\"" + EventSimulatorConstant.STREAM_ATTRIBUTE_NAME + "\":\"" +
                                payloadAttributeList.get(r).getName() + "\",\"" + EventSimulatorConstant.COLUMN_NAME + "\":\"" +
                                columnAndStreamAttributeNames[0][j] + "\",\"" + EventSimulatorConstant.COLUMN_TYPE + "\":\"" +
                                payloadAttributeList.get(r).getType() + "\"},";
                    }
                }
                r++;
            }
        }
        jsonFormattedAllInfo = jsonFormattedAllInfo + jsonAttribute + "]}";
        return jsonFormattedAllInfo;
    }

    @Override
    public void sendEventsViaDB(String fileName, JSONObject allInfo, String getPreparedSelectStatement)
            throws AxisFault {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventCreatorForDB eventCreatorForDB = new EventCreatorForDB(fileName, tenantId, allInfo, getPreparedSelectStatement);
        Thread eventCreatorThread = new Thread(eventCreatorForDB);
        Map<String, EventCreatorForDB> dbEventSimulatorMap = tenantSpecificDBEventSimulatorMap.get(tenantId);
        if (dbEventSimulatorMap == null) {
            dbEventSimulatorMap = new HashMap<>();
            tenantSpecificDBEventSimulatorMap.put(tenantId, dbEventSimulatorMap);
        }
        dbEventSimulatorMap.put(fileName, eventCreatorForDB);
        eventCreatorThread.start();

        Map<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoHashMap = tenantSpecificDataSourceInfoMap.get(tenantId);
        if (dataSourceTableAndStreamInfoHashMap != null) {
            DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoHashMap.get(fileName);
            if (dataSourceTableAndStreamInfo != null) {
                dataSourceTableAndStreamInfo.setStatus(DataSourceTableAndStreamInfo.Status.STARTED);
            }
        }
    }

    @Override
    public void pauseEventsViaDB(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventCreatorForDB> dbEventSimulatorMap = tenantSpecificDBEventSimulatorMap.get(tenantId);
        EventCreatorForDB eventCreator = dbEventSimulatorMap.get(fileName);
        eventCreator.pause();

        Map<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoHashMap = tenantSpecificDataSourceInfoMap.get(tenantId);
        if (dataSourceTableAndStreamInfoHashMap != null) {
            DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoHashMap.get(fileName);
            if (dataSourceTableAndStreamInfo != null) {
                dataSourceTableAndStreamInfo.setStatus(DataSourceTableAndStreamInfo.Status.PAUSED);
            }
        }
    }

    @Override
    public void resumeEventsViaDB(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventCreatorForDB> dbEventSimulatorMap = tenantSpecificDBEventSimulatorMap.get(tenantId);
        EventCreatorForDB eventCreator = dbEventSimulatorMap.get(fileName);
        eventCreator.resume();

        Map<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoHashMap = tenantSpecificDataSourceInfoMap.get(tenantId);
        if (dataSourceTableAndStreamInfoHashMap != null) {
            DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoHashMap.get(fileName);
            if (dataSourceTableAndStreamInfo != null) {
                dataSourceTableAndStreamInfo.setStatus(DataSourceTableAndStreamInfo.Status.RESUMED);
            }
        }
    }

    @Override
    public void stopEventsViaDB(String fileName) throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventCreatorForDB> dbEventSimulatorMap = tenantSpecificDBEventSimulatorMap.get(tenantId);
        EventCreatorForDB eventCreator = dbEventSimulatorMap.get(fileName);
        eventCreator.stop();

        Map<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoHashMap = tenantSpecificDataSourceInfoMap.get(tenantId);
        if (dataSourceTableAndStreamInfoHashMap != null) {
            DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoHashMap.get(fileName);
            if (dataSourceTableAndStreamInfo != null) {
                dataSourceTableAndStreamInfo.setStatus(DataSourceTableAndStreamInfo.Status.STOPPED);
            }
        }
    }


    class EventCreatorForDB implements Runnable {

        private String fileName;
        private ResultSet resultSet;
        private int tenantId;
        private JSONObject allInfo;
        private DataSource datasource;
        private JSONArray columnAndAttributeMapping;
        private long delayBetweenEventsInMillis;
        private final Object lock = new Object();
        private volatile boolean isPaused = false;
        private volatile boolean isStopped = false;

        public EventCreatorForDB(String fileName, int tenantId, JSONObject tableAndAttributeMappingJsonObj,
                                 String preparedSelectStatement)
                throws AxisFault {
            this.fileName = fileName;
            this.tenantId = tenantId;
            this.allInfo = tableAndAttributeMappingJsonObj;
            CarbonDataSource carbonDataSource;
            String dataSourceName;
            try {
                dataSourceName = tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.DATA_SOURCE_NAME);
                delayBetweenEventsInMillis = tableAndAttributeMappingJsonObj.getLong(
                        EventSimulatorConstant.DELAY_BETWEEN_EVENTS_IN_MILIES);
                if (delayBetweenEventsInMillis <= 0) {
                    log.warn("Events will be sent continuously since the delay between events are set to "
                            + delayBetweenEventsInMillis + "milliseconds");
                    delayBetweenEventsInMillis = 0;
                }
                try {
                    carbonDataSource = EventSimulatorValueHolder.getDataSourceService().getDataSource(dataSourceName);
                    datasource = (DataSource) carbonDataSource.getDSObject();

                    try (Connection con = datasource.getConnection(); Statement stmt = con.createStatement()) {
                        resultSet = stmt.executeQuery(preparedSelectStatement);

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
        public void run() {

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId, true);

                columnAndAttributeMapping = allInfo.getJSONArray(
                        EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO);
                StreamDefinition streamDefinition = getStreamDefinition(allInfo.getString(EventSimulatorConstant.EVENT_STREAM_ID));
                List<Attribute> metaAttributeList = streamDefinition.getMetaData();
                List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
                List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

                while (resultSet.next()) {
                    if (!isPaused) {
                        Event event = new Event();
                        event.setStreamDefinition(streamDefinition);
                        String[] attributeValues = new String[columnAndAttributeMapping.length()];
                        int noOfAttributes = 0;
                        int columnIndex = 0;

                        String columnName = columnAndAttributeMapping.getJSONObject(columnIndex).getString(
                                EventSimulatorConstant.COLUMN_NAME);

                        if (metaAttributeList != null) {
                            for (Attribute metaAttribute : metaAttributeList) {
                                if (metaAttribute.getType() == AttributeType.INT) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getInt(columnName));
                                } else if (metaAttribute.getType() == AttributeType.LONG) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getLong(columnName));
                                } else if (metaAttribute.getType() == AttributeType.FLOAT) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getFloat(columnName));
                                } else if (metaAttribute.getType() == AttributeType.DOUBLE) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getDouble(columnName));
                                } else if (metaAttribute.getType() == AttributeType.STRING) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getString(columnName));
                                } else if (metaAttribute.getType() == AttributeType.BOOL) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getBoolean(columnName));
                                }

                                if (columnIndex < columnAndAttributeMapping.length() - 1) {
                                    noOfAttributes++;
                                    columnIndex++;
                                    columnName = columnAndAttributeMapping.getJSONObject(columnIndex).getString(
                                            EventSimulatorConstant.COLUMN_NAME);

                                }
                            }
                        }
                        if (correlationAttributeList != null) {
                            for (Attribute correlationAttribute : correlationAttributeList) {
                                if (correlationAttribute.getType() == AttributeType.INT) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getInt(columnName));
                                } else if (correlationAttribute.getType() == AttributeType.LONG) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getLong(columnName));
                                } else if (correlationAttribute.getType() == AttributeType.FLOAT) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getFloat(columnName));
                                } else if (correlationAttribute.getType() == AttributeType.DOUBLE) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getDouble(columnName));
                                } else if (correlationAttribute.getType() == AttributeType.STRING) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getString(columnName));
                                } else if (correlationAttribute.getType() == AttributeType.BOOL) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getBoolean(columnName));
                                }
                                if (columnIndex < columnAndAttributeMapping.length() - 1) {
                                    noOfAttributes++;
                                    columnIndex++;
                                    columnName = columnAndAttributeMapping.getJSONObject(columnIndex).getString(
                                            EventSimulatorConstant.COLUMN_NAME);
                                }
                            }
                        }
                        if (payloadAttributeList != null) {
                            for (Attribute payloadAttribute : payloadAttributeList) {
                                if (payloadAttribute.getType() == AttributeType.INT) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getInt(columnName));
                                } else if (payloadAttribute.getType() == AttributeType.LONG) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getLong(columnName));
                                } else if (payloadAttribute.getType() == AttributeType.FLOAT) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getFloat(columnName));
                                } else if (payloadAttribute.getType() == AttributeType.DOUBLE) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getDouble(columnName));
                                } else if (payloadAttribute.getType() == AttributeType.STRING) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getString(columnName));
                                } else if (payloadAttribute.getType() == AttributeType.BOOL) {
                                    attributeValues[noOfAttributes] = String.valueOf(resultSet.getBoolean(columnName));
                                }
                                if (columnIndex < columnAndAttributeMapping.length() - 1) {
                                    noOfAttributes++;
                                    columnIndex++;
                                    columnName = columnAndAttributeMapping.getJSONObject(columnIndex).getString(
                                            EventSimulatorConstant.COLUMN_NAME);
                                }
                            }
                        }
                        event.setAttributeValues(attributeValues);
                        sendEvent(event);
                        if (delayBetweenEventsInMillis > 0) {
                            Thread.sleep(delayBetweenEventsInMillis);
                        }
                    } else if (isStopped) {
                        break;
                    } else {
                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                continue;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("database exception occurred: " + e.getMessage(), e);
            } catch (JSONException e) {
                log.error("JSON Exception occurred when manipulating ", e);
            } catch (AxisFault axisFault) {
                log.error(axisFault.getMessage(), axisFault);
            } catch (InterruptedException e) {
                log.error("Error when delaying sending events: " + e.getMessage(), e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
                Map<String, DataSourceTableAndStreamInfo> dataSourceTableAndStreamInfoHashMap = tenantSpecificDataSourceInfoMap.get(tenantId);
                if (dataSourceTableAndStreamInfoHashMap != null) {
                    DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo = dataSourceTableAndStreamInfoHashMap.get(fileName);
                    if (dataSourceTableAndStreamInfo != null) {
                        dataSourceTableAndStreamInfo.setStatus(DataSourceTableAndStreamInfo.Status.STOPPED);
                    }
                }
            }
        }

        public void pause() {
            isPaused = true;
        }

        public void resume() {
            isPaused = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        public void stop() {
            isPaused = true;
            isStopped = true;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}
