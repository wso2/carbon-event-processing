/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.simulator.admin;


public class EventStreamInfoDto {

    private String streamName;
    private String streamVersion;
    private String streamDefinition;
    private String streamDescription;
    private EventStreamAttributeDto[] metaAttributes;
    private EventStreamAttributeDto[] correlationAttributes;
    private EventStreamAttributeDto[] payloadAttributes;

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

    public EventStreamInfoDto(String streamName, String streamVersion) {
        this.streamName = streamName;
        this.streamVersion = streamVersion;
    }

    public EventStreamInfoDto() {
    }

    public EventStreamAttributeDto[] getMetaAttributes() {
        return metaAttributes;
    }

    public void setMetaAttributes(EventStreamAttributeDto[] metaAttributes) {
        this.metaAttributes = metaAttributes;
    }

    public EventStreamAttributeDto[] getCorrelationAttributes() {
        return correlationAttributes;
    }

    public void setCorrelationAttributes(EventStreamAttributeDto[] correlationAttributes) {
        this.correlationAttributes = correlationAttributes;
    }

    public EventStreamAttributeDto[] getPayloadAttributes() {
        return payloadAttributes;
    }

    public void setPayloadAttributes(EventStreamAttributeDto[] payloadAttributes) {
        this.payloadAttributes = payloadAttributes;
    }
}
