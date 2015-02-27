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
import org.wso2.carbon.event.receiver.core.internal.type.xml.XMLInputMapping;
import org.wso2.carbon.event.receiver.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;

import java.util.ArrayList;
import java.util.List;

public class XmlDtoConverter extends DtoConverter {

    public EventReceiverConfiguration toEventReceiverConfiguration(String eventReceiverName,
                                                                 String streamNameWithVersion,
                                                                 String eventAdaptorType,
                                                                 EventInputPropertyConfigurationDto[] xpathExpressions,
                                                                 PropertyDto[] inputPropertyConfiguration,
                                                                 PropertyDto[] xpathDefinitions,
                                                                 String parentSelectorXpath,
                                                                 boolean mappingEnabled)
            throws EventReceiverAdminServiceException {
        EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

        XMLInputMapping xmlInputMapping = new XMLInputMapping();
        xmlInputMapping.setParentSelectorXpath(parentSelectorXpath);
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        List<XPathDefinition> xPathDefinitionList = new ArrayList<XPathDefinition>();
        for (PropertyDto propertyDto : inputPropertyConfiguration) {
            inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().addEventAdaptorProperty(propertyDto.getKey(), propertyDto.getValue());
        }
        for (PropertyDto propertyDto : xpathDefinitions) {
            // We send as key = prefix, value = namespace uri
            xPathDefinitionList.add(new XPathDefinition(propertyDto.getKey(), propertyDto.getValue()));
        }
        for (EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto : xpathExpressions) {
            String attribTypeName = eventInputPropertyConfigurationDto.getType();
            AttributeType attributeType = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribTypeName.toLowerCase());
            if (attributeType == null) {
                throw new EventReceiverAdminServiceException(attribTypeName.toLowerCase() + " is not a supported attribute type, only the following are supported: " + EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP.keySet());
            }
            // For XML we use toElementKey as the property key.
            InputMappingAttribute xmlMappingAttribute = new InputMappingAttribute(eventInputPropertyConfigurationDto.getValueOf(), eventInputPropertyConfigurationDto.getName(), attributeType);
            xmlMappingAttribute.setDefaultValue(eventInputPropertyConfigurationDto.getDefaultValue());
            xmlInputMapping.addInputMappingAttribute(xmlMappingAttribute);

        }
        xmlInputMapping.setXPathDefinitions(xPathDefinitionList);
        setCommonPropertiesToEventReceiverConfig(eventReceiverConfiguration, inputEventAdaptorConfiguration, eventReceiverName, streamNameWithVersion, eventAdaptorType, mappingEnabled, xmlInputMapping);

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

        EventInputPropertyConfigurationDto[] xPathDefinitionProperties = getXPathDefinitionProperties(eventReceiverConfiguration);
        EventInputPropertyConfigurationDto[] xpathExpressions = getXPathExpressionProperties(eventReceiverConfiguration);
        eventReceiverConfigurationDto.setXpathDefinitions(xPathDefinitionProperties);
        eventReceiverConfigurationDto.setPayloadEventReceiverProperties(xpathExpressions);

        eventReceiverConfigurationDto.setParentSelectorXpath(((XMLInputMapping) eventReceiverConfiguration.getInputMapping()).getParentSelectorXpath());

        return eventReceiverConfigurationDto;

    }

    private EventInputPropertyConfigurationDto[] getXPathDefinitionProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        XMLInputMapping xmlInputMapping = (XMLInputMapping) eventReceiverConfiguration.getInputMapping();

        // Add XPathDefinition as a property.
        List<XPathDefinition> xpathDefs = xmlInputMapping.getXPathDefinitions();
        if (xpathDefs != null) {
            for (XPathDefinition xpathDef : xpathDefs) {
                EventInputPropertyConfigurationDto xpathDefPropertyDto = new EventInputPropertyConfigurationDto();
                xpathDefPropertyDto.setName(xpathDef.getPrefix());
                xpathDefPropertyDto.setValueOf(xpathDef.getNamespaceUri());
                eventInputPropertyConfigurationDtos.add(xpathDefPropertyDto);
            }
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto[] getXPathExpressionProperties(
            EventReceiverConfiguration eventReceiverConfiguration) {
        List<EventInputPropertyConfigurationDto> eventInputPropertyConfigurationDtos = new ArrayList<EventInputPropertyConfigurationDto>();
        XMLInputMapping xmlInputMapping = (XMLInputMapping) eventReceiverConfiguration.getInputMapping();

        for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
            EventInputPropertyConfigurationDto eventReceiverMessagePropertyDto = getMappingSectionProperty(inputMappingAttribute);
            eventInputPropertyConfigurationDtos.add(eventReceiverMessagePropertyDto);
        }

        return eventInputPropertyConfigurationDtos.toArray(new EventInputPropertyConfigurationDto[eventInputPropertyConfigurationDtos.size()]);
    }

    private EventInputPropertyConfigurationDto getMappingSectionProperty(
            InputMappingAttribute inputMappingAttribute) {
        // For XML we use ToElementKey as the key.
        EventInputPropertyConfigurationDto eventInputPropertyConfigurationDto = new EventInputPropertyConfigurationDto();
        eventInputPropertyConfigurationDto.setName(inputMappingAttribute.getToElementKey());
        eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getFromElementKey());
        eventInputPropertyConfigurationDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
        eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());

        return eventInputPropertyConfigurationDto;
    }
}
