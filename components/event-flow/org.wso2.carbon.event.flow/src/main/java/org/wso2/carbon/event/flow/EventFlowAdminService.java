/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.flow;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.flow.internal.EventFlowServiceValueHolder;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;

import java.util.List;
import java.util.Map;

public class EventFlowAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EventFlowAdminService.class);

    public String getEventFlow() throws AxisFault {
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<InputEventAdaptorConfiguration> inputEventAdaptors = EventFlowServiceValueHolder.getInputEventAdaptorManagerService().getAllActiveInputEventAdaptorConfiguration(axisConfiguration);
            List<OutputEventAdaptorConfiguration> outputEventAdaptors = EventFlowServiceValueHolder.getOutputEventAdaptorManagerService().getAllActiveOutputEventAdaptorConfiguration(axisConfiguration);
            List<EventFormatterConfiguration> eventFormatterConfigurations = EventFlowServiceValueHolder.getEventFormatterService().getAllActiveEventFormatterConfiguration(axisConfiguration);
            List<EventBuilderConfiguration> eventBuilderConfigurations = EventFlowServiceValueHolder.getEventBuilderService().getAllActiveEventBuilderConfigurations(tenantId);
            List<String> streamIds = EventFlowServiceValueHolder.getEventStreamService().getStreamIds(tenantId);
            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = EventFlowServiceValueHolder.getEventProcessorService().getAllActiveExecutionConfigurations(tenantId);

            StringBuilder eventFlow = new StringBuilder( " '{ \"nodes\": [ ");
            for (InputEventAdaptorConfiguration inputEventAdaptorConfiguration : inputEventAdaptors) {
                eventFlow.append("  { \"id\" : \"").append(inputEventAdaptorConfiguration.getName().toUpperCase()).append("_IEA").append("\", \"label\":\"").append(inputEventAdaptorConfiguration.getName()).append("\", \"nodeclass\": \"IEA\" },");
            }
            for (OutputEventAdaptorConfiguration outputEventAdaptorConfiguration : outputEventAdaptors) {
                eventFlow.append("  { \"id\": \"").append(outputEventAdaptorConfiguration.getName().toUpperCase()).append("_OEA").append("\", \"label\":\"").append(outputEventAdaptorConfiguration.getName()).append("\", \"nodeclass\": \"OEA\" },");
            }
            for (EventBuilderConfiguration eventBuilderConfiguration : eventBuilderConfigurations) {
                eventFlow.append("  { \"id\": \"").append(eventBuilderConfiguration.getEventBuilderName().toUpperCase()).append("_EB").append("\", \"label\":\"").append(eventBuilderConfiguration.getEventBuilderName()).append("\", \"nodeclass\": \"EB\" },");
            }
            for (String name : executionPlanConfigurations.keySet()) {
                eventFlow.append("  { \"id\": \"").append(name.toUpperCase()).append("_EP").append("\", \"label\":\"").append(name).append("\", \"nodeclass\": \"EP\" },");
            }
            for (EventFormatterConfiguration formatterConfiguration : eventFormatterConfigurations) {
                eventFlow.append("  { \"id\": \"").append(formatterConfiguration.getEventFormatterName().toUpperCase()).append("_EF").append("\", \"label\":\"").append(formatterConfiguration.getEventFormatterName()).append("\", \"nodeclass\": \"EF\" },");
            }
            for (String streamId : streamIds) {
                eventFlow.append("  { \"id\": \"").append(streamId.replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\", \"label\":\"").append(streamId).append("\", \"nodeclass\": \"ES\" },");
            }

            eventFlow =new StringBuilder(eventFlow.substring(0,eventFlow.length() - 1)) ;
            eventFlow.append("], \"edges\": [ ");


            for (EventBuilderConfiguration eventBuilderConfiguration : eventBuilderConfigurations) {
                eventFlow.append("  { \"from\": \"").append(eventBuilderConfiguration.getInputStreamConfiguration().getInputEventAdaptorName().toUpperCase()).append("_IEA").append("\", \"to\":\"").append(eventBuilderConfiguration.getEventBuilderName().toUpperCase()).append("_EB").append("\" },");
                eventFlow.append("  { \"from\": \"").append(eventBuilderConfiguration.getEventBuilderName().toUpperCase()).append("_EB").append("\", \"to\":\"").append((eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion()).replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\" },");
            }
            for (EventFormatterConfiguration formatterConfiguration : eventFormatterConfigurations) {
                eventFlow.append("  { \"from\": \"").append(formatterConfiguration.getEventFormatterName().toUpperCase()).append("_EF").append("\", \"to\":\"").append(formatterConfiguration.getToPropertyConfiguration().getEventAdaptorName().toUpperCase()).append("_OEA").append("\" },");
                eventFlow.append("  { \"from\": \"").append((formatterConfiguration.getFromStreamName() + ":" + formatterConfiguration.getFromStreamVersion()).replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\", \"to\":\"").append(formatterConfiguration.getEventFormatterName().toUpperCase()).append("_EF").append("\" },");
            }
            for (ExecutionPlanConfiguration executionPlanConfiguration : executionPlanConfigurations.values()) {
                for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getImportedStreams()) {
                    eventFlow.append("  { \"from\": \"").append(streamConfiguration.getStreamId().replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\", \"to\":\"").append(executionPlanConfiguration.getName().toUpperCase()).append("_EP").append("\" },");
                }
                for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                    eventFlow.append("  { \"to\": \"").append(streamConfiguration.getStreamId().replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\", \"from\":\"").append(executionPlanConfiguration.getName().toUpperCase()).append("_EP").append("\" },");
                }
            }

            eventFlow =new StringBuilder(eventFlow.substring(0,eventFlow.length() - 1)) ;

            eventFlow.append("]}'");

            return eventFlow.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AxisFault("Exception:" + e.getMessage());
        }

    }

}
