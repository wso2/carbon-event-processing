package org.wso2.carbon.event.stream.manager.core;

import org.wso2.carbon.databridge.commons.Event;

import java.util.List;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public interface WSO2EventListConsumer extends WSO2EventConsumer{
    /**
     * This method will be triggered for all listeners whenever an event list is received
     *
     * @param event the event object which will be an instance of {@link org.wso2.carbon.databridge.commons.Event}
     */
    public void onEventList(List<Event> event);
}
