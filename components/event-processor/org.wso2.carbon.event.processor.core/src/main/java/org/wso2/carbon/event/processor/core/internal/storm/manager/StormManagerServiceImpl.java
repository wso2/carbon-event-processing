package org.wso2.carbon.event.processor.core.internal.storm.manager;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.EndpointNotFoundException;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.NotStormManagerException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StormManagerServiceImpl implements StormManagerService.Iface {
    private static  Logger log = Logger.getLogger(StormManagerServiceImpl.class);
    public static final long MILLISECONDS_PER_MINUTE = 60000;
    private ConcurrentHashMap<String, Set<Endpoint>> stormReceivers = new ConcurrentHashMap<String, Set<Endpoint>>();
    private ConcurrentHashMap<String, Set<Endpoint>> cepPublishers = new ConcurrentHashMap<String, Set<Endpoint>>();
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
    public synchronized String getStormReceiver(int tenantId, String executionPlanName, String cepReceiverHostName) throws NotStormManagerException, EndpointNotFoundException, TException {
        if (!isStormManager) {
            throw new NotStormManagerException(hostPort + " not a storm manager");
        }
        Set<Endpoint> endpointSet = stormReceivers.get(constructKey(tenantId, executionPlanName));
        Endpoint selectedEndpoint = getEndpoint(endpointSet, cepReceiverHostName);

        if (null != selectedEndpoint) {
            return selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort();
        } else {
            throw new EndpointNotFoundException("No Storm Receiver for executionPlanName: " + executionPlanName + " of tenantId:" + tenantId + " for CEP Receiver form:" + cepReceiverHostName);
        }
    }

    @Override
    public synchronized String getCEPPublisher(int tenantId, String executionPlanName, String stormPublisherHostName) throws NotStormManagerException, EndpointNotFoundException, TException {
        if (!isStormManager) {
            throw new NotStormManagerException(hostPort + " not a storm manager");
        }
        Set<Endpoint> endpointSet = cepPublishers.get(constructKey(tenantId, executionPlanName));
        Endpoint selectedEndpoint = getEndpoint(endpointSet, stormPublisherHostName);

        if (null != selectedEndpoint) {
            return selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort();
        } else {
            throw new EndpointNotFoundException("No CEP Publisher for executionPlanName: " + executionPlanName + " of tenantId:" + tenantId + " for Storm Publisher form:" + stormPublisherHostName);
        }
    }

    private synchronized Endpoint getEndpoint(Set<Endpoint> endpointSet, String requesterIp) {
        Endpoint selectedEndpoint = null;

        Set<Endpoint> sameHostEndpoints = new HashSet<Endpoint>();
        if (endpointSet != null && !endpointSet.isEmpty()) {

            if (!"".equals(requesterIp)) {
                for (Endpoint endpoint : endpointSet) {
                    if (endpoint.getHostName().equals(requesterIp)) {
                        sameHostEndpoints.add(endpoint);
                    }
                }
            }

            // If  there's a storm receivers/cep publishers in the same host as requester IP select among them
            if (!sameHostEndpoints.isEmpty()) {
                selectedEndpoint = selectEndpoint(sameHostEndpoints);
            }else{
                selectedEndpoint = selectEndpoint(endpointSet);
            }

            if (selectedEndpoint != null) {
                selectedEndpoint.setConnectionCount(selectedEndpoint.getConnectionCount() + 1);

            }
        }
        return selectedEndpoint;
    }

    // TODO : Heart beat time configurable
    private synchronized Endpoint selectEndpoint(Set<Endpoint> endpointSet) {
        Endpoint selectedEndpoint = null;
        int minConnectionCount = Integer.MAX_VALUE;
        for (Endpoint endpoint : endpointSet) {
            if (endpoint.getConnectionCount() < minConnectionCount){
                if (endpoint.getLastRegisterTimestamp() >= (System.currentTimeMillis() - MILLISECONDS_PER_MINUTE)){
                    minConnectionCount = endpoint.getConnectionCount();
                    selectedEndpoint = endpoint;
                }else{
                    log.warn("End point " + endpoint.getHostName() + ":" + endpoint.getPort() + " have not send a heart beat for "
                            + Math.floor(System.currentTimeMillis() - endpoint.getLastRegisterTimestamp()/ MILLISECONDS_PER_MINUTE) + "mins");
                }
            }
        }
        return selectedEndpoint;
    }

    private static synchronized void insertToCollection(ConcurrentHashMap<String, Set<Endpoint>> collection, String key, Endpoint endpoint) {
        Set<Endpoint> endpointSet = collection.get(key);
        boolean isHeartbeat = false;

        if (endpointSet == null) {
            endpointSet = new HashSet<Endpoint>();
            collection.put(key, endpointSet);
        }else{
            for (Endpoint currentEndpoint : endpointSet){
                if (currentEndpoint.equals(endpoint)){
                    isHeartbeat = true;
                    currentEndpoint.updateLastRegisteredTimestamp();
                    break;
                }
            }
        }

        if (!isHeartbeat){
            endpointSet.add(endpoint);
        }
    }

    private static String constructKey(int tenantId, String executionPlanName) {
        return tenantId + ":" + executionPlanName;
    }

    public void setStormManager(boolean stormManager) {
        this.isStormManager = stormManager;
    }

    private class Endpoint {
        private int port;
        private String hostName;
        private int connectionCount = 0;
        private long lastRegisterTimestamp;

        Endpoint(int port, String hostName) {
            this.port = port;
            this.hostName = hostName;
            this.lastRegisterTimestamp = System.currentTimeMillis();
        }

        public long getLastRegisterTimestamp(){return lastRegisterTimestamp;}

        public void updateLastRegisteredTimestamp(){
            lastRegisterTimestamp = System.currentTimeMillis();
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

        @Override
        public boolean equals(Object object){
            if (object == null || (this.getClass() != object.getClass())){
                return false;
            }
            final Endpoint argument = (Endpoint)object;

            return ((this.hostName.equals(argument.getHostName())) && (this.port == argument.getPort()));
        }
    }

    public boolean isStormManager() {
        return isStormManager;
    }
}
