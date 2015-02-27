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
package org.wso2.carbon.event.publisher.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.publisher.core.adapter.AbstractOutputEventAdapter;
import org.wso2.carbon.event.publisher.core.adapter.OutputEventAdapterDto;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorService;
import org.wso2.carbon.event.publisher.core.config.EventPublisher;
import org.wso2.carbon.event.publisher.core.config.OutputAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherProcessingException;
import org.wso2.carbon.event.publisher.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventAdaptor service implementation.
 */
public class CarbonOutputEventAdaptorService implements OutputEventAdaptorService {

    private static Log log = LogFactory.getLog(CarbonOutputEventAdaptorService.class);
    private Map<String, AbstractOutputEventAdapter> eventAdaptorMap;

    public CarbonOutputEventAdaptorService() {
        this.eventAdaptorMap = new ConcurrentHashMap();
    }

    public void registerEventAdaptor(
            AbstractOutputEventAdapter abstractOutputEventAdapter) {
        OutputEventAdapterDto outputEventAdapterDto = abstractOutputEventAdapter.getOutputEventAdapterDto();
        this.eventAdaptorMap.put(outputEventAdapterDto.getEventAdaptorTypeName(), abstractOutputEventAdapter);
    }

    public void unRegisterEventAdaptor(
            AbstractOutputEventAdapter abstractOutputEventAdapter) {
        OutputEventAdapterDto outputEventAdapterDto = abstractOutputEventAdapter.getOutputEventAdapterDto();
        this.eventAdaptorMap.remove(outputEventAdapterDto.getEventAdaptorTypeName());
    }


    @Override
    public List<OutputEventAdapterDto> getEventAdaptors() {
        List<OutputEventAdapterDto> outputEventAdapterDtos = new ArrayList<OutputEventAdapterDto>();
        for (AbstractOutputEventAdapter abstractEventAdaptor : this.eventAdaptorMap.values()) {
            outputEventAdapterDtos.add(abstractEventAdaptor.getOutputEventAdapterDto());
        }
        return outputEventAdapterDtos;
    }


    @Override
    public void publish(String eventPublisherName,
                        Object object, int tenantId) {
        try {

            CarbonEventPublisherService eventPublisherService = EventPublisherServiceValueHolder.getCarbonEventPublisherService();
            Map<Integer, Map<String, EventPublisher>> eventPublisherMap = eventPublisherService.getTenantSpecificEventPublisherConfigurationMap();

            if (eventPublisherMap != null) {
                Map<String, EventPublisher> tenantSpecificEventPublisherMap = eventPublisherMap.get(tenantId);
                EventPublisher eventPublisher = tenantSpecificEventPublisherMap.get(eventPublisherName);
                OutputAdaptorConfiguration outputAdaptorConfiguration = eventPublisher.getEventPublisherConfiguration().getOutputAdaptorConfiguration();
                AbstractOutputEventAdapter outputEventAdaptor = this.eventAdaptorMap.get(outputAdaptorConfiguration.getEndpointType());
                outputEventAdaptor.publishCall(object, outputAdaptorConfiguration, tenantId);
            }
        } catch (EventPublisherProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EventPublisherProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void publish(OutputAdaptorConfiguration outputAdaptorConfiguration, Object object, int tenantId) {
        AbstractOutputEventAdapter outputEventAdaptor = this.eventAdaptorMap.get(outputAdaptorConfiguration.getEndpointType());
        outputEventAdaptor.publishCall(object, outputAdaptorConfiguration, tenantId);

    }


    @Override
    public void testConnection(
            OutputAdaptorConfiguration outputAdaptorConfiguration) {
        AbstractOutputEventAdapter outputEventAdaptor = this.eventAdaptorMap.get(outputAdaptorConfiguration.getEndpointType());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            outputEventAdaptor.testConnection(outputAdaptorConfiguration, tenantId);
        } catch (TestConnectionUnavailableException e) {
            throw new TestConnectionUnavailableException(e.getMessage(), e);
        } catch (EventPublisherProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EventPublisherProcessingException(e.getMessage(), e);
        }
    }


    @Override
    public OutputEventAdapterDto getEventAdaptorDto(String eventAdaptorType) {

        AbstractOutputEventAdapter abstractOutputEventAdapter = eventAdaptorMap.get(eventAdaptorType);
        if (abstractOutputEventAdapter != null) {
            return abstractOutputEventAdapter.getOutputEventAdapterDto();
        }
        return null;
    }

    @Override
    public void removeConnectionInfo(
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {
        AbstractOutputEventAdapter outputEventAdaptor = this.eventAdaptorMap.get(outputAdaptorConfiguration.getEndpointType());
        try {
            outputEventAdaptor.removeConnectionInfo(outputAdaptorConfiguration, tenantId);
        } catch (EventPublisherProcessingException e) {
            log.error(e.getMessage(), e);
            throw new EventPublisherProcessingException(e.getMessage(), e);
        }
    }


}
