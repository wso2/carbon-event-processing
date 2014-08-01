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

package org.wso2.carbon.event.input.adaptor.websocket.local;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.websocket.local.internal.util.WebsocketLocalEventAdaptorConstants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public final class WebsocketLocalEventAdaptorType extends AbstractInputEventAdaptor {

    private static WebsocketLocalEventAdaptorType websocketLocalEventAdaptor = new WebsocketLocalEventAdaptorType();
    private ResourceBundle resourceBundle;
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>>>();

    private static Log log = LogFactory.getLog(WebsocketLocalEventAdaptorType.class);

    /**
     * @return Websocket event adaptor instance
     */
    public static WebsocketLocalEventAdaptorType getInstance() {
        return websocketLocalEventAdaptor;
    }

    @Override
    protected String getName() {
        return WebsocketLocalEventAdaptorConstants.ADAPTOR_TYPE_WEBSOCKET_LOCAL;
    }

    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.websocket.local.i18n.Resources", Locale.getDefault());
    }

    @Override
    protected List<Property> getInputAdaptorProperties() {
        return null;
    }

    @Override
    protected List<Property> getInputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set topic
        Property topicProperty = new Property(WebsocketLocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(WebsocketLocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(WebsocketLocalEventAdaptorConstants.ADAPTER_TOPIC_HINT));
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
        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(WebsocketLocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);

        ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);

        if (tenantSpecificListenerMap == null) {
            tenantSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>>();
            if (null != inputEventAdaptorListenerMap.putIfAbsent(tenantId, tenantSpecificListenerMap)){
                tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
            }
        }

        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());

        if (adaptorSpecificListeners == null) {
            adaptorSpecificListeners = new ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>();
            if (null != tenantSpecificListenerMap.putIfAbsent(inputEventAdaptorConfiguration.getName(), adaptorSpecificListeners)){
                adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        CopyOnWriteArrayList<WebsocketAdaptorListener> topicSpecificListeners = adaptorSpecificListeners.get(topic);

        if (topicSpecificListeners == null){
            topicSpecificListeners = new CopyOnWriteArrayList<WebsocketAdaptorListener>();
            if (null != adaptorSpecificListeners.putIfAbsent(topic,topicSpecificListeners)){
                topicSpecificListeners = adaptorSpecificListeners.get(topic);
            }
        }
        topicSpecificListeners.add(new WebsocketAdaptorListener(subscriptionId, inputEventAdaptorListener, tenantId));

        return subscriptionId;
    }

    @Override
    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration, InputEventAdaptorConfiguration inputEventAdaptorConfiguration, AxisConfiguration axisConfiguration, String subscriptionId) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(WebsocketLocalEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap != null) {
            Map<String, CopyOnWriteArrayList<WebsocketAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());
            if (adaptorSpecificListeners != null) {
                CopyOnWriteArrayList<WebsocketAdaptorListener> topicSpecificListeners = adaptorSpecificListeners.get(topic);
                if (topicSpecificListeners != null) {
                    for (Iterator<WebsocketAdaptorListener> iterator = topicSpecificListeners.iterator(); iterator.hasNext(); ) {
                        WebsocketAdaptorListener websocketAdaptorListener = iterator.next();
                        if (subscriptionId.equals(websocketAdaptorListener.getSubscriptionId())) {
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    public static CopyOnWriteArrayList<WebsocketAdaptorListener> getTopicSpecificListeners(int tenantId, String adaptorName, String topic) {
        Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap == null){
            if (log.isDebugEnabled()){
                log.debug("Dropping message from tenant id:"+tenantId+", for adaptor name:"+adaptorName+", for topic:"+topic+". Reason: No websocket-local input adaptors created for this tenant.");
            }
            return null;
        }
        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsocketAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(adaptorName);
        if (adaptorSpecificListeners == null) {
            if (log.isDebugEnabled()){
                log.debug("Dropping message from tenant id:"+tenantId+", for adaptor name:"+adaptorName+", for topic:"+topic+". Reason: No adaptor configured for this tenant.");
            }
            return null;
        }
        final CopyOnWriteArrayList<WebsocketAdaptorListener> topicSpecificListeners = adaptorSpecificListeners.get(topic);
        if (topicSpecificListeners == null) {
            if (log.isDebugEnabled()){
                log.debug("Dropping message from tenant id:"+tenantId+", for adaptor name:"+adaptorName+", for topic:"+topic+". Reason: No listeners registered for topic:");
            }
            return null;
        }
        return topicSpecificListeners;
    }



protected class WebsocketAdaptorListener {
    String subscriptionId;
    InputEventAdaptorListener inputeventadaptorlistener;
    int tenantId;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public InputEventAdaptorListener getInputeventadaptorlistener() {
        return inputeventadaptorlistener;
    }

    WebsocketAdaptorListener(String subscriptionId,
                             InputEventAdaptorListener inputeventadaptorlistener, int tenantId) {
        this.subscriptionId = subscriptionId;
        this.inputeventadaptorlistener = inputeventadaptorlistener;
        this.tenantId = tenantId;
    }
}
}
