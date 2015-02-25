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
package org.wso2.carbon.event.receiver.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.core.MessageType;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMapperFactory;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.helper.ConfigurationValidator;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;
import java.util.Map;


public class EventReceiverConfigBuilder {

    private static final Log log = LogFactory.getLog(EventReceiverConfigBuilder.class);

    public static OMElement eventReceiverConfigurationToOM(
            EventReceiverConfiguration eventReceiverConfiguration) {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement eventReceiverConfigElement = factory.createOMElement(new QName(EventReceiverConstants.EB_ELEMENT_ROOT_ELEMENT));
        eventReceiverConfigElement.declareDefaultNamespace(EventReceiverConstants.EB_CONF_NS);

        eventReceiverConfigElement.addAttribute(EventReceiverConstants.EB_ATTR_NAME, eventReceiverConfiguration.getEventReceiverName(), null);
        if (eventReceiverConfiguration.isTraceEnabled()) {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.EB_ATTR_TRACE_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.EB_ATTR_TRACE_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }
        if (eventReceiverConfiguration.isStatisticsEnabled()) {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.EB_ATTR_STATISTICS_ENABLED, EventReceiverConstants.ENABLE_CONST, null);
        } else {
            eventReceiverConfigElement.addAttribute(EventReceiverConstants.EB_ATTR_STATISTICS_ENABLED, EventReceiverConstants.DISABLE_CONST, null);
        }

        //From properties - Stream Name and version
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = eventReceiverConfiguration.getInputEventAdaptorConfiguration();
        OMElement fromOMElement = factory.createOMElement(EventReceiverConstants.EB_ELEMENT_FROM, eventReceiverConfigElement.getDefaultNamespace());
        fromOMElement.addAttribute(EventReceiverConstants.EB_ATTR_TA_TYPE, inputEventAdaptorConfiguration.getInputEventAdaptorType(), null);

        Map<String, String> wso2EventInputPropertyMap = inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().getProperties();
        for (Map.Entry<String, String> propertyEntry : wso2EventInputPropertyMap.entrySet()) {
            OMElement propertyElement = factory.createOMElement(EventReceiverConstants.EB_ELEMENT_PROPERTY, fromOMElement.getDefaultNamespace());
            propertyElement.addAttribute(EventReceiverConstants.EB_ATTR_NAME, propertyEntry.getKey(), null);
            propertyElement.setText(propertyEntry.getValue());
            fromOMElement.addChild(propertyElement);
        }

        eventReceiverConfigElement.addChild(fromOMElement);

        String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
        InputMapperFactory mapperFactory = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType);
        OMElement mappingOMElement = mapperFactory.constructOMFromInputMapping(eventReceiverConfiguration.getInputMapping(), factory);
        mappingOMElement.setNamespace(eventReceiverConfigElement.getDefaultNamespace());
        eventReceiverConfigElement.addChild(mappingOMElement);

        OMElement toOMElement = factory.createOMElement(EventReceiverConstants.EB_ELEMENT_TO, eventReceiverConfigElement.getDefaultNamespace());
        toOMElement.addAttribute(EventReceiverConstants.EB_ATTR_STREAM_NAME, eventReceiverConfiguration.getToStreamName(), null);
        toOMElement.addAttribute(EventReceiverConstants.EB_ATTR_VERSION, eventReceiverConfiguration.getToStreamVersion(), null);

        eventReceiverConfigElement.addChild(toOMElement);
        try {
            String formattedXml = XmlFormatter.format(eventReceiverConfigElement.toString());
            eventReceiverConfigElement = AXIOMUtil.stringToOM(formattedXml);
        } catch (XMLStreamException e) {
            log.warn("Could not format OMElement properly." + eventReceiverConfigElement.toString());
        }

        return eventReceiverConfigElement;
    }

    public static EventReceiverConfiguration getEventReceiverConfiguration(
            OMElement eventReceiverConfigOMElement, String mappingType, int tenantId)
            throws EventReceiverConfigurationException {

        if (!eventReceiverConfigOMElement.getLocalName().equals(EventReceiverConstants.EB_ELEMENT_ROOT_ELEMENT)) {
            throw new EventReceiverConfigurationException("Root element is not an event builder.");
        }

        String eventReceiverName = eventReceiverConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_NAME));
        boolean traceEnabled = false;
        boolean statisticsEnabled = false;
        String traceEnabledAttribute = eventReceiverConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_TRACE_ENABLED));
        if (traceEnabledAttribute != null && traceEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            traceEnabled = true;
        }
        String statisticsEnabledAttribute = eventReceiverConfigOMElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_STATISTICS_ENABLED));
        if (statisticsEnabledAttribute != null && statisticsEnabledAttribute.equalsIgnoreCase(EventReceiverConstants.ENABLE_CONST)) {
            statisticsEnabled = true;
        }

        OMElement fromElement = eventReceiverConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_FROM));
        OMElement mappingElement = eventReceiverConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_MAPPING));
        OMElement toElement = eventReceiverConfigOMElement.getFirstChildWithName(new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_TO));

        String fromEventAdaptorType = fromElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_TA_TYPE));

        ConfigurationValidator.validateInputEventAdaptor(fromEventAdaptorType);

        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = EventReceiverConfigHelper.getInputEventAdaptorConfiguration(fromEventAdaptorType);
        inputEventAdaptorConfiguration.setInputEventAdaptorName(eventReceiverName);
        inputEventAdaptorConfiguration.setInputEventAdaptorType(fromEventAdaptorType);

        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_PROPERTY));
        while (fromElementPropertyIterator.hasNext()) {
            OMElement fromElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = fromElementProperty.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_NAME));
            String propertyValue = fromElementProperty.getText();
            if (inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().getProperties().containsKey(propertyName)) {
                inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().addEventAdaptorProperty(propertyName, propertyValue);
            }
        }

        String toStreamName = toElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_STREAM_NAME));
        String toStreamVersion = toElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_VERSION));

        ConfigurationValidator.validateToStream(toStreamName, toStreamVersion, tenantId);

        EventReceiverConfiguration eventReceiverConfiguration;

        if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_WSO2EVENT_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.WSO2EVENT)) {
                throw new EventReceiverConfigurationException("Wso2 Event Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventReceiverConfiguration = new EventReceiverConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_TEXT_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.TEXT)) {
                throw new EventReceiverConfigurationException("Text Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventReceiverConfiguration = new EventReceiverConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_MAP_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.MAP)) {
                throw new EventReceiverConfigurationException("Mapping for Map input is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventReceiverConfiguration = new EventReceiverConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_XML_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.XML)) {
                throw new EventReceiverConfigurationException("XML Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventReceiverConfiguration = new EventReceiverConfiguration();
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_JSON_MAPPING_TYPE)) {
            if (!ConfigurationValidator.validateSupportedMapping(fromEventAdaptorType, MessageType.JSON)) {
                throw new EventReceiverConfigurationException("JSON Mapping is not supported by event adaptor type " + fromEventAdaptorType);
            }
            eventReceiverConfiguration = new EventReceiverConfiguration();
        } else {
            String factoryClassName = getMappingTypeFactoryClass(mappingElement);
            if (factoryClassName == null) {
                throw new EventReceiverConfigurationException("Corresponding mappingType " + mappingType + " is not valid");
            }

            Class factoryClass;
            try {
                factoryClass = Class.forName(factoryClassName);
                InputMapperFactory inputMapperFactory = (InputMapperFactory) factoryClass.newInstance();
                EventReceiverServiceValueHolder.getMappingFactoryMap().putIfAbsent(mappingType, inputMapperFactory);
                eventReceiverConfiguration = new EventReceiverConfiguration();
            } catch (ClassNotFoundException e) {
                throw new EventReceiverConfigurationException("Class not found exception occurred ", e);
            } catch (InstantiationException e) {
                throw new EventReceiverConfigurationException("Instantiation exception occurred ", e);
            } catch (IllegalAccessException e) {
                throw new EventReceiverConfigurationException("Illegal exception occurred ", e);
            }
        }

        eventReceiverConfiguration.setEventReceiverName(eventReceiverName);
        eventReceiverConfiguration.setTraceEnabled(traceEnabled);
        eventReceiverConfiguration.setStatisticsEnabled(statisticsEnabled);
        eventReceiverConfiguration.setToStreamName(toStreamName);
        eventReceiverConfiguration.setToStreamVersion(toStreamVersion);
        InputMapperFactory mapperFactory = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType);
        eventReceiverConfiguration.setInputMapping(mapperFactory.constructInputMappingFromOM(mappingElement));
        eventReceiverConfiguration.setInputEventAdaptorConfiguration(inputEventAdaptorConfiguration);
        return eventReceiverConfiguration;
    }

    public static String getMappingTypeFactoryClass(OMElement omElement) {
        return omElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_FACTORY_CLASS));
    }

    public static String getAttributeType(AttributeType attributeType) {
        Map<String, AttributeType> attributeMap = EventReceiverConstants.STRING_ATTRIBUTE_TYPE_MAP;
        for (Map.Entry<String, AttributeType> entry : attributeMap.entrySet()) {
            if (entry.getValue().equals(attributeType)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
