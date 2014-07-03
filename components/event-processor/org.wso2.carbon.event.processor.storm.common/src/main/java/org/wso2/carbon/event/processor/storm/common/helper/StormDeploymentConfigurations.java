package org.wso2.carbon.event.processor.storm.common.helper;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by sajith on 7/1/14.
 */
public class StormDeploymentConfigurations {
    private static String CONFIG_FILE_NAME = "storm-deployment-config.xml";
    private static Log log = LogFactory.getLog(StormDeploymentConfigurations.class);

    public static boolean isRunningOnStorm() {
        return isRunningOnStorm;
    }

    public static String getCepManagerHost() {
        return cepManagerHost;
    }

    public static int getCepManagerPort() {
        return cepManagerPort;
    }

    public static int getReconnectInterval() {
        return reconnectInterval;
    }

    public static int getMaxListeningPort() {
        return maxListeningPort;
    }

    public static int getMinListingPort() {
        return minListingPort;
    }

    public static String getKeyStorePath() {
        return keyStorePath;
    }

    public static String getTrustStorePath() {
        return trustStorePath;
    }


    private static boolean isRunningOnStorm = false;
    private static String cepManagerHost = "localhost";
    private static String keyStorePath =  System.getProperty("user.home") + File.separator + "wso2carbon.jks";
    private static String trustStorePath = System.getProperty("user.home") + File.separator  + "client-truststore.jks";
    private static int cepManagerPort = 9773;
    private static int reconnectInterval = 10;


    private static int maxListeningPort = 15100;
    private static int minListingPort = 15000;

    public static void LoadConfigurations(){
        String carbonHome = System.getProperty("carbon.config.dir.path");
        String path = carbonHome + File.separator + StormDeploymentConfigurations.CONFIG_FILE_NAME;

        OMElement configurationElement;
        BufferedInputStream inputStream = null;
        try {
            File configFile = new File(path);
            inputStream = new BufferedInputStream(new FileInputStream(configFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            configurationElement = builder.getDocumentElement();
            configurationElement.build();
            readConfigurationValue(configurationElement);
        } catch (XMLStreamException e) {
            log.error("Error while reading storm deployment configurations", e);
        }catch (FileNotFoundException e) {
            log.error("Failed to find " + CONFIG_FILE_NAME + ". Using default value for storm deployment", e);
        }
    }

    private static void readConfigurationValue(OMElement configurations){
        // Checking if CEP is running on storm
        String stormEnabledValue = configurations.getFirstChildWithName(new QName("stormEnabled")).getText();
        isRunningOnStorm = (stormEnabledValue.equalsIgnoreCase("true")) ? true : false;

        if (isRunningOnStorm){
            log.info("CEP is Running on Storm");
            // Reading CEP manager host details
            OMElement cepManagerConfigurations = configurations.getFirstChildWithName(new QName("cepManager"));
            cepManagerHost = cepManagerConfigurations.getFirstChildWithName(new QName("hostName")).getText();
            cepManagerPort = Integer.parseInt(cepManagerConfigurations.getFirstChildWithName(new QName("port")).getText());
            reconnectInterval = Integer.parseInt(cepManagerConfigurations.getFirstChildWithName(new QName("reconnectionInterval")).getText());

            log.info("Storm Deployment CEP Manger host configurations [Host=" + cepManagerHost   + ", Port=" + cepManagerPort + ", ReconnectInterval=" + reconnectInterval + "]");

            // Reading storm receiver/listener port range
            OMElement receiverPortRange  = configurations.getFirstChildWithName(new QName("receiverPortRange"));
            maxListeningPort = Integer.parseInt(receiverPortRange.getFirstChildWithName(new QName("max")).getText());
            minListingPort = Integer.parseInt(receiverPortRange.getFirstChildWithName(new QName("min")).getText());

            OMElement securityElement = configurations.getFirstChildWithName(new QName("security"));
            keyStorePath = securityElement.getFirstChildWithName(new QName("keyStorePath")).getText();
            trustStorePath = securityElement.getFirstChildWithName(new QName("trustStorePath")).getText();

            log.info("Storm Deployment CEP Manger Listening port Range [Max=" + maxListeningPort + ", Min=" + minListingPort + "]");

        }
    }
}
