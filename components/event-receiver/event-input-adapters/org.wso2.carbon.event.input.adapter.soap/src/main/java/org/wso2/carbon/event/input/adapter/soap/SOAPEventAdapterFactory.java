/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.soap;


import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.soap.internal.util.SOAPEventAdapterConstants;

import java.util.*;


public class SOAPEventAdapterFactory extends InputEventAdapterFactory {
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.soap.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return SOAPEventAdapterConstants.ADAPTER_TYPE_SOAP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.XML);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        // set receiver url
        Property operationProperty = new Property(SOAPEventAdapterConstants.ADAPTER_MESSAGE_OPERATION_NAME);
        operationProperty.setDisplayName(
                resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_MESSAGE_OPERATION_NAME));
        operationProperty.setRequired(true);
        operationProperty.setHint(resourceBundle.getString(SOAPEventAdapterConstants.ADAPTER_MESSAGE_HINT_OPERATION_NAME));

        propertyList.add(operationProperty);

        return propertyList;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        return new SOAPEventAdapter(eventAdapterConfiguration,globalProperties);
    }
}
