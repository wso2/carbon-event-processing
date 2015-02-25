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
package org.wso2.carbon.event.output.adaptor.sms;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.publisher.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.publisher.core.MessageType;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.EndpointAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.output.adaptor.sms.internal.ds.SMSEventAdaptorServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.sms.internal.util.SMSEventAdaptorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class SMSEventAdaptorType extends AbstractOutputEventAdaptor {

    private static final Log log = LogFactory.getLog(SMSEventAdaptorType.class);

    private static SMSEventAdaptorType SMSEventAdaptor = new SMSEventAdaptorType();
    private ConcurrentHashMap<EndpointAdaptorConfiguration, List<String>> smsSenderConfigurationMap = new ConcurrentHashMap<EndpointAdaptorConfiguration, List<String>>();
    private ResourceBundle resourceBundle;

    private SMSEventAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return Email event adaptor instance
     */
    public static SMSEventAdaptorType getInstance() {
        return SMSEventAdaptor;
    }

    /**
     * @return name of the Email event adaptor
     */
    @Override
    protected String getName() {
        return SMSEventAdaptorConstants.ADAPTOR_TYPE_SMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.output.adaptor.sms.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // set sms address
        Property phoneNo = new Property(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO);
        phoneNo.setDisplayName(
                resourceBundle.getString(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
        phoneNo.setHint(resourceBundle.getString(SMSEventAdaptorConstants.ADAPTOR_CONF_SMS_HINT_NO));
        phoneNo.setRequired(true);

        propertyList.add(phoneNo);
        return propertyList;
    }


    /**
     * @param message
     * @param endpointAdaptorConfiguration
     *
     * @param tenantId
     */
    public void publish(
            Object message,
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {

        List<String> smsList = smsSenderConfigurationMap.get(endpointAdaptorConfiguration.getOutputAdaptorProperties().get(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
        if (smsList == null) {
            smsList = new ArrayList<String>();
            smsList.add(endpointAdaptorConfiguration.getOutputAdaptorProperties().get(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
            smsSenderConfigurationMap.putIfAbsent(endpointAdaptorConfiguration, smsList);
        }

        String[] smsNOs = smsList.toArray(new String[0]);
        if (smsNOs != null) {
            for (String smsNo : smsNOs) {
                OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                        BaseConstants.DEFAULT_TEXT_WRAPPER, null);
                payload.setText(message.toString());

                try {
                    ServiceClient serviceClient;
                    ConfigurationContext configContext = SMSEventAdaptorServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                    if (configContext != null) {
                        serviceClient = new ServiceClient(configContext, null);
                    } else {
                        serviceClient = new ServiceClient();
                    }
                    Options options = new Options();
                    options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                    options.setTo(new EndpointReference("sms://" + smsNo));
                    serviceClient.setOptions(options);
                    serviceClient.fireAndForget(payload);

                } catch (AxisFault axisFault) {
                    smsSenderConfigurationMap.remove(endpointAdaptorConfiguration.getOutputAdaptorProperties().get(SMSEventAdaptorConstants.ADAPTOR_MESSAGE_SMS_NO));
                    String msg = "Error in delivering the message, " +
                            "message: " + message + ", to: " + smsNo + ".";
                    log.error(msg, axisFault);
                } catch (Exception ex) {
                    String msg = "Error in delivering the message, " +
                            "message: " + message + ", to: " + smsNo + ".";
                    log.error(msg, ex);
                }
            }
        }
    }


    @Override
    public void testConnection(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        throw new TestConnectionUnavailableException("not-available");
    }

    @Override
    public void removeConnectionInfo(
            EndpointAdaptorConfiguration endpointAdaptorConfiguration, int tenantId) {
        smsSenderConfigurationMap.remove(endpointAdaptorConfiguration);
    }

}
