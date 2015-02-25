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
package org.wso2.carbon.event.receiver.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.receiver.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorDto;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorListener;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.exception.InputEventAdaptorEventProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdaptor service implementation.
 */
public class CarbonInputEventAdaptorService implements InputEventAdaptorService {

    private static Log log = LogFactory.getLog(CarbonInputEventAdaptorService.class);
    private Map<String, AbstractInputEventAdaptor> eventAdaptorMap;

    public CarbonInputEventAdaptorService() {
        this.eventAdaptorMap = new ConcurrentHashMap<String, AbstractInputEventAdaptor>();
    }

    public void registerEventAdaptor(AbstractInputEventAdaptor abstractInputEventAdaptor) {
        InputEventAdaptorDto inputEventAdaptorDto = abstractInputEventAdaptor.getInputEventAdaptorDto();
        this.eventAdaptorMap.put(inputEventAdaptorDto.getEventAdaptorTypeName(), abstractInputEventAdaptor);
    }

    public void unRegisterEventAdaptor(AbstractInputEventAdaptor abstractInputEventAdaptor) {
        InputEventAdaptorDto inputEventAdaptorDto = abstractInputEventAdaptor.getInputEventAdaptorDto();
        this.eventAdaptorMap.remove(inputEventAdaptorDto.getEventAdaptorTypeName());
    }

    @Override
    public List<InputEventAdaptorDto> getEventAdaptors() {
        List<InputEventAdaptorDto> inputEventAdaptorDtos = new ArrayList<InputEventAdaptorDto>();
        for (AbstractInputEventAdaptor abstractEventAdaptor : this.eventAdaptorMap.values()) {
            inputEventAdaptorDtos.add(abstractEventAdaptor.getInputEventAdaptorDto());
        }
        return inputEventAdaptorDtos;
    }


    @Override
    public String subscribe(InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            AxisConfiguration axisConfiguration) {
        AbstractInputEventAdaptor inputEventAdaptor = this.eventAdaptorMap.get(inputEventAdaptorConfiguration.getInputEventAdaptorType());

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return inputEventAdaptor.subscribe(inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration);
        } catch (InputEventAdaptorEventProcessingException e) {
            log.error(e.getMessage(), e);
            throw new InputEventAdaptorEventProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {
        AbstractInputEventAdaptor abstractInputEventAdaptor = this.eventAdaptorMap.get(inputEventAdaptorConfiguration.getInputEventAdaptorType());
        // We do not throw an error when trying to unsubscribing from an input event adaptor that is not there.
        if (abstractInputEventAdaptor != null) {
            try {
                abstractInputEventAdaptor.unsubscribe(inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
            } catch (InputEventAdaptorEventProcessingException e) {
                log.error(e.getMessage(), e);
                throw new InputEventAdaptorEventProcessingException(e.getMessage(), e);
            }
        }
    }

    @Override
    public InputEventAdaptorDto getEventAdaptorDto(String eventAdaptorType) {

        AbstractInputEventAdaptor abstractInputEventAdaptor = eventAdaptorMap.get(eventAdaptorType);
        if (abstractInputEventAdaptor != null) {
            return abstractInputEventAdaptor.getInputEventAdaptorDto();
        }
        return null;
    }


}
