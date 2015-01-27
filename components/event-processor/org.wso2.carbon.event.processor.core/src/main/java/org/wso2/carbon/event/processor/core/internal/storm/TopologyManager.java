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
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormQueryPlanBuilder;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormTopologyConstructor;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
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
        System.setProperty("storm.yaml", stormConfigDirPath + File.separator + "storm.yaml");
        stormConfig = Utils.readStormConfig();
        client = NimbusClient.getConfiguredClient(stormConfig).getClient();
        jarLocation = stormConfigDirPath + File.separator + EventProcessorValueHolder.getStormDeploymentConfig().getJar();
    }

    public static List<TopologySummary> getTopologies() throws StormDeploymentException {
        try {
            return client.getClusterInfo().get_topologies();
        } catch (TException e) {
            throw new StormDeploymentException("Cannot get topologies from strom cluster", e);
        }
    }

    public static void submitTopology(ExecutionPlanConfiguration configuration, List<StreamDefinition> streamDefinitions, int tenantId) throws StormDeploymentException, ExecutionPlanConfigurationException {
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, streamDefinitions);
        String executionPlanName = configuration.getName();
        TopologyBuilder builder = null;
        try {
            String stormQueryPlan = getStringQueryPlan(document);
            builder = StormTopologyConstructor.constructTopologyBuilder(stormQueryPlan, executionPlanName, tenantId, EventProcessorValueHolder.getStormDeploymentConfig());
        } catch (XMLStreamException e) {
            throw new ExecutionPlanConfigurationException("Invalid Config for Execution Plan " + executionPlanName + " for tenant " + tenantId, e);
        } catch (TransformerException e) {
            throw new ExecutionPlanConfigurationException("Error while converting to storm query plan string. " +
                    "Execution plan: " + executionPlanName + " Tenant: " + tenantId, e);
        }

        String uploadedJarLocation = StormSubmitter.submitJar(stormConfig, jarLocation);

        try {
            String jsonConf = JSONValue.toJSONString(stormConfig);
            client.submitTopology(getTopologyName(executionPlanName, tenantId), uploadedJarLocation, jsonConf, builder.createTopology());
            Thread.sleep(10000); // Sleeping while for topology to get deployed at storm cluster
            log.info("Successfully submitted storm topology '" + getTopologyName(executionPlanName, tenantId) + "'");
        } catch (AlreadyAliveException e) {
            log.warn("Topology '" + getTopologyName(executionPlanName, tenantId) + "' already existing", e);
            Thread retryThread = new Thread(new TopologySubmitter(executionPlanName, uploadedJarLocation, builder.createTopology(), tenantId, true));
            retryThread.start();
        } catch (TException e) {
            log.warn("Error connecting to storm when trying to submit topology '" + getTopologyName(executionPlanName, tenantId) + "'", e);
            Thread retryThread = new Thread(new TopologySubmitter(executionPlanName, uploadedJarLocation, builder.createTopology(), tenantId, false));
            retryThread.start();
        } catch (InvalidTopologyException e) {
            // No point in retrying to submit if the topology is invalid. Therefore, throwing an exception without retrying.
            throw new ExecutionPlanConfigurationException("Invalid Execution Plan " + executionPlanName + " for tenant " + tenantId, e);
        } catch (InterruptedException e) {
            //Igonre
        }
    }

    public static void killTopology(String executionPlanName, int tenantId) throws StormDeploymentException{
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

    public static String getTopologyName(String executionPlanName, int tenantId){
        return (executionPlanName + "[" + tenantId + "]");
    }

    static class TopologySubmitter implements Runnable{
        String executionPlanName;
        String uploadedJarLocation;
        StormTopology topology;
        int tenantId;
        boolean isTopologyAlive;
        public TopologySubmitter(String executionPlanName, String uploadedJarLocation, StormTopology topology, int tenantId, boolean isTopologyAlive){
            this.executionPlanName = executionPlanName;
            this.uploadedJarLocation = uploadedJarLocation;
            this.topology = topology;
            this.tenantId = tenantId;
            this.isTopologyAlive = isTopologyAlive;
        }

        private boolean submitTopology(){
            String jsonConf = JSONValue.toJSONString(stormConfig);
            try {
                if (isTopologyAlive){
                    log.info("Killing already existing storm topology '" + getTopologyName(executionPlanName, tenantId) + "' to re-submit");
                    KillOptions options = new KillOptions();
                    options.set_wait_secs(10);
                    client.killTopologyWithOpts(getTopologyName(executionPlanName, tenantId), options);
                    Thread.sleep(15000); // Sleep 15s till end of 10s waiting time in storm cluster for killed topology.
                }
                client.submitTopology(getTopologyName(executionPlanName, tenantId), uploadedJarLocation, jsonConf, topology);
                Thread.sleep(10000);// Sleeping while for topology to get deployed at storm cluster.
            } catch (AlreadyAliveException e) {
                log.warn("Topology '" + getTopologyName(executionPlanName, tenantId) + "' already existing", e);
                return false;
            } catch (TException e) {
                log.warn("Error connecting to storm when trying to submit topology '" + getTopologyName(executionPlanName, tenantId) + "'", e);
                return false;
            }  catch (NotAliveException e) {
                log.info("Topology '" + getTopologyName(executionPlanName, tenantId) + "' is not alive to kill");
                isTopologyAlive = false;
                return false;
            } catch (InterruptedException e) {
                // Ignore
            } catch (InvalidTopologyException e) {
                // Do nothing. Will not reach here since this exception will occur in the first attempt to submit by parent thread.
            }
            log.info("Successfully submitted storm topology '" + getTopologyName(executionPlanName, tenantId) + "'");
            return true;
        }

        @Override
        public void run() {
            do  {
                log.info("Retrying to submit topology '" + getTopologyName(executionPlanName, tenantId) + "' in 30 sec");
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    //ignore
                }
            } while (!submitTopology());
        }
    }
}
