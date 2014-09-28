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
package org.wso2.carbon.event.processor.storm.topology;

import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.Nimbus;
import backtype.storm.generated.TopologySummary;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;
import org.json.simple.JSONValue;
import org.wso2.carbon.event.processor.storm.common.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.storm.common.util.StormConfigReader;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.List;
import java.util.Map;

/**
 * The Siddhi Topology
 */
public class SiddhiTopologySubmitter {


    public static void main(String[] args) throws Exception {

        System.setProperty("storm.yaml", "/Users/suho/wso2/packs/cep/storm/apache-storm-0.9.2-incubating/conf/storm.yaml");
        Map conf = Utils.readStormConfig();
        Nimbus.Client client = NimbusClient.getConfiguredClient(conf).getClient();

        List<TopologySummary> topologyList = client.getClusterInfo().get_topologies();

        System.out.println(topologyList);


//        KillOptions killOpts = new KillOptions();
//////killOpts.set_wait_secs(waitSeconds) // time to wait before killing
//        client.killTopologyWithOpts("Siddhi-Topology", killOpts); //provide topology name

                                    String jarLocation="/Users/suho/wso2/src/dev/carbon-event-processing/components/event-processor/org.wso2.carbon.event.processor.storm.topology/target/org.wso2.carbon.event.processor.storm.topology-1.0.0-SNAPSHOT-jar-with-dependencies.jar";

        String topologyName = "Siddhi-Topology";

        TopologyBuilder builder = getTopology();


        String uploadedJarLocation = StormSubmitter.submitJar(conf,
                jarLocation);
        try {
            String jsonConf = JSONValue.toJSONString(conf);
            client.submitTopology(topologyName,
                    uploadedJarLocation, jsonConf, builder.createTopology());
        } catch (AlreadyAliveException ae) {
            ae.printStackTrace();
        }

//        try{
//            StormSubmitter.submitJar(conf,
//                    jarLocation);
//            StormSubmitter.submitTopology(topologyName, conf,
//                    builder.createTopology());
//        } catch (AlreadyAliveException ae) {
//            ae.printStackTrace();
//        }

//
//        client.submitTopology(topologyName,jarLocation,);

//
//        StormConfig
//        conf.put(Config.NIMBUS_HOST, NIMBUS_NODE);
//        conf.put(Config.NIMBUS_THRIFT_PORT,6627);
//        conf.put(Config.STORM_ZOOKEEPER_PORT,2181);
//        conf.put(Config.STORM_ZOOKEEPER_SERVERS,ZOOKEEPER_ID);
//        conf.setNumWorkers(20);
//        conf.setMaxSpoutPending(5000);
//        StormSubmitter submitter = new StormSubmitter();
//        submitter.submitTopology("test", conf, builder.createTopology());
//
//        //create nimbus object
//        NimbusClient nimbus = new NimbusClient(nimbusMachine);
//
//        //upload jar to nimbus
//        StormSubmitter.submitJar(conf, inputJar);
//
//
//        //submit topology
//        nimbus.getClient().submitTopology(conf.get("topology.name").toString(),
//                uploadedJarLocation,
//                jsonConf,
//                topology);
//
//
//        //sleep runningTime then kill topology
//        Thread.sleep(runningTime * 1000);
//
//        nimbus.getClient().killTopology(conf.get("topology.name").toString());
//


    }

    private static TopologyBuilder getTopology() {
        StormDeploymentConfig stormDeploymentConfig = StormConfigReader.loadConfigurations("/Users/suho/wso2/src/dev/product-cep/modules/distribution/target/receiver/wso2cep-4.0.0-SNAPSHOT/");

        String inputStreamDef = "define stream analyticsStats (meta_ipAdd string, meta_index long, meta_timestamp long, meta_nanoTime long,userID string, searchTerms string);";
        String query = "from analyticsStats[meta_ipAdd != '192.168.1.1']#window.time(5 min) " +
                "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                "insert into filteredStatStream;";
        String exeucutionPlanName = "PreprocessStats";
        String inputStream = "analyticsStats";
        String outputStream = "filteredStatStream";

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream(inputStreamDef);
        siddhiManager.addQuery(query);

        String[] importedStreams = new String[1];
        importedStreams[0] = inputStreamDef;

        TopologyBuilder builder = new TopologyBuilder();

//        builder.setSpout("EventReceiverSpout", new EventReceiverSpout(-1234, stormDeploymentConfig, importedStreams, exeucutionPlanName), 2);
//        builder.setBolt("Siddhibolt", new SiddhiBolt(importedStreams, new String[]{query}, new String[]{outputStream}), 3).shuffleGrouping("EventReceiverSpout", inputStream);
//        builder.setBolt("EventPublisherBolt", new EventPublisherBolt(-1234, importedStreams, new String[]{query}, new String[]{outputStream}, exeucutionPlanName, stormDeploymentConfig), 3).shuffleGrouping("Siddhibolt", outputStream).setDebug(true);
             return builder;
    }
}
