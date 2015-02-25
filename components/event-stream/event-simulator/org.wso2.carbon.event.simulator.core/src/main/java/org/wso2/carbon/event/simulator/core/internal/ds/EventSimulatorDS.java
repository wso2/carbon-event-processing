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
package org.wso2.carbon.event.simulator.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.simulator.core.EventSimulator;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;

/**
 * @scr.component name="eventSimulatorService.component" immediate="true"
 * @scr.reference name="stream.carboneventstream.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService"
 * cardinality="1..1" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */
public class EventSimulatorDS {

    private static final Log log = LogFactory.getLog(EventSimulator.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventSimulator carbonEventSimulator=createEventSimulator();
            setEventSimulator(carbonEventSimulator);
            context.getBundleContext().registerService(EventSimulator.class.getName(), carbonEventSimulator, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventSimulator");
            }

        } catch (RuntimeException e) {
            log.error("Could not create EventSimulator : " + e.getMessage(), e);
        }
    }

    protected void setEventStreamService(EventStreamService eventstreamservice) {
        EventSimulatorValueHolder.setEventStreamService(eventstreamservice);
    }

    protected void unsetEventStreamService(EventStreamService eventstreamservice) {
        EventSimulatorValueHolder.unsetEventStreamService();
    }

    private CarbonEventSimulator createEventSimulator() {
        return new CarbonEventSimulator();
    }

    protected void setEventSimulator(CarbonEventSimulator carbonEventSimulator){
        EventSimulatorValueHolder.setEventSimulator(carbonEventSimulator);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        EventSimulatorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        EventSimulatorValueHolder.setDataSourceService(null);
    }

}
