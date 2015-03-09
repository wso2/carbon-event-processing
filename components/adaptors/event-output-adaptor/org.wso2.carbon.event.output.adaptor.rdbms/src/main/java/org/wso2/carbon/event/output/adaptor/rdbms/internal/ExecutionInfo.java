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

package org.wso2.carbon.event.output.adaptor.rdbms.internal;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.output.adaptor.rdbms.internal.util.DecayTimer;

import javax.sql.DataSource;
import java.util.List;

/**
 * Contain all the configuration details to execute db actions
 */
public class ExecutionInfo {

    private List<Attribute> insertQueryColumnOrder;
    private List<Attribute> updateQueryColumnOrder;
    private List<Attribute> existenceCheckQueryColumnOrder;
    private String preparedInsertStatement;
    private String preparedUpdateStatement;
    private String preparedCreateTableStatement;
    private String preparedTableExistenceCheckStatement;
    private DataSource datasource;
    private Boolean isConnectionLive = false;
    private long nextConnectionTime;
    private DecayTimer decayTimer;

    private boolean updateMode;

    public List<Attribute> getInsertQueryColumnOrder() {
        return insertQueryColumnOrder;
    }

    public void setInsertQueryColumnOrder(List<Attribute> insertQueryColumnOrder) {
        this.insertQueryColumnOrder = insertQueryColumnOrder;
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

    public boolean isUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(boolean updateMode) {
        this.updateMode = updateMode;
    }

    public String getPreparedCreateTableStatement() {
        return preparedCreateTableStatement;
    }

    public void setPreparedCreateTableStatement(String preparedCreateTableStatement) {
        this.preparedCreateTableStatement = preparedCreateTableStatement;
    }

    public List<Attribute> getUpdateQueryColumnOrder() {
        return updateQueryColumnOrder;
    }

    public void setUpdateQueryColumnOrder(List<Attribute> updateQueryColumnOrder) {
        this.updateQueryColumnOrder = updateQueryColumnOrder;
    }

    public List<Attribute> getExistenceCheckQueryColumnOrder() {
        return existenceCheckQueryColumnOrder;
    }

    public void setExistenceCheckQueryColumnOrder(List<Attribute> existenceCheckQueryColumnOrder) {
        this.existenceCheckQueryColumnOrder = existenceCheckQueryColumnOrder;
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    public Boolean getIsConnectionLive() {
        return isConnectionLive;
    }

    public void setIsConnectionLive(Boolean isConnectionLive) {
        this.isConnectionLive = isConnectionLive;
    }

    public long getNextConnectionTime() {

        nextConnectionTime = getDecayTimer().returnTimeToWait();
        return System.currentTimeMillis() + (nextConnectionTime);
    }

    public DecayTimer getDecayTimer() {
        return decayTimer;
    }

    public void setDecayTimer(DecayTimer decayTimer) {
        this.decayTimer = decayTimer;
    }

    public String getPreparedTableExistenceCheckStatement() {
        return preparedTableExistenceCheckStatement;
    }

    public void setPreparedTableExistenceCheckStatement(String preparedTableExistenceCheckStatement) {
        this.preparedTableExistenceCheckStatement = preparedTableExistenceCheckStatement;
    }
}
