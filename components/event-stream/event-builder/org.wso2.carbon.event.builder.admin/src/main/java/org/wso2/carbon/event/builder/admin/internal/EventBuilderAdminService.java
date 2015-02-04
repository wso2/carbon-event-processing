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
package org.wso2.carbon.event.builder.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.ds.EventBuilderAdminServiceValueHolder;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConverter;
import org.wso2.carbon.event.builder.admin.internal.util.DtoConverterFactory;
import org.wso2.carbon.event.builder.admin.internal.util.dto.converter.*;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfigurationFile;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.MessageDto;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;

import java.util.List;

public class EventBuilderAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EventBuilderAdminService.class);
    private DtoConverterFactory dtoConverterFactory;

    public EventBuilderAdminService() {
        dtoConverterFactory = new DtoConverterFactory();
    }

    /**
     * @return
     * @throws AxisFault
     */
    public EventBuilderConfigurationInfoDto[] getAllActiveEventBuilderConfigurations()
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurationList = eventBuilderService.getAllActiveEventBuilderConfigurations(tenantId);
        if (eventBuilderConfigurationList != null && !eventBuilderConfigurationList.isEmpty()) {
            EventBuilderConfigurationInfoDto[] eventBuilderConfigurationInfoDtos = new EventBuilderConfigurationInfoDto[eventBuilderConfigurationList.size()];
            for (int i = 0; i < eventBuilderConfigurationList.size(); i++) {
                EventBuilderConfiguration eventBuilderConfiguration = eventBuilderConfigurationList.get(i);
                DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());
                EventBuilderConfigurationInfoDto eventBuilderConfigurationDto;
                try {
                    eventBuilderConfigurationDto = dtoConverter.getEventBuilderConfigurationInfoDto(eventBuilderConfiguration);
                } catch (EventBuilderAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventBuilderConfigurationInfoDtos[i] = eventBuilderConfigurationDto;
            }

            return eventBuilderConfigurationInfoDtos;
        }

        return new EventBuilderConfigurationInfoDto[0];
    }

    public EventBuilderConfigurationDto[] getAllStreamSpecificActiveEventBuilderConfiguration(
            String streamId)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfiguration> eventBuilderConfigurationList = eventBuilderService.getAllStreamSpecificActiveEventBuilderConfigurations(streamId, tenantId);
        if (eventBuilderConfigurationList != null && !eventBuilderConfigurationList.isEmpty()) {
            EventBuilderConfigurationDto[] eventBuilderConfigurationDtos = new EventBuilderConfigurationDto[eventBuilderConfigurationList.size()];
            for (int i = 0; i < eventBuilderConfigurationList.size(); i++) {
                EventBuilderConfiguration eventBuilderConfiguration = eventBuilderConfigurationList.get(i);
                DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());
                EventBuilderConfigurationDto eventBuilderConfigurationDto;
                try {
                    eventBuilderConfigurationDto = dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
                } catch (EventBuilderAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventBuilderConfigurationDtos[i] = eventBuilderConfigurationDto;
            }

            return eventBuilderConfigurationDtos;
        }

        return new EventBuilderConfigurationDto[0];
    }

    /**
     * @return
     */
    public EventBuilderConfigurationFileDto[] getAllInactiveEventBuilderConfigurations() {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        List<EventBuilderConfigurationFile> eventBuilderConfigurationFileList = eventBuilderService.getAllInactiveEventBuilderConfigurations(getAxisConfig());
        if (eventBuilderConfigurationFileList != null) {
            EventBuilderConfigurationFileDto[] eventBuilderConfigurationFileDtos = new EventBuilderConfigurationFileDto[eventBuilderConfigurationFileList.size()];
            int i = 0;
            for (EventBuilderConfigurationFile eventBuilderConfigurationFile : eventBuilderConfigurationFileList) {
                String statusMsg = eventBuilderConfigurationFile.getDeploymentStatusMessage();
                if (eventBuilderConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventBuilderConfigurationFile.getDependency() + "]";
                }
                eventBuilderConfigurationFileDtos[i++] = new EventBuilderConfigurationFileDto(
                        eventBuilderConfigurationFile.getFileName(), eventBuilderConfigurationFile.getEventBuilderName(), statusMsg);
            }
            return eventBuilderConfigurationFileDtos;
        }
        return new EventBuilderConfigurationFileDto[0];
    }

    public EventBuilderConfigurationDto getActiveEventBuilderConfiguration(String eventBuilderName)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        EventBuilderConfiguration eventBuilderConfiguration = eventBuilderService.getActiveEventBuilderConfiguration(eventBuilderName, tenantId);
        DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventBuilderConfiguration.getInputMapping().getMappingType());

        try {
            return dtoConverter.fromEventBuilderConfiguration(eventBuilderConfiguration);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventBuilderConfigurationContent(String filename) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        String eventBuilderConfigurationContent;
        try {
            eventBuilderConfigurationContent = eventBuilderService.getInactiveEventBuilderConfigurationContent(filename, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return eventBuilderConfigurationContent;
    }

    public String getActiveEventBuilderConfigurationContent(String eventBuilderName)
            throws AxisFault {
        try {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            return eventBuilderService.getActiveEventBuilderConfigurationContent(eventBuilderName, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventBuilderMessagePropertyDto[] getEventBuilderMessageProperties(
            String inputEventAdaptorName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        InputEventAdaptorService inputEventAdaptorService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorService();
        InputEventAdaptorManagerService inputEventAdaptorManagerService = EventBuilderAdminServiceValueHolder.getInputEventAdaptorManagerService();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = null;
        try {
            inputEventAdaptorConfiguration = inputEventAdaptorManagerService.getActiveInputEventAdaptorConfiguration(inputEventAdaptorName, tenantId);
        } catch (InputEventAdaptorManagerConfigurationException e) {
            String errorMsg = "Error retrieving input event adaptor configuration with name '" + inputEventAdaptorName + "' " + e.getMessage();
            log.error(errorMsg, e);
        }
        if (inputEventAdaptorConfiguration != null) {
            MessageDto messageDto = inputEventAdaptorService.getEventMessageDto(inputEventAdaptorConfiguration.getType());
            DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(null);
            return dtoConverter.getEventBuilderPropertiesFrom(messageDto, null);
        }

        return new EventBuilderMessagePropertyDto[0];
    }

    /**
     * Deploys an event builder by parsing the passed in XML configuration
     *
     * @param eventBuilderConfigXml event builder configuration xml syntax as a string
     * @throws AxisFault
     */
    public void deployEventBuilderConfiguration(String eventBuilderConfigXml)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty()) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfigXml, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    /**
     * Deploys an event builder with mapping type of Wso2Event
     *
     * @param eventBuilderName           name of the event builder
     * @param streamNameWithVersion      the stream name with version for the stream exported from the event builder
     * @param eventAdaptorName           the name of the input event adaptor
     * @param eventAdaptorType           the type of the event adaptor (e.g. wso2event, jms, http)
     * @param metaData                   meta data attributes
     * @param correlationData            correlation data attributes
     * @param payloadData                payload data attributes
     * @param inputPropertyConfiguration input message properties
     * @param mappingEnabled             is custom mapping enabled
     * @throws AxisFault
     */
    public void deployWso2EventBuilderConfiguration(String eventBuilderName,
                                                    String streamNameWithVersion,
                                                    String eventAdaptorName,
                                                    String eventAdaptorType,
                                                    EventInputPropertyConfigurationDto[] metaData,
                                                    EventInputPropertyConfigurationDto[] correlationData,
                                                    EventInputPropertyConfigurationDto[] payloadData,
                                                    PropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        Wso2EventDtoConverter wso2EventDtoConverter = new Wso2EventDtoConverter();
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = wso2EventDtoConverter.toEventBuilderConfiguration(eventBuilderName, streamNameWithVersion,
                    eventAdaptorName, eventAdaptorType, metaData, correlationData, payloadData, inputPropertyConfiguration, mappingEnabled);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventBuilderConfiguration != null) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployXmlEventBuilderConfiguration(String eventBuilderName,
                                                   String streamNameWithVersion,
                                                   String eventAdaptorName,
                                                   String eventAdaptorType,
                                                   EventInputPropertyConfigurationDto[] xpathExpressions,
                                                   PropertyDto[] inputPropertyConfiguration,
                                                   PropertyDto[] xpathDefinitions,
                                                   String parentSelectorXpath,
                                                   boolean mappingEnabled)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        XmlDtoConverter xmlDtoConverter = new XmlDtoConverter();
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = xmlDtoConverter.toEventBuilderConfiguration(eventBuilderName, streamNameWithVersion,
                    eventAdaptorName, eventAdaptorType, xpathExpressions, inputPropertyConfiguration, xpathDefinitions, parentSelectorXpath, mappingEnabled);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventBuilderConfiguration != null) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployJsonEventBuilderConfiguration(String eventBuilderName,
                                                    String streamNameWithVersion,
                                                    String eventAdaptorName,
                                                    String eventAdaptorType,
                                                    EventInputPropertyConfigurationDto[] jsonPathExpressions,
                                                    PropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        JsonDtoConverter jsonDtoConverter = new JsonDtoConverter();
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = jsonDtoConverter.toEventBuilderConfiguration(eventBuilderName, streamNameWithVersion,
                    eventAdaptorName, eventAdaptorType, jsonPathExpressions, inputPropertyConfiguration, mappingEnabled);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventBuilderConfiguration != null) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployMapEventBuilderConfiguration(String eventBuilderName,
                                                   String streamNameWithVersion,
                                                   String eventAdaptorName,
                                                   String eventAdaptorType,
                                                   EventInputPropertyConfigurationDto[] mappingProperties,
                                                   PropertyDto[] inputPropertyConfiguration,
                                                   boolean mappingEnabled)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        MapDtoConverter mapDtoConverter = new MapDtoConverter();
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = mapDtoConverter.toEventBuilderConfiguration(eventBuilderName, streamNameWithVersion,
                    eventAdaptorName, eventAdaptorType, mappingProperties, inputPropertyConfiguration, mappingEnabled);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventBuilderConfiguration != null) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployTextEventBuilderConfiguration(String eventBuilderName,
                                                    String streamNameWithVersion,
                                                    String eventAdaptorName,
                                                    String eventAdaptorType,
                                                    EventInputPropertyConfigurationDto[] textMappingAttributes,
                                                    PropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        TextDtoConverter textDtoConverter = new TextDtoConverter();
        EventBuilderConfiguration eventBuilderConfiguration;
        try {
            eventBuilderConfiguration = textDtoConverter.toEventBuilderConfiguration(eventBuilderName, streamNameWithVersion,
                    eventAdaptorName, eventAdaptorType, textMappingAttributes, inputPropertyConfiguration, mappingEnabled);
        } catch (EventBuilderAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventBuilderConfiguration != null) {
            try {
                eventBuilderService.deployEventBuilderConfiguration(eventBuilderConfiguration, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    /**
     * @param eventBuilderName the name of the event builder to be undeployed
     * @throws AxisFault
     */
    public void undeployActiveEventBuilderConfiguration(String eventBuilderName) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.undeployActiveEventBuilderConfiguration(eventBuilderName, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * @param filename filename of the event builder configuration file that needs to be undeployed. e.g. "wso2EventBuilder.xml"
     * @throws AxisFault
     */
    public void undeployInactiveEventBuilderConfiguration(String filename) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.undeployInactiveEventBuilderConfiguration(filename, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editActiveEventBuilderConfiguration(String originalEventBuilderName,
                                                    String eventBuilderConfigXml) throws AxisFault {
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty() && originalEventBuilderName != null && !originalEventBuilderName.isEmpty()) {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            try {
                eventBuilderService.editActiveEventBuilderConfiguration(eventBuilderConfigXml, originalEventBuilderName, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    public void editInactiveEventBuilderConfiguration(String filename, String eventBuilderConfigXml)
            throws AxisFault {
        if (eventBuilderConfigXml != null && !eventBuilderConfigXml.isEmpty() && filename != null && !filename.isEmpty()) {
            EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
            try {
                eventBuilderService.editInactiveEventBuilderConfiguration(eventBuilderConfigXml, filename, getAxisConfig());
            } catch (EventBuilderConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    public void deployDefaultEventReceiver(String streamId) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventBuilderService.deployDefaultEventBuilder(streamId, axisConfiguration);
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled) throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.setTraceEnabled(eventBuilderName, traceEnabled, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled)
            throws AxisFault {
        EventBuilderService eventBuilderService = EventBuilderAdminServiceValueHolder.getEventBuilderService();
        try {
            eventBuilderService.setStatisticsEnabled(eventBuilderName, statisticsEnabled, getAxisConfig());
        } catch (EventBuilderConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

}
