/*
*  Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.admin.internal.util.EventPublisherAdminServiceValueHolder;
import org.wso2.carbon.event.publisher.admin.internal.util.PropertyAttributeTypeConstants;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorService;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.*;
import org.wso2.carbon.event.publisher.core.config.mapping.*;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventPublisherAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventPublisherAdminService.class);

    public EventPublisherConfigurationInfoDto[] getAllActiveEventPublisherConfiguration()
            throws AxisFault {

        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event publisher configurations
            List<EventPublisherConfiguration> eventPublisherConfigurationList;
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfiguration(axisConfiguration);

            if (eventPublisherConfigurationList != null) {
                // create event publisher configuration details array
                EventPublisherConfigurationInfoDto[] eventPublisherConfigurationInfoDtoArray = new
                        EventPublisherConfigurationInfoDto[eventPublisherConfigurationList.size()];
                for (int index = 0; index < eventPublisherConfigurationInfoDtoArray.length; index++) {
                    EventPublisherConfiguration eventPublisherConfiguration = eventPublisherConfigurationList.get(index);
                    String eventPublisherName = eventPublisherConfiguration.getEventPublisherName();
                    String mappingType = eventPublisherConfiguration.getOutputMapping().getMappingType();
                    String outputEndpointType = eventPublisherConfiguration.getEndpointAdaptorConfiguration().getEndpointType();
                    String streamNameWithVersion = eventPublisherConfiguration.getFromStreamName() + ":" + eventPublisherConfiguration.getFromStreamVersion();


                    eventPublisherConfigurationInfoDtoArray[index] = new EventPublisherConfigurationInfoDto();
                    eventPublisherConfigurationInfoDtoArray[index].setEventPublisherName(eventPublisherName);
                    eventPublisherConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventPublisherConfigurationInfoDtoArray[index].setOutputEndpointType(outputEndpointType);
                    eventPublisherConfigurationInfoDtoArray[index].setInputStreamId(streamNameWithVersion);
                    eventPublisherConfigurationInfoDtoArray[index].setEnableStats(eventPublisherConfiguration.isEnableStatistics());
                    eventPublisherConfigurationInfoDtoArray[index].setEnableTracing(eventPublisherConfiguration.isEnableTracing());
                }
                return eventPublisherConfigurationInfoDtoArray;
            } else {
                return new EventPublisherConfigurationInfoDto[0];
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventPublisherConfigurationInfoDto[] getAllStreamSpecificActiveEventPublisherConfiguration(
            String streamId)
            throws AxisFault {

        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event publisher configurations
            List<EventPublisherConfiguration> eventPublisherConfigurationList;
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfiguration(axisConfiguration, streamId);

            if (eventPublisherConfigurationList != null) {
                // create event publisher configuration details array
                EventPublisherConfigurationInfoDto[] eventPublisherConfigurationInfoDtoArray = new
                        EventPublisherConfigurationInfoDto[eventPublisherConfigurationList.size()];
                for (int index = 0; index < eventPublisherConfigurationInfoDtoArray.length; index++) {
                    EventPublisherConfiguration eventPublisherConfiguration = eventPublisherConfigurationList.get(index);
                    String eventPublisherName = eventPublisherConfiguration.getEventPublisherName();
                    String mappingType = eventPublisherConfiguration.getOutputMapping().getMappingType();
                    String outputEndpointType = eventPublisherConfiguration.getEndpointAdaptorConfiguration().getEndpointType();

                    eventPublisherConfigurationInfoDtoArray[index] = new EventPublisherConfigurationInfoDto();
                    eventPublisherConfigurationInfoDtoArray[index].setEventPublisherName(eventPublisherName);
                    eventPublisherConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventPublisherConfigurationInfoDtoArray[index].setOutputEndpointType(outputEndpointType);
                    eventPublisherConfigurationInfoDtoArray[index].setEnableStats(eventPublisherConfiguration.isEnableStatistics());
                    eventPublisherConfigurationInfoDtoArray[index].setEnableTracing(eventPublisherConfiguration.isEnableTracing());
                }
                return eventPublisherConfigurationInfoDtoArray;
            } else {
                return new EventPublisherConfigurationInfoDto[0];
            }
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventPublisherConfigurationFileDto[] getAllInactiveEventPublisherConfiguration()
            throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<EventPublisherConfigurationFile> eventPublisherConfigurationFileList = eventPublisherService.getAllInactiveEventPublisherConfiguration(axisConfiguration);
        if (eventPublisherConfigurationFileList != null) {

            // create event publisher file details array
            EventPublisherConfigurationFileDto[] eventPublisherFileDtoArray = new
                    EventPublisherConfigurationFileDto[eventPublisherConfigurationFileList.size()];

            for (int index = 0; index < eventPublisherFileDtoArray.length; index++) {
                EventPublisherConfigurationFile eventPublisherConfigurationFile = eventPublisherConfigurationFileList.get(index);
                String fileName = eventPublisherConfigurationFile.getFileName();
                String eventPublisherName = eventPublisherConfigurationFile.getEventPublisherName();
                String statusMsg = eventPublisherConfigurationFile.getDeploymentStatusMessage();
                if (eventPublisherConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventPublisherConfigurationFile.getDependency() + "]";
                }

                eventPublisherFileDtoArray[index] = new EventPublisherConfigurationFileDto(fileName, eventPublisherName, statusMsg);
            }
            return eventPublisherFileDtoArray;
        } else {
            return new EventPublisherConfigurationFileDto[0];
        }
    }

    public EventPublisherConfigurationDto getActiveEventPublisherConfiguration(
            String eventPublisherName) throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        AxisConfiguration axisConfiguration = getAxisConfig();

        try {
            EventPublisherConfiguration eventPublisherConfiguration = eventPublisherService.getActiveEventPublisherConfiguration(eventPublisherName, tenantId);
            if (eventPublisherConfiguration != null) {
                EventPublisherConfigurationDto eventPublisherConfigurationDto = new EventPublisherConfigurationDto();
                eventPublisherConfigurationDto.setEventPublisherName(eventPublisherConfiguration.getEventPublisherName());
                String streamNameWithVersion = eventPublisherConfiguration.getFromStreamName() + ":" + eventPublisherConfiguration.getFromStreamVersion();
                eventPublisherConfigurationDto.setFromStreamNameWithVersion(streamNameWithVersion);
                eventPublisherConfigurationDto.setStreamDefinition(getStreamAttributes(eventPublisherService.getStreamDefinition(streamNameWithVersion, axisConfiguration)));

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = eventPublisherConfiguration.getEndpointAdaptorConfiguration();
                if (endpointAdaptorConfiguration != null) {
                    EndpointPropertyConfigurationDto endpointPropertyConfigurationDto = new EndpointPropertyConfigurationDto();
                    endpointPropertyConfigurationDto.setEventAdaptorType(endpointAdaptorConfiguration.getEndpointType());
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = endpointAdaptorConfiguration.getOutputAdaptorConfiguration();
                    if (internalOutputEventAdaptorConfiguration != null && internalOutputEventAdaptorConfiguration.getProperties().size() > 0) {
                        EventPublisherPropertyDto[] eventPublisherPropertyDtos = getOutputEventPublisherMessageConfiguration(internalOutputEventAdaptorConfiguration.getProperties(), endpointAdaptorConfiguration.getEndpointType());
                        endpointPropertyConfigurationDto.setOutputEventAdaptorConfiguration(eventPublisherPropertyDtos);
                    }

                    eventPublisherConfigurationDto.setEndpointPropertyConfigurationDto(endpointPropertyConfigurationDto);
                }

                if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_JSON_MAPPING_TYPE)) {
                    JSONOutputMapping jsonOutputMapping = (JSONOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    JSONOutputMappingDto jsonOutputMappingDto = new JSONOutputMappingDto();
                    jsonOutputMappingDto.setMappingText(jsonOutputMapping.getMappingText());
                    jsonOutputMappingDto.setRegistryResource(jsonOutputMapping.isRegistryResource());
                    eventPublisherConfigurationDto.setJsonOutputMappingDto(jsonOutputMappingDto);
                    eventPublisherConfigurationDto.setMappingType("json");
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_XML_MAPPING_TYPE)) {
                    XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    XMLOutputMappingDto xmlOutputMappingDto = new XMLOutputMappingDto();
                    xmlOutputMappingDto.setMappingXMLText(xmlOutputMapping.getMappingXMLText());
                    xmlOutputMappingDto.setRegistryResource(xmlOutputMapping.isRegistryResource());
                    eventPublisherConfigurationDto.setXmlOutputMappingDto(xmlOutputMappingDto);
                    eventPublisherConfigurationDto.setMappingType("xml");
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_TEXT_MAPPING_TYPE)) {
                    TextOutputMapping textOutputMapping = (TextOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    TextOutputMappingDto textOutputMappingDto = new TextOutputMappingDto();
                    textOutputMappingDto.setMappingText(textOutputMapping.getMappingText());
                    textOutputMappingDto.setRegistryResource(textOutputMapping.isRegistryResource());
                    eventPublisherConfigurationDto.setTextOutputMappingDto(textOutputMappingDto);
                    eventPublisherConfigurationDto.setMappingType("text");
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_MAP_MAPPING_TYPE)) {
                    MapOutputMapping mapOutputMapping = (MapOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    MapOutputMappingDto mapOutputMappingDto = new MapOutputMappingDto();
                    List<EventOutputProperty> outputPropertyList = mapOutputMapping.getOutputPropertyConfiguration();
                    if (outputPropertyList != null && outputPropertyList.size() > 0) {
                        EventOutputPropertyDto[] eventOutputPropertyDtos = new EventOutputPropertyDto[outputPropertyList.size()];
                        int index = 0;
                        for (EventOutputProperty eventOutputProperty : outputPropertyList) {
                            eventOutputPropertyDtos[index] = new EventOutputPropertyDto();
                            eventOutputPropertyDtos[index].setName(eventOutputProperty.getName());
                            eventOutputPropertyDtos[index].setValueOf(eventOutputProperty.getValueOf());
                            index++;
                        }
                        mapOutputMappingDto.setOutputPropertyConfiguration(eventOutputPropertyDtos);
                    }

                    eventPublisherConfigurationDto.setMapOutputMappingDto(mapOutputMappingDto);
                    eventPublisherConfigurationDto.setMappingType("map");
                } else if (eventPublisherConfiguration.getOutputMapping().getMappingType().equals(EventPublisherConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
                    WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventPublisherConfiguration.getOutputMapping();
                    WSO2EventOutputMappingDto wso2EventOutputMappingDto = new WSO2EventOutputMappingDto();
                    List<EventOutputProperty> metaOutputPropertyList = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> correlationOutputPropertyList = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> payloadOutputPropertyList = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

                    wso2EventOutputMappingDto.setMetaWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(metaOutputPropertyList));
                    wso2EventOutputMappingDto.setCorrelationWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(correlationOutputPropertyList));
                    wso2EventOutputMappingDto.setPayloadWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(payloadOutputPropertyList));

                    eventPublisherConfigurationDto.setWso2EventOutputMappingDto(wso2EventOutputMappingDto);
                    eventPublisherConfigurationDto.setMappingType("wso2event");
                }

                return eventPublisherConfigurationDto;
            }

        } catch (EventPublisherConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return null;
    }

    public String getActiveEventPublisherConfigurationContent(String eventPublisherName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return eventPublisherService.getActiveEventPublisherConfigurationContent(eventPublisherName, axisConfiguration);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventPublisherConfigurationContent(String fileName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            String eventPublisherConfigurationFile = eventPublisherService.getInactiveEventPublisherConfigurationContent(fileName, getAxisConfig());
            return eventPublisherConfigurationFile.trim();
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventPublisherPropertyDto[] getEventPublisherAdaptorProperties(String endpointAdaptorType)
            throws AxisFault {

        OutputEventAdaptorService eventAdaptorService = EventPublisherAdminServiceValueHolder.getOutputEventAdaptorService();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OutputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(endpointAdaptorType);

            List<Property> propertyList = eventAdaptorDto.getAdaptorPropertyList();
            if (propertyList != null) {
                EventPublisherPropertyDto[] eventPublisherPropertyDtoArray = new EventPublisherPropertyDto[propertyList.size()];
                for (int index = 0; index < eventPublisherPropertyDtoArray.length; index++) {
                    Property property = propertyList.get(index);
                    // set event fotifier property parameters
                    eventPublisherPropertyDtoArray[index] = new EventPublisherPropertyDto(property.getPropertyName(), "");
                    eventPublisherPropertyDtoArray[index].setRequired(property.isRequired());
                    eventPublisherPropertyDtoArray[index].setSecured(property.isSecured());
                    eventPublisherPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    eventPublisherPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    eventPublisherPropertyDtoArray[index].setHint(property.getHint());
                    eventPublisherPropertyDtoArray[index].setOptions(property.getOptions());
                }
                return eventPublisherPropertyDtoArray;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return new EventPublisherPropertyDto[0];
    }

    public void undeployActiveEventPublisherConfiguration(String eventPublisherName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventPublisherService.undeployActiveEventPublisherConfiguration(eventPublisherName, axisConfiguration);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void undeployInactiveEventPublisherConfiguration(String fileName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            eventPublisherService.undeployInactiveEventPublisherConfiguration(fileName, axisConfiguration);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editActiveEventPublisherConfiguration(String eventPublisherConfiguration,
                                                      String eventPublisherName)
            throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventPublisherService.editActiveEventPublisherConfiguration(eventPublisherConfiguration, eventPublisherName, axisConfiguration);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editInactiveEventPublisherConfiguration(
            String eventPublisherConfiguration,
            String fileName)
            throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventPublisherService.editInactiveEventPublisherConfiguration(eventPublisherConfiguration, fileName, axisConfiguration);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void deployEventPublisherConfiguration(String eventPublisherConfigXml)
            throws AxisFault {
        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
            eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfigXml, getAxisConfig());
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void deployWSO2EventPublisherConfiguration(String eventPublisherName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorType,
                                                      EventOutputPropertyConfigurationDto[] metaData,
                                                      EventOutputPropertyConfigurationDto[] correlationData,
                                                      EventOutputPropertyConfigurationDto[] payloadData,
                                                      PropertyDto[] outputPropertyConfiguration,
                                                      boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                StreamDefinition streamDefinition = eventPublisherService.getStreamDefinition(streamNameWithVersion, axisConfiguration);

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventPublisherName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventPublisherProperty : outputPropertyConfiguration) {
                        if (!eventPublisherProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventPublisherConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                WSO2EventOutputMapping wso2EventOutputMapping = new WSO2EventOutputMapping();
                wso2EventOutputMapping.setCustomMappingEnabled(mappingEnabled);

                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (metaData != null && metaData.length != 0) {
                        for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : metaData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(), streamDefinition)));
                            wso2EventOutputMapping.addMetaWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                        }

                    }

                    if (correlationData != null && correlationData.length != 0) {
                        for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : correlationData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(), streamDefinition)));
                            wso2EventOutputMapping.addCorrelationWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                        }
                    }

                    if (payloadData != null && payloadData.length != 0) {
                        for (EventOutputPropertyConfigurationDto wso2EventOutputPropertyConfiguration : payloadData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(wso2EventOutputPropertyConfiguration.getName(), wso2EventOutputPropertyConfiguration.getValueOf(), PropertyAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(getPropertyAttributeDataType(wso2EventOutputPropertyConfiguration.getValueOf(), streamDefinition)));
                            wso2EventOutputMapping.addPayloadWSO2EventOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(wso2EventOutputPropertyConfiguration.getValueOf());
                        }
                    }
                }

                eventPublisherConfiguration.setOutputMapping(wso2EventOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, streamDefinition)) {
                    eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }

    }

    public void deployTextEventPublisherConfiguration(String eventPublisherName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorType,
                                                      String textData,
                                                      PropertyDto[] outputPropertyConfiguration,
                                                      String dataFrom, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventPublisherName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventPublisherProperty : outputPropertyConfiguration) {
                        if (!eventPublisherProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventPublisherConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                TextOutputMapping textOutputMapping = new TextOutputMapping();
                textOutputMapping.setCustomMappingEnabled(mappingEnabled);

                textOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                textOutputMapping.setMappingText(textData);

                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (dataFrom.equalsIgnoreCase("registry")) {
                        textData = eventPublisherService.getRegistryResourceContent(textData, tenantId);
                    }
                    outputEventAttributes = getOutputMappingPropertyList(textData);
                }
                eventPublisherConfiguration.setOutputMapping(textOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventPublisherService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }

    }

    public void deployXmlEventPublisherConfiguration(String eventPublisherName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorType,
                                                     String textData,
                                                     PropertyDto[] outputPropertyConfiguration,
                                                     String dataFrom, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventPublisherName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventPublisherProperty : outputPropertyConfiguration) {
                        if (!eventPublisherProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventPublisherConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                XMLOutputMapping xmlOutputMapping = new XMLOutputMapping();
                xmlOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    xmlOutputMapping.setMappingXMLText(textData);
                    xmlOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                    outputEventAttributes = getOutputMappingPropertyList(textData);
                }

                eventPublisherConfiguration.setOutputMapping(xmlOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventPublisherService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventPublisherConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }

    }

    public void deployMapEventPublisherConfiguration(String eventPublisherName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorType,
                                                     EventOutputPropertyConfigurationDto[] mapData,
                                                     PropertyDto[] outputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventPublisherName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventPublisherProperty : outputPropertyConfiguration) {
                        if (!eventPublisherProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventPublisherConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                MapOutputMapping mapOutputMapping = new MapOutputMapping();
                mapOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (mapData != null && mapData.length != 0) {
                        for (EventOutputPropertyConfigurationDto eventOutputPropertyConfiguration : mapData) {
                            EventOutputProperty eventOutputProperty = new EventOutputProperty(eventOutputPropertyConfiguration.getName(), eventOutputPropertyConfiguration.getValueOf());
                            mapOutputMapping.addOutputPropertyConfiguration(eventOutputProperty);
                            outputEventAttributes.add(eventOutputPropertyConfiguration.getValueOf());
                        }

                    }
                }

                eventPublisherConfiguration.setOutputMapping(mapOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventPublisherService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventPublisherConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }

    }

    public void deployJsonEventPublisherConfiguration(String eventPublisherName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorType,
                                                      String jsonData,
                                                      PropertyDto[] outputPropertyConfiguration,
                                                      String dataFrom, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventPublisherValidity(eventPublisherName)) {
            try {
                EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();

                EventPublisherConfiguration eventPublisherConfiguration = new EventPublisherConfiguration();

                eventPublisherConfiguration.setEventPublisherName(eventPublisherName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventPublisherConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventPublisherConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventPublisherName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventPublisherProperty : outputPropertyConfiguration) {
                        if (!eventPublisherProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventPublisherProperty.getKey().trim(), eventPublisherProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventPublisherConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();

                jsonOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    jsonOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                    jsonOutputMapping.setMappingText(jsonData);
                    outputEventAttributes = getOutputMappingPropertyList(jsonData);
                }

                eventPublisherConfiguration.setOutputMapping(jsonOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventPublisherService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventPublisherService.deployEventPublisherConfiguration(eventPublisherConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventPublisherConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventPublisherName + " is already registered for this tenant");
        }

    }

    public void setStatisticsEnabled(String eventPublisherName, boolean flag) throws AxisFault {

        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventPublisherService.setStatisticsEnabled(eventPublisherName, axisConfiguration, flag);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setTracingEnabled(String eventPublisherName, boolean flag) throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventPublisherService.setTraceEnabled(eventPublisherName, axisConfiguration, flag);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public void deployDefaultEventSender(String streamId) throws AxisFault {
        EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventPublisherService.deployDefaultEventSender(streamId, axisConfiguration);
        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    private EventOutputPropertyDto[] getEventPropertyDtoArray(
            List<EventOutputProperty> eventOutputPropertyList) {

        if (eventOutputPropertyList != null && eventOutputPropertyList.size() > 0) {
            EventOutputPropertyDto[] eventOutputPropertyDtos = new EventOutputPropertyDto[eventOutputPropertyList.size()];
            int index = 0;
            Iterator<EventOutputProperty> outputPropertyIterator = eventOutputPropertyList.iterator();
            while (outputPropertyIterator.hasNext()) {
                EventOutputProperty eventOutputProperty = outputPropertyIterator.next();
                eventOutputPropertyDtos[index] = new EventOutputPropertyDto(eventOutputProperty.getName(), eventOutputProperty.getValueOf(), eventOutputProperty.getType().toString().toLowerCase());
                index++;
            }

            return eventOutputPropertyDtos;
        }
        return null;
    }

    private EventPublisherPropertyDto[] getOutputEventPublisherMessageConfiguration(
            Map<String, String> messageProperties, String eventAdaptorType) {

        OutputEventAdaptorService outputEventAdaptorService = EventPublisherAdminServiceValueHolder.getOutputEventAdaptorService();
        List<Property> outputMessagePropertyList = outputEventAdaptorService.getEventAdaptorDto(eventAdaptorType).getAdaptorPropertyList();
        if (outputMessagePropertyList != null) {
            EventPublisherPropertyDto[] eventPublisherPropertyDtoArray = new EventPublisherPropertyDto[outputMessagePropertyList.size()];
            int index = 0;
            for (Property property : outputMessagePropertyList) {
                // create output event property
                eventPublisherPropertyDtoArray[index] = new EventPublisherPropertyDto(property.getPropertyName(),
                        messageProperties.get(property.getPropertyName()));
                // set output event property parameters
                eventPublisherPropertyDtoArray[index].setSecured(property.isSecured());
                eventPublisherPropertyDtoArray[index].setRequired(property.isRequired());
                eventPublisherPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                eventPublisherPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                eventPublisherPropertyDtoArray[index].setHint(property.getHint());
                eventPublisherPropertyDtoArray[index].setOptions(property.getOptions());

                index++;
            }
            return eventPublisherPropertyDtoArray;
        }
        return new EventPublisherPropertyDto[0];
    }

    private boolean checkStreamAttributeValidity(List<String> outputEventAttributes,
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


            if (outputEventAttributes.size() > 0) {
                if (inComingStreamAttributes.containsAll(outputEventAttributes)) {
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

    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains("{{") && text.indexOf("}}") > 0) {
            mappingTextList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return mappingTextList;
    }

    private boolean checkEventPublisherValidity(String eventPublisherName) throws AxisFault {
        try {
            EventPublisherService eventPublisherService = EventPublisherAdminServiceValueHolder.getEventPublisherService();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<EventPublisherConfiguration> eventPublisherConfigurationList = null;
            eventPublisherConfigurationList = eventPublisherService.getAllActiveEventPublisherConfiguration(axisConfiguration);
            Iterator eventPublisherConfigurationIterator = eventPublisherConfigurationList.iterator();
            while (eventPublisherConfigurationIterator.hasNext()) {

                EventPublisherConfiguration eventPublisherConfiguration = (EventPublisherConfiguration) eventPublisherConfigurationIterator.next();
                if (eventPublisherConfiguration.getEventPublisherName().equalsIgnoreCase(eventPublisherName)) {
                    return false;
                }
            }

        } catch (EventPublisherConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return true;
    }

    private boolean validateRegistrySource(String fromData) {

        return !fromData.equalsIgnoreCase("inline");
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

        throw new AxisFault("Output Stream attributes are not matching with input stream definition");

    }

}