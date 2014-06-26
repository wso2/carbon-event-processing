package org.wso2.carbon.event.input.adaptor.mqtt.internal.util;

public class MQTTBrokerConnectionConfiguration {

    private String brokerUsername = null;
    private String brokerPassword = null;
    private boolean cleanSession = false;
    private String brokerUrl;

    public String getBrokerPassword() {
        return brokerPassword;
    }

    public void setBrokerPassword(String brokerPassword) {
        this.brokerPassword = brokerPassword;
    }

    public String getBrokerUsername() {
        return brokerUsername;
    }

    public void setBrokerUsername(String brokerUsername) {
        this.brokerUsername = brokerUsername;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }


    public boolean isCleanSession() {
        return cleanSession;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public MQTTBrokerConnectionConfiguration(String brokerUrl, String brokerUsername,
                                             String brokerPassword, boolean cleanSession) {
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.brokerUrl = brokerUrl;
        this.cleanSession = cleanSession;
    }
}
