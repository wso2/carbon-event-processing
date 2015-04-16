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

    public void deployExecutionPlan(String executionPlan)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();

        if (eventProcessorService != null) {
            try {
                eventProcessorService.deployExecutionPlan(executionPlan);
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

    public void undeployActiveExecutionPlan(String planName) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            try {
                eventProcessorService.undeployActiveExecutionPlan(planName);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage());
            }
        }
    }

    public void undeployInactiveExecutionPlan(String fileName) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            try {
                eventProcessorService.undeployInactiveExecutionPlan(fileName);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }

    public void editActiveExecutionPlan(String executionPlan, String name)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        try {
            eventProcessorService.editActiveExecutionPlan(executionPlan, name);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        } catch (ExecutionPlanDependencyValidationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public void editInactiveExecutionPlan(String executionPlan, String fileName)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        try {
            eventProcessorService.editInactiveExecutionPlan(executionPlan, fileName);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        } catch (ExecutionPlanDependencyValidationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public ExecutionPlanConfigurationDto getActiveExecutionPlanConfiguration(String planName)
            throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            ExecutionPlanConfiguration executionConfiguration = eventProcessorService.getActiveExecutionPlanConfiguration(planName,
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
            copyConfigurationsToDto(executionConfiguration, dto);
            return dto;
        }
        return null;
    }

    public String getActiveExecutionPlan(String planName) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        try {
            return eventProcessorService.getActiveExecutionPlan(planName);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public String getInactiveExecutionPlan(String filename) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        try {
            return eventProcessorService.getInactiveExecutionPlan(filename);
        } catch (ExecutionPlanConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public ExecutionPlanConfigurationDto[] getAllActiveExecutionPlanConfigurations()
            throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.getAllActiveExecutionConfigurations(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
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
                    String statusMsg = file.getDeploymentStatusMessage();
                    if (file.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + file.getDependency() + "]";
                    }
                    fileDtoArray[i].setDeploymentStatusMessage(statusMsg);
                }
                return fileDtoArray;
            }
        }
        return new ExecutionPlanConfigurationFileDto[0];
    }

    public ExecutionPlanConfigurationDto[] getAllExportedStreamSpecificActiveExecutionPlanConfiguration(String streamId)
            throws AxisFault {

        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.
                    getAllExportedStreamSpecificActiveExecutionConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), streamId);
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

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.
                    getAllImportedStreamSpecificActiveExecutionConfigurations(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), streamId);
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

    public void setTracingEnabled(String executionPlanName, boolean isEnabled) throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            try {
                eventProcessorService.setTracingEnabled(executionPlanName, isEnabled);
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
            try {
                eventProcessorService.setStatisticsEnabled(executionPlanName, isEnabled);
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("Event processor is not loaded.");
        }
    }

    public String validateExecutionPlan(String executionPlan) throws AxisFault {
        try {
            EventProcessorAdminValueHolder.getEventProcessorService().validateExecutionPlan(executionPlan);
            return "success";
        } catch (ExecutionPlanConfigurationException e) {
            return e.getMessage();
        } catch (ExecutionPlanDependencyValidationException e) {
            return e.getMessage();
        }
    }

    public StreamDefinitionDto[] getSiddhiStreams(String executionPlan) throws AxisFault {
        List<StreamDefinition> streamdefinitions = EventProcessorAdminValueHolder.getEventProcessorService().getSiddhiStreams(executionPlan);
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

    private void copyConfigurationsToDto(ExecutionPlanConfiguration config,
                                         ExecutionPlanConfigurationDto dto) {
        dto.setName(config.getName());
        dto.setDescription(config.getDescription());
        dto.setExecutionPlan(config.getExecutionPlan());
        dto.setStatisticsEnabled(config.isStatisticsEnabled());
        dto.setTracingEnabled(config.isTracingEnabled());
        dto.setEditable(config.isEditable());

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
