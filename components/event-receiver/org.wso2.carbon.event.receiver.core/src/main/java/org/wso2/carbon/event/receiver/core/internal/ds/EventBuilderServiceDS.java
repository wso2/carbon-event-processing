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
package org.wso2.carbon.event.receiver.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.event.receiver.core.EventBuilderService;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.receiver.core.internal.CarbonInputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.internal.DataBridgeStreamAddRemoveListenerImpl;
import org.wso2.carbon.event.receiver.core.internal.EventStreamListenerImpl;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.List;


/**
 * @scr.component name="eventBuilderService.component" immediate="true"
 * @scr.reference name="inputEventAdaptor.service"
 * interface="org.wso2.carbon.event.receiver.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorService" unbind="unsetInputEventAdaptorService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="databridge.receiver.service"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService" cardinality="1..1"
 * policy="dynamic" bind="setDataBridgeReceiverService" unbind="unSetDataBridgeReceiverService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EventBuilderServiceDS {

    private static final Log log = LogFactory.getLog(EventBuilderServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            CarbonEventBuilderService carbonEventBuilderService = new CarbonEventBuilderService();
            EventBuilderServiceValueHolder.registerEventBuilderService(carbonEventBuilderService);
            context.getBundleContext().registerService(EventBuilderService.class.getName(), carbonEventBuilderService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventReceiver Service.");
            }




            EventBuilderServiceValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());
            EventBuilderServiceValueHolder.getDataBridgeReceiverService().subscribe(new DataBridgeStreamAddRemoveListenerImpl());

            registerEventAdaptorTypes();

        } catch (RuntimeException e) {
            log.error("Could not create EventBuilderService or EventReceiver : " + e.getMessage(), e);
        }
    }

    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        EventBuilderServiceValueHolder.registerDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unSetDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeSubscriberService) {
        EventBuilderServiceValueHolder.registerDataBridgeReceiverService(null);

    }

    protected void setInputEventAdaptorService(InputEventAdaptorService inputEventAdaptorService) {
        EventBuilderServiceValueHolder.registerInputEventAdaptorService(inputEventAdaptorService);
    }

    protected void unsetInputEventAdaptorService(
            InputEventAdaptorService inputEventAdaptorService) {
        EventBuilderServiceValueHolder.registerInputEventAdaptorService(null);
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.registerEventStatisticsService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventBuilderServiceValueHolder.registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventBuilderServiceValueHolder.registerRegistryService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventBuilderServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventBuilderServiceValueHolder.registerEventStreamService(null);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventBuilderServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventBuilderServiceValueHolder.setConfigurationContextService(null);

    }

    private void registerEventAdaptorTypes() {
        List<InputEventAdaptorFactory> inputEventAdaptorFactories = InputEventAdaptorServiceTrackerDS.inputEventAdaptorFactories;
        for (InputEventAdaptorFactory inputEventAdaptorFactory : inputEventAdaptorFactories) {
            ((CarbonInputEventAdaptorService) InputEventAdaptorServiceValueHolder.getCarbonInputEventAdaptorService()).registerEventAdaptor(inputEventAdaptorFactory.getEventAdaptor());
        }
    }

}
