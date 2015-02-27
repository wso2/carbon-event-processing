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
package org.wso2.carbon.event.receiver.core;


import org.apache.log4j.Logger;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;

/**
 * listener class to receive the events from the event proxy
 */

public abstract class InputEventAdaptorListener {

    /**
     * when an event definition is defined, event calls this method with the definition.
     *
     * @param object - received event definition
     */
    public abstract void addEventDefinition(Object object);

    /**
     * when an event definition is removed event proxy call this method with the definition.
     *
     * @param object - received event definition
     */
    public abstract void removeEventDefinition(Object object);

    /**
     * when an event happens event proxy call this method with the received event.
     *
     * @param object - received event
     */
    public abstract void onEvent(Object object);


    /**
     * when an event definition is defined, event calls this method with the definition.
     *
     * @param object - received event definition
     */
    public void addEventDefinitionCall(Object object) {
        addEventDefinition(object);
    }

    /**
     * when an event definition is removed event proxy call this method with the definition.
     *
     * @param object - received event definition
     */
    public void removeEventDefinitionCall(Object object) {
        removeEventDefinition(object);
    }

    /**
     * when an event happens event proxy call this method with the received event.
     *
     * @param object - received event
     */
    public void onEventCall(Object object) {
        onEvent(object);
    }

}
