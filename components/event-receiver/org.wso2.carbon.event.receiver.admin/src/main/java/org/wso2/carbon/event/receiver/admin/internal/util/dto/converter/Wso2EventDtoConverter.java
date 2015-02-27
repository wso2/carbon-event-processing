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
import org.wso2.carbon.event.receiver.core.internal.type.AbstractInputMapping;
import org.wso2.carbon.event.receiver.core.internal.type.wso2event.Wso2EventInputMapping;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;

import java.util.ArrayList;
import java.util.List;

public class Wso2EventDtoConverter extends DtoConverter {

    public EventReceiverConfiguration toEventReceiverConfiguration(String eventReceiverName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] metaData,
                                                                 EventInputPropertyConfigurationDto[] correlationData,
                                                                 EventInputPropertyConfigurationDto[] payloadData,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 boolean mappingEnabled)
            throws EventReceiverAdminServiceException {
        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

        Wso2EventInputMapping wso2EventInputMapping = new Wso2EventInputMapping();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();

        populateMappingAttributesFromDto(metaData, correlationData, payloadData, inputPropertyConfiguration, wso2EventInputMapping, inputEventAdaptorConfiguration);
        setCommonPropertiesToEventReceiverConfig(eventReceiverConfiguration, inputEventAdaptorConfiguration, eventReceiverName, streamNameWithVersion, eventAdaptorType, mappingEnabled, wso2EventInputMapping);

        return eventReceiverConfiguration;
    }

    private void populateMappingAttributesFromDto(EventInputPropertyConfigurationDto[] metaData, EventInputPropertyConfigurationDto[] correlationData, EventInputPropertyConfigurationDto[] payloadData, PropertyDto[] inputPropertyConfiguration, AbstractInputMapping inputMapping, InputEventAdaptorConfiguration inputEventAdaptorConfiguration) throws EventReceiverAdminServiceException {
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().addEventAdaptorProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        int mappingPropertyPos = 0;
        for (EventInputPropertyConfigurationDto metaAttribute : metaData) {
            String attribTypeName = metaAttribute.getType();
            AttributeType attributeType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventReceiverAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute metaInputMappingAttribute = new InputMappingAttribute(metaAttribute.getName(), metaAttribute.getValueOf(), attributeType, EventReceiverConstants.META_DATA_VAL);
            metaInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            inputMapping.addInputMappingAttribute(metaInputMappingAttribute);
        }
        for (EventInputPropertyConfigurationDto correlationAttribute : correlationData) {
            String attribTypeName = correlationAttribute.getType();
            AttributeType attributeType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventReceiverAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute correlationInputMappingAttribute = new InputMappingAttribute(correlationAttribute.getName(), correlationAttribute.getValueOf(), attributeType, EventReceiverConstants.CORRELATION_DATA_VAL);
            correlationInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            inputMapping.addInputMappingAttribute(correlationInputMappingAttribute);
        }
        for (EventInputPropertyConfigurationDto payloadAttribute : payloadData) {
            String attribTypeName = payloadAttribute.getType();
            AttributeType attributeType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventReceiverAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute payloadInputMappingAttribute = new InputMappingAttribute(payloadAttribute.getName(), payloadAttribute.getValueOf(), attributeType, EventReceiverConstants.PAYLOAD_DATA_VAL);
            payloadInputMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            inputMapping.addInputMappingAttribute(payloadInputMappingAttribute);
        }
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

        EventInputPropertyConfigurationDto[] metaEventReceiverProperties = getMetaEventReceiverProperties(eventReceiverConfiguration);
        EventInputPropertyConfigurationDto[] correlationEventReceiverProperties = getCorrelationEventReceiverProperties(eventReceiverConfiguration);
        EventInputPropertyConfigurationDto[] payloadEventReceiverProperties = getPayloadEventReceiverProperties(eventReceiverConfiguration);
        eventReceiverConfigurationDto.setMetaEventReceiverProperties(metaEventReceiverProperties);
        eventReceiverConfigurationDto.setCorrelationEventReceiverProperties(correlationEventReceiverProperties);
        eventReceiverConfigurationDto.setPayloadEventReceiverProperties(payloadEventReceiverProperties);

        return eventReceiverConfigurationDto;
    }

    private EventInputPropertyConfigurationDto[] getMetaEventReceiverProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventReceiverConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getFromElementType().equals(EventReceiverConstants.META_DATA_VAL)) {
                EventInputPropertyConfigurationDto eventReceiverMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
                eventInputPropertyConfigurationDtos.add(eventReceiverMessagePropertyDto);
            }
        }

/*
        EventReceiverAdaptorPropertyDto customMappingValuePropertyDto = new EventReceiverAdaptorPropertyDto();
        customMappingValuePropertyDto.setKey(EventReceiverAdminConstants.SPECIFIC_ATTR_PREFIX + EventReceiverAdminConstants.CUSTOM_MAPPING_VALUE_KEY + EventReceiverAdminConstants.MAPPING_SUFFIX);
        customMappingValuePropertyDto.setValue(wso2EventInputMapping.isCustomMappingEnabled() ? EventReceiverAdminConstants.ENABLE_CONST : EventReceiverAdminConstants.DISABLE_CONST);
        eventReceiverMessagePropertyDtoList.add(customMappingValuePropertyDto);
*/

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto[] getCorrelationEventReceiverProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventReceiverConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getFromElementType().equals(EventReceiverConstants.CORRELATION_DATA_VAL)) {
                EventInputPropertyConfigurationDto eventReceiverMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
                eventInputPropertyConfigurationDtos.add(eventReceiverMessagePropertyDto);
            }
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto[] getPayloadEventReceiverProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventReceiverConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            if (inputMappingAttribute.getFromElementType().equals(EventReceiverConstants.PAYLOAD_DATA_VAL)) {
                EventInputPropertyConfigurationDto eventReceiverMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
                eventInputPropertyConfigurationDtos.add(eventReceiverMessagePropertyDto);
            }
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(inputMappingAttribute.getFromElementKey());
        eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getToElementKey());
        eventInputPropertyConfigurationDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }
}
