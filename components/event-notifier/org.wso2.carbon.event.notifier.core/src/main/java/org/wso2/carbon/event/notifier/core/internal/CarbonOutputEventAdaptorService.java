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
package org.wso2.carbon.event.notifier.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.notifier.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.config.EventNotifier;
import org.wso2.carbon.event.notifier.core.config.EndpointAdaptorConfiguration;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierProcessingException;
import org.wso2.carbon.event.notifier.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdaptor service implementation.
 */
public class CarbonOutputEventAdaptorService implements OutputEventAdaptorService {

    private static Log log = LogFactory.getLog(CarbonOutputEventAdaptorService.class);
    private Map<String, AbstractOutputEventAdaptor> eventAdaptorMap;

    public CarbonOutputEventAdaptorService() {
        this.eventAdaptorMap = new ConcurrentHashMap();
    }

    public void registerEventAdaptor(
            AbstractOutputEventAdaptor abstractOutputEventAdaptor) {
        OutputEventAdaptorDto outputEventAdaptorDto = abstractOutputEventAdaptor.getOutputEventAdaptorDto();
        this.eventAdaptorMap.put(outputEventAdaptorDto.getEventAdaptorTypeName(), abstractOutputEventAdaptor);
    }

    public void unRegisterEventAdaptor(
            AbstractOutputEventAdaptor abstractOutputEventAdaptor) {
        OutputEventAdaptorDto outputEventAdaptorDto = abstractOutputEventAdaptor.getOutputEventAdaptorDto();
        this.eventAdaptorMap.remove(outputEventAdaptorDto.getEventAdaptorTypeName());
    }


    @Override
    public List<OutputEventAdaptorDto> getEventAdaptors() {
        List<OutputEventAdaptorDto> outputEventAdaptorDtos = new ArrayList<OutputEventAdaptorDto>();
        for (AbstractOutputEventAdaptor abstractEventAdaptor : this.eventAdaptorMap.values()) {
            outputEventAdaptorDtos.add(abstractEventAdaptor.getOutputEventAdaptorDto());
        }
        return outputEventAdaptorDtos;
    }


    @Override
    public void publish(String eventNotifierName,
                        Object object, int tenantId) {
        try {

            CarbonEventNotifierService eventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();
            Map<Integer, Map<String, EventNotifier>> eventNotifierMap = eventNotifierService.getTenantSpecificEventNotifierConfigurationMap();

            if (eventNotifierMap != null) {
                Map<String, EventNotifier> tenantSpecificEventNotifierMap = eventNotifierMap.get(tenantId);
                EventNotifier eventNotifier = tenantSpecificEventNotifierMap.get(eventNotifierName);
                EndpointAdaptorConfiguration endpointAdaptorConfiguration = eventNotifier.getEventNotifierConfiguration().getEndpointAdaptorConfiguration();
                AbstractOutputEventAdaptor outputEventAdaptor = this.eventAdaptorMap.get(endpointAdaptorConfiguration.getEndpointType());
                outputEventAdaptor.publishCall(object, endpointAdaptorConfiguration, tenantId);
            }
        } catch (EventNotifierProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EventNotifierProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void publish(EndpointAdaptorConfiguration endpointAdaptorConfiguration, Object object, int tenantId) {
        AbstractOutputEventAdaptor outputEventAdaptor = this.eventAdaptorMap.get(endpointAdaptorConfiguration.getEndpointType());
        outputEventAdaptor.publishCall(object, endpointAdaptorConfiguration, tenantId);

    }


    @Override
    public void testConnection(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration) {
        AbstractOutputEventAdaptor outputEventAdaptor = this.eventAdaptorMap.get(endpointAdaptorConfiguration.getEndpointType());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            outputEventAdaptor.testConnection(endpointAdaptorConfiguration, tenantId);
        } catch (TestConnectionUnavailableException e) {
            throw new TestConnectionUnavailableException(e.getMessage(), e);
        } catch (EventNotifierProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EventNotifierProcessingException(e.getMessage(), e);
        }
    }


    @Override
    public OutputEventAdaptorDto getEventAdaptorDto(String eventAdaptorType) {

        AbstractOutputEventAdaptor abstractOutputEventAdaptor = eventAdaptorMap.get(eventAdaptorType);
        if (abstractOutputEventAdaptor != null) {
            return abstractOutputEventAdaptor.getOutputEventAdaptorDto();
        }
        return null;
    }

    @Override
    public void removeConnectionInfo(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        AbstractOutputEventAdaptor outputEventAdaptor = this.eventAdaptorMap.get(endpointAdaptorConfiguration.getEndpointType());
        try {
            outputEventAdaptor.removeConnectionInfo(endpointAdaptorConfiguration, tenantId);
        } catch (EventNotifierProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EventNotifierProcessingException(e.getMessage(), e);
        }
    }


}
