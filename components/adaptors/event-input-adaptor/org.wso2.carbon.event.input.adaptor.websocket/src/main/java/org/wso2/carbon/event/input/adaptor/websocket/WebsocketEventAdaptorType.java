/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.event.input.adaptor.websocket;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.tyrus.client.ClientManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.websocket.internal.WebsocketClient;
import org.wso2.carbon.event.input.adaptor.websocket.internal.util.WebsocketEventAdaptorConstants;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.*;

public final class WebsocketEventAdaptorType extends AbstractInputEventAdaptor {

    private static WebsocketEventAdaptorType websocketEventAdaptor = new WebsocketEventAdaptorType();
    private ResourceBundle resourceBundle;

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>>>> inputEventAdaptorClientManagerMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>>>>();

    private static Log log = LogFactory.getLog(WebsocketEventAdaptorType.class);

    /**
     * @return Websocket event adaptor instance
     */
    public static WebsocketEventAdaptorType getInstance() {
        return websocketEventAdaptor;
    }

    @Override
    protected String getName() {
        return WebsocketEventAdaptorConstants.ADAPTOR_TYPE_WEBSOCKET;
    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);

        return supportInputMessageTypes;    }

    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.websocket.i18n.Resources", Locale.getDefault());
    }

    @Override
    protected List<Property> getInputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();
        Property urlProperty = new Property(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);
        urlProperty.setDisplayName(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL));
        urlProperty.setHint(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL_HINT));
        urlProperty.setRequired(true);
        propertyList.add(urlProperty);
        return propertyList;
    }

    @Override
    protected List<Property> getInputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        Property topicProperty = new Property(WebsocketEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC));
        topicProperty.setRequired(false);
        topicProperty.setHint(resourceBundle.getString(WebsocketEventAdaptorConstants.ADAPTER_TOPIC_HINT));
        propertyList.add(topicProperty);
        return propertyList;
    }

    @Override
    public String subscribe(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(WebsocketEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        String socketServerUrl = inputEventAdaptorConfiguration.getInputProperties().get(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);

        if (!socketServerUrl.startsWith("ws://")){
            throw new InputEventAdaptorEventProcessingException("Provided websocket URL "+socketServerUrl+" is invalid.");   //TODO: Make this exception propagate to the UI.
        }

        if (topic != null){
            socketServerUrl = socketServerUrl+"/"+topic;
        } else {
            topic = "";  //Using empty string to map the cases with no topic, because topic is optional
        }
        ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>>> tenantSpecificAdaptorMap = inputEventAdaptorClientManagerMap.get(tenantId);
        if (tenantSpecificAdaptorMap == null) {
            tenantSpecificAdaptorMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>>>();
            if (null != inputEventAdaptorClientManagerMap.putIfAbsent(tenantId, tenantSpecificAdaptorMap)){
                tenantSpecificAdaptorMap = inputEventAdaptorClientManagerMap.get(tenantId);
            }
        }

        ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>> adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(inputEventAdaptorConfiguration.getName());

        if (adaptorSpecificTopicMap == null) {
            adaptorSpecificTopicMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>>();
            if (null != tenantSpecificAdaptorMap.put(inputEventAdaptorConfiguration.getName(), adaptorSpecificTopicMap)) {
                adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        CopyOnWriteArrayList<ClientManagerWrapper> topicSpecificClientManagers = adaptorSpecificTopicMap.get(topic);
        if (topicSpecificClientManagers == null) {
            topicSpecificClientManagers = new CopyOnWriteArrayList<ClientManagerWrapper>();
            if (null != adaptorSpecificTopicMap.putIfAbsent(topic, topicSpecificClientManagers)){
                topicSpecificClientManagers = adaptorSpecificTopicMap.get(topic);
            }
        }

        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();
        ClientManagerWrapper clientManagerWrapper = new ClientManagerWrapper();
        clientManagerWrapper.setClientManager(client);
        clientManagerWrapper.setSubscriptionId(subscriptionId);
        try {
            client.connectToServer(new WebsocketClient(inputEventAdaptorListener), clientEndpointConfig, new URI(socketServerUrl));    //TODO: Handle reconnecting, in case server disconnects. Suggestion: Create a scheduler.
            topicSpecificClientManagers.add(clientManagerWrapper);
        } catch (DeploymentException e) {
            throw new InputEventAdaptorEventProcessingException(e);      //TODO: These exceptions might get modified into new types, as these do not propagate to the UI.
        } catch (IOException e) {
            throw new InputEventAdaptorEventProcessingException(e);
        } catch (Throwable e) {
            throw new InputEventAdaptorEventProcessingException(e);
        }
        return subscriptionId;
    }

    @Override
    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration, InputEventAdaptorConfiguration inputEventAdaptorConfiguration, AxisConfiguration axisConfiguration, String subscriptionId) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(WebsocketEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        String socketServerUrl = inputEventAdaptorConfiguration.getInputProperties().get(WebsocketEventAdaptorConstants.ADAPTER_SERVER_URL);

            Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>>> tenantSpecificAdaptorMap = inputEventAdaptorClientManagerMap.get(tenantId);
            if (tenantSpecificAdaptorMap != null) {
                ConcurrentHashMap<String, CopyOnWriteArrayList<ClientManagerWrapper>> adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(inputEventAdaptorConfiguration.getName());

                if (adaptorSpecificTopicMap != null) {
                    CopyOnWriteArrayList<ClientManagerWrapper> topicSpecificClientManagers = adaptorSpecificTopicMap.get(topic);
                    if (topicSpecificClientManagers != null) {
                        for (Iterator<ClientManagerWrapper> iterator = topicSpecificClientManagers.iterator(); iterator.hasNext(); ) {
                            ClientManagerWrapper clientManagerWrapper = iterator.next();
                            if (subscriptionId.equals(clientManagerWrapper.getSubscriptionId())) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
            }
    }



private class ClientManagerWrapper{
    String subscriptionId;
    ClientManager clientManager;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }
}
}