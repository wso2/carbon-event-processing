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
package org.wso2.carbon.event.processor.storm.topology.component;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.storm.common.event.server.EventServerConfig;
import org.wso2.carbon.event.processor.storm.common.event.server.StreamCallback;
import org.wso2.carbon.event.processor.storm.common.management.client.ManagerServiceClient;
import org.wso2.carbon.event.processor.storm.common.util.StormUtils;
import org.wso2.carbon.event.processor.storm.topology.util.SiddhiUtils;
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

    /**
     * Siddhi stream definitions of all incoming streams. Required to declare output fields
     */
    private String[] incomingStreamDefinitions;
    private BinaryTransportEventServer binaryTransportEventServer;

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
    private int cepMangerPort;
    private String cepMangerHost;
    private String logPrefix;
    private String keyStorePath;
    private String keyStorePassword;
    private int minListeningPort;
    private int maxListeningPort;

    /**
     * Receives events from the CEP Receiver through Thrift using data bridge and pass through the events
     * to a downstream component as tupels.
     *
     * @param minListeningPort          - the lower bound of the range of listening ports of the Thrift server
     * @param maxListeningPort          - the upper bound for the range of listening ports allocated for this Thrift server
     * @param incomingStreamDefinitions - Incoming Siddhi stream definitions
     */
    public EventReceiverSpout(int minListeningPort, int maxListeningPort, String keyStorePath, String cepManagerHost, int cepManagerPort, String[] incomingStreamDefinitions, String executionPlanName) {
        this.minListeningPort = minListeningPort;
        this.maxListeningPort = maxListeningPort;
        this.cepMangerHost = cepManagerHost;
        this.cepMangerPort = cepManagerPort;
        this.incomingStreamDefinitions = incomingStreamDefinitions;
        this.executionPlanName = executionPlanName;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = "wso2carbon";
        this.logPrefix = "{" + executionPlanName + ":" + tenantId + "} - ";

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // Declaring all incoming streams as output streams because this spouts role is to pass through all the incoming events as tuples.
        List<StreamDefinition> streamDefinitions = SiddhiUtils.toSiddhiStreamDefinitions(incomingStreamDefinitions);
        for (StreamDefinition siddhiStreamDefinition : streamDefinitions) {
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
        System.setProperty("Security.KeyStore.Location", keyStorePath);
        System.setProperty("Security.KeyStore.Password", keyStorePassword);

        try {
            selectPort();
            this.thisHostIp = HostAddressFinder.findAddress("localhost");
            binaryTransportEventServer = new BinaryTransportEventServer(new EventServerConfig(listeningPort), this);
            List<StreamDefinition> siddhiStreamDefinitions = SiddhiUtils.toSiddhiStreamDefinitions(incomingStreamDefinitions);
            for(StreamDefinition siddhiStreamDefinition : siddhiStreamDefinitions) {
                binaryTransportEventServer.subscribe(siddhiStreamDefinition);
            }
            binaryTransportEventServer.start();
            log.info(logPrefix + "EventReceiverSpout starting to listen for events on port " + listeningPort);
            registerWithCepMangerService();
        } catch (Exception e) {
            log.error(logPrefix + "Error starting event listener for spout");
        }

    }

    private void registerWithCepMangerService() {
        log.info("Registering Storm receiver for " + executionPlanName + ":" + tenantId + " at " + thisHostIp + ":" + listeningPort);
        ManagerServiceClient client = new ManagerServiceClient(cepMangerHost, cepMangerPort, null);
        client.registerStormReceiver(executionPlanName, tenantId, thisHostIp, listeningPort, 20);
    }

    @Override
    public void nextTuple() {
        Event event = storedEvents.poll();
        if (event != null) {
            final String siddhiStreamName = SiddhiUtils.getSiddhiStreamName(event.getStreamId());

            if (incomingStreamIDs.contains(siddhiStreamName)) {
                if (log.isDebugEnabled()) {
                    log.debug(logPrefix + "Sending event : " + siddhiStreamName + "=>" + event.toString());
                }
                spoutOutputCollector.emit(siddhiStreamName, Arrays.asList(event.getPayloadData()));
            } else {
                log.warn(logPrefix + "Event received for unknown stream : " + siddhiStreamName);
            }
        }
    }

    private void selectPort() {
        for (int i = minListeningPort; i <= maxListeningPort; i++) {
            if (!StormUtils.isPortUsed(i)) {
                listeningPort = i;
                break;
            }
        }
    }

    @Override
    public void receive(String streamId, Object[] event) {
        storedEvents.add(new Event(streamId, System.currentTimeMillis(), null, null, event));
    }
}
