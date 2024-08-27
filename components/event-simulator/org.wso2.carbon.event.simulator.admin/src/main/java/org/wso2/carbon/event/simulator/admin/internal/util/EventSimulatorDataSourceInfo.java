/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.simulator.admin.internal.util;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.event.simulator.admin.internal.ExecutionInfo;
import org.wso2.carbon.event.simulator.admin.internal.jaxbMappings.Element;
import org.wso2.carbon.event.simulator.admin.internal.jaxbMappings.Mapping;
import org.wso2.carbon.event.simulator.admin.internal.jaxbMappings.Mappings;
import org.wso2.carbon.event.simulator.core.EventSimulatorConstant;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.utils.CarbonUtils;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates database table information Construct all the queries and assign to executionInfo instance
 */
public class EventSimulatorDataSourceInfo {

    private static Map<String, Map<String, String>> dbTypeMappings;
    private static final Log log = LogFactory.getLog(EventSimulatorDataSourceInfo.class);

    /**
     * Populate xml values to Jaxb mapping classes
     */
    private static void populateJaxbMappings() throws AxisFault {

        JAXBContext jaxbContext;
        dbTypeMappings = new HashMap<String, Map<String, String>>();
        try {
            jaxbContext = JAXBContext.newInstance(Mappings.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String path = CarbonUtils.getCarbonConfigDirPath() + File.separator
                    + EventSimulatorDataSourceConstants.GENERIC_RDBMS_FILE_SPECIFIC_PATH
                    + EventSimulatorDataSourceConstants.GENERIC_RDBMS_FILE_NAME;
            File configFile = new File(path);
            if (!configFile.exists()) {
                throw new AxisFault("The " + EventSimulatorDataSourceConstants.GENERIC_RDBMS_FILE_NAME
                        + " can not found in " + path);
            }
            Mappings mappings = (Mappings) unmarshaller.unmarshal(configFile);
            Map<String, Mapping> dbMap = new HashMap<String, Mapping>();
            List<Mapping> mappingList = mappings.getMapping();

            for (Mapping mapping : mappingList) {
                dbMap.put(mapping.getDb(), mapping);
            }

            //Constructs a map to contain all db wise elements and there values
            for (Mapping mapping : mappingList) {
                if (mapping.getDb() != null) {
                    Mapping defaultMapping = dbMap.get(null);
                    Mapping specificMapping = dbMap.get(mapping.getDb());
                    List<Element> defaultElementList = defaultMapping.getElements().getElementList();
                    Map<String, String> elementMappings = new HashMap<String, String>();
                    for (Element element : defaultElementList) {
                        //Check if the mapping is present in the specific mapping
                        Element elementDetails = null;
                        if (specificMapping.getElements().getElementList() != null) {
                            elementDetails = specificMapping.getElements().getType(element.getKey());
                        }
                        //If a specific mapping is not found then use the default mapping
                        if (elementDetails == null) {
                            elementDetails = defaultMapping.getElements().getType(element.getKey());
                        }
                        elementMappings.put(elementDetails.getKey(), elementDetails.getValue());
                    }
                    dbTypeMappings.put(mapping.getDb(), elementMappings);
                }
            }
        } catch (JAXBException e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * Validates database table information Construct all the queries and assign to executionInfo instance
     *
     * @param tableAndAttributeMappingJsonObj
     *         JSONObject which contains dataSource, event stream, configuration name,
     *         table name, delay between events in milliseconds,
     *         table columns and mapping stream attributes and types
     */
    public static ExecutionInfo getInitializedDatabaseExecutionInfo(JSONObject tableAndAttributeMappingJsonObj) throws AxisFault {
        Connection con;
        String dbName;
        Statement stmt;

        populateJaxbMappings();

        ExecutionInfo executionInfo = new ExecutionInfo();
        String dataSourceName;
        try {
            dataSourceName = tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.DATA_SOURCE_NAME);
            String tableName = tableAndAttributeMappingJsonObj.getString(EventSimulatorConstant.TABLE_NAME);

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

                    String isTableExistQuery = elementMappings.get("isTableExist").replace(
                            EventSimulatorDataSourceConstants.GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME, tableName);
                    executionInfo.setPreparedTableExistenceCheckStatement(isTableExistQuery);

                    try {
                        stmt = con.createStatement();
                        stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
                        String getColumnsQuery = "";

                        boolean addedFirstColumn = false;
                        JSONArray attributeColumnMappingArray = tableAndAttributeMappingJsonObj.getJSONArray(
                                EventSimulatorConstant.DATABASE_COLUMNS_AND_STREAM_ATTRIBUTE_INFO);
                        for (int i = 0; i < attributeColumnMappingArray.length(); i++) {
                            JSONObject attributeAndMappingColumn = attributeColumnMappingArray.getJSONObject(i);
                            if (!getColumnsQuery.contains(attributeAndMappingColumn.getString(EventSimulatorConstant.COLUMN_NAME))) {
                                if (addedFirstColumn) {
                                    getColumnsQuery = getColumnsQuery + ",";
                                }
                                addedFirstColumn = true;
                                getColumnsQuery = getColumnsQuery + attributeAndMappingColumn.getString(EventSimulatorConstant.COLUMN_NAME);
                            }
                        }

                        String columnsDataTypeQuery = elementMappings.get("selectAllColumnsDataTypeInTable").replace(
                                EventSimulatorDataSourceConstants.GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME, tableName);
                        String selectQuery = elementMappings.get("selectFromTable").replace(
                                EventSimulatorDataSourceConstants.GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME, tableName).replace(
                                EventSimulatorDataSourceConstants.GENERIC_RDBMS_ATTRIBUTE_COLUMNS, getColumnsQuery);

                        executionInfo.setPreparedCheckTableColumnsDataTypeStatement(columnsDataTypeQuery);
                        executionInfo.setPreparedSelectStatement(selectQuery);

                        int columnAndDataTypeCount = 0;

                        columnsDataTypeQuery = executionInfo.getPreparedCheckTableColumnsDataTypeStatement();
                        //to check validity of entered columns
                        ResultSet rs = stmt.executeQuery(columnsDataTypeQuery);
                        while (rs.next()) {
                            String tableVariable = rs.getString(1);
                            for (int j = 0; j < attributeColumnMappingArray.length(); j++) {
                                JSONObject mappingAttributeAndColumn = attributeColumnMappingArray.getJSONObject(j);
                                if (mappingAttributeAndColumn.getString(EventSimulatorConstant.COLUMN_NAME)
                                        .equalsIgnoreCase(tableVariable)) {
                                    columnAndDataTypeCount++;
                                }
                            }
                        }

                        if (columnAndDataTypeCount < attributeColumnMappingArray.length()) {
                            log.error("Entered Column name(s) are nt valid in " + tableName);
                            throw new AxisFault("Entered Column name(s) are nt valid in " + tableName);
                        }

                        rs = stmt.executeQuery(executionInfo.getPreparedSelectStatement());
                        if (!rs.next()) {
                            log.error(tableName + " table does not contain data");
                            throw new AxisFault(tableName + " table does not contain data");
                        }
                        cleanupConnections(stmt, con, rs);
                    } catch (SQLException e) {
                        log.error(tableName + " table does not exist or no data", e);
                        throw new AxisFault(tableName + " table does not exist or no data", e);
                    }
                } catch (SQLException e) {
                    log.error("Exception when getting connection string for : " + dataSourceName, e);
                    throw new AxisFault("Exception when getting connection string for : " + dataSourceName, e);
                }

            } catch (DataSourceException e) {
                log.error("There is no any data source found named: " + dataSourceName, e);
                throw new AxisFault("There is no any data source found named: " + dataSourceName, e);

            }
        } catch (JSONException e) {
            log.error("Created JSON formatted string with attribute mapping information is not valid", e);
            throw new AxisFault("Created JSON formatted string with attribute mapping information is not valid", e);
        }

        return executionInfo;
    }

    /**
     * Closing connections
     *
     * @param stmt       object used for executing a static SQL statement
     * @param connection database connection
     */
    private static void cleanupConnections(Statement stmt, Connection connection, ResultSet resultSet) {
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
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("unable to close result set", e);
            }
        }
    }

}