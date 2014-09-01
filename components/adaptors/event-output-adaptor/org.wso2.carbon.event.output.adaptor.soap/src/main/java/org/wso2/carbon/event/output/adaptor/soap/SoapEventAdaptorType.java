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
package org.wso2.carbon.event.output.adaptor.soap;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.soap.internal.util.SoapEventAdaptorConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.*;

public final class SoapEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(SoapEventAdaptorType.class);
    private static SoapEventAdaptorType soapEventAdaptor = new SoapEventAdaptorType();
    private ResourceBundle resourceBundle;
    ExecutorService executorService = new ThreadPoolExecutor(SoapEventAdaptorConstants.ADAPTER_MIN_THREAD_POOL_SIZE,
                                                             SoapEventAdaptorConstants.ADAPTER_MAX_THREAD_POOL_SIZE, SoapEventAdaptorConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS,

                                                             new LinkedBlockingQueue<Runnable>(SoapEventAdaptorConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE));


    private SoapEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);

        return supportOutputMessageTypes;
    }

    /**
     * @return WS Event adaptor instance
     */
    public static SoapEventAdaptorType getInstance() {
        return soapEventAdaptor;
    }

    /**
     * @return name of the WS Event adaptor
     */
    @Override
    protected String getName() {
        return SoapEventAdaptorConstants.ADAPTOR_TYPE_SOAP;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {

        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.soap.i18n.Resources", Locale.getDefault());
    }


    @Override
    public List<Property> getOutputAdaptorProperties() {
        return null;
    }

    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // Url
        Property host = new Property(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_URL);
        host.setDisplayName(
                resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_URL));
        host.setRequired(true);
        host.setHint(resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_URL_HINT));
        propertyList.add(host);

        // Username
        Property userNameProperty = new Property(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_USERNAME));
        propertyList.add(userNameProperty);


        // Password
        Property passwordProperty = new Property(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_PASSWORD));
        propertyList.add(passwordProperty);

        // header name
        Property headerProperty = new Property(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_HEADERS);
        headerProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_HEADERS));
        headerProperty.setRequired(false);
        propertyList.add(headerProperty);

        return propertyList;
    }

    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        String url = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_URL);
        String userName = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_USERNAME);
        String password = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_PASSWORD);
        Map<String, String> headers = this.extractHeaders(outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_CONF_SOAP_HEADERS));

        this.executorService.submit(new SoapSender(url, message, userName, password, headers));
    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        String testMessage = " <eventAdaptorConfigurationTest>\n" +
                             "   <message>This is a test message.</message>\n" +
                             "   </eventAdaptorConfigurationTest>";
        try {
            XMLStreamReader reader1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(testMessage.getBytes()));
            StAXOMBuilder builder1 = new StAXOMBuilder(reader1);
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
            Map<String, String> propertyList = new ConcurrentHashMap<String, String>();
            outputEventAdaptorMessageConfiguration.setOutputMessageProperties(propertyList);
            publish(outputEventAdaptorMessageConfiguration, builder1.getDocumentElement(), outputEventAdaptorConfiguration, tenantId);

        } catch (XMLStreamException e) {
            throw new OutputEventAdaptorEventProcessingException(e.getMessage());
        } catch (OutputEventAdaptorEventProcessingException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }

    }

    @Override
    public void removeConnectionInfo(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        //not-required
    }

    private Map<String, String> extractHeaders(String headers) {
        if (headers == null || headers.trim().length() == 0) {
            return null;
        }
        try {
            String[] entries = headers.split(",");
            String[] keyValue;
            Map<String, String> result = new HashMap<String, String>();
            for (String entry : entries) {
                keyValue = entry.split(":");
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
            return result;
        } catch (Exception e) {
            log.error("Invalid headers format: \"" + headers + "\", ignoring headers...");
            return null;
        }
    }

    public class SoapSender implements Runnable {

        private String url;
        private Object payload;
        private String username;
        private String password;
        private Map<String, String> headers;

        public SoapSender(String url, Object payload, String username, String password,
                          Map<String, String> headers) {
            this.url = url;
            this.payload = payload;
            this.username = username;
            this.password = password;
            this.headers = headers;
        }

        @Override
        public void run() {
            ConfigurationContext configContext;
            try {
                configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(System.getProperty(ServerConstants.CARBON_HOME) + SoapEventAdaptorConstants.SERVER_CLIENT_DEPLOYMENT_DIR, System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH) + SoapEventAdaptorConstants.AXIS2_CLIENT_CONF_FILE);
                ServiceClient serviceClient = null;
                try {
                    serviceClient = new ServiceClient(configContext, null);
                    Options options = new Options();
                    options.setTo(new EndpointReference(url));
                    try {
                        if (headers != null) {
                            for (Map.Entry<String, String> headerValue : headers.entrySet()) {
                                options.setProperty(headerValue.getKey(), headerValue.getValue());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Invalid headers : \"" + headers + "\", ignoring headers...");
                    }

                    if (username != null || password != null) {
                        options.setUserName(username);
                        options.setPassword(password);
                        serviceClient.engageModule("rampart");
                        options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy());
                    }

                    serviceClient.setOptions(options);
                    serviceClient.fireAndForget(AXIOMUtil.stringToOM(payload.toString()));

                } catch (AxisFault axisFault) {
                    throw new OutputEventAdaptorEventProcessingException("Exception while sending events to soap endpoint ", axisFault);
                } catch (XMLStreamException e) {
                    throw new OutputEventAdaptorEventProcessingException("Exception while converting the event to xml object ", e);
                } catch (Exception e) {
                    throw new OutputEventAdaptorEventProcessingException(e.getMessage(), e);
                }
            } catch (AxisFault axisFault) {
                throw new OutputEventAdaptorEventProcessingException(axisFault.getMessage(), axisFault);
            }

        }

        private Policy loadPolicy() throws Exception {
            OMElement omElement = AXIOMUtil.stringToOM("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
                                                       "            xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"\n" +
                                                       "            wsu:Id=\"UTOverTransport\">\n" +
                                                       "    <wsp:ExactlyOne>\n" +
                                                       "        <wsp:All>\n" +
                                                       "            <sp:TransportBinding xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">\n" +
                                                       "                <wsp:Policy>\n" +
                                                       "                    <sp:TransportToken>\n" +
                                                       "                        <wsp:Policy>\n" +
                                                       "                            <sp:HttpsToken RequireClientCertificate=\"false\"></sp:HttpsToken>\n" +
                                                       "                        </wsp:Policy>\n" +
                                                       "                    </sp:TransportToken>\n" +
                                                       "                    <sp:AlgorithmSuite>\n" +
                                                       "                        <wsp:Policy>\n" +
                                                       "                            <sp:Basic256></sp:Basic256>\n" +
                                                       "                        </wsp:Policy>\n" +
                                                       "                    </sp:AlgorithmSuite>\n" +
                                                       "                    <sp:Layout>\n" +
                                                       "                        <wsp:Policy>\n" +
                                                       "                            <sp:Lax></sp:Lax>\n" +
                                                       "                        </wsp:Policy>\n" +
                                                       "                    </sp:Layout>\n" +
                                                       "                    <sp:IncludeTimestamp></sp:IncludeTimestamp>\n" +
                                                       "                </wsp:Policy>\n" +
                                                       "            </sp:TransportBinding>\n" +
                                                       "            <sp:SignedSupportingTokens\n" +
                                                       "                    xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">\n" +
                                                       "                <wsp:Policy>\n" +
                                                       "                    <sp:UsernameToken\n" +
                                                       "                            sp:IncludeToken=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient\"></sp:UsernameToken>\n" +
                                                       "                </wsp:Policy>\n" +
                                                       "            </sp:SignedSupportingTokens>\n" +
                                                       "        </wsp:All>\n" +
                                                       "    </wsp:ExactlyOne>\n" +
                                                       "</wsp:Policy>");
            return PolicyEngine.getPolicy(omElement);
        }
    }
}


