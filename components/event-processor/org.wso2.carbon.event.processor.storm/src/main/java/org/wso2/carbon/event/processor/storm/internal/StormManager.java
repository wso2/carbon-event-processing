/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.storm.internal;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.storm.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.storm.StreamConfiguration;
import org.wso2.carbon.event.processor.storm.exception.StormConfigurationException;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;
import org.wso2.carbon.event.processor.storm.internal.listener.ConsumingQueuedEventSource;
import org.wso2.carbon.event.processor.storm.internal.listener.StormTerminalBolt;
import org.wso2.carbon.event.processor.storm.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.storm.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.storm.internal.stream.EventProducer;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.ExecutionPlanReference;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StormManager {

    public static final String SIDDHI_SPOUT_PREFIX = "siddhi-spout-";
    public static final String SIDDHI_BOLT_PREFIX = "siddhi-bolt-";
    public static final String SIDDHI_TERMINAL_BOLT_PREFIX = "siddhi-terminal-bolt-";
    private static Log log = LogFactory.getLog(StormManager.class);
    private Config config;
    private List<SiddhiBolt> siddhiBoltList = new ArrayList<SiddhiBolt>();
    private List<SiddhiSpout> siddhiSpoutList = new ArrayList<SiddhiSpout>();
    private List<StormTerminalBolt> terminalBoltList = new ArrayList<StormTerminalBolt>();
    private List<ExecutionPlanReference> executionPlanReferenceList = new ArrayList<ExecutionPlanReference>();
    private SiddhiManager siddhiManager;
    private LocalCluster cluster;

    public StormManager(Config config, SiddhiManager siddhiManager) {
        this.config = config;
        this.siddhiManager = siddhiManager;
    }

    public SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public ExecutionPlanReference submitExecutionPlan(String executionPlan, ExecutionPlanConfiguration executionPlanConfiguration) throws StormConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ExecutionPlanReference executionPlanReference = siddhiManager.addExecutionPlan(executionPlan);
        executionPlanReferenceList.add(executionPlanReference);
        for (String queryReference : executionPlanReference.getQueryReferenceList()) {
            Query query = siddhiManager.getQuery(queryReference);
            StormProcessorValueHolder.getStormProcessorService().addQuery(queryReference, query, tenantId);
        }

        return executionPlanReference;
    }

    public Map<String, SiddhiBolt> instantiateStormComponents(ExecutionPlanConfiguration executionPlanConfiguration, ExecutionPlanReference executionPlanReference) throws StormConfigurationException {
        executionPlanReferenceList.add(executionPlanReference);
        Map<String, SiddhiBolt> siddhiBoltsForQuery = new ConcurrentHashMap<String, SiddhiBolt>();
        for (InputHandler inputHandler : executionPlanReference.getInputHandlerList()) {
            StreamDefinition streamDefinition = siddhiManager.getStreamDefinition(inputHandler.getStreamId());
            ConsumingQueuedEventSource eventSource = new ConsumingQueuedEventSource(streamDefinition, executionPlanConfiguration);
            SiddhiSpout siddhiSpout = new SiddhiSpout(streamDefinition, eventSource, getTopLevelStreamId(streamDefinition.getStreamId(), executionPlanConfiguration));
            siddhiSpoutList.add(siddhiSpout);
        }
        for (String queryReference : executionPlanReference.getQueryReferenceList()) {
            Query query = siddhiManager.getQuery(queryReference);
            String outputStreamId = query.getOutputStream().getStreamId();
            String topLevelOutputStreamId = getTopLevelStreamId(outputStreamId, executionPlanConfiguration);
            String inputStreamId = query.getInputStream().getStreamIds().get(0);
            SiddhiBolt siddhiBolt = new SiddhiBolt(queryReference, inputStreamId, new String[]{outputStreamId},
                    getTopLevelStreamId(inputStreamId, executionPlanConfiguration));
            siddhiBoltsForQuery.put(topLevelOutputStreamId, siddhiBolt);
        }
        siddhiBoltList.addAll(siddhiBoltsForQuery.values());
        return siddhiBoltsForQuery;
    }

    private String getTopLevelStreamId(String siddhiStreamName, ExecutionPlanConfiguration executionPlanConfiguration) {
        if (siddhiStreamName != null) {
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getImportedStreams()) {
                if (siddhiStreamName.equals(streamConfiguration.getSiddhiStreamName())) {
                    return streamConfiguration.getStreamId();
                }
            }
            for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                if (siddhiStreamName.equals(streamConfiguration.getSiddhiStreamName())) {
                    return streamConfiguration.getStreamId();
                }
            }
        }

        return null;
    }

    public void defineStream(StreamDefinition streamDefinition, ExecutionPlanConfiguration executionPlanConfiguration) {
        ConsumingQueuedEventSource eventSource = new ConsumingQueuedEventSource(streamDefinition, executionPlanConfiguration);
        siddhiManager.defineStream(streamDefinition);
        siddhiSpoutList.add(new SiddhiSpout(streamDefinition, eventSource, getTopLevelStreamId(streamDefinition.getStreamId(), executionPlanConfiguration)));
    }

    public StormTerminalBolt createTerminalBolt(String siddhiStreamId, String streamId, ExecutionPlanConfiguration executionPlanConfiguration) {
        StormTerminalBolt stormTerminalBolt = new StormTerminalBolt(siddhiStreamId, streamId, executionPlanConfiguration);
        terminalBoltList.add(stormTerminalBolt);
        return stormTerminalBolt;
    }

    private String getSpoutId(String streamId) {
        return SIDDHI_SPOUT_PREFIX + streamId;
    }

    private String getBoltId(String streamId) {
        return SIDDHI_BOLT_PREFIX + streamId;
    }

    private String getTerminalBoltId(String streamId) {
        return SIDDHI_TERMINAL_BOLT_PREFIX + streamId;
    }

    public void buildTopology() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        TopologyBuilder builder = new TopologyBuilder();
        for (SiddhiSpout siddhiSpout : siddhiSpoutList) {
            builder.setSpout(getSpoutId(siddhiSpout.getExportedSiddhiStreamId()), siddhiSpout, 2);
        }
        for (SiddhiBolt siddhiBolt : siddhiBoltList) {
            BoltDeclarer boltDeclarer = builder.setBolt(getBoltId(siddhiBolt.getInputSiddhiStreamId()), siddhiBolt, 3);
            EventJunction eventJunction = StormProcessorValueHolder.getStormProcessorService().getEventJunction(siddhiBolt.getImportedTopLevelStreamId(), tenantId);
            for (EventConsumer eventConsumer : eventJunction.getAllEventConsumers()) {
                if (eventConsumer instanceof ConsumingQueuedEventSource) {
                    String streamId = ((ConsumingQueuedEventSource) eventConsumer).getStreamId();
                    boltDeclarer.shuffleGrouping(getSpoutId(streamId), streamId);
                }
            }
            for (EventProducer eventProducer : eventJunction.getAllEventProducers()) {
                if (eventProducer instanceof SiddhiBolt) {
                    String inStreamId = ((SiddhiBolt) eventProducer).getInputSiddhiStreamId();
                    for(String outStreamId: ((SiddhiBolt)eventProducer).getOutputSiddhiStreamIds()) {
                        boltDeclarer.shuffleGrouping(getBoltId(inStreamId), outStreamId);
                    }
                }
            }
        }
        for (StormTerminalBolt terminalBolt : terminalBoltList) {
            BoltDeclarer boltDeclarer = builder.setBolt(getTerminalBoltId(terminalBolt.getSiddhiStreamId()), terminalBolt, 3);
            EventJunction eventJunction = StormProcessorValueHolder.getStormProcessorService().getEventJunction(terminalBolt.getStreamId(), tenantId);
            for (EventConsumer eventConsumer : eventJunction.getAllEventConsumers()) {
                if (eventConsumer instanceof ConsumingQueuedEventSource) {
                    String streamId = ((ConsumingQueuedEventSource) eventConsumer).getStreamId();
                    boltDeclarer.shuffleGrouping(getSpoutId(streamId), streamId);
                }
            }
            for (EventProducer eventProducer : eventJunction.getAllEventProducers()) {
                if (eventProducer instanceof SiddhiBolt) {
                    String inStreamId = ((SiddhiBolt) eventProducer).getInputSiddhiStreamId();
                    for(String outStreamId: ((SiddhiBolt)eventProducer).getOutputSiddhiStreamIds()) {
                        boltDeclarer.shuffleGrouping(getBoltId(inStreamId), outStreamId);
                    }
                }
            }
        }


        config.setDebug(false);
        config.setMaxTaskParallelism(3);

//        config.put(Config.NIMBUS_HOST, "localhost");
//        config.put(Config.NIMBUS_THRIFT_PORT, 6627);
//        config.put(Config.STORM_THRIFT_TRANSPORT_PLUGIN, "backtype.storm.security.auth.SimpleTransportPlugin");

//        try {
//            NimbusClient nimbus = new NimbusClient(config, "localhost", 6627);
//            String jarLocation = StormSubmitter.submitJar(config, "repository/components/dropins/org.wso2.carbon.event.processor.storm_1.0.0.jar");
        System.setProperty("storm.jar", "/home/lasantha/WSO2-Staging/CEP-3.0.0-PostWork/wso2cep-3.0.0/repository/components/dropins/org.wso2.carbon.event.processor.storm_1.0.0.jar");
        cluster = new LocalCluster();
        cluster.submitTopology("siddhi-default", config, builder.createTopology());
        Utils.sleep(10000);
//        } catch (TTransportException e) {
//            log.error("Thrift transport exception: " + e.getMessage(), e);
//        }
    }

    public void shutdownCluster() {
        cluster.shutdown();
    }
}
