package org.wso2.carbon.event.output.adaptor.websocket.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.*;

/**
 * This is the client that is used to publish events to a user-configured websocket-server end point.
 */
public class WebsocketClient extends Endpoint {

    private static final Log log = LogFactory.getLog(WebsocketClient.class);

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        if (log.isDebugEnabled()){
            log.debug("Websocket Output Adaptor: WebsocketClient connected, with session ID: " + session.getId()+", to the remote end point URI - "+session.getRequestURI());
        }
    }
}
