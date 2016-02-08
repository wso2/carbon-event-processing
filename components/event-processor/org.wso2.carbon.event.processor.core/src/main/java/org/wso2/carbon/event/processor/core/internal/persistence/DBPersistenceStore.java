/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.core.internal.persistence;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.internal.persistence.util.ExecutionInfo;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.Map;

public class DBPersistenceStore implements PersistenceStore {

    private static final Log log = LogFactory.getLog(DBPersistenceStore.class);

    private DataSource dataSource;
    private String tableName;
    private String dataSourceName;
    private ExecutionInfo executionInfo = null;


    @Override
    public void save(String executionPlanId, String revision, byte[] snapshot) {
        connect();
        if (executionInfo == null) {
            executionInfo = new ExecutionInfo();
            initializeDatabaseExecutionInfo();
        }
        createTableIfNotExist();
        saveRevision(executionPlanId, revision, snapshot);
    }

    @Override
    public void setProperties(Map properties) {
        dataSourceName = (String) properties.get("DataSource");
        tableName = (String) properties.get("TableName");
        if (executionInfo == null) {
            executionInfo = new ExecutionInfo();
            initializeDatabaseExecutionInfo();
        }
    }

    @Override
    public byte[] load(String executionPlanId, String revision) {
        connect();
        String tenantId = getTenantId();
        if (executionInfo == null) {
            executionInfo = new ExecutionInfo();
            initializeDatabaseExecutionInfo();
        }
        return getRevision(executionPlanId, revision, tenantId);
    }

    private byte[] getRevision(String executionPlanId, String revision, String tenantId) {
        PreparedStatement stmt = null;
        Connection con = null;
        byte[] blobAsBytes = null;
        try {
            try {
                con = dataSource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to the data source" + dataSourceName, e);
            }

            stmt = con.prepareStatement(executionInfo.getPreparedSelectStatement());
            stmt.setString(1, revision);
            stmt.setString(2, tenantId);
            stmt.setString(3, executionPlanId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Blob blobSnapshot = resultSet.getBlob("snapshot");
                int blobLength = (int) blobSnapshot.length();
                blobAsBytes = blobSnapshot.getBytes(1, blobLength);
            }

        } catch (SQLException e) {
            log.error("Error while retrieving revision " + revision + "of execution plan:" + executionPlanId + "from the database", e);
        } finally {
            cleanupConnections(stmt, con);
        }
        return blobAsBytes;
    }

    @Override
    public String getLastRevision(String executionPlanId) {
        String tenantId = getTenantId();
        connect();
        if (executionInfo == null) {
            executionInfo = new ExecutionInfo();
            initializeDatabaseExecutionInfo();
        }
        createTableIfNotExist();
        return getLastRevision(executionPlanId, tenantId);
    }

    private String getLastRevision(String executionPlanId, String tenantId) {
        PreparedStatement stmt = null;
        Connection con = null;
        String revision = "";
        try {
            try {
                con = dataSource.getConnection();
            } catch (SQLException e) {
                log.error("Cannot establish connection to the data source" + dataSourceName, e);
            }

            stmt = con.prepareStatement(executionInfo.getPreparedSelectLastStatement());
            stmt.setString(1, tenantId);
            stmt.setString(2, executionPlanId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                revision = String.valueOf(resultSet.getString("revision"));
            }
        } catch (SQLException e) {
            log.error("Error while retrieving last revision of execution plan:" + executionPlanId + "from the database", e);
        } finally {
            cleanupConnections(stmt, con);
        }
        return revision;
    }

    public void connect() {

        Connection con = null;
        try {
            CarbonDataSource carbonDataSource = EventProcessorValueHolder.getDataSourceService().getDataSource(dataSourceName);
            dataSource = (DataSource) carbonDataSource.getDSObject();
            con = ((DataSource) carbonDataSource.getDSObject()).getConnection();
        } catch (DataSourceException e) {
            log.error("No data-source found by the dataSourceName: " + dataSourceName, e);
        } catch (SQLException e) {
            log.error("Cannot establish connection to the data source" + dataSourceName, e);
        } finally {
            cleanupConnections(null, con);
        }

    }


    public void saveRevision(String executionPlanId, String revision, byte[] snapshot) {

        PreparedStatement stmt = null;
        Connection con = null;
        try {
            try {
                con = dataSource.getConnection();
                con.setAutoCommit(false);
            } catch (SQLException e) {
                log.error("Cannot establish connection to the data source" + dataSourceName, e);
            }
            stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
            stmt.setString(1, getTenantId());
            stmt.setString(2, executionPlanId);
            stmt.setString(3, revision);
            stmt.setBlob(4, new SerialBlob(snapshot));
            stmt.executeUpdate();
            con.commit();
        } catch (SQLException e) {
            log.error("Error while saving revision" + revision + " of the execution plan" + executionPlanId + "to the database", e);
        } finally {
            cleanupConnections(stmt, con);
        }
    }

    public void createTableIfNotExist() {

        if (!executionInfo.isTableExist()) {
            Statement stmt = null;
            Boolean tableExists = true;
            Connection con = null;
            try {
                try {
                    con = dataSource.getConnection();
                    con.setAutoCommit(false);
                } catch (SQLException e) {
                    log.error("Cannot establish connection to the data source" + dataSourceName, e);
                }
                stmt = con.createStatement();
                try {
                    stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
                    executionInfo.setTableExist(true);

                } catch (SQLException e) {
                    tableExists = false;
                    if (log.isDebugEnabled()) {
                        log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
                    }
                }

                try {
                    if (!tableExists) {
                        stmt.executeUpdate(executionInfo.getPreparedCreateTableStatement());
                        con.commit();
                        executionInfo.setTableExist(true);
                    }
                } catch (SQLException e) {
                    log.error("Cannot Execute Create Table Query", e);
                }
            } catch (SQLException e) {
                log.error("Connection unavailable", e);
            } finally {
                cleanupConnections(stmt, con);
            }
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

    /**
     * Construct all the queries and assign to executionInfo instance
     */
    private void initializeDatabaseExecutionInfo() {
        //Constructing query to create a new table
        String createTableQuery = "CREATE TABLE " + tableName + " (id INT NOT NULL AUTO_INCREMENT, tenantId VARCHAR(100), executionPlanId VARCHAR(100),revision  VARCHAR(100),snapshot  BLOB,  PRIMARY KEY (id))";
        //constructing query to insert date into the table row
        String insertTableRowQuery = "INSERT INTO " + tableName + " (tenantId, executionPlanId, revision, snapshot) VALUES (?, ?, ?,?)";
        //Constructing query to check for the table existence
        String isTableExistQuery = "SELECT * FROM " + tableName + " limit 1";
        //Constructing query to select snapshot
        String selectTableQuery = "SELECT snapshot FROM " + tableName + " WHERE  revision = ? AND  tenantId = ? AND executionPlanId = ? ";
        //Constructing query to select latest revision
        String selectLastQuery = "SELECT revision FROM " + tableName + " WHERE tenantId = ? AND executionPlanId = ? ORDER BY id DESC  LIMIT 1";

        executionInfo.setPreparedInsertStatement(insertTableRowQuery);
        executionInfo.setPreparedCreateTableStatement(createTableQuery);
        executionInfo.setPreparedTableExistenceCheckStatement(isTableExistQuery);
        executionInfo.setPreparedSelectStatement(selectTableQuery);
        executionInfo.setPreparedSelectLastStatement(selectLastQuery);
    }

    private String getTenantId() {
        return String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()).replaceAll("-", "M");
    }

}
