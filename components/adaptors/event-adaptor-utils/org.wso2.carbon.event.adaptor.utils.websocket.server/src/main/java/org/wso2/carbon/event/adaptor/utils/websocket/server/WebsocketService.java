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

package org.wso2.carbon.event.adaptor.utils.websocket.server;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import java.util.ArrayList;

/**
 * OSGI interface for the Websocket Service
 */

public interface WebsocketService {

    public void publish();

    public void subscribe(String topic, RemoteEndpoint subscriber);

    /*
    * This method allows an output adaptor to get the list of subscribers for a given topic; Hence, it is part of the interface.
    * */
    public ArrayList<RemoteEndpoint> getSubscribers(String topic);
}
