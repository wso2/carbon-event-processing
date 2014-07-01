/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.simulator.core;


import java.io.File;

public class EventSimulatorConstant {

    public static final String STRING="STRING";
    public static final String INT="INT";
    public static final String LONG="LONG";
    public static final String FLOAT="FLOAT";
    public static final String DOUBLE="DOUBLE";
    public static final String BOOLEAN="BOOLEAN";

    public static final String DEPLOY_DIRECTORY_PATH= "eventsimulatorfiles";
    public static final String ROOT_ELEMENT_NAME="streamConfiguration";
    public static final String FILE_ELEMENT="file";
    public static final String STREAM_ID_ELEMENT="streamID";
    public static final String SEPARATE_CHAR_ELEMENT="separateChar";
    public static final String CONFIGURATION_XML_PREFIX="_streamConfiguration.xml";
    public static final String TEMP_DIR_PATH=File.separator + "tmp" ;



}
