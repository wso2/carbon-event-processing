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
package org.wso2.carbon.event.processor.storm.common.component;

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
import org.wso2.carbon.event.processor.storm.common.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.storm.common.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.storm.common.transport.server.StreamCallback;
import org.wso2.carbon.event.processor.storm.common.transport.server.TCPEventServer;
import org.wso2.carbon.event.processor.storm.common.transport.server.TCPEventServerConfig;
import org.wso2.carbon.event.processor.storm.common.util.Utils;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Receive events from CEP receivers through data bridge thrift receiver and pass through
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

    /**
     * Receives events from the CEP Receiver through Thrift using data bridge and pass through the events
     * to a downstream component as tupels.
     *
     * @param stormDeploymentConfig
     * @param incomingStreamDefinitions - Incoming Siddhi stream definitions
     * @param executionPlanName
     * @param tenantId
     */
    public EventReceiverSpout(StormDeploymentConfig stormDeploymentConfig, List<StreamDefinition> incomingStreamDefinitions, String executionPlanName, int tenantId) {
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.incomingStreamDefinitions = incomingStreamDefinitions;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;

        this.logPrefix = "{" + executionPlanName + ":" + tenantId + "} - ";

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // Declaring all incoming streams as output streams because this spouts role is to pass through all the incoming events as tuples.
        for (StreamDefinition siddhiStreamDefinition : incomingStreamDefinitions) {
            Fields fields = new Fields(siddhiStreamDefinition.getAttributeNameArray());
            outputFieldsDeclarer.declareStream(siddhiStreamDefinition.getStreamId(), fields);
            incomingStreamIDs.add(siddhiStreamDefinition.getStreamId());
            log.info(logPrefix + "Declaring output fields for stream - " + siddhiStreamDefinition.getStreamId());
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
            final String siddhiStreamName = event.getStreamId();

            if (incomingStreamIDs.contains(siddhiStreamName)) {
                if (log.isDebugEnabled()) {
                    log.debug(logPrefix + "Sending event : " + siddhiStreamName + " => " + event.toString());

                }
                if (++eventCount % 10000 == 0) {
                    double timeSpentInSecs = (System.currentTimeMillis() - batchStartTime) / 1000.0D;
                    double throughput = 10000 / timeSpentInSecs;
                    log.info("Processed 10000 events in " + timeSpentInSecs + " seconds, throughput : " + throughput + " events/sec");
                    eventCount = 0;
                    batchStartTime = System.currentTimeMillis();
                }
                spoutOutputCollector.emit(siddhiStreamName, Arrays.asList(event.getData()));
            } else {
                log.warn(logPrefix + "Event received for unknown stream : " + siddhiStreamName);
            }
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
        storedEvents.add(new InEvent(streamId, System.currentTimeMillis(), eventData));
    }


    class Registrar implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            log.info(logPrefix + "Registering Storm receiver with " + thisHostIp + ":" + listeningPort);
            if (!registerStormReceiverWithStormMangerService()) {
                log.info(logPrefix + "Retry registering Storm receiver in 30 sec");
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    //ignore
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
