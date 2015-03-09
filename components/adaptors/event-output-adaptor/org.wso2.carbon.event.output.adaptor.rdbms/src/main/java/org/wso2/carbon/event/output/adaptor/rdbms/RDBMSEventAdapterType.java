/*
*  Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adaptor.rdbms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.rdbms.exception.RDBMSConnectionException;
import org.wso2.carbon.event.output.adaptor.rdbms.exception.RDBMSEventProcessingException;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.ExecutionInfo;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.ds.EventAdaptorValueHolder;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.jaxbMappings.Element;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.jaxbMappings.Mapping;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.jaxbMappings.Mappings;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.util.DecayTimer;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.util.RDBMSEventAdaptorConstants;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.utils.CarbonUtils;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class will Insert or Update/Insert values to selected RDBMS
 */

public final class RDBMSEventAdapterType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(RDBMSEventAdapterType.class);

    private static RDBMSEventAdapterType rdbmsEventAdaptor = new RDBMSEventAdapterType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ExecutionInfo>> tables;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<OutputEventAdaptorConfiguration,
            ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, ExecutionInfo>>> initialConfiguration;
    private Map<String, Map<String, String>> dbTypeMappings;

    private RDBMSEventAdapterType() {
        this.initialConfiguration = new ConcurrentHashMap<Integer, ConcurrentHashMap<OutputEventAdaptorConfiguration,
                ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, ExecutionInfo>>>();
        this.tables = new ConcurrentHashMap<String, ConcurrentHashMap<String, ExecutionInfo>>();
    }

    /**
     * @return rdbms event adaptor instance
     */
    public static RDBMSEventAdapterType getInstance() {
        return rdbmsEventAdaptor;
    }

    @Override
    protected String getName() {
        return RDBMSEventAdaptorConstants.ADAPTOR_TYPE_GENERIC_RDBMS;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.MAP);
        return supportOutputMessageTypes;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle
                .getBundle("org.wso2.carbon.event.output.adaptor.rdbms.i18n.Resources", Locale.getDefault());
        populateJaxbMappings();

    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    protected List<Property> getOutputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();
        Property datasourceName = new Property(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME);
        datasourceName.setDisplayName(
                resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME));
        datasourceName.setRequired(true);
        propertyList.add(datasourceName);
        return propertyList;
    }

    /**
     * @return output adaptor Message configuration property list
     */
    @Override
    protected List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        Property tableName = new Property(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_TABLE_NAME);
        tableName.setDisplayName(resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_TABLE_NAME));
        tableName.setRequired(true);
        propertyList.add(tableName);

        Property executionMode = new Property(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE);
        executionMode.setDisplayName(
                resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE));
        executionMode.setOptions(new String[] {
                resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE_INSERT),
                resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE_UPDATE) });
        executionMode.setHint(
                resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE_HINT));
        executionMode.setRequired(true);
        propertyList.add(executionMode);

        Property updateColumnKeys = new Property(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_UPDATE_KEYS);
        updateColumnKeys
                .setDisplayName(resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_UPDATE_KEYS));
        updateColumnKeys
                .setHint(resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_UPDATE_KEYS_HINT));
        propertyList.add(updateColumnKeys);

        return propertyList;
    }

    @Override
    protected void publish(
            OutputEventAdaptorMessageConfiguration outputEventMessageConfiguration,
            Object message, OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
            int tenantId) {

        ExecutionInfo executionInfo = null;
        String tableName = null;
        try {
            if (message instanceof Map) {

                tableName = outputEventMessageConfiguration.getOutputMessageProperties()
                        .get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_TABLE_NAME);
                String executionMode = outputEventMessageConfiguration.getOutputMessageProperties()
                        .get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE);
                String updateColumnKeys = outputEventMessageConfiguration.getOutputMessageProperties()
                        .get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_UPDATE_KEYS);

                ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, ExecutionInfo> outputMessageConfigurationMap;
                ConcurrentHashMap<OutputEventAdaptorConfiguration,
                        ConcurrentHashMap<OutputEventAdaptorMessageConfiguration,
                                ExecutionInfo>> outputAdapterConfigurationMap = initialConfiguration
                        .get(tenantId);

                if (outputAdapterConfigurationMap != null) {
                    outputMessageConfigurationMap = outputAdapterConfigurationMap.get(outputEventAdaptorConfiguration);
                    if (outputMessageConfigurationMap != null) {
                        executionInfo = outputMessageConfigurationMap.get(outputEventMessageConfiguration);
                    } else {
                        outputMessageConfigurationMap = new ConcurrentHashMap<OutputEventAdaptorMessageConfiguration,
                                ExecutionInfo>();
                        outputAdapterConfigurationMap
                                .putIfAbsent(outputEventAdaptorConfiguration, outputMessageConfigurationMap);
                        outputMessageConfigurationMap = outputAdapterConfigurationMap
                                .get(outputEventAdaptorConfiguration);
                    }
                } else {
                    outputAdapterConfigurationMap = new ConcurrentHashMap<OutputEventAdaptorConfiguration,
                            ConcurrentHashMap<OutputEventAdaptorMessageConfiguration, ExecutionInfo>>();
                    initialConfiguration.putIfAbsent(tenantId, outputAdapterConfigurationMap);
                    outputAdapterConfigurationMap = initialConfiguration.get(tenantId);

                    outputMessageConfigurationMap = new ConcurrentHashMap<OutputEventAdaptorMessageConfiguration,
                            ExecutionInfo>();
                    outputAdapterConfigurationMap
                            .putIfAbsent(outputEventAdaptorConfiguration, outputMessageConfigurationMap);
                    outputMessageConfigurationMap = outputAdapterConfigurationMap.get(outputEventAdaptorConfiguration);
                }

                if (executionInfo == null) {
                    executionInfo = new ExecutionInfo();
                    executionInfo.setDecayTimer(new DecayTimer());

                    outputMessageConfigurationMap.put(outputEventMessageConfiguration, executionInfo);
                    initializeDatabaseExecutionInfo(tableName, executionMode, updateColumnKeys, message,
                            outputEventAdaptorConfiguration, executionInfo);
                }
                executeProcessActions(message, executionInfo, tableName);
            } else {
                throw new OutputEventAdaptorEventProcessingException(
                        message.getClass().toString() + "is not a compatible type. Hence Event is dropped.");
            }
        } catch (RDBMSConnectionException e) {
            executionInfo.setIsConnectionLive(false);
            String dataSourceName = outputEventAdaptorConfiguration
                    .getOutputProperties().get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME);
            long timeLeftForNextActivation = executionInfo.getNextConnectionTime() - System.currentTimeMillis();
            log.error("Error while initializing connection for datasource " + dataSourceName
                    + "Reconnection will try from " + timeLeftForNextActivation + " milliseconds.", e);
            executionInfo.getDecayTimer().incrementPosition();

            if (executionInfo.getNextConnectionTime() == 0) {
                try {
                    executeProcessActions(message, executionInfo, tableName);
                } catch (RDBMSConnectionException e1) {
                    timeLeftForNextActivation = executionInfo.getNextConnectionTime() - System
                            .currentTimeMillis();
                    log.error("Error while initializing connection for datasource " + dataSourceName
                            + "Reconnection will try from " + timeLeftForNextActivation + " milliseconds.", e1);
                    executionInfo.getDecayTimer().incrementPosition();
                } catch (RDBMSEventProcessingException e2) {
                    log.error(e2.getMessage() + " Hence Event is dropped.", e2);
                }
            }
        } catch (RDBMSEventProcessingException e) {
            log.error(e.getMessage() + " Hence Event is dropped.", e);
        }
    }

    public void executeProcessActions(Object message, ExecutionInfo executionInfo, String tableName)
            throws RDBMSConnectionException, RDBMSEventProcessingException {

        if (!executionInfo.getIsConnectionLive()) {
            long nextConnectionTime = executionInfo.getNextConnectionTime();
            long currentTime = System.currentTimeMillis();
            if (currentTime >= nextConnectionTime) {
                createTableIfNotExist(executionInfo, tableName);
                executeDbActions(message, executionInfo);
                executionInfo.getDecayTimer().reset();
                executionInfo.setIsConnectionLive(true);
            } else {
                if (log.isDebugEnabled()) {
                    long timeLeftForNextActivation =
                            executionInfo.getNextConnectionTime() - currentTime;
                    log.debug("End point suspended hence dropping event. End point will be active after "
                            + timeLeftForNextActivation + " milliseconds.");
                }
            }
        } else {
            executeDbActions(message, executionInfo);
        }
    }

    public void executeDbActions(Object message, ExecutionInfo executionInfo)
            throws RDBMSConnectionException, RDBMSEventProcessingException {

        Connection con;
        PreparedStatement stmt = null;

        try {
            con = executionInfo.getDatasource().getConnection();
        } catch (SQLException e) {
            throw new RDBMSConnectionException(e);
        }

        Map<String, Object> map = (Map<String, Object>) message;

        boolean executeInsert = true;

        try {
            synchronized (this) {
                if (executionInfo.isUpdateMode()) {

                    stmt = con.prepareStatement(executionInfo.getPreparedUpdateStatement());
                    populateStatement(map, stmt, executionInfo.getUpdateQueryColumnOrder());
                    int updatedRows = stmt.executeUpdate();

                    if (stmt != null) {
                        stmt.close();
                    }

                    if (updatedRows > 0) {
                        executeInsert = false;
                    }
                }

                if (executeInsert) {
                    stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
                    populateStatement(map, stmt, executionInfo.getInsertQueryColumnOrder());
                    stmt.executeUpdate();
                }

            }
        } catch (SQLException e) {
            throw new RDBMSEventProcessingException(
                    "Cannot Execute Insert/Update Query for event " + message.toString() + " " + e.getMessage(), e);
        } finally {
            cleanupConnections(stmt, con);
        }
    }

    /**
     * Populating column values to table Insert query
     */
    private void populateStatement(Map<String, Object> map, PreparedStatement stmt, List<Attribute> colOrder)
            throws RDBMSEventProcessingException {
        Attribute attribute = null;

        try {
            for (int i = 0; i < colOrder.size(); i++) {
                attribute = colOrder.get(i);
                Object value = map.get(attribute.getName());
                if (value != null) {
                    switch (attribute.getType()) {
                    case INT:
                        stmt.setInt(i + 1, (Integer) value);
                        break;
                    case LONG:
                        stmt.setLong(i + 1, (Long) value);
                        break;
                    case FLOAT:
                        stmt.setFloat(i + 1, (Float) value);
                        break;
                    case DOUBLE:
                        stmt.setDouble(i + 1, (Double) value);
                        break;
                    case STRING:
                        stmt.setString(i + 1, (String) value);
                        break;
                    case BOOL:
                        stmt.setBoolean(i + 1, (Boolean) value);
                        break;
                    }
                } else {
                    throw new RDBMSEventProcessingException("Cannot Execute Insert/Update. Null value detected for " +
                            "attribute" + attribute.getName());
                }
            }
        } catch (SQLException e) {
            cleanupConnections(stmt, null);
            throw new RDBMSEventProcessingException("Cannot set value to attribute name " + attribute.getName() + ". " +
                    "Hence dropping the event." + e.getMessage(), e);
        }
    }

    /**
     * Populate xml values to Jaxb mapping classes
     */
    private void populateJaxbMappings() {

        JAXBContext jaxbContext;
        dbTypeMappings = new HashMap<String, Map<String, String>>();
        try {
            jaxbContext = JAXBContext.newInstance(Mappings.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            String path = CarbonUtils.getCarbonConfigDirPath() + File.separator
                    + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FILE_SPECIFIC_PATH
                    + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FILE_NAME;
            File configFile = new File(path);
            if (!configFile.exists()) {
                throw new OutputEventAdaptorEventProcessingException("The " + RDBMSEventAdaptorConstants
                        .ADAPTOR_GENERIC_RDBMS_FILE_NAME + " can not found in " + path);
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
            throw new OutputEventAdaptorEventProcessingException("Syntax Error.Cannot unmarshal provided File "
                    + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FILE_NAME + e.getMessage(), e);
        }
    }

    /**
     * Construct all the queries and assign to executionInfo instance
     */
    private void initializeDatabaseExecutionInfo(String tableName, String executionMode, String updateColumnKeys,
            Object message, OutputEventAdaptorConfiguration adaptorConfig, ExecutionInfo executionInfo)
            throws RDBMSConnectionException {

        if (resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE_UPDATE)
                .equalsIgnoreCase(executionMode)) {
            executionInfo.setUpdateMode(true);
        }

        Connection con = null;
        String dbName;

        try {
            CarbonDataSource carbonDataSource = EventAdaptorValueHolder.getDataSourceService().getDataSource(
                    adaptorConfig.getOutputProperties().get(RDBMSEventAdaptorConstants
                            .ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME));
            if (carbonDataSource != null) {
                executionInfo.setDatasource((DataSource) carbonDataSource.getDSObject());
            }

            try {
                con = executionInfo.getDatasource().getConnection();
                DatabaseMetaData databaseMetaData = con.getMetaData();
                dbName = databaseMetaData.getDatabaseProductName();
            } catch (SQLException e) {
                throw new RDBMSConnectionException(e);
            }

            Map<String, String> elementMappings = dbTypeMappings.get(dbName.toLowerCase());

            //Constructing (eg: ID  varchar2(255),INFORMATION  varchar2(255)) type values : column_types
            StringBuilder column_types = new StringBuilder("");

            //Constructing (eg: id,information) type values : columns
            StringBuilder columns = new StringBuilder("");

            //Constructing (eg: ?,?,?) type values : valuePositionsBuilder
            StringBuilder valuePositionsBuilder = new StringBuilder("");

            List<Attribute> tableInsertColumnList = new ArrayList<Attribute>();
            boolean appendComma = false;
            for (Map.Entry<String, Object> entry : (((Map<String, Object>) message).entrySet())) {
                AttributeType type = null;
                String columnName = entry.getKey().toUpperCase();
                if (appendComma) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_COMMA));
                }
                column_types.append(columnName).append("  ");
                if (entry.getValue() instanceof Integer) {
                    type = AttributeType.INT;
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_INTEGER));
                } else if (entry.getValue() instanceof Long) {
                    type = AttributeType.LONG;
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_LONG));
                } else if (entry.getValue() instanceof Float) {
                    type = AttributeType.FLOAT;
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FLOAT));
                } else if (entry.getValue() instanceof Double) {
                    type = AttributeType.DOUBLE;
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DOUBLE));
                } else if (entry.getValue() instanceof String) {
                    type = AttributeType.STRING;
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_STRING));
                } else if (entry.getValue() instanceof Boolean) {
                    type = AttributeType.BOOL;
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_BOOLEAN));
                }
                Attribute attribute = new Attribute(entry.getKey(), type);
                if (appendComma) {
                    columns.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_COMMA));
                    valuePositionsBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_COMMA));
                } else {
                    appendComma = true;
                }
                tableInsertColumnList.add(attribute);
                columns.append(attribute.getName());
                valuePositionsBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_QUESTION_MARK));
            }

            //Constructing quert to create a new table
            String createTableQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                            .ADAPTOR_GENERIC_RDBMS_CREATE_TABLE), column_types, null,null, null, null);

            //constructing query to insert date into the table row
            String insertTableRowQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                            .ADAPTOR_GENERIC_RDBMS_INSERT_DATA), null,columns, valuePositionsBuilder, null, null);

            //Constructing query to check for the table existence
            String isTableExistQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                            .ADAPTOR_GENERIC_RDBMS_TABLE_EXIST), null, null, null,null, null);

            executionInfo.setPreparedInsertStatement(insertTableRowQuery);
            executionInfo.setPreparedCreateTableStatement(createTableQuery);
            executionInfo.setInsertQueryColumnOrder(tableInsertColumnList);
            executionInfo.setPreparedTableExistenceCheckStatement(isTableExistQuery);

            if (executionMode.equalsIgnoreCase(
                    resourceBundle.getString(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_EXECUTION_MODE_UPDATE))) {

                String[] queryAttributes = updateColumnKeys.trim().split(",");
                List<Attribute> queryAttributeList = new ArrayList<Attribute>(queryAttributes.length);

                for (int i = 0; i < queryAttributes.length; i++) {

                    for (Attribute attribute : executionInfo.getInsertQueryColumnOrder()) {
                        if (queryAttributes[i].equalsIgnoreCase(attribute.getName())) {
                            queryAttributeList.add(attribute);
                            break;
                        }
                    }
                }
                executionInfo.setExistenceCheckQueryColumnOrder(queryAttributeList);

                //Constructing (eg: information = ?  , latitude = ?) type values : column_values
                StringBuilder column_values = new StringBuilder("");
                List<Attribute> updateAttributes = new ArrayList<Attribute>();

                appendComma = false;
                for (Attribute at : executionInfo.getInsertQueryColumnOrder()) {
                    if (!executionInfo.getExistenceCheckQueryColumnOrder().contains(at)) {
                        if (appendComma) {
                            column_values.append(" ").append(elementMappings.get(RDBMSEventAdaptorConstants
                                    .ADAPTOR_GENERIC_RDBMS_COMMA)).append(" ");
                        }
                        column_values.append(at.getName());
                        column_values.append(" ").append(elementMappings.get(RDBMSEventAdaptorConstants
                                .ADAPTOR_GENERIC_RDBMS_EQUAL)).append(" ")
                                .append(elementMappings.get(RDBMSEventAdaptorConstants
                                        .ADAPTOR_GENERIC_RDBMS_QUESTION_MARK)).append(" ");
                        updateAttributes.add(at);
                        appendComma = true;
                    }
                }

                //Constructing (eg: id = ?) type values for WHERE condition : condition
                StringBuilder condition = new StringBuilder("");
                boolean appendAnd = false;
                for (Attribute at : executionInfo.getExistenceCheckQueryColumnOrder()) {
                    if (appendAnd) {
                        condition.append(" ").append(elementMappings.get(RDBMSEventAdaptorConstants
                                .ADAPTOR_GENERIC_RDBMS_AND)).append(" ");
                    }
                    condition.append(at.getName());
                    condition.append(" ").append(elementMappings.get(RDBMSEventAdaptorConstants
                            .ADAPTOR_GENERIC_RDBMS_EQUAL)).append(" ")
                            .append(elementMappings.get(RDBMSEventAdaptorConstants
                                    .ADAPTOR_GENERIC_RDBMS_QUESTION_MARK)).append(" ");
                    updateAttributes.add(at);
                    appendAnd = true;
                }
                executionInfo.setUpdateQueryColumnOrder(updateAttributes);

                //constructing query to update data into the table
                String tableUpdateRowQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                                .ADAPTOR_GENERIC_RDBMS_UPDATE_TABLE), null,null, null, column_values, condition);
                executionInfo.setPreparedUpdateStatement(tableUpdateRowQuery);
            }
        } catch (DataSourceException e) {
            log.error("There is no any data-source found called : " + adaptorConfig.getOutputProperties()
                    .get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME), e);
            throw new RDBMSConnectionException(e.getMessage(), e);
        } finally {
            cleanupConnections(null, con);
        }
    }

    /**
     * Replace attribute values with target build queries
     */
    public String constructQuery(String tableName, String query, StringBuilder column_types, StringBuilder columns,
            StringBuilder values, StringBuilder column_values, StringBuilder condition) {

        if (query.contains(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME)) {
            query = query.replace(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_TABLE_NAME, tableName);
        }
        if (query.contains(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMN_TYPES)) {
            query = query.replace(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMN_TYPES,
                    column_types.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMNS)) {
            query = query.replace(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMNS,
                    columns.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_VALUES)) {
            query = query.replace(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_VALUES, values.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMN_VALUES)) {
            query = query.replace(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_COLUMN_VALUES,
                    column_values.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_CONDITION)) {
            query = query.replace(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_ATTRIBUTE_CONDITION,
                    condition.toString());
        }
        return query;
    }

    public void createTableIfNotExist(ExecutionInfo executionInfo, String tableName)
            throws RDBMSConnectionException, RDBMSEventProcessingException {

        Connection connection;
        Statement stmt = null;
        Boolean tableExists = true;

        try {
            connection = executionInfo.getDatasource().getConnection();
        } catch (SQLException e) {
            throw new RDBMSConnectionException(e);
        }


        try {
            stmt = connection.createStatement();
            stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
        } catch (SQLException e) {
            tableExists = false;
            if (log.isDebugEnabled()) {
                log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
            }
        }

        try {
            if (!tableExists) {
                stmt.executeUpdate(executionInfo.getPreparedCreateTableStatement());
            }
        } catch (SQLException e) {
            throw new RDBMSEventProcessingException("Cannot Execute Create Table Query. " + e.getMessage(), e);
        } finally {
            cleanupConnections(stmt, connection);
        }
    }

    private void cleanupConnections(Statement stmt, Connection connection) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("unable to close statement." + e.getMessage(), e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("unable to close connection." + e.getMessage(), e);
            }
        }
    }

    @Override
    public void testConnection(OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        try {
            DataSource dataSource;
            CarbonDataSource carbonDataSource = EventAdaptorValueHolder.getDataSourceService().
                    getDataSource(outputEventAdaptorConfiguration.getOutputProperties().
                            get(RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME));
            if (carbonDataSource != null) {
                dataSource = (DataSource) carbonDataSource.getDSObject();
                Connection conn = dataSource.getConnection();
                conn.close();
            } else {
                throw new OutputEventAdaptorEventProcessingException("There is no any datasource found named "
                        + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_DATASOURCE_NAME + " to connect.");
            }
        } catch (Exception e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }

    @Override
    public void removeConnectionInfo(OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        initialConfiguration.get(tenantId).get(outputEventAdaptorConfiguration)
                .remove(outputEventAdaptorMessageConfiguration);

        if (initialConfiguration.get(tenantId).get(outputEventAdaptorConfiguration).isEmpty()) {
            initialConfiguration.get(tenantId).remove(outputEventAdaptorConfiguration);
        }
        if (initialConfiguration.get(tenantId).isEmpty()) {
            initialConfiguration.remove(tenantId);
        }
    }
}
