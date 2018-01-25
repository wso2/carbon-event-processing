/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.storm.status.monitor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.internal.storm.status.monitor.exception.DeploymentStatusMonitorException;
import org.wso2.carbon.event.processor.core.util.DistributedModeConstants;
import org.wso2.carbon.event.processor.core.util.ExecutionPlanStatusHolder;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StormStatusMonitor implements ConnectionCallback {

    private static final Log log = LogFactory.getLog(StormStatusMonitor.class);

    private final String stormTopologyName;
    private final String executionPlanName;
    private final String executionPlanStatusHolderKey;
    private final ExecutorService executorService;
    private final int lockTimeout;
    private final String tenantDomain;
    private String hostIp = null;
    private AtomicInteger connectedCepReceiversCount;
    private int importedStreamsCount = 0;
    private AtomicInteger connectedPublisherBoltsCount;

    public StormStatusMonitor(int tenantId, String executionPlanName, int importedStreamsCount)
            throws DeploymentStatusMonitorException {
        if (EventProcessorValueHolder.getHazelcastInstance() == null) {
            throw new DeploymentStatusMonitorException("Couldn't initialize Distributed Deployment Status monitor as" +
                                                       " the hazelcast instance is null. Enable clustering and restart the server");
        }
        executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().
                setNameFormat("Thread pool- component - StormStatusMonitor.executorService;tenantId - " +
                        tenantId + ";executionPlanName - " + executionPlanName).build());
        tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        connectedCepReceiversCount = new AtomicInteger(0);
        connectedPublisherBoltsCount = new AtomicInteger(0);
        try {
            hostIp = HostAddressFinder.findAddress("localhost");
        } catch (SocketException e) {
            //do nothing. Let this be retried in the callbacks.
        }
        this.importedStreamsCount = importedStreamsCount;
        this.executionPlanName = executionPlanName;
        this.stormTopologyName = StormTopologyManager.getTopologyName(executionPlanName, tenantId);
        this.executionPlanStatusHolderKey = DistributedModeConstants.STORM_STATUS_MAP + "." + stormTopologyName;
        lockTimeout = EventProcessorValueHolder.getStormDeploymentConfiguration().getStatusLockTimeout();
        executorService.execute(new GlobalStatUpdater());
    }

    @Override
    public void onCepReceiverConnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
            try {
                if (hostIp == null) {
                    hostIp = HostAddressFinder.findAddress("localhost");
                }
                if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.MILLISECONDS)) {
                    try {
                        ExecutionPlanStatusHolder executionPlanStatusHolder =
                                executionPlanStatusHolderIMap.get(stormTopologyName);
                        if (executionPlanStatusHolder == null) {
                            log.error("Couldn't increment connected CEP receivers count for execution plan: " + executionPlanName +
                                      ", for tenant-domain: " + tenantDomain
                                      + " as status object not initialized by manager.");
                        } else {
                            executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.incrementAndGet(), importedStreamsCount);
                            executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                            if (log.isDebugEnabled()) {
                                log.debug("Incremented connected CEP receiver count as " + connectedCepReceiversCount.get() +
                                          " for execution plan: " + executionPlanName + ", for tenant-domain: " + tenantDomain
                                          + ", for IP address: " + hostIp);
                            }
                        }
                    } finally {
                        executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                    }
                } else {
                    log.error("Couldn't increment connected CEP receivers count for execution plan: " + executionPlanName +
                              ", for tenant-domain: " + tenantDomain
                              + " as the hazelcast lock acquisition failed.");
                }
            } catch (InterruptedException e) {
                log.error("Couldn't increment connected CEP receivers count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the hazelcast lock acquisition was interrupted.", e);
                Thread.currentThread().interrupt();
            } catch (SocketException e) {
                log.error("Couldn't increment connected CEP receivers count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the host IP couldn't be found for this node.", e);
            }
        } else {
            log.error("Couldn't increment connected CEP receivers count for execution plan: " + executionPlanName +
                      ", for tenant-domain: " + tenantDomain
                      + " as the hazelcast instance is not active or not available.");
        }
    }

    @Override
    public void onCepReceiverDisconnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
            try {
                if (hostIp == null) {
                    hostIp = HostAddressFinder.findAddress("localhost");
                }
                if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.MILLISECONDS)) {
                    try {
                        ExecutionPlanStatusHolder executionPlanStatusHolder =
                                executionPlanStatusHolderIMap.get(stormTopologyName);
                        if (executionPlanStatusHolder == null) {
                            log.error("Couldn't decrement connected CEP receivers count for execution plan: " + executionPlanName +
                                      ", for tenant-domain: " + tenantDomain
                                      + " as status object not initialized by manager.");
                        } else {
                            executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.decrementAndGet(), importedStreamsCount);
                            executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                            if (log.isDebugEnabled()) {
                                log.debug("Decremented connected CEP receiver count as " + connectedCepReceiversCount.get() +
                                          " for execution plan: " + executionPlanName + ", for tenant-domain: " + tenantDomain
                                          + ", for IP address: " + hostIp);
                            }
                        }
                    } finally {
                        executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                    }
                } else {
                    log.error("Couldn't decrement connected CEP receivers count for execution plan: " + executionPlanName +
                              ", for tenant-domain: " + tenantDomain
                              + " as the hazelcast lock acquisition failed.");
                }
            } catch (InterruptedException e) {
                log.error("Couldn't decrement connected CEP receivers count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the hazelcast lock acquisition was interrupted.", e);
                Thread.currentThread().interrupt();
            } catch (SocketException e) {
                log.error("Couldn't decrement connected CEP receivers count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the host IP couldn't be found for this node.", e);
            }
        } else {
            log.error("Couldn't decrement connected CEP receivers count for execution plan: " + executionPlanName +
                      ", for tenant-domain: " + tenantDomain
                      + " as the hazelcast instance is not active or not available.");
        }
    }

    @Override
    public void onPublisherBoltConnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
            try {
                if (hostIp == null) {
                    hostIp = HostAddressFinder.findAddress("localhost");
                }
                if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.MILLISECONDS)) {
                    try {
                        ExecutionPlanStatusHolder executionPlanStatusHolder =
                                executionPlanStatusHolderIMap.get(stormTopologyName);
                        if (executionPlanStatusHolder == null) {
                            log.error("Couldn't increment connected publisher bolts count for execution plan: " + executionPlanName +
                                      ", for tenant-domain: " + tenantDomain
                                      + " as status object not initialized by manager.");
                        } else {
                            executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp, connectedPublisherBoltsCount.incrementAndGet());
                            executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                            if (log.isDebugEnabled()) {
                                log.debug("Incremented connected publisher bolt count as " + connectedPublisherBoltsCount.get() +
                                          " for execution plan: " + executionPlanName + ", for tenant-domain: " + tenantDomain
                                          + ", for IP address: " + hostIp);
                            }
                        }
                    } finally {
                        executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                    }
                } else {
                    log.error("Couldn't increment connected publisher bolts count for execution plan: " + executionPlanName +
                              ", for tenant-domain: " + tenantDomain
                              + " as the hazelcast lock acquisition failed.");
                }
            } catch (InterruptedException e) {
                log.error("Couldn't increment connected publisher bolts count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the hazelcast lock acquisition was interrupted.", e);
                Thread.currentThread().interrupt();
            } catch (SocketException e) {
                log.error("Couldn't increment connected publisher bolts count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the host IP couldn't be found for this node.", e);
            }
        } else {
            log.error("Couldn't increment connected publisher bolts count for execution plan: " + executionPlanName +
                      ", for tenant-domain: " + tenantDomain
                      + " as the hazelcast instance is not active or not available.");
        }
    }

    @Override
    public void onPublisherBoltDisconnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
            try {
                if (hostIp == null) {
                    hostIp = HostAddressFinder.findAddress("localhost");
                }
                if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.MILLISECONDS)) {
                    try {
                        ExecutionPlanStatusHolder executionPlanStatusHolder =
                                executionPlanStatusHolderIMap.get(stormTopologyName);
                        if (executionPlanStatusHolder == null) {
                            log.error("Couldn't decrement connected publisher bolts count for execution plan: " + executionPlanName +
                                      ", for tenant-domain: " + tenantDomain
                                      + " as status object not initialized by manager.");
                        } else {
                            executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp, connectedPublisherBoltsCount.decrementAndGet());
                            executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                            if (log.isDebugEnabled()) {
                                log.debug("Decremented connected publisher bolt count as " + connectedPublisherBoltsCount.get() +
                                          " for execution plan: " + executionPlanName + ", for tenant-domain: " + tenantDomain
                                          + ", for IP address: " + hostIp);
                            }
                        }
                    } finally {
                        executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                    }
                } else {
                    log.error("Couldn't decrement connected publisher bolts count for execution plan: " + executionPlanName +
                              ", for tenant-domain: " + tenantDomain
                              + " as the hazelcast lock acquisition failed.");
                }
            } catch (InterruptedException e) {
                log.error("Couldn't decrement connected publisher bolts count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the hazelcast lock acquisition was interrupted.", e);
                Thread.currentThread().interrupt();
            } catch (SocketException e) {
                log.error("Couldn't decrement connected publisher bolts count for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the host IP couldn't be found for this node.", e);
            }
        } else {
            log.error("Couldn't decrement connected publisher bolts count for execution plan: " + executionPlanName +
                      ", for tenant-domain: " + tenantDomain
                      + " as the hazelcast instance is not active or not available.");
        }
    }

    public void hazelcastListenerCallback() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
            try {
                if (hostIp == null) {
                    hostIp = HostAddressFinder.findAddress("localhost");
                }
                if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.MILLISECONDS)) {
                    try {
                        ExecutionPlanStatusHolder executionPlanStatusHolder =
                                executionPlanStatusHolderIMap.get(stormTopologyName);
                        if (executionPlanStatusHolder == null) {
                            log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                                      ", for tenant-domain: " + tenantDomain
                                      + " as status object not initialized by manager.");
                        } else {
                            executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.get(), importedStreamsCount);
                            executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp, connectedPublisherBoltsCount.get());
                            executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                            if (log.isDebugEnabled()) {
                                log.debug("Updated distributed deployment status as follows. " +
                                          "\nConnected CEP receivers count: " + connectedCepReceiversCount.get() +
                                          "\nConnected publisher bolts count: " + connectedPublisherBoltsCount.get() +
                                          "\nfor execution plan: " + executionPlanName + ", for tenant-domain: " + tenantDomain
                                          + ", for IP address: " + hostIp);
                            }
                        }
                    } finally {
                        executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                    }
                } else {
                    log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                              ", for tenant-domain: " + tenantDomain
                              + " as the hazelcast lock acquisition failed.");
                }
            } catch (InterruptedException e) {
                log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the hazelcast lock acquisition was interrupted.", e);
                Thread.currentThread().interrupt();
            } catch (SocketException e) {
                log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                          ", for tenant-domain: " + tenantDomain
                          + " as the host IP couldn't be found for this node.", e);
            }
        } else {
            log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                      ", for tenant-domain: " + tenantDomain
                      + " as the hazelcast instance is not active or not available.");
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    /**
     * Updates the ExecutionPlanStatusHolder periodically.
     */
    class GlobalStatUpdater implements Runnable {

        private final int updateRate;

        GlobalStatUpdater() {
            updateRate = EventProcessorValueHolder.getStormDeploymentConfiguration().getStatusUpdateInterval();
        }

        @Override
        public void run() {
            while (true) {

                /**
                 * Update
                 */
                HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
                if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
                    IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
                    try {
                        if (hostIp == null) {
                            hostIp = HostAddressFinder.findAddress("localhost");
                        }
                        if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.MILLISECONDS)) {
                            try {
                                ExecutionPlanStatusHolder executionPlanStatusHolder =
                                        executionPlanStatusHolderIMap.get(stormTopologyName);
                                if (executionPlanStatusHolder == null) {
                                    log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                                              ", for tenant-domain: " + tenantDomain
                                              + " as status object not initialized by manager.");
                                } else {
                                    executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.get(), importedStreamsCount);
                                    executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp, connectedPublisherBoltsCount.get());
                                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Updated distributed deployment status as follows. " +
                                                  "\nConnected CEP receivers count: " + connectedCepReceiversCount.get() +
                                                  "\nConnected publisher bolts count: " + connectedPublisherBoltsCount.get() +
                                                  "\nfor execution plan: " + executionPlanName + ", for tenant-domain: " + tenantDomain
                                                  + ", for IP address: " + hostIp);
                                    }
                                }
                            } finally {
                                executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                            }
                        } else {
                            log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                                      ", for tenant-domain: " + tenantDomain
                                      + " as the hazelcast lock acquisition failed.");
                        }
                    } catch (InterruptedException e) {
                        log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                                  ", for tenant-domain: " + tenantDomain
                                  + " as the hazelcast lock acquisition was interrupted.", e);
                        Thread.currentThread().interrupt();
                        return;
                    } catch (SocketException e) {
                        log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                                  ", for tenant-domain: " + tenantDomain
                                  + " as the host IP couldn't be found for this node.", e);
                    }
                } else {
                    log.error("Couldn't update distributed deployment status for execution plan: " + executionPlanName +
                              ", for tenant-domain: " + tenantDomain
                              + " as the hazelcast instance is not active or not available.");
                }

                /**
                 * Sleep
                 */
                try {
                    Thread.sleep(updateRate);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (log.isDebugEnabled()) {
                        log.debug("GlobalStatUpdater was interrupted, hence returning. " +
                                  "Details: execution plan name: " + executionPlanName + ", tenant domain: " + tenantDomain);
                    }
                    return;
                }
            }
        }
    }
}
