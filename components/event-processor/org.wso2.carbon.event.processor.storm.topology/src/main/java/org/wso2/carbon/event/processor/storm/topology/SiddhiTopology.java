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

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import org.wso2.carbon.event.processor.storm.common.helper.StormDeploymentConfiguration;
import org.wso2.carbon.event.processor.storm.topology.component.EventPublisherBolt;
import org.wso2.carbon.event.processor.storm.topology.component.EventReceiverSpout;
import org.wso2.carbon.event.processor.storm.topology.component.SiddhiBolt;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * The Siddhi Topology
 */
public class SiddhiTopology {
    public static void main(String[] args) throws Exception {
        if(args.length >= 2) {
            String carbonHome = args[1];
            StormDeploymentConfiguration.loadConfigurations(carbonHome);
        } else {
            StormDeploymentConfiguration.loadConfigurations();
        }

        String inputStreamDef = "define stream authStream (username string, ipAddress string, browser string);";
        String query = "from every a1 = authStream " +
                "-> b1 = authStream[username == a1.username and ipAddress != a1.ipAddress] " +
                "within 10000 " +
                "select a1.username as username, a1.ipAddress as ip1, b1.ipAddress as ip2 " +
                "insert into alertStream;";
        String exeucutionPlanName = "Login_Info_Analyzer";
        String inputStream = "authStream";
        String outputStream = "alertStream";
/*
        String inputStreamDef = "define stream analyticsStats (meta_ipAdd string, meta_index long, meta_timestamp long, meta_nanoTime long,userID string, searchTerms string);";
        String query = "from analyticsStats[meta_ipAdd != '192.168.1.1']#window.time(5 min) " +
                "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                "insert into filteredStatStream;";
        String exeucutionPlanName = "PreprocessStats";
        String inputStream = "analyticsStats";
        String outputStream = "filteredStatStream";
*/

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream(inputStreamDef);
        siddhiManager.addQuery(query);

        String[] importedStreams = new String[1];
        importedStreams[0] = inputStreamDef;
        int maxListenerPort = StormDeploymentConfiguration.getMaxListeningPort();
        int minListenerPort = StormDeploymentConfiguration.getMinListingPort();
        String keyStorePath = StormDeploymentConfiguration.getKeyStorePath();
        String trustStroePath = StormDeploymentConfiguration.getTrustStorePath();
        String cepManagerHost = StormDeploymentConfiguration.getCepManagerHost();
        int cepManagerPort = StormDeploymentConfiguration.getCepManagerPort();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("EventReceiverSpout", new EventReceiverSpout(minListenerPort, maxListenerPort, keyStorePath, cepManagerHost, cepManagerPort, importedStreams, exeucutionPlanName), 2);
        builder.setBolt("Siddhibolt", new SiddhiBolt(importedStreams, new String[]{query}, new String[]{outputStream}), 3).shuffleGrouping("EventReceiverSpout", inputStream);
        builder.setBolt("EventPublisherBolt", new EventPublisherBolt(cepManagerHost, cepManagerPort, trustStroePath,
                importedStreams, new String[]{query}, new String[]{outputStream}, exeucutionPlanName), 2).shuffleGrouping("Siddhibolt", outputStream);

        Config conf = new Config();
        conf.setDebug(true);

        conf.setMaxTaskParallelism(12);
        conf.setNumWorkers(5);
        StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
/*
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("SiddhiTopology", conf, builder.createTopology());
*/
    }
}
