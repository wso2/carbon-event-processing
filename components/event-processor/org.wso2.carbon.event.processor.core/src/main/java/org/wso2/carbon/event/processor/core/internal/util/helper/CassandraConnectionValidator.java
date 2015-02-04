/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.core.internal.util.helper;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

public class CassandraConnectionValidator {

    public static final String HECTOR_CONFIGURATION_TAG = "<HectorConfiguration/>";
    public static final String CLUSTER_TAG = "Cluster";
    public static final String NODES_TAG = "Nodes";
    public static final String HOST_DELIMITER = ",";
    private static final String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";
    private static final int CARBON_DEFAULT_PORT_OFFSET = 0;
    private static final int CASSANDRA_RPC_PORT = 9160;
    private static final String LOCAL_HOST_NAME = "localhost";
    private static final String HECTOR_CONFIG = File.separator + "repository" + File.separator + "conf"
            + File.separator + "etc" + File.separator + "hector-config.xml";
    private static CassandraConnectionValidator cassandraConnectionValidator;
    private static Log log = LogFactory.getLog(CassandraConnectionValidator.class);
    private final List<String> nodes = new ArrayList<String>();
    private String nodesString;

    public CassandraConnectionValidator() {
//        setClusterNodes(loadConfigXML());
    }

    public static CassandraConnectionValidator getInstance() {
        if (cassandraConnectionValidator == null) {
            cassandraConnectionValidator = new CassandraConnectionValidator();
        }

        return cassandraConnectionValidator;
    }

    public int readPortOffset() {
        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
        String portOffset = carbonConfig.getFirstProperty(CARBON_CONFIG_PORT_OFFSET);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    public boolean checkCassandraConnection(String userName, String password) {
        String cassandraHosts = nodesString;
        if (cassandraHosts == null || cassandraHosts.isEmpty()) {
            String connectionPort = CASSANDRA_RPC_PORT + readPortOffset() + "";
            cassandraHosts = LOCAL_HOST_NAME + ":" + connectionPort;
        }

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", userName);
        credentials.put("password", password);


        CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(cassandraHosts);
        hostConfigurator.setRetryDownedHosts(false);
        // this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
        Cluster cluster = new ThriftCluster("test-cluster", hostConfigurator, credentials);
        Set knownPools = cluster.getKnownPoolHosts(true);
        return knownPools != null && knownPools.size() > 0;
    }

//    private OMElement loadConfigXML() {
//
//        String carbonHome = CarbonUtils.getCarbonHome();
//        String path = carbonHome + HECTOR_CONFIG;
//        BufferedInputStream inputStream = null;
//        try {
//            File file = new File(path);
//            if (!file.exists()) {
//                log.info("There is no " + HECTOR_CONFIG + ". Using the default configuration");
//                inputStream = new BufferedInputStream(
//                        new ByteArrayInputStream(HECTOR_CONFIGURATION_TAG.getBytes()));
//            } else {
//                inputStream = new BufferedInputStream(new FileInputStream(file));
//            }
//            XMLStreamReader parser = XMLInputFactory.newInstance().
//                    createXMLStreamReader(inputStream);
//            StAXOMBuilder builder = new StAXOMBuilder(parser);
//            return builder.getDocumentElement();
//        } catch (FileNotFoundException e) {
//            throw new DataAccessComponentException(HECTOR_CONFIG + "cannot be found in the path : " + path, e, log);
//        } catch (XMLStreamException e) {
//            throw new DataAccessComponentException("Invalid XML for " + HECTOR_CONFIG + " located in " +
//                    "the path : " + path, e, log);
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (IOException ignored) {
//                //ignored
//            }
//        }
//    }

    private void setClusterNodes(OMElement severElement) {
        OMElement cluster = severElement.getFirstChildWithName(new QName(CLUSTER_TAG));
        if (cluster != null) {
            OMElement nodesElement = cluster.getFirstChildWithName(new QName(NODES_TAG));
            if (nodesElement != null) {
                this.nodesString = nodesElement.getText();
                if (nodesString != null && !"".endsWith(nodesString.trim())) {
                    nodesString = (nodesString.trim());
                    String nodes[] = nodesString.split(HOST_DELIMITER);
                    Collections.addAll(this.nodes, nodes);
                }
            }
        }

    }

}
