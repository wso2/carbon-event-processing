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
package org.wso2.carbon.event.processor.storm.common.client;

public class ManagerServiceConstants {
    // Constants related to messages
    /**
     * Namespace for the SOAP messages used in CEP manager service
     */
    public static final String NAMESPACE = "http://org.wso2.cep.manager.com";
    /**
     * XML element name which hold the value of tenant ID in requests/responses
     */
    public static final String ELEMENT_TENANT_ID = "tenantId";
    /**
     * XML element name which hold execution plan name in requests/responses
     */
    public static final String ELEMENT_EXEC_PLAN = "executionPlan";
    /**
     * XML element name which hold the host name in requests/responses
     */
    public static final String ELEMENT_HOST_NAME = "hostName";
    /**
     * XML element name which hold the port number in requests/responses
     */
    public static final String ELEMENT_PORT = "port";
    /**
     * XML element name which hold IP address of the originator of the request
     */
    public static final String ELEMENT_REQUESTER_IP = "requesterIp";
    /**
     * Root XML element of the which contains information of storm receivers
     */
    public static final String ELEMENT_STORM_RECEIVER_RESPONSE = "stormReceiver";
    /**
     * Root XML element of the which contains information of CEP publishers
     */
    public static final String ELEMENT_CEP_PUBLISHER_RESPONSE = "cepPublisher";

    // Operations/Endpoints of CEP manager service
    /**
     * Operation name which must be used to register a storm receiver in CEP manager service
     */
    public static final String END_POINT_REGISTER_STORM_RECEIVER = "registerStormReceiver";
    /**
     * Operation name which must be used to register a CEP publisher in CEP manager service
     */
    public static final String END_POINT_REGISTER_CEP_PUBLISHER  = "registerCepPublisher";
    /**
     * Operation name which must be used to retrieve connection details of storm receivers in CEP manager service
     */
    public static final String END_POINT_GET_STORM_RECEIVER = "getStormReceiver";
    /**
     * Operation name which must be used to retrieve connection details of CEP publishers in CEP manager service
     */
    public static final String END_POINT_GET_CEP_PUBLISHER = "getCEPPublisher";

}
