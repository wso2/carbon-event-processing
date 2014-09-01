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

package org.wso2.carbon.event.input.adaptor.email;


import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.core.AbstractInputEventAdaptor;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.core.MessageType;
import org.wso2.carbon.event.input.adaptor.core.Property;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.input.adaptor.core.message.config.InputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.input.adaptor.email.internal.Axis2Util;
import org.wso2.carbon.event.input.adaptor.email.internal.LateStartAdaptorListener;
import org.wso2.carbon.event.input.adaptor.email.internal.ds.EmailEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.input.adaptor.email.internal.util.EmailEventAdaptorConstants;

import java.util.*;

public final class EmailEventAdaptorType extends AbstractInputEventAdaptor implements
                                                                           LateStartAdaptorListener {

    private boolean readyToPoll = false;
    private static final Log log = LogFactory.getLog(EmailEventAdaptorType.class);
    private static EmailEventAdaptorType emailEventAdaptor = new EmailEventAdaptorType();
    private ResourceBundle resourceBundle;
    List<LateStartAdaptorConfig> lateStartAdaptorConfigList = new ArrayList<LateStartAdaptorConfig>();

    private EmailEventAdaptorType() {

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
     * @return Email event adaptor instance
     */
    public static EmailEventAdaptorType getInstance() {
        return emailEventAdaptor;
    }

    /**
     * @return name of the Email event adaptor
     */
    @Override
    protected String getName() {
        return EmailEventAdaptorConstants.ADAPTOR_TYPE_EMAIL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {

        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adaptor.email.i18n.Resources", Locale.getDefault());
        EmailEventAdaptorServiceValueHolder.addLateStartAdaptorListener(this);
    }


    @Override
    public List<Property> getInputAdaptorProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set receiving mail address
        Property emailAddress = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS);
        emailAddress.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS));
        emailAddress.setRequired(true);
        emailAddress.setHint(resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_ADDRESS_HINT));
        propertyList.add(emailAddress);

        // set receiving mail protocol
        Property protocol = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL);
        protocol.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL));

        protocol.setOptions(new String[]{"pop3", "imap"});
        protocol.setDefaultValue("imap");
        protocol.setHint(resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_HINT));
        propertyList.add(protocol);

        // set receiving mail poll interval
        Property pollInterval = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL);
        pollInterval.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL));
        pollInterval.setRequired(true);
        pollInterval.setHint(resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_POLL_INTERVAL_HINT));
        propertyList.add(pollInterval);

        // set receiving mail host
        Property host = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_HOST);
        host.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_HOST));
        host.setRequired(true);
        propertyList.add(host);

        // set receiving mail host
        Property port = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_PORT);
        port.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PROTOCOL_PORT));
        port.setRequired(true);
        propertyList.add(port);

        // set receiving mail username
        Property userName = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_USERNAME);
        userName.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_USERNAME));
        userName.setRequired(true);
        propertyList.add(userName);

        // set receiving mail password
        Property password = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_PASSWORD));
        password.setRequired(true);
        password.setSecured(true);
        propertyList.add(password);

        // set receiving mail socket factory class
        Property socketFactoryClass = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS);
        socketFactoryClass.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_CLASS));
        socketFactoryClass.setRequired(true);
        propertyList.add(socketFactoryClass);

        // set receiving mail socket factory fallback
        Property socketFactoryFallback = new Property(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK);
        socketFactoryFallback.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_CONF_RECEIVING_EMAIL_SOCKET_FACTORY_FALLBACK));
        socketFactoryFallback.setRequired(true);
        socketFactoryFallback.setOptions(new String[]{"true", "false"});
        socketFactoryFallback.setDefaultValue("false");
        propertyList.add(socketFactoryFallback);

        return propertyList;

    }

    @Override
    public List<Property> getInputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // set incoming email subject
        Property subject = new Property(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT);
        subject.setDisplayName(
                resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT));
        subject.setRequired(true);
        subject.setHint(resourceBundle.getString(EmailEventAdaptorConstants.ADAPTOR_MESSAGE_RECEIVING_EMAIL_SUBJECT_HINT));

        propertyList.add(subject);
        return propertyList;
    }

    @Override
    public String subscribe(InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
                            InputEventAdaptorListener inputEventAdaptorListener,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration) {

        // When publishing we only need to register the axis2 service
        String subscriptionId = UUID.randomUUID().toString();
        if (! readyToPoll) {
            lateStartAdaptorConfigList.add(new LateStartAdaptorConfig(inputEventMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId));
        } else {
            createEmailAdaptorListener(inputEventMessageConfiguration, inputEventAdaptorListener, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
        }
        return subscriptionId;
    }

    @Override
    public void unsubscribe(InputEventAdaptorMessageConfiguration inputEventMessageConfiguration,
                            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.removeEmailServiceOperation(inputEventMessageConfiguration, inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputEventAdaptorEventProcessingException("Can not remove operation ", axisFault);
        }
    }

    @Override
    public void tryStartAdaptor() {
        log.info("Email input event adaptor loading listeners ");
        readyToPoll = true;
        for (LateStartAdaptorConfig lateStartAdaptorConfig : lateStartAdaptorConfigList) {
            this.createEmailAdaptorListener(lateStartAdaptorConfig.getInputEventAdaptorMessageConfiguration(), lateStartAdaptorConfig.getInputEventAdaptorListener(), lateStartAdaptorConfig.getInputEventAdaptorConfiguration(), lateStartAdaptorConfig.getAxisConfiguration(), lateStartAdaptorConfig.getSubscriptionId());
        }
    }

    private void createEmailAdaptorListener(
            InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
            InputEventAdaptorListener inputEventAdaptorListener,
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) {
        try {
            Axis2Util.registerAxis2EmailService(inputEventAdaptorMessageConfiguration, inputEventAdaptorListener,
                                                inputEventAdaptorConfiguration, axisConfiguration, subscriptionId);
        } catch (AxisFault axisFault) {
            throw new InputEventAdaptorEventProcessingException("Can not create the axis2 service to receive email events", axisFault);
        }


    }


    class LateStartAdaptorConfig {
        InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration;
        InputEventAdaptorListener inputEventAdaptorListener;
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration;
        AxisConfiguration axisConfiguration;
        String subscriptionId;


        public LateStartAdaptorConfig(
                InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration,
                InputEventAdaptorListener inputEventAdaptorListener,
                InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
                AxisConfiguration axisConfiguration, String subscriptionId) {
            this.inputEventAdaptorMessageConfiguration = inputEventAdaptorMessageConfiguration;
            this.inputEventAdaptorListener = inputEventAdaptorListener;
            this.inputEventAdaptorConfiguration = inputEventAdaptorConfiguration;
            this.axisConfiguration = axisConfiguration;
            this.subscriptionId = subscriptionId;
        }

        public InputEventAdaptorMessageConfiguration getInputEventAdaptorMessageConfiguration() {
            return inputEventAdaptorMessageConfiguration;
        }

        public void setInputEventAdaptorMessageConfiguration(
                InputEventAdaptorMessageConfiguration inputEventAdaptorMessageConfiguration) {
            this.inputEventAdaptorMessageConfiguration = inputEventAdaptorMessageConfiguration;
        }

        public InputEventAdaptorListener getInputEventAdaptorListener() {
            return inputEventAdaptorListener;
        }

        public void setInputEventAdaptorListener(
                InputEventAdaptorListener inputEventAdaptorListener) {
            this.inputEventAdaptorListener = inputEventAdaptorListener;
        }

        public InputEventAdaptorConfiguration getInputEventAdaptorConfiguration() {
            return inputEventAdaptorConfiguration;
        }

        public void setInputEventAdaptorConfiguration(
                InputEventAdaptorConfiguration inputEventAdaptorConfiguration) {
            this.inputEventAdaptorConfiguration = inputEventAdaptorConfiguration;
        }

        public AxisConfiguration getAxisConfiguration() {
            return axisConfiguration;
        }

        public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
            this.axisConfiguration = axisConfiguration;
        }

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public void setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }
    }

}
