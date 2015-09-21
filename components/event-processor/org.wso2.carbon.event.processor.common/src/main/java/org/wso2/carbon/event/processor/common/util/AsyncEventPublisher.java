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

package org.wso2.carbon.event.processor.common.util;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.EndpointNotFoundException;
import org.wso2.carbon.event.processor.common.storm.manager.service.exception.NotStormCoordinatorException;
import org.wso2.carbon.event.processor.manager.commons.transport.client.ConnectionFailureHandler;
import org.wso2.carbon.event.processor.manager.commons.transport.client.TCPEventPublisher;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Sending events asynchronously from "CEP Receiver" -> "Storm Receiver" and
 * "Storm Publisher" -> "CEP Publisher" using TCPEventPublisher. This will
 * discover and connect to the endpoint(i.e. Storm Receiver/CEP Publisher) to
 * send events by talking to "Storm Management" service using EndpointConnectionCreator.
 */
public class AsyncEventPublisher implements EventHandler<AsynchronousEventBuffer.DataHolder>, ConnectionFailureHandler {
    public enum DestinationType {STORM_RECEIVER, CEP_PUBLISHER}

    private transient Logger log = Logger.getLogger(AsyncEventPublisher.class);
    private String logPrefix;

    private String destinationTypeString;
    private String publisherTypeString;
    private DestinationType destinationType;

    private Set<StreamDefinition> streams;
    private String executionPlanName;
    private int tenantId;
    private String thisHostIp;
    private List<HostAndPort> managerServiceEndpoints;
    private DistributedConfiguration stormDeploymentConfig;
    AsynchronousEventBuffer eventSendBuffer = null;

    private ConnectionCallback connectionCallback;

    private TCPEventPublisher tcpEventPublisher = null;
    private EndpointConnectionCreator endpointConnectionCreator;

    private boolean shutdown = false;

    private ThroughputProbe inputThroughputProbe;
    private ThroughputProbe publishThroughputProbe;

    /**
     * @param connectionCallback is a callback, invoked on connect() and disconnect() methods of TCPEventPublisher. Set to null if the callback is not needed.
     */
    public AsyncEventPublisher(DestinationType destinationType, Set<StreamDefinition> streams,
                               List<HostAndPort> managerServiceEndpoints,
                               String executionPlanName, int tenantId, DistributedConfiguration stormDeploymentConfig,
                               ConnectionCallback connectionCallback) {
        this.destinationType = destinationType;
        this.streams = streams;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.managerServiceEndpoints = managerServiceEndpoints;
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.connectionCallback = connectionCallback;
        this.endpointConnectionCreator = new EndpointConnectionCreator();

        this.destinationTypeString = (destinationType == DestinationType.STORM_RECEIVER) ? "StormReceiver" : "CEPPublisher";
        this.publisherTypeString = (destinationType == DestinationType.STORM_RECEIVER) ? "CEPReceiver" : "PublisherBolt";

        int bufferSize = (publisherTypeString.equals("CEPReceiver")) ?
                stormDeploymentConfig.getCepReceiverOutputQueueSize() : stormDeploymentConfig.getStormPublisherOutputQueueSize();
        eventSendBuffer = new AsynchronousEventBuffer<Object[]>(bufferSize, this);

        this.logPrefix = "[" + tenantId + ":" + executionPlanName + ":" + publisherTypeString + "] ";
    }

    /**
     * Initialize and try to make a connection with remote endpoint
     *
     * @param sync if set to true returns only after obtaining a connection to the remote endpoint. Otherwise initialization happens on a newly spawned
     *             thread and this method returns immediately.
     */
    public void initializeConnection(boolean sync) {
        try {
            this.thisHostIp = Utils.findAddress("localhost");

            if (sync) {
                endpointConnectionCreator.establishConnection();
            } else {
                Thread thread = new Thread(endpointConnectionCreator);
                thread.start();
            }
            inputThroughputProbe = new ThroughputProbe(logPrefix + "-In", 10);
            publishThroughputProbe = new ThroughputProbe(logPrefix + " -Publish", 10);

            inputThroughputProbe.startSampling();
            publishThroughputProbe.startSampling();

        } catch (SocketException e) {
            log.error(logPrefix + "Error while trying to obtain this host IP address", e);
        }
    }

    /**
     * Add event to the outbound event buffer and this call will return. Event will be sent asynchronously by disruptor consumer thread
     * via AsyncEventPublisher#onEvent.
     *
     * @param eventData
     * @param streamId
     */
    public void sendEvent(Object[] eventData, long timestamp, String streamId) {
        eventSendBuffer.addEvent(eventData, timestamp, streamId);
        inputThroughputProbe.update();
    }

    /**
     * Callback from disruptor for the consumer to consume data. This is where events are actually dispatched to the remote end.
     * If an exception occurs when trying send data it will keep trying to send for ever until succeeds. Returns only
     * after sending the event.
     *
     * @param dataHolder
     * @param sequence
     * @param endOfBatch
     */
    @Override
    public void onEvent(AsynchronousEventBuffer.DataHolder dataHolder, long sequence, boolean endOfBatch) {
        while (tcpEventPublisher == null) {
            log.info(logPrefix + "Can't send event. TCP event publisher not initialized. Waiting " + stormDeploymentConfig.getTransportReconnectInterval() + "s");
            try {
                synchronized (this) {
                    if (shutdown) {
                        log.info(logPrefix + "Aborting retry to send events. AsyncEventPublisher has shutdown.");
                        return;
                    }
                }
                Thread.sleep(stormDeploymentConfig.getTransportReconnectInterval());
            } catch (InterruptedException e) {
            }
        }

        // TODO : comment on message lost of the last batch
        try {
            tcpEventPublisher.sendEvent(dataHolder.getStreamId(), dataHolder.getTimestamp(), (Object[]) dataHolder.getData(), endOfBatch);
            publishThroughputProbe.update();
        } catch (IOException e) {
            log.error(logPrefix + "Error while trying to send event to " + destinationTypeString + " at " + tcpEventPublisher.getHostUrl(), e);
            reconnect();
            onEvent(dataHolder, sequence, endOfBatch);
        }
    }

    /**
     * First tires to reconnect to the already obtained end point. If failed re-initialize the connection.
     */
    private void reconnect() {
        String destinationHostPort = tcpEventPublisher.getHostUrl();
        resetTCPEventPublisher();
        // Retrying to connect to the existing endpoint.
        tcpEventPublisher = endpointConnectionCreator.connectToEndpoint(destinationHostPort, 3);
        // Initialize connection from the beginning if can't connect to the existing endpoint
        if (tcpEventPublisher == null) {
            log.error(logPrefix + "Failed to connect to existing " + destinationTypeString + " at " + destinationHostPort + ". Reinitializing connection process");
            initializeConnection(true);
        }
    }

    private void resetTCPEventPublisher() {
        if (tcpEventPublisher != null) {
            tcpEventPublisher.terminate();
            tcpEventPublisher = null;
        }
    }

    @Override
    protected void finalize() {
        if (tcpEventPublisher != null) {
            tcpEventPublisher.shutdown();
            tcpEventPublisher = null;
        }
    }

    public void shutdown() {
        synchronized (this) {
            shutdown = true;
        }
        eventSendBuffer.terminate();
        finalize();
    }

    @Override
    public void onConnectionFail(Exception e) {
        if (log.isDebugEnabled()) {
            log.debug("Pinging failed to " + tcpEventPublisher.getHostUrl() + ". Trying to re-connect.");
        }

        if (!shutdown) {
            reconnect();
        } else {
            log.info("Not trying to reconnect to " + tcpEventPublisher.getHostUrl() + " because event publisher is shutdown");
        }
    }

    /**
     * Creates connection to remote endpoint. First, talk to manager service to retrieve endpoint for given execution plan and tenant.
     * And then creates a connection to connect to remote endpoint.
     */
    class EndpointConnectionCreator implements Runnable {
        /**
         * Get the IP and the port of CEP Publisher/ Storm Receive by talking to Storm manager service.
         * Returns only after retrieving information from manager service. In case of a failure keep trying
         * to connect to manager service.
         *
         * @return endpoint Host and port in <ip>:<port> format
         */
        public String getEndpointFromManagerService() {
            String endpointHostPort = null;
            do {
                for (HostAndPort endpoint : managerServiceEndpoints) {
                    TTransport transport = null;
                    try {
                        transport = new TSocket(endpoint.getHostName(), endpoint.getPort());
                        TProtocol protocol = new TBinaryProtocol(transport);
                        transport.open();
                        StormManagerService.Client client = new StormManagerService.Client(protocol);

                        if (destinationType == DestinationType.CEP_PUBLISHER) {
                            endpointHostPort = client.getCEPPublisher(tenantId, executionPlanName, thisHostIp);
                        } else {
                            endpointHostPort = client.getStormReceiver(tenantId, executionPlanName, thisHostIp);
                        }
                        log.info(logPrefix + "Retrieved " + destinationTypeString + " at " + endpointHostPort + " " +
                                "from storm manager service at " + endpoint.getHostName() + ":" + endpoint.getPort());
                        break;
                    } catch (NotStormCoordinatorException e) {
                        log.info(logPrefix + "Cannot retrieve " + destinationType.name() +
                                " endpoint information from storm manager service at " +
                                endpoint.getHostName() + ":" + endpoint.getPort() + " as it's not a Storm coordinator, Trying next Storm manager.");

                    } catch (TTransportException e) {
                        log.info(logPrefix + "Cannot retrieve " + destinationType.name() +
                                " endpoint information from storm manager service at " +
                                endpoint.getHostName() + ":" + endpoint.getPort() + " as it's not reachable, " + e.getMessage() + ". Trying next Storm manager.");

                    } catch (TException e) {
                        log.info(logPrefix + "Cannot retrieve " + destinationType.name() +
                                " endpoint information from storm manager service at " +
                                endpoint.getHostName() + ":" + endpoint.getPort() + " as it's not reachable, " + e.getMessage() + ". Trying next Storm manager.");

                    } catch (EndpointNotFoundException e) {
                        log.info(logPrefix + destinationType.name() +
                                " endpoint information not available on storm manager service at " +
                                endpoint.getHostName() + ":" + endpoint.getPort() + ". Trying next Storm manager.");
                    } finally {
                        if (transport != null) {
                            transport.close();
                        }
                    }
                }

                synchronized (AsyncEventPublisher.this) {
                    if (shutdown) {
                        log.info(logPrefix + "Stopping attempting to connect to Storm manager service. Async event publisher is shutdown");
                        return null;
                    }
                }

                if (endpointHostPort == null) {
                    try {
                        log.info(logPrefix + "Failed to retrieve " + destinationType.name() + " from given " +
                                "set of Storm Managers. Retrying to retrieve endpoint from manager " +
                                "service in " + stormDeploymentConfig.getManagementReconnectInterval()
                                + " ms to get a " + destinationTypeString);
                        Thread.sleep(stormDeploymentConfig.getManagementReconnectInterval());
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }

            } while (endpointHostPort == null);

            return endpointHostPort;
        }

        /**
         * Connect to a given endpoint (i.e. CEP publisher or storm receiver). In case of failure retry to connect. Returns only
         * after connecting to the endpoint or after reaching maximum attempts.
         *
         * @param endpoint      Destination Ip and port in <ip>:<port> format
         * @param retryAttempts maximum number of retry attempts. 0 means retry for ever.
         * @return Returns TCPEvent publisher to talk to endpoint or null if reaches maximum number of attempts without succeeding
         */
        public TCPEventPublisher connectToEndpoint(String endpoint, int retryAttempts) {
            TCPEventPublisher tcpEventPublisher = null;
            int attemptCount = 0;
            do {
                synchronized (AsyncEventPublisher.this) {
                    if (shutdown) {
                        log.info(logPrefix + "Stopping attempting to connect to endpoint " + endpoint + ". Async event publisher is shutdown");
                        return null;
                    }
                }

                try {
                    tcpEventPublisher = new TCPEventPublisher(endpoint, stormDeploymentConfig.constructTransportPublisherConfig(), true, connectionCallback);
                    StringBuilder streamsIDs = new StringBuilder();
                    for (StreamDefinition siddhiStreamDefinition : streams) {
                        tcpEventPublisher.addStreamDefinition(siddhiStreamDefinition);
                        streamsIDs.append(siddhiStreamDefinition.getId() + ",");
                    }
                    tcpEventPublisher.registerConnectionFailureHandler(AsyncEventPublisher.this);
                    log.info(logPrefix + "Connected to " + destinationTypeString + " at " + endpoint + " for the Stream(s) " + streamsIDs.toString());
                } catch (IOException e) {
                    log.info(logPrefix + "Cannot connect to " + destinationTypeString + " at " + endpoint + ", " + e.getMessage());
                    if (log.isDebugEnabled()) {
                        log.debug(logPrefix + "Cannot connect to " + destinationTypeString + " at " + endpoint, e);
                    }
                }

                if (tcpEventPublisher == null) {
                    ++attemptCount;
                    if (retryAttempts > 0 && (attemptCount > retryAttempts)) {
                        return null;
                    }

                    try {
                        log.info(logPrefix + "Retrying(" + attemptCount + ") to connect to " + destinationTypeString + " at " + endpoint + " in "
                                + stormDeploymentConfig.getTransportReconnectInterval() + "ms");
                        Thread.sleep(stormDeploymentConfig.getTransportReconnectInterval());
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            } while (tcpEventPublisher == null);

            return tcpEventPublisher;
        }

        /**
         * First connect to the manager service and retrieve endpoint ip and port. Then connect to the endpoint.
         * Returns only after completing these tasks. Keeps trying forever until succeeds.
         */
        public void establishConnection() {
            log.info(logPrefix + "Requesting a " + destinationTypeString + " for " + thisHostIp);

            do {
                String endpointHostPort = getEndpointFromManagerService();
                if (endpointHostPort != null) {
                    tcpEventPublisher = connectToEndpoint(endpointHostPort, 3);
                }
            } while (tcpEventPublisher == null);
        }

        @Override
        public void run() {
            establishConnection();
        }
    }
}

/**
 * Store events in a disruptor
 *
 * @param <Type> Type of data to be stored in buffer.
 */
class AsynchronousEventBuffer<Type> {
    private Disruptor<DataHolder> disruptor;
    private RingBuffer<DataHolder> ringBuffer;

    /**
     * Creates a AsynchronousEventBuffer instance
     *
     * @param bufferSize     size of the buffer
     * @param publishHandler Instance of publish handler which is responsible for consuming events in the buffer
     */
    public AsynchronousEventBuffer(int bufferSize, EventHandler publishHandler) {
        this.disruptor = new Disruptor<DataHolder>(new EventFactory<DataHolder>() {
            @Override
            public DataHolder newInstance() {
                return new DataHolder();
            }
        }, bufferSize, Executors.newSingleThreadExecutor());

        this.ringBuffer = disruptor.getRingBuffer();

        this.disruptor.handleEventsWith(publishHandler);

        disruptor.start();
    }

    public void addEvent(Type data, long timestamp, String streamId) {
        long sequenceNo = ringBuffer.next();
        try {
            DataHolder existingHolder = ringBuffer.get(sequenceNo);
            existingHolder.setData(data);
            existingHolder.setTimestamp(timestamp);
            existingHolder.setStreamId(streamId);
        } finally {
            ringBuffer.publish(sequenceNo);
        }
    }

    public void terminate() {
        disruptor.halt();
    }

    class DataHolder {
        Type data;
        String streamId;
        private long timestamp;

        public void setData(Type data) {
            this.data = data;
        }

        public Type getData() {
            return data;
        }

        public void setStreamId(String streamId) {
            this.streamId = streamId;
        }

        public String getStreamId() {
            return streamId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

    }
}


