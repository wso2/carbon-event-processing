/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.flow;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.flow.internal.EventFlowServiceValueHolder;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;

import java.util.List;
import java.util.Map;

public class EventFlowAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(EventFlowAdminService.class);

    public String getEventFlow() throws AxisFault {
        try {
            AxisConfiguration axisConfiguration = getAxisConfig();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<EventReceiverConfiguration> eventReceiverConfigurations = EventFlowServiceValueHolder.getEventReceiverService().getAllActiveEventReceiverConfigurations();
            List<EventPublisherConfiguration> eventPublisherConfigurations = EventFlowServiceValueHolder.getEventPublisherService().getAllActiveEventPublisherConfigurations();
            List<String> streamIds = EventFlowServiceValueHolder.getEventStreamService().getStreamIds();
            Map<String, ExecutionPlanConfiguration> executionPlanConfigurations = EventFlowServiceValueHolder.getEventProcessorService().getAllActiveExecutionConfigurations();

            StringBuilder eventFlow = new StringBuilder(" '{ \"nodes\": [ ");
            for (EventReceiverConfiguration receiverConfiguration : eventReceiverConfigurations) {
                String url = "../eventreceiver/eventReceiver_details.jsp?ordinal=1&eventReceiverName=" + receiverConfiguration.getEventReceiverName();
                eventFlow.append("  { \"id\": \"").append(receiverConfiguration.getEventReceiverName().toUpperCase()).append("_ER").append("\", \"label\":\"").append(receiverConfiguration.getEventReceiverName()).append("\", \"url\":\"").append(url).append("\", \"nodeclass\": \"ER\" },");
            }
            for (String name : executionPlanConfigurations.keySet()) {
                String url = "../eventprocessor/execution_plan_details.jsp?ordinal=1&execPlan=" + name;
                eventFlow.append("  { \"id\": \"").append(name.toUpperCase()).append("_EXP").append("\", \"label\":\"").append(name).append("\", \"url\":\"").append(url).append("\", \"nodeclass\": \"EXP\" },");
            }
            for (EventPublisherConfiguration publisherConfiguration : eventPublisherConfigurations) {
                String url = "../eventpublisher/eventPublisher_details.jsp?ordinal=1&eventPublisherName=" + publisherConfiguration.getEventPublisherName();
                eventFlow.append("  { \"id\": \"").append(publisherConfiguration.getEventPublisherName().toUpperCase()).append("_EP").append("\", \"label\":\"").append(publisherConfiguration.getEventPublisherName()).append("\", \"url\":\"").append(url).append("\", \"nodeclass\": \"EP\" },");
            }
            for (String streamId : streamIds) {
                String url = "../eventstream/eventStreamDetails.jsp?ordinal=1&eventStreamWithVersion=" + streamId;
                eventFlow.append("  { \"id\": \"").append(streamId.replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\", \"label\":\"").append(streamId).append("\", \"url\":\"").append(url).append("\", \"nodeclass\": \"ES\" },");
            }

            eventFlow = new StringBuilder(eventFlow.substring(0, eventFlow.length() - 1));
            eventFlow.append("], \"edges\": [ ");

            for (EventReceiverConfiguration receiverConfiguration : eventReceiverConfigurations) {
                eventFlow.append("  { \"from\": \"").append(receiverConfiguration.getEventReceiverName().toUpperCase()).append("_ER").
                        append("\", \"to\":\"").append((receiverConfiguration.getToStreamName() + ":" + receiverConfiguration.getToStreamVersion()).replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").append("\" },");
            }
            for (ExecutionPlanConfiguration executionPlanConfiguration : executionPlanConfigurations.values()) {
                for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getImportedStreams()) {
                    eventFlow.append("  { \"from\": \"").append(streamConfiguration.getStreamId().replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").
                            append("\", \"to\":\"").append(executionPlanConfiguration.getName().toUpperCase()).append("_EXP").append("\" },");
                }
                for (StreamConfiguration streamConfiguration : executionPlanConfiguration.getExportedStreams()) {
                    eventFlow.append("  { \"to\": \"").append(streamConfiguration.getStreamId().replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").
                            append("\", \"from\":\"").append(executionPlanConfiguration.getName().toUpperCase()).append("_EXP").append("\" },");
                }
            }
            for (EventPublisherConfiguration publisherConfiguration : eventPublisherConfigurations) {
                eventFlow.append("  { \"from\": \"").append((publisherConfiguration.getFromStreamName() + ":" + publisherConfiguration.getFromStreamVersion()).replaceAll("\\.", "_").replaceAll(":", "_").toUpperCase()).append("_ES").
                        append("\", \"to\":\"").append(publisherConfiguration.getEventPublisherName().toUpperCase()).append("_EP").append("\" },");
            }

            eventFlow = new StringBuilder(eventFlow.substring(0, eventFlow.length() - 1));

            eventFlow.append("]}'");

            return eventFlow.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AxisFault("Exception:" + e.getMessage());
        }

    }

}
