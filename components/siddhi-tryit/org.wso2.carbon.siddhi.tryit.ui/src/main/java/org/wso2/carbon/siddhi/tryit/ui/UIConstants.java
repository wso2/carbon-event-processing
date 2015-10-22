/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.tryit.ui;

public class UIConstants {
    public static final String ANNOTATION_PLAN = "Plan";

    public static final String ANNOTATION_NAME_NAME = "name";
    public static final String ANNOTATION_TOKEN_AT = "@";
    public static final String ANNOTATION_TOKEN_COLON = ":";
    public static final String ANNOTATION_TOKEN_OPENING_BRACKET = "(";
    public static final String ANNOTATION_TOKEN_CLOSING_BRACKET = ")";

    public static final String SIDDHI_LINE_SEPARATER = "\n";
    public static final String SIDDHI_SINGLE_QUOTE = "'";

    public static final String EXECUTION_PLAN_BASIC_TEMPLATE = ANNOTATION_TOKEN_AT + ANNOTATION_PLAN +
            ANNOTATION_TOKEN_COLON +
            ANNOTATION_NAME_NAME +
            ANNOTATION_TOKEN_OPENING_BRACKET +
            SIDDHI_SINGLE_QUOTE +
            "TestExecutionPlan" +
            SIDDHI_SINGLE_QUOTE +
            ANNOTATION_TOKEN_CLOSING_BRACKET +
            SIDDHI_LINE_SEPARATER + SIDDHI_LINE_SEPARATER;
    public static final String EXECUTION_PLAN_SAMPLE =
            "define stream sensorStream (sensorId string, temperature float);\n" +
                    "\n" +
                    "@info(name = 'query1') \n" +
                    "from sensorStream[temperature>98.6] \n" +
                    "select sensorId \n" +
                    "insert into outputStream;";
    public static final String EVENT_STREAM_SAMPLE = "sensorStream=[tempID1,99.8]\n" +
            "delay(100)\n" +
            "sensorStream=[tempID2,80.6]";
}
