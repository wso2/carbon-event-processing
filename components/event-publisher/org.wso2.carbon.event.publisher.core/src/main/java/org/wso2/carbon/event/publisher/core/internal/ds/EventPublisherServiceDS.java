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
package org.wso2.carbon.event.publisher.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.publisher.core.*;
import org.wso2.carbon.event.publisher.core.adapter.AbstractOutputEventAdapter;
import org.wso2.carbon.event.publisher.core.internal.CarbonEventPublisherService;
import org.wso2.carbon.event.publisher.core.internal.CarbonOutputEventAdaptorService;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.List;


/**
 * @scr.component name="eventPublisherService.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 */
public class EventPublisherServiceDS {
    private static final Log log = LogFactory.getLog(EventPublisherServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventPublisherService carbonEventPublisherService = new CarbonEventPublisherService();
            EventPublisherServiceValueHolder.registerEventPublisherService(carbonEventPublisherService);

            CarbonOutputEventAdaptorService carbonOutputEventAdaptorService = new CarbonOutputEventAdaptorService();
            EventPublisherServiceValueHolder.registerOutputEventAdaptorService(carbonOutputEventAdaptorService);
            context.getBundleContext().registerService(EventPublisherService.class.getName(), carbonEventPublisherService, null);

            OutputEventAdaptorService outputEventAdaptorService = new CarbonOutputEventAdaptorService();
            context.getBundleContext().registerService(OutputEventAdaptorService.class.getName(), outputEventAdaptorService, null);

            registerEventAdaptorTypes();

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventPublisherService");
            }

            EventPublisherServiceValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());

        } catch (RuntimeException e) {
            log.error("Could not create EventPublisherService : " + e.getMessage(), e);
        }
    }


    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventPublisherServiceValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventPublisherServiceValueHolder.unSetRegistryService();
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventPublisherServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventPublisherServiceValueHolder.registerEventStatisticsService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventPublisherServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventPublisherServiceValueHolder.registerEventStreamService(null);
    }


    private void registerEventAdaptorTypes() {

        List<OutputEventAdaptorFactory> outputEventAdaptorFactories = OutputEventAdaptorServiceTrackerDS.outputEventAdaptorFactories;

        for (OutputEventAdaptorFactory outputEventAdaptorFactory : outputEventAdaptorFactories) {
            try {
                AbstractOutputEventAdapter abstractOutputEventAdapter = outputEventAdaptorFactory.getEventAdaptor();
                ((CarbonOutputEventAdaptorService) EventPublisherServiceValueHolder.getOutputEventAdaptorService()).registerEventAdaptor(abstractOutputEventAdapter);
                EventPublisherServiceValueHolder.getCarbonEventPublisherService().activateInactiveEventFormatterConfigurationForAdaptor(abstractOutputEventAdapter.getOutputEventAdapterDto().getEventAdaptorTypeName());
            } catch (Throwable t) {
                log.error("Unexpected error at initializing output event adaptor instances "
                        + outputEventAdaptorFactory + ": " + t.getMessage(), t);
            }
        }
    }

}
