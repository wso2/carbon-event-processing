/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.websocket.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CopyOnWriteArrayList;


public class WebsocketLocalInputService {

    private static final Log log = LogFactory.getLog(WebsocketLocalInputService.class);

    CopyOnWriteArrayList<WebsocketLocalEventAdaptorType.WebsocketAdaptorListener> topicSpecificListeners;

    public void invokeListener(int tenantId, String adaptorName, String topic, final String message){

        topicSpecificListeners = WebsocketLocalEventAdaptorType.getTopicSpecificListeners(tenantId, adaptorName, topic);
        if(topicSpecificListeners != null){
            for (WebsocketLocalEventAdaptorType.WebsocketAdaptorListener listener: topicSpecificListeners){
                listener.getInputeventadaptorlistener().onEventCall(message);
            }
        }
    }
}
