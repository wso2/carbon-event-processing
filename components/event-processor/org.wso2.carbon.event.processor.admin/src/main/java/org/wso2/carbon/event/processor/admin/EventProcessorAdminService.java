/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.admin;

import org.apache.axis2.AxisFault;
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
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

//import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

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
                throw new AxisFault(e.getMessage(), e);
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
            ExecutionPlanConfiguration executionConfiguration = eventProcessorService.getActiveExecutionPlanConfiguration(planName);
            ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
            copyConfigurationsToDto(executionConfiguration, dto, null);
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

            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = eventProcessorService.getAllActiveExecutionConfigurations();

            if (executionPlanConfigurations != null && !executionPlanConfigurations.isEmpty()) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                if (isDistributedProcessingEnabled()) {
                    Map<String, String> executionPlanStatuses = eventProcessorService.getAllExecutionPlanStatusesInStorm();
                    for (Map.Entry<String, ExecutionPlanConfiguration> entry : executionPlanConfigurations.entrySet()) {
                        ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                        String status = executionPlanStatuses.get(entry.getKey());
                        if (status == null) {
                            log.error("No distributed deployment status information available for execution plan " + entry.getKey());
                        }
                        copyConfigurationsToDto(entry.getValue(), dto, status);
                        configurationDtos[i] = dto;
                        i++;
                    }
                } else {
                    for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                        ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                        copyConfigurationsToDto(planConfiguration, dto, null);
                        configurationDtos[i] = dto;
                        i++;
                    }
                }

                Arrays.sort(configurationDtos, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((ExecutionPlanConfigurationDto) o1).getName().compareTo(((ExecutionPlanConfigurationDto) o2).getName());
                    }
                });

                if (!isDistributedProcessingEnabled()) {
                    configurationDtos[0].setDeploymentStatus(EventProcessorConstants.NOT_DISTRIBUTED);
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
            List<ExecutionPlanConfigurationFile> files = eventProcessorService.getAllInactiveExecutionPlanConfiguration();
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
                Arrays.sort(fileDtoArray, new Comparator() {

                    @Override
                    public int compare(Object ExecutionPlanConfigurationFileDtoObject, Object ExecutionPlanConfigurationFileDtoAnotherObject) {
                        return ((ExecutionPlanConfigurationFileDto) ExecutionPlanConfigurationFileDtoObject).getName().compareTo(((ExecutionPlanConfigurationFileDto) ExecutionPlanConfigurationFileDtoAnotherObject).getName());
                    }
                });
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
                    getAllExportedStreamSpecificActiveExecutionConfigurations(streamId);
            if (executionPlanConfigurations != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(planConfiguration, dto, null);
                    configurationDtos[i] = dto;
                    i++;
                }
                Arrays.sort(configurationDtos, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((ExecutionPlanConfigurationDto) o1).getName().compareTo(((ExecutionPlanConfigurationDto) o2).getName());
                    }
                });
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
                    getAllImportedStreamSpecificActiveExecutionConfigurations(streamId);
            if (executionPlanConfigurations != null) {
                ExecutionPlanConfigurationDto[] configurationDtos = new ExecutionPlanConfigurationDto[executionPlanConfigurations.size()];

                int i = 0;
                for (ExecutionPlanConfiguration planConfiguration : executionPlanConfigurations.values()) {
                    ExecutionPlanConfigurationDto dto = new ExecutionPlanConfigurationDto();
                    copyConfigurationsToDto(planConfiguration, dto, null);
                    configurationDtos[i] = dto;
                    i++;
                }
                Arrays.sort(configurationDtos, new Comparator() {

                    @Override
                    public int compare(Object o1, Object o2) {
                        return ((ExecutionPlanConfigurationDto) o1).getName().compareTo(((ExecutionPlanConfigurationDto) o2).getName());
                    }
                });
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
            log.error("Exception when validating execution plan", e);
            return e.getMessage();
        } catch (ExecutionPlanDependencyValidationException e) {
            log.error("Exception when validating execution plan", e);
            return e.getMessage();
        } catch (ExecutionPlanValidationException e) {
            log.error("Exception when validating execution plan", e);
            return e.getMessage();
        } catch (Throwable t) {
            log.error("Exception when validating execution plan", t);
            return t.getMessage();
        }
    }

    public StreamDefinitionDto[] getSiddhiStreams(String executionPlan) throws AxisFault {
        try {
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
        } catch (Throwable t) {
            log.error("Exception when generating siddhi streams", t);
            throw new AxisFault(t.getMessage(), t);
        }
    }

    private boolean isDistributedProcessingEnabled() throws AxisFault {
        EventProcessorService eventProcessorService = EventProcessorAdminValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            return eventProcessorService.isDistributedProcessingEnabled();
        } else {
            throw new AxisFault("Event processor is not loaded.");
        }
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
                                         ExecutionPlanConfigurationDto dto, String distributedDeploymentStatus) {
        dto.setName(config.getName());
        dto.setDescription(config.getDescription());
        dto.setExecutionPlan(config.getExecutionPlan());
        dto.setStatisticsEnabled(config.isStatisticsEnabled());
        dto.setTracingEnabled(config.isTracingEnabled());
        dto.setEditable(config.isEditable());
        dto.setDeploymentStatus(distributedDeploymentStatus);

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
