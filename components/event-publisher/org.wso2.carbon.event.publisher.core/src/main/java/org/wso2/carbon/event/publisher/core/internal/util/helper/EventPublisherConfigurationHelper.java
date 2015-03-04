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
package org.wso2.carbon.event.publisher.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterSchema;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.Property;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherValidationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;

import javax.xml.namespace.QName;
import java.util.*;

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
        Iterator fromPropertyIter = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_FROM_PROPERTY));
        OMElement fromPropertyOMElement = null;
        count = 0;
        while (fromPropertyIter.hasNext()) {
            fromPropertyOMElement = (OMElement) fromPropertyIter.next();
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
        Iterator mappingPropertyIter = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_PROPERTY));
        OMElement mappingPropertyOMElement = null;
        count = 0;
        while (mappingPropertyIter.hasNext()) {
            mappingPropertyOMElement = (OMElement) mappingPropertyIter.next();
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
        Iterator toPropertyIter = eventPublisherOMElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_TO_PROPERTY));
        OMElement toPropertyOMElement = null;
        count = 0;
        while (toPropertyIter.hasNext()) {
            toPropertyOMElement = (OMElement) toPropertyIter.next();
            count++;
        }
        if (count != 1) {
            throw new EventPublisherConfigurationException("There can be only one 'To' element in Event Publisher configuration file.");
        }
        String toEventAdapterType = toPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TA_TYPE));

        if (toEventAdapterType == null) {
            throw new EventPublisherConfigurationException("There should be a event adapter type in Publisher configuration file.");
        }

        if (!validateToPropertyConfiguration(toPropertyOMElement, toEventAdapterType)) {
            throw new EventPublisherConfigurationException("To property does not contains all the required values for event adapter type " + toEventAdapterType);
        }
    }


    private static boolean validateToPropertyConfiguration(OMElement toElement,
                                                           String eventAdapterType)
            throws EventPublisherConfigurationException {

        List<String> requiredProperties = new ArrayList<String>();
        List<String> propertiesInConfig = new ArrayList<String>();

        Iterator toElementPropertyIterator = toElement.getChildrenWithName(
                new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_PROPERTY)
        );

        OutputEventAdapterService eventAdapterService = EventPublisherServiceValueHolder.getOutputEventAdapterService();
        OutputEventAdapterSchema adapterSchema = eventAdapterService.getOutputEventAdapterSchema(eventAdapterType);

        if (adapterSchema == null) {
            throw new EventPublisherValidationException("Event Adapter with type: " + eventAdapterType + " does not exist", eventAdapterType);
        }

        List<Property> propertyList = adapterSchema.getStaticPropertyList();
        if (propertyList != null && adapterSchema.getDynamicPropertyList() != null) {
            propertyList.addAll(adapterSchema.getDynamicPropertyList());
        } else {
            propertyList = adapterSchema.getDynamicPropertyList();
        }

        if (propertyList != null) {

            for (Property property : propertyList) {
                if (property.isRequired()) {
                    requiredProperties.add(property.getPropertyName());
                }
            }

            while (toElementPropertyIterator.hasNext()) {
                OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
                String propertyName = toElementProperty.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
                propertiesInConfig.add(propertyName);
            }

            if (!propertiesInConfig.containsAll(requiredProperties)) {
                return false;
            }
        }

        return true;
    }


    public static OutputEventAdapterConfiguration getOutputEventAdapterConfiguration(
            String eventAdapterType, String publisherName, String messageFormat) {
        OutputEventAdapterSchema schema = EventPublisherServiceValueHolder.getOutputEventAdapterService().getOutputEventAdapterSchema(eventAdapterType);
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(publisherName);
        outputEventAdapterConfiguration.setMessageFormat(messageFormat);
        outputEventAdapterConfiguration.setType(eventAdapterType);
        Map<String, String> staticProperties = new HashMap<String, String>();
        if (schema != null && schema.getStaticPropertyList() != null) {
            for (Property property : schema.getStaticPropertyList()) {
                staticProperties.put(property.getPropertyName(), property.getDefaultValue());
            }
        }
        outputEventAdapterConfiguration.setStaticProperties(staticProperties);
        return outputEventAdapterConfiguration;
    }

    public static Map<String, String> getDynamicProperties(String eventAdapterType) {
        Map<String, String> dynamicProperties = new HashMap<String, String>();
        OutputEventAdapterSchema schema = EventPublisherServiceValueHolder.getOutputEventAdapterService().getOutputEventAdapterSchema(eventAdapterType);
        if (schema != null && schema.getDynamicPropertyList() != null) {
            for (Property property : schema.getDynamicPropertyList()) {
                dynamicProperties.put(property.getPropertyName(), property.getDefaultValue());
            }
        }
        return dynamicProperties;
    }

    public static String getOutputMappingType(OMElement eventPublisherOMElement) {
        OMElement mappingPropertyOMElement = eventPublisherOMElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELE_MAPPING_PROPERTY));
        return mappingPropertyOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TYPE));
    }

    public static String getEventPublisherName(OMElement eventPublisherOMElement) {
        return eventPublisherOMElement.getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_NAME));
    }


}
