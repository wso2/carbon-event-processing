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
package org.wso2.carbon.event.receiver.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.receiver.admin.exception.EventReceiverAdminServiceException;
import org.wso2.carbon.event.receiver.admin.internal.ds.EventReceiverAdminServiceValueHolder;
import org.wso2.carbon.event.receiver.admin.internal.util.DtoConverter;
import org.wso2.carbon.event.receiver.admin.internal.util.DtoConverterFactory;
import org.wso2.carbon.event.receiver.admin.internal.util.dto.converter.*;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorDto;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.util.List;

public class EventReceiverAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EventReceiverAdminService.class);
    private DtoConverterFactory dtoConverterFactory;

    public EventReceiverAdminService() {
        dtoConverterFactory = new DtoConverterFactory();
    }

    /**
     * @return
     * @throws AxisFault
     */
    public EventReceiverConfigurationInfoDto[] getAllActiveEventReceiverConfigurations()
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfiguration> eventReceiverConfigurationList = eventReceiverService.getAllActiveEventReceiverConfigurations(tenantId);
        if (eventReceiverConfigurationList != null && !eventReceiverConfigurationList.isEmpty()) {
            EventReceiverConfigurationInfoDto[] eventReceiverConfigurationInfoDtos = new EventReceiverConfigurationInfoDto[eventReceiverConfigurationList.size()];
            for (int i = 0; i < eventReceiverConfigurationList.size(); i++) {
                EventReceiverConfiguration eventReceiverConfiguration = eventReceiverConfigurationList.get(i);
                DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventReceiverConfiguration.getInputMapping().getMappingType());
                EventReceiverConfigurationInfoDto eventReceiverConfigurationDto;
                try {
                    eventReceiverConfigurationDto = dtoConverter.getEventReceiverConfigurationInfoDto(eventReceiverConfiguration);
                } catch (EventReceiverAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventReceiverConfigurationInfoDtos[i] = eventReceiverConfigurationDto;
            }

            return eventReceiverConfigurationInfoDtos;
        }

        return new EventReceiverConfigurationInfoDto[0];
    }

    public EventReceiverConfigurationDto[] getAllStreamSpecificActiveEventReceiverConfiguration(
            String streamId)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfiguration> eventReceiverConfigurationList = eventReceiverService.getAllStreamSpecificActiveEventReceiverConfigurations(streamId, tenantId);
        if (eventReceiverConfigurationList != null && !eventReceiverConfigurationList.isEmpty()) {
            EventReceiverConfigurationDto[] eventReceiverConfigurationDtos = new EventReceiverConfigurationDto[eventReceiverConfigurationList.size()];
            for (int i = 0; i < eventReceiverConfigurationList.size(); i++) {
                EventReceiverConfiguration eventReceiverConfiguration = eventReceiverConfigurationList.get(i);
                DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventReceiverConfiguration.getInputMapping().getMappingType());
                EventReceiverConfigurationDto eventReceiverConfigurationDto;
                try {
                    eventReceiverConfigurationDto = dtoConverter.fromEventReceiverConfiguration(eventReceiverConfiguration);
                } catch (EventReceiverAdminServiceException e) {
                    throw new AxisFault(e.getMessage());
                }
                eventReceiverConfigurationDtos[i] = eventReceiverConfigurationDto;
            }

            return eventReceiverConfigurationDtos;
        }

        return new EventReceiverConfigurationDto[0];
    }

    /**
     * @return
     */
    public EventReceiverConfigurationFileDto[] getAllInactiveEventReceiverConfigurations() {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = eventReceiverService.getAllInactiveEventReceiverConfigurations(getAxisConfig());
        if (eventReceiverConfigurationFileList != null) {
            EventReceiverConfigurationFileDto[] eventReceiverConfigurationFileDtos = new EventReceiverConfigurationFileDto[eventReceiverConfigurationFileList.size()];
            int i = 0;
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFileList) {
                String statusMsg = eventReceiverConfigurationFile.getDeploymentStatusMessage();
                if (eventReceiverConfigurationFile.getDependency() != null) {
                    statusMsg = statusMsg + " [Dependency: " + eventReceiverConfigurationFile.getDependency() + "]";
                }
                eventReceiverConfigurationFileDtos[i++] = new EventReceiverConfigurationFileDto(
                        eventReceiverConfigurationFile.getFileName(), eventReceiverConfigurationFile.getEventReceiverName(), statusMsg);
            }
            return eventReceiverConfigurationFileDtos;
        }
        return new EventReceiverConfigurationFileDto[0];
    }

    public EventReceiverConfigurationDto getActiveEventReceiverConfiguration(String eventReceiverName)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        EventReceiverConfiguration eventReceiverConfiguration = eventReceiverService.getActiveEventReceiverConfiguration(eventReceiverName, tenantId);
        DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(eventReceiverConfiguration.getInputMapping().getMappingType());

        try {
            return dtoConverter.fromEventReceiverConfiguration(eventReceiverConfiguration);
        } catch (EventReceiverAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public String getInactiveEventReceiverConfigurationContent(String filename) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        String eventReceiverConfigurationContent;
        try {
            eventReceiverConfigurationContent = eventReceiverService.getInactiveEventReceiverConfigurationContent(filename, getAxisConfig());
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        return eventReceiverConfigurationContent;
    }

    public String getActiveEventReceiverConfigurationContent(String eventReceiverName)
            throws AxisFault {
        try {
            EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
            return eventReceiverService.getActiveEventReceiverConfigurationContent(eventReceiverName, getAxisConfig());
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public EventReceiverAdaptorPropertyDto[] getEventReceiverAdaptorProperties(
            String inputEventAdaptorType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        InputEventAdaptorService inputEventAdaptorService = EventReceiverAdminServiceValueHolder.getInputEventAdaptorService();
        InputEventAdaptorDto inputEventAdaptorDto = inputEventAdaptorService.getEventAdaptorDto(inputEventAdaptorType);

        if(inputEventAdaptorDto == null){
            log.error("There in no any input event adaptor type : "+inputEventAdaptorType + " found");
        }

        DtoConverter dtoConverter = dtoConverterFactory.getDtoConverter(null);
        return dtoConverter.getEventReceiverPropertiesFrom(inputEventAdaptorDto, null);

    }


    /**
     * Deploys an event receiver by parsing the passed in XML configuration
     *
     * @param eventReceiverConfigXml event receiver configuration xml syntax as a string
     * @throws AxisFault
     */
    public void deployEventReceiverConfiguration(String eventReceiverConfigXml)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        if (eventReceiverConfigXml != null && !eventReceiverConfigXml.isEmpty()) {
            try {
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfigXml, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    /**
     * Deploys an event receiver with mapping type of Wso2Event
     *
     * @param eventReceiverName          name of the event receiver
     * @param streamNameWithVersion      the stream name with version for the stream exported from the event receiver
     * @param eventAdaptorType           the type of the event adaptor (e.g. wso2event, jms, http)
     * @param metaData                   meta data attributes
     * @param correlationData            correlation data attributes
     * @param payloadData                payload data attributes
     * @param inputPropertyConfiguration input message properties
     * @param mappingEnabled             is custom mapping enabled
     * @throws AxisFault
     */
    public void deployWso2EventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorType,
                                                     EventInputPropertyConfigurationDto[] metaData,
                                                     EventInputPropertyConfigurationDto[] correlationData,
                                                     EventInputPropertyConfigurationDto[] payloadData,
                                                     PropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        Wso2EventDtoConverter wso2EventDtoConverter = new Wso2EventDtoConverter();
        EventReceiverConfiguration eventReceiverConfiguration;
        try {
            eventReceiverConfiguration = wso2EventDtoConverter.toEventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdaptorType, metaData, correlationData, payloadData, inputPropertyConfiguration, mappingEnabled);
        } catch (EventReceiverAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventReceiverConfiguration != null) {
            try {
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployXmlEventReceiverConfiguration(String eventReceiverName,
                                                    String streamNameWithVersion,
                                                    String eventAdaptorType,
                                                    EventInputPropertyConfigurationDto[] xpathExpressions,
                                                    PropertyDto[] inputPropertyConfiguration,
                                                    PropertyDto[] xpathDefinitions,
                                                    String parentSelectorXpath,
                                                    boolean mappingEnabled)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        XmlDtoConverter xmlDtoConverter = new XmlDtoConverter();
        EventReceiverConfiguration eventReceiverConfiguration;
        try {
            eventReceiverConfiguration = xmlDtoConverter.toEventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdaptorType, xpathExpressions, inputPropertyConfiguration, xpathDefinitions, parentSelectorXpath, mappingEnabled);
        } catch (EventReceiverAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventReceiverConfiguration != null) {
            try {
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployJsonEventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,

                                                     String eventAdaptorType,
                                                     EventInputPropertyConfigurationDto[] jsonPathExpressions,
                                                     PropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        JsonDtoConverter jsonDtoConverter = new JsonDtoConverter();
        EventReceiverConfiguration eventReceiverConfiguration;
        try {
            eventReceiverConfiguration = jsonDtoConverter.toEventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdaptorType, jsonPathExpressions, inputPropertyConfiguration, mappingEnabled);
        } catch (EventReceiverAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventReceiverConfiguration != null) {
            try {
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployMapEventReceiverConfiguration(String eventReceiverName,
                                                    String streamNameWithVersion,
                                                    String eventAdaptorType,
                                                    EventInputPropertyConfigurationDto[] mappingProperties,
                                                    PropertyDto[] inputPropertyConfiguration,
                                                    boolean mappingEnabled)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        MapDtoConverter mapDtoConverter = new MapDtoConverter();
        EventReceiverConfiguration eventReceiverConfiguration;
        try {
            eventReceiverConfiguration = mapDtoConverter.toEventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdaptorType, mappingProperties, inputPropertyConfiguration, mappingEnabled);
        } catch (EventReceiverAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventReceiverConfiguration != null) {
            try {
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    public void deployTextEventReceiverConfiguration(String eventReceiverName,
                                                     String streamNameWithVersion,
                                                     String eventAdaptorType,
                                                     EventInputPropertyConfigurationDto[] textMappingAttributes,
                                                     PropertyDto[] inputPropertyConfiguration,
                                                     boolean mappingEnabled)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        TextDtoConverter textDtoConverter = new TextDtoConverter();
        EventReceiverConfiguration eventReceiverConfiguration;
        try {
            eventReceiverConfiguration = textDtoConverter.toEventReceiverConfiguration(eventReceiverName, streamNameWithVersion,
                    eventAdaptorType, textMappingAttributes, inputPropertyConfiguration, mappingEnabled);
        } catch (EventReceiverAdminServiceException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
        if (eventReceiverConfiguration != null) {
            try {
                eventReceiverService.deployEventReceiverConfiguration(eventReceiverConfiguration, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }

    }

    /**
     * @param eventReceiverName the name of the event receiver to be undeployed
     * @throws AxisFault
     */
    public void undeployActiveEventReceiverConfiguration(String eventReceiverName) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.undeployActiveEventReceiverConfiguration(eventReceiverName, getAxisConfig());
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    /**
     * @param filename filename of the event receiver configuration file that needs to be undeployed. e.g. "wso2EventReceiver.xml"
     * @throws AxisFault
     */
    public void undeployInactiveEventReceiverConfiguration(String filename) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.undeployInactiveEventReceiverConfiguration(filename, getAxisConfig());
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void editActiveEventReceiverConfiguration(String originalEventReceiverName,
                                                     String eventReceiverConfigXml) throws AxisFault {
        if (eventReceiverConfigXml != null && !eventReceiverConfigXml.isEmpty() && originalEventReceiverName != null && !originalEventReceiverName.isEmpty()) {
            EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
            try {
                eventReceiverService.editActiveEventReceiverConfiguration(eventReceiverConfigXml, originalEventReceiverName, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        } else {
            String errMsg = "Some required parameters were null or empty. Cannot proceed with updating.";
            log.error(errMsg);
            throw new AxisFault(errMsg);
        }
    }

    public void editInactiveEventReceiverConfiguration(String filename, String eventReceiverConfigXml)
            throws AxisFault {
        if (eventReceiverConfigXml != null && !eventReceiverConfigXml.isEmpty() && filename != null && !filename.isEmpty()) {
            EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
            try {
                eventReceiverService.editInactiveEventReceiverConfiguration(eventReceiverConfigXml, filename, getAxisConfig());
            } catch (EventReceiverConfigurationException e) {
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
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        AxisConfiguration axisConfiguration = getAxisConfig();
        try {
            eventReceiverService.deployDefaultEventReceiver(streamId, axisConfiguration);
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }

    }

    public void setTraceEnabled(String eventReceiverName, boolean traceEnabled) throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.setTraceEnabled(eventReceiverName, traceEnabled, getAxisConfig());
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

    public void setStatisticsEnabled(String eventReceiverName, boolean statisticsEnabled)
            throws AxisFault {
        EventReceiverService eventReceiverService = EventReceiverAdminServiceValueHolder.getEventReceiverService();
        try {
            eventReceiverService.setStatisticsEnabled(eventReceiverName, statisticsEnabled, getAxisConfig());
        } catch (EventReceiverConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage());
        }
    }

}
