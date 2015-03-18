package org.wso2.carbon.event.processor.common.storm.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.common.util.Utils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.util.Iterator;


public class StormDeploymentConfigReader {

    private static Logger log = Logger.getLogger(StormDeploymentConfigReader.class);
    private static final String CONFIG_FILE_NAME = "event-processing.xml";

    public static StormDeploymentConfig loadConfigurations(String stormConfigFilePath) {
        String path = stormConfigFilePath + File.separator + CONFIG_FILE_NAME;

        OMElement configurationElement;
        BufferedInputStream inputStream;
        try {
            File configFile = new File(path);
            inputStream = new BufferedInputStream(new FileInputStream(configFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            configurationElement = builder.getDocumentElement();
            configurationElement.build();
            return readConfigurationValues(configurationElement);
        } catch (XMLStreamException e) {
            log.error("Error while reading storm deployment configurations", e);
        } catch (FileNotFoundException e) {
            log.error("Failed to find " + CONFIG_FILE_NAME + ". Using default value for storm deployment", e);
        }
        return null;
    }

    private static StormDeploymentConfig readConfigurationValues(OMElement configurations) {

        // Checking if CEP is running on storm
        OMElement processingElement = configurations.getFirstChildWithName(new QName("processing"));
        String distributedEnabledValue = processingElement.getAttributeValue(new QName("mode"));
        if (distributedEnabledValue == null || !distributedEnabledValue.equalsIgnoreCase("Distributed")) {
            return null;
        }

        StormDeploymentConfig stormDeploymentConfig = new StormDeploymentConfig();

        // Reading storm managers
        OMElement management = processingElement.getFirstChildWithName(new QName("management"));
        OMElement managers = management.getFirstChildWithName(new QName("managers"));
        Iterator<OMElement> iterator = managers.getChildElements();
        if (!iterator.hasNext()) {
            try {
                String hostName = Utils.findAddress("localhost");
                int port = 8904;
                stormDeploymentConfig.addManager(hostName, port);
                log.info("No storm managers are provided. Hence automatically electing " + hostName + ":" + port + " node as " +
                        "manager");
            } catch (SocketException e) {
                log.error("Error while automatically populating storm managers. Please check the event-processing.xml" +
                        " at CARBON_HOME/repository/conf", e);
                return null;
            }
        }
        while (iterator.hasNext()) {
            OMElement manager = iterator.next();
            String hostName = manager.getFirstChildWithName(new QName("hostName")).getText();
            int port = Integer.parseInt(manager.getFirstChildWithName(new QName("port")).getText());
            stormDeploymentConfig.addManager(hostName, port);
        }

        if (management.getFirstChildWithName(new QName("heartbeatInterval")) != null) {
            stormDeploymentConfig.setHeartbeatInterval(Integer.parseInt(management.getFirstChildWithName(new QName
                    ("heartbeatInterval")).getText()));
        } else {
            log.info("No heartbeat interval provided. Hence using default heartbeat interval");
        }
        if (management.getFirstChildWithName(new QName("reconnectionInterval")) != null){
            stormDeploymentConfig.setManagementReconnectInterval(Integer.parseInt(management.getFirstChildWithName
                    (new QName("reconnectionInterval")).getText()));
        } else {
            log.info("No reconnection interval provided. Hence using default reconnection interval");
        }
        if (management.getFirstChildWithName(new QName("topologyResubmitInterval")) != null) {
            stormDeploymentConfig.setTopologySubmitRetryInterval(Integer.parseInt(management.getFirstChildWithName(new QName
                    ("topologyResubmitInterval")).getText()));
        } else {
            log.info("No topology resubmit interval provided. Hence using default topology resubmit interval");
        }

        //Reading transport
        OMElement transport = processingElement.getFirstChildWithName(new QName("transport"));
        OMElement portRange = transport.getFirstChildWithName(new QName("portRange"));
        if(portRange != null) {
            stormDeploymentConfig.setTransportMaxPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("max")).getText()));
            stormDeploymentConfig.setTransportMinPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("min")).getText()));
        } else {
            log.info("No port information provided. Hence using default port settings");
        }
        if(transport.getFirstChildWithName(new QName("reconnectionInterval"))!=null) {
            stormDeploymentConfig.setTransportReconnectInterval(Integer.parseInt(transport.getFirstChildWithName(new QName("reconnectionInterval")).getText()));
        }else{
            log.info("No transport reconnection interval provided. Hence using default topology resubmit interval");
        }


        //Reading node info
        OMElement node = processingElement.getFirstChildWithName(new QName("nodeType"));
        if (node != null) {
            OMElement receiver = node.getFirstChildWithName(new QName("receiver"));
            if ("true".equalsIgnoreCase(receiver.getAttributeValue(new QName("enable")))) {
                stormDeploymentConfig.setReceiverNode(true);
            }

            OMElement publisher = node.getFirstChildWithName(new QName("publisher"));
            if ("true".equalsIgnoreCase(publisher.getAttributeValue(new QName("enable")))) {
                stormDeploymentConfig.setPublisherNode(true);
            }

            OMElement manager = node.getFirstChildWithName(new QName("manager"));
            if ("true".equalsIgnoreCase(manager.getAttributeValue(new QName("enable")))) {
                stormDeploymentConfig.setManagerNode(true);
                String hostName = manager.getFirstChildWithName(new QName("hostName")).getText();
                int port = Integer.parseInt(manager.getFirstChildWithName(new QName("port")).getText());
                stormDeploymentConfig.setLocalManagerConfig(hostName, port);
            }
        } else {
            log.info("No node type configurations provided. Hence using default node type configurations");
        }

        OMElement defaultParallelism = processingElement.getFirstChildWithName(new QName("defaultParallelism"));
        if(defaultParallelism != null){
            int receiver = Integer.parseInt(defaultParallelism.getFirstChildWithName(new QName("receiver")).getText());
            int publisher = Integer.parseInt(defaultParallelism.getFirstChildWithName(new QName("publisher")).getText());
            stormDeploymentConfig.setReceiverSpoutParallelism(receiver);
            stormDeploymentConfig.setPublisherBoltParallelism(publisher);
        } else {
            log.info("No parallelism configurations provided. Hence using default parallelism configurations. Event " +
                    "Receiver Spout = 1. Event Publisher Bolt = 1.");
        }

        OMElement distributedUI = processingElement.getFirstChildWithName(new QName("distributedUIUrl"));
        if(distributedUI != null){
            String url = distributedUI.getText();
            stormDeploymentConfig.setDistributedUIUrl(url);
        }

        //Get Jar name
        OMElement jar = processingElement.getFirstChildWithName(new QName("jar"));
        stormDeploymentConfig.setJar(jar.getText());

        return stormDeploymentConfig;
    }
}