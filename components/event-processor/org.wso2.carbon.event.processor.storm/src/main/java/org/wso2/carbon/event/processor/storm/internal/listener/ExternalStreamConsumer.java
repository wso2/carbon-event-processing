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
package org.wso2.carbon.event.processor.storm.internal.listener;

import org.wso2.carbon.event.processor.api.send.EventSender;
import org.wso2.carbon.event.processor.storm.internal.stream.EventConsumer;
import org.wso2.siddhi.core.event.Event;

public class ExternalStreamConsumer implements EventConsumer {

    private EventSender eventSender;
    private Object owner;

    public ExternalStreamConsumer(EventSender eventSender, Object owner) {
        this.eventSender = eventSender;
        this.owner = owner;
    }

    @Override
    public void consumeEvents(Object[][] events) {
        for (Object[] eventData : events) {
            eventSender.sendEventData(eventData);
        }
    }

    @Override
    public void consumeEvents(Event[] events) {
        for (Event event : events) {
            eventSender.sendEventData(event.getData());
        }
    }

    @Override
    public void consumeEvent(Object[] eventData) {
        eventSender.sendEventData(eventData);
    }

    @Override
    public void consumeEvent(Event event) {
        eventSender.sendEventData(event.getData());
    }

    @Override
    public Object getOwner() {
        return owner;
    }

}
