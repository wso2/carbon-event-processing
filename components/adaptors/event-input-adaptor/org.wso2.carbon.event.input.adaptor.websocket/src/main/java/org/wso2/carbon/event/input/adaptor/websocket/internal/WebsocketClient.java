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

package org.wso2.carbon.event.input.adaptor.websocket.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.util.logging.Logger;

/**
 * An instance of this class is used, when we connect to a user-given socket-server, as a client - to send events.
 */
public class WebsocketClient extends Endpoint {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private static final Log log = LogFactory.getLog(WebsocketClient.class);
    private InputEventAdaptorListener inputEventAdaptorListener;

    public WebsocketClient(InputEventAdaptorListener inputEventAdaptorListener) {
        this.inputEventAdaptorListener = inputEventAdaptorListener;
    }

    @Override
    public void onOpen(Session session, EndpointConfig EndpointConfig) {
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                    if (log.isDebugEnabled()){
                        log.debug("Received message: " + message);
                    }
                    inputEventAdaptorListener.onEventCall(message);
            }
        });
    }

    @Override
    public void onClose(javax.websocket.Session session, javax.websocket.CloseReason closeReason) {
        if (log.isDebugEnabled()){
            log.debug("Input ws-adaptor: WebsocketClient Endpoint closed: "+closeReason.toString()+"for request URI - "+session.getRequestURI());
        }
    }


    @Override
    public void onError(javax.websocket.Session session, java.lang.Throwable thr) {
        log.error("Error occured during session ID:"+session.getId()+", for request URI - "+session.getRequestURI()+", Reason: "+thr);   //TODO: log everything   - done
    }

}
