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
import org.wso2.carbon.event.receiver.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.CarbonInputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.internal.DataBridgeStreamAddRemoveListenerImpl;
import org.wso2.carbon.event.receiver.core.internal.EventStreamListenerImpl;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;


/**
 * @scr.component name="eventReceiverService.component" immediate="true"
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
public class EventReceiverServiceDS {

    public static List<InputEventAdaptorFactory> inputEventAdaptorFactories = new ArrayList<InputEventAdaptorFactory>();
    private static final Log log = LogFactory.getLog(EventReceiverServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            CarbonEventReceiverService carbonEventReceiverService = new CarbonEventReceiverService();
            EventReceiverServiceValueHolder.registerEventReceiverService(carbonEventReceiverService);

            EventReceiverService eventReceiverService = new CarbonEventReceiverService();
            context.getBundleContext().registerService(EventReceiverService.class.getName(), eventReceiverService, null);

            InputEventAdaptorService inputEventAdaptorService = new CarbonInputEventAdaptorService();
            context.getBundleContext().registerService(InputEventAdaptorService.class.getName(), inputEventAdaptorService, null);
            CarbonInputEventAdaptorService carbonInputEventAdaptorService = new CarbonInputEventAdaptorService();
            EventReceiverServiceValueHolder.registerCarbonInputEventAdaptorService(carbonInputEventAdaptorService);


            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventReceiver Service.");
            }


            EventReceiverServiceValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());
            EventReceiverServiceValueHolder.getDataBridgeReceiverService().subscribe(new DataBridgeStreamAddRemoveListenerImpl());

            registerEventAdaptorTypes();

        } catch (RuntimeException e) {
            log.error("Could not create EventReceiverService or EventReceiver : " + e.getMessage(), e);
        }
    }

    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        EventReceiverServiceValueHolder.registerDataBridgeReceiverService(dataBridgeReceiverService);
    }

    protected void unSetDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeSubscriberService) {
        EventReceiverServiceValueHolder.registerDataBridgeReceiverService(null);

    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventReceiverServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventReceiverServiceValueHolder.registerEventStatisticsService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventReceiverServiceValueHolder.registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventReceiverServiceValueHolder.registerRegistryService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventReceiverServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventReceiverServiceValueHolder.registerEventStreamService(null);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventReceiverServiceValueHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventReceiverServiceValueHolder.setConfigurationContextService(null);

    }

    private void registerEventAdaptorTypes() {


        List<InputEventAdaptorFactory> inputEventAdaptorFactories = InputEventAdaptorServiceTrackerDS.inputEventAdaptorFactories;
        for (InputEventAdaptorFactory inputEventAdaptorFactory : inputEventAdaptorFactories) {
            AbstractInputEventAdaptor abstractInputEventAdaptor = inputEventAdaptorFactory.getEventAdaptor();
            String adaptorType = abstractInputEventAdaptor.getInputEventAdaptorDto().getEventAdaptorTypeName();
            (EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService()).registerEventAdaptor(abstractInputEventAdaptor);
            try {
                EventReceiverServiceValueHolder.getCarbonEventReceiverService().activateInactiveEventReceiverConfigurationsForAdaptor(adaptorType);
            } catch (EventReceiverConfigurationException e) {
                log.error("Error while activating inactive event receiver for event adaptor Type " + adaptorType, e);
            } catch (Throwable t) {
                log.error("Error while deploying input event adaptor Type " + adaptorType, t);
            }
        }
    }

}
