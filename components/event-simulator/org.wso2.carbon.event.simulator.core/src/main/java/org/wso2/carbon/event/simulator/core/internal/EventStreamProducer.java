/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.simulator.core.internal;


import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.core.internal.ds.EventSimulatorValueHolder;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorUtil;
import org.wso2.carbon.event.stream.core.EventProducer;
import org.wso2.carbon.event.stream.core.EventProducerCallback;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

public class EventStreamProducer implements EventProducer {

    private String streamID;
    private EventProducerCallback eventProducerCallback;
    private StreamDefinition streamDefinition;

    @Override
    public String getStreamId() {
        return streamID;
    }

    @Override
    public void setCallBack(EventProducerCallback eventProducerCallback) {
        this.eventProducerCallback = eventProducerCallback;
    }

    public void setStreamID(String streamID) throws EventStreamConfigurationException {
        this.streamID = streamID;
        this.streamDefinition = EventSimulatorValueHolder.getEventStreamService().getStreamDefinition(streamID);
    }

    public void sendData(Object[] data) {
        eventProducerCallback.sendEvent(EventSimulatorUtil.getWso2Event(streamDefinition, System.currentTimeMillis(), data));
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }
}
