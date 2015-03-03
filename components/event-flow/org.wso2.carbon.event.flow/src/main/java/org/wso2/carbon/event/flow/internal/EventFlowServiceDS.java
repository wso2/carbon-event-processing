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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;


/**
 * @scr.component name="eventFlowService.component" immediate="true"
 * @scr.reference name="inputEventAdaptorManager.service"
 * interface="org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorManagerService" unbind="unsetInputEventAdaptorManagerService"
 * @scr.reference name="outputEventAdaptorManager.service"
 * interface="org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setOutputEventAdaptorManagerService" unbind="unsetOutputEventAdaptorManagerService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="eventFormatter.service"
 * interface="org.wso2.carbon.event.formatter.core.EventFormatterService" cardinality="1..1"
 * policy="dynamic" bind="setEventFormatterService" unbind="unsetEventFormatterService"
 * @scr.reference name="eventBuilder.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 * @scr.reference name="eventProcessor.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorService" unbind="unsetEventProcessorService"
 */
public class EventFlowServiceDS {
    private static final Log log = LogFactory.getLog(EventFlowServiceDS.class);

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Successfully deployed Event Flow Service.");
        }
    }

    protected void setInputEventAdaptorManagerService(
            InputEventAdaptorManagerService eventManagerService) {
        EventFlowServiceValueHolder.registerInputEventAdaptorManagerService(eventManagerService);
    }

    protected void unsetInputEventAdaptorManagerService(
            InputEventAdaptorManagerService eventManagerService) {
        EventFlowServiceValueHolder.registerInputEventAdaptorManagerService(null);
    }

    protected void setOutputEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventManagerService) {
        EventFlowServiceValueHolder.registerOutputEventAdaptorManagerService(eventManagerService);
    }

    protected void unsetOutputEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventManagerService) {
        EventFlowServiceValueHolder.registerOutputEventAdaptorManagerService(null);
    }

    protected void setEventBuilderService(EventBuilderService eventBuilderService) {
        EventFlowServiceValueHolder.registerEventBuilderService(eventBuilderService);
    }

    protected void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        EventFlowServiceValueHolder.registerEventBuilderService(null);
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

    protected void setEventFormatterService(EventFormatterService eventFormatterService) {
        EventFlowServiceValueHolder.registerEventFormatterService(eventFormatterService);
    }

    protected void unsetEventFormatterService(EventFormatterService eventFormatterService) {
        EventFlowServiceValueHolder.registerEventFormatterService(null);
    }

}
