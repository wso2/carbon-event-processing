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
package org.wso2.carbon.event.processor.ui;

public interface UIConstants {
    String SIDDHI_DISTRIBUTED_PROCESSING = "siddhi.enable.distributed.processing";
    String SIDDHI_SNAPSHOT_INTERVAL = "siddhi.persistence.snapshot.time.interval.minutes";

    String TRUE_LITERAL = "true";
    String FALSE_LITERAL = "false";

    final String ANNOTATION_PLAN = "Plan";

    final String ANNOTATION_NAME_NAME = "name";
    final String ANNOTATION_NAME_DESCRIPTION = "description";

    final String ANNOTATION_TOKEN_AT = "@";
    final String ANNOTATION_TOKEN_COLON = ":";
    final String ANNOTATION_TOKEN_OPENING_BRACKET = "(";
    final String ANNOTATION_TOKEN_CLOSING_BRACKET = ")";

    final String SIDDHI_LINE_SEPARATER = "\n";
    final String SIDDHI_SINGLE_QUOTE = "'";

    final String SIDDHI_LITERAL_BEGIN_MULTI_LINE_COMMENT = "/*";
    final String SIDDHI_LITERAL_END_MULTI_LINE_COMMENT = "*/";
    final String SIDDHI_LITERAL_DECLARE_SINGLE_LINE_COMMENT = "--";

    final String EXECUTION_PLAN_BASIC_TEMPLATE = SIDDHI_LITERAL_BEGIN_MULTI_LINE_COMMENT +
            " Enter a unique ExecutionPlan " +
            SIDDHI_LITERAL_END_MULTI_LINE_COMMENT + SIDDHI_LINE_SEPARATER +
            ANNOTATION_TOKEN_AT + ANNOTATION_PLAN +
            ANNOTATION_TOKEN_COLON +
            ANNOTATION_NAME_NAME +
            ANNOTATION_TOKEN_OPENING_BRACKET +
            SIDDHI_SINGLE_QUOTE +
            "ExecutionPlan"+
            SIDDHI_SINGLE_QUOTE +
            ANNOTATION_TOKEN_CLOSING_BRACKET  + SIDDHI_LINE_SEPARATER + SIDDHI_LINE_SEPARATER +
            SIDDHI_LITERAL_BEGIN_MULTI_LINE_COMMENT +
            " Enter a unique description for ExecutionPlan " +
            SIDDHI_LITERAL_END_MULTI_LINE_COMMENT + SIDDHI_LINE_SEPARATER +
            SIDDHI_LITERAL_DECLARE_SINGLE_LINE_COMMENT + " "+
            ANNOTATION_TOKEN_AT + ANNOTATION_PLAN +
            ANNOTATION_TOKEN_COLON +
            ANNOTATION_NAME_DESCRIPTION +
            ANNOTATION_TOKEN_OPENING_BRACKET +
            SIDDHI_SINGLE_QUOTE +"ExecutionPlan"+
            SIDDHI_SINGLE_QUOTE +
            ANNOTATION_TOKEN_CLOSING_BRACKET + SIDDHI_LINE_SEPARATER +
            SIDDHI_LINE_SEPARATER +
            SIDDHI_LITERAL_BEGIN_MULTI_LINE_COMMENT +
            " define streams/tables and write queries here ... " +
            SIDDHI_LITERAL_END_MULTI_LINE_COMMENT +
            SIDDHI_LINE_SEPARATER;
}
