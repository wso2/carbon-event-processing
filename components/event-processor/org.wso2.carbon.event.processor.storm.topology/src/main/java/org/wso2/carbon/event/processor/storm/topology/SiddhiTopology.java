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
        StormDeploymentConfiguration.loadConfigurations();

        String authStreamStreamDef = "define stream authStream (username string, ipAddress string, browser string);";
        String query = "from every a1 = authStream " +
                       "-> b1 = authStream[username == a1.username and ipAddress != a1.ipAddress] " +
                       "within 10000 " +
                       "select a1.username as username, a1.ipAddress as ip1, b1.ipAddress as ip2 " +
                       "insert into alertStream;";


        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.defineStream(authStreamStreamDef);
        siddhiManager.addQuery(query);

        String[] importedStreams = new String[1];
        importedStreams[0] = authStreamStreamDef;
        int maxListenerPort = StormDeploymentConfiguration.getMaxListeningPort();
        int minListenerPort = StormDeploymentConfiguration.getMinListingPort();
        String keyStorePath = StormDeploymentConfiguration.getKeyStorePath();
        String trustStroePath = StormDeploymentConfiguration.getTrustStorePath();
        String cepManagerHost = StormDeploymentConfiguration.getCepManagerHost();
        int cepManagerPort = StormDeploymentConfiguration.getCepManagerPort();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("EventReceiverSpout", new EventReceiverSpout(minListenerPort, maxListenerPort, keyStorePath, cepManagerHost, cepManagerPort, importedStreams), 1);
        builder.setBolt("Siddhibolt", new SiddhiBolt(importedStreams, new String[]{query}, new String[]{"alertStream"})).allGrouping("EventReceiverSpout", "authStream");
        builder.setBolt("EventPublisherBolt", new EventPublisherBolt(cepManagerHost, cepManagerPort, trustStroePath, importedStreams, new String[]{query}, new String[]{"alertStream"})).allGrouping("Siddhibolt", "alertStream");

        Config conf = new Config();
        conf.setDebug(true);

        conf.setMaxTaskParallelism(3);
        conf.setNumWorkers(3);
        StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
/*
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("SiddhiTopology", conf, builder.createTopology());
*/
    }
}
