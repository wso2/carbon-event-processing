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


@ServerEndpoint(value = "/{adaptorname}/{topic}")
public class SuperTenantTopicSubscriptionEndpoint extends TopicSubscriptionEndpoint{

    private static final Log log = LogFactory.getLog(SuperTenantTopicSubscriptionEndpoint.class);
    private int tenantId;

    @OnOpen
    public void onOpen (Session session, @PathParam("topic") String topic, @PathParam("adaptorname") String adaptorName) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket opened, for Session id: "+session.getId()+", for the Adaptor:"+adaptorName+", for the Topic:"+topic);
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(-1234);
        tenantId = carbonContext.getTenantId();
        websocketLocalOutputCallbackRegisterService.subscribe(tenantId, adaptorName, topic, session);
    }

    @OnMessage
    public void onMessage (Session session, String message, @PathParam("topic") String topic, @PathParam("adaptorname") String adaptorName) {
        if (log.isDebugEnabled()) {
            log.debug("Received: " + message+", for Session id: "+session.getId()+", for the Adaptor:"+adaptorName+", for the Topic:"+topic);
        }
    }

    @OnClose
    public void onClose (Session session, CloseReason reason, @PathParam("topic") String topic, @PathParam("adaptorname") String adaptorName) {
        super.onClose(session, reason, topic, adaptorName, tenantId);
    }

    @OnError
    public void onError (Session session, Throwable throwable, @PathParam("topic") String topic, @PathParam("adaptorname") String adaptorName) {
        super.onError(session, throwable, topic, adaptorName, tenantId);
    }

}
