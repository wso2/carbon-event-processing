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
package org.wso2.carbon.event.notifier.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.admin.internal.util.EventNotifierAdminServiceValueHolder;
import org.wso2.carbon.event.notifier.admin.internal.util.PropertyAttributeTypeConstants;
import org.wso2.carbon.event.notifier.core.EventNotifierService;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.Property;
import org.wso2.carbon.event.notifier.core.config.*;
import org.wso2.carbon.event.notifier.core.config.mapping.*;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.internal.config.EventOutputProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventNotifierAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventNotifierAdminService.class);

    public EventNotifierConfigurationInfoDto[] getAllActiveEventNotifierConfiguration()
            throws AxisFault {

        try {
            EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event notifier configurations
            List<EventNotifierConfiguration> eventNotifierConfigurationList;
            eventNotifierConfigurationList = eventNotifierService.getAllActiveEventNotifierConfiguration(axisConfiguration);

            if (eventNotifierConfigurationList != null) {
                // create event notifier configuration details array
                EventNotifierConfigurationInfoDto[] eventNotifierConfigurationInfoDtoArray = new
                        EventNotifierConfigurationInfoDto[eventNotifierConfigurationList.size()];
                for (int index = 0; index < eventNotifierConfigurationInfoDtoArray.length; index++) {
                    EventNotifierConfiguration eventNotifierConfiguration = eventNotifierConfigurationList.get(index);
                    String eventNotifierName = eventNotifierConfiguration.getEventNotifierName();
                    String mappingType = eventNotifierConfiguration.getOutputMapping().getMappingType();
                    String outputEndpointType = eventNotifierConfiguration.getEndpointAdaptorConfiguration().getEndpointType();
                    String streamNameWithVersion = eventNotifierConfiguration.getFromStreamName() + ":" + eventNotifierConfiguration.getFromStreamVersion();


                    eventNotifierConfigurationInfoDtoArray[index] = new EventNotifierConfigurationInfoDto();
                    eventNotifierConfigurationInfoDtoArray[index].setEventNotifierName(eventNotifierName);
                    eventNotifierConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventNotifierConfigurationInfoDtoArray[index].setOutputEndpointType(outputEndpointType);
                    eventNotifierConfigurationInfoDtoArray[index].setInputStreamId(streamNameWithVersion);
                    eventNotifierConfigurationInfoDtoArray[index].setEnableStats(eventNotifierConfiguration.isEnableStatistics());
                    eventNotifierConfigurationInfoDtoArray[index].setEnableTracing(eventNotifierConfiguration.isEnableTracing());
                }
                return eventNotifierConfigurationInfoDtoArray;
            } else {
                return new EventNotifierConfigurationInfoDto[0];
            }
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventNotifierConfigurationInfoDto[] getAllStreamSpecificActiveEventNotifierConfiguration(
            String streamId)
            throws AxisFault {

        try {
            EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

            AxisConfiguration axisConfiguration = getAxisConfig();

            // get event notifier configurations
            List<EventNotifierConfiguration> eventNotifierConfigurationList;
            eventNotifierConfigurationList = eventNotifierService.getAllActiveEventNotifierConfiguration(axisConfiguration, streamId);

            if (eventNotifierConfigurationList != null) {
                // create event notifier configuration details array
                EventNotifierConfigurationInfoDto[] eventNotifierConfigurationInfoDtoArray = new
                        EventNotifierConfigurationInfoDto[eventNotifierConfigurationList.size()];
                for (int index = 0; index < eventNotifierConfigurationInfoDtoArray.length; index++) {
                    EventNotifierConfiguration eventNotifierConfiguration = eventNotifierConfigurationList.get(index);
                    String eventNotifierName = eventNotifierConfiguration.getEventNotifierName();
                    String mappingType = eventNotifierConfiguration.getOutputMapping().getMappingType();
                    String outputEndpointType = eventNotifierConfiguration.getEndpointAdaptorConfiguration().getEndpointType();

                    eventNotifierConfigurationInfoDtoArray[index] = new EventNotifierConfigurationInfoDto();
                    eventNotifierConfigurationInfoDtoArray[index].setEventNotifierName(eventNotifierName);
                    eventNotifierConfigurationInfoDtoArray[index].setMappingType(mappingType);
                    eventNotifierConfigurationInfoDtoArray[index].setOutputEndpointType(outputEndpointType);
                    eventNotifierConfigurationInfoDtoArray[index].setEnableStats(eventNotifierConfiguration.isEnableStatistics());
                    eventNotifierConfigurationInfoDtoArray[index].setEnableTracing(eventNotifierConfiguration.isEnableTracing());
                }
                return eventNotifierConfigurationInfoDtoArray;
            } else {
                return new EventNotifierConfigurationInfoDto[0];
            }
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventNotifierConfigurationFileDto[] getAllInactiveEventNotifierConfiguration()
            throws AxisFault {

        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        List<EventNotifierConfigurationFile> eventNotifierConfigurationFileList = eventNotifierService.getAllInactiveEventNotifierConfiguration(axisConfiguration);
        if (eventNotifierConfigurationFileList != null) {

            // create event notifier file details array
            EventNotifierConfigurationFileDto[] eventNotifierFileDtoArray = new
                    EventNotifierConfigurationFileDto[eventNotifierConfigurationFileList.size()];

            for (int index = 0; index < eventNotifierFileDtoArray.length; index++) {
                EventNotifierConfigurationFile eventNotifierConfigurationFile = eventNotifierConfigurationFileList.get(index);
                String fileName = eventNotifierConfigurationFile.getFileName();
                String eventNotifierName = eventNotifierConfigurationFile.getEventNotifierName();
                String statusMsg = eventNotifierConfigurationFile.getDeploymentStatusMessage();
                if (eventNotifierConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventNotifierConfigurationFile.getDependency() + "]";
                }

                eventNotifierFileDtoArray[index] = new EventNotifierConfigurationFileDto(fileName, eventNotifierName, statusMsg);
            }
            return eventNotifierFileDtoArray;
        } else {
            return new EventNotifierConfigurationFileDto[0];
        }
    }

    public EventNotifierConfigurationDto getActiveEventNotifierConfiguration(
            String eventNotifierName) throws AxisFault {

        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        AxisConfiguration axisConfiguration = getAxisConfig();

        try {
            EventNotifierConfiguration eventNotifierConfiguration = eventNotifierService.getActiveEventNotifierConfiguration(eventNotifierName, tenantId);
            if (eventNotifierConfiguration != null) {
                EventNotifierConfigurationDto eventNotifierConfigurationDto = new EventNotifierConfigurationDto();
                eventNotifierConfigurationDto.setEventNotifierName(eventNotifierConfiguration.getEventNotifierName());
                String streamNameWithVersion = eventNotifierConfiguration.getFromStreamName() + ":" + eventNotifierConfiguration.getFromStreamVersion();
                eventNotifierConfigurationDto.setFromStreamNameWithVersion(streamNameWithVersion);
                eventNotifierConfigurationDto.setStreamDefinition(getStreamAttributes(eventNotifierService.getStreamDefinition(streamNameWithVersion, axisConfiguration)));

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = eventNotifierConfiguration.getEndpointAdaptorConfiguration();
                if (endpointAdaptorConfiguration != null) {
                    EndpointPropertyConfigurationDto endpointPropertyConfigurationDto = new EndpointPropertyConfigurationDto();
                    endpointPropertyConfigurationDto.setEventAdaptorType(endpointAdaptorConfiguration.getEndpointType());
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = endpointAdaptorConfiguration.getOutputAdaptorConfiguration();
                    if (internalOutputEventAdaptorConfiguration != null && internalOutputEventAdaptorConfiguration.getProperties().size() > 0) {
                        EventNotifierPropertyDto[] eventNotifierPropertyDtos = getOutputEventNotifierMessageConfiguration(internalOutputEventAdaptorConfiguration.getProperties(), endpointAdaptorConfiguration.getEndpointType());
                        endpointPropertyConfigurationDto.setOutputEventAdaptorConfiguration(eventNotifierPropertyDtos);
                    }

                    eventNotifierConfigurationDto.setEndpointPropertyConfigurationDto(endpointPropertyConfigurationDto);
                }

                if (eventNotifierConfiguration.getOutputMapping().getMappingType().equals(EventNotifierConstants.EF_JSON_MAPPING_TYPE)) {
                    JSONOutputMapping jsonOutputMapping = (JSONOutputMapping) eventNotifierConfiguration.getOutputMapping();
                    JSONOutputMappingDto jsonOutputMappingDto = new JSONOutputMappingDto();
                    jsonOutputMappingDto.setMappingText(jsonOutputMapping.getMappingText());
                    jsonOutputMappingDto.setRegistryResource(jsonOutputMapping.isRegistryResource());
                    eventNotifierConfigurationDto.setJsonOutputMappingDto(jsonOutputMappingDto);
                    eventNotifierConfigurationDto.setMappingType("json");
                } else if (eventNotifierConfiguration.getOutputMapping().getMappingType().equals(EventNotifierConstants.EF_XML_MAPPING_TYPE)) {
                    XMLOutputMapping xmlOutputMapping = (XMLOutputMapping) eventNotifierConfiguration.getOutputMapping();
                    XMLOutputMappingDto xmlOutputMappingDto = new XMLOutputMappingDto();
                    xmlOutputMappingDto.setMappingXMLText(xmlOutputMapping.getMappingXMLText());
                    xmlOutputMappingDto.setRegistryResource(xmlOutputMapping.isRegistryResource());
                    eventNotifierConfigurationDto.setXmlOutputMappingDto(xmlOutputMappingDto);
                    eventNotifierConfigurationDto.setMappingType("xml");
                } else if (eventNotifierConfiguration.getOutputMapping().getMappingType().equals(EventNotifierConstants.EF_TEXT_MAPPING_TYPE)) {
                    TextOutputMapping textOutputMapping = (TextOutputMapping) eventNotifierConfiguration.getOutputMapping();
                    TextOutputMappingDto textOutputMappingDto = new TextOutputMappingDto();
                    textOutputMappingDto.setMappingText(textOutputMapping.getMappingText());
                    textOutputMappingDto.setRegistryResource(textOutputMapping.isRegistryResource());
                    eventNotifierConfigurationDto.setTextOutputMappingDto(textOutputMappingDto);
                    eventNotifierConfigurationDto.setMappingType("text");
                } else if (eventNotifierConfiguration.getOutputMapping().getMappingType().equals(EventNotifierConstants.EF_MAP_MAPPING_TYPE)) {
                    MapOutputMapping mapOutputMapping = (MapOutputMapping) eventNotifierConfiguration.getOutputMapping();
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

                    eventNotifierConfigurationDto.setMapOutputMappingDto(mapOutputMappingDto);
                    eventNotifierConfigurationDto.setMappingType("map");
                } else if (eventNotifierConfiguration.getOutputMapping().getMappingType().equals(EventNotifierConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
                    WSO2EventOutputMapping wso2EventOutputMapping = (WSO2EventOutputMapping) eventNotifierConfiguration.getOutputMapping();
                    WSO2EventOutputMappingDto wso2EventOutputMappingDto = new WSO2EventOutputMappingDto();
                    List<EventOutputProperty> metaOutputPropertyList = wso2EventOutputMapping.getMetaWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> correlationOutputPropertyList = wso2EventOutputMapping.getCorrelationWSO2EventOutputPropertyConfiguration();
                    List<EventOutputProperty> payloadOutputPropertyList = wso2EventOutputMapping.getPayloadWSO2EventOutputPropertyConfiguration();

                    wso2EventOutputMappingDto.setMetaWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(metaOutputPropertyList));
                    wso2EventOutputMappingDto.setCorrelationWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(correlationOutputPropertyList));
                    wso2EventOutputMappingDto.setPayloadWSO2EventOutputPropertyConfigurationDto(getEventPropertyDtoArray(payloadOutputPropertyList));

                    eventNotifierConfigurationDto.setWso2EventOutputMappingDto(wso2EventOutputMappingDto);
                    eventNotifierConfigurationDto.setMappingType("wso2event");
                }

                return eventNotifierConfigurationDto;
            }

        } catch (EventNotifierConfigurationException ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return null;
    }

    public String getActiveEventNotifierConfigurationContent(String eventNotifierName)
            throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            return eventNotifierService.getActiveEventNotifierConfigurationContent(eventNotifierName, axisConfiguration);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventNotifierConfigurationContent(String fileName)
            throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        try {
            String eventNotifierConfigurationFile = eventNotifierService.getInactiveEventNotifierConfigurationContent(fileName, getAxisConfig());
            return eventNotifierConfigurationFile.trim();
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventNotifierPropertyDto[] getEventNotifierAdaptorProperties(String endpointAdaptorType)
            throws AxisFault {

        OutputEventAdaptorService eventAdaptorService = EventNotifierAdminServiceValueHolder.getOutputEventAdaptorService();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OutputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(endpointAdaptorType);

            List<Property> propertyList = eventAdaptorDto.getAdaptorPropertyList();
            if (propertyList != null) {
                EventNotifierPropertyDto[] eventNotifierPropertyDtoArray = new EventNotifierPropertyDto[propertyList.size()];
                for (int index = 0; index < eventNotifierPropertyDtoArray.length; index++) {
                    Property property = propertyList.get(index);
                    // set event fotifier property parameters
                    eventNotifierPropertyDtoArray[index] = new EventNotifierPropertyDto(property.getPropertyName(), "");
                    eventNotifierPropertyDtoArray[index].setRequired(property.isRequired());
                    eventNotifierPropertyDtoArray[index].setSecured(property.isSecured());
                    eventNotifierPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                    eventNotifierPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                    eventNotifierPropertyDtoArray[index].setHint(property.getHint());
                    eventNotifierPropertyDtoArray[index].setOptions(property.getOptions());
                }
                return eventNotifierPropertyDtoArray;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new AxisFault(ex.getMessage());
        }
        return new EventNotifierPropertyDto[0];
    }

    public void undeployActiveEventNotifierConfiguration(String eventNotifierName)
            throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventNotifierService.undeployActiveEventNotifierConfiguration(eventNotifierName, axisConfiguration);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void undeployInactiveEventNotifierConfiguration(String fileName)
            throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            eventNotifierService.undeployInactiveEventNotifierConfiguration(fileName, axisConfiguration);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editActiveEventNotifierConfiguration(String eventNotifierConfiguration,
                                                      String eventNotifierName)
            throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventNotifierService.editActiveEventNotifierConfiguration(eventNotifierConfiguration, eventNotifierName, axisConfiguration);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editInactiveEventNotifierConfiguration(
            String eventNotifierConfiguration,
            String fileName)
            throws AxisFault {

        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventNotifierService.editInactiveEventNotifierConfiguration(eventNotifierConfiguration, fileName, axisConfiguration);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void deployEventNotifierConfiguration(String eventNotifierConfigXml)
            throws AxisFault {
        try {
            EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
            eventNotifierService.deployEventNotifierConfiguration(eventNotifierConfigXml, getAxisConfig());
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void deployWSO2EventNotifierConfiguration(String eventNotifierName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorType,
                                                      EventOutputPropertyConfigurationDto[] metaData,
                                                      EventOutputPropertyConfigurationDto[] correlationData,
                                                      EventOutputPropertyConfigurationDto[] payloadData,
                                                      PropertyDto[] outputPropertyConfiguration,
                                                      boolean mappingEnabled)
            throws AxisFault {

        if (checkEventNotifierValidity(eventNotifierName)) {
            try {
                EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

                EventNotifierConfiguration eventNotifierConfiguration = new EventNotifierConfiguration();

                eventNotifierConfiguration.setEventNotifierName(eventNotifierName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventNotifierConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventNotifierConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                StreamDefinition streamDefinition = eventNotifierService.getStreamDefinition(streamNameWithVersion, axisConfiguration);

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventNotifierName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventNotifierProperty : outputPropertyConfiguration) {
                        if (!eventNotifierProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventNotifierProperty.getKey().trim(), eventNotifierProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

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

                eventNotifierConfiguration.setOutputMapping(wso2EventOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, streamDefinition)) {
                    eventNotifierService.deployEventNotifierConfiguration(eventNotifierConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventNotifierConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventNotifierName + " is already registered for this tenant");
        }

    }

    public void deployTextEventNotifierConfiguration(String eventNotifierName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorType,
                                                      String textData,
                                                      PropertyDto[] outputPropertyConfiguration,
                                                      String dataFrom, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventNotifierValidity(eventNotifierName)) {
            try {
                EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

                EventNotifierConfiguration eventNotifierConfiguration = new EventNotifierConfiguration();

                eventNotifierConfiguration.setEventNotifierName(eventNotifierName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventNotifierConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventNotifierConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventNotifierName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventNotifierProperty : outputPropertyConfiguration) {
                        if (!eventNotifierProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventNotifierProperty.getKey().trim(), eventNotifierProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                TextOutputMapping textOutputMapping = new TextOutputMapping();
                textOutputMapping.setCustomMappingEnabled(mappingEnabled);

                textOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                textOutputMapping.setMappingText(textData);

                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    if (dataFrom.equalsIgnoreCase("registry")) {
                        textData = eventNotifierService.getRegistryResourceContent(textData, tenantId);
                    }
                    outputEventAttributes = getOutputMappingPropertyList(textData);
                }
                eventNotifierConfiguration.setOutputMapping(textOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventNotifierService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventNotifierService.deployEventNotifierConfiguration(eventNotifierConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventNotifierConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventNotifierName + " is already registered for this tenant");
        }

    }

    public void deployXmlEventNotifierConfiguration(String eventNotifierName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorType,
                                                     String textData,
                                                     PropertyDto[] outputPropertyConfiguration,
                                                     String dataFrom, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventNotifierValidity(eventNotifierName)) {
            try {
                EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

                EventNotifierConfiguration eventNotifierConfiguration = new EventNotifierConfiguration();

                eventNotifierConfiguration.setEventNotifierName(eventNotifierName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventNotifierConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventNotifierConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventNotifierName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventNotifierProperty : outputPropertyConfiguration) {
                        if (!eventNotifierProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventNotifierProperty.getKey().trim(), eventNotifierProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                XMLOutputMapping xmlOutputMapping = new XMLOutputMapping();
                xmlOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    xmlOutputMapping.setMappingXMLText(textData);
                    xmlOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                    outputEventAttributes = getOutputMappingPropertyList(textData);
                }

                eventNotifierConfiguration.setOutputMapping(xmlOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventNotifierService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventNotifierService.deployEventNotifierConfiguration(eventNotifierConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventNotifierConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            throw new AxisFault(eventNotifierName + " is already registered for this tenant");
        }

    }

    public void deployMapEventNotifierConfiguration(String eventNotifierName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorType,
                                                     EventOutputPropertyConfigurationDto[] mapData,
                                                     PropertyDto[] outputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {

        if (checkEventNotifierValidity(eventNotifierName)) {
            try {
                EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

                EventNotifierConfiguration eventNotifierConfiguration = new EventNotifierConfiguration();

                eventNotifierConfiguration.setEventNotifierName(eventNotifierName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventNotifierConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventNotifierConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventNotifierName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventNotifierProperty : outputPropertyConfiguration) {
                        if (!eventNotifierProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventNotifierProperty.getKey().trim(), eventNotifierProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

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

                eventNotifierConfiguration.setOutputMapping(mapOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventNotifierService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventNotifierService.deployEventNotifierConfiguration(eventNotifierConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventNotifierConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventNotifierName + " is already registered for this tenant");
        }

    }

    public void deployJsonEventNotifierConfiguration(String eventNotifierName,
                                                      String streamNameWithVersion,
                                                      String eventAdaptorType,
                                                      String jsonData,
                                                      PropertyDto[] outputPropertyConfiguration,
                                                      String dataFrom, boolean mappingEnabled)
            throws AxisFault {

        if (checkEventNotifierValidity(eventNotifierName)) {
            try {
                EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();

                EventNotifierConfiguration eventNotifierConfiguration = new EventNotifierConfiguration();

                eventNotifierConfiguration.setEventNotifierName(eventNotifierName);
                String[] fromStreamProperties = streamNameWithVersion.split(":");
                eventNotifierConfiguration.setFromStreamName(fromStreamProperties[0]);
                eventNotifierConfiguration.setFromStreamVersion(fromStreamProperties[1]);

                AxisConfiguration axisConfiguration = getAxisConfig();

                EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
                endpointAdaptorConfiguration.setEndpointAdaptorName(eventNotifierName);
                endpointAdaptorConfiguration.setEndpointType(eventAdaptorType);

                // add output message property configuration to the map
                if (outputPropertyConfiguration != null && outputPropertyConfiguration.length != 0) {
                    InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();

                    for (PropertyDto eventNotifierProperty : outputPropertyConfiguration) {
                        if (!eventNotifierProperty.getValue().trim().equals("")) {
                            internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(eventNotifierProperty.getKey().trim(), eventNotifierProperty.getValue().trim());
                        }
                    }
                    endpointAdaptorConfiguration.setOutputAdaptorConfiguration(internalOutputEventAdaptorConfiguration);
                }

                eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);

                JSONOutputMapping jsonOutputMapping = new JSONOutputMapping();

                jsonOutputMapping.setCustomMappingEnabled(mappingEnabled);
                List<String> outputEventAttributes = new ArrayList<String>();

                if (mappingEnabled) {
                    jsonOutputMapping.setRegistryResource(validateRegistrySource(dataFrom));
                    jsonOutputMapping.setMappingText(jsonData);
                    outputEventAttributes = getOutputMappingPropertyList(jsonData);
                }

                eventNotifierConfiguration.setOutputMapping(jsonOutputMapping);

                if (checkStreamAttributeValidity(outputEventAttributes, eventNotifierService.getStreamDefinition(streamNameWithVersion, axisConfiguration))) {
                    eventNotifierService.deployEventNotifierConfiguration(eventNotifierConfiguration, axisConfiguration);
                } else {
                    throw new AxisFault("Output Stream attributes are not matching with input stream definition ");
                }

            } catch (EventNotifierConfigurationException ex) {
                log.error(ex.getMessage(), ex);
                throw new AxisFault(ex.getMessage());
            }
        } else {
            throw new AxisFault(eventNotifierName + " is already registered for this tenant");
        }

    }

    public void setStatisticsEnabled(String eventNotifierName, boolean flag) throws AxisFault {

        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventNotifierService.setStatisticsEnabled(eventNotifierName, axisConfiguration, flag);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setTracingEnabled(String eventNotifierName, boolean flag) throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventNotifierService.setTraceEnabled(eventNotifierName, axisConfiguration, flag);
        } catch (EventNotifierConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public void deployDefaultEventSender(String streamId) throws AxisFault {
        EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventNotifierService.deployDefaultEventSender(streamId, axisConfiguration);
        } catch (EventNotifierConfigurationException e) {
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

    private EventNotifierPropertyDto[] getOutputEventNotifierMessageConfiguration(
            Map<String, String> messageProperties, String eventAdaptorType) {

        OutputEventAdaptorService outputEventAdaptorService = EventNotifierAdminServiceValueHolder.getOutputEventAdaptorService();
        List<Property> outputMessagePropertyList = outputEventAdaptorService.getEventAdaptorDto(eventAdaptorType).getAdaptorPropertyList();
        if (outputMessagePropertyList != null) {
            EventNotifierPropertyDto[] eventNotifierPropertyDtoArray = new EventNotifierPropertyDto[outputMessagePropertyList.size()];
            int index = 0;
            for (Property property : outputMessagePropertyList) {
                // create output event property
                eventNotifierPropertyDtoArray[index] = new EventNotifierPropertyDto(property.getPropertyName(),
                        messageProperties.get(property.getPropertyName()));
                // set output event property parameters
                eventNotifierPropertyDtoArray[index].setSecured(property.isSecured());
                eventNotifierPropertyDtoArray[index].setRequired(property.isRequired());
                eventNotifierPropertyDtoArray[index].setDisplayName(property.getDisplayName());
                eventNotifierPropertyDtoArray[index].setDefaultValue(property.getDefaultValue());
                eventNotifierPropertyDtoArray[index].setHint(property.getHint());
                eventNotifierPropertyDtoArray[index].setOptions(property.getOptions());

                index++;
            }
            return eventNotifierPropertyDtoArray;
        }
        return new EventNotifierPropertyDto[0];
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

    private boolean checkEventNotifierValidity(String eventNotifierName) throws AxisFault {
        try {
            EventNotifierService eventNotifierService = EventNotifierAdminServiceValueHolder.getEventNotifierService();
            AxisConfiguration axisConfiguration = getAxisConfig();

            List<EventNotifierConfiguration> eventNotifierConfigurationList = null;
            eventNotifierConfigurationList = eventNotifierService.getAllActiveEventNotifierConfiguration(axisConfiguration);
            Iterator eventNotifierConfigurationIterator = eventNotifierConfigurationList.iterator();
            while (eventNotifierConfigurationIterator.hasNext()) {

                EventNotifierConfiguration eventNotifierConfiguration = (EventNotifierConfiguration) eventNotifierConfigurationIterator.next();
                if (eventNotifierConfiguration.getEventNotifierName().equalsIgnoreCase(eventNotifierName)) {
                    return false;
                }
            }

        } catch (EventNotifierConfigurationException e) {
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