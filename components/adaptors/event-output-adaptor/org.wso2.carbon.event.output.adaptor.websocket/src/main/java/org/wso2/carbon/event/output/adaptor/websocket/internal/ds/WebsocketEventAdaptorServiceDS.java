/*
 * Copyright 2013 The Apache Software Foundation.
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
package org.wso2.carbon.event.output.adaptor.websocket.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

import org.wso2.carbon.event.adaptor.utils.websocket.server.WebsocketService;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.websocket.WebsocketEventAdaptorFactory;

/**
 * @scr.component name="output.adaptor.websocket.component" immediate="true"
 * @scr.reference name="server.WebsocketService"
 * interface="org.wso2.carbon.event.adaptor.utils.websocket.server.WebsocketService" cardinality="1..1"
 * policy="dynamic" bind="setWebsocketService" unbind="unsetWebsocketService"
 */
public class WebsocketEventAdaptorServiceDS {
    
    private static final Log log = LogFactory.getLog(WebsocketEventAdaptorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            WebsocketEventAdaptorFactory websocketEventAdaptorFactory = new WebsocketEventAdaptorFactory();
            context.getBundleContext().registerService(OutputEventAdaptorFactory.class.getName(),
            		websocketEventAdaptorFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output Websocket event adaptor service");
            }
        } catch (Throwable e) {
            log.error("Can not create the output Websocket event adaptor service: " + e.getMessage(), e);
        }
    }

    public void setWebsocketService(WebsocketService websocketService) {
        WebsocketEventAdaptorServiceValueHolder.registerWebsocketService(websocketService);
    }

    public void unsetWebsocketService(WebsocketService websocketService) {
        WebsocketEventAdaptorServiceValueHolder.registerWebsocketService(null);
    }
    
}
