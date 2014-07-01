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

package org.wso2.carbon.event.input.adaptor.jms;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.jms.internal.LateStartAdaptorListener;
import org.wso2.carbon.event.input.adaptor.jms.internal.ds.JMSEventAdaptorServiceHolder;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSConnectionFactory;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSConstants;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSEventAdaptorConstants;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSListener;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSMessageListener;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSTaskManager;
import org.wso2.carbon.event.input.adaptor.jms.internal.util.JMSTaskManagerFactory;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class JMSEventAdaptorType extends AbstractInputEventAdaptor implements
                                                                         LateStartAdaptorListener {

    private boolean readyToPoll = false;
    private static final Log log = LogFactory.getLog(JMSEventAdaptorType.class);
    private static JMSEventAdaptorType jmsEventAdaptorAdaptor = new JMSEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>> tenantAdaptorDestinationSubscriptionsMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>>();
    List<LateStartAdaptorConfig> lateStartAdaptorConfigList = new ArrayList<LateStartAdaptorConfig>();

    private JMSEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.MAP);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    /**
     * @return agent event adaptor instance
     */
    public static JMSEventAdaptorType getInstance() {

        return jmsEventAdaptorAdaptor;
    }

    /**
     * @return name of the event adaptor
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
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.jms.i18n.Resources", Locale.getDefault());
        JMSEventAdaptorServiceHolder.addLateStartAdaptorListener(this);
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {

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

        // Connection Factory JNDI Name
        Property subscriberNameProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_DURABLE_SUBSCRIBER_NAME);
        subscriberNameProperty.setRequired(false);
        subscriberNameProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DURABLE_SUBSCRIBER_NAME));
        subscriberNameProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DURABLE_SUBSCRIBER_NAME_HINT));
        propertyList.add(subscriberNameProperty);

        return propertyList;
    }

    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // Topic
        Property topicProperty = new Property(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION_HINT));
        propertyList.add(topicProperty);

        return propertyList;

    }


    public String subscribe(InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {


        String subscriptionId = UUID.randomUUID().toString();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!readyToPoll) {
            lateStartAdaptorConfigList.add(new LateStartAdaptorConfig(inputEventMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId, tenantId));
        } else {
            createJMSAdaptorListener(inputEventMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId, tenantId);
        }

        return subscriptionId;
    }


    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {

        String destination = inputEventMessageConfiguration.getInputMessageProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>> adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + destination + " for tenant " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>> destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputEventAdaptorConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + destination + " for event adaptor " + inputEventAdaptorConfiguration.getName());
        }

        ConcurrentHashMap<String, SubscriptionDetails> subscriptionsMap = destinationSubscriptionsMap.get(destination);
        if (subscriptionsMap == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + destination);
        }

        SubscriptionDetails subscriptionDetails = subscriptionsMap.get(subscriptionId);
        if (subscriptionDetails == null) {
            throw new InputEventAdaptorEventProcessingException("There is no subscription for " + destination + " for the subscriptionId:" + subscriptionId);
        }

        try {
            subscriptionDetails.close();
        } catch (JMSException e) {
            throw new InputEventAdaptorEventProcessingException("Can not unsubscribe from the destination " + destination + " with the event adaptor " + inputEventAdaptorConfiguration.getName(), e);
        }

    }

    @Override
    public void tryStartAdaptor() {
        log.info("JMS input event adaptor loading listeners ");
        readyToPoll = true;
        for (LateStartAdaptorConfig lateStartAdaptorConfig : lateStartAdaptorConfigList) {
            this.createJMSAdaptorListener(lateStartAdaptorConfig.getInputEventAdaptorMessageConfiguration(), lateStartAdaptorConfig.getInputEventAdaptorListener(), lateStartAdaptorConfig.getInputEventAdaptorConfiguration(), lateStartAdaptorConfig.getAxisConfiguration(), lateStartAdaptorConfig.getSubscriptionId(), lateStartAdaptorConfig.getTenantId());
        }
    }

    private void createJMSAdaptorListener(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId, int tenantId) {


        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>> adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            adaptorDestinationSubscriptionsMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>();
            if (null != tenantAdaptorDestinationSubscriptionsMap.putIfAbsent(tenantId, adaptorDestinationSubscriptionsMap)) {
                adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
            }
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>> destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputEventAdaptorConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            destinationSubscriptionsMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>();
            if (null != adaptorDestinationSubscriptionsMap.putIfAbsent(inputEventAdaptorConfiguration.getName(), destinationSubscriptionsMap)) {
                destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        String destination = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_DESTINATION);

        ConcurrentHashMap<String, SubscriptionDetails> subscriptionsMap = destinationSubscriptionsMap.get(destination);
        if (subscriptionsMap == null) {
            subscriptionsMap = new ConcurrentHashMap<String, SubscriptionDetails>();
            if (null != destinationSubscriptionsMap.putIfAbsent(destination, subscriptionsMap)) {
                subscriptionsMap = destinationSubscriptionsMap.get(destination);
            }
        }


        Map<String, String> adaptorProperties = new HashMap<String, String>();
        if(inputEventAdaptorConfiguration.getInputProperties().get(JMSEventAdaptorConstants.ADAPTOR_JMS_DURABLE_SUBSCRIBER_NAME) != null){
            InternalInputEventAdaptorConfiguration internalInputEventAdaptorConfiguration = inputEventAdaptorConfiguration.getInputConfiguration();
            internalInputEventAdaptorConfiguration.addEventAdaptorProperty(JMSEventAdaptorConstants.ADAPTOR_JMS_SUBSCRIPTION_DURABLE,"true");
            inputEventAdaptorConfiguration.setInputConfiguration(internalInputEventAdaptorConfiguration);
        }else {
            InternalInputEventAdaptorConfiguration internalInputEventAdaptorConfiguration = inputEventAdaptorConfiguration.getInputConfiguration();
            internalInputEventAdaptorConfiguration.addEventAdaptorProperty(JMSEventAdaptorConstants.ADAPTOR_JMS_SUBSCRIPTION_DURABLE,"false");
            inputEventAdaptorConfiguration.setInputConfiguration(internalInputEventAdaptorConfiguration);
        }

        adaptorProperties.putAll(inputEventAdaptorConfiguration.getInputProperties());

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(new Hashtable<String, String>(adaptorProperties), inputEventAdaptorConfiguration.getName());

        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, destination);
        JMSTaskManager jmsTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory, inputEventAdaptorConfiguration.getName(), new NativeWorkerPool(4, 100, 1000, 1000, "JMS Threads", "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManager.setJmsMessageListener(new JMSMessageListener(inputEventAdaptorListener, axisConfiguration));

        JMSListener jmsListener = new JMSListener(inputEventAdaptorConfiguration.getName() + "#" + destination, jmsTaskManager);
        jmsListener.startListener();
        SubscriptionDetails subscriptionDetails = new SubscriptionDetails(jmsConnectionFactory, jmsListener);
        subscriptionsMap.put(subscriptionId, subscriptionDetails);

    }

    class SubscriptionDetails {

        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSListener jmsListener;

        public SubscriptionDetails(JMSConnectionFactory jmsConnectionFactory,
                                   JMSListener jmsListener) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsListener = jmsListener;
        }

        public void close() throws JMSException {
            this.jmsListener.stopListener();
            this.jmsConnectionFactory.stop();
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSListener getJmsListener() {
            return jmsListener;
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

}
