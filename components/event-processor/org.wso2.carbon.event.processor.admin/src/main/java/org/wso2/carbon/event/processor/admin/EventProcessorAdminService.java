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
package org.wso2.carbon.event.processor.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.admin.internal.ds.EventProcessorAdminValueHolder;
import org.wso2.carbon.event.processor.admin.internal.util.EventProcessorAdminUtil;
import org.wso2.carbon.event.processor.admin.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfigurationFile;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

import java.util.List;
import java.util.Map;

public class EventProcessorAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(EventProcessorAdminService.class);


    public void deployExecutionPlanConfiguration(ExecutionPlanConfigurationDto configurationDto)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
            copyConfigurationsFromDto(configuration, configurationDto);

            try {
                eventProcessorService.deployExecutionPlanConfiguration(configuration, getAxisConfig());
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            } catch (ExecutionPlanDependencyValidationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault(configurationDto.getName() + " already registered as an execution in this tenant");
        }
    }

    public void deployExecutionPlanConfigurationFromConfigXml(String executionPlanConfigurationXml)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();

        if (eventProcessorService != null) {
            try {
                eventProcessorService.deployExecutionPlanConfiguration(executionPlanConfigurationXml, getAxisConfig());
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            } catch (ExecutionPlanDependencyValidationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("EventProcessorService is not available for EventProcessorAdminService in runtime!");
        }
    }

    public void undeployActiveExecutionPlanConfiguration(String name) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.undeployActiveExecutionPlanConfiguration(name, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    public void undeployInactiveExecutionPlanConfiguration(String fileName) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.undeployInactiveExecutionPlanConfiguration(fileName, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }

    public void editActiveExecutionPlanConfiguration(String configuration, String name)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            eventProcessorService.editActiveExecutionPlanConfiguration(configuration, name, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        } catch (ExecutionPlanDependencyValidationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public void editInactiveExecutionPlanConfiguration(String configuration, String fileName)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            eventProcessorService.editInactiveExecutionPlanConfiguration(configuration, fileName, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        } catch (ExecutionPlanDependencyValidationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }


    public String getInactiveExecutionPlanConfigurationContent(String filename) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            return eventProcessorService.getInactiveExecutionPlanConfigurationContent(filename, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }


    public String getActiveExecutionPlanConfigurationContent(String planName) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        AxisConfiguration axisConfig = getAxisConfig();
        try {
            return eventProcessorService.getActiveExecutionPlanConfigurationContent(planName, axisConfig);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }


    public ExecutionPlanConfigurationDto[] getAllActiveExecutionPlanConfigurations()
            throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.getAllActiveExecutionConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            if (executionPlanConfigurations != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(planConfiguration, dto);
                    configurationDtos[i] = dto;
                    i++;
                }
                return configurationDtos;
            }
        }
        return new ExecutionPlanConfigurationDto[0];
    }

    public ExecutionPlanConfigurationDto[] getAllExportedStreamSpecificActiveExecutionPlanConfiguration(String streamId)
            throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.getAllExportedStreamSpecificActiveExecutionConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), streamId);
            if (executionPlanConfigurations != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(planConfiguration, dto);
                    configurationDtos[i] = dto;
                    i++;
                }
                return configurationDtos;
            }
        }
        return new ExecutionPlanConfigurationDto[0];
    }

    public ExecutionPlanConfigurationDto[] getAllImportedStreamSpecificActiveExecutionPlanConfiguration(String streamId)
            throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.getAllImportedStreamSpecificActiveExecutionConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), streamId);
            if (executionPlanConfigurations != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(planConfiguration, dto);
                    configurationDtos[i] = dto;
                    i++;
                }
                return configurationDtos;
            }
        }
        return new ExecutionPlanConfigurationDto[0];
    }

    public ExecutionPlanConfigurationDto getActiveExecutionPlanConfiguration(String name)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            ExecutionPlanConfiguration executionConfiguration = eventProcessorService.getActiveExecutionPlanConfiguration(name, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
            copyConfigurationsToDto(executionConfiguration, dto);
            return dto;
        }
        return null;
    }

    public ExecutionPlanConfigurationFileDto[] getAllInactiveExecutionPlanConigurations()
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<ExecutionPlanConfigurationFile> files = eventProcessorService.getAllInactiveExecutionPlanConfiguration(tenantId);
            if (files != null) {
                ExecutionPlanConfigurationFileDto[] fileDtoArray = new ExecutionPlanConfigurationFileDto[files.size()];
                for (int i = 0; i < files.size(); i++) {
                    ExecutionPlanConfigurationFile file = files.get(i);
                    fileDtoArray[i] = new ExecutionPlanConfigurationFileDto();
                    fileDtoArray[i].setName(file.getExecutionPlanName());
                    fileDtoArray[i].setFileName(file.getFileName());
                    if (file.getStatus() != null) {
                        fileDtoArray[i].setStatus(file.getStatus().name());
                    }
                }
                return fileDtoArray;
            }
        }
        return new ExecutionPlanConfigurationFileDto[0];
    }

    public void setTracingEnabled(String executionPlanName, boolean isEnabled) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.setTracingEnabled(executionPlanName, isEnabled, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("Event processor is not loaded.");
        }
    }

    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            AxisConfiguration axisConfig = getAxisConfig();
            try {
                eventProcessorService.setStatisticsEnabled(executionPlanName, isEnabled, axisConfig);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("Event processor is not loaded.");
        }
    }

    public boolean validateSiddhiQueries(String[] inputStreamDefiniitons, String queryExpressions) throws AxisFault {
        return EventProcessorAdminValueHolder.getEventProcessorService().validateSiddhiQueries(inputStreamDefiniitons, queryExpressions);
    }

    public StreamDefinitionDto[] getSiddhiStreams(String[] inputStreamDefinitions, String queryExpressions) throws AxisFault {
        List<StreamDefinition> streamdefinitions = EventProcessorAdminValueHolder.getEventProcessorService().getSiddhiStreams(inputStreamDefinitions, queryExpressions);
        StreamDefinitionDto[] streamDefinitionDtos = new StreamDefinitionDto[streamdefinitions.size()];
        int i = 0;
        for (StreamDefinition databridgeStreamDef : streamdefinitions) {
            StreamDefinitionDto dto = new StreamDefinitionDto();
            dto.setName(databridgeStreamDef.getName());
            dto.setMetaData(convertAttributeList(databridgeStreamDef.getMetaData()));
            dto.setCorrelationData(convertAttributeList(databridgeStreamDef.getCorrelationData()));
            dto.setPayloadData(convertAttributeList(databridgeStreamDef.getPayloadData()));
            streamDefinitionDtos[i] = dto;
            i++;
        }
        return streamDefinitionDtos;
    }

    //TODO put inside the ExecutionPlanConfigurationFileDto
    public String getExecutionPlanStatusAsString(String filename) {
        EventProcessorService eventFormatterService = EventProcessorAdminValueHolder.getEventProcessorService();
        return eventFormatterService.getExecutionPlanStatusAsString(filename);
    }

    private String[] convertAttributeList(List<org.wso2.carbon.databridge.commons.Attribute> attributeList) {
        if (attributeList != null) {
            String[] convertedAttributes = new String[attributeList.size()];
            int i = 0;
            for (org.wso2.carbon.databridge.commons.Attribute attribute : attributeList) {
                convertedAttributes[i] = attribute.getName() + " " + EventProcessorConstants.STRING_ATTRIBUTE_TYPE_MAP.get(attribute.getType());
                i++;
            }
            return convertedAttributes;
        }
        return new String[0];
    }

    private void copyConfigurationsFromDto(ExecutionPlanConfiguration config,
                                           ExecutionPlanConfigurationDto dto) {
        config.setName(dto.getName());
        config.setDescription(dto.getDescription());
        config.setQueryExpressions(dto.getQueryExpressions());
        config.setStatisticsEnabled(dto.isStatisticsEnabled());
        config.setTracingEnabled(dto.isTracingEnabled());
        if (dto.getSiddhiConfigurations() != null) {
            for (SiddhiConfigurationDto siddhiConfig : dto.getSiddhiConfigurations()) {
                config.addSiddhiConfigurationProperty(siddhiConfig.getKey(), siddhiConfig.getValue());
            }
        }

        if (dto.getImportedStreams() != null) {
            for (StreamConfigurationDto streamConfigurationDto : dto.getImportedStreams()) {
                StreamConfiguration streamConfig = new StreamConfiguration(EventProcessorAdminUtil.getStreamName(streamConfigurationDto.getStreamId()), EventProcessorAdminUtil.getVersion(streamConfigurationDto.getStreamId()), streamConfigurationDto.getSiddhiStreamName());
                config.addImportedStream(streamConfig);
            }
        }

        if (dto.getExportedStreams() != null) {
            for (StreamConfigurationDto streamConfigurationDto : dto.getExportedStreams()) {
                StreamConfiguration streamConfig = new StreamConfiguration(EventProcessorAdminUtil.getStreamName(streamConfigurationDto.getStreamId()), EventProcessorAdminUtil.getVersion(streamConfigurationDto.getStreamId()), streamConfigurationDto.getSiddhiStreamName());
                config.addExportedStream(streamConfig);
            }
        }
    }

    private void copyConfigurationsToDto(ExecutionPlanConfiguration config,
                                         ExecutionPlanConfigurationDto dto) {
        dto.setName(config.getName());
        dto.setDescription(config.getDescription());
        dto.setQueryExpressions(config.getQueryExpressions());
        dto.setStatisticsEnabled(config.isStatisticsEnabled());
        dto.setTracingEnabled(config.isTracingEnabled());
        dto.setEditable(config.isEditable());
        if (config.getSiddhiConfigurationProperties() != null) {
            SiddhiConfigurationDto[] siddhiConfigs = new SiddhiConfigurationDto[config.getSiddhiConfigurationProperties().size()];
            int i = 0;
            for (Map.Entry<String, String> configEntry : config.getSiddhiConfigurationProperties().entrySet()) {
                SiddhiConfigurationDto siddhiConfigurationDto = new SiddhiConfigurationDto();
                siddhiConfigurationDto.setKey(configEntry.getKey());
                siddhiConfigurationDto.setValue(configEntry.getValue());
                siddhiConfigs[i] = siddhiConfigurationDto;
                i++;
            }
            dto.setSiddhiConfigurations(siddhiConfigs);
        }

        if (config.getImportedStreams() != null) {
            StreamConfigurationDto[] importedStreamDtos = new StreamConfigurationDto[config.getImportedStreams().size()];
            for (int i = 0; i < config.getImportedStreams().size(); i++) {
                StreamConfiguration streamConfiguration = config.getImportedStreams().get(i);
                StreamConfigurationDto streamDto = new StreamConfigurationDto(streamConfiguration.getStreamId(), streamConfiguration.getSiddhiStreamName());
                importedStreamDtos[i] = streamDto;
            }
            dto.setImportedStreams(importedStreamDtos);
        }

        if (config.getExportedStreams() != null) {
            StreamConfigurationDto[] exportedStreamDtos = new StreamConfigurationDto[config.getExportedStreams().size()];
            for (int i = 0; i < config.getExportedStreams().size(); i++) {
                StreamConfiguration streamConfiguration = config.getExportedStreams().get(i);
                StreamConfigurationDto streamDto = new StreamConfigurationDto(streamConfiguration.getStreamId(), streamConfiguration.getSiddhiStreamName());
                exportedStreamDtos[i] = streamDto;
            }
            dto.setExportedStreams(exportedStreamDtos);
        }
    }

}
