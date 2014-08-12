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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adaptor.websocket.local.WebsocketLocalOutputCallbackRegisterService;
import java.util.ArrayList;
import java.util.HashMap;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


public class TopicSubscriptionEndpoint {

    protected WebsocketLocalOutputCallbackRegisterService websocketLocalOutputCallbackRegisterService;

    private static final Log log = LogFactory.getLog(TopicSubscriptionEndpoint.class);

    public TopicSubscriptionEndpoint() {
        websocketLocalOutputCallbackRegisterService = (WebsocketLocalOutputCallbackRegisterService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(WebsocketLocalOutputCallbackRegisterService.class);
    }

    public void onClose (Session session, CloseReason reason, String topic, String adaptorName, int tenantId) {
        if (log.isDebugEnabled()) {
            log.debug("Closing a WebSocket due to "+reason.getReasonPhrase()+", for session ID:"+session.getId()+", for request URI - "+session.getRequestURI());
        }
        websocketLocalOutputCallbackRegisterService.unsubscribe(tenantId, adaptorName, topic, session);
    }

    public void onError (Session session, Throwable throwable,String topic, String adaptorName, int tenantId) {
        log.error("Error occurred in session ID: "+session.getId()+", for request URI - "+session.getRequestURI()+", "+throwable.getMessage(),throwable);
        websocketLocalOutputCallbackRegisterService.unsubscribe(tenantId, adaptorName, topic, session);
    }

}
