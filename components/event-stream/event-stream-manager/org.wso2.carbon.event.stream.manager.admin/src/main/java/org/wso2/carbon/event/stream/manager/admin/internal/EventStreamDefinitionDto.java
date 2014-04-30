package org.wso2.carbon.event.stream.manager.admin.internal;

public class EventStreamDefinitionDto {

    private String name;
    private String version;
    private String description;
    private String nickName;
    private EventStreamAttributeDto[] metaAttributes;
    private EventStreamAttributeDto[] correlationAttributes;
    private EventStreamAttributeDto[] payloadAttributes;

    public EventStreamAttributeDto[] getMetaData() {
        return metaAttributes;
    }

    public void setMetaData(EventStreamAttributeDto[] metaData) {
        this.metaAttributes = metaData;
    }

    public EventStreamAttributeDto[] getCorrelationData() {
        return correlationAttributes;
    }

    public void setCorrelationData(EventStreamAttributeDto[] correlationData) {
        this.correlationAttributes = correlationData;
    }

    public EventStreamAttributeDto[] getPayloadData() {
        return payloadAttributes;
    }

    public void setPayloadData(EventStreamAttributeDto[] payloadData) {
        this.payloadAttributes = payloadData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
