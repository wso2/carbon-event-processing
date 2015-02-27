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
package org.wso2.carbon.event.output.adaptor.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.publisher.core.adapter.AbstractOutputEventAdapter;
import org.wso2.carbon.event.publisher.core.MessageType;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.OutputAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.EndpointAdaptorProcessingException;
import org.wso2.carbon.event.output.adaptor.mqtt.internal.util.MQTTAdaptorPublisher;
import org.wso2.carbon.event.output.adaptor.mqtt.internal.util.MQTTBrokerConnectionConfiguration;
import org.wso2.carbon.event.output.adaptor.mqtt.internal.util.MQTTEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class MQTTEventAdapterType extends AbstractOutputEventAdapter {

    private static final Log log = LogFactory.getLog(MQTTEventAdapterType.class);
    private static MQTTEventAdapterType MQTTEventAdaptorAdaptor = new MQTTEventAdapterType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>>> publisherMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>>>();

    private MQTTEventAdapterType() {

    }

    /**
     * @return mqtt event adaptor instance
     */
    public static MQTTEventAdapterType getInstance() {

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
     * @param message                      - is and Object[]{Event, EventDefinition}
     * @param outputAdaptorConfiguration the object that will be used to
     *                                     get configuration information
     * @param tenantId                     tenant id of the calling thread.
     */
    public void publish(
            Object message,
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>> clientIdSpecificEventSenderMap = publisherMap.get(outputAdaptorConfiguration.getAdaptorName());
        if (null == clientIdSpecificEventSenderMap) {
            clientIdSpecificEventSenderMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>>();
            if (null != publisherMap.putIfAbsent(outputAdaptorConfiguration.getAdaptorName(), clientIdSpecificEventSenderMap)) {
                clientIdSpecificEventSenderMap = publisherMap.get(outputAdaptorConfiguration.getAdaptorName());
            }
        }

        String clientId = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID);
        ConcurrentHashMap<String, MQTTAdaptorPublisher> topicSpecificEventPublisherMap = clientIdSpecificEventSenderMap.get(clientId);
        if (null == topicSpecificEventPublisherMap) {
            topicSpecificEventPublisherMap = new ConcurrentHashMap<String, MQTTAdaptorPublisher>();
            if (null != clientIdSpecificEventSenderMap.putIfAbsent(clientId, topicSpecificEventPublisherMap)) {
                topicSpecificEventPublisherMap = clientIdSpecificEventSenderMap.get(clientId);
            }
        }

        String topic = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        MQTTAdaptorPublisher mqttAdaptorPublisher = topicSpecificEventPublisherMap.get(topic);
        if (mqttAdaptorPublisher == null) {
            MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE));
            mqttAdaptorPublisher = new MQTTAdaptorPublisher(mqttBrokerConnectionConfiguration, outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID));
            topicSpecificEventPublisherMap.put(topic, mqttAdaptorPublisher);
        }
        String qos = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_QOS);

        try {
            if (qos == null) {
                mqttAdaptorPublisher.publish(message.toString());
            } else {
                mqttAdaptorPublisher.publish(Integer.parseInt(qos), message.toString());
            }
        } catch (EndpointAdaptorProcessingException ex) {
            log.error(ex);
            topicSpecificEventPublisherMap.remove(topic);
            throw new EndpointAdaptorProcessingException(ex);
        }
    }

    @Override
    public void testConnection(
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {
        try {
            MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION), outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE));
            new MQTTAdaptorPublisher(mqttBrokerConnectionConfiguration, "testTopic", "testClientID");

        } catch (Exception e) {
            throw new EndpointAdaptorProcessingException(e);
        }
    }

    @Override
    public void removeConnectionInfo(
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorPublisher>> clientIdSpecificEventSenderMap = publisherMap.get(outputAdaptorConfiguration.getAdaptorName());
        if (clientIdSpecificEventSenderMap != null) {
            String clientId = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID);
            ConcurrentHashMap<String, MQTTAdaptorPublisher> topicSpecificEventSenderMap = clientIdSpecificEventSenderMap.get(clientId);
            if (topicSpecificEventSenderMap != null) {
                String topicName = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
                MQTTAdaptorPublisher mqttAdaptorPublisher = topicSpecificEventSenderMap.get(topicName);
                if (mqttAdaptorPublisher != null) {
                    mqttAdaptorPublisher.close();
                }
                topicSpecificEventSenderMap.remove(topicName);
            }
        }
    }


}
