package org.wso2.carbon.event.processor.core.internal.storm.manager;

import org.apache.thrift.TException;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.EndpointNotFoundException;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.NotStormManagerException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StormManagerServiceImpl implements StormManagerService.Iface {

    private HashMap<String, Set<Endpoint>> stormReceivers = new HashMap<String, Set<Endpoint>>();
    private HashMap<String, Set<Endpoint>> cepPublishers = new HashMap<String, Set<Endpoint>>();
    private boolean isStormManager;
    private String hostPort;

    public StormManagerServiceImpl(String hostPort) {

        this.hostPort = hostPort;
    }

    @Override
    public void registerStormReceiver(int tenantId, String executionPlanName, String hostName, int port) throws NotStormManagerException, TException {
        if (!isStormManager) {
            throw new NotStormManagerException(hostPort + " not a storm manager");
        }
        insertToCollection(stormReceivers, constructKey(tenantId, executionPlanName), new Endpoint(port, hostName));

    }

    @Override
    public void registerCEPPublisher(int tenantId, String executionPlanName, String hostName, int port) throws NotStormManagerException, TException {
        if (!isStormManager) {
            throw new NotStormManagerException(hostPort + " not a storm manager");
        }
        insertToCollection(cepPublishers, constructKey(tenantId, executionPlanName), new Endpoint(port, hostName));
    }

    @Override
    public String getStormReceiver(int tenantId, String executionPlanName, String cepReceiverHostName) throws NotStormManagerException, EndpointNotFoundException, TException {
        if (!isStormManager) {
            throw new NotStormManagerException(hostPort + " not a storm manager");
        }
        Set<Endpoint> endpointSet = stormReceivers.get(constructKey(tenantId, executionPlanName));
        Endpoint selectedEndpoint = selectEndpoint(endpointSet, cepReceiverHostName);

        if (null != selectedEndpoint) {
            return selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort();
        } else {
            throw new EndpointNotFoundException("No Storm Receiver for executionPlanName: " + executionPlanName + " of tenantId:" + tenantId + " for CEP Receiver form:" + cepReceiverHostName);
        }
    }

    @Override
    public String getCEPPublisher(int tenantId, String executionPlanName, String stormPublisherHostName) throws NotStormManagerException, EndpointNotFoundException, TException {
        if (!isStormManager) {
            throw new NotStormManagerException(hostPort + " not a storm manager");
        }
        Set<Endpoint> endpointSet = cepPublishers.get(constructKey(tenantId, executionPlanName));
        Endpoint selectedEndpoint = selectEndpoint(endpointSet, stormPublisherHostName);

        if (null != selectedEndpoint) {
            return selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort();
        } else {
            throw new EndpointNotFoundException("No CEP Publisher for executionPlanName: " + executionPlanName + " of tenantId:" + tenantId + " for Storm Publisher form:" + stormPublisherHostName);
        }
    }

    private Endpoint selectEndpoint(Set<Endpoint> endpointSet, String requesterIp) {
        Endpoint selectedEndpoint = null;

        if (endpointSet != null && !endpointSet.isEmpty()) {
            // If  there's a storm receiver/cep publisher in the same host as requester IP select it
            if (!"".equals(requesterIp)) {
                for (Endpoint endpoint : endpointSet) {
                    if (endpoint.getHostName().equals(requesterIp)) {
                        selectedEndpoint = endpoint;

//                        if (log.isDebugEnabled()) {
//                            log.debug("Selecting" + endpoint.toString() + " since it's in the same host as the requester");
//                        }
                        break;
                    }
                }
            }
            // If there are no endpoints in the same host. Select the endpoint with lease number of connections
            if (selectedEndpoint == null) {
                int minConnectionCount = Integer.MAX_VALUE;

                for (Endpoint endpoint : endpointSet) {
//                    if (log.isDebugEnabled()) {
//                        log.debug("Endpoint " + endpoint.toString() + " has " + endpoint.getConnectionCount() + " connections.");
//                    }

                    if (endpoint.getConnectionCount() < minConnectionCount) {
                        minConnectionCount = endpoint.getConnectionCount();
                        selectedEndpoint = endpoint;
                    }
                }
            }
            if (selectedEndpoint != null) {
                selectedEndpoint.setConnectionCount(selectedEndpoint.getConnectionCount() + 1);

            }
        }
        return selectedEndpoint;
    }

    private static void insertToCollection(HashMap<String, Set<Endpoint>> collection, String key, Endpoint endpoint) {
        Set<Endpoint> endpointSet = collection.get(key);

        if (endpointSet == null) {
            endpointSet = new HashSet<Endpoint>();
            collection.put(key, endpointSet);
        }
        endpointSet.add(endpoint);
    }

    private static String constructKey(int tenantId, String executionPlanName) {
        return tenantId + ":" + executionPlanName;
    }

    public void setStormManager(boolean stormManager) {
        this.isStormManager = stormManager;
    }


    private class Endpoint {
        //    public static final String ENDPOINT_TYPE_STORM_RECEIVER = "StormReceiver";
        //    public static final String ENDPOINT_TYPE_CEP_PUBLISHER = "CepPublisher";
        //
        //    enum EndpointType{
        //         STORM_RECEOVE
        //    }

        private int port;
        private String hostName;
        private int connectionCount = 0;
        //    private String type;

        Endpoint(int port, String hostName) {
            this.port = port;
            this.hostName = hostName;
            //        this.type = type;
        }

        public String getHostName() {
            return hostName;
        }

        public int getPort() {
            return port;
        }

        public void setConnectionCount(int connections) {
            connectionCount = connections;
        }

        public int getConnectionCount() {
            return connectionCount;
        }

        //    public String toString(){
        //        return ("[" + type + "]" + hostName + ":" + port);
        //    }
    }

    public boolean isStormManager() {
        return isStormManager;
    }
}
