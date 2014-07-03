package org.wso2.carbon.event.processor.storm.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import org.wso2.carbon.event.processor.storm.common.helper.StormDeploymentConfigurations;
import org.wso2.carbon.event.processor.storm.topology.component.EventPublisherBolt;
import org.wso2.carbon.event.processor.storm.topology.component.EventReceiverSpout;
import org.wso2.carbon.event.processor.storm.topology.component.SiddhiBolt;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * The Siddhi Topology
 */
public class SiddhiTopology {
    public static void main(String[] args) throws Exception {
        StormDeploymentConfigurations.LoadConfigurations();

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
        int maxListenerPort = StormDeploymentConfigurations.getMaxListeningPort();
        int minListenerPort = StormDeploymentConfigurations.getMinListingPort();
        String keyStorePath = StormDeploymentConfigurations.getKeyStorePath();
        String trustStroePath = StormDeploymentConfigurations.getTrustStorePath();
        String cepManagerHost = StormDeploymentConfigurations.getCepManagerHost();
        int cepManagerPort = StormDeploymentConfigurations.getCepManagerPort();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("EventReceiverSpout", new EventReceiverSpout(minListenerPort, maxListenerPort, keyStorePath, cepManagerHost, cepManagerPort, importedStreams), 1);
        builder.setBolt("Siddhibolt", new SiddhiBolt(importedStreams, new String[]{query}, new String[]{"alertStream"})).allGrouping("EventReceiverSpout", "authStream");
        builder.setBolt("EventPublisherBolt", new EventPublisherBolt(cepManagerHost, cepManagerPort, trustStroePath, importedStreams, new String[]{query}, new String[]{"alertStream"})).allGrouping("Siddhibolt", "alertStream");

        Config conf = new Config();
        conf.setDebug(true);

        conf.setMaxTaskParallelism(3);
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("TriftTopology", conf, builder.createTopology());
    }
}
