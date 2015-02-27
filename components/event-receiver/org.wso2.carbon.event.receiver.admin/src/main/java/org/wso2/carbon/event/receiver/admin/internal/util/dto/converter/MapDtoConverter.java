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
import org.wso2.carbon.event.receiver.core.config.mapping.MapInputMapping;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;

import java.util.ArrayList;
import java.util.List;

public class MapDtoConverter extends DtoConverter {

    public EventReceiverConfiguration toEventReceiverConfiguration(String eventReceiverName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] mapMappings,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 boolean mappingEnabled)
            throws EventReceiverAdminServiceException {
        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

        MapInputMapping mapInputMapping = new MapInputMapping();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().addEventAdaptorProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        int mappingPropertyPos = 0;
        for (EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto : mapMappings) {
            String attribTypeName = eventInputPropertyConfigurationDto.getType();
            AttributeType attributeType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventReceiverAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute mapMappingAttribute = new InputMappingAttribute(eventInputPropertyConfigurationDto.getName(), eventInputPropertyConfigurationDto.getValueOf(), attributeType, EventReceiverConstants.PAYLOAD_DATA_VAL);
            mapMappingAttribute.setDefaultValue(eventInputPropertyConfigurationDto.getDefaultValue());
            mapMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            mapInputMapping.addInputMappingAttribute(mapMappingAttribute);
        }
        setCommonPropertiesToEventReceiverConfig(eventReceiverConfiguration, inputEventAdaptorConfiguration, eventReceiverName, streamNameWithVersion, eventAdaptorType, mappingEnabled, mapInputMapping);

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

        EventInputPropertyConfigurationDto[] eventReceiverProperties = getMappingProperties(eventReceiverConfiguration);
        eventReceiverConfigurationDto.setPayloadEventReceiverProperties(eventReceiverProperties);

        return eventReceiverConfigurationDto;
    }

    private EventInputPropertyConfigurationDto[] getMappingProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        MapInputMapping wso2EventInputMapping = (MapInputMapping) eventReceiverConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            EventInputPropertyConfigurationDto eventReceiverMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
            eventInputPropertyConfigurationDtos.add(eventReceiverMessagePropertyDto);
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        String key = inputMappingAttribute.getFromElementKey();
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(key);
        eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getToElementKey());
        eventInputPropertyConfigurationDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }

}
