package org.wso2.carbon.event.processor.core.internal.ha;

import java.io.Serializable;

public class CEPMembership implements Serializable {

    String host;
    int port;

    public CEPMembership(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
