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
package org.wso2.carbon.event.receiver.admin.internal.util.dto.converter;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.admin.exception.EventReceiverAdminServiceException;
import org.wso2.carbon.event.receiver.admin.internal.EventInputPropertyConfigurationDto;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverAdaptorPropertyDto;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverConfigurationDto;
import org.wso2.carbon.event.receiver.admin.internal.PropertyDto;
import org.wso2.carbon.event.receiver.admin.internal.util.DtoConverter;
import org.wso2.carbon.event.receiver.admin.internal.util.EventReceiverAdminConstants;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.internal.type.text.TextInputMapping;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;

import java.util.ArrayList;
import java.util.List;

public class TextDtoConverter extends DtoConverter {

    public EventReceiverConfiguration toEventReceiverConfiguration(String eventReceiverName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] textMappingAttributes,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 boolean mappingEnabled)
            throws EventReceiverAdminServiceException {
        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

        TextInputMapping textInputMapping = new TextInputMapping();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().addEventAdaptorProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        for (EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto : textMappingAttributes) {
            String attribTypeName = eventInputPropertyConfigurationDto.getType();
            AttributeType attributeType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventReceiverAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            // For text mapping, the regex comes as the from element key
            InputMappingAttribute textMappingAttribute = new InputMappingAttribute(eventInputPropertyConfigurationDto.getValueOf(), eventInputPropertyConfigurationDto.getName(), attributeType);
            textMappingAttribute.setDefaultValue(eventInputPropertyConfigurationDto.getDefaultValue());
            textInputMapping.addInputMappingAttribute(textMappingAttribute);
        }
        setCommonPropertiesToEventReceiverConfig(eventReceiverConfiguration, inputEventAdaptorConfiguration, eventReceiverName, streamNameWithVersion, eventAdaptorType, mappingEnabled, textInputMapping);

        return eventReceiverConfiguration;
    }

    @Override
    public EventReceiverConfigurationDto fromEventReceiverConfiguration(
            EventReceiverConfiguration eventReceiverConfiguration) {
        EventReceiverConfigurationDto eventReceiverConfigurationDto = new EventReceiverConfigurationDto();

        eventReceiverConfigurationDto.setEventReceiverConfigName(eventReceiverConfiguration.getEventReceiverName());
        eventReceiverConfigurationDto.setInputMappingType(eventReceiverConfiguration.getInputMapping().getMappingType());
        eventReceiverConfigurationDto.setInputEventAdaptorType(eventReceiverConfiguration.getInputEventAdaptorConfiguration().getInputEventAdaptorType());
        eventReceiverConfigurationDto.setToStreamName(eventReceiverConfiguration.getToStreamName());
        eventReceiverConfigurationDto.setToStreamVersion(eventReceiverConfiguration.getToStreamVersion());
        eventReceiverConfigurationDto.setTraceEnabled(eventReceiverConfiguration.isTraceEnabled());
        eventReceiverConfigurationDto.setStatisticsEnabled(eventReceiverConfiguration.isStatisticsEnabled());
        eventReceiverConfigurationDto.setCustomMappingEnabled(eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled());

        EventReceiverAdaptorPropertyDto[] eventReceiverAdaptorPropertyDtos = getEventReceiverMessageProperties(eventReceiverConfiguration);
        eventReceiverConfigurationDto.setEventReceiverMessageProperties(eventReceiverAdaptorPropertyDtos);

        EventInputPropertyConfigurationDto[] textMappingAttributeProperties = getTextMappingAttributeProperties(eventReceiverConfiguration);
        eventReceiverConfigurationDto.setPayloadEventReceiverProperties(textMappingAttributeProperties);

        return eventReceiverConfigurationDto;
    }

    private EventInputPropertyConfigurationDto[] getTextMappingAttributeProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        TextInputMapping textInputMapping = (TextInputMapping) eventReceiverConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : textInputMapping.getInputMappingAttributes()) {
            EventInputPropertyConfigurationDto eventReceiverMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
            eventInputPropertyConfigurationDtos.add(eventReceiverMessagePropertyDto);
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        String key = inputMappingAttribute.getToElementKey();
        String fromValue = inputMappingAttribute.getFromElementKey();
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(key);
        eventInputPropertyConfigurationDto.setValueOf(fromValue);
        eventInputPropertyConfigurationDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }
}
