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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.receive.EventReceiverStreamNotificationListener;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;

public class EventReceiverStreamNotificationListenerImpl implements EventReceiverStreamNotificationListener {

    private static final Log log = LogFactory.getLog(EventReceiverStreamNotificationListenerImpl.class);
    private EventReceiver eventReceiver;

    public EventReceiverStreamNotificationListenerImpl(EventReceiver eventReceiver) {
        this.eventReceiver = eventReceiver;
    }

    @Override
    public void addedNewEventStream(int tenantId, String streamId) {
        try {
            log.info("Trying to redeploy configuration files for stream: " + streamId);
            StormProcessorValueHolder.getStormProcessorService().addExternalStream(streamId, eventReceiver);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void removedEventStream(int tenantId, String streamId) {
        try {
            log.info("Trying to undeploy execution plans for stream: " + streamId);
            StormProcessorValueHolder.getStormProcessorService().removeExternalStream(tenantId, streamId, eventReceiver);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
}
