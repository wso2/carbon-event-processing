package org.wso2.carbon.event.processor.core.internal.ha.server;

/**
 * Created by suho on 3/13/14.
 */
public class HAManagementServerConfiguration {
    private int dataReceiverPort;
    private String receiverHostName;

    public HAManagementServerConfiguration(int dataReceiverPort) {
        this.dataReceiverPort = dataReceiverPort;
    }

    public void setDataReceiverPort(int dataReceiverPort) {
        this.dataReceiverPort = dataReceiverPort;
    }

    public int getDataReceiverPort() {
        return dataReceiverPort;
    }

    public void setReceiverHostName(String receiverHostName) {
        this.receiverHostName = receiverHostName;
    }

    public String getReceiverHostName() {
        return receiverHostName;
    }
}
