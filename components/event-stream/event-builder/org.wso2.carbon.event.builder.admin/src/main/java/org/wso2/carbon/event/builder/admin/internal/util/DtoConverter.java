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
package org.wso2.carbon.event.builder.admin.internal.util;

import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationInfoDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderMessagePropertyDto;
import org.wso2.carbon.event.builder.admin.internal.ds.EventBuilderAdminServiceValueHolder;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.config.InputStreamConfiguration;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.List;
import java.util.Map;

public abstract class DtoConverter {
    /**
     * Returns {@link MessageDto} properties of the input event adaptor
     * of the passed in event builder as an array of {@link org.wso2.carbon.event.builder.admin.internal.EventBuilderMessagePropertyDto}
     *
     * @param messageDto                the message dto to be used for extracting the properties needed
     * @param eventBuilderConfiguration the event builder configuration which will be used to extract information about
     *                                  the InputEventAdaptorConfiguration
     * @return an array of {@link org.wso2.carbon.event.builder.admin.internal.EventBuilderMessagePropertyDto}
     */
    public EventBuilderMessagePropertyDto[] getEventBuilderPropertiesFrom(MessageDto messageDto,
                                                                          EventBuilderConfiguration eventBuilderConfiguration) {
        List<Property> messageDtoPropertyList = messageDto.getMessageInPropertyList();
        if (messageDtoPropertyList != null) {
            EventBuilderMessagePropertyDto[] eventBuilderMessagePropertyDtos = new EventBuilderMessagePropertyDto[messageDtoPropertyList.size()];
            int i = 0;
            if (eventBuilderConfiguration != null) {
                Map<String, String> propertyValueMap = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorMessageConfiguration().getInputMessageProperties();
                for (Property property : messageDtoPropertyList) {
                    String value = propertyValueMap.get(property.getPropertyName());
                    EventBuilderMessagePropertyDto eventBuilderMessagePropertyDto = getEventBuilderPropertyFrom(property, value);
                    eventBuilderMessagePropertyDtos[i++] = eventBuilderMessagePropertyDto;
                }
            } else {
                for (Property property : messageDtoPropertyList) {
                    EventBuilderMessagePropertyDto eventBuilderMessagePropertyDto = getEventBuilderPropertyFrom(property, null);
                    eventBuilderMessagePropertyDtos[i++] = eventBuilderMessagePropertyDto;
                }
            }
            return eventBuilderMessagePropertyDtos;
        }else {
            return new EventBuilderMessagePropertyDto[0];
        }

    }

    /**
     * Returns an {@link EventBuilderConfigurationDto} for the passed in {@link EventBuilderConfiguration}
     *
     * @param eventBuilderConfiguration the event builder configuration that needs to be converted to a DTO
     * @return an {@link EventBuilderConfigurationDto} instance that matches the passed in EventBuilderConfiguration
     * @throws EventBuilderAdminServiceException
     *
     */
    public abstract EventBuilderConfigurationDto fromEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderAdminServiceException;

    public EventBuilderConfigurationInfoDto getEventBuilderConfigurationInfoDto(
            EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderAdminServiceException {
        EventBuilderConfigurationInfoDto eventBuilderConfigurationDto = new EventBuilderConfigurationInfoDto();

        eventBuilderConfigurationDto.setEventBuilderName(eventBuilderConfiguration.getEventBuilderName());
        eventBuilderConfigurationDto.setInputMappingType(eventBuilderConfiguration.getInputMapping().getMappingType());
        eventBuilderConfigurationDto.setInputEventAdaptorName(eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName());
        eventBuilderConfigurationDto.setInputEventAdaptorType(eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorType());
        eventBuilderConfigurationDto.setToStreamId(eventBuilderConfiguration.getToStreamName() +
                                                   EventBuilderAdminConstants.STREAM_NAME_VER_DELIMITER + eventBuilderConfiguration.getToStreamVersion());
        eventBuilderConfigurationDto.setEnableTracing(eventBuilderConfiguration.isTraceEnabled());
        eventBuilderConfigurationDto.setEnableStats(eventBuilderConfiguration.isStatisticsEnabled());
        eventBuilderConfigurationDto.setEditable(eventBuilderConfiguration.isEditable());

        return eventBuilderConfigurationDto;
    }

    private EventBuilderMessagePropertyDto getEventBuilderPropertyFrom(Property msgDtoProperty,
                                                                       String value) {
        String key = msgDtoProperty.getPropertyName();
        EventBuilderMessagePropertyDto eventBuilderMessagePropertyDto = new EventBuilderMessagePropertyDto();
        eventBuilderMessagePropertyDto.setKey(key);
        eventBuilderMessagePropertyDto.setDefaultValue(msgDtoProperty.getDefaultValue());
        eventBuilderMessagePropertyDto.setDisplayName(msgDtoProperty.getDisplayName());
        eventBuilderMessagePropertyDto.setHint(msgDtoProperty.getHint());
        eventBuilderMessagePropertyDto.setRequired(msgDtoProperty.isRequired());
        eventBuilderMessagePropertyDto.setSecured(msgDtoProperty.isSecured());
        if (value != null) {
            eventBuilderMessagePropertyDto.setValue(value);
        } else {
            eventBuilderMessagePropertyDto.setValue(msgDtoProperty.getDefaultValue());
        }
        eventBuilderMessagePropertyDto.setOptions(msgDtoProperty.getOptions());

        return eventBuilderMessagePropertyDto;
    }

    protected void setCommonPropertiesToEventBuilderConfig(
            EventBuilderConfiguration eventBuilderConfiguration,
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            String eventBuilderName, String streamNameWithVersion, String eventAdaptorName,
            String eventAdaptorType, boolean mappingEnabled, InputMapping inputMapping) {
        inputMapping.setCustomMappingEnabled(mappingEnabled);
        eventBuilderConfiguration.setEventBuilderName(eventBuilderName);
        eventBuilderConfiguration.setInputMapping(inputMapping);

        String[] toStreamProperties = streamNameWithVersion.split(":");
        eventBuilderConfiguration.setToStreamName(toStreamProperties[0]);
        eventBuilderConfiguration.setToStreamVersion(toStreamProperties[1]);
        InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
        inputStreamConfiguration.setInputEventAdaptorMessageConfiguration(inputEventAdaptorMessageConfiguration);
        inputStreamConfiguration.setInputEventAdaptorName(eventAdaptorName);
        inputStreamConfiguration.setInputEventAdaptorType(eventAdaptorType);
        eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);
    }

    protected EventBuilderMessagePropertyDto[] getEventBuilderMessageProperties(
            EventBuilderConfiguration eventBuilderConfiguration) {
        String inputEventAdaptorType = eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorType();
        InputEventAdaptorService inputEventAdaptorService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorService();
        if (inputEventAdaptorType != null) {
            MessageDto messageDto = inputEventAdaptorService.getEventMessageDto(inputEventAdaptorType);
            return getEventBuilderPropertiesFrom(messageDto, eventBuilderConfiguration);
        }

        return new EventBuilderMessagePropertyDto[0];
    }
}
