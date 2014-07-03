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

package org.wso2.carbon.event.adaptor.utils.websocket.server.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.wso2.carbon.event.adaptor.utils.websocket.server.WebsocketService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * EventAdaptor service implementation.
 */
public class CarbonWebsocketService implements WebsocketService {

    private static Log log = LogFactory.getLog(CarbonWebsocketService.class);

    private HashMap<String,ArrayList<RemoteEndpoint>> subscribersMap = new HashMap<String, ArrayList<RemoteEndpoint>>();

    public CarbonWebsocketService() {
    }

    @Override
    public void publish() {

    }

    @Override
    public void subscribe(String topic, RemoteEndpoint subscriber) {
        ArrayList<RemoteEndpoint> addrList = subscribersMap.get(topic);
        if (addrList == null){
            addrList = new ArrayList<RemoteEndpoint>();
            addrList.add(subscriber);
            subscribersMap.put(topic,addrList);
        }
        else{
            addrList.add(subscriber);
        }
    }

    @Override
    public ArrayList<RemoteEndpoint> getSubscribers(String topic) {
        return subscribersMap.get(topic);
    }
}
