/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.mqtt.internal.util.MQTTAdaptorPublisher;
import org.wso2.carbon.event.output.adaptor.mqtt.internal.util.MQTTBrokerConnectionConfiguration;
import org.wso2.carbon.event.output.adaptor.mqtt.internal.util.MQTTEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class MQTTEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(MQTTEventAdaptorType.class);
    private static MQTTEventAdaptorType MQTTEventAdaptorAdaptor = new MQTTEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>>> publisherMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>>>();

    private MQTTEventAdaptorType() {

    }

    /**
     * @return mqtt event adaptor instance
     */
    public static MQTTEventAdaptorType getInstance() {

        return MQTTEventAdaptorAdaptor;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return name of the mqtt event adaptor
     */
    @Override
    protected String getName() {
        return MQTTEventAdaptorConstants.ADAPTOR_TYPE_MQTT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.mqtt.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();
        //Broker Url
        Property brokerUrl = new Property(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL);
        brokerUrl.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL));
        brokerUrl.setRequired(true);
        brokerUrl.setHint(resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL_HINT));
        propertyList.add(brokerUrl);


        //Broker Username
        Property userName = new Property(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME);
        userName.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME));
        userName.setRequired(false);
        userName.setHint(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME_HINT));
        propertyList.add(userName);

        //Broker Password
        Property password = new Property(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD));
        password.setRequired(false);
        password.setHint(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD_HINT));
        propertyList.add(password);


        //Broker clear session
        Property clearSession = new Property(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION);
        clearSession.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION));
        clearSession.setRequired(false);
        clearSession.setOptions(new String[]{"true", "false"});
        clearSession.setDefaultValue("true");
        clearSession.setHint(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION_HINT));
        propertyList.add(clearSession);

        //Broker clear session
        Property keepAlive = new Property(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE);
        keepAlive.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE));
        keepAlive.setRequired(false);
        propertyList.add(keepAlive);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set topic
        Property topicProperty = new Property(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        // set clientId
        Property clientId = new Property(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID);
        clientId.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID));
        clientId.setRequired(true);
        propertyList.add(clientId);

        // set Quality f Service
        Property qos = new Property(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_QOS);
        qos.setDisplayName(
                resourceBundle.getString(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_QOS));
        qos.setRequired(false);
        qos.setOptions(new String[]{"0", "1", "2"});
        qos.setDefaultValue("1");
        propertyList.add(qos);

        return propertyList;
    }

    /**
     * @param outputEventAdaptorMessageConfiguration
     *                 - topic name to publish messages
     * @param message  - is and Object[]{Event, EventDefinition}
     * @param outputEventAdaptorConfiguration
     *                 the {@link OutputEventAdaptorConfiguration} object that will be used to
     *                 get configuration information
     * @param tenantId tenant id of the calling thread.
     */
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object message,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>> clientIdSpecificEventSenderMap = publisherMap.get(outputEventAdaptorConfiguration.getName());
        if (null == clientIdSpecificEventSenderMap) {
            clientIdSpecificEventSenderMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>>();
            if (null != publisherMap.putIfAbsent(outputEventAdaptorConfiguration.getName(), clientIdSpecificEventSenderMap)) {
                clientIdSpecificEventSenderMap = publisherMap.get(outputEventAdaptorConfiguration.getName());
            }
        }

        String clientId = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID);
        ConcurrentHashMap<String, MQTTAdaptorPublisher> topicSpecificEventPublisherMap = clientIdSpecificEventSenderMap.get(clientId);
        if (null == topicSpecificEventPublisherMap) {
            topicSpecificEventPublisherMap = new ConcurrentHashMap<String, MQTTAdaptorPublisher>();
            if (null != clientIdSpecificEventSenderMap.putIfAbsent(clientId, topicSpecificEventPublisherMap)) {
                topicSpecificEventPublisherMap = clientIdSpecificEventSenderMap.get(clientId);
            }
        }

        String topic = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        MQTTAdaptorPublisher mqttAdaptorPublisher = topicSpecificEventPublisherMap.get(topic);
        if (mqttAdaptorPublisher == null) {
            MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE));
            mqttAdaptorPublisher = new MQTTAdaptorPublisher(mqttBrokerConnectionConfiguration, outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC), outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID));

        }
        String qos = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_QOS);

        try {
            if (qos == null) {
                mqttAdaptorPublisher.publish(message.toString());
            } else {
                mqttAdaptorPublisher.publish(Integer.parseInt(qos), message.toString());
            }
        } catch (OutputEventAdaptorEventProcessingException ex) {
            log.error(ex);
            topicSpecificEventPublisherMap.remove(topic);
            throw new OutputEventAdaptorEventProcessingException(ex);
        }
    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        try {
            MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION), outputEventAdaptorConfiguration.getOutputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE));
            new MQTTAdaptorPublisher(mqttBrokerConnectionConfiguration, "testTopic", "testClientID");

        } catch (Exception e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }

    @Override
    public void removeConnectionInfo(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>> clientIdSpecificEventSenderMap = publisherMap.get(outputEventAdaptorConfiguration.getName());
        if (clientIdSpecificEventSenderMap != null) {
            String clientId = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID);
            ConcurrentHashMap<String, MQTTAdaptorPublisher> topicSpecificEventSenderMap = clientIdSpecificEventSenderMap.get(clientId);
            if (topicSpecificEventSenderMap != null) {
                String topicName = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
                MQTTAdaptorPublisher mqttAdaptorPublisher = topicSpecificEventSenderMap.get(topicName);
                if (mqttAdaptorPublisher != null) {
                    try {
                        mqttAdaptorPublisher.close();
                    } catch (OutputEventAdaptorEventProcessingException e) {
                        throw new OutputEventAdaptorEventProcessingException(e);
                    }
                }
                topicSpecificEventSenderMap.remove(topicName);
            }
        }
    }


}
