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
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.json.JsonInputMapping;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;

import java.util.ArrayList;
import java.util.List;

public class JsonDtoConverter extends DtoConverter {

    public EventBuilderConfiguration toEventBuilderConfiguration(String eventBuilderName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorName,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] jsonPathExpressions,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 boolean mappingEnabled)
            throws EventBuilderAdminServiceException {
        EventBuilderConfiguration eventBuilderConfiguration = new EventBuilderConfiguration();

        JsonInputMapping jsonInputMapping = new JsonInputMapping();
        InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorMessageConfiguration.addInputMessageProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        for (EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto : jsonPathExpressions) {
            String attribTypeName = eventInputPropertyConfigurationDto.getType();
            AttributeType attributeType = EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventBuilderAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventBuilderConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            // For JSON we use toElementKey as the property key.
            InputMappingAttribute jsonMappingAttribute = new InputMappingAttribute(eventInputPropertyConfigurationDto.getValueOf(), eventInputPropertyConfigurationDto.getName(), attributeType);
            jsonMappingAttribute.setDefaultValue(eventInputPropertyConfigurationDto.getDefaultValue());
            jsonInputMapping.addInputMappingAttribute(jsonMappingAttribute);
        }
        setCommonPropertiesToEventBuilderConfig(eventBuilderConfiguration, inputEventAdaptorMessageConfiguration, eventBuilderName, streamNameWithVersion, eventAdaptorName, eventAdaptorType, mappingEnabled, jsonInputMapping);

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

        EventInputPropertyConfigurationDto[] jsonPathExpressions = getJsonPathExpressions(eventBuilderConfiguration);
        eventBuilderConfigurationDto.setPayloadEventBuilderProperties(jsonPathExpressions);

        return eventBuilderConfigurationDto;

    }

    private EventInputPropertyConfigurationDto[] getJsonPathExpressions(
            EventBuilderConfiguration eventBuilderConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        InputMapping jsonInputMapping = eventBuilderConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : jsonInputMapping.getInputMappingAttributes()) {
            EventInputPropertyConfigurationDto eventBuilderMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
            eventInputPropertyConfigurationDtos.add(eventBuilderMessagePropertyDto);
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        // For JSON, we use toElementKey as the property key
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(inputMappingAttribute.getToElementKey());
        eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getFromElementKey());
        eventInputPropertyConfigurationDto.setType(EventBuilderAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }

}
