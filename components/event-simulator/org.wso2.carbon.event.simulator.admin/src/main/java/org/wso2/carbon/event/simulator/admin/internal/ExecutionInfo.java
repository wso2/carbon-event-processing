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

package org.wso2.carbon.event.simulator.admin.internal;

import javax.sql.DataSource;

/**
 * Contain all the configuration details to execute db actions
 */
public class ExecutionInfo {

    private String preparedTableExistenceCheckStatement;
    private String preparedCheckTableColomnsDataTypeStatement;
    private String preparedSelectStatement;
    private DataSource datasource;

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
    public String getPreparedCheckTableColomnsDataTypeStatement() {
        return preparedCheckTableColomnsDataTypeStatement;
    }

    public void setPreparedCheckTableColomnsDataTypeStatement(String preparedCheckTableColomnsDataTypeStatement) {
        this.preparedCheckTableColomnsDataTypeStatement = preparedCheckTableColomnsDataTypeStatement;
    }
}
