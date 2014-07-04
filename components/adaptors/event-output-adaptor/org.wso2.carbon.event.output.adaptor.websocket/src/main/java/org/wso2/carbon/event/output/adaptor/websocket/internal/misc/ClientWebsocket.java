package org.wso2.carbon.event.output.adaptor.websocket.internal.misc;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

/**
 * An instance of this class is used, when we connect to a user-given socket-server, as a client - to send events.
 */
@WebSocket
public class ClientWebsocket extends WebSocketAdapter {


    @OnWebSocketConnect
    public void onConnect(Session session) { }


    @OnWebSocketClose
    public void onClose(int statusCode, String reason) { }

    @OnWebSocketMessage
    public void onMessage(String msg) { }
}
