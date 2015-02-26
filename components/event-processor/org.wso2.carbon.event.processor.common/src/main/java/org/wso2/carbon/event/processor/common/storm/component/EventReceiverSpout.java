/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.common.storm.event.Event;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.common.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.common.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.common.util.Utils;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private StormDeploymentConfig stormDeploymentConfig;
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
    private transient ConcurrentLinkedQueue<Event> storedEvents = null;

    private SpoutOutputCollector spoutOutputCollector = null;

    private String executionPlanName;
    private int tenantId = -1234;
    private String logPrefix;
    private int eventCount;
    private long batchStartTime;
    private int heartbeatInterval;

    /**
     * Receives events from the CEP Receiver through Thrift using data bridge and pass through the events
     * to a downstream component as tupels.
     *
     * @param stormDeploymentConfig
     * @param incomingStreamDefinitions - Incoming Siddhi stream definitions
     * @param executionPlanName
     * @param tenantId
     */
    public EventReceiverSpout(StormDeploymentConfig stormDeploymentConfig, List<String> incomingStreamDefinitions,
                              String executionPlanName, int tenantId, int heartbeatInterval) {
        this.incomingStreamDefinitions = new ArrayList<StreamDefinition>(incomingStreamDefinitions.size());
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.heartbeatInterval = heartbeatInterval;
        for (String definition : incomingStreamDefinitions) {
            this.incomingStreamDefinitions.add(SiddhiCompiler.parseStreamDefinition(definition));
        }
        this.logPrefix = "{" + executionPlanName + ":" + tenantId + "} - ";
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // Declaring all incoming streams as output streams because this spouts role is to pass through all the incoming events as tuples.
        for (StreamDefinition siddhiStreamDefinition : incomingStreamDefinitions) {
            Fields fields = new Fields(siddhiStreamDefinition.getAttributeNameArray());
            outputFieldsDeclarer.declareStream(siddhiStreamDefinition.getId(), fields);
            incomingStreamIDs.add(siddhiStreamDefinition.getId());
            log.info(logPrefix + "Declaring output fields for stream - " + siddhiStreamDefinition.getId());
        }
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        this.storedEvents = new ConcurrentLinkedQueue<Event>();
        this.eventCount = 0;
        this.batchStartTime = System.currentTimeMillis();

        try {
            listeningPort = findPort();
            thisHostIp = Utils.findAddress("localhost");
            tcpEventServer = new TCPEventServer(new TCPEventServerConfig(listeningPort), this);
            for (StreamDefinition siddhiStreamDefinition : incomingStreamDefinitions) {
                tcpEventServer.subscribe(siddhiStreamDefinition);
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
            final String siddhiStreamName = event.getStreamId();        //use custom event obj?

            if (incomingStreamIDs.contains(siddhiStreamName)) {
                if (log.isDebugEnabled()) {
                    log.debug(logPrefix + "Sending event : " + siddhiStreamName + " => " + event.toString());
                }
                spoutOutputCollector.emit(siddhiStreamName, Arrays.asList(event.getData()));
                updateThroughputCounter();
            } else {
                log.warn(logPrefix + "Event received for unknown stream : " + siddhiStreamName);
            }
        }
    }

    private void updateThroughputCounter() {
        if (++eventCount % 10000 == 0) {
            double timeSpentInSecs = (System.currentTimeMillis() - batchStartTime) / 1000.0D;
            double throughput = 10000 / timeSpentInSecs;
            log.info("Processed 10000 events in " + timeSpentInSecs + " seconds, throughput : " + throughput + " events/sec");
            eventCount = 0;
            batchStartTime = System.currentTimeMillis();
        }
    }

    private int findPort() throws Exception {
        for (int i = stormDeploymentConfig.getTransportMinPort(); i <= stormDeploymentConfig.getTransportMaxPort(); i++) {
            if (!Utils.isPortUsed(i)) {
                return i;
            }
        }
        throw new Exception("Cannot find free port in range " + stormDeploymentConfig.getTransportMinPort() + "~" + stormDeploymentConfig.getTransportMaxPort());
    }


    @Override
    public void receive(String streamId, Object[] eventData) {
        if (log.isDebugEnabled()) {
            log.debug(logPrefix + "Received event for stream '" + streamId + "': " + Arrays.deepToString(eventData));
        }
        storedEvents.add(new Event(System.currentTimeMillis(), eventData, streamId));
    }


    class Registrar implements Runnable {

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug(logPrefix + "Registering Storm receiver with " + thisHostIp + ":" + listeningPort);
            }

            // Infinitely call register. Each register call will act as a heartbeat
            while (true) {
                registerStormReceiverWithStormMangerService();
                try {
                    Thread.sleep(heartbeatInterval);
                } catch (InterruptedException e1) {
                    continue;
                }
            }
        }

        private boolean registerStormReceiverWithStormMangerService() {
            TTransport transport = null;
            try {
                transport = new TSocket(stormDeploymentConfig.getManagers().get(0).getHostName(), stormDeploymentConfig.getManagers().get(0).getPort());
                TProtocol protocol = new TBinaryProtocol(transport);
                transport.open();

                StormManagerService.Client client = new StormManagerService.Client(protocol);
                client.registerStormReceiver(tenantId, executionPlanName, thisHostIp, listeningPort);
                log.info(logPrefix + "Successfully registering Storm receiver with " + thisHostIp + ":" + listeningPort);
                return true;
            } catch (Exception e) {
                log.error(logPrefix + "Error in registering Storm receiver", e);
                return false;
            } finally {
                if (transport != null) {
                    transport.close();
                }
            }
        }
    }
}
