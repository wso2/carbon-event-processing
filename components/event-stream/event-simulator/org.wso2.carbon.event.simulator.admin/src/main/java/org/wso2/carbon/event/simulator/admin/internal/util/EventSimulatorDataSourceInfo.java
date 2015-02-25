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
package org.wso2.carbon.event.simulator.admin.internal.util;

import org.apache.axis2.AxisFault;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.simulator.admin.exception.RDBMSConnectionException;
import org.wso2.carbon.event.simulator.admin.internal.ExecutionInfo;
import org.wso2.carbon.event.simulator.core.EventSimulatorConstant;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.utils.CarbonUtils;

import org.wso2.carbon.event.simulator.admin.internal.jaxbMappings.Element;
import org.wso2.carbon.event.simulator.admin.internal.jaxbMappings.Mapping;
import org.wso2.carbon.event.simulator.admin.internal.jaxbMappings.Mappings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.*;
import java.util.*;

public class EventSimulatorDataSourceInfo {
    /**
     * Construct all the queries and assign to executionInfo instance
     */

    private static Map<String,Map<String,String>> dbTypeMappings;
    private ResourceBundle resourceBundle;

    private static final Log log = LogFactory.getLog(EventSimulatorDataSourceInfo.class);


    /**
     * Populate xml values to Jaxb mapping classes
     */
    private static void populateJaxbMappings() throws AxisFault {

        JAXBContext jaxbContext;
        dbTypeMappings =new HashMap<String, Map<String, String>>();
        try {
            jaxbContext = JAXBContext.newInstance(Mappings.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_FILE_SPECIFIC_PATH + EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_FILE_NAME;
            File configFile = new File(path);
            if (!configFile.exists()) {
                throw new AxisFault("The " + EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_FILE_NAME + " can not found in " + path);
            }
            Mappings mappings = (Mappings) unmarshaller.unmarshal(configFile);
            Map<String,Mapping> dbMap=new HashMap<String, Mapping>();
            List<Mapping> mappingList=mappings.getMapping();

            for(Mapping mapping : mappingList){
                dbMap.put(mapping.getDb(),mapping);
            }

            //Constructs a map to contain all db wise elements and there values
            for(Mapping mapping : mappingList){
                if(mapping.getDb()!=null){
                    Mapping defaultMapping = dbMap.get(null);
                    Mapping specificMapping = dbMap.get(mapping.getDb());
                    List<Element> defaultElementList = defaultMapping.getElements().getElementList();
                    Map<String, String> elementMappings = new HashMap<String, String>();
                    for(Element element : defaultElementList){
                        //Check if the mapping is present in the specific mapping
                        Element elementDetails = null;
                        if( specificMapping.getElements().getElementList() != null){
                            elementDetails = specificMapping.getElements().getType(element.getKey());
                        }
                        //If a specific mapping is not found then use the default mapping
                        if(elementDetails == null){
                            elementDetails = defaultMapping.getElements().getType(element.getKey());
                        }
                        elementMappings.put(elementDetails.getKey(),elementDetails.getValue());
                    }
                    dbTypeMappings.put(mapping.getDb(), elementMappings);
                }
            }
        } catch (JAXBException e) {
            throw new AxisFault(e.getMessage(),e);
        }
    }

    public static ExecutionInfo getInitializedDatabaseExecutionInfo(JSONObject jsonDBConfigAndColumnStreamAttributeIngo) throws AxisFault {
        Connection con;
        String dbName;
        Statement stmt;

        populateJaxbMappings();

        ExecutionInfo executionInfo = new ExecutionInfo();
        String dataSourceName;
        try {
            dataSourceName = jsonDBConfigAndColumnStreamAttributeIngo.getString(EventSimulatorConstant.DATA_SOURCE_NAME);
            String tableName = jsonDBConfigAndColumnStreamAttributeIngo.getString(EventSimulatorConstant.TABLE_NAME);
            try {
                CarbonDataSource carbonDataSource = EventSimulatorAdminvalueHolder.getDataSourceService().getDataSource(dataSourceName);
                if (carbonDataSource != null) {
                    executionInfo.setDatasource((DataSource) carbonDataSource.getDSObject());
                }

                try {
                    con = executionInfo.getDatasource().getConnection();
                    DatabaseMetaData databaseMetaData = con.getMetaData();
                    dbName = databaseMetaData.getDatabaseProductName();

                    Map<String, String> elementMappings = dbTypeMappings.get(dbName.toLowerCase());

                    String isTableExistQuery = elementMappings.get("isTableExist").replace(EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME,tableName);
                    executionInfo.setPreparedTableExistenceCheckStatement(isTableExistQuery);

                    try {
                        stmt = con.createStatement();
                        stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
                        String getColumnsQuery = "";

                        boolean addedFirstColumn = false;
                        JSONArray dataSourceColumnsAndTypes = jsonDBConfigAndColumnStreamAttributeIngo.getJSONArray(EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO);
                        for (int i=0; i<dataSourceColumnsAndTypes.length();i++){
                            JSONObject temp = dataSourceColumnsAndTypes.getJSONObject(i);
                            if(!getColumnsQuery.contains(temp.getString(EventSimulatorConstant.COLUMN_NAME))){
                                if(addedFirstColumn){
                                    getColumnsQuery = getColumnsQuery + ",";
                                }
                                addedFirstColumn = true;
                                getColumnsQuery = getColumnsQuery + temp.getString(EventSimulatorConstant.COLUMN_NAME);

                            }
                        }

                        String getColumnsDataTypeQuery = elementMappings.get("selectAllColumnsDataTypeInTable").replace(EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME,tableName);
                        String getSelectQuery = elementMappings.get("selectFromTable").replace(EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME,tableName).replace(EventSimulatorDataSourceConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMNS,getColumnsQuery);

                        executionInfo.setPreparedCheckTableColomnsDataTypeStatement(getColumnsDataTypeQuery);
                        executionInfo.setPreparedSelectStatement(getSelectQuery);

                        int columndataTypeCorrectCount= 0;
                        boolean emptyTable;

                        emptyTable = true;
                        getColumnsDataTypeQuery = executionInfo.getPreparedCheckTableColomnsDataTypeStatement().replace("$tableName",tableName);
                        //to check columns and its data type matching
                        ResultSet rs = stmt.executeQuery(getColumnsDataTypeQuery);
                        while(rs.next()){

                            String temp2 = rs.getString(1);
                            String temp3 = rs.getString(2);

                            for(int j=0; j<dataSourceColumnsAndTypes.length();j++){
                                JSONObject temp = dataSourceColumnsAndTypes.getJSONObject(j);
                                String temp4;
                                if (temp.getString(EventSimulatorConstant.COLUMN_TYPE).equalsIgnoreCase("int")) {
                                    temp4 = elementMappings.get("integer");
                                } else{
                                    temp4 = elementMappings.get(temp.getString(EventSimulatorConstant.COLUMN_TYPE).toLowerCase());
                                    temp4 = temp4.replaceAll("[^a-zA-Z]", "");
                                }
                                if(temp.getString(EventSimulatorConstant.COLUMN_NAME).equalsIgnoreCase(temp2) && temp4.equalsIgnoreCase(temp3)){
                                    columndataTypeCorrectCount++;
                                }
                            }


                        }

                        if(columndataTypeCorrectCount < dataSourceColumnsAndTypes.length()){
                            log.error(tableName + EventSimulatorDataSourceConstants.DATA_TYPES_DOESNT_MATCH);
                            throw new AxisFault(tableName + EventSimulatorDataSourceConstants.DATA_TYPES_DOESNT_MATCH);
                        }

                        if(!emptyTable){
                            log.error(tableName + EventSimulatorDataSourceConstants.NO_DATA_IN_TABLE);
                            throw new AxisFault(tableName + EventSimulatorDataSourceConstants.NO_DATA_IN_TABLE);
                        }
                        cleanupConnections(stmt,con);
                    } catch (SQLException e) {
                        log.error(tableName + EventSimulatorDataSourceConstants.NO_TABLE_OR_NO_DATA, e);
                        throw new AxisFault(tableName + EventSimulatorDataSourceConstants.NO_TABLE_OR_NO_DATA, e);
                    }
                } catch (SQLException e) {
                    log.error(EventSimulatorDataSourceConstants.CONNECTION_STRING_NOT_FOUND + dataSourceName, e);
                    throw new AxisFault(EventSimulatorDataSourceConstants.CONNECTION_STRING_NOT_FOUND + dataSourceName, e);
                }

            } catch (DataSourceException e) {
                log.error(EventSimulatorDataSourceConstants.DATA_SOURCE_NOT_FOUND_FOR_DATA_SOURCE_NAME + dataSourceName, e);
                throw new AxisFault(EventSimulatorDataSourceConstants.DATA_SOURCE_NOT_FOUND_FOR_DATA_SOURCE_NAME + dataSourceName, e);

            }
        } catch (JSONException e) {
            log.error(EventSimulatorDataSourceConstants.JSON_EXCEPTION, e);
            throw new AxisFault(EventSimulatorDataSourceConstants.JSON_EXCEPTION, e);
        }

        return executionInfo;
    }

    /**
     * Replace attribute values with target build queries
     */
    /*
    public String constructQuery(String tableName, String query, StringBuilder column_types, StringBuilder columns, StringBuilder values, StringBuilder column_values, StringBuilder condition){

        if(query.contains(EventSimulatoRDBMSDataSourceConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME)){
            query = query.replace(EventSimulatoRDBMSDataSourceConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME,tableName);
        }
        return query;
    }*/

    private static void cleanupConnections(Statement stmt, Connection connection) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("unable to close statement", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("unable to close connection", e);
            }
        }
    }

}
