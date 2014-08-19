/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adaptor.websocket.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.websocket.WebsocketEventAdaptorFactory;

/**
 * @scr.component name="output.adaptor.websocket.component" immediate="true"
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
            log.error("Can not create the output Websocket event adaptor service: " + e);
        }
    }
}
