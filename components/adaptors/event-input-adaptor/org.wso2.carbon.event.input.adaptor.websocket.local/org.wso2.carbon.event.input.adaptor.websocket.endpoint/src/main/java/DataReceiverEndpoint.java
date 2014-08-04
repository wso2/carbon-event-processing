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
import org.wso2.carbon.event.input.adaptor.websocket.local.WebsocketLocalInputService;


import javax.websocket.*;

public class DataReceiverEndpoint {

    protected WebsocketLocalInputService websocketLocalInputService;
    private static final Log log = LogFactory.getLog(TenantDataReceiverEndpoint.class);

    public DataReceiverEndpoint() {
        websocketLocalInputService = (WebsocketLocalInputService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(WebsocketLocalInputService.class);
    }

    public void onClose (Session session, CloseReason reason) {
        if (log.isDebugEnabled()) {
            log.debug("Closing a WebSocket due to "+reason.getReasonPhrase()+", for session ID:"+session.getId()+", for request URI - "+session.getRequestURI());
        }
    }

    public void onError (Session session, Throwable throwable) {
        log.error("Error occurred in session ID: "+session.getId()+", for request URI - "+session.getRequestURI()+", "+throwable.getMessage(),throwable);
    }
}
