/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.mysql.internal.util;


public final class MysqlEventAdaptorConstants {


    private MysqlEventAdaptorConstants() {

    }

    public static final String ADAPTOR_MYSQL_DATASOURCE_NAME = "datasource.name";
    public static final String ADAPTOR_MYSQL_TABLE_NAME = "table.name";
    public static final String ADAPTOR_MYSQL_EXECUTION_MODE = "execution.mode";
    public static final String ADAPTOR_MYSQL_EXECUTION_MODE_HINT = "execution.mode.hint";
    public static final String ADAPTOR_MYSQL_EXECUTION_MODE_UPDATE = "execution.mode.update";
    public static final String ADAPTOR_MYSQL_EXECUTION_MODE_INSERT = "execution.mode.insert";
    public static final String ADAPTOR_MYSQL_UPDATE_KEYS = "update.keys";
    public static final String ADAPTOR_MYSQL_UPDATE_KEYS_HINT = "update.keys.hint";

    public static final String ADAPTOR_TYPE_MYSQL = "mysql";
    public static final int TINYINT_MYSQL_VALUE = -7;

}
