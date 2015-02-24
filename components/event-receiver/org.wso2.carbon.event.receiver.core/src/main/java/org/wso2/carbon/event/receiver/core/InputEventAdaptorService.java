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

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;

import java.util.List;

/**
 * OSGI interface for the EventAdaptor Service
 */

public interface InputEventAdaptorService {


    /**
     * this method returns all the available event types. UI use this details to
     * show the types and the properties to be set to the user when creating the
     * event objects.
     *
     * @return list of available types
     */
    List<InputEventAdaptorDto> getEventAdaptors();


    /**
     * subscribe to a particular event configuration. When the EventAdaptor receives the
     * message it send that to the user through the listener interface.
     *
     * @param inputEventAdaptorConfiguration - Configuration details of the event
     * @param inputEventAdaptorListener      - listener interface to notify
     */
    String subscribe(InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                     InputEventAdaptorListener inputEventAdaptorListener,
                     AxisConfiguration axisConfiguration);

    /**
     * un subscribes from the event.
     *
     * @param inputEventAdaptorConfiguration - event configuration to be used
     * @param axisConfiguration              - axis configuration
     */
    void unsubscribe(InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                     AxisConfiguration axisConfiguration, String subscriptionId);


    /**
     * This method returns the event adaptor dto for a specific event adaptor type
     *
     * @param eventAdaptorType
     * @return
     */
    InputEventAdaptorDto getEventAdaptorDto(String eventAdaptorType);


}
