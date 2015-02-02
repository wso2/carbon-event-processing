package org.wso2.carbon.event.processor.common.storm.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;


public class StormDeploymentConfigReader {

    private static Logger log = Logger.getLogger(StormDeploymentConfigReader.class);
    private static final String CONFIG_FILE_NAME = "storm-deployment-config.xml";

    public static StormDeploymentConfig loadConfigurations(String stormConfigFilePath) {
        String path = stormConfigFilePath + File.separator + CONFIG_FILE_NAME;

        OMElement configurationElement;
        BufferedInputStream inputStream = null;
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
        String stormEnabledValue = configurations.getFirstChildWithName(new QName("stormEnabled")).getText();
        if (!stormEnabledValue.equalsIgnoreCase("true")) {
            return null;
        }

        StormDeploymentConfig stormDeploymentConfig = new StormDeploymentConfig();

        // Reading storm managers
        OMElement management = configurations.getFirstChildWithName(new QName("management"));
        OMElement managers = management.getFirstChildWithName(new QName("managers"));
        Iterator<OMElement> iterator = managers.getChildElements();
        while (iterator.hasNext()) {
            OMElement manager = iterator.next();
            String hostName = manager.getFirstChildWithName(new QName("hostName")).getText();
            int port = Integer.parseInt(manager.getFirstChildWithName(new QName("port")).getText());
            stormDeploymentConfig.addManager(hostName, port);
        }


        stormDeploymentConfig.setHeartbeatInterval(Integer.parseInt(management.getFirstChildWithName(new QName("heartbeatInterval")).getText()));
        stormDeploymentConfig.setManagementReconnectInterval(Integer.parseInt(management.getFirstChildWithName(new QName("reconnectionInterval")).getText()));

        //Reading transport
        OMElement transport = configurations.getFirstChildWithName(new QName("transport"));
        OMElement portRange = transport.getFirstChildWithName(new QName("portRange"));
        stormDeploymentConfig.setTransportMaxPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("max")).getText()));
        stormDeploymentConfig.setTransportMinPort(Integer.parseInt(portRange.getFirstChildWithName(new QName("min")).getText()));

        stormDeploymentConfig.setTransportReconnectInterval(Integer.parseInt(transport.getFirstChildWithName(new QName("reconnectionInterval")).getText()));
        stormDeploymentConfig.setTopologySubmitRetryInterval(Integer.parseInt(transport.getFirstChildWithName(new QName("topologyResubmitInterval")).getText()));

        //Reading node info
        OMElement node = configurations.getFirstChildWithName(new QName("node"));
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

        //Get Jar name
        OMElement jar = configurations.getFirstChildWithName(new QName("jar"));
        stormDeploymentConfig.setJar(jar.getText());

        return stormDeploymentConfig;
    }
}