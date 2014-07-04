package org.wso2.carbon.event.output.adaptor.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.wso2.carbon.event.adaptor.utils.websocket.server.WebsocketService;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.websocket.internal.ds.WebsocketEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.websocket.internal.misc.ClientWebsocket;
import org.wso2.carbon.event.output.adaptor.websocket.internal.util.WebsocketEventAdaptorConstants;

public class WebsocketEventAdaptor extends AbstractOutputEventAdaptor{
	
    private List<Property> outputAdapterProps;

    private List<Property> outputMessageProps;
    
    private List<String> supportOutputMessageTypes;

    private Map<String,Session> urlSessionMap = new HashMap<String, Session>();
	
	private static WebsocketEventAdaptor instance = new WebsocketEventAdaptor();

    public static WebsocketEventAdaptor getInstance() {
        return instance;
    }

    private static Log log = LogFactory.getLog(WebsocketEventAdaptor.class);
	
	@Override
	protected String getName() {
		return WebsocketEventAdaptorConstants.ADAPTER_TYPE_WEBSOCKET;
	}

	@Override
	protected List<String> getSupportedOutputMessageTypes() {
		return supportOutputMessageTypes;
	}

	@Override
	protected void init() {
		populateAdapterMessageProps();
        this.supportOutputMessageTypes = new ArrayList<String>();
        this.supportOutputMessageTypes.add(MessageType.XML);
        this.supportOutputMessageTypes.add(MessageType.JSON);
        this.supportOutputMessageTypes.add(MessageType.TEXT);
	}

	@Override
	protected List<Property> getOutputAdaptorProperties() {
		return outputAdapterProps;
	}

	@Override
	protected List<Property> getOutputMessageProperties() {
		return outputMessageProps;
	}

	@Override
	protected void publish(
			OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
			Object message,
			OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
			int tenantId) {
        String topic = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(WebsocketEventAdaptorConstants.ADAPTER_TOPIC);
        String socketServerUrl = outputEventAdaptorConfiguration.getOutputProperties().get(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);
        if (socketServerUrl == null){
            WebsocketService websocketService = WebsocketEventAdaptorServiceValueHolder.getWebsocketService();
            ArrayList<RemoteEndpoint> subscribers = websocketService.getSubscribers(topic);
            for (RemoteEndpoint subscriber : subscribers){
                try {
                    subscriber.sendString(message.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            socketServerUrl = socketServerUrl+"/"+topic;
            Session session = urlSessionMap.get(socketServerUrl);
            if (session == null){
                WebSocketClient webSocketClient = new WebSocketClient();
                try {
                    webSocketClient.start();
                    URI uri = null;
                    uri = new URI(socketServerUrl);
                    ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
                    ClientWebsocket clientWebsocket = new ClientWebsocket();
                    Future<Session> future = webSocketClient.connect(clientWebsocket, uri, upgradeRequest);
                    session = future.get();
                    urlSessionMap.put(socketServerUrl, session);
                } catch (IOException e) {
                    log.error("connecting to socket server failed",e);
                } catch (URISyntaxException e) {
                    log.error("URI Syntax is wrong", e);
                } catch (Exception e) {
                    log.error("Socket-Client start failed", e);
                }
            }
            try {
                session.getRemote().sendString(message.toString());
            } catch (IOException e) {
                log.error("Error when sending a string to the remote websocket server",e);
            }
        }


    }

	@Override
	public void testConnection(
			OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
			int tenantId) {
        Map<String, String> adaptorProps = outputEventAdaptorConfiguration.getOutputProperties();

        if (adaptorProps != null) {
            if (adaptorProps.get(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL) != null ) {
                String url = adaptorProps.get(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);
                WebSocketSession session = null;
                try {
                    WebSocketClient webSocketClient = new WebSocketClient();
                    webSocketClient.start();
                    URI uri = new URI(url);
                    ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
                    Future<Session> sessionFuture = webSocketClient.connect(new ClientWebsocket(), uri, upgradeRequest);
                    session = (WebSocketSession) sessionFuture.get();    //waiting until the above connect() operation is complete. Otherwise proper exceptions might not be thrown.
                } catch (IOException e) {
                    throw new OutputEventAdaptorEventProcessingException(e);
                } catch (URISyntaxException e) {
                    throw new OutputEventAdaptorEventProcessingException(e);
                } catch (IllegalArgumentException e) {
                    throw new OutputEventAdaptorEventProcessingException(e);
                } catch (Exception e) {
                    throw new OutputEventAdaptorEventProcessingException(e);
                }
                finally {
                    session.close();
                }
            } else {
                throw new TestConnectionUnavailableException("not-available");
            }
        }
	}

    @Override
    public void removeConnectionInfo(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        //no required
    }
	
    private void populateAdapterMessageProps() {
        this.outputAdapterProps = new ArrayList<Property>();
        this.outputMessageProps = new ArrayList<Property>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.event.output.adaptor.websocket.i18n.Resources", Locale.getDefault());

        Property socketUrlProp = new Property(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);
        socketUrlProp.setDisplayName(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL));
        socketUrlProp.setHint(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL_HINT));
        socketUrlProp.setRequired(false);

        Property topicProp = new Property(WebsocketEventAdaptorConstants.ADAPTER_TOPIC);
        topicProp.setDisplayName(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_TOPIC));
        topicProp.setHint(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_TOPIC_HINT));
        topicProp.setRequired(false);

        this.outputAdapterProps.add(socketUrlProp);

        this.outputMessageProps.add(topicProp);
    }

}
