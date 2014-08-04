/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint(value = "/t/{tdomain}/{adaptorname}/{topic}")
public class TenantDataReceiverEndpoint extends DataReceiverEndpoint {

    private int tenantId;

    private static final Log log = LogFactory.getLog(TenantDataReceiverEndpoint.class);

    @OnOpen
    public void onOpen (Session session, @PathParam("topic") String topic, @PathParam("tdomain") String tdomain, @PathParam("adaptorname") String adaptorName) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket opened with session id: "+session.getId()+", for the tenant domain:"+tdomain+", for the adaptor"+adaptorName+", for the topic:"+topic);
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tdomain,true);
        tenantId = carbonContext.getTenantId();
    }

    @OnMessage
    public void onMessage (Session session, String message, @PathParam("topic") String topic, @PathParam("tdomain") String tdomain, @PathParam("adaptorname") String adaptorName){
        if (log.isDebugEnabled()) {
            log.debug("Received message: " + message+", for session id: "+session.getId()+", for tenant domain"+tdomain+", for the adaptor:"+adaptorName+", for the topic:"+topic);
        }
        websocketLocalInputService.invokeListener(tenantId, adaptorName, topic, message);
    }

    @OnClose
    public void onClose (Session session, CloseReason reason) {
        super.onClose(session,reason);
    }

    @OnError
    public void onError (Session session, Throwable throwable) {
        super.onError(session,throwable);
    }
}
