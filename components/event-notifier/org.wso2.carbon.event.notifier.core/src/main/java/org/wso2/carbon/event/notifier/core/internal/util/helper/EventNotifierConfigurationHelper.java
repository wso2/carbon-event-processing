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
package org.wso2.carbon.event.notifier.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.Property;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
import org.wso2.carbon.event.notifier.core.config.InternalOutputEventAdaptorConfiguration;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierValidationException;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventNotifierConfigurationHelper {

    public static void validateEventNotifierConfiguration(OMElement eventNotifierOMElement) throws
            EventNotifierConfigurationException,
            EventNotifierValidationException {

        if (eventNotifierOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_NAME)) == null || eventNotifierOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_NAME)).trim().isEmpty()) {
            throw new EventNotifierConfigurationException("Need to have an eventNotifier name");
        }

        Iterator childElements = eventNotifierOMElement.getChildElements();
        int count = 0;

        while (childElements.hasNext()) {
            count++;
            childElements.next();
        }

        if (count != 3) {
            throw new EventNotifierConfigurationException("Not a valid configuration, Event Notifier Configuration can only contains 3 child tags (From,Mapping & To)");
        }

        //From property of the event notifier configuration file
        Iterator fromPropertyIterator = eventNotifierOMElement.getChildrenWithName(
                new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_FROM_PROPERTY));
        OMElement fromPropertyOMElement = null;
        count = 0;
        while (fromPropertyIterator.hasNext()) {
            fromPropertyOMElement = (OMElement) fromPropertyIterator.next();
            count++;
        }
        if (count != 1) {
            throw new EventNotifierConfigurationException("There can be only one 'From' element in Event Notifier configuration file.");
        }
        String fromStreamName = fromPropertyOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromPropertyOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_VERSION));

        if (fromStreamName == null || fromStreamVersion == null) {
            throw new EventNotifierConfigurationException("There should be stream name and version in the 'From' element");
        }

        //Mapping property of the event notifier configuration file
        Iterator mappingPropertyIterator = eventNotifierOMElement.getChildrenWithName(
                new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_MAPPING_PROPERTY));
        OMElement mappingPropertyOMElement = null;
        count = 0;
        while (mappingPropertyIterator.hasNext()) {
            mappingPropertyOMElement = (OMElement) mappingPropertyIterator.next();
            count++;
        }
        if (count != 1) {
            throw new EventNotifierConfigurationException("There can be only one 'Mapping' element in Event Notifier configuration file.");
        }

        String mappingType = mappingPropertyOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_TYPE));

        if (mappingType == null) {
            throw new EventNotifierConfigurationException("There should be proper mapping type in Event Notifier configuration file.");

        }

        //To property of the event notifier configuration file
        Iterator endpointPropertyIterator = eventNotifierOMElement.getChildrenWithName(
                new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_ENDPOINT_PROPERTY));
        OMElement endpointPropertyOMElement = null;
        count = 0;
        while (endpointPropertyIterator.hasNext()) {
            endpointPropertyOMElement = (OMElement) endpointPropertyIterator.next();
            count++;
        }
        if (count != 1) {
            throw new EventNotifierConfigurationException("There can be only one 'To' element in Event Notifier configuration file.");
        }
        String endpointAdaptorType = endpointPropertyOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_TA_TYPE));

        if (endpointAdaptorType == null) {
            throw new EventNotifierConfigurationException("There should be a endpoint adaptor type in Notifier configuration file.");
        }

        if (!validateEndpointPropertyConfiguration(endpointPropertyOMElement, endpointAdaptorType)) {
            throw new EventNotifierConfigurationException("Endpoint adaptor property does not contains all the required values for event adaptor type " + endpointAdaptorType);
        }
    }


    private static boolean validateEndpointPropertyConfiguration(OMElement endpointElement,
                                                                 String eventAdaptorType)
            throws EventNotifierConfigurationException {

        List<String> requiredProperties = new ArrayList<String>();
        List<String> endpointAdaptorProperties = new ArrayList<String>();

        Iterator endpointElementPropertyIterator = endpointElement.getChildrenWithName(
                new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_PROPERTY)
        );

        OutputEventAdaptorService eventAdaptorService = EventNotifierServiceValueHolder.getOutputEventAdaptorService();
        OutputEventAdaptorDto outputEventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorType);

        if (outputEventAdaptorDto == null) {
            throw new EventNotifierValidationException("Event Adaptor with type: " + eventAdaptorType + " does not exist", eventAdaptorType);
        }

        List<Property> adaptorPropertyList = outputEventAdaptorDto.getAdaptorPropertyList();
        if (adaptorPropertyList != null) {

            for (Property property : adaptorPropertyList) {
                if (property.isRequired()) {
                    requiredProperties.add(property.getPropertyName());
                }
            }

            while (endpointElementPropertyIterator.hasNext()) {
                OMElement endpointElementProperty = (OMElement) endpointElementPropertyIterator.next();
                String propertyName = endpointElementProperty.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_NAME));
                endpointAdaptorProperties.add(propertyName);
            }

            if (!endpointAdaptorProperties.containsAll(requiredProperties)) {
                return false;
            }
        }

        return true;
    }


    public static InternalOutputEventAdaptorConfiguration getOutputEventAdaptorConfiguration(
            String eventAdaptorTypeName) {
        OutputEventAdaptorDto outputEventAdaptorDto = EventNotifierServiceValueHolder.getOutputEventAdaptorService().getEventAdaptorDto(eventAdaptorTypeName);
        InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = null;
        if (outputEventAdaptorDto != null && outputEventAdaptorDto.getAdaptorPropertyList() != null) {
            internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();
            for (Property property : outputEventAdaptorDto.getAdaptorPropertyList()) {
                internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return internalOutputEventAdaptorConfiguration;
    }

    public static String getOutputMappingType(OMElement eventNotifierOMElement) {
        OMElement mappingPropertyOMElement = eventNotifierOMElement.getFirstChildWithName(new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_MAPPING_PROPERTY));
        return mappingPropertyOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_TYPE));
    }

    public static String getEventNotifierName(OMElement eventNotifierOMElement) {
        return eventNotifierOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_NAME));
    }


}
