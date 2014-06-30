package org.wso2.carbon.event.input.adaptor.mqtt.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;


public class MQTTAdaptorListener implements MqttCallback {

    private static final Log log = LogFactory.getLog(MQTTAdaptorListener.class);

    private MqttClient mqttClient;
    private MqttConnectOptions connectionOptions;
    private boolean cleanSession;
    private int keepAlive;
    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;
    private String mqttClientId;
    private String topic;

    private InputEventAdaptorListener eventAdaptorListener = null;


    public MQTTAdaptorListener(MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration,
                               String topic, String mqttClientId,
                               InputEventAdaptorListener inputEventAdaptorListener) {

        this.mqttBrokerConnectionConfiguration = mqttBrokerConnectionConfiguration;
        this.mqttClientId = mqttClientId;
        this.cleanSession = mqttBrokerConnectionConfiguration.isCleanSession();
        this.keepAlive = mqttBrokerConnectionConfiguration.getKeepAlive();
        this.topic = topic;
        eventAdaptorListener = inputEventAdaptorListener;

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

            // Set this wrapper as the callback handler
            mqttClient.setCallback(this);

        } catch (MqttException e) {
            log.error("Exception occurred while subscribing to MQTT broker" + e);
            throw new InputEventAdaptorEventProcessingException(e);
        } catch (Throwable e) {
            log.error("Exception occurred while subscribing to MQTT broker" + e);
            throw new InputEventAdaptorEventProcessingException(e);
        }

    }

    public void startListener() throws InputEventAdaptorEventProcessingException {
        try {
            // Connect to the MQTT server
            mqttClient.connect(connectionOptions);

            // Subscribe to the requested topic
            // The QoS specified is the maximum level that messages will be sent to the client at.
            // For instance if QoS 1 is specified, any messages originally published at QoS 2 will
            // be downgraded to 1 when delivering to the client but messages published at 1 and 0
            // will be received at the same level they were published at.
            mqttClient.subscribe(topic);
        } catch (MqttException e) {
            throw new InputEventAdaptorEventProcessingException(e);
        }

    }

    public void stopListener(String adaptorName) throws InputEventAdaptorEventProcessingException {
        try {
            // Disconnect to the MQTT server
            mqttClient.disconnect(3000);
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            throw new InputEventAdaptorEventProcessingException("Can not unsubscribe from the destination " + topic + " with the event adaptor " + adaptorName, e);
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        try {
            String msgText = mqttMessage.toString();
            eventAdaptorListener.onEventCall(msgText);
        } catch (InputEventAdaptorEventProcessingException e) {
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
