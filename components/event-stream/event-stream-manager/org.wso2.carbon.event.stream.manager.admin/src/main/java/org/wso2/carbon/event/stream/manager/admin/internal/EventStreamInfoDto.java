package org.wso2.carbon.event.stream.manager.admin.internal;

/**
 * This class contains properties of inputs and outputs
 */
public class EventStreamInfoDto {

    private String streamName;
    private String streamVersion;
    private String streamDefinition;
    private String streamDescription;

    public EventStreamInfoDto(String streamName, String streamVersion) {
        this.streamName = streamName;
        this.streamVersion = streamVersion;
    }

    public EventStreamInfoDto() {
    }

    public String getStreamDescription() {
        return streamDescription;
    }

    public void setStreamDescription(String streamDescription) {
        this.streamDescription = streamDescription;
    }

    public String getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(String streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }
}
