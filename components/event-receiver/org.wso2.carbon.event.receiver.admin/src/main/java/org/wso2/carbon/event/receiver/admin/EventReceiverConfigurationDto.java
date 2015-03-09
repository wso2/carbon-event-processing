/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.receiver.admin;


public class EventReceiverConfigurationDto {

    private String eventReceiverName;

    private String fromStreamNameWithVersion;

    private InputAdapterConfigurationDto toAdapterConfigurationDto;

    private String messageFormat;

    private EventMappingPropertyDto[] mappingPropertyDtos;

    public String getEventReceiverName() {
        return eventReceiverName;
    }

    public void setEventReceiverName(String eventReceiverName) {
        this.eventReceiverName = eventReceiverName;
    }

    public String getFromStreamNameWithVersion() {
        return fromStreamNameWithVersion;
    }

    public void setFromStreamNameWithVersion(String fromStreamNameWithVersion) {
        this.fromStreamNameWithVersion = fromStreamNameWithVersion;
    }

    public InputAdapterConfigurationDto getToAdapterConfigurationDto() {
        return toAdapterConfigurationDto;
    }

    public void setToAdapterConfigurationDto(
            InputAdapterConfigurationDto toAdapterConfigurationDto) {
        this.toAdapterConfigurationDto = toAdapterConfigurationDto;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public EventMappingPropertyDto[] getMappingPropertyDtos() {
        return mappingPropertyDtos;
    }

    public void setMappingPropertyDtos(EventMappingPropertyDto[] mappingPropertyDtos) {
        this.mappingPropertyDtos = mappingPropertyDtos;
    }
}
