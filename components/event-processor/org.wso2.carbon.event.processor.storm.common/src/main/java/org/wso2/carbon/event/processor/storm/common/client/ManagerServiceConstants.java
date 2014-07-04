package org.wso2.carbon.event.processor.storm.common.client;

/**
 * Created by sajith on 6/20/14.
 */
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
