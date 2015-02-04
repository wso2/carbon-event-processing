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
import org.wso2.carbon.event.builder.core.internal.type.map.MapInputMapping;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MapDtoConverter extends DtoConverter {

    public EventBuilderConfiguration toEventBuilderConfiguration(String eventBuilderName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorName,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] mapMappings,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 boolean mappingEnabled)
            throws EventBuilderAdminServiceException {
        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration();

        MapInputMapping mapInputMapping = new MapInputMapping();
        InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorMessageConfiguration.addInputMessageProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        int mappingPropertyPos = 0;
        for (EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto : mapMappings) {
            String attribTypeName = eventInputPropertyConfigurationDto.getType();
            AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventBuilderAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            InputMappingAttribute mapMappingAttribute = new InputMappingAttribute(eventInputPropertyConfigurationDto.getName(), eventInputPropertyConfigurationDto.getValueOf(), attributeType, EventBuilderConstants.PAYLOAD_DATA_VAL);
            mapMappingAttribute.setDefaultValue(eventInputPropertyConfigurationDto.getDefaultValue());
            mapMappingAttribute.setToStreamPosition(mappingPropertyPos++);
            mapInputMapping.addInputMappingAttribute(mapMappingAttribute);
        }
        setCommonPropertiesToEventBuilderConfig(eventBuilderConfiguration, inputEventAdaptorMessageConfiguration, eventBuilderName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, mappingEnabled, mapInputMapping);

        return eventBuilderConfiguration;
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

        EventBuilderMessagePropertyDto[] eventBuilderMessagePropertyDtos = getEventBuilderMessageProperties(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setEventBuilderMessageProperties(eventBuilderMessagePropertyDtos);

        EventInputPropertyConfigurationDto[] eventBuilderProperties = getMappingProperties(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setPayloadEventBuilderProperties(eventBuilderProperties);

        return eventBuilderConfigurationDto;
    }

    private EventInputPropertyConfigurationDto[] getMappingProperties(
            EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        MapInputMapping wso2EventInputMapping = (MapInputMapping) eventBuilderConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
            EventInputPropertyConfigurationDto eventBuilderMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
            eventInputPropertyConfigurationDtos.add(eventBuilderMessagePropertyDto);
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        String key = inputMappingAttribute.getFromElementKey();
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(key);
        eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getToElementKey());
        eventInputPropertyConfigurationDto.setType(EventBuilderAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }

}
