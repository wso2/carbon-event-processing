/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.receiver.admin.internal.util;

import org.wso2.carbon.event.receiver.admin.exception.EventReceiverAdminServiceException;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverAdaptorPropertyDto;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverConfigurationDto;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverConfigurationInfoDto;
import org.wso2.carbon.event.receiver.admin.internal.ds.EventReceiverAdminServiceValueHolder;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorDto;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.Property;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMapping;

import java.util.List;
import java.util.Map;

public abstract class DtoConverter {
    /**
     * Returns {@link InputEventAdaptorDto} properties of the input event adaptor
     * of the passed in event receiver as an array of {@link org.wso2.carbon.event.receiver.admin.internal.EventReceiverAdaptorPropertyDto}
     *
     * @param inputEventAdaptorDto       the message dto to be used for extracting the properties needed
     * @param eventReceiverConfiguration the event receiver configuration which will be used to extract information about
     *                                   the InputEventAdaptorConfiguration
     * @return an array of {@link org.wso2.carbon.event.receiver.admin.internal.EventReceiverAdaptorPropertyDto}
     */
    public EventReceiverAdaptorPropertyDto[] getEventReceiverPropertiesFrom(InputEventAdaptorDto inputEventAdaptorDto,
                                                                            EventReceiverConfiguration eventReceiverConfiguration) {
        List<Property> messageDtoPropertyList = inputEventAdaptorDto.getAdaptorPropertyList();
        if (messageDtoPropertyList != null) {
            EventReceiverAdaptorPropertyDto[] eventReceiverAdaptorPropertyDtos = new EventReceiverAdaptorPropertyDto[messageDtoPropertyList.size()];
            int i = 0;
            if (eventReceiverConfiguration != null) {
                Map<String, String> propertyValueMap = eventReceiverConfiguration.getInputEventAdaptorConfiguration().getInternalInputEventAdaptorConfiguration().getProperties();
                for (Property property : messageDtoPropertyList) {
                    String value = propertyValueMap.get(property.getPropertyName());
                    EventReceiverAdaptorPropertyDto eventReceiverAdaptorPropertyDto = getEventReceiverPropertyFrom(property, value);
                    eventReceiverAdaptorPropertyDtos[i++] = eventReceiverAdaptorPropertyDto;
                }
            } else {
                for (Property property : messageDtoPropertyList) {
                    EventReceiverAdaptorPropertyDto eventReceiverAdaptorPropertyDto = getEventReceiverPropertyFrom(property, null);
                    eventReceiverAdaptorPropertyDtos[i++] = eventReceiverAdaptorPropertyDto;
                }
            }
            return eventReceiverAdaptorPropertyDtos;
        } else {
            return new EventReceiverAdaptorPropertyDto[0];
        }

    }

    /**
     * Returns an {@link org.wso2.carbon.event.receiver.admin.internal.EventReceiverConfigurationDto} for the passed in {@link EventReceiverConfiguration}
     *
     * @param eventReceiverConfiguration the event builder configuration that needs to be converted to a DTO
     * @return an {@link org.wso2.carbon.event.receiver.admin.internal.EventReceiverConfigurationDto} instance that matches the passed in EventReceiverConfiguration
     * @throws org.wso2.carbon.event.receiver.admin.exception.EventReceiverAdminServiceException
     *
     */
    public abstract EventReceiverConfigurationDto fromEventReceiverConfiguration(
            EventReceiverConfiguration eventReceiverConfiguration)
            throws EventReceiverAdminServiceException;

    public EventReceiverConfigurationInfoDto getEventReceiverConfigurationInfoDto(
            EventReceiverConfiguration eventReceiverConfiguration)
            throws EventReceiverAdminServiceException {
        EventReceiverConfigurationInfoDto eventReceiverConfigurationDto = new EventReceiverConfigurationInfoDto();

        eventReceiverConfigurationDto.setEventReceiverName(eventReceiverConfiguration.getEventReceiverName());
        eventReceiverConfigurationDto.setInputMappingType(eventReceiverConfiguration.getInputMapping().getMappingType());
        eventReceiverConfigurationDto.setInputEventAdaptorType(eventReceiverConfiguration.getInputEventAdaptorConfiguration().getInputEventAdaptorType());
        eventReceiverConfigurationDto.setToStreamId(eventReceiverConfiguration.getToStreamName() +
                EventReceiverAdminConstants.STREAM_NAME_VER_DELIMITER + eventReceiverConfiguration.getToStreamVersion());
        eventReceiverConfigurationDto.setEnableTracing(eventReceiverConfiguration.isTraceEnabled());
        eventReceiverConfigurationDto.setEnableStats(eventReceiverConfiguration.isStatisticsEnabled());

        return eventReceiverConfigurationDto;
    }

    private EventReceiverAdaptorPropertyDto getEventReceiverPropertyFrom(Property msgDtoProperty,
                                                                         String value) {
        String key = msgDtoProperty.getPropertyName();
        EventReceiverAdaptorPropertyDto eventReceiverAdaptorPropertyDto = new EventReceiverAdaptorPropertyDto();
        eventReceiverAdaptorPropertyDto.setKey(key);
        eventReceiverAdaptorPropertyDto.setDefaultValue(msgDtoProperty.getDefaultValue());
        eventReceiverAdaptorPropertyDto.setDisplayName(msgDtoProperty.getDisplayName());
        eventReceiverAdaptorPropertyDto.setHint(msgDtoProperty.getHint());
        eventReceiverAdaptorPropertyDto.setRequired(msgDtoProperty.isRequired());
        eventReceiverAdaptorPropertyDto.setSecured(msgDtoProperty.isSecured());
        if (value != null) {
            eventReceiverAdaptorPropertyDto.setValue(value);
        } else {
            eventReceiverAdaptorPropertyDto.setValue(msgDtoProperty.getDefaultValue());
        }
        eventReceiverAdaptorPropertyDto.setOptions(msgDtoProperty.getOptions());

        return eventReceiverAdaptorPropertyDto;
    }

    protected void setCommonPropertiesToEventReceiverConfig(
            EventReceiverConfiguration eventReceiverConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            String eventReceiverName, String streamNameWithVersion,
            String eventAdaptorType, boolean mappingEnabled, InputMapping inputMapping) {
        inputMapping.setCustomMappingEnabled(mappingEnabled);
        eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
        eventReceiverConfiguration.setInputMapping(inputMapping);

        String[] toStreamProperties = streamNameWithVersion.split(":");
        eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
        eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);
        inputEventAdaptorConfiguration.setInputEventAdaptorType(eventReceiverName);
        inputEventAdaptorConfiguration.setInputEventAdaptorType(eventAdaptorType);
        eventReceiverConfiguration.setInputEventAdaptorConfiguration(inputEventAdaptorConfiguration);
    }

    protected EventReceiverAdaptorPropertyDto[] getEventReceiverMessageProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        String inputEventAdaptorType = eventReceiverConfiguration.getInputEventAdaptorConfiguration().getInputEventAdaptorType();
        InputEventAdaptorService inputEventAdaptorService = EventReceiverAdminServiceValueHolder.getInputEventAdaptorService();
        if (inputEventAdaptorType != null) {
            InputEventAdaptorDto inputEventAdaptorDto = inputEventAdaptorService.getEventAdaptorDto(inputEventAdaptorType);
            return getEventReceiverPropertiesFrom(inputEventAdaptorDto, eventReceiverConfiguration);
        }

        return new EventReceiverAdaptorPropertyDto[0];
    }
}
