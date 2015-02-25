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
package org.wso2.carbon.event.receiver.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.receiver.core.EventReceiverDeployer;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorDto;
import org.wso2.carbon.event.receiver.core.Property;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMappingAttribute;
import org.wso2.carbon.event.receiver.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class EventReceiverConfigHelper {

    public static InputEventAdaptorConfiguration getInputEventAdaptorConfiguration(
            String eventAdaptorTypeName) {
        InputEventAdaptorDto inputEventAdaptorDto = EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService().getEventAdaptorDto(eventAdaptorTypeName);
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = null;
        if (inputEventAdaptorDto != null && inputEventAdaptorDto.getAdaptorPropertyList() != null) {
            inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
            InternalInputEventAdaptorConfiguration internalInputEventAdaptorConfiguration = new InternalInputEventAdaptorConfiguration();
            inputEventAdaptorConfiguration.setInternalInputEventAdaptorConfiguration(internalInputEventAdaptorConfiguration);
            for (Property property : inputEventAdaptorDto.getAdaptorPropertyList()) {
                internalInputEventAdaptorConfiguration.addEventAdaptorProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return inputEventAdaptorConfiguration;
    }

    public static String getInputMappingType(OMElement eventReceiverOMElement) {
        OMElement mappingElement = eventReceiverOMElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_MAPPING));
        return mappingElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TYPE));
    }

    public static String getEventReceiverName(OMElement eventReceiverOMElement) {
        return eventReceiverOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
    }

    public static EventReceiverDeployer getEventReceiverDeployer(
            AxisConfiguration axisConfiguration) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
        return (EventReceiverDeployer) deploymentEngine.getDeployer(EventReceiverConstants.ER_CONFIG_DIRECTORY, EventReceiverConstants.ER_CONFIG_FILE_EXTENSION);
    }

    public static Attribute[] getAttributes(List<InputMappingAttribute> inputMappingAttributes) {
        List<Attribute> metaAttributes = new ArrayList<Attribute>();
        List<Attribute> correlationAttributes = new ArrayList<Attribute>();
        List<Attribute> payloadAttributes = new ArrayList<Attribute>();
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
            if (inputMappingAttribute.getToElementKey().startsWith(EventReceiverConstants.META_DATA_PREFIX)) {
                metaAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else if (inputMappingAttribute.getToElementKey().startsWith(EventReceiverConstants.CORRELATION_DATA_PREFIX)) {
                correlationAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            } else {
                payloadAttributes.add(new Attribute(inputMappingAttribute.getToElementKey(), inputMappingAttribute.getToElementType()));
            }
        }
        Attribute[] outputAttributes = new Attribute[metaAttributes.size() + correlationAttributes.size() + payloadAttributes.size()];
        int attributeCount = 0;
        for (Attribute attribute : metaAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        for (Attribute attribute : correlationAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        for (Attribute attribute : payloadAttributes) {
            outputAttributes[attributeCount++] = attribute;
        }
        return outputAttributes;
    }

}
