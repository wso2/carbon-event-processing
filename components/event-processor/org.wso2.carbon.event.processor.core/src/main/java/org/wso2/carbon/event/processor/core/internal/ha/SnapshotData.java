package org.wso2.carbon.event.processor.core.internal.ha;

public class SnapshotData {

    private byte[] states;
    private byte[] nextEventData;

    public SnapshotData() {
    }

    public SnapshotData(byte[] nextEventData, byte[] states) {

        this.nextEventData = nextEventData;
        this.states = states;
    }

    public byte[] getStates() {
        return states;
    }

    public byte[] getNextEventData() {
        return nextEventData;
    }

    public void setStates(byte[] states) {
        this.states = states;
    }

    public void setNextEventData(byte[] nextEventData) {
        this.nextEventData = nextEventData;
    }
}
