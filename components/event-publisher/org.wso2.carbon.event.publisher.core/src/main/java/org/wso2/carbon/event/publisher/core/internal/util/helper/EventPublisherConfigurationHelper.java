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
package org.wso2.carbon.event.publisher.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorService;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.config.InternalOutputEventAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventPublisherConfigurationHelper {

    public static void validateEventPublisherConfiguration(OMElement eventPublisherOMElement) throws
            EventPublisherConfigurationException,
            EventPublisherValidationException {

        if (eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME)) == null || eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME)).trim().isEmpty()) {
            throw new EventPublisherConfigurationException("Need to have an eventPublisher name");
        }

        Iterator childElements = eventPublisherOMElement.getChildElements();
        int count = 0;

        while (childElements.hasNext()) {
            count++;
            childElements.next();
        }

        if (count != 3) {
            throw new EventPublisherConfigurationException("Not a valid configuration, Event Publisher Configuration can only contains 3 child tags (From,Mapping & To)");
        }

        //From property of the event publisher configuration file
        Iterator fromPropertyIterator = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_FROM_PROPERTY));
        OMElement fromPropertyOMElement = null;
        count = 0;
        while (fromPropertyIterator.hasNext()) {
            fromPropertyOMElement = (OMElement) fromPropertyIterator.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'From' element in Event Publisher configuration file.");
        }
        String fromStreamName = fromPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_VERSION));

        if (fromStreamName == null || fromStreamVersion == null) {
            throw new EventPublisherConfigurationException("There should be stream name and version in the 'From' element");
        }

        //Mapping property of the event publisher configuration file
        Iterator mappingPropertyIterator = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_PROPERTY));
        OMElement mappingPropertyOMElement = null;
        count = 0;
        while (mappingPropertyIterator.hasNext()) {
            mappingPropertyOMElement = (OMElement) mappingPropertyIterator.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'Mapping' element in Event Publisher configuration file.");
        }

        String mappingType = mappingPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TYPE));

        if (mappingType == null) {
            throw new EventPublisherConfigurationException("There should be proper mapping type in Event Publisher configuration file.");

        }

        //To property of the event publisher configuration file
        Iterator endpointPropertyIterator = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_ENDPOINT_PROPERTY));
        OMElement endpointPropertyOMElement = null;
        count = 0;
        while (endpointPropertyIterator.hasNext()) {
            endpointPropertyOMElement = (OMElement) endpointPropertyIterator.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'To' element in Event Publisher configuration file.");
        }
        String endpointAdaptorType = endpointPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TA_TYPE));

        if (endpointAdaptorType == null) {
            throw new EventPublisherConfigurationException("There should be a endpoint adaptor type in Publisher configuration file.");
        }

        if (!validateEndpointPropertyConfiguration(endpointPropertyOMElement, endpointAdaptorType)) {
            throw new EventPublisherConfigurationException("Endpoint adaptor property does not contains all the required values for event adaptor type " + endpointAdaptorType);
        }
    }


    private static boolean validateEndpointPropertyConfiguration(OMElement endpointElement,
                                                                 String eventAdaptorType)
            throws EventPublisherConfigurationException {

        List<String> requiredProperties = new ArrayList<String>();
        List<String> endpointAdaptorProperties = new ArrayList<String>();

        Iterator endpointElementPropertyIterator = endpointElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY)
        );

        OutputEventAdaptorService eventAdaptorService = EventPublisherServiceValueHolder.getOutputEventAdaptorService();
        OutputEventAdaptorDto outputEventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorType);

        if (outputEventAdaptorDto == null) {
            throw new EventPublisherValidationException("Event Adaptor with type: " + eventAdaptorType + " does not exist", eventAdaptorType);
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
                String propertyName = endpointElementProperty.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
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
        OutputEventAdaptorDto outputEventAdaptorDto = EventPublisherServiceValueHolder.getOutputEventAdaptorService().getEventAdaptorDto(eventAdaptorTypeName);
        InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = null;
        if (outputEventAdaptorDto != null && outputEventAdaptorDto.getAdaptorPropertyList() != null) {
            internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();
            for (Property property : outputEventAdaptorDto.getAdaptorPropertyList()) {
                internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(property.getPropertyName(), property.getDefaultValue());
            }
        }

        return internalOutputEventAdaptorConfiguration;
    }

    public static String getOutputMappingType(OMElement eventPublisherOMElement) {
        OMElement mappingPropertyOMElement = eventPublisherOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_PROPERTY));
        return mappingPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TYPE));
    }

    public static String getEventPublisherName(OMElement eventPublisherOMElement) {
        return eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
    }


}
