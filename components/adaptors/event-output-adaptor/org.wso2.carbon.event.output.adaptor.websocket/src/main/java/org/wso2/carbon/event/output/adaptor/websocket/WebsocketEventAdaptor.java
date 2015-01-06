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
package org.wso2.carbon.event.output.adaptor.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.tyrus.client.ClientManager;
import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.OutputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.websocket.internal.WebsocketClient;
import org.wso2.carbon.event.output.adaptor.websocket.internal.util.WebsocketEventAdaptorConstants;

import javax.websocket.*;

public class WebsocketEventAdaptor extends AbstractOutputEventAdaptor{

    private static final Log log = LogFactory.getLog(WebsocketEventAdaptor.class);

    private List<Property> outputAdapterProps;
    private List<Property> outputMessageProps;
    private List<String> supportOutputMessageTypes;
    private ConcurrentHashMap<Integer,ConcurrentHashMap<String,Session>> outputEventAdaptorSessionMap = new ConcurrentHashMap<Integer,ConcurrentHashMap<String,Session>>();        //<tenantId, <url,session> >

	private static WebsocketEventAdaptor instance = new WebsocketEventAdaptor();

    public static WebsocketEventAdaptor getInstance() {
        return instance;
    }

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
        if (!socketServerUrl.startsWith("ws://")){
            throw new OutputEventAdaptorEventProcessingException("Provided websocket URL - "+socketServerUrl+" is invalid.");
        }
        if (topic != null){
            socketServerUrl = socketServerUrl+"/"+topic;
        }
        ConcurrentHashMap<String,Session> urlSessionMap = outputEventAdaptorSessionMap.get(tenantId);
        if (urlSessionMap == null){
            urlSessionMap = new ConcurrentHashMap<String,Session>();
            if (null != outputEventAdaptorSessionMap.putIfAbsent(tenantId,urlSessionMap)){
                urlSessionMap = outputEventAdaptorSessionMap.get(tenantId);
            }
        }
        Session session = urlSessionMap.get(socketServerUrl);
        if (session == null){                                                                  //TODO: Handle reconnecting, in case server disconnects. Suggestion: Create a scheduler.
            ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
            ClientManager client = ClientManager.createClient();
            try {
                session = client.connectToServer(new WebsocketClient(), cec, new URI(socketServerUrl));
                if (null != urlSessionMap.putIfAbsent(socketServerUrl, session)){
                    session.close();
                    session = urlSessionMap.get(socketServerUrl);
                }
            } catch (DeploymentException e) {
                throw new OutputEventAdaptorEventProcessingException(e);
            } catch (IOException e) {
                throw new OutputEventAdaptorEventProcessingException(e);
            } catch (URISyntaxException e) {
                throw new OutputEventAdaptorEventProcessingException(e);
            }
        }
        synchronized (session){
            session.getAsyncRemote().sendText(message.toString());      //this method call was synchronized to fix CEP-996
        }
    }

	@Override
	public void testConnection(
			OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
			int tenantId) {
        Map<String, String> adaptorProps = outputEventAdaptorConfiguration.getOutputProperties();
        String url = adaptorProps.get(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();
        Session session = null;
        try {
            session = client.connectToServer(new WebsocketClient(), cec, new URI(url));
        } catch (URISyntaxException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        } catch (DeploymentException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        } catch (IOException e) {
            throw new OutputEventAdaptorEventProcessingException(e);
        } finally {
            try {
                session.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
	}

    @Override
    public void removeConnectionInfo(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        /**
         * Clearing all the sessions created.
         */
        for (ConcurrentHashMap<String,Session> urlSessionMap : outputEventAdaptorSessionMap.values()){
            for (Session session : urlSessionMap.values()){
                try {
                    session.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
	
    private void populateAdapterMessageProps() {
        this.outputAdapterProps = new ArrayList<Property>();
        this.outputMessageProps = new ArrayList<Property>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.event.output.adaptor.websocket.i18n.Resources", Locale.getDefault());
        Property socketUrlProp = new Property(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);
        socketUrlProp.setDisplayName(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL));
        socketUrlProp.setHint(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL_HINT));
        socketUrlProp.setRequired(true);
        Property topicProp = new Property(WebsocketEventAdaptorConstants.ADAPTER_TOPIC);
        topicProp.setDisplayName(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_TOPIC));
        topicProp.setHint(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_TOPIC_HINT));
        topicProp.setRequired(false);
        this.outputAdapterProps.add(socketUrlProp);
        this.outputMessageProps.add(topicProp);
    }

}
