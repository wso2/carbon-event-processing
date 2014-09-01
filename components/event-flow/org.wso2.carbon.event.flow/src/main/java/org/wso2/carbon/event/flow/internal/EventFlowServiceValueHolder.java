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
package org.wso2.carbon.event.flow.internal;

import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;

public class EventFlowServiceValueHolder {
    private static EventBuilderService eventBuilderService;
    private static EventFormatterService eventFormatterService;
    private static InputEventAdaptorManagerService inputEventAdaptorManagerService;
    private static OutputEventAdaptorManagerService outputEventAdaptorManagerService;
    private static EventStreamService eventStreamService;
    private static EventProcessorService eventProcessorService;

    private EventFlowServiceValueHolder() {

    }

    public static void registerEventBuilderService(EventBuilderService eventBuilderService) {
        EventFlowServiceValueHolder.eventBuilderService = eventBuilderService;
    }

    public static EventBuilderService getEventBuilderService() {
        return EventFlowServiceValueHolder.eventBuilderService;
    }

    public static void registerEventFormatterService(EventFormatterService eventFormatterService) {
        EventFlowServiceValueHolder.eventFormatterService = eventFormatterService;
    }

    public static EventFormatterService getEventFormatterService() {
        return EventFlowServiceValueHolder.eventFormatterService;
    }

    public static void registerInputEventAdaptorManagerService(
            InputEventAdaptorManagerService inputEventAdaptorManagerService) {
        EventFlowServiceValueHolder.inputEventAdaptorManagerService = inputEventAdaptorManagerService;
    }

    public static InputEventAdaptorManagerService getInputEventAdaptorManagerService() {
        return EventFlowServiceValueHolder.inputEventAdaptorManagerService;
    }

    public static void registerOutputEventAdaptorManagerService(
            OutputEventAdaptorManagerService outputEventAdaptorManagerService) {
        EventFlowServiceValueHolder.outputEventAdaptorManagerService = outputEventAdaptorManagerService;
    }

    public static OutputEventAdaptorManagerService getOutputEventAdaptorManagerService() {
        return EventFlowServiceValueHolder.outputEventAdaptorManagerService;
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
}
