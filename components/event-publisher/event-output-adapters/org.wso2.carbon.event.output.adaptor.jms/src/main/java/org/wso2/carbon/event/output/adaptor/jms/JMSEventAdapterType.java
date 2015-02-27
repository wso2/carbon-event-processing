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
package org.wso2.carbon.event.output.adaptor.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.publisher.core.adapter.AbstractOutputEventAdapter;
import org.wso2.carbon.event.publisher.core.MessageType;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.OutputAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.EndpointAdaptorProcessingException;
import org.wso2.carbon.event.output.adaptor.jms.internal.util.*;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JMSEventAdapterType extends AbstractOutputEventAdapter {

    private static final Log log = LogFactory.getLog(JMSEventAdapterType.class);
    private static JMSEventAdapterType jmsEventAdaptorAdaptor = new JMSEventAdapterType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>> publisherMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>>();

    private JMSEventAdapterType() {

    }

    /**
     * @return jms event adaptor instance
     */
    public static JMSEventAdapterType getInstance() {

        return jmsEventAdaptorAdaptor;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.MAP);
        supportOutputMessageTypes.add(MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return name of the jms event adaptor
     */
    @Override
    protected String getName() {
        return JMSEventAdaptorConstants.ADAPTOR_TYPE_JMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.jms.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // JNDI initial context factory class
        Property initialContextProperty = new Property(JMSEventAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS);
        initialContextProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        initialContextProperty.setRequired(true);
        initialContextProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT));
        propertyList.add(initialContextProperty);


        // JNDI Provider URL
        Property javaNamingProviderUrlProperty = new Property(JMSEventAdaptorConstants.JAVA_NAMING_PROVIDER_URL);
        javaNamingProviderUrlProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.JAVA_NAMING_PROVIDER_URL));
        javaNamingProviderUrlProperty.setRequired(true);
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.JAVA_NAMING_PROVIDER_URL_HINT));
        propertyList.add(javaNamingProviderUrlProperty);


        // JNDI Username
        Property userNameProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_USERNAME);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_USERNAME));
        propertyList.add(userNameProperty);


        // JNDI Password
        Property passwordProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_PASSWORD);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_PASSWORD));
        propertyList.add(passwordProperty);

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME));
        connectionFactoryNameProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_CONNECTION_FACTORY_JNDINAME_HINT));
        propertyList.add(connectionFactoryNameProperty);


        // Destination Type
        Property destinationTypeProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_TYPE);
        destinationTypeProperty.setRequired(true);
        destinationTypeProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_TYPE));
        destinationTypeProperty.setOptions(new String[]{"queue", "topic"});
        destinationTypeProperty.setDefaultValue("topic");
        destinationTypeProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_TYPE_HINT));
        propertyList.add(destinationTypeProperty);

        // Topic
        Property topicProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        // Header
        Property headerProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER);
        headerProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER));
        headerProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER_HINT));
        propertyList.add(headerProperty);


        return propertyList;

    }

    /**
     * @param message                      - is and Object[]{Event, EventDefinition}
     * @param outputAdaptorConfiguration the  object that will be used to
     *                                     get configuration information
     * @param tenantId                     tenant id of the calling thread.
     */
    public void publish(
            Object message,
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, PublisherDetails> topicEventSender = publisherMap.get(outputAdaptorConfiguration.getAdaptorName());
        if (null == topicEventSender) {
            topicEventSender = new ConcurrentHashMap<String, PublisherDetails>();
            if (null != publisherMap.putIfAbsent(outputAdaptorConfiguration.getAdaptorName(), topicEventSender)) {
                topicEventSender = publisherMap.get(outputAdaptorConfiguration.getAdaptorName());
            }
        }

        String topicName = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);
        PublisherDetails publisherDetails = topicEventSender.get(topicName);
        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);
        try {
            if (null == publisherDetails) {
                publisherDetails = initPublisher(outputAdaptorConfiguration, topicEventSender, topicName, messageConfig);
            }
            Message jmsMessage = publisherDetails.getJmsMessageSender().convertToJMSMessage(message, messageConfig);
            setJMSTransportHeaders(jmsMessage, outputAdaptorConfiguration.getEndpointAdaptorProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER));
            publisherDetails.getJmsMessageSender().send(jmsMessage, messageConfig);
        } catch (RuntimeException e) {
            log.warn("Caught exception: " + e.getMessage() + ". Reinitializing connection and sending...");
            publisherDetails = topicEventSender.remove(topicName);
            if (publisherDetails != null) {
                publisherDetails.getJmsMessageSender().close();
                publisherDetails.getJmsConnectionFactory().stop();
            }
            //TODO If this send also fails, the exception will be thrown up. Will that break the flow?
            // Retry sending after reinitializing connection
            publisherDetails = initPublisher(outputAdaptorConfiguration, topicEventSender, topicName, messageConfig);
            Message jmsMessage = publisherDetails.getJmsMessageSender().convertToJMSMessage(message, messageConfig);
            setJMSTransportHeaders(jmsMessage, outputAdaptorConfiguration.getEndpointAdaptorProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_HEADER));
            publisherDetails.getJmsMessageSender().send(jmsMessage, messageConfig);
        }
    }

    private PublisherDetails initPublisher(
            OutputAdaptorConfiguration outputAdaptorConfiguration,
            ConcurrentHashMap<String, PublisherDetails> topicEventSender, String topicName,
            Map<String, String> messageConfig) {
        PublisherDetails publisherDetails;
        Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
        adaptorProperties.putAll(outputAdaptorConfiguration.getEndpointAdaptorProperties());

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputAdaptorConfiguration.getAdaptorName());
        JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory, messageConfig);
        publisherDetails = new PublisherDetails(jmsConnectionFactory, jmsMessageSender);
        topicEventSender.put(topicName, publisherDetails);
        return publisherDetails;
    }

    private Message setJMSTransportHeaders(Message message, String headerProperty) {

        Map<String, String> messageConfiguration = new HashMap<String, String>();

        if (headerProperty != null && message != null) {
            String[] headers = headerProperty.split(",");

            if (headers != null && headers.length > 0) {
                for (String header : headers) {
                    String[] headerPropertyWithValue = header.split(":");
                    if (headerPropertyWithValue.length == 2) {
                        messageConfiguration.put(headerPropertyWithValue[0], headerPropertyWithValue[1]);
                    } else {
                        log.warn("Header property not defined in the correct format");
                    }
                }
            }

            try {
                return JMSUtils.setTransportHeaders(messageConfiguration, message);
            } catch (JMSException e) {
                throw new EndpointAdaptorProcessingException(e);
            }
        }

        return message;
    }

    @Override
    public void testConnection(
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {
        try {
            Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
            adaptorProperties.putAll(outputAdaptorConfiguration.getEndpointAdaptorProperties());

            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputAdaptorConfiguration.getAdaptorName());
            Connection connection = jmsConnectionFactory.getConnection();
            connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.close();
            jmsConnectionFactory.stop();
        } catch (Exception e) {
            throw new EndpointAdaptorProcessingException(e);
        }
    }

    @Override
    public void removeConnectionInfo(
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {
        ConcurrentHashMap<String, PublisherDetails> topicEventSenderMap = publisherMap.get(outputAdaptorConfiguration.getAdaptorName());
        if (topicEventSenderMap != null) {
            String topicName = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);
            topicEventSenderMap.remove(topicName);
        }
    }

    class PublisherDetails {
        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSMessageSender jmsMessageSender;

        public PublisherDetails(JMSConnectionFactory jmsConnectionFactory,
                                JMSMessageSender jmsMessageSender) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsMessageSender = jmsMessageSender;
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSMessageSender getJmsMessageSender() {
            return jmsMessageSender;
        }
    }


}
