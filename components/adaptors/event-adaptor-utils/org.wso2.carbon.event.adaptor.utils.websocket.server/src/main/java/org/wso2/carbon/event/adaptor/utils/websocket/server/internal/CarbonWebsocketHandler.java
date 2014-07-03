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
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.wso2.carbon.event.adaptor.utils.websocket.server.internal.ds.WebsocketServiceValueHolder;

/**
 * Websocket server implementation
 */
@WebSocket
public class CarbonWebsocketHandler {

    private static Log log = LogFactory.getLog(CarbonWebsocketHandler.class);

    public CarbonWebsocketHandler() {
    }

    RemoteEndpoint subscriber;


    @OnWebSocketConnect
    public void onConnect(Session session) {
        subscriber = session.getRemote();
        String topic = session.getUpgradeRequest().getRequestURI().getPath().substring(1);

        WebsocketServiceValueHolder.getCarbonWebsocketService().subscribe(topic,subscriber);  //TODO: check what to do if topic does not exist in the map as a key
        if (log.isDebugEnabled()){
            log.debug("Client connected to web socket server. Client address:" + session.getRemoteAddress());
        }
    }


    @OnWebSocketMessage
    public void onMessage(String message) {
        if (log.isDebugEnabled()){
            log.debug("Client sent a message to server. Message:" + message);
        }
    }


    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (log.isDebugEnabled()){
            log.debug("Websocket Server closed connection: statusCode=" + statusCode + ", reason=" + reason);
        }
    }


    @OnWebSocketError
    public void onError(Throwable t) {
        log.error("Error: " + t.getMessage());
    }
}
