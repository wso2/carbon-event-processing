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

import org.wso2.carbon.event.stream.manager.core.EventStreamService;

public class EventSimulatorValueHolder {
    private static EventStreamService eventstreamservice;

    private static CarbonEventSimulator eventSimulator;

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

    public static void setEventSimulator(CarbonEventSimulator eventSimulator){
        EventSimulatorValueHolder.eventSimulator=eventSimulator;
    }

    public static CarbonEventSimulator getEventSimulator(){
        return EventSimulatorValueHolder.eventSimulator;
    }
}
