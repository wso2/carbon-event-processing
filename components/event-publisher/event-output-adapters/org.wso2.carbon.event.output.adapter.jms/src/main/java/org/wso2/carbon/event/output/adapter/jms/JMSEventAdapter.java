/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.jms.internal.util.*;

import javax.jms.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import javax.jms.JMSException;
import java.util.*;

public class JMSEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(JMSEventAdapter.class);
    private static ThreadPoolExecutor threadPoolExecutor;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;


    private ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>> publisherMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>>();

    public JMSEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                             Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-available");
    }

    @Override
    public void connect() {
        //not required
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        ConcurrentHashMap<String, PublisherDetails> topicEventSender = publisherMap.get("dynamicConfig");
        if (null == topicEventSender) {
            topicEventSender = new ConcurrentHashMap<String, PublisherDetails>();
            if (null != publisherMap.putIfAbsent("dynamicConfig", topicEventSender)) {
                topicEventSender = publisherMap.get("dynamicConfig");
            }
        }

        String topicName = dynamicProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);
        PublisherDetails publisherDetails = topicEventSender.get(topicName);
        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);
        try {
            if (null == publisherDetails) {
                publisherDetails = initPublisher(dynamicProperties, topicEventSender, topicName, messageConfig);
            }
            Message jmsMessage = publisherDetails.getJmsMessageSender().convertToJMSMessage(message, messageConfig);
            setJMSTransportHeaders(jmsMessage, dynamicProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_HEADER));
            publisherDetails.getJmsMessageSender().send(jmsMessage, messageConfig);
        } catch (RuntimeException e) {
            log.warn("Caught exception: " + e.getMessage() + ". Reinitializing connection and sending...");
            publisherDetails = topicEventSender.remove(topicName);
            if (publisherDetails != null) {
                publisherDetails.getJmsMessageSender().close();
                publisherDetails.getJmsConnectionFactory().stop();
            }
            throw new OutputEventAdapterRuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    private PublisherDetails initPublisher(
            Map<String, String> dynamicProperties,
            ConcurrentHashMap<String, PublisherDetails> topicEventSender, String topicName,
            Map<String, String> messageConfig) {
        PublisherDetails publisherDetails;
        Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
        adaptorProperties.putAll(dynamicProperties);

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, "dynamicConfig");
        JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory, messageConfig);
        publisherDetails = new PublisherDetails(jmsConnectionFactory, jmsMessageSender);
        topicEventSender.put(topicName, publisherDetails);
        return publisherDetails;
    }

    private Message setJMSTransportHeaders(Message message, String headerProperty) {

        Map<String, String> messageConfiguration = new HashMap<String, String>();

        if (headerProperty != null && message != null) {
            String[] headers = headerProperty.split(",");

            if (headers != null && headers.length > 0) {
                for (String header : headers) {
                    String[] headerPropertyWithValue = header.split(":");
                    if(headerPropertyWithValue.length == 2){
                        messageConfiguration.put(headerPropertyWithValue[0], headerPropertyWithValue[1]);
                    }else {
                        log.warn("Header property not defined in the correct format");
                    }
                }
            }

            try {
                return JMSUtils.setTransportHeaders(messageConfiguration, message);
            } catch (JMSException e) {
                throw new OutputEventAdapterRuntimeException(e);
            }
        }

        return message;
    }


    class PublisherDetails {
        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSMessageSender jmsMessageSender;

        public PublisherDetails(JMSConnectionFactory jmsConnectionFactory,
                                JMSMessageSender jmsMessageSender) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsMessageSender = jmsMessageSender;
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSMessageSender getJmsMessageSender() {
            return jmsMessageSender;
        }
    }

}
