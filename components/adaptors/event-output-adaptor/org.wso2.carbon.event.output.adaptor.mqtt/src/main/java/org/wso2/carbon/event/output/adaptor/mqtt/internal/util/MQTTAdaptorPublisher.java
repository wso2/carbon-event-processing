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
package org.wso2.carbon.event.output.adaptor.mqtt.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;


public class MQTTAdaptorPublisher {

    private static final Log log = LogFactory.getLog(MQTTAdaptorPublisher.class);

    private MqttClient mqttClient;
    private MqttConnectOptions connectionOptions;
    private boolean cleanSession;
    private int keepAlive;
    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;
    private String mqttClientId;
    private String topic;

    public MQTTAdaptorPublisher(MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration,
                                String topic, String mqttClientId) {

        this.mqttBrokerConnectionConfiguration = mqttBrokerConnectionConfiguration;
        this.mqttClientId = mqttClientId;
        this.cleanSession = mqttBrokerConnectionConfiguration.isCleanSession();
        this.keepAlive = mqttBrokerConnectionConfiguration.getKeepAlive();
        this.topic = topic;

        //SORTING messages until the server fetches them
        String temp_directory = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(temp_directory);


        try {
            // Construct the connection options object that contains connection parameters
            // such as cleanSession and LWT
            connectionOptions = new MqttConnectOptions();
            connectionOptions.setCleanSession(cleanSession);
            connectionOptions.setKeepAliveInterval(keepAlive);
            if (this.mqttBrokerConnectionConfiguration.getBrokerPassword() != null) {
                connectionOptions.setPassword(this.mqttBrokerConnectionConfiguration.getBrokerPassword().toCharArray());
            }
            if (this.mqttBrokerConnectionConfiguration.getBrokerUsername() != null) {
                connectionOptions.setUserName(this.mqttBrokerConnectionConfiguration.getBrokerUsername());
            }

            // Construct an MQTT blocking mode client
            mqttClient = new MqttClient(this.mqttBrokerConnectionConfiguration.getBrokerUrl(), this.mqttClientId, dataStore);
            mqttClient.connect(connectionOptions);

        } catch (MqttException e) {
            log.error(e);
            throw new OutputEventAdaptorEventProcessingException(e);
        }

    }

    public void publish(int qos, String payload) throws OutputEventAdaptorEventProcessingException {
        try {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);

            // Send the message to the server, control is not returned until
            // it has been delivered to the server meeting the specified
            // quality of service.
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }

    public void publish(String payload) throws OutputEventAdaptorEventProcessingException {
        try {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload.getBytes());
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }

    public void close() throws OutputEventAdaptorEventProcessingException {
        try {
            mqttClient.disconnect(1000);
            mqttClient.close();
        } catch (MqttException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        }
    }

}
