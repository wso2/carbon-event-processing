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
package org.wso2.carbon.event.processor.core.internal.ha.server.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.event.processor.core.internal.ha.server.HAManagementServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * Helper class to build Agent Server Initial Configurations
 */
public final class HAManagementServerBuilder {

    private static final Log log = LogFactory.getLog(HAManagementServerBuilder.class);

    private HAManagementServerBuilder() {
    }


    private static void populatePorts(OMElement config,
                                      int portOffset,
                                      HAManagementServerConfiguration haManagementServerConfiguration) {

        OMElement receiverPort = config.getFirstChildWithName(new QName(HAManagementServerConstants.CEP_HA_MANAGEMENT_NAME_SPACE,
                HAManagementServerConstants.PORT_ELEMENT));
        if (receiverPort != null) {
            try {
                haManagementServerConfiguration.setDataReceiverPort(Integer
                        .parseInt(receiverPort.getText()) + portOffset);
            } catch (NumberFormatException ignored) {

            }
        }
    }

    private static void populateHostName(OMElement config,
                                         HAManagementServerConfiguration haManagementServerConfiguration) {
        OMElement receiverHostName = config.getFirstChildWithName(
                new QName(HAManagementServerConstants.CEP_HA_MANAGEMENT_NAME_SPACE,
                        HAManagementServerConstants.RECEIVER_HOST_NAME));
        if (receiverHostName != null && receiverHostName.getText() != null
                && !receiverHostName.getText().trim().equals("")) {
            haManagementServerConfiguration.setReceiverHostName(receiverHostName.getText());
        }
    }


    public static int readPortOffset() {
        return CarbonUtils.
                getPortFromServerConfig(HAManagementServerConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
    }


    public static void populateConfigurations(int portOffset,
                                              HAManagementServerConfiguration haManagementServerConfiguration,
                                              OMElement initialConfig) {
        if (initialConfig != null) {
            populatePorts(initialConfig, portOffset, haManagementServerConfiguration);
            populateHostName(initialConfig, haManagementServerConfiguration);
        }
    }

    public static OMElement loadConfigXML() throws DataBridgeConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + HAManagementServerConstants.CEP_HA_MANAGEMENT_ELEMENT_CONFIG_XML;

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = HAManagementServerConstants.CEP_HA_MANAGER_ELEMENT
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + HAManagementServerConstants.CEP_HA_MANAGEMENT_ELEMENT_CONFIG_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not shutdown the input stream";
                log.error(errorMessage, e);
            }
        }
    }

}
