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
package org.wso2.carbon.event.output.adaptor.manager.core.internal.ds;

import org.wso2.carbon.event.output.adaptor.manager.core.internal.CarbonOutputEventAdaptorManagerService;
import org.wso2.carbon.event.statistics.EventStatisticsService;

/**
 * To hold the event adaptor manager service
 */
public class OutputEventAdaptorManagerValueHolder {

    private static CarbonOutputEventAdaptorManagerService carbonEventAdaptorManagerService;
    private static EventStatisticsService eventStatisticsService;

    private OutputEventAdaptorManagerValueHolder() {
    }

    public static void registerCarbonEventAdaptorManagerService(
            CarbonOutputEventAdaptorManagerService carbonEventAdaptorManagerService) {

        OutputEventAdaptorManagerValueHolder.carbonEventAdaptorManagerService = carbonEventAdaptorManagerService;
    }

    public static CarbonOutputEventAdaptorManagerService getCarbonEventAdaptorManagerService() {
        return OutputEventAdaptorManagerValueHolder.carbonEventAdaptorManagerService;
    }

    public static void registerEventStatisticsService(
            EventStatisticsService eventStatisticsService) {
        OutputEventAdaptorManagerValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }
}
