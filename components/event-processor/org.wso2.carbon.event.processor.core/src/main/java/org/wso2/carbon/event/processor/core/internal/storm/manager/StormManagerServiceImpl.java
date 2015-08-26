/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.storm.manager;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.EndpointNotFoundException;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.NotStormCoordinatorException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StormManagerServiceImpl implements StormManagerService.Iface {
    private static  Logger log = Logger.getLogger(StormManagerServiceImpl.class);
    public static final long MILLISECONDS_PER_MINUTE = 60000;
    private ConcurrentHashMap<String, Set<Endpoint>> stormReceivers = new ConcurrentHashMap<String, Set<Endpoint>>();
    private ConcurrentHashMap<String, Set<Endpoint>> cepPublishers = new ConcurrentHashMap<String, Set<Endpoint>>();
    private boolean isStormCoordinator;
    private String hostPort;

    public StormManagerServiceImpl(String hostPort) {
        this.hostPort = hostPort;
    }

    @Override
    public void registerStormReceiver(int tenantId, String executionPlanName, String hostName, int port) throws NotStormCoordinatorException, TException {
        if (!isStormCoordinator) {
            throw new NotStormCoordinatorException(hostPort + " not a storm coordinator");
        }
        insertToCollection(stormReceivers, constructKey(tenantId, executionPlanName), new Endpoint(port, hostName));
    }

    @Override
    public void registerCEPPublisher(int tenantId, String executionPlanName, String hostName, int port) throws NotStormCoordinatorException, TException {
        if (!isStormCoordinator) {
            throw new NotStormCoordinatorException(hostPort + " not a storm coordinator");
        }
        insertToCollection(cepPublishers, constructKey(tenantId, executionPlanName), new Endpoint(port, hostName));
    }

    @Override
    public synchronized String getStormReceiver(int tenantId, String executionPlanName, String cepReceiverHostName) throws NotStormCoordinatorException, EndpointNotFoundException, TException {
        if (!isStormCoordinator) {
            throw new NotStormCoordinatorException(hostPort + " not a storm coordinator");
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
    public synchronized String getCEPPublisher(int tenantId, String executionPlanName, String stormPublisherHostName) throws NotStormCoordinatorException, EndpointNotFoundException, TException {
        if (!isStormCoordinator) {
            throw new NotStormCoordinatorException(hostPort + " not a storm coordinator");
        }
        Set<Endpoint> endpointSet = cepPublishers.get(constructKey(tenantId, executionPlanName));
        Endpoint selectedEndpoint = getEndpoint(endpointSet, stormPublisherHostName);

        if (null != selectedEndpoint) {
            return selectedEndpoint.getHostName() + ":" + selectedEndpoint.getPort();
        } else {
            throw new EndpointNotFoundException("No CEP Publisher for executionPlanName: " + executionPlanName + " of tenantId:" + tenantId + " for Storm Publisher form:" + stormPublisherHostName);
        }
    }

    public synchronized void deleteExecPlanEndpoints(int tenantId, String executionPlanName){
        Set<Endpoint> endpointSet = cepPublishers.get(constructKey(tenantId, executionPlanName));
        if (endpointSet != null){
            cepPublishers.remove(constructKey(tenantId, executionPlanName));
        }

        endpointSet = stormReceivers.get(constructKey(tenantId, executionPlanName));
        if (endpointSet != null){
            stormReceivers.remove(constructKey(tenantId, executionPlanName));
        }

        log.info("Removed all end point details related to '" + constructKey(tenantId, executionPlanName) + "' from Manager service.");
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

    private synchronized Endpoint selectEndpoint(Set<Endpoint> endpointSet) {
        Endpoint selectedEndpoint = null;
        int minConnectionCount = Integer.MAX_VALUE;
        for (Endpoint endpoint : endpointSet) {
            if (endpoint.getConnectionCount() < minConnectionCount){
                if (endpoint.getLastRegisterTimestamp() >= (System.currentTimeMillis() - MILLISECONDS_PER_MINUTE)){
                    minConnectionCount = endpoint.getConnectionCount();
                    selectedEndpoint = endpoint;
                }else{

                    log.warn("Ignoring endpoint " + endpoint.getHostName() + ":" + endpoint.getPort() + " because it has not sent a heart beat for "
                            + (int) Math.floor((System.currentTimeMillis() - endpoint.getLastRegisterTimestamp()) / MILLISECONDS_PER_MINUTE) + " min(s)");
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

    public void setStormCoordinator(boolean isStormCoordinator) {
        this.isStormCoordinator = isStormCoordinator;
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

    public boolean isStormCoordinator() {
        return isStormCoordinator;
    }
}
