package org.wso2.carbon.event.output.adaptor.mqtt.internal.util;

public class MQTTBrokerConnectionConfiguration {

    private String brokerUsername = null;
    private String brokerPassword = null;
    private boolean cleanSession = true;
    private int keepAlive = 60000;
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

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    public MQTTBrokerConnectionConfiguration(String brokerUrl, String brokerUsername,
                                             String brokerPassword, String cleanSession,
                                             String keepAlive) {
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.brokerUrl = brokerUrl;
        if (cleanSession != null) {
            this.cleanSession = Boolean.parseBoolean(cleanSession);
        }
        if (keepAlive != null) {
            this.keepAlive = Integer.parseInt(keepAlive);
        }
    }
}
