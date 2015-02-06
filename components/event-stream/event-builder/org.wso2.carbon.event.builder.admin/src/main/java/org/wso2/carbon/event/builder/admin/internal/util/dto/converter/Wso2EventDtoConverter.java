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
package org.wso2.carbon.event.builder.admin.internal.util.dto.converter;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderMessagePropertyDto;
import org.wso2.carbon.event.builder.admin.internal.EventInputPropertyConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.PropertyDto;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConverter;
import org.wso2.carbon.event.builder.admin.internal.util.EventBuilderAdminConstants;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.AbstractInputMapping;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventInputMapping;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Wso2EventDtoConverter extends DtoConverter {

    public EventBuilderConfiguration toEventBuilderConfiguration(String eventBuilderName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorName,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] metaData,
                                                                 EventInputPropertyConfigurationDto[] correlationData,
                                                                 EventInputPropertyConfigurationDto[] payloadData,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 boolean mappingEnabled)
            throws EventBuilderAdminServiceException {
        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration();

        Wso2EventInputMapping wso2EventInputMapping = new Wso2EventInputMapping();
        InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();

        populateMappingAttributesFromDto(metaData, correlationData, payloadData, inputPropertyConfiguration, wso2EventInputMapping, inputEventAdaptorMessageConfiguration);
        setCommonPropertiesToEventBuilderConfig(eventBuilderConfiguration, inputEventAdaptorMessageConfiguration, eventBuilderName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, mappingEnabled, wso2EventInputMapping);

        return eventBuilderConfiguration;
    }

    private void populateMappingAttributesFromDto(EventInputPropertyConfigurationDto[] metaData, EventInputPropertyConfigurationDto[] correlationData, EventInputPropertyConfigurationDto[] payloadData, PropertyDto[] inputPropertyConfiguration, AbstractInputMapping inputMapping, InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration) throws EventBuilderAdminServiceException {
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorMessageConfiguration.addInputMessageProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        int mappingPropertyPos = 0;
        for (EventInputPropertyConfigurationDto metaAttribute : metaData) {
            String attribTypeName = metaAttribute.getType();
            AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventBuilderAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute metaInputMappingAttribute = new InputMappingAttribute(metaAttribute.getName(), metaAttribute.getValueOf(), attributeType, EventBuilderConstants.META_DATA_VAL);
            metaInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            inputMapping.addInputMappingAttribute(metaInputMappingAttribute);
        }
        for (EventInputPropertyConfigurationDto correlationAttribute : correlationData) {
            String attribTypeName = correlationAttribute.getType();
            AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventBuilderAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute correlationInputMappingAttribute = new InputMappingAttribute(correlationAttribute.getName(), correlationAttribute.getValueOf(), attributeType, EventBuilderConstants.CORRELATION_DATA_VAL);
            correlationInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            inputMapping.addInputMappingAttribute(correlationInputMappingAttribute);
        }
        for (EventInputPropertyConfigurationDto payloadAttribute : payloadData) {
            String attribTypeName = payloadAttribute.getType();
            AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventBuilderAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute payloadInputMappingAttribute = new InputMappingAttribute(payloadAttribute.getName(), payloadAttribute.getValueOf(), attributeType, EventBuilderConstants.PAYLOAD_DATA_VAL);
            payloadInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            inputMapping.addInputMappingAttribute(payloadInputMappingAttribute);
        }
    }

    @Override
    public EventBuilderConfigurationDto fromEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration) {
        EventBuilderConfigurationDto eventBuilderConfigurationDto = new EventBuilderConfigurationDto();

        eventBuilderConfigurationDto.setEventBuilderConfigName(eventBuilderConfiguration.getEventBuilderName());
        eventBuilderConfigurationDto.setInputMappingType(eventBuilderConfiguration.getInputMapping().getMappingType());
        eventBuilderConfigurationDto.setInputEventAdaptorName(eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName());
        eventBuilderConfigurationDto.setInputEventAdaptorType(eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorType());
        eventBuilderConfigurationDto.setToStreamName(eventBuilderConfiguration.getToStreamName());
        eventBuilderConfigurationDto.setToStreamVersion(eventBuilderConfiguration.getToStreamVersion());
        eventBuilderConfigurationDto.setTraceEnabled(eventBuilderConfiguration.isTraceEnabled());
        eventBuilderConfigurationDto.setStatisticsEnabled(eventBuilderConfiguration.isStatisticsEnabled());
        eventBuilderConfigurationDto.setCustomMappingEnabled(eventBuilderConfiguration.getInputMapping().isCustomMappingEnabled());
        eventBuilderConfigurationDto.setEditable(eventBuilderConfiguration.isEditable());

        EventBuilderMessagePropertyDto[] eventBuilderMessagePropertyDtos = getEventBuilderMessageProperties(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setEventBuilderMessageProperties(eventBuilderMessagePropertyDtos);

        EventInputPropertyConfigurationDto[] metaEventBuilderProperties = getMetaEventBuilderProperties(eventBuilderConfiguration);
        EventInputPropertyConfigurationDto[] correlationEventBuilderProperties = getCorrelationEventBuilderProperties(eventBuilderConfiguration);
        EventInputPropertyConfigurationDto[] payloadEventBuilderProperties = getPayloadEventBuilderProperties(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setMetaEventBuilderProperties(metaEventBuilderProperties);
        eventBuilderConfigurationDto.setCorrelationEventBuilderProperties(correlationEventBuilderProperties);
        eventBuilderConfigurationDto.setPayloadEventBuilderProperties(payloadEventBuilderProperties);

        return eventBuilderConfigurationDto;
    }

    private EventInputPropertyConfigurationDto[] getMetaEventBuilderProperties(
            EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getFromElementType().equals(EventBuilderConstants.META_DATA_VAL)) {
                EventInputPropertyConfigurationDto eventBuilderMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
                eventInputPropertyConfigurationDtos.add(eventBuilderMessagePropertyDto);
            }
        }

/*
        EventBuilderMessagePropertyDto customMappingValuePropertyDto = new EventBuilderMessagePropertyDto();
        customMappingValuePropertyDto.setKey(EventBuilderAdminConstants.SPECIFIC_ATTR_PREFIX + EventBuilderAdminConstants.CUSTOM_MAPPING_VALUE_KEY + EventBuilderAdminConstants.MAPPING_SUFFIX);
        customMappingValuePropertyDto.setValue(wso2EventInputMapping.isCustomMappingEnabled() ? EventBuilderAdminConstants.ENABLE_CONST : EventBuilderAdminConstants.DISABLE_CONST);
        eventBuilderMessagePropertyDtoList.add(customMappingValuePropertyDto);
*/

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto[] getCorrelationEventBuilderProperties(
            EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getFromElementType().equals(EventBuilderConstants.CORRELATION_DATA_VAL)) {
                EventInputPropertyConfigurationDto eventBuilderMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
                eventInputPropertyConfigurationDtos.add(eventBuilderMessagePropertyDto);
            }
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto[] getPayloadEventBuilderProperties(
            EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getFromElementType().equals(EventBuilderConstants.PAYLOAD_DATA_VAL)) {
                EventInputPropertyConfigurationDto eventBuilderMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
                eventInputPropertyConfigurationDtos.add(eventBuilderMessagePropertyDto);
            }
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(inputMappingAttribute.getFromElementKey());
        eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getToElementKey());
        eventInputPropertyConfigurationDto.setType(EventBuilderAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }
}
