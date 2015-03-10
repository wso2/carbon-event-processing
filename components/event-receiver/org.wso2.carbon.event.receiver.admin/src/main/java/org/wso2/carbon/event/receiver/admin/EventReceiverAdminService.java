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

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSchema;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.carbon.event.receiver.admin.internal.EventReceiverAdminConstants;
import org.wso2.carbon.event.receiver.admin.internal.PropertyAttributeTypeConstants;
import org.wso2.carbon.event.receiver.admin.internal.ds.EventReceiverAdminServiceValueHolder;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.config.*;
import org.wso2.carbon.event.receiver.core.config.mapping.JSONInputMapping;
import org.wso2.carbon.event.receiver.core.config.mapping.*;
import org.wso2.carbon.event.receiver.core.config.mapping.WSO2EventInputMapping;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.util.*;

public class EventReceiverAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventReceiverAdminService.class);

    public EventReceiverConfigurationInfoDto[] getAllActiveEventReceiverConfigurations()
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        // get event receiver configurations
        List<EventReceiverConfiguration> eventReceiverConfigurationList;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();

        if (eventReceiverConfigurationList != null) {
            // create event receiver configuration details array
            EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtoArray = new
                    EventReceiverConfigurationInfoDto[eventReceiverConfigurationList.size()];
            for (int index = 0; index < eventReceiverConfigurationInfoDtoArray.length; index++) {
                EventReceiverConfiguration eventReceiverConfiguration = eventReceiverConfigurationList.get(index);
                String eventReceiverName = eventReceiverConfiguration.getEventReceiverName();
                String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
                String inputEventAdapterType = eventReceiverConfiguration.getFromAdapterConfiguration().getType();
                String streamNameWithVersion = eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion();


                eventReceiverConfigurationInfoDtoArray[index] = new EventReceiverConfigurationInfoDto();
                eventReceiverConfigurationInfoDtoArray[index].setEventReceiverName(eventReceiverName);
                eventReceiverConfigurationInfoDtoArray[index].setMessageFormat(mappingType);
                eventReceiverConfigurationInfoDtoArray[index].setInputAdapterType(inputEventAdapterType);
                eventReceiverConfigurationInfoDtoArray[index].setInputStreamId(streamNameWithVersion);
                eventReceiverConfigurationInfoDtoArray[index].setEnableStats(eventReceiverConfiguration.isStatisticsEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEnableTracing(eventReceiverConfiguration.isTraceEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEditable(eventReceiverConfiguration.isEditable());
            }
            return eventReceiverConfigurationInfoDtoArray;
        } else {
            return new EventReceiverConfigurationInfoDto[0];
        }
    }

    public EventReceiverConfigurationInfoDto[] getAllStreamSpecificActiveEventReceiverConfigurations(
            String streamId)
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        // get event receiver configurations
        List<EventReceiverConfiguration> eventReceiverConfigurationList;
        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations(streamId);

        if (eventReceiverConfigurationList != null) {
            // create event receiver configuration details array
            EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtoArray = new
                    EventReceiverConfigurationInfoDto[eventReceiverConfigurationList.size()];
            for (int index = 0; index < eventReceiverConfigurationInfoDtoArray.length; index++) {
                EventReceiverConfiguration eventReceiverConfiguration = eventReceiverConfigurationList.get(index);
                String eventReceiverName = eventReceiverConfiguration.getEventReceiverName();
                String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
                String inputEventAdapterType = eventReceiverConfiguration.getFromAdapterConfiguration().getType();

                eventReceiverConfigurationInfoDtoArray[index] = new EventReceiverConfigurationInfoDto();
                eventReceiverConfigurationInfoDtoArray[index].setEventReceiverName(eventReceiverName);
                eventReceiverConfigurationInfoDtoArray[index].setMessageFormat(mappingType);
                eventReceiverConfigurationInfoDtoArray[index].setInputAdapterType(inputEventAdapterType);
                eventReceiverConfigurationInfoDtoArray[index].setEnableStats(eventReceiverConfiguration.isStatisticsEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEnableTracing(eventReceiverConfiguration.isTraceEnabled());
                eventReceiverConfigurationInfoDtoArray[index].setEditable(eventReceiverConfiguration.isEditable());
            }
            return eventReceiverConfigurationInfoDtoArray;
        } else {
            return new EventReceiverConfigurationInfoDto[0];
        }
    }

    public EventReceiverConfigurationFileDto[] getAllInactiveEventReceiverConfigurations()
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = eventReceiverService.getAllInactiveEventReceiverConfigurations();
        if (eventReceiverConfigurationFileList != null) {

            // create event receiver file details array
            EventReceiverConfigurationFileDto[] eventReceiverFileDtoArray = new
                    EventReceiverConfigurationFileDto[eventReceiverConfigurationFileList.size()];

            for (int index = 0; index < eventReceiverFileDtoArray.length; index++) {
                EventReceiverConfigurationFile eventReceiverConfigurationFile = eventReceiverConfigurationFileList.get(index);
                String fileName = eventReceiverConfigurationFile.getFileName();
                String eventReceiverName = eventReceiverConfigurationFile.getEventReceiverName();
                String statusMsg = eventReceiverConfigurationFile.getDeploymentStatusMessage();
                if (eventReceiverConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventReceiverConfigurationFile.getDependency() + "]";
                }

                eventReceiverFileDtoArray[index] = new EventReceiverConfigurationFileDto(fileName, eventReceiverName, statusMsg);
            }
            return eventReceiverFileDtoArray;
        } else {
            return new EventReceiverConfigurationFileDto[0];
        }
    }

    public EventReceiverConfigurationDto getActiveEventReceiverConfiguration(
            String eventReceiverName) throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        AxisConfiguration axisConfiguration = getAxisConfig();

        EventReceiverConfiguration eventReceiverConfiguration = eventReceiverService.getActiveEventReceiverConfiguration(eventReceiverName);
        if (eventReceiverConfiguration != null) {
            EventReceiverConfigurationDto eventReceiverConfigurationDto = new EventReceiverConfigurationDto();
            eventReceiverConfigurationDto.setEventReceiverName(eventReceiverConfiguration.getEventReceiverName());
            String streamNameWithVersion = eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion();
            eventReceiverConfigurationDto.setFromStreamNameWithVersion(streamNameWithVersion);

            InputEventAdapterConfiguration fromAdapterConfiguration = eventReceiverConfiguration.getFromAdapterConfiguration();

            if (fromAdapterConfiguration != null) {
                InputEventAdapterService inputEventAdapterService = EventReceiverAdminServiceValueHolder.getInputEventAdapterService();
                InputEventAdapterSchema inputEventAdapterSchema = inputEventAdapterService.getInputEventAdapterSchema(fromAdapterConfiguration.getType());

                InputAdapterConfigurationDto fromAdapterConfigurationDto = new InputAdapterConfigurationDto();
                fromAdapterConfigurationDto.setEventAdapterType(fromAdapterConfiguration.getType());
                fromAdapterConfigurationDto.setSupportedMessageFormats(
                        inputEventAdapterSchema.getSupportedMessageFormats().
                                toArray(new String[inputEventAdapterSchema.getSupportedMessageFormats().size()]));

                Map<String, String> inputAdapterProperties = new HashMap<String, String>();
                inputAdapterProperties.putAll(fromAdapterConfiguration.getProperties());

                DetailInputAdapterPropertyDto[] detailInputAdapterPropertyDtos = getPropertyConfigurations(inputAdapterProperties, inputEventAdapterSchema.getPropertyList());
                fromAdapterConfigurationDto.setInputEventAdapterProperties(detailInputAdapterPropertyDtos);

                eventReceiverConfigurationDto.setToAdapterConfigurationDto(fromAdapterConfigurationDto);
            }

            InputMapping inputMapping = eventReceiverConfiguration.getInputMapping();
            List<EventMappingPropertyDto> eventMappingPropertyDtos = new ArrayList<EventMappingPropertyDto>();
            for (InputMappingAttribute inputMappingAttribute : inputMapping.getInputMappingAttributes()) {

                EventMappingPropertyDto eventInputPropertyConfigurationDto = new EventMappingPropertyDto();
                eventInputPropertyConfigurationDto.setName(inputMappingAttribute.getToElementKey());
                eventInputPropertyConfigurationDto.setValueOf(inputMappingAttribute.getFromElementKey());
                eventInputPropertyConfigurationDto.setType(EventReceiverAdminConstants.ATTRIBUTE_TYPE_STRING_MAP.get(inputMappingAttribute.getToElementType()));
                eventInputPropertyConfigurationDto.setDefaultValue(inputMappingAttribute.getDefaultValue());
                eventMappingPropertyDtos.add(eventInputPropertyConfigurationDto);
            }
            eventReceiverConfigurationDto.setMappingPropertyDtos(eventMappingPropertyDtos.toArray(new EventMappingPropertyDto[eventMappingPropertyDtos.size()]));
            return eventReceiverConfigurationDto;
        }

        return null;
    }

    public String getActiveEventReceiverConfigurationContent(String eventReceiverName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return eventReceiverService.getActiveEventReceiverConfigurationContent(eventReceiverName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventReceiverConfigurationContent(String fileName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            String eventReceiverConfigurationFile = eventReceiverService.getInactiveEventReceiverConfigurationContent(fileName);
            return eventReceiverConfigurationFile.trim();
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void undeployActiveEventReceiverConfiguration(String eventReceiverName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventReceiverService.undeployActiveEventReceiverConfiguration(eventReceiverName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void undeployInactiveEventReceiverConfiguration(String fileName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            eventReceiverService.undeployInactiveEventReceiverConfiguration(fileName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editActiveEventReceiverConfiguration(String eventReceiverConfiguration,
                                                     String eventReceiverName)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventReceiverService.editActiveEventReceiverConfiguration(eventReceiverConfiguration, eventReceiverName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editInactiveEventReceiverConfiguration(
            String eventReceiverConfiguration,
            String fileName)
            throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventReceiverService.editInactiveEventReceiverConfiguration(eventReceiverConfiguration, fileName);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void deployEventReceiverConfiguration(String eventReceiverConfigXml)
            throws AxisFault {
        try {
            EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
            eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfigXml);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void deployWso2EventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,
                                                     String eventAdapterType,
                                                     EventMappingPropertyDto[] metaData,
                                                     EventMappingPropertyDto[] correlationData,
                                                     EventMappingPropertyDto[] payloadData,
                                                     BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
//                StreamDefinition streamDefinition = eventReceiverService.getStreamDefinition(streamNameWithVersion, axisConfiguration);

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE);

                WSO2EventInputMapping wso2EventInputMapping = new WSO2EventInputMapping();
                wso2EventInputMapping.setCustomMappingEnabled(mappingEnabled);

                if (mappingEnabled) {
                    if (metaData != null && metaData.length != 0) {
                        for (EventMappingPropertyDto wso2EventInputPropertyConfiguration : metaData) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(wso2EventInputPropertyConfiguration.getName(), wso2EventInputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(wso2EventInputPropertyConfiguration.getType()), EventReceiverConstants.META_DATA_VAL);
                            wso2EventInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                    if (correlationData != null && correlationData.length != 0) {
                        for (EventMappingPropertyDto wso2EventInputPropertyConfiguration : correlationData) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(wso2EventInputPropertyConfiguration.getName(), wso2EventInputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(wso2EventInputPropertyConfiguration.getType()), EventReceiverConstants.CORRELATION_DATA_VAL);
                            wso2EventInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                    if (payloadData != null && payloadData.length != 0) {
                        for (EventMappingPropertyDto wso2EventInputPropertyConfiguration : payloadData) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(wso2EventInputPropertyConfiguration.getName(), wso2EventInputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(wso2EventInputPropertyConfiguration.getType()), EventReceiverConstants.PAYLOAD_DATA_VAL);
                            wso2EventInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(wso2EventInputMapping);
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }

    }

    public void deployTextEventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,
                                                     String eventAdapterType,
                                                     EventMappingPropertyDto[] inputMappings,
                                                     BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_TEXT_MAPPING_TYPE);

                TextInputMapping textInputMapping = new TextInputMapping();
                textInputMapping.setCustomMappingEnabled(mappingEnabled);
                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            textInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(textInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);

            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }

    }

    public void deployXmlEventReceiverConfiguration(String eventReceiverName,
                                                    String streamNameWithVersion,
                                                    String eventAdapterType,
                                                    String parentXpath,
                                                    EventMappingPropertyDto[] namespaces,
                                                    EventMappingPropertyDto[] inputMappings,
                                                    BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_XML_MAPPING_TYPE);

                XMLInputMapping xmlInputMapping = new XMLInputMapping();
                xmlInputMapping.setCustomMappingEnabled(mappingEnabled);
                xmlInputMapping.setParentSelectorXpath(parentXpath);
                if (namespaces != null && namespaces.length != 0) {
                    List<XPathDefinition> xPathDefinitions = new ArrayList<XPathDefinition>();
                    for (EventMappingPropertyDto namespace : namespaces) {
                        XPathDefinition xPathDefinition = new XPathDefinition(namespace.getName(), namespace.getValueOf());
                        xPathDefinitions.add(xPathDefinition);
                    }
                    xmlInputMapping.setXPathDefinitions(xPathDefinitions);
                }

                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(),mappingProperty.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            inputProperty.setDefaultValue(mappingProperty.getDefaultValue());
                            xmlInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(xmlInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }

    }

    public void deployMapEventReceiverConfiguration(String eventReceiverName,
                                                    String streamNameWithVersion,
                                                    String eventAdapterType,
                                                    EventMappingPropertyDto[] inputMappings,
                                                    BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_MAP_MAPPING_TYPE);


                MapInputMapping mapInputMapping = new MapInputMapping();
                mapInputMapping.setCustomMappingEnabled(mappingEnabled);

                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            mapInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(mapInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);

            } catch (EventReceiverConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }

    }

    public void deployJsonEventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,
                                                     String eventAdapterType,
                                                     EventMappingPropertyDto[] inputMappings,
                                                     BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {

        if (checkEventReceiverValidity(eventReceiverName)) {
            try {
                EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();

                EventReceiverConfiguration eventReceiverConfiguration = new EventReceiverConfiguration();

                eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
                String[] toStreamProperties = streamNameWithVersion.split(":");
                eventReceiverConfiguration.setToStreamName(toStreamProperties[0]);
                eventReceiverConfiguration.setToStreamVersion(toStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                constructInputAdapterRelatedConfigs(eventReceiverName, eventAdapterType, inputPropertyConfiguration,
                        eventReceiverConfiguration, EventReceiverConstants.ER_JSON_MAPPING_TYPE);

                JSONInputMapping jsonInputMapping = new JSONInputMapping();
                jsonInputMapping.setCustomMappingEnabled(mappingEnabled);
                if (mappingEnabled) {
                    if (inputMappings != null && inputMappings.length != 0) {
                        for (EventMappingPropertyDto mappingProperty : inputMappings) {
                            InputMappingAttribute inputProperty = new InputMappingAttribute(mappingProperty.getName(), mappingProperty.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(mappingProperty.getType()));
                            jsonInputMapping.addInputMappingAttribute(inputProperty);
                        }
                    }
                }
                eventReceiverConfiguration.setInputMapping(jsonInputMapping);

                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration);

            } catch (EventReceiverConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventReceiverName + " is already registered for this tenant");
        }

    }

    public void setStatisticsEnabled(String eventReceiverName, boolean flag) throws AxisFault {

        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventReceiverService.setStatisticsEnabled(eventReceiverName, flag);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setTracingEnabled(String eventReceiverName, boolean flag) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventReceiverService.setTraceEnabled(eventReceiverName, flag);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public InputAdapterConfigurationDto getInputAdapterConfigurationSchema(String adopterType) {
        InputEventAdapterService inputEventAdapterService = EventReceiverAdminServiceValueHolder.getInputEventAdapterService();
        InputEventAdapterSchema inputEventAdapterSchema = inputEventAdapterService.getInputEventAdapterSchema(adopterType);

        InputAdapterConfigurationDto inputAdapterConfigurationDto = new InputAdapterConfigurationDto();
        inputAdapterConfigurationDto.setInputEventAdapterProperties(getPropertyConfigurations(null, inputEventAdapterSchema.getPropertyList()));
        inputAdapterConfigurationDto.setEventAdapterType(adopterType);
        inputAdapterConfigurationDto.setSupportedMessageFormats(
                inputEventAdapterSchema.getSupportedMessageFormats().
                        toArray(new String[inputEventAdapterSchema.getSupportedMessageFormats().size()]));
        return inputAdapterConfigurationDto;
    }

    public String[] getAllInputAdapterTypes() {
        InputEventAdapterService inputEventAdapterService = EventReceiverAdminServiceValueHolder.getInputEventAdapterService();
        List<String> inputEventAdapters = inputEventAdapterService.getInputEventAdapterTypes();
        if (inputEventAdapters == null) {
            return new String[0];
        } else {
            String[] types = new String[inputEventAdapters.size()];
            return inputEventAdapters.toArray(types);
        }
    }


    private DetailInputAdapterPropertyDto[] getPropertyConfigurations(Map<String, String> messageProperties, List<Property> propertyList) {
        if (propertyList != null && propertyList.size() > 0) {
            DetailInputAdapterPropertyDto[] detailInputAdapterPropertyDtoArray = new DetailInputAdapterPropertyDto[propertyList.size()];
            int index = 0;
            for (Property property : propertyList) {
                // create input event property
                String value = null;
                if (messageProperties != null) {
                    value = messageProperties.get(property.getPropertyName());
                }
                detailInputAdapterPropertyDtoArray[index] = new DetailInputAdapterPropertyDto(property.getPropertyName(),
                        value);
                // set input event property parameters
                detailInputAdapterPropertyDtoArray[index].setSecured(property.isSecured());
                detailInputAdapterPropertyDtoArray[index].setRequired(property.isRequired());
                detailInputAdapterPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                detailInputAdapterPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                detailInputAdapterPropertyDtoArray[index].setHint(property.getHint());
                detailInputAdapterPropertyDtoArray[index].setOptions(property.getOptions());
                index++;
            }
            return detailInputAdapterPropertyDtoArray;
        }
        return new DetailInputAdapterPropertyDto[0];
    }

    private boolean checkStreamAttributeValidity(List<String> inputEventAttributes,
                                                 StreamDefinition streamDefinition) {

        if (streamDefinition != null) {
            List<String> inComingStreamAttributes = new ArrayList<String>();
            final String PROPERTY_META_PREFIX = "meta_";
            final String PROPERTY_CORRELATION_PREFIX = "correlation_";

            List<Attribute> metaAttributeList = streamDefinition.getMetaData();
            List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
            List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();


            if (metaAttributeList != null) {
                for (Attribute attribute : metaAttributeList) {
                    inComingStreamAttributes.add(PROPERTY_META_PREFIX + attribute.getName());
                }
            }
            if (correlationAttributeList != null) {
                for (Attribute attribute : correlationAttributeList) {
                    inComingStreamAttributes.add(PROPERTY_CORRELATION_PREFIX + attribute.getName());
                }
            }
            if (payloadAttributeList != null) {
                for (Attribute attribute : payloadAttributeList) {
                    inComingStreamAttributes.add(attribute.getName());
                }
            }


            if (inputEventAttributes.size() > 0) {
                if (inComingStreamAttributes.containsAll(inputEventAttributes)) {
                    return true;
                } else {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }

    }

    private String getStreamAttributes(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        String attributes = "";

        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                attributes += PropertyAttributeTypeConstants.PROPERTY_META_PREFIX + attribute.getName() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                attributes += PropertyAttributeTypeConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                attributes += attribute.getName() + " " + attribute.getType().toString().toLowerCase() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            return attributes.substring(0, attributes.lastIndexOf(","));
        } else {
            return attributes;
        }
    }

    private boolean checkEventReceiverValidity(String eventReceiverName) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        List<EventReceiverConfiguration> eventReceiverConfigurationList = null;

        eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations();
        Iterator eventReceiverConfigurationIterator = eventReceiverConfigurationList.iterator();
        while (eventReceiverConfigurationIterator.hasNext()) {

            EventReceiverConfiguration eventReceiverConfiguration = (EventReceiverConfiguration) eventReceiverConfigurationIterator.next();
            if (eventReceiverConfiguration.getEventReceiverName().equalsIgnoreCase(eventReceiverName)) {
                return false;
            }
        }

        return true;
    }

    private String getPropertyAttributeDataType(String propertyName,
                                                StreamDefinition streamDefinition)
            throws AxisFault {

        if (propertyName != null) {
            List<Attribute> metaDataList = streamDefinition.getMetaData();
            if (metaDataList != null) {
                for (Attribute attribute : metaDataList) {
                    if (propertyName.equalsIgnoreCase(PropertyAttributeTypeConstants.PROPERTY_META_PREFIX + attribute.getName())) {
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }

            List<Attribute> correlationDataList = streamDefinition.getCorrelationData();
            if (correlationDataList != null) {
                for (Attribute attribute : correlationDataList) {
                    if (propertyName.equalsIgnoreCase(PropertyAttributeTypeConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName())) {
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }

            List<Attribute> payloadDataList = streamDefinition.getPayloadData();
            if (payloadDataList != null) {
                for (Attribute attribute : payloadDataList) {
                    if (propertyName.equalsIgnoreCase(attribute.getName())) {
                        return attribute.getType().toString().toLowerCase();
                    }
                }
            }
        }

        throw new AxisFault("Input Stream attributes are not matching with input stream definition");

    }

    private void constructInputAdapterRelatedConfigs(String eventReceiverName, String eventAdapterType,
                                                     BasicInputAdapterPropertyDto[] inputPropertyConfiguration,
                                                     EventReceiverConfiguration eventReceiverConfiguration,
                                                     String messageFormat) {
        InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
        inputEventAdapterConfiguration.setName(eventReceiverName);
        inputEventAdapterConfiguration.setType(eventAdapterType);
        inputEventAdapterConfiguration.setMessageFormat(messageFormat);
        inputEventAdapterConfiguration.setProperties(new HashMap<String, String>());

        // add input message property configuration to the map
        if (inputPropertyConfiguration != null && inputPropertyConfiguration.length != 0) {

            for (BasicInputAdapterPropertyDto eventReceiverProperty : inputPropertyConfiguration) {
                if (!eventReceiverProperty.getValue().trim().equals("")) {
                    inputEventAdapterConfiguration.getProperties().put(eventReceiverProperty.getKey().trim(), eventReceiverProperty.getValue().trim());
                }
            }
        }

        eventReceiverConfiguration.setFromAdapterConfiguration(inputEventAdapterConfiguration);
    }


}