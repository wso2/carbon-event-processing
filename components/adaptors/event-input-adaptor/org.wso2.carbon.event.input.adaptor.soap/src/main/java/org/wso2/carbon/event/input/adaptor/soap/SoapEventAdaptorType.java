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
package org.wso2.carbon.event.input.adaptor.soap;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.soap.internal.util.Axis2Util;
import org.wso2.carbon.event.input.adaptor.soap.internal.util.SoapEventAdaptorConstants;
import org.wso2.carbon.event.input.adaptor.soap.internal.util.SubscriptionMessageReceiver;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public final class SoapEventAdaptorType extends AbstractInputEventAdaptor {

    private static final Log log = LogFactory.getLog(SoapEventAdaptorType.class);
    private static SoapEventAdaptorType soapAdaptor = new SoapEventAdaptorType();
    public static ConcurrentHashMap<Integer, Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>>>> inputEventAdaptorListenerMap =
            new ConcurrentHashMap<Integer, Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>>>>();
    private ResourceBundle resourceBundle;


    private SoapEventAdaptorType() {

    }


    @Override
    protected List<String> getSupportedInputMessageTypes() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);

        return supportInputMessageTypes;
    }


    /**
     * @return WS Local event adaptor instance
     */
    public static SoapEventAdaptorType getInstance() {

        return soapAdaptor;
    }

    /**
     * @return name of the WS Local event adaptor
     */
    @Override
    protected String getName() {
        return SoapEventAdaptorConstants.ADAPTOR_TYPE_SOAP;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.soap.i18n.Resources", Locale.getDefault());
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

        // set receiver url
        Property operationProperty = new Property(SoapEventAdaptorConstants.ADAPTOR_MESSAGE_OPERATION_NAME);
        operationProperty.setDisplayName(
                resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_MESSAGE_OPERATION_NAME));
        operationProperty.setRequired(true);
        operationProperty.setHint(resourceBundle.getString(SoapEventAdaptorConstants.ADAPTOR_MESSAGE_HINT_OPERATION_NAME));

        propertyList.add(operationProperty);

        return propertyList;

    }


    @Override
    public String subscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration) {

        String subscriptionId = UUID.randomUUID().toString();

        String operation = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_MESSAGE_OPERATION_NAME);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap == null) {
            tenantSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>>>();
            inputEventAdaptorListenerMap.put(tenantId, tenantSpecificListenerMap);

        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());

        if (adaptorSpecificListeners == null) {
            adaptorSpecificListeners = new ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>>();
            if (null != tenantSpecificListenerMap.put(inputEventAdaptorConfiguration.getName(), adaptorSpecificListeners)) {
                adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());
            }
        }

        AxisService axisService = null;

        ConcurrentHashMap<String, SoapAdaptorListener> operationSpecificListeners = adaptorSpecificListeners.get(operation);
        if (operationSpecificListeners == null || operationSpecificListeners.size() == 0) {
            try {
                axisService = Axis2Util.registerAxis2Service(inputEventAdaptorMessageConfiguration,
                                                             inputEventAdaptorConfiguration, axisConfiguration);
            } catch (AxisFault axisFault) {
                throw new InputEventAdaptorEventProcessingException("Can not create " +
                                                                    "the axis2 service to receive events", axisFault);
            }
            operationSpecificListeners = new ConcurrentHashMap<String, SoapAdaptorListener>();
            if (null != adaptorSpecificListeners.put(operation, operationSpecificListeners)) {
                operationSpecificListeners = adaptorSpecificListeners.get(operation);
            }
        }

        if (axisService == null) {
            String axisServiceName = inputEventAdaptorConfiguration.getName();
            try {
                axisService = axisConfiguration.getService(axisServiceName);
            } catch (AxisFault axisFault) {
                throw new InputEventAdaptorEventProcessingException("There is no service with the name ==> " + axisServiceName, axisFault);
            }

        }

        String operationNameWithoutSlash = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_MESSAGE_OPERATION_NAME).replaceAll("/", "");
        AxisOperation axisOperation = axisService.getOperation(new QName("", operationNameWithoutSlash));
        SubscriptionMessageReceiver messageReceiver =
                (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
        messageReceiver.addEventAdaptorListener(subscriptionId, inputEventAdaptorListener);

        operationSpecificListeners.put(subscriptionId, new SoapAdaptorListener(subscriptionId, inputEventAdaptorListener));
        return subscriptionId;
    }

    @Override
    public void unsubscribe(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,

            AxisConfiguration axisConfiguration, String subscriptionId) {

        String operationName = inputEventAdaptorMessageConfiguration.getInputMessageProperties().get(SoapEventAdaptorConstants.ADAPTOR_MESSAGE_OPERATION_NAME);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>>> tenantSpecificListenerMap = inputEventAdaptorListenerMap.get(tenantId);
        if (tenantSpecificListenerMap != null) {
            ConcurrentHashMap<String, ConcurrentHashMap<String, SoapAdaptorListener>> adaptorSpecificListeners = tenantSpecificListenerMap.get(inputEventAdaptorConfiguration.getName());

            if (adaptorSpecificListeners != null) {
                ConcurrentHashMap<String, SoapAdaptorListener> operationSpecificListeners = adaptorSpecificListeners.get(operationName);
                if (operationSpecificListeners != null) {
                    operationSpecificListeners.remove(subscriptionId);
                    if (operationSpecificListeners.isEmpty()) {
                        tenantSpecificListenerMap.remove(operationName);
                        try {
                            Axis2Util.removeOperation(inputEventAdaptorMessageConfiguration, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
                        } catch (AxisFault axisFault) {
                            throw new InputEventAdaptorEventProcessingException("Can not remove operation ", axisFault);
                        }
                    }
                }
            }
        }

    }
}

class SoapAdaptorListener {
    String subscriptionId;
    InputEventAdaptorListener inputeventadaptorlistener;

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

    SoapAdaptorListener(String subscriptionId,
                        InputEventAdaptorListener inputeventadaptorlistener) {
        this.subscriptionId = subscriptionId;
        this.inputeventadaptorlistener = inputeventadaptorlistener;
    }
}
