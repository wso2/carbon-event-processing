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
package org.wso2.carbon.event.input.adaptor.wso2event;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.wso2event.internal.ds.WSO2EventAdaptorServiceValueHolder;
import org.wso2.carbon.event.input.adaptor.wso2event.internal.util.WSO2EventAdaptorConstants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class WSO2EventEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(WSO2EventEventAdaptorType.class);
    private static WSO2EventEventAdaptorType wso2EventAdaptor = new WSO2EventEventAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition>> inputStreamDefinitionMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition>>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, EventAdaptorConf>>> streamIdEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, EventAdaptorConf>>>();

    private WSO2EventEventAdaptorType() {

        WSO2EventAdaptorServiceValueHolder.getDataBridgeSubscriberService().subscribe(new AgentTransportCallback());

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.WSO2EVENT);

        return supportInputMessageTypes;
    }

    /**
     * @return WSO2EventReceiver event adaptor instance
     */
    public static WSO2EventEventAdaptorType getInstance() {

        return wso2EventAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver event adaptor
     */
    @Override
    protected String getName() {
        return WSO2EventAdaptorConstants.ADAPTOR_TYPE_WSO2EVENT;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.wso2event.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return input adaptor configuration property list
     */
    @Override
    public List<Property> getInputAdaptorProperties() {
        return null;
    }

    /**
     * @return input message configuration property list
     */
    @Override
    public List<Property> getInputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set stream definition
        Property streamDefinitionProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME);
        streamDefinitionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME));
        streamDefinitionProperty.setRequired(true);


        // set stream version
        Property streamVersionProperty = new Property(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION);
        streamVersionProperty.setDisplayName(
                resourceBundle.getString(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION));
        streamVersionProperty.setRequired(true);

        propertyList.add(streamDefinitionProperty);
        propertyList.add(streamVersionProperty);

        return propertyList;

    }

    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        EventAdaptorConf eventAdaptorConf = new EventAdaptorConf(inputEventAdaptorListener, tenantId, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());

        ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>> tenantSpecificAdaptorListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificAdaptorListenerMap == null) {
            tenantSpecificAdaptorListenerMap = new ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>>();
            inputEventAdaptorListenerMap.put(tenantId, tenantSpecificAdaptorListenerMap);
        }

        if (!tenantSpecificAdaptorListenerMap.keySet().contains(inputEventAdaptorMessageConfiguration)) {
            ConcurrentHashMap<String, EventAdaptorConf> map = new ConcurrentHashMap<String, WSO2EventEventAdaptorType.EventAdaptorConf>();
            map.put(subscriptionId, eventAdaptorConf);
            tenantSpecificAdaptorListenerMap.put(inputEventAdaptorMessageConfiguration, map);
        } else {
            tenantSpecificAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration).put(subscriptionId, eventAdaptorConf);
            ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition> tenantSpecificInputStreamDefinitionMap = inputStreamDefinitionMap.get(tenantId);
            if (tenantSpecificInputStreamDefinitionMap != null) {
                StreamDefinition streamDefinition = tenantSpecificInputStreamDefinitionMap.get(inputEventAdaptorMessageConfiguration);
                if (streamDefinition != null) {
                    inputEventAdaptorListener.addEventDefinitionCall(streamDefinition);
                }
            }

        }

        return subscriptionId;
    }

    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {

        ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>> tenantSpecificAdaptorListenerMap = inputEventAdaptorListenerMap.get(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
        if (tenantSpecificAdaptorListenerMap != null) {
            Map<String, EventAdaptorConf> map = tenantSpecificAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration);
            if (map != null) {
                map.remove(subscriptionId);
            }
        }

    }


    private class AgentTransportCallback implements AgentCallback {

        @Override
        public void removeStream(StreamDefinition streamDefinition, int tenantId) {
            ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition> tenantSpecificSteamDefinitionMap = inputStreamDefinitionMap.get(tenantId);
            if (tenantSpecificSteamDefinitionMap != null) {
                tenantSpecificSteamDefinitionMap.remove(createTopic(streamDefinition));
            }

            ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>> tenantSpecificAdaptorListenerMap = WSO2EventEventAdaptorType.this.inputEventAdaptorListenerMap.get(tenantId);
            if (tenantSpecificAdaptorListenerMap != null) {
                ConcurrentHashMap<String, EventAdaptorConf> inputEventAdaptorListenerMap = tenantSpecificAdaptorListenerMap.get(createTopic(streamDefinition));
                if (inputEventAdaptorListenerMap != null) {
                    for (EventAdaptorConf eventAdaptorConf : inputEventAdaptorListenerMap.values()) {
                        try {
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(eventAdaptorConf.tenantId);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(eventAdaptorConf.tenantDomain);
                            eventAdaptorConf.inputEventAdaptorListener.removeEventDefinitionCall(streamDefinition);
                        } catch (InputEventAdaptorEventProcessingException e) {
                            log.error("Cannot remove Stream Definition from a eventAdaptorListener subscribed to " +
                                      streamDefinition.getStreamId(), e);
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }
            }

            ConcurrentHashMap<String, ConcurrentHashMap<String, EventAdaptorConf>> tenantSpecificAdaptorMap = streamIdEventAdaptorListenerMap.get(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
            if (tenantSpecificAdaptorMap != null) {
                tenantSpecificAdaptorMap.remove(streamDefinition.getStreamId());
            }
        }

        @Override
        public void definedStream(StreamDefinition streamDefinition, int tenantId) {
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = createTopic(streamDefinition);

            ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition> tenantSpecificStreamDefinitionMap = inputStreamDefinitionMap.get(tenantId);
            if (tenantSpecificStreamDefinitionMap == null) {
                tenantSpecificStreamDefinitionMap = new ConcurrentHashMap<InputEventAdaptorMessageConfiguration, StreamDefinition>();
                inputStreamDefinitionMap.put(tenantId, tenantSpecificStreamDefinitionMap);
            }
            tenantSpecificStreamDefinitionMap.put(inputEventAdaptorMessageConfiguration, streamDefinition);

            ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>> tenantSpecificAdaptorListenerMap = inputEventAdaptorListenerMap.get(tenantId);
            if (tenantSpecificAdaptorListenerMap == null) {
                tenantSpecificAdaptorListenerMap = new ConcurrentHashMap<InputEventAdaptorMessageConfiguration, ConcurrentHashMap<String, EventAdaptorConf>>();
                inputEventAdaptorListenerMap.put(tenantId, tenantSpecificAdaptorListenerMap);
            }
            ConcurrentHashMap<String, EventAdaptorConf> eventAdaptorListeners = tenantSpecificAdaptorListenerMap.get(inputEventAdaptorMessageConfiguration);
            if (eventAdaptorListeners == null) {
                eventAdaptorListeners = new ConcurrentHashMap<String, EventAdaptorConf>();
                tenantSpecificAdaptorListenerMap.put(inputEventAdaptorMessageConfiguration, eventAdaptorListeners);

            }

            for (EventAdaptorConf eventAdaptorConf : eventAdaptorListeners.values()) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(eventAdaptorConf.tenantId);
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(eventAdaptorConf.tenantDomain);
                    eventAdaptorConf.inputEventAdaptorListener.addEventDefinitionCall(streamDefinition);
                } catch (InputEventAdaptorEventProcessingException e) {
                    log.error("Cannot send Stream Definition to a eventAdaptorListener subscribed to " +
                              streamDefinition.getStreamId(), e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            ConcurrentHashMap<String, ConcurrentHashMap<String, EventAdaptorConf>> tenantSpecificAdaptorMap = streamIdEventAdaptorListenerMap.get(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
            if (tenantSpecificAdaptorMap == null) {
                tenantSpecificAdaptorMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, EventAdaptorConf>>();
                streamIdEventAdaptorListenerMap.put(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true), tenantSpecificAdaptorMap);
            }
            tenantSpecificAdaptorMap.put(streamDefinition.getStreamId(), inputEventAdaptorListenerMap.get(tenantId).get(inputEventAdaptorMessageConfiguration));
        }

        private InputEventAdaptorMessageConfiguration createTopic(
                StreamDefinition streamDefinition) {

            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration = new InputEventAdaptorMessageConfiguration();
            Map<String, String> inputMessageProperties = new HashMap<String, String>();
            inputMessageProperties.put(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_NAME, streamDefinition.getName());
            inputMessageProperties.put(WSO2EventAdaptorConstants.ADAPTOR_MESSAGE_STREAM_VERSION, streamDefinition.getVersion());
            inputEventAdaptorMessageConfiguration.setInputMessageProperties(inputMessageProperties);

            return inputEventAdaptorMessageConfiguration;
        }

        @Override
        public void receive(List<Event> events, Credentials credentials) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(credentials.getTenantId());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(credentials.getDomainName());
                for (Event event : events) {
                    Map<String, EventAdaptorConf> eventAdaptorListeners = streamIdEventAdaptorListenerMap.get(credentials.getTenantId()).get(event.getStreamId());
                    if (eventAdaptorListeners == null) {
                        try {
                            definedStream(WSO2EventAdaptorServiceValueHolder.getDataBridgeSubscriberService().getStreamDefinition(event.getStreamId(), credentials.getTenantId()), credentials.getTenantId());
                        } catch (StreamDefinitionNotFoundException e) {
                            log.error("No Stream definition store found for the event " +
                                      event.getStreamId(), e);
                            return;
                        } catch (StreamDefinitionStoreException e) {
                            log.error("No Stream definition store found when checking stream definition for " +
                                      event.getStreamId(), e);
                            return;
                        }
                        eventAdaptorListeners = streamIdEventAdaptorListenerMap.get(credentials.getTenantId()).get(event.getStreamId());
                        if (eventAdaptorListeners == null) {
                            log.error("No event adaptor listeners for  " + event.getStreamId());
                            return;
                        }
                    }

                    if(log.isDebugEnabled()){
                        log.debug("Event received in wso2Event Adaptor - "+event);
                    }

                    for (EventAdaptorConf eventAdaptorConf : eventAdaptorListeners.values()) {
                        try {
                            eventAdaptorConf.inputEventAdaptorListener.onEventCall(event);
                        } catch (InputEventAdaptorEventProcessingException e) {
                            log.error("Cannot send event to a eventAdaptorListener subscribed to " +
                                      event.getStreamId(), e);
                        }
                    }

                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

        }

    }

    private class EventAdaptorConf {

        private final InputEventAdaptorListener inputEventAdaptorListener;
        private final int tenantId;
        private final String tenantDomain;

        public EventAdaptorConf(InputEventAdaptorListener inputEventAdaptorListener, int tenantId,
                                String tenantDomain) {
            this.inputEventAdaptorListener = inputEventAdaptorListener;
            this.tenantId = tenantId;
            this.tenantDomain = tenantDomain;
        }
    }

}
