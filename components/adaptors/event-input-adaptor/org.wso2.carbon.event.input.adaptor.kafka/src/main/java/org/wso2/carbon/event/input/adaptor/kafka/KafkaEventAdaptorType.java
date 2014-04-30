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
package org.wso2.carbon.event.input.adaptor.kafka;

import kafka.consumer.ConsumerConfig;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.kafka.internal.LateStartAdaptorListener;
import org.wso2.carbon.event.input.adaptor.kafka.internal.ds.KafkaEventAdaptorServiceHolder;
import org.wso2.carbon.event.input.adaptor.kafka.internal.util.ConsumerKafkaConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;

public final class KafkaEventAdaptorType extends AbstractInputEventAdaptor implements
                                                                           LateStartAdaptorListener {

    private boolean readyToPoll = false;
    private static final Log log = LogFactory.getLog(KafkaEventAdaptorType.class);
    private ResourceBundle resourceBundle;
    private static KafkaEventAdaptorType kafkaAdaptorEventAdaptor = new KafkaEventAdaptorType();
    Map<String, ConsumerKafkaAdaptor> consumerAdaptorMap = new HashMap<String, ConsumerKafkaAdaptor>();
    List<LateStartAdaptorConfig> lateStartAdaptorConfigList = new ArrayList<LateStartAdaptorConfig>();

    private KafkaEventAdaptorType() {
    }

    public static KafkaEventAdaptorType getInstance() {

        return kafkaAdaptorEventAdaptor;
    }

    @Override
    protected String getName() {
        return ConsumerKafkaConstants.ADAPTOR_TYPE_KAFKA;
    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.kafka.i18n.Resources", Locale.getDefault());
        KafkaEventAdaptorServiceHolder.addLateStartAdaptorListener(this);
    }

    @Override
    protected List<Property> getInputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        //set Zk Connect of broker
        Property webZKConnect = new Property(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT);
        webZKConnect.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT));
        webZKConnect.setHint(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT_HINT));
        webZKConnect.setRequired(true);
        propertyList.add(webZKConnect);

        //set GroupID of broker
        Property webGroupID = new Property(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_GROUP_ID);
        webGroupID.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_GROUP_ID));
        webGroupID.setHint(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_GROUP_ID_hint));
        webGroupID.setRequired(true);
        propertyList.add(webGroupID);

        //set Subscriber threads
        Property webThreads = new Property(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_THREADS);
        webThreads.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_THREADS));
        webThreads.setHint(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_THREADS_HINT));
        webThreads.setRequired(true);
        propertyList.add(webThreads);

        Property optionConfigProperties = new Property(ConsumerKafkaConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        optionConfigProperties.setDisplayName(
                resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES));
        optionConfigProperties.setHint(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES_HINT));
        propertyList.add(optionConfigProperties);

        return propertyList;
    }

    @Override
    protected List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();
        //set Topic of broker
        Property webTopic = new Property(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_TOPIC);
        webTopic.setDisplayName(resourceBundle.getString(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_TOPIC));
        webTopic.setRequired(true);
        propertyList.add(webTopic);
        return propertyList;
    }

    @Override
    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();
        if (!readyToPoll) {
            lateStartAdaptorConfigList.add(new LateStartAdaptorConfig(inputEventAdaptorMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId));
        } else {
            createKafkaAdaptorListener(inputEventAdaptorMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
        }
        return subscriptionId;
    }

    @Override
    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {
        if (consumerAdaptorMap != null) {
            ConsumerKafkaAdaptor consumerKafkaAdaptor = consumerAdaptorMap.get(subscriptionId);
            if (consumerKafkaAdaptor != null) {
                consumerKafkaAdaptor.shutdown();
            }
        }
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId,
                                                       String optionalConfigs) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);

        if (optionalConfigs != null) {
            String[] optionalProperties = optionalConfigs.split(",");

            if (optionalProperties != null && optionalProperties.length > 0) {
                for (String header : optionalProperties) {
                    String[] configPropertyWithValue = header.split(":");
                    if (configPropertyWithValue.length == 2) {
                        props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                    } else {
                        log.warn("Optional configuration property not defined in the correct format");
                    }
                }
            }
        }
        return new ConsumerConfig(props);
    }

    @Override
    public void tryStartAdaptor() {
        log.info("Kafka input event adaptor loading listeners ");
        readyToPoll = true;
        for (LateStartAdaptorConfig lateStartAdaptorConfig : lateStartAdaptorConfigList) {
            this.createKafkaAdaptorListener(lateStartAdaptorConfig.getInputEventAdaptorMessageConfiguration(), lateStartAdaptorConfig.getInputEventAdaptorListener(), lateStartAdaptorConfig.getInputEventAdaptorConfiguration(), lateStartAdaptorConfig.getAxisConfiguration(), lateStartAdaptorConfig.getSubscriptionId());
        }
    }

    private void createKafkaAdaptorListener(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {
        Map<String, String> brokerProperties = new HashMap<String, String>();
        brokerProperties.putAll(inputEventAdaptorConfiguration.getInputProperties());
        String zkConnect = brokerProperties.get(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT);
        String groupID = brokerProperties.get(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_GROUP_ID);
        String threadsStr = brokerProperties.get(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_THREADS);
        String optionalConfiguration = brokerProperties.get(ConsumerKafkaConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        int threads = Integer.parseInt(threadsStr);

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(ConsumerKafkaConstants.ADAPTOR_SUSCRIBER_TOPIC);

        ConsumerKafkaAdaptor consumerAdaptor = new ConsumerKafkaAdaptor(topic,
                                                   KafkaEventAdaptorType.createConsumerConfig(zkConnect, groupID, optionalConfiguration));
        consumerAdaptor.run(threads, inputEventAdaptorListener);
        consumerAdaptorMap.put(subscriptionId,consumerAdaptor);
    }

    class LateStartAdaptorConfig {
        InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration;
        InputEventAdaptorListener inputEventAdaptorListener;
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration;
        AxisConfiguration axisConfiguration;
        String subscriptionId;


        public LateStartAdaptorConfig(
                InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                InputEventAdaptorListener inputEventAdaptorListener,
                InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                AxisConfiguration axisConfiguration, String subscriptionId) {
            this.inputEventAdaptorMessageConfiguration = inputEventAdaptorMessageConfiguration;
            this.inputEventAdaptorListener = inputEventAdaptorListener;
            this.inputEventAdaptorConfiguration = inputEventAdaptorConfiguration;
            this.axisConfiguration = axisConfiguration;
            this.subscriptionId = subscriptionId;
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
    }

}
