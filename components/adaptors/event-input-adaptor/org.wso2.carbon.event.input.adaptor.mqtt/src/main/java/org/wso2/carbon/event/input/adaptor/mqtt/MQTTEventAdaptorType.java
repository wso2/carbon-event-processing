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

package org.wso2.carbon.event.input.adaptor.mqtt;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.mqtt.internal.LateStartAdaptorListener;
import org.wso2.carbon.event.input.adaptor.mqtt.internal.ds.MQTTEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.input.adaptor.mqtt.internal.util.MQTTAdaptorListener;
import org.wso2.carbon.event.input.adaptor.mqtt.internal.util.MQTTBrokerConnectionConfiguration;
import org.wso2.carbon.event.input.adaptor.mqtt.internal.util.MQTTEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MQTTEventAdaptorType extends AbstractInputEventAdaptor implements
                                                                          LateStartAdaptorListener {

    private boolean readyToPoll = false;
    private static final Log log = LogFactory.getLog(MQTTEventAdaptorType.class);
    private static MQTTEventAdaptorType MQTTEventAdaptor = new MQTTEventAdaptorType();
    private ResourceBundle resourceBundle;
    public static ConcurrentHashMap<Integer, Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>>>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>>>>();

    List<LateStartAdaptorConfig> lateStartAdaptorConfigList = new ArrayList<LateStartAdaptorConfig>();

    private MQTTEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.TEXT);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.XML);

        return supportInputMessageTypes;
    }

    /**
     * @return WSO2EventReceiver event adaptor instance
     */
    public static MQTTEventAdaptorType getInstance() {

        return MQTTEventAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver event adaptor
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.mqtt.i18n.Resources", Locale.getDefault());
        MQTTEventAdaptorServiceValueHolder.addLateStartAdaptorListener(this);
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {

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
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

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

        return propertyList;

    }

    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!readyToPoll) {
            lateStartAdaptorConfigList.add(new LateStartAdaptorConfig(inputEventAdaptorMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId, tenantId));
        } else {
            createMQTTAdaptorListener(inputEventAdaptorMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId, tenantId);
        }

        return subscriptionId;
    }

    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>>> adaptorDestinationSubscriptionsMap = inputEventAdaptorListenerMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + topic + " for tenant " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>> destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputEventAdaptorConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + topic + " for event adaptor " + inputEventAdaptorConfiguration.getName());
        }

        ConcurrentHashMap<String, MQTTAdaptorListener> subscriptionsMap = destinationSubscriptionsMap.get(topic);
        if (subscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + topic);
        }

        MQTTAdaptorListener mqttAdaptorListener = subscriptionsMap.get(subscriptionId);
        if (mqttAdaptorListener == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + topic + " for the subscriptionId:" + subscriptionId);
        }

        mqttAdaptorListener.stopListener(inputEventAdaptorConfiguration.getName());

    }

    @Override
    public void tryStartAdaptor() {
        log.info("MQTT input event adaptor loading listeners ");
        readyToPoll = true;
        for (LateStartAdaptorConfig lateStartAdaptorConfig : lateStartAdaptorConfigList) {
            this.createMQTTAdaptorListener(lateStartAdaptorConfig.getInputEventAdaptorMessageConfiguration(), lateStartAdaptorConfig.getInputEventAdaptorListener(), lateStartAdaptorConfig.getInputEventAdaptorConfiguration(), lateStartAdaptorConfig.getAxisConfiguration(), lateStartAdaptorConfig.getSubscriptionId(), lateStartAdaptorConfig.getTenantId());
        }
    }


    private void createMQTTAdaptorListener(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId, int tenantId) {

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap == null) {
            tenantSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>>>();
            inputEventAdaptorListenerMap.put(tenantId, tenantSpecificListenerMap);
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>> adaptorSpecificListenerMap = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());

        if (adaptorSpecificListenerMap == null) {
            adaptorSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdaptorListener>>();
            if (null != tenantSpecificListenerMap.put(inputEventAdaptorConfiguration.getName(), adaptorSpecificListenerMap)) {
                adaptorSpecificListenerMap = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        ConcurrentHashMap<String, MQTTAdaptorListener> topicSpecificListenMap = adaptorSpecificListenerMap.get(topic);
        if (topicSpecificListenMap == null) {
            topicSpecificListenMap = new ConcurrentHashMap<String, MQTTAdaptorListener>();
            if (null != adaptorSpecificListenerMap.putIfAbsent(topic, topicSpecificListenMap)) {
                topicSpecificListenMap = adaptorSpecificListenerMap.get(topic);
            }
        }

        MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(inputEventAdaptorConfiguration.getInputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_URL), inputEventAdaptorConfiguration.getInputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_USERNAME), inputEventAdaptorConfiguration.getInputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_PASSWORD), inputEventAdaptorConfiguration.getInputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_CLEAN_SESSION), inputEventAdaptorConfiguration.getInputProperties().get(MQTTEventAdaptorConstants.ADAPTOR_CONF_KEEP_ALIVE));
        MQTTAdaptorListener mqttAdaptorListener = new MQTTAdaptorListener(mqttBrokerConnectionConfiguration, inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC), inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(MQTTEventAdaptorConstants.ADAPTOR_MESSAGE_CLIENTID), inputEventAdaptorListener);
        mqttAdaptorListener.startListener();
        topicSpecificListenMap.put(subscriptionId, mqttAdaptorListener);

    }
}

class LateStartAdaptorConfig {
    InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration;
    InputEventAdaptorListener inputEventAdaptorListener;
    InputEventAdaptorConfiguration inputEventAdaptorConfiguration;
    AxisConfiguration axisConfiguration;
    String subscriptionId;
    int tenantId;


    public LateStartAdaptorConfig(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId, int tenantId) {
        this.inputEventAdaptorMessageConfiguration = inputEventAdaptorMessageConfiguration;
        this.inputEventAdaptorListener = inputEventAdaptorListener;
        this.inputEventAdaptorConfiguration = inputEventAdaptorConfiguration;
        this.axisConfiguration = axisConfiguration;
        this.subscriptionId = subscriptionId;
        this.tenantId = tenantId;
    }

    public InputEventAdaptorMessageConfiguration getInputEventAdaptorMessageConfiguration() {
        return inputEventAdaptorMessageConfiguration;
    }

    public void setInputEventAdaptorMessageConfiguration(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration) {
        this.inputEventAdaptorMessageConfiguration = inputEventAdaptorMessageConfiguration;
    }

    public InputEventAdaptorListener getInputEventAdaptorListener() {
        return inputEventAdaptorListener;
    }

    public void setInputEventAdaptorListener(
            InputEventAdaptorListener inputEventAdaptorListener) {
        this.inputEventAdaptorListener = inputEventAdaptorListener;
    }

    public InputEventAdaptorConfiguration getInputEventAdaptorConfiguration() {
        return inputEventAdaptorConfiguration;
    }

    public void setInputEventAdaptorConfiguration(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration) {
        this.inputEventAdaptorConfiguration = inputEventAdaptorConfiguration;
    }

    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}


