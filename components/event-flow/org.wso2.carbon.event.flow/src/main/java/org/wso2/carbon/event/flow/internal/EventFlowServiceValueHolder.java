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

import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.stream.core.EventStreamService;

public class EventFlowServiceValueHolder {
    private static EventStreamService eventStreamService;
    private static EventProcessorService eventProcessorService;
    private static EventPublisherService eventPublisherService;
    private static EventReceiverService eventReceiverService;

    private EventFlowServiceValueHolder() {
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventFlowServiceValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return EventFlowServiceValueHolder.eventStreamService;
    }

    public static void registerEventProcessorService(EventProcessorService eventProcessorService) {
        EventFlowServiceValueHolder.eventProcessorService = eventProcessorService;
    }

    public static EventProcessorService getEventProcessorService() {
        return EventFlowServiceValueHolder.eventProcessorService;
    }

    public static void registerEventReceiverService(EventReceiverService eventReceiverService) {
        EventFlowServiceValueHolder.eventReceiverService = eventReceiverService;
    }

    public static EventReceiverService getEventReceiverService(){
        return EventFlowServiceValueHolder.eventReceiverService;
    }

    public static void registerEventPublisherService(EventPublisherService eventPublisherService) {
        EventFlowServiceValueHolder.eventPublisherService = eventPublisherService;
    }

    public static EventPublisherService getEventPublisherService() {
        return EventFlowServiceValueHolder.eventPublisherService;
    }
}
