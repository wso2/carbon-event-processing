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

package org.wso2.carbon.event.adaptor.utils.websocket.server.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.adaptor.utils.websocket.server.WebsocketService;
import org.wso2.carbon.event.adaptor.utils.websocket.server.internal.CarbonWebsocketHandler;
import org.wso2.carbon.event.adaptor.utils.websocket.server.internal.CarbonWebsocketService;

/**
 * @scr.component name="dashboard.websocket.service.component" immediate="true"
 */

public class WebsocketServiceDS {

    private static final Log log = LogFactory.getLog(WebsocketServiceDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            Server server = new Server(9099);
            WebSocketHandler wsHandler = new WebSocketHandler() {
                @Override
                public void configure(WebSocketServletFactory factory) {
                    factory.register(CarbonWebsocketHandler.class);
                }
            };
            server.setHandler(wsHandler);
            server.start();  //After the server starts, but before server.join() gets called, registering of websocketService must be done.

            WebsocketService websocketService = (WebsocketService) new CarbonWebsocketService();
            WebsocketServiceValueHolder.registerCarbonEventService(websocketService);

            context.getBundleContext().registerService(WebsocketService.class.getName(), websocketService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed web socket server");
            }

            server.join();

        } catch (RuntimeException e) {
            log.error("Can not deploy web socket server ", e);
        } catch (Exception e){
            log.error("Socket server start-up failed",e);
        } catch (Throwable t){
            log.error(t);
        }
    }
}
