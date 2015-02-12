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
package org.wso2.carbon.event.notifier.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.core.MessageType;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorDto;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.config.*;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierStreamValidationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierValidationException;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;
import org.wso2.carbon.event.notifier.core.internal.util.helper.EventNotifierConfigurationHelper;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventNotifierConfigurationBuilder {

    private static boolean validateStreamDetails(String streamName, String streamVersion,
                                                 int tenantId)
            throws EventNotifierConfigurationException {

        EventStreamService eventStreamService = EventNotifierServiceValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(streamName, streamVersion, tenantId);
            if (streamDefinition != null) {
                return true;
            }
        } catch (EventStreamConfigurationException e) {
            throw new EventNotifierConfigurationException("Error while validating stream definition with store : " + e.getMessage(), e);
        }
        return false;

    }

    private static boolean validateEventAdaptor(String eventAdaptorType) {


        //TODO , need to change this map to see when adaptor is loaded or not..

        OutputEventAdaptorService outputEventAdaptorService = EventNotifierServiceValueHolder.getOutputEventAdaptorService();
        List<OutputEventAdaptorDto> eventAdaptorInfoList = outputEventAdaptorService.getEventAdaptors();

        if (eventAdaptorInfoList == null || eventAdaptorInfoList.size() == 0) {
            throw new EventNotifierValidationException("Event adaptor with type: " + eventAdaptorType + " does not exist", eventAdaptorType);
        }

        Iterator<OutputEventAdaptorDto> eventAdaIteratorInfoIterator = eventAdaptorInfoList.iterator();
        for (; eventAdaIteratorInfoIterator.hasNext(); ) {
            OutputEventAdaptorDto eventAdaptorInfo = eventAdaIteratorInfoIterator.next();
            if (eventAdaptorInfo.getEventAdaptorTypeName().equals(eventAdaptorType)) {
                return true;
            }
        }

        return false;
    }

    private static boolean validateSupportedMapping(String eventAdaptorType,
                                                    String messageType) {

        OutputEventAdaptorService eventAdaptorService = EventNotifierServiceValueHolder.getOutputEventAdaptorService();
        OutputEventAdaptorDto eventAdaptorDto = eventAdaptorService.getEventAdaptorDto(eventAdaptorType);

        if (eventAdaptorDto == null) {
            throw new EventNotifierValidationException("Event Adaptor with type: " + eventAdaptorType + " does not exist", eventAdaptorType);
        }
        List<String> supportedOutputMessageTypes = eventAdaptorDto.getSupportedMessageTypes();
        return supportedOutputMessageTypes.contains(messageType);

    }


    public static EventNotifierConfiguration getEventNotifierConfiguration(
            OMElement eventNotifierConfigOMElement, int tenantId, String mappingType)
            throws EventNotifierConfigurationException, EventNotifierValidationException {

        EventNotifierConfiguration eventNotifierConfiguration = new EventNotifierConfiguration();

        OMElement fromElement = eventNotifierConfigOMElement.getFirstChildWithName(new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_FROM_PROPERTY));
        OMElement mappingElement = eventNotifierConfigOMElement.getFirstChildWithName(new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_MAPPING_PROPERTY));
        OMElement endpointElement = eventNotifierConfigOMElement.getFirstChildWithName(new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_ENDPOINT_PROPERTY));

        String fromStreamName = fromElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_STREAM_NAME));
        String fromStreamVersion = fromElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_VERSION));

        String endpointAdaptorType = endpointElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_TA_TYPE));

        if (!validateEventAdaptor(endpointAdaptorType)) {
            throw new EventNotifierValidationException("Event Adaptor with type: " + endpointAdaptorType + " does not exist", endpointAdaptorType);
        }

        if (!validateStreamDetails(fromStreamName, fromStreamVersion, tenantId)) {
            throw new EventNotifierStreamValidationException("Stream " + fromStreamName + ":" + fromStreamVersion + " does not exist", fromStreamName + ":" + fromStreamVersion);
        }

        InternalOutputEventAdaptorConfiguration outputEventAdaptorConfiguration = EventNotifierConfigurationHelper.getOutputEventAdaptorConfiguration(endpointAdaptorType);
        EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();

        //TODO put a proper name
        endpointAdaptorConfiguration.setEndpointAdaptorName(eventNotifierConfiguration.getEventNotifierName());
        endpointAdaptorConfiguration.setEndpointType(endpointAdaptorType);

        Iterator toElementPropertyIterator = endpointElement.getChildrenWithName(
                new QName(EventNotifierConstants.EF_CONF_NS, EventNotifierConstants.EF_ELE_PROPERTY)
        );

        while (toElementPropertyIterator.hasNext()) {
            OMElement toElementProperty = (OMElement) toElementPropertyIterator.next();
            String propertyName = toElementProperty.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_NAME));
            String propertyValue = toElementProperty.getText();
            outputEventAdaptorConfiguration.addEventAdaptorProperty(propertyName, propertyValue);
        }

        endpointAdaptorConfiguration.setOutputAdaptorConfiguration(outputEventAdaptorConfiguration);


        if (mappingType.equalsIgnoreCase(EventNotifierConstants.EF_WSO2EVENT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(endpointAdaptorType, MessageType.WSO2EVENT)) {
                throw new EventNotifierConfigurationException("WSO2Event Mapping is not supported by event adaptor type " + endpointAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventNotifierConstants.EF_TEXT_MAPPING_TYPE)) {
            if (!validateSupportedMapping(endpointAdaptorType, MessageType.TEXT)) {
                throw new EventNotifierConfigurationException("Text Mapping is not supported by event adaptor type " + endpointAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventNotifierConstants.EF_MAP_MAPPING_TYPE)) {
            if (!validateSupportedMapping(endpointAdaptorType, MessageType.MAP)) {
                throw new EventNotifierConfigurationException("Map Mapping is not supported by event adaptor type " + endpointAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventNotifierConstants.EF_XML_MAPPING_TYPE)) {
            if (!validateSupportedMapping(endpointAdaptorType, MessageType.XML)) {
                throw new EventNotifierConfigurationException("XML Mapping is not supported by event adaptor type " + endpointAdaptorType);
            }
        } else if (mappingType.equalsIgnoreCase(EventNotifierConstants.EF_JSON_MAPPING_TYPE)) {
            if (!validateSupportedMapping(endpointAdaptorType, MessageType.JSON)) {
                throw new EventNotifierConfigurationException("JSON Mapping is not supported by event adaptor type " + endpointAdaptorType);
            }
        } else {
            String factoryClassName = getMappingTypeFactoryClass(mappingElement);
            if (factoryClassName == null) {
                throw new EventNotifierConfigurationException("Corresponding mappingType " + mappingType + " is not valid");
            }

            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryClassName);
                OutputMapperFactory outputMapperFactory = (OutputMapperFactory) factoryClass.newInstance();
                EventNotifierServiceValueHolder.getMappingFactoryMap().putIfAbsent(mappingType, outputMapperFactory);
            } catch (ClassNotFoundException e) {
                throw new EventNotifierConfigurationException("Class not found exception occurred ", e);
            } catch (InstantiationException e) {
                throw new EventNotifierConfigurationException("Instantiation exception occurred ", e);
            } catch (IllegalAccessException e) {
                throw new EventNotifierConfigurationException("Illegal exception occurred ", e);
            }
        }


        eventNotifierConfiguration.setEventNotifierName(eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_NAME)));

        if (eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_STATISTICS)) != null && eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_STATISTICS)).equals(EventNotifierConstants.TM_VALUE_ENABLE)) {
            eventNotifierConfiguration.setEnableStatistics(true);
        } else if (eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_STATISTICS)) != null && eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_STATISTICS)).equals(EventNotifierConstants.TM_VALUE_DISABLE)) {
            eventNotifierConfiguration.setEnableStatistics(false);
        }

        if (eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_TRACING)) != null && eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_TRACING)).equals(EventNotifierConstants.TM_VALUE_ENABLE)) {
            eventNotifierConfiguration.setEnableTracing(true);
        } else if (eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_TRACING)) != null && eventNotifierConfigOMElement.getAttributeValue(new QName(EventNotifierConstants.TM_ATTR_TRACING)).equals(EventNotifierConstants.TM_VALUE_DISABLE)) {
            eventNotifierConfiguration.setEnableTracing(false);
        }

        eventNotifierConfiguration.setFromStreamName(fromStreamName);
        eventNotifierConfiguration.setFromStreamVersion(fromStreamVersion);
        eventNotifierConfiguration.setOutputMapping(EventNotifierServiceValueHolder.getMappingFactoryMap().get(mappingType).constructOutputMapping(mappingElement));
        eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);
        return eventNotifierConfiguration;

    }

    public static OMElement eventNotifierConfigurationToOM(
            EventNotifierConfiguration eventNotifierConfiguration)
            throws EventNotifierConfigurationException {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventFormatterConfigElement = factory.createOMElement(new QName(
                EventNotifierConstants.EF_ELE_ROOT_ELEMENT));
        eventFormatterConfigElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);

        eventFormatterConfigElement.addAttribute(EventNotifierConstants.EF_ATTR_NAME, eventNotifierConfiguration.getEventNotifierName(), null);

        if (eventNotifierConfiguration.isEnableStatistics()) {
            eventFormatterConfigElement.addAttribute(EventNotifierConstants.TM_ATTR_STATISTICS, EventNotifierConstants.TM_VALUE_ENABLE,
                    null);
        } else if (!eventNotifierConfiguration.isEnableStatistics()) {
            eventFormatterConfigElement.addAttribute(EventNotifierConstants.TM_ATTR_STATISTICS, EventNotifierConstants.TM_VALUE_DISABLE,
                    null);
        }

        if (eventNotifierConfiguration.isEnableTracing()) {
            eventFormatterConfigElement.addAttribute(EventNotifierConstants.TM_ATTR_TRACING, EventNotifierConstants.TM_VALUE_ENABLE,
                    null);
        } else if (!eventNotifierConfiguration.isEnableTracing()) {
            eventFormatterConfigElement.addAttribute(EventNotifierConstants.TM_ATTR_TRACING, EventNotifierConstants.TM_VALUE_DISABLE,
                    null);
        }

        //From properties - Stream Name and version
        OMElement fromPropertyElement = factory.createOMElement(new QName(
                EventNotifierConstants.EF_ELE_FROM_PROPERTY));
        fromPropertyElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);
        fromPropertyElement.addAttribute(EventNotifierConstants.EF_ATTR_STREAM_NAME, eventNotifierConfiguration.getFromStreamName(), null);
        fromPropertyElement.addAttribute(EventNotifierConstants.EF_ATTR_VERSION, eventNotifierConfiguration.getFromStreamVersion(), null);
        eventFormatterConfigElement.addChild(fromPropertyElement);

        OMElement mappingOMElement = EventNotifierServiceValueHolder.getMappingFactoryMap().get(eventNotifierConfiguration.getOutputMapping().getMappingType()).constructOutputMappingOM(eventNotifierConfiguration.getOutputMapping(), factory);

        eventFormatterConfigElement.addChild(mappingOMElement);


        OMElement toOMElement = factory.createOMElement(new QName(
                EventNotifierConstants.EF_ELE_ENDPOINT_PROPERTY));
        toOMElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);

        EndpointAdaptorConfiguration endpointAdaptorConfiguration = eventNotifierConfiguration.getEndpointAdaptorConfiguration();
        toOMElement.addAttribute(EventNotifierConstants.EF_ATTR_TA_TYPE, endpointAdaptorConfiguration.getEndpointType(), null);

        Map<String, String> eventPropertyMap = endpointAdaptorConfiguration.getOutputAdaptorProperties();
        for (Map.Entry<String, String> propertyEntry : eventPropertyMap.entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(
                    EventNotifierConstants.EF_ELE_PROPERTY));
            propertyElement.declareDefaultNamespace(EventNotifierConstants.EF_CONF_NS);
            propertyElement.addAttribute(EventNotifierConstants.EF_ATTR_NAME, propertyEntry.getKey(), null);
            propertyElement.setText(propertyEntry.getValue());
            toOMElement.addChild(propertyElement);
        }

        eventFormatterConfigElement.addChild(toOMElement);
        return eventFormatterConfigElement;
    }

    public static String getMappingTypeFactoryClass(OMElement omElement) {
        return omElement.getAttributeValue(new QName(EventNotifierConstants.EF_ATTR_FACTORY_CLASS));
    }


}
