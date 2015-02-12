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
package org.wso2.carbon.event.notifier.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.notifier.core.*;
import org.wso2.carbon.event.notifier.core.internal.CarbonEventNotifierService;
import org.wso2.carbon.event.notifier.core.internal.CarbonOutputEventAdaptorService;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.List;


/**
 * @scr.component name="eventNotifierService.component" immediate="true"
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
public class EventNotifierServiceDS {
    private static final Log log = LogFactory.getLog(EventNotifierServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventNotifierService carbonEventNotifierService = new CarbonEventNotifierService();
            EventNotifierServiceValueHolder.registerEventNotifierService(carbonEventNotifierService);

            CarbonOutputEventAdaptorService carbonOutputEventAdaptorService = new CarbonOutputEventAdaptorService();
            EventNotifierServiceValueHolder.registerOutputEventAdaptorService(carbonOutputEventAdaptorService);
            context.getBundleContext().registerService(EventNotifierService.class.getName(), carbonEventNotifierService, null);

            OutputEventAdaptorService outputEventAdaptorService = new CarbonOutputEventAdaptorService();
            context.getBundleContext().registerService(OutputEventAdaptorService.class.getName(), outputEventAdaptorService, null);

            registerEventAdaptorTypes();

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventNotifierService");
            }

            EventNotifierServiceValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());

        } catch (RuntimeException e) {
            log.error("Could not create EventNotifierService : " + e.getMessage(), e);
        }
    }


    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventNotifierServiceValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventNotifierServiceValueHolder.unSetRegistryService();
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventNotifierServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventNotifierServiceValueHolder.registerEventStatisticsService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventNotifierServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventNotifierServiceValueHolder.registerEventStreamService(null);
    }


    private void registerEventAdaptorTypes() {

        List<OutputEventAdaptorFactory> outputEventAdaptorFactories = OutputEventAdaptorServiceTrackerDS.outputEventAdaptorFactories;

        for (OutputEventAdaptorFactory outputEventAdaptorFactory : outputEventAdaptorFactories) {
            try {
                AbstractOutputEventAdaptor abstractOutputEventAdaptor = outputEventAdaptorFactory.getEventAdaptor();
                ((CarbonOutputEventAdaptorService) EventNotifierServiceValueHolder.getOutputEventAdaptorService()).registerEventAdaptor(abstractOutputEventAdaptor);
                EventNotifierServiceValueHolder.getCarbonEventNotifierService().activateInactiveEventFormatterConfigurationForAdaptor(abstractOutputEventAdaptor.getOutputEventAdaptorDto().getEventAdaptorTypeName());
            } catch (Throwable t) {
                log.error("Unexpected error at initializing output event adaptor instances "
                        + outputEventAdaptorFactory + ": " + t.getMessage(), t);
            }
        }
    }

}
