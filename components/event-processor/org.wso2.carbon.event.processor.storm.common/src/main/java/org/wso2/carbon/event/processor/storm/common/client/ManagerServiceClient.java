package org.wso2.carbon.event.processor.storm.common.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.util.collection.Pair;

import javax.xml.namespace.QName;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Communicates with the CEP manager service
 */
public class ManagerServiceClient {
    private Logger log = Logger.getLogger(ManagerServiceClient.class);
    private EndpointReference targetEPR;
    private Timer reconnectTimer  = new Timer();
    private ManagerServiceClientCallback callback;

    public ManagerServiceClient(String host, int port, ManagerServiceClientCallback callback){
        targetEPR = new EndpointReference("http://" + host + ":" + port + "/services/CEPManagerService");
        this.callback = callback;
    }

    /**
     * Publish connection details (i.e. host name and port) of a storm receiver to CEP manager service.
     * @param executionPlan Name of the execution plan that the receiver belongs to
     * @param tenantId tenant ID of the execution plan owner
     * @param hostName Host on which the storm receiver is running
     * @param port Port number on which storm receiver is listening for events
     * @param retryInterval Time interval in seconds between two consecutive connect attempts to CEP manger service
     */
    public void registerStormReceiver(final String executionPlan, final int tenantId, final String hostName, final int port, final int retryInterval){
        OMElement request = ManagerServiceMessageCreator.createRegisterStormReceiverRequest(executionPlan, tenantId, hostName, port);
        try {
            ServiceClient sender = new ServiceClient();
            Options options = new Options();
            sender.setOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            sender.fireAndForget(request);
            log.info("Registering storm receiver for " + executionPlan + ":" + tenantId + " at " + hostName + ":" + port);
        } catch (AxisFault axisFault) {
            log.warn("Error while connecting to CEP manager service. Retrying in " + retryInterval + " seconds.", axisFault);
            reconnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    registerStormReceiver(executionPlan, tenantId, hostName, port, retryInterval);
                }
            }, retryInterval *1000);
        }
        log.info("Storm receiver Registered for " + executionPlan + ":" + tenantId + " at " + hostName + ":" + port);
    }

    /**
     *Publish connection details (i.e. host name and port) of a CEP publisher to CEP manager service.
     * @param executionPlan Name of the execution plan that the CEP publisher belongs to
     * @param tenantId tenant ID of the execution plan owner
     * @param hostName Host on which the CEP publisher is running
     * @param port Port number on which CEP publisher is listening for events
     * @param retryInterval Time interval in seconds between two consecutive connect attempts to CEP manger service
     */
    public void registerCepPublisher(final String executionPlan, final int tenantId, final String hostName, final int port, final int retryInterval){
        OMElement request = ManagerServiceMessageCreator.createRegisterCepPublisherRequest(executionPlan, tenantId, hostName, port);
        try{
            ServiceClient sender = new ServiceClient();
            Options options = new Options();
            sender.setOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            sender.fireAndForget(request);
            log.info("Registering CEP Publisher receiver for " + executionPlan + ":" + tenantId + " at " + hostName + ":" + port);
        } catch (AxisFault axisFault) {
            log.warn("Error while connecting to CEP manager service. Retrying in " + retryInterval + " seconds.", axisFault);
            reconnectTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    registerCepPublisher(executionPlan, tenantId, hostName, port, retryInterval);
                }
            }, retryInterval * 1000);
        }
        log.info("CEP publisher Registered for " + executionPlan + ":" + tenantId + " at " + hostName + ":" + port);
    }

    /**
     *  Retrieve connection details of storm receivers for a given execution plan of a given tenant. This function will return immediately and
     *  once the information is retrieved will be notified through the call back
     * @param executionPlan Name of the execution plan that the receiver belongs to
     * @param tenantId tenant ID of the execution plan owner
     * @param retryInterval Time interval in seconds between two consecutive connect attempts to CEP manger service
     * @return Connection details of storm receiver
     */
    public void getStormReceiver(final String executionPlan, final int tenantId,  final int retryInterval, final String requesterIp){

        Runnable connectorThreadImplementer  = new Runnable() {
            @Override
            public void run() {
                OMElement response = sendGetStormReceiversRequest(executionPlan, tenantId, requesterIp);
                // Try while the CEP manager service is available
                while (response == null){
                    try {
                        log.info("Failed to connect to CEP manager service. Retrying in " + retryInterval + " seconds");
                        Thread.sleep(retryInterval * 1000);
                        response = sendGetStormReceiversRequest(executionPlan, tenantId, requesterIp);
                    } catch (InterruptedException e) {
                        // Keep trying even if there's a exception
                    }
                }

                // Try while the storm receivers are registered for the execution plan
                Pair<String, Integer> returnValue = decodeResponse(response, ManagerServiceConstants.ELEMENT_STORM_RECEIVER_RESPONSE);
                while (returnValue == null){
                    try {
                        log.info("No Storm receivers registered yet for " + executionPlan + ":" + tenantId + ". Retrying in " + retryInterval + " seconds");
                        Thread.sleep(retryInterval * 1000);
                        response = sendGetStormReceiversRequest(executionPlan, tenantId, requesterIp);
                        returnValue = decodeResponse(response, ManagerServiceConstants.ELEMENT_STORM_RECEIVER_RESPONSE);
                    } catch (InterruptedException e) {
                        // Keep trying even if there's a exception
                    }
                }
                callback.OnResponseReceived(returnValue);
            }
        };
        Thread connectorThread = new Thread(connectorThreadImplementer);
        connectorThread.start();
    }

    /**
     * Retrieve connection details of CEP publishers for a given execution plan of a given tenant. Call to this method will not return
     * until information is fetched from the CEP manager service
     * @param executionPlan Name of the execution plan that CEP publisher belongs to
     * @param tenantId tenant ID of the execution plan owner
     * @param retryInterval Time interval in seconds between two consecutive connect attempts to CEP manger service
     * @return  Connection (i.e Host name and port number) of CEP publisher
     */
    public void getCepPublisher(final String executionPlan, final int tenantId, final int retryInterval, final String requesterIp){

        Runnable connectorThreadImplementer  = new Runnable() {
            @Override
            public void run() {
                OMElement response = sendGetCepPublishersRequest(executionPlan, tenantId, requesterIp);
                // Try while the CEP manager service is available
                while (response == null){
                    try {
                        log.info("Failed to connect to CEP manager service. Retrying in " + retryInterval + " seconds");
                        Thread.sleep(retryInterval * 1000);
                        response = sendGetCepPublishersRequest(executionPlan, tenantId, requesterIp);
                    } catch (InterruptedException e) {
                        // Keep trying even if there's an exception
                    }
                }

                // Try while the storm receivers are registered for the execution plan
                Pair<String, Integer> returnValue = decodeResponse(response, ManagerServiceConstants.ELEMENT_CEP_PUBLISHER_RESPONSE);
                while (returnValue == null){
                    try {
                        log.info("No CEP publishers registered yet for " + executionPlan + ":" + tenantId + ". Retrying in " + retryInterval + " seconds");
                        Thread.sleep(retryInterval * 1000);
                        response = sendGetCepPublishersRequest(executionPlan, tenantId, requesterIp);
                        returnValue = decodeResponse(response, ManagerServiceConstants.ELEMENT_CEP_PUBLISHER_RESPONSE);
                    } catch (InterruptedException e) {
                        // Keep trying even if there's an exception
                    }
                }
                callback.OnResponseReceived(returnValue);
            }
        };
        Thread connectorThread = new Thread(connectorThreadImplementer);
        connectorThread.start();
    }

    private OMElement sendGetStormReceiversRequest(String executionPlan, int tenantId, String requesterIp){
        OMElement request = ManagerServiceMessageCreator.createGetStormReceiverRequest(executionPlan, tenantId, requesterIp);
        OMElement result = null;
        try {
            ServiceClient sender = new ServiceClient();
            Options options = new Options();
            sender.setOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            log.info("Requesting  storm receiver details for " + executionPlan + ":" + tenantId);
            result = sender.sendReceive(request);
        } catch (AxisFault axisFault) {
            log.error("Error while connecting to CEP manager service", axisFault);
        }

        return result;
    }

    private OMElement sendGetCepPublishersRequest(String executionPlan, int tenantId, String requesterIp){
        OMElement request = ManagerServiceMessageCreator.createGetCepPublisherRequest(executionPlan, tenantId, requesterIp);
        OMElement result = null;
        try {
            ServiceClient sender = new ServiceClient();
            Options options = new Options();
            sender.setOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            log.info("Requesting  CEP publisher details for " + executionPlan + ":" + tenantId);
            result = sender.sendReceive(request);
        } catch (AxisFault axisFault) {
            log.error("Error while connecting to CEP manager service", axisFault);
        }

        return result;
    }

    private Pair<String, Integer> decodeResponse(OMElement response, String responseRootElementName){
        OMElement hostNameElement = response.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_HOST_NAME));
        OMElement portElement = response.getFirstChildWithName(new QName(ManagerServiceConstants.NAMESPACE, ManagerServiceConstants.ELEMENT_PORT));

        Pair<String, Integer> endpoint = null;
        if (hostNameElement != null || portElement != null){
            Integer port = new Integer(portElement.getText());
            String hostName = hostNameElement.getText();
            endpoint = new Pair<String, Integer>(hostName, port);
        }

        return  endpoint;
    }
}
