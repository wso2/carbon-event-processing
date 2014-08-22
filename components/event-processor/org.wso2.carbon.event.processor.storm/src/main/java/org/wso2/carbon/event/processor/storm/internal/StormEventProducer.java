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
package org.wso2.carbon.event.processor.storm.internal;

import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.processor.api.send.EventProducerStreamNotificationListener;
import org.wso2.carbon.event.processor.api.send.EventSender;
import org.wso2.carbon.event.processor.api.send.exception.EventProducerException;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;


public class StormEventProducer implements EventProducer {


    private CarbonStormProcessorService carbonStormProcessorService;

    public StormEventProducer(CarbonStormProcessorService carbonStormProcessorService) {
        this.carbonStormProcessorService = carbonStormProcessorService;
    }

    @Override
    public void subscribe(int tenantId, String streamId, EventSender eventSender) throws EventProducerException {
        carbonStormProcessorService.subscribeStreamListener(streamId, eventSender);
    }

    @Override
    public void subscribe(String streamId, EventSender eventSender) throws EventProducerException {
        carbonStormProcessorService.subscribeStreamListener(streamId, eventSender);
    }

    @Override
    public void unsubscribe(int tenantId, String streamId, EventSender eventSender) throws EventProducerException {
        carbonStormProcessorService.unsubscribeStreamListener(streamId, eventSender, tenantId);
    }

    @Override
    public void subscribeNotificationListener(EventProducerStreamNotificationListener eventProducerStreamNotificationListener) {
        StormProcessorValueHolder.registerNotificationListener(eventProducerStreamNotificationListener);
        StormProcessorValueHolder.getStormProcessorService().notifyAllStreamsToFormatter();
    }

    @Override
    public void registerPassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        carbonStormProcessorService.registerPassthroughSenderConfigurator(passthroughSenderConfigurator);
    }
}
