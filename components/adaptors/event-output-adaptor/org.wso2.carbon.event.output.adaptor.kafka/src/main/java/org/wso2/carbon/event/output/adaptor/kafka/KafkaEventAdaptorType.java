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
package org.wso2.carbon.event.output.adaptor.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.kafka.internal.util.KafkaOutEventAdaptorConstants;

import java.util.*;

public final class KafkaEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(KafkaEventAdaptorType.class);
    private ResourceBundle resourceBundle;
    private static KafkaEventAdaptorType kafkaAdaptor = new KafkaEventAdaptorType();

    @Override
    protected String getName() {
        return KafkaOutEventAdaptorConstants.ADAPTOR_TYPE_KAFKA;
    }

    public static KafkaEventAdaptorType getInstance() {
        return kafkaAdaptor;
    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.TEXT);
        supportOutputMessageTypes.add(MessageType.XML);
        return supportOutputMessageTypes;
    }

    @Override
    protected void init() {
        this.resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.kafka.i18n.Resources", Locale.getDefault());

    }

    @Override
    protected List<Property> getOutputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        //set Kafka Connect of broker
        Property webKafkaConnect = new Property(KafkaOutEventAdaptorConstants.ADAPTOR_META_BROKER_LIST);
        webKafkaConnect.setDisplayName(resourceBundle.getString(KafkaOutEventAdaptorConstants.ADAPTOR_META_BROKER_LIST));
        webKafkaConnect.setRequired(true);
        propertyList.add(webKafkaConnect);

        Property optionConfigProperties = new Property(KafkaOutEventAdaptorConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        optionConfigProperties.setDisplayName(
                resourceBundle.getString(KafkaOutEventAdaptorConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES));
        optionConfigProperties.setHint(resourceBundle.getString(KafkaOutEventAdaptorConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES_HINT));
        propertyList.add(optionConfigProperties);

        return propertyList;
    }

    @Override
    protected List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        //set Topic of broker
        Property webTopic = new Property(KafkaOutEventAdaptorConstants.ADAPTOR_PUBLISH_TOPIC);
        webTopic.setDisplayName(resourceBundle.getString(KafkaOutEventAdaptorConstants.ADAPTOR_PUBLISH_TOPIC));
        webTopic.setRequired(true);
        propertyList.add(webTopic);

        return propertyList;
    }

    @Override
    public void publish(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            Object event, OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
            int tenantId) {
        Map<String, String> brokerProperties = outputEventAdaptorConfiguration.getOutputProperties();
        String kafkaConnect = brokerProperties.get(KafkaOutEventAdaptorConstants.ADAPTOR_META_BROKER_LIST);

        Map<String, String> messageProperties = outputEventAdaptorMessageConfiguration.getOutputMessageProperties();
        String topic = messageProperties.get(KafkaOutEventAdaptorConstants.ADAPTOR_PUBLISH_TOPIC);
        String optionalConfigs = brokerProperties.get(KafkaOutEventAdaptorConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        Properties props = new Properties();
        props.put("metadata.broker.list", kafkaConnect);
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        if (optionalConfigs != null) {
            String[] optionalProperties = optionalConfigs.split(",");

            if (optionalProperties != null && optionalProperties.length > 0) {
                for (String header : optionalProperties) {
                    String[] configPropertyWithValue = header.split(":");
                    if(configPropertyWithValue.length == 2){
                        props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                    }else {
                        log.warn("Optional configuration property not defined in the correct format");
                    }
                }
            }
        }

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, Object> producer = new Producer<String, Object>(config);
        KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(topic,event.toString());
        producer.send(data);
        producer.close();
    }

    @Override
    public void testConnection(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        throw new TestConnectionUnavailableException("not-available");
    }

    @Override
    public void removeConnectionInfo(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        //not-required
    }
}
