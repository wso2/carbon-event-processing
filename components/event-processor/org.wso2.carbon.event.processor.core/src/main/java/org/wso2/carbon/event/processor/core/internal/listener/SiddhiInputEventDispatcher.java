/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.event.processor.core.internal.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;

/**
 * Feed incoming events to Siddhi engine through Siddhi input handler
 */
public class SiddhiInputEventDispatcher extends AbstractSiddhiInputEventDispatcher {
    private Logger trace = Logger.getLogger(EventProcessorConstants.EVENT_TRACE_LOGGER);
    private static Log log = LogFactory.getLog(SiddhiInputEventDispatcher.class);

    /**
     * Siddhi Input event handler
     */
    protected InputHandler inputHandler;

    public SiddhiInputEventDispatcher(String streamId, InputHandler inputHandler, ExecutionPlanConfiguration executionPlanConfiguration, int tenantId) {
        super(streamId, inputHandler.getStreamId(), executionPlanConfiguration, tenantId);
        this.inputHandler = inputHandler;
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        sendEvent(event.getData());
    }

    @Override
    public void sendEvent(Object[] eventData) throws InterruptedException {
        inputHandler.send(eventData);
    }

}
