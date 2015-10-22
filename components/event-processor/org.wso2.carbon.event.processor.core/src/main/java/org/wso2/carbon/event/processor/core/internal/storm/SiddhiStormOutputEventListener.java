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
package org.wso2.carbon.event.processor.core.internal.storm;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.util.ThroughputProbe;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Receives events from the Event publisher bolt running on storm. There will be one SiddhiStormOutputEventListener instance
 * per execution plan per tenant (all exported streams of execution plan are handled form a single SiddhiStormOutputEventListener). When events are
 * received from storm, the event  will be directed to the relevant output stream listener depending on the stream to forward
 * the event to the relevant output adaptor for the stream.
 */
public class SiddhiStormOutputEventListener implements StreamCallback {
    private static final Logger log = Logger.getLogger(SiddhiStormOutputEventListener.class);
    private ExecutionPlanConfiguration executionPlanConfiguration;
    private int listeningPort;
    private int tenantId;
    private final DistributedConfiguration stormDeploymentConfig;
    private String thisHostIp;
    private HashMap<String, SiddhiOutputStreamListener> streamNameToOutputStreamListenerMap = new HashMap<>();
    private TCPEventServer tcpEventServer;
    private String logPrefix = "";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int heartbeatInterval;
    private ThroughputProbe inputThroughputProbe;

    private final ConnectionCallback connectionCallback;

    public SiddhiStormOutputEventListener(ExecutionPlanConfiguration executionPlanConfiguration, int tenantId,
                                          DistributedConfiguration stormDeploymentConfig, ConnectionCallback connectionCallback) {
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.tenantId = tenantId;
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.heartbeatInterval = stormDeploymentConfig.getManagementHeartbeatInterval();
        this.connectionCallback = connectionCallback;
        init();
    }

    private void init() {
        logPrefix = "[" + tenantId + ":" + executionPlanConfiguration.getName() + ":" + "CEPPublisher" + "] ";
        log.info(logPrefix + "Initializing storm output event listener");
        inputThroughputProbe = new ThroughputProbe(logPrefix + "-Receive", 10);
        inputThroughputProbe.startSampling();
        try {
            thisHostIp = HostAddressFinder.findAddress("localhost");
            listeningPort = findPort(thisHostIp);
            TCPEventServerConfig configs = new TCPEventServerConfig(thisHostIp, listeningPort);
            tcpEventServer = new TCPEventServer(configs, this, connectionCallback);
            tcpEventServer.start();
            executorService.execute(new Registrar());
        } catch (Exception e) {
            log.error(logPrefix + "Failed to start event listener", e);
        }
    }


    public void registerOutputStreamListener(StreamDefinition siddhiStreamDefinition, SiddhiOutputStreamListener outputStreamListener) {
        log.info(logPrefix + "Registering output stream listener for Siddhi stream : " + siddhiStreamDefinition.getId());
        streamNameToOutputStreamListenerMap.put(siddhiStreamDefinition.getId(), outputStreamListener);
        tcpEventServer.addStreamDefinition(siddhiStreamDefinition);
    }

    @Override
    public void receive(String streamId, long timestamp, Object[] eventData) {
        SiddhiOutputStreamListener outputStreamListener = streamNameToOutputStreamListenerMap.get(streamId);
        if (outputStreamListener != null) {
            outputStreamListener.sendEvent(new Event(timestamp, eventData));
            inputThroughputProbe.update();
        } else {
            log.warn("Cannot find output event listener for stream " + streamId + " in execution plan " + executionPlanConfiguration.getName()
                    + " of tenant " + tenantId + ". Discarding Event:" + streamId +
                    ":" + Arrays.deepToString(eventData) + "@" + timestamp);
        }
    }

    private int findPort(String host) throws Exception {
        for (int i = stormDeploymentConfig.getTransportMinPort(); i <= stormDeploymentConfig.getTransportMaxPort(); i++) {
            if (!Utils.isPortUsed(i, host)) {
                return i;
            }
        }
        throw new Exception("Cannot find free port in range " + stormDeploymentConfig.getTransportMinPort() + "~" + stormDeploymentConfig.getTransportMaxPort());
    }

    public void shutdown() {
        executorService.shutdown();
        tcpEventServer.shutdown();
    }


    class Registrar implements Runnable {
        private String managerHost;
        private int managerPort;

        @Override
        public void run() {
            log.info(logPrefix + "Registering CEP publisher for " + thisHostIp + ":" + listeningPort);

            // Infinitely call register. Each register call will act as a heartbeat
            while (true) {
                if (registerCEPPublisherWithStormMangerService()) {
                    while (true) {
                        TTransport transport = null;
                        try {
                            transport = new TSocket(managerHost, managerPort);
                            TProtocol protocol = new TBinaryProtocol(transport);
                            transport.open();

                            StormManagerService.Client client = new StormManagerService.Client(protocol);
                            client.registerCEPPublisher(tenantId, executionPlanConfiguration.getName(), thisHostIp,
                                    listeningPort);
                            if (log.isDebugEnabled()) {
                                log.debug(logPrefix + "Successfully registered CEP publisher for " + thisHostIp + ":" +
                                        listeningPort);
                            }
                            try {
                                Thread.sleep(heartbeatInterval);
                            } catch (InterruptedException e1) {
                                Thread.currentThread().interrupt();
                            }
                        } catch (Exception e) {
                            log.error(logPrefix + "Error in registering CEP publisher for " + thisHostIp + ":" +
                                    listeningPort + " with manager " + managerHost + ":" + managerPort + ". Trying " +
                                    "next manager after " + heartbeatInterval + "ms", e);
                            break;
                        } finally {
                            if (transport != null) {
                                transport.close();
                            }
                        }
                    }
                } else {
                    log.error(logPrefix + "Error registering CEP publisher with current manager. Retrying " +
                            "after " + heartbeatInterval + "ms");
                }
                try {
                    Thread.sleep(heartbeatInterval);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }

        }

        private boolean registerCEPPublisherWithStormMangerService() {
            TTransport transport = null;
            for (HostAndPort endpoint : stormDeploymentConfig.getManagers()) {
                try {
                    transport = new TSocket(endpoint.getHostName(), endpoint.getPort());
                    TProtocol protocol = new TBinaryProtocol(transport);
                    transport.open();

                    StormManagerService.Client client = new StormManagerService.Client(protocol);
                    client.registerCEPPublisher(tenantId, executionPlanConfiguration.getName(), thisHostIp,
                            listeningPort);
                    log.info(logPrefix + "Successfully registered CEP publisher for " + thisHostIp + ":" +
                            listeningPort + "  with manager service at" + endpoint.getHostName() + ":" + endpoint.getPort());
                    managerHost = endpoint.getHostName();
                    managerPort = endpoint.getPort();
                    return true;
                } catch (Exception e) {
                    log.error(logPrefix + "Error in registering CEP publisher for " + thisHostIp + ":" +
                            listeningPort + " with manager " + endpoint.getHostName() + ":" + endpoint.getPort() + "." +
                            " Trying next manager", e);
                    continue;
                } finally {
                    if (transport != null) {
                        transport.close();
                    }
                }
            }
            return false;
        }
    }
}
