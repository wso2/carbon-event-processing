package org.wso2.carbon.event.processor.storm.common.client;

import org.apache.axiom.om.*;

/**
 * Creating required requests to communicate with CEP manager service
 */
public class ManagerServiceMessageCreator {
    /**
     * Create the request for registering storm receiver with the connection details with CEP manager service.
     * @param executionPlan Name of the execution plan
     * @param tenantId Id of the tenant which execution plan belongs to
     * @param hostName Host name or the IP address of the storm receiver
     * @param port listening port of the storm receiver
     * @return request to be sent
     */
    public static OMElement createRegisterStormReceiverRequest(String executionPlan, int tenantId, String hostName, int port){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace OMNamespace = factory.createOMNamespace(ManagerServiceConstants.NAMESPACE, "tns");

        OMElement request = factory.createOMElement(ManagerServiceConstants.END_POINT_REGISTER_STORM_RECEIVER, OMNamespace);
        OMElement tenantIdElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_TENANT_ID, OMNamespace);
        OMElement executionPlanElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_EXEC_PLAN, OMNamespace);
        OMElement hostNameElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_HOST_NAME, OMNamespace);
        OMElement portElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_PORT, OMNamespace);

        OMText tenantIdText = factory.createOMText(tenantIdElement, Integer.toString(tenantId));
        OMText executionPlanText = factory.createOMText(executionPlanElement, executionPlan);
        OMText hostNameText = factory.createOMText(hostNameElement, hostName);
        OMText portText = factory.createOMText(portElement, Integer.toString(port));

        tenantIdElement.addChild(tenantIdText);
        executionPlanElement.addChild(executionPlanText);
        hostNameElement.addChild(hostNameText);
        portElement.addChild(portText);

        request.addChild(tenantIdElement);
        request.addChild(executionPlanElement);
        request.addChild(hostNameElement);
        request.addChild(portElement);

        return request;
    }

    /**
     * Create the request for registering CEP publisher with the connection details with CEP manager service.
     * @param executionPlan Name of the execution plan
     * @param tenantId Id of the tenant which execution plan belongs to
     * @param hostName Host name or the IP address of the storm receiver
     * @param port listening port of the storm receiver
     * @return request to be sent
     */
    public static OMElement createRegisterCepPublisherRequest(String executionPlan, int tenantId, String hostName, int port){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace OMNamespace = factory.createOMNamespace(ManagerServiceConstants.NAMESPACE, "tns");

        OMElement request = factory.createOMElement(ManagerServiceConstants.END_POINT_REGISTER_CEP_PUBLISHER, OMNamespace);
        OMElement tenantIdElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_TENANT_ID, OMNamespace);
        OMElement executionPlanElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_EXEC_PLAN, OMNamespace);
        OMElement hostNameElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_HOST_NAME, OMNamespace);
        OMElement portElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_PORT, OMNamespace);

        OMText tenantIdText = factory.createOMText(tenantIdElement, Integer.toString(tenantId));
        OMText executionPlanText = factory.createOMText(executionPlanElement, executionPlan);
        OMText hostNameText = factory.createOMText(hostNameElement, hostName);
        OMText portText = factory.createOMText(portElement, Integer.toString(port));

        tenantIdElement.addChild(tenantIdText);
        executionPlanElement.addChild(executionPlanText);
        hostNameElement.addChild(hostNameText);
        portElement.addChild(portText);

        request.addChild(tenantIdElement);
        request.addChild(executionPlanElement);
        request.addChild(hostNameElement);
        request.addChild(portElement);

        return request;
    }

    /**
     * create the request to retrieve list of CEP publishers for a given execution plan of a given tenant from
     * CEP manager service.
     * @param executionPlan Name of the execution plan
     * @param tenantId Id of the tenant which execution plan belongs to
     * @param requesterIp IP address of the request originator
     * @return request to be sent
     */
    public static OMElement createGetCepPublisherRequest(String executionPlan, int tenantId, String requesterIp){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace OMNamespace = factory.createOMNamespace(ManagerServiceConstants.NAMESPACE, "tns");

        OMElement request = factory.createOMElement(ManagerServiceConstants.END_POINT_GET_CEP_PUBLISHER, OMNamespace);
        OMElement tenantIdElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_TENANT_ID, OMNamespace);
        OMElement executionPlanElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_EXEC_PLAN, OMNamespace);
        OMElement requesterIpElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_REQUESTER_IP, OMNamespace);

        OMText tenantIdText = factory.createOMText(tenantIdElement, Integer.toString(tenantId));
        OMText executionPlanText = factory.createOMText(executionPlanElement, executionPlan);
        OMText requesterIpText = factory.createOMText(requesterIpElement, (requesterIp == null) ? "" : requesterIp);

        tenantIdElement.addChild(tenantIdText);
        executionPlanElement.addChild(executionPlanText);
        requesterIpElement.addChild(requesterIpText);

        request.addChild(tenantIdElement);
        request.addChild(executionPlanElement);
        request.addChild(requesterIpElement);

        return request;
    }

    /**
     * create the request to retrieve list of storm receivers for a given execution plan of a given tenant from
     * CEP manager service.
     * @param executionPlan Name of the execution plan
     * @param tenantId Id of the tenant which execution plan belongs to
     * @param requesterIp IP address of the request originator
     * @return request to be sent
     */
    public static OMElement createGetStormReceiverRequest(String executionPlan, int tenantId, String requesterIp){
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace OMNamespace = factory.createOMNamespace(ManagerServiceConstants.NAMESPACE, "tns");

        OMElement request = factory.createOMElement(ManagerServiceConstants.END_POINT_GET_STORM_RECEIVER, OMNamespace);
        OMElement tenantIdElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_TENANT_ID, OMNamespace);
        OMElement executionPlanElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_EXEC_PLAN, OMNamespace);
        OMElement requesterIpElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_REQUESTER_IP, OMNamespace);

        OMText tenantIdText = factory.createOMText(tenantIdElement, Integer.toString(tenantId));
        OMText executionPlanText = factory.createOMText(executionPlanElement, executionPlan);
        OMText requesterIpText = factory.createOMText(requesterIpElement, (requesterIp == null) ? "" : requesterIp);

        tenantIdElement.addChild(tenantIdText);
        executionPlanElement.addChild(executionPlanText);
        requesterIpElement.addChild(requesterIpText);

        request.addChild(tenantIdElement);
        request.addChild(executionPlanElement);
        request.addChild(requesterIpElement);

        return request;
    }
}
