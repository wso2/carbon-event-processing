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
package org.wso2.carbon.event.flow.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;

/**
 * @scr.component name="eventFlowService.component" immediate="true"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="eventProcessor.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorService" unbind="unsetEventProcessorService"
 * @scr.reference name="eventReceiver.service"
 * interface="org.wso2.carbon.event.receiver.core.EventReceiverService" cardinality="1..1"
 * policy="dynamic" bind="setEventReceiverService" unbind="unsetEventReceiverService"
 * @scr.reference name="eventPublisher.service"
 * interface="org.wso2.carbon.event.publisher.core.EventPublisherService" cardinality="1..1"
 * policy="dynamic" bind="setEventPublisherService" unbind="unsetEventPublisherService"
 */
public class EventFlowServiceDS {
    private static final Log log = LogFactory.getLog(EventFlowServiceDS.class);

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Successfully deployed Event Flow Service.");
        }
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventFlowServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventFlowServiceValueHolder.registerEventStreamService(null);
    }
    protected void setEventProcessorService(EventProcessorService eventProcessorService) {
        EventFlowServiceValueHolder.registerEventProcessorService(eventProcessorService);
    }

    protected void unsetEventProcessorService(EventProcessorService eventProcessorService) {
        EventFlowServiceValueHolder.registerEventProcessorService(null);
    }

    protected void setEventReceiverService(EventReceiverService eventReceiverService) {
        EventFlowServiceValueHolder.registerEventReceiverService(eventReceiverService);
    }

    protected void unsetEventReceiverService(EventReceiverService eventReceiverService) {
        EventFlowServiceValueHolder.registerEventReceiverService(null);
    }

    protected void setEventPublisherService(EventPublisherService eventPublisherService) {
        EventFlowServiceValueHolder.registerEventPublisherService(eventPublisherService);
    }

    protected void unsetEventPublisherService(EventPublisherService eventPublisherService) {
        EventFlowServiceValueHolder.registerEventPublisherService(null);
    }
}
