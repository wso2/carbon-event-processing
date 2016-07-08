/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.simulator.core.internal.ds;

import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.simulator.core.internal.CarbonEventSimulator;
import org.wso2.carbon.event.simulator.core.internal.EventStreamProducer;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventSimulatorValueHolder {
    private static EventStreamService eventstreamservice;
    private static CarbonEventSimulator eventSimulator;
    private static DataSourceService dataSourceService;
    private static EventManagementService eventManagerService;
    private static Map<Integer, Map<String, EventStreamProducer>> eventProducerMap = new ConcurrentHashMap<>();

    private EventSimulatorValueHolder() {

    }

    public static void setEventStreamService(EventStreamService eventstreamservice) {
        EventSimulatorValueHolder.eventstreamservice = eventstreamservice;
    }

    public static void unsetEventStreamService() {
        EventSimulatorValueHolder.eventstreamservice = null;
    }

    public static EventStreamService getEventStreamService() {
        return EventSimulatorValueHolder.eventstreamservice;
    }

    public static void setEventSimulator(CarbonEventSimulator eventSimulator) {
        EventSimulatorValueHolder.eventSimulator = eventSimulator;
    }

    public static CarbonEventSimulator getEventSimulator() {
        return EventSimulatorValueHolder.eventSimulator;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        EventSimulatorValueHolder.dataSourceService = dataSourceService;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setEventManagementService(EventManagementService eventManagerService) {
        EventSimulatorValueHolder.eventManagerService = eventManagerService;
    }

    public static EventManagementService getEventManagementService() {
        return eventManagerService;
    }

    public static Map<String, EventStreamProducer> getEventProducerMap(int tenantID) {
        return eventProducerMap.get(tenantID);
    }

    public static void setEventProducerMap(int tenantID) {
        eventProducerMap.put(tenantID, new ConcurrentHashMap<String, EventStreamProducer>());
    }
}
