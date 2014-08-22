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
package org.wso2.carbon.event.output.adaptor.mysql.internal;

import org.wso2.carbon.databridge.commons.Attribute;

import java.util.ArrayList;

public class TableInfo {
    private String tableName;
    private String databaseName;
    private ArrayList<Attribute> insertColumnOrder;
    private ArrayList<Attribute> updateColumnOrder;
    private ArrayList<Attribute> existenceCheckColumnOrder;
    private String preparedInsertStatement;
    private String preparedUpdateStatement;
    private String preparedExistenceCheckStatement;

    private boolean updateMode;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<Attribute> getInsertColumnOrder() {
        return insertColumnOrder;
    }

    public void setInsertColumnOrder(ArrayList<Attribute> columnOrder) {
        this.insertColumnOrder = columnOrder;
    }

    public String getPreparedInsertStatement() {
        return preparedInsertStatement;
    }

    public void setPreparedInsertStatement(String insertStatementPrefix) {
        this.preparedInsertStatement = insertStatementPrefix;
    }

    public String getPreparedUpdateStatement() {
        return preparedUpdateStatement;
    }

    public void setPreparedUpdateStatement(String preparedUpdateStatement) {
        this.preparedUpdateStatement = preparedUpdateStatement;
    }

    public String getPreparedExistenceCheckStatement() {
        return preparedExistenceCheckStatement;
    }

    public void setPreparedExistenceCheckStatement(String preparedExistenceCheckStatement) {
        this.preparedExistenceCheckStatement = preparedExistenceCheckStatement;
    }

    public boolean isUpdateMode() {
        return updateMode;
    }

    public ArrayList<Attribute> getUpdateColumnOrder() {
        return updateColumnOrder;
    }

    public void setUpdateColumnOrder(ArrayList<Attribute> updateColumnOrder) {
        this.updateColumnOrder = updateColumnOrder;
    }

    public ArrayList<Attribute> getExistenceCheckColumnOrder() {
        return existenceCheckColumnOrder;
    }

    public void setExistenceCheckColumnOrder(ArrayList<Attribute> existenceCheckColumnOrder) {
        this.existenceCheckColumnOrder = existenceCheckColumnOrder;
    }

    public void setUpdateMode(boolean updateMode) {
        this.updateMode = updateMode;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}