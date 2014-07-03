package org.wso2.carbon.event.processor.storm.manager;

import org.apache.axiom.om.*;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * CEP Manager service which keeps track of Storm receivers and CEP publishers
 */
public class ManagerService {
    private static Logger log = Logger.getLogger(ManagerService.class);
    private OMFactory factory = OMAbstractFactory.getOMFactory();
    private OMNamespace OMNamespace = factory.createOMNamespace(ManagerServiceConstants.NAMESPACE, "ns");
    private HashMap<String, Set<Endpoint>> stormReceivers = new HashMap<String, Set<Endpoint>>();
    private HashMap<String, Set<Endpoint>> cepPublishers = new HashMap<String, Set<Endpoint>>();

    private static String getKey(String executionPlanName, String tenantId){
        return executionPlanName + ":" + tenantId;
    }

    private static void insertToCollection(HashMap<String, Set<Endpoint>> collection, String key, Endpoint endpoint){
        Set<Endpoint> endpointSet = collection.get(key);

        if (endpointSet == null){
            endpointSet = new HashSet<Endpoint>();
            collection.put(key, endpointSet);
        }
        endpointSet.add(endpoint);
    }

    public void registerStormReceiver(OMElement request) throws XMLStreamException {
        request.build();
        request.detach();

        OMElement tenantId = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_TENANT_ID));
        OMElement executionPlan = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_EXEC_PLAN));
        OMElement hostName = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_HOST_NAME));
        OMElement port = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_PORT));

        String key = getKey(executionPlan.getText(), tenantId.getText());
        int portNumber = Integer.parseInt(port.getText());
        insertToCollection(stormReceivers, key, new Endpoint(portNumber, hostName.getText(),  Endpoint.ENDPOINT_TYPE_STORM_RECEIVER));
        log.info("Registering Storm Receiver for " + key + " at " + hostName.getText() + ":" + port.getText());
    }

    public void registerCepPublisher(OMElement request) throws XMLStreamException{
        request.build();
        request.detach();

        OMElement tenantId = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_TENANT_ID));
        OMElement executionPlan = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_EXEC_PLAN));
        OMElement hostName = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_HOST_NAME));
        OMElement port = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_PORT));

        String key = getKey(executionPlan.getText(), tenantId.getText());
        int portNumber = Integer.parseInt(port.getText());
        insertToCollection(cepPublishers, key, new Endpoint(portNumber, hostName.getText(), Endpoint.ENDPOINT_TYPE_CEP_PUBLISHER));
        log.info("Registering CEP Publisher for " + key + " at " + hostName.getText() + ":" + port.getText());

    }

    public OMElement getStormReceiver(OMElement request) throws XMLStreamException {
        request.build();
        request.detach();

        OMElement tenantId = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_TENANT_ID));
        OMElement executionPlan = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_EXEC_PLAN));
        OMElement requesterIp = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_REQUESTER_IP));
        String key = getKey(executionPlan.getText(), tenantId.getText());
        log.info("Storm receiver requested for " + key);

        Set<Endpoint> endpointSet = stormReceivers.get(key);
        Endpoint selectedEndpoint = selectEndpoint(endpointSet, requesterIp.getText());
        OMElement response = factory.createOMElement(ManagerServiceConstants.ELEMENT_STORM_RECEIVER_RESPONSE, OMNamespace);
        if (selectedEndpoint != null){
            OMElement hostNameElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_HOST_NAME, OMNamespace);
            OMElement portElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_PORT, OMNamespace);

            OMText hostNameText = factory.createOMText(hostNameElement, selectedEndpoint.getHostName());
            OMText portText = factory.createOMText(portElement, Integer.toString(selectedEndpoint.getPort()));

            hostNameElement.addChild(hostNameText);
            portElement.addChild(portText);
            response.addChild(hostNameElement);
            response.addChild(portElement);
            log.info("Returning Storm Receiver :" + selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort());
        }else{
            log.warn("No Storm receiver registered " + key);
        }

        return response;
    }

    public OMElement getCEPPublisher(OMElement request) throws XMLStreamException {
        request.build();
        request.detach();

        OMElement tenantId = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_TENANT_ID));
        OMElement executionPlan = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_EXEC_PLAN));
        OMElement requesterIp = request.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_REQUESTER_IP));
        String key = getKey(executionPlan.getText(), tenantId.getText());
        log.info("CEP Publisher requested for  " + key);

        Set<Endpoint> endpointSet = cepPublishers.get(key);
        Endpoint selectedEndpoint = selectEndpoint(endpointSet, requesterIp.getText());
        OMElement response = factory.createOMElement(ManagerServiceConstants.ELEMENT_CEP_PUBLISHER_RESPONSE, OMNamespace);
        if (selectedEndpoint != null){
            OMElement hostNameElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_HOST_NAME, OMNamespace);
            OMElement portElement = factory.createOMElement(ManagerServiceConstants.ELEMENT_PORT, OMNamespace);

            OMText hostNameText = factory.createOMText(hostNameElement, selectedEndpoint.getHostName());
            OMText portText = factory.createOMText(portElement, Integer.toString(selectedEndpoint.getPort()));

            hostNameElement.addChild(hostNameText);
            portElement.addChild(portText);
            response.addChild(hostNameElement);
            response.addChild(portElement);
            log.info("Returning CEP Publisher:" + selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort());
        }else{
            log.warn("No CEP publishers registered " + key);
        }

        return response;
    }

    private Endpoint selectEndpoint(Set<Endpoint> endpointSet, String requesterIp){
        Endpoint selectedEndpoint = null;

        if (endpointSet != null && !endpointSet.isEmpty()){
            // If  there's a storm receiver/cep publisher in the same host as requester IP select it
            if ("".equals(requesterIp) == false){
                for (Endpoint endpoint : endpointSet){
                    if (endpoint.getHostName().equals(requesterIp)){
                        selectedEndpoint = endpoint;

                        if (log.isDebugEnabled()){
                            log.debug("Selecting" + endpoint.toString() + " since it's in the same host as the requester");
                        }
                        break;
                    }
                }
            }
            // If there are no endpoints in the same host. Select the endpoint with lease number of connections
            if (selectedEndpoint == null){
                int minConnectionCount = Integer.MAX_VALUE;

                for (Endpoint endpoint : endpointSet){
                    if (log.isDebugEnabled()){
                        log.debug("Endpoint " + endpoint.toString() + " has " + endpoint.getConnectionCount() + " connections.");
                    }

                    if (endpoint.getConnectionCount() < minConnectionCount){
                        minConnectionCount = endpoint.getConnectionCount();
                        selectedEndpoint = endpoint;
                    }
                }
            }

            selectedEndpoint.setConnectionCount(selectedEndpoint.getConnectionCount() + 1);
        }
        return selectedEndpoint;
    }
}

class Endpoint{
    public static final String ENDPOINT_TYPE_STORM_RECEIVER = "StormReceiver";
    public static final String ENDPOINT_TYPE_CEP_PUBLISHER = "CepPublisher";

    private int port;
    private String hostName;
    private int connectionCount = 0;
    private String type;

    Endpoint(int port, String hostName, String type) {
        this.port = port;
        this.hostName = hostName;
        this.type = type;
    }

    public String getHostName(){
        return hostName;
    }

    public int getPort(){
        return port;
    }

    public void setConnectionCount(int connections){
        connectionCount = connections;
    }

    public int getConnectionCount(){return connectionCount;}

    public String toString(){
        return ("[" + type + "]" + hostName + ":" + port);
    }
}