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
package org.wso2.carbon.event.input.adaptor.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;

import java.util.List;

/**
 * This is a EventAdaptor type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public abstract class AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(AbstractInputEventAdaptor.class);
    private InputEventAdaptorDto inputEventAdaptorDto;

    protected AbstractInputEventAdaptor() {

        init();

        this.inputEventAdaptorDto = new InputEventAdaptorDto();
        this.inputEventAdaptorDto.setEventAdaptorTypeName(this.getName());
        this.inputEventAdaptorDto.setSupportedMessageTypes(this.getSupportedInputMessageTypes());

        inputEventAdaptorDto.setAdaptorPropertyList(((this)).getInputAdaptorProperties());
    }

    public InputEventAdaptorDto getInputEventAdaptorDto() {
        return inputEventAdaptorDto;
    }

    /**
     * returns the name of the input event adaptor type
     *
     * @return event adaptor type name
     */
    protected abstract String getName();

    /**
     * To get the information regarding supported message types event adaptor
     *
     * @return List of supported input message types
     */
    protected abstract List<String> getSupportedInputMessageTypes();

    /**
     * any initialization can be done in this method
     */
    protected abstract void init();

    /**
     * the information regarding the adaptor related properties of a specific event adaptor type
     *
     * @return List of properties related to input event adaptor
     */
    protected abstract List<Property> getInputAdaptorProperties();


    /**
     * subscribe to the connection specified in the event adaptor configuration.
     *
     * @param inputEventAdaptorListener      - event type will invoke this when it receive events
     * @param inputEventAdaptorConfiguration - event adaptor configuration details
     */
    public abstract String subscribe(
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration);

    /**
     * this method unsubscribes the subscription from the event adaptor.
     *
     * @param inputEventAdaptorConfiguration - event adaptor configuration
     */
    public abstract void unsubscribe(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId);


}
