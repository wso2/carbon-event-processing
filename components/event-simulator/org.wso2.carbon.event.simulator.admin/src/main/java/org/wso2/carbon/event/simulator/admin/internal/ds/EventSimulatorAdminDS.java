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
package org.wso2.carbon.event.simulator.admin.internal.ds;


import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.simulator.admin.internal.util.EventSimulatorAdminvalueHolder;
import org.wso2.carbon.event.simulator.core.EventSimulator;
import org.wso2.carbon.ndatasource.core.DataSourceService;

/**
 * This class is used to get the Event Simulator service.
 *
 * @scr.component name="eventSimulatorAdmin.component" immediate="true"
 * @scr.reference name="eventSimulatorService.component"
 * interface="org.wso2.carbon.event.simulator.core.EventSimulator" cardinality="1..1"
 * policy="dynamic" bind="setEventSimulatorService" unbind="unsetEventSimulatorService"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 */

public class EventSimulatorAdminDS {

    protected void activate(ComponentContext context) {


    }

    protected void setEventSimulatorService(EventSimulator eventSimulatorService) {
        EventSimulatorAdminvalueHolder.registerEventSimulator(eventSimulatorService);
    }

    protected void unsetEventSimulatorService(EventSimulator eventSimulatorService) {

    }


    protected void setDataSourceService(DataSourceService dataSourceService) {
        EventSimulatorAdminvalueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        EventSimulatorAdminvalueHolder.setDataSourceService(null);
    }
}
