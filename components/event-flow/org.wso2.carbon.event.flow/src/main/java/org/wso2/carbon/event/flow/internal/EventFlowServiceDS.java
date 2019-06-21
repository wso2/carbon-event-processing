/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.flow.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "eventFlowService.component",
        immediate = true)
public class EventFlowServiceDS {

    private static final Log log = LogFactory.getLog(EventFlowServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Successfully deployed Event Flow Service.");
        }
    }

    @Reference(
            name = "eventStreamManager.service",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {

        EventFlowServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        EventFlowServiceValueHolder.registerEventStreamService(null);
    }

    @Reference(
            name = "eventProcessor.service",
            service = org.wso2.carbon.event.processor.core.EventProcessorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventProcessorService")
    protected void setEventProcessorService(EventProcessorService eventProcessorService) {

        EventFlowServiceValueHolder.registerEventProcessorService(eventProcessorService);
    }

    protected void unsetEventProcessorService(EventProcessorService eventProcessorService) {

        EventFlowServiceValueHolder.registerEventProcessorService(null);
    }

    @Reference(
            name = "eventReceiver.service",
            service = org.wso2.carbon.event.receiver.core.EventReceiverService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventReceiverService")
    protected void setEventReceiverService(EventReceiverService eventReceiverService) {

        EventFlowServiceValueHolder.registerEventReceiverService(eventReceiverService);
    }

    protected void unsetEventReceiverService(EventReceiverService eventReceiverService) {

        EventFlowServiceValueHolder.registerEventReceiverService(null);
    }

    @Reference(
            name = "eventPublisher.service",
            service = org.wso2.carbon.event.publisher.core.EventPublisherService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventPublisherService")
    protected void setEventPublisherService(EventPublisherService eventPublisherService) {

        EventFlowServiceValueHolder.registerEventPublisherService(eventPublisherService);
    }

    protected void unsetEventPublisherService(EventPublisherService eventPublisherService) {

        EventFlowServiceValueHolder.registerEventPublisherService(null);
    }
}
