/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.core.internal.storm;

import backtype.storm.StormSubmitter;
import backtype.storm.generated.*;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.NimbusClient;
import backtype.storm.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift7.TException;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.StormDeploymentException;
import org.wso2.carbon.event.processor.core.exception.StormQueryConstructionException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormQueryPlanBuilder;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormTopologyConstructor;
import org.wso2.carbon.utils.CarbonUtils;
import org.yaml.snakeyaml.Yaml;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * The Siddhi Topology Manager
 */
public class TopologyManager {
    private static Map stormConfig;
    private static Nimbus.Client client;
    private static String jarLocation;
    private static final Log log = LogFactory.getLog(TopologyManager.class);

    static {
        String stormConfigDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "cep" + File.separator + "storm";
        try {
            InputStream stormConf = new FileInputStream(new File(stormConfigDirPath + File.separator + "storm.yaml"));
            Yaml yaml = new Yaml();
            Map data = (Map) yaml.load(stormConf);
            if (data != null) {                          //Can be null for a commented out config
                stormConfig = Utils.readDefaultConfig();
                stormConfig.putAll(data);
            } else {
                stormConfig = Utils.readStormConfig();
            }
        } catch (FileNotFoundException e) {
            log.warn("Error occurred while reading storm configurations using default configurations", e);
        }

        client = NimbusClient.getConfiguredClient(stormConfig).getClient();
        jarLocation = stormConfigDirPath + File.separator + EventProcessorValueHolder.getStormDeploymentConfig().getJar();
    }

    public static List<TopologySummary> getTopologies() throws StormDeploymentException {
        try {
            return client.getClusterInfo().get_topologies();
        } catch (TException e) {
            throw new StormDeploymentException("Cannot get topologies from storm cluster", e);
        }
    }

    private static void waitForTopologyToBeRemoved(String topologyName) throws TException {
        log.info("Waiting topology '" + topologyName + "' to be removed from Storm cluster");
        boolean isExisting = false;
        try {
            while (true) {
                List<TopologySummary> topologies = client.getClusterInfo().get_topologies();
                for (TopologySummary topologySummary : topologies) {
                    if (topologySummary.get_name().equals(topologyName)) {
                        isExisting = true;
                        Thread.sleep(5000);
                    }
                }

                if (!isExisting || topologies.isEmpty()) {
                    log.info("Topology '" + topologyName + "' removed from Storm cluster");
                    return;
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
        }
    }

    private static void waitForTopologyToBeActive(String topologyName) throws TException {
        log.info("Waiting topology '" + topologyName + "' to be " + "ACTIVE");
        try {
            while (true) {
                List<TopologySummary> topologies = client.getClusterInfo().get_topologies();
                for (TopologySummary topologySummary : topologies) {
                    if (topologySummary.get_name().equals(topologyName)) {
                        if (topologySummary.get_status().equals("ACTIVE")) {
                            Thread.sleep(5000);
                            log.info("Topology '" + topologyName + "' is ACTIVE");
                            return;
                        }
                        Thread.sleep(2000);
                    }
                }
            }
         } catch (InterruptedException e) {}
    }

    public static void submitTopology(ExecutionPlanConfiguration configuration, List<String> importStreams,
                                      List<String> exportStreams, int tenantId, int resubmitRetryInterval) throws
            StormDeploymentException, ExecutionPlanConfigurationException {
        String executionPlanName = configuration.getName();
        TopologyBuilder builder;
        try {
            Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importStreams, exportStreams);
            String stormQueryPlan = getStringQueryPlan(document);
            if (log.isDebugEnabled()) {
                log.debug("Following is the generated Storm query plan for execution plan: " + configuration.getName() +
                        "\n" + stormQueryPlan);
            }
            builder = StormTopologyConstructor.constructTopologyBuilder(stormQueryPlan, executionPlanName, tenantId, EventProcessorValueHolder.getStormDeploymentConfig());
        } catch (XMLStreamException e) {
            throw new StormDeploymentException("Invalid Config for Execution Plan " + executionPlanName + " for tenant " + tenantId, e);
        } catch (TransformerException e) {
            throw new StormDeploymentException("Error while converting to storm query plan string. " +
                    "Execution plan: " + executionPlanName + " Tenant: " + tenantId, e);
        } catch (StormQueryConstructionException e) {
            throw new StormDeploymentException("Error while converting to XML storm query plan. " +
                    "Execution plan: " + executionPlanName + " Tenant: " + tenantId + ". " + e.getMessage(), e);
        }

        String uploadedJarLocation = StormSubmitter.submitJar(stormConfig, jarLocation);

        try {
            String jsonConf = JSONValue.toJSONString(stormConfig);
            client.submitTopology(getTopologyName(executionPlanName, tenantId), uploadedJarLocation, jsonConf, builder.createTopology());
            log.info("Successfully submitted storm topology '" + getTopologyName(executionPlanName, tenantId) + "'");

            waitForTopologyToBeActive(getTopologyName(executionPlanName, tenantId));
        } catch (AlreadyAliveException e) {
            log.warn("Topology '" + getTopologyName(executionPlanName, tenantId) + "' already existing", e);
            Thread retryThread = new Thread(new TopologySubmitter(executionPlanName, uploadedJarLocation, builder.createTopology(), tenantId, true, resubmitRetryInterval));
            retryThread.start();
        } catch (TException e) {
            log.warn("Error connecting to storm when trying to submit topology '" + getTopologyName(executionPlanName, tenantId) + "'", e);
            Thread retryThread = new Thread(new TopologySubmitter(executionPlanName, uploadedJarLocation, builder.createTopology(), tenantId, false, resubmitRetryInterval));
            retryThread.start();
        } catch (InvalidTopologyException e) {
            // No point in retrying to submit if the topology is invalid. Therefore, throwing an exception without retrying.
            throw new ExecutionPlanConfigurationException("Invalid Execution Plan " + executionPlanName + " for tenant " + tenantId, e);
        }
    }

    public static void killTopology(String executionPlanName, int tenantId) throws StormDeploymentException {
        try {
            log.info("Killing storm topology '" + executionPlanName + "'");
            client.killTopologyWithOpts(getTopologyName(executionPlanName, tenantId), new KillOptions()); //provide topology name
        } catch (NotAliveException e) {
            // do nothing
        } catch (TException e) {
            throw new StormDeploymentException("Error connecting to Storm", e);
        }
    }

    private static String getStringQueryPlan(Document document) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        String xmlString = sw.toString();
        return xmlString;
    }

    public static String getTopologyName(String executionPlanName, int tenantId) {
        return (executionPlanName + "[" + tenantId + "]");
    }

    static class TopologySubmitter implements Runnable {
        String executionPlanName;
        String uploadedJarLocation;
        StormTopology topology;
        int tenantId;
        boolean isTopologyAlive;
        int retryInterval;

        public TopologySubmitter(String executionPlanName, String uploadedJarLocation, StormTopology topology,
                                 int tenantId, boolean isTopologyAlive, int resubmitRetryInterval) {
            this.executionPlanName = executionPlanName;
            this.uploadedJarLocation = uploadedJarLocation;
            this.topology = topology;
            this.tenantId = tenantId;
            this.isTopologyAlive = isTopologyAlive;
            this.retryInterval = resubmitRetryInterval;
        }

        private boolean submitTopology() {
            String jsonConf = JSONValue.toJSONString(stormConfig);
            try {
                if (isTopologyAlive) {
                    log.info("Killing already existing storm topology '" + getTopologyName(executionPlanName, tenantId) + "' to re-submit");
                    KillOptions options = new KillOptions();
                    options.set_wait_secs(10);
                    client.killTopologyWithOpts(getTopologyName(executionPlanName, tenantId), options);
                    waitForTopologyToBeRemoved(getTopologyName(executionPlanName, tenantId));
                }

                client.submitTopology(getTopologyName(executionPlanName, tenantId), uploadedJarLocation, jsonConf, topology);
                log.info("Successfully submitted storm topology '" + getTopologyName(executionPlanName, tenantId) + "'");

                waitForTopologyToBeActive(getTopologyName(executionPlanName, tenantId));
            } catch (AlreadyAliveException e) {
                log.warn("Topology '" + getTopologyName(executionPlanName, tenantId) + "' already existing. Trying to kill and re-submit", e);
                return false;
            } catch (TException e) {
                log.error("Error connecting to storm when trying to submit topology '" + getTopologyName(executionPlanName, tenantId) + "'", e);
                return false;
            } catch (NotAliveException e) {
                log.info("Topology '" + getTopologyName(executionPlanName, tenantId) + "' is not alive to kill");
                isTopologyAlive = false;
                return false;
            } catch (InvalidTopologyException e) {
                // Do nothing. Will not reach here since this exception will occur in the first attempt to submit by parent thread.
            }

            return true;
        }

        @Override
        public void run() {
            //TODO : Handle execution plan undeploy. Stop retrying.
            do {
                log.info("Retrying to submit topology '" + getTopologyName(executionPlanName, tenantId) + "' in " + retryInterval + "ms");
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e1) {
                    //ignore
                }
            } while (!submitTopology());
        }
    }
}
