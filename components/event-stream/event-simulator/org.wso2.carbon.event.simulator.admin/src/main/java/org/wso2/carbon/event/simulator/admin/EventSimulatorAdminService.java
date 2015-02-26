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
package org.wso2.carbon.event.simulator.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.admin.internal.util.EventSimulatorAdminvalueHolder;
import org.wso2.carbon.event.simulator.admin.internal.util.EventSimulatorDataSourceConstants;
import org.wso2.carbon.event.simulator.admin.internal.util.EventSimulatorDataSourceInfo;
import org.wso2.carbon.event.simulator.core.*;

import org.wso2.carbon.event.simulator.admin.internal.ExecutionInfo;

import java.util.Collection;
import java.util.List;

//import org.wso2.carbon.eventsimulator.core.EventSimulator;


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

    public void sendDBConfigFileNameToSimulate(String fileName) throws AxisFault {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();
        String jsonFormattedDBConfigAndColumnAndStreamAttributeDetails = eventSimulator.getEventStreamInfo(fileName);
        JSONObject jsonConvertedInfo;
        ExecutionInfo executionInfo;
        try {
            jsonConvertedInfo = new JSONObject(jsonFormattedDBConfigAndColumnAndStreamAttributeDetails);
            executionInfo = EventSimulatorDataSourceInfo.getInitializedDatabaseExecutionInfo(jsonConvertedInfo);
        } catch (JSONException e) {
            throw new AxisFault("JSON exception when converting result of information retrieved by file name.");
        }
        eventSimulator.sendEventsViaDB(jsonConvertedInfo, executionInfo.getPreparedSelectStatement());
    }

    public void deleteDBConfigFile(String fileName) throws AxisFault {
        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();
        ConfigurationContext configurationContext = getConfigContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        eventSimulator.deleteDBConfigFile(fileName, axisConfiguration);
    }

    public String testSimulateRDBMSDataSourceConnection(String eventStreamDataSourceColumnNamesAndTypeInfo) throws AxisFault{
        ExecutionInfo executionInfo;

        try {
            JSONObject jsonConvertedInfo = new JSONObject(eventStreamDataSourceColumnNamesAndTypeInfo);
            try{
                executionInfo = EventSimulatorDataSourceInfo.getInitializedDatabaseExecutionInfo(jsonConvertedInfo);
                if(executionInfo!=null){
                    String result = "{\""+ EventSimulatorConstant.EVENT_STREAM_ID+"\":\"" + jsonConvertedInfo.getString(EventSimulatorConstant.EVENT_STREAM_ID)
                            + "\",\""+EventSimulatorConstant.EVENT_STREAM_NAME+"\":\"" + jsonConvertedInfo.getString(EventSimulatorConstant.EVENT_STREAM_NAME)
                            + "\",\""+EventSimulatorConstant.DATA_SOURCE_NAME+"\":\"" + jsonConvertedInfo.getString(EventSimulatorConstant.DATA_SOURCE_NAME)
                            + "\",\""+EventSimulatorConstant.TABLE_NAME+"\":\"" + jsonConvertedInfo.getString(EventSimulatorConstant.TABLE_NAME)
                            + "\",\""+EventSimulatorConstant.CONFIGURATION_NAME+"\":\"" + jsonConvertedInfo.getString(EventSimulatorConstant.CONFIGURATION_NAME)
                            + "\",\""+EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO+"\":" + jsonConvertedInfo.getJSONArray(EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO)
                            + "}";

                    return result;
                }
            }catch(AxisFault e){
                throw e;
            }
        } catch (JSONException e) {
            log.error(EventSimulatorDataSourceConstants.JSON_EXCEPTION, e);
            throw new AxisFault(EventSimulatorDataSourceConstants.JSON_EXCEPTION, e);
        }
        return "failed";
    }

    public void saveDataSourceConfigDetails(String dataSourceConfigAndEventStreamInfo) throws AxisFault {

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        ConfigurationContext configurationContext = getConfigContext();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();

        eventSimulator.createConfigurationXMLForDataSource(dataSourceConfigAndEventStreamInfo, axisConfiguration);

    }

    public DataSourceTableAndStreamInfoDto[] getAllDataSourceTableAndStreamInfo() {


        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        try {
            List<DataSourceTableAndStreamInfo> DataSourceTableAndStreamInfoList = eventSimulator.getAllDataSourceInfo();

            if (DataSourceTableAndStreamInfoList != null) {

                DataSourceTableAndStreamInfoDto[] DataSourceTableAndStreamInfoDtoArray = new DataSourceTableAndStreamInfoDto[DataSourceTableAndStreamInfoList.size()];

                int index = 0;
                for (DataSourceTableAndStreamInfo dataSourceTableAndStreamInfo : DataSourceTableAndStreamInfoList) {

                    DataSourceTableAndStreamInfoDtoArray[index] = new DataSourceTableAndStreamInfoDto();
                    DataSourceTableAndStreamInfoDtoArray[index].setConfigurationName(dataSourceTableAndStreamInfo.getConfigurationName());
                    DataSourceTableAndStreamInfoDtoArray[index].setDataSourceName(dataSourceTableAndStreamInfo.getDataSourceName());
                    DataSourceTableAndStreamInfoDtoArray[index].setTableName(dataSourceTableAndStreamInfo.getTableName());
                    DataSourceTableAndStreamInfoDtoArray[index].setEventStreamID(dataSourceTableAndStreamInfo.getEventStreamID());
                    DataSourceTableAndStreamInfoDtoArray[index].setColumnNames(dataSourceTableAndStreamInfo.getDataSourceColumnsAndTypes()[0]);
                    DataSourceTableAndStreamInfoDtoArray[index].setStreamAtrributeNames(dataSourceTableAndStreamInfo.getDataSourceColumnsAndTypes()[1]);
                    DataSourceTableAndStreamInfoDtoArray[index].setFileName(dataSourceTableAndStreamInfo.getFileName());
                    DataSourceTableAndStreamInfoDtoArray[index].setFilePath(dataSourceTableAndStreamInfo.getFilePath());

                    index++;
                }

                return DataSourceTableAndStreamInfoDtoArray;
            } else {
                return new DataSourceTableAndStreamInfoDto[0];
            }

        } catch (Exception e) {
            log.error(e);
        }

        return new DataSourceTableAndStreamInfoDto[0];
    }

}
