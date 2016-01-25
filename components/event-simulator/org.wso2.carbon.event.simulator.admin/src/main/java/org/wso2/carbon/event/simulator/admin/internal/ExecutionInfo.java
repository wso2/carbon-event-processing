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

package org.wso2.carbon.event.simulator.admin.internal;

import javax.sql.DataSource;

/**
 * Contain all the configuration details to execute db actions
 */
public class ExecutionInfo {

    private String preparedTableExistenceCheckStatement;
    private String preparedCheckTableColumnsDataTypeStatement;
    private String preparedSelectStatement;
    private DataSource datasource;
    private long delayBetweenEventsInMillis;

    public DataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    public String getPreparedTableExistenceCheckStatement() {
        return preparedTableExistenceCheckStatement;
    }

    public void setPreparedTableExistenceCheckStatement(String preparedTableExistenceCheckStatement) {
        this.preparedTableExistenceCheckStatement = preparedTableExistenceCheckStatement;
    }

    public String getPreparedSelectStatement() {
        return preparedSelectStatement;
    }

    public void setPreparedSelectStatement(String preparedSelectStatement) {
        this.preparedSelectStatement = preparedSelectStatement;
    }

    public String getPreparedCheckTableColumnsDataTypeStatement() {
        return preparedCheckTableColumnsDataTypeStatement;
    }

    public void setPreparedCheckTableColumnsDataTypeStatement(String preparedCheckTableColumnsDataTypeStatement) {
        this.preparedCheckTableColumnsDataTypeStatement = preparedCheckTableColumnsDataTypeStatement;
    }

    public long getDelayBetweenEventsInMillis() {
        return delayBetweenEventsInMillis;
    }

    public void setDelayBetweenEventsInMillis(long delayBetweenEventsInMillis) {
        this.delayBetweenEventsInMillis = delayBetweenEventsInMillis;
    }
}
