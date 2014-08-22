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

package org.wso2.carbon.event.input.adaptor.http;

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
import org.wso2.carbon.event.input.adaptor.http.internal.ds.HTTPEventAdaptorServiceDS;
import org.wso2.carbon.event.input.adaptor.http.internal.util.HTTPEventAdaptorConstants;

import java.util.*;
import java.util.concurrent.*;

public final class HTTPEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(HTTPEventAdaptorType.class);
    private static HTTPEventAdaptorType httpEventAdaptor = new HTTPEventAdaptorType();
    private ResourceBundle resourceBundle;
    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>>>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>>>>();

    public static ExecutorService executorService = new ThreadPoolExecutor(HTTPEventAdaptorConstants.ADAPTER_MIN_THREAD_POOL_SIZE,
                                                                           HTTPEventAdaptorConstants.ADAPTER_MAX_THREAD_POOL_SIZE, HTTPEventAdaptorConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                                                                           new LinkedBlockingQueue<Runnable>(HTTPEventAdaptorConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE));

    private HTTPEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);

        return supportInputMessageTypes;
    }

    /**
     * @return WSO2EventReceiver event adaptor instance
     */
    public static HTTPEventAdaptorType getInstance() {

        return httpEventAdaptor;
    }

    /**
     * @return name of the WSO2EventReceiver event adaptor
     */
    @Override
    protected String getName() {
        return HTTPEventAdaptorConstants.ADAPTOR_TYPE_HTTP;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.http.i18n.Resources", Locale.getDefault());
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

        // set topic
        Property topicProperty = new Property(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        topicProperty.setDisplayName(
                resourceBundle.getString(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);
        return propertyList;

    }

    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {
        String subscriptionId = UUID.randomUUID().toString();

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap == null) {
            tenantSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>>>();
            inputEventAdaptorListenerMap.put(tenantId, tenantSpecificListenerMap);
        }

        ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());

        if (adaptorSpecificListeners == null) {
            adaptorSpecificListeners = new ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>>();
            if (null != tenantSpecificListenerMap.put(inputEventAdaptorConfiguration.getName(), adaptorSpecificListeners)) {
                adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        ArrayList<HTTPAdaptorListener> topicSpecificListeners = adaptorSpecificListeners.get(topic);
        ArrayList<HTTPAdaptorListener> newTopicSpecificListeners;
        if (topicSpecificListeners == null || topicSpecificListeners.size() == 0) {
            HTTPEventAdaptorServiceDS.registerDynamicEndpoint(inputEventAdaptorConfiguration.getName(), topic, tenantId);
            newTopicSpecificListeners = new ArrayList<HTTPAdaptorListener>();
        } else {
            newTopicSpecificListeners = new ArrayList<HTTPAdaptorListener>(topicSpecificListeners);
        }

        newTopicSpecificListeners.add(new HTTPAdaptorListener(subscriptionId, inputEventAdaptorListener, tenantId));
        adaptorSpecificListeners.put(topic, newTopicSpecificListeners);

        return subscriptionId;
    }

    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {

        String topic = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(HTTPEventAdaptorConstants.ADAPTOR_MESSAGE_TOPIC);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ConcurrentHashMap<String, ArrayList<HTTPAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap != null) {
            Map<String, ArrayList<HTTPAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());

            if (adaptorSpecificListeners != null) {
                ArrayList<HTTPAdaptorListener> topicSpecificListeners = adaptorSpecificListeners.get(topic);
                if (topicSpecificListeners != null) {
                    ArrayList<HTTPAdaptorListener> newTopicSpecificListeners = new ArrayList<HTTPAdaptorListener>(topicSpecificListeners);
                    for (Iterator<HTTPAdaptorListener> iterator = newTopicSpecificListeners.iterator(); iterator.hasNext(); ) {
                        HTTPAdaptorListener httpAdaptorListener = iterator.next();
                        if (subscriptionId.equals(httpAdaptorListener.getSubscriptionId())) {
                            iterator.remove();
                            break;
                        }
                    }
                    if (newTopicSpecificListeners.isEmpty()) {
                        tenantSpecificListenerMap.remove(topic);
                        HTTPEventAdaptorServiceDS.unregisterDynamicEndpoint(inputEventAdaptorConfiguration.getName(), topic);
                    }
                    adaptorSpecificListeners.put(topic, newTopicSpecificListeners);
                }
            }
        }

    }

}

class HTTPAdaptorListener {
    String subscriptionId;
    InputEventAdaptorListener inputeventadaptorlistener;
    int tenantId;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public InputEventAdaptorListener getInputeventadaptorlistener() {
        return inputeventadaptorlistener;
    }

    public void setInputeventadaptorlistener(
            InputEventAdaptorListener inputeventadaptorlistener) {
        this.inputeventadaptorlistener = inputeventadaptorlistener;
    }

    HTTPAdaptorListener(String subscriptionId,
                        InputEventAdaptorListener inputeventadaptorlistener, int tenantId) {
        this.subscriptionId = subscriptionId;
        this.inputeventadaptorlistener = inputeventadaptorlistener;
        this.tenantId = tenantId;
    }
}