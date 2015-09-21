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
package org.wso2.carbon.event.processor.common.storm.component;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.event.processor.common.storm.event.Event;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.util.ThroughputProbe;
import org.wso2.carbon.event.processor.manager.commons.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.manager.commons.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.manager.commons.utils.HostAndPort;
import org.wso2.carbon.event.processor.manager.commons.utils.Utils;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Receive events from CEP receivers through thrift receiver and pass through
 * the events as tuples to the connected component(i.e. Siddhi Bolt).
 */
public class EventReceiverSpout extends BaseRichSpout implements StreamCallback {
    private static transient Logger log = Logger.getLogger(EventReceiverSpout.class);
    /**
     * Listening port of the thrift receiver
     */
    private int listeningPort;
    private String thisHostIp;

    private DistributedConfiguration stormDeploymentConfig;
    /**
     * Siddhi stream definitions of all incoming streams. Required to declare output fields
     */
    private List<StreamDefinition> incomingStreamDefinitions;
    private TCPEventServer tcpEventServer;

    /**
     * Stream IDs of incoming streams
     */
    private List<String> incomingStreamIDs = new ArrayList<String>();

    /**
     * Store received events until nextTuple is called. This list has to be synchronized since
     * this is filled by the receiver thread of data bridge and consumed by the nextTuple which
     * runs on the worker thread of spout.
     */
    private transient LinkedBlockingQueue<Event> storedEvents = null;

    private SpoutOutputCollector spoutOutputCollector = null;

    private String executionPlanName;
    private int tenantId = -1234;
    private String logPrefix;
    private int heartbeatInterval;

    private transient ThroughputProbe inputThroughputProbe;
    private transient ThroughputProbe outputThroughputProbe;

    /**
     * Receives events from the CEP Receiver through Thrift using data bridge and pass through the events
     * to a downstream component as tupels.
     *
     * @param stormDeploymentConfig
     * @param incomingStreamDefinitions - Incoming Siddhi stream definitions
     * @param executionPlanName
     * @param tenantId
     */
    public EventReceiverSpout(DistributedConfiguration stormDeploymentConfig, List<String> incomingStreamDefinitions,
                              String executionPlanName, int tenantId, int heartbeatInterval) {
        this.incomingStreamDefinitions = new ArrayList<StreamDefinition>(incomingStreamDefinitions.size());
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.heartbeatInterval = heartbeatInterval;
        for (String definition : incomingStreamDefinitions) {
            this.incomingStreamDefinitions.add(SiddhiCompiler.parseStreamDefinition(definition));
        }
        this.logPrefix = "[" + tenantId + ":" + executionPlanName + ":" + "EventReceiverSpout] ";


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // Declaring all incoming streams as output streams because this spouts role is to pass through all the incoming events as tuples.
        for (StreamDefinition siddhiStreamDefinition : incomingStreamDefinitions) {
            List<String> attributeList = new ArrayList<>(Arrays.asList(siddhiStreamDefinition.getAttributeNameArray()));
            attributeList.add(0, "_timestamp");
            Fields fields = new Fields(attributeList);
            outputFieldsDeclarer.declareStream(siddhiStreamDefinition.getId(), fields);
            incomingStreamIDs.add(siddhiStreamDefinition.getId());
            log.info(logPrefix + "Declaring output fields for stream : " + siddhiStreamDefinition.getId());
        }
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        this.storedEvents = new LinkedBlockingQueue<Event>(stormDeploymentConfig.getStormSpoutBufferSize());

        inputThroughputProbe = new ThroughputProbe(logPrefix + "-IN", 10);
        outputThroughputProbe = new ThroughputProbe(logPrefix + " -OUT", 10);

        inputThroughputProbe.startSampling();
        outputThroughputProbe.startSampling();

        try {
            thisHostIp = Utils.findAddress("localhost");
            listeningPort = findPort(thisHostIp);
            TCPEventServerConfig configs = new TCPEventServerConfig(thisHostIp, listeningPort);
            tcpEventServer = new TCPEventServer(configs, this, null);
            for (StreamDefinition siddhiStreamDefinition : incomingStreamDefinitions) {
                tcpEventServer.addStreamDefinition(siddhiStreamDefinition);
            }
            tcpEventServer.start();
            log.info(logPrefix + "EventReceiverSpout starting to listen for events on port " + listeningPort);
            Thread thread = new Thread(new Registrar());
            thread.start();
        } catch (Throwable e) {
            log.error(logPrefix + "Error starting event listener for spout: " + e.getMessage(), e);
        }
    }

    @Override
    public void nextTuple() {
        Event event = storedEvents.poll();
        if (event != null) {
            final String siddhiStreamName = event.getStreamId();
            if (incomingStreamIDs.contains(siddhiStreamName)) {
                Object[] eventData = Arrays.copyOf(event.getData(), event.getData().length + 1);
                eventData[event.getData().length] = event.getTimestamp();
                spoutOutputCollector.emit(siddhiStreamName, Arrays.asList(eventData));

                if (log.isDebugEnabled()) {
                    log.debug(logPrefix + "Emitted Event: " + siddhiStreamName + ":" + Arrays.deepToString(eventData) + "@" + event.getTimestamp());
                }
                outputThroughputProbe.update();
            } else {
                log.warn(logPrefix + "Event received for unknown stream : " + siddhiStreamName);
            }
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


    @Override
    public void receive(String streamId, long timestamp, Object[] eventData) {
        if (log.isDebugEnabled()) {
            log.debug(logPrefix + "Received Event: " + streamId + ":" + Arrays.deepToString(eventData) + "@" + timestamp);
        }
        try {
            storedEvents.put(new Event(timestamp, eventData, streamId));
            inputThroughputProbe.update();
        } catch (InterruptedException e) {
            //ignore
        }
    }


    class Registrar implements Runnable {
        private String managerHost;
        private int managerPort;

        @Override
        public void run() {
            log.info(logPrefix + "Registering Event Receiver Spout for " + thisHostIp + ":" + listeningPort);

            // Infinitely call register. Each register call will act as a heartbeat
            while (true) {
                if (registerStormReceiverWithStormMangerService()) {
                    while (true) {
                        TTransport transport = null;
                        try {
                            transport = new TSocket(managerHost, managerPort);
                            TProtocol protocol = new TBinaryProtocol(transport);
                            transport.open();

                            StormManagerService.Client client = new StormManagerService.Client(protocol);
                            client.registerStormReceiver(tenantId, executionPlanName, thisHostIp, listeningPort);
                            if (log.isDebugEnabled()) {
                                log.debug(logPrefix + "Successfully registered Event Receiver Spout for " +
                                        thisHostIp + ":" + listeningPort);
                            }
                            try {
                                Thread.sleep(heartbeatInterval);
                            } catch (InterruptedException e1) {
                                continue;
                            }
                        } catch (Exception e) {
                            log.error(logPrefix + "Error in registering Event Receiver Spout for " + thisHostIp + ":" +
                                    listeningPort + " with manager " + managerHost + ":" + managerPort + ". Trying next " +
                                    "manager after " + heartbeatInterval + "ms", e);
                            break;
                        } finally {
                            if (transport != null) {
                                transport.close();
                            }
                        }
                    }
                } else {
                    log.error(logPrefix + "Error registering Event Receiver Spout with given set of manager nodes. " +
                            "Retrying after " + heartbeatInterval + "ms");
                }
                try {
                    Thread.sleep(heartbeatInterval);
                } catch (InterruptedException e1) {
                    continue;
                }
            }
        }

        private boolean registerStormReceiverWithStormMangerService() {
            TTransport transport = null;
            for (HostAndPort endpoint : stormDeploymentConfig.getManagers()) {
                try {
                    transport = new TSocket(endpoint.getHostName(), endpoint.getPort());
                    TProtocol protocol = new TBinaryProtocol(transport);
                    transport.open();

                    StormManagerService.Client client = new StormManagerService.Client(protocol);
                    client.registerStormReceiver(tenantId, executionPlanName, thisHostIp, listeningPort);

                    log.info(logPrefix + "Successfully registered Event Receiver Spout for " + thisHostIp + ":" + listeningPort
                            + " with manager service at " + endpoint.getHostName() + ":" + endpoint.getPort());

                    managerHost = endpoint.getHostName();
                    managerPort = endpoint.getPort();
                    return true;
                } catch (Exception e) {
                    log.error(logPrefix + "Error in registering Event Receiver Spout for " + thisHostIp + ":" +
                            listeningPort + " with manager " + endpoint.getHostName() + ":" + endpoint.getPort() +
                            ", Trying next manager.", e);
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
