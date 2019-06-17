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
package org.wso2.carbon.event.simulator.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.simulator.core.EventSimulator;
import org.wso2.carbon.event.simulator.core.internal.CarbonEventSimulator;
import org.wso2.carbon.event.simulator.core.internal.util.EventStreamListenerImpl;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "eventSimulatorService.component",
        immediate = true)
public class EventSimulatorDS {

    private static final Log log = LogFactory.getLog(EventSimulator.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            CarbonEventSimulator carbonEventSimulator = createEventSimulator();
            setEventSimulator(carbonEventSimulator);
            context.getBundleContext().registerService(EventSimulator.class.getName(), carbonEventSimulator, null);
            context.getBundleContext().registerService(EventStreamListener.class.getName(), new
                    EventStreamListenerImpl(), null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventSimulator");
            }
        } catch (RuntimeException e) {
            log.error("Could not create EventSimulator : " + e.getMessage(), e);
        }
    }

    @Reference(
            name = "stream.carboneventstream.service",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventstreamservice) {

        EventSimulatorValueHolder.setEventStreamService(eventstreamservice);
    }

    protected void unsetEventStreamService(EventStreamService eventstreamservice) {

        EventSimulatorValueHolder.unsetEventStreamService();
    }

    private CarbonEventSimulator createEventSimulator() {

        return new CarbonEventSimulator();
    }

    protected void setEventSimulator(CarbonEventSimulator carbonEventSimulator) {

        EventSimulatorValueHolder.setEventSimulator(carbonEventSimulator);
    }

    @Reference(
            name = "org.wso2.carbon.ndatasource",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {

        EventSimulatorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {

        EventSimulatorValueHolder.setDataSourceService(null);
    }

    @Reference(
            name = "eventManagement.service",
            service = org.wso2.carbon.event.processor.manager.core.EventManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventManagementService")
    protected void setEventManagementService(EventManagementService eventManagementService) {

        EventSimulatorValueHolder.setEventManagementService(eventManagementService);
    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {

        EventSimulatorValueHolder.setEventManagementService(null);
    }
}
