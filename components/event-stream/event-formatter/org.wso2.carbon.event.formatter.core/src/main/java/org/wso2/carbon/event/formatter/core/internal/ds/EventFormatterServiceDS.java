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
package org.wso2.carbon.event.formatter.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.EventStreamListenerImpl;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.EventAdaptorNotificationListenerImpl;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;


/**
 * @scr.component name="eventFormatterService.component" immediate="true"
 * @scr.reference name="eventAdaptor.service"
 * interface="org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorService" unbind="unsetEventAdaptorService"
 * @scr.reference name="event.adaptor.manager.service"
 * interface="org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorManagerService" unbind="unSetEventAdaptorManagerService"
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
public class EventFormatterServiceDS {
    private static final Log log = LogFactory.getLog(EventFormatterServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            CarbonEventFormatterService carbonEventFormatterService = new CarbonEventFormatterService();
            EventFormatterServiceValueHolder.registerFormatterService(carbonEventFormatterService);
            context.getBundleContext().registerService(EventFormatterService.class.getName(), carbonEventFormatterService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventFormatterService");
            }

            EventFormatterServiceValueHolder.getOutputEventAdaptorManagerService().registerDeploymentNotifier(new EventAdaptorNotificationListenerImpl());
            EventFormatterServiceValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());

        } catch (RuntimeException e) {
            log.error("Could not create EventFormatterService : " + e.getMessage(), e);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            log.error("Could not register deployment notifier to event adaptor service");
        }
    }

    protected void setEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventFormatterServiceValueHolder.registerEventAdaptorService(eventAdaptorService);
    }

    protected void unsetEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventFormatterServiceValueHolder.registerEventAdaptorService(null);
    }

    protected void setEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventAdaptorManagerService) {
        EventFormatterServiceValueHolder.registerEventAdaptorManagerService(eventAdaptorManagerService);
    }

    protected void unSetEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventAdaptorManagerService) {
        EventFormatterServiceValueHolder.unRegisterEventAdaptorManagerService(eventAdaptorManagerService);

    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventFormatterServiceValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventFormatterServiceValueHolder.unSetRegistryService();
    }

    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventFormatterServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventFormatterServiceValueHolder.registerEventStatisticsService(null);
    }

    public void setEventStreamService(EventStreamService eventStreamService) {
        EventFormatterServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    public void unsetEventStreamService(EventStreamService eventStreamService) {
        EventFormatterServiceValueHolder.registerEventStreamService(null);
    }
}
