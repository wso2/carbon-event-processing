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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorDto;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.type.json.JsonInputMappingConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.map.MapInputMappingConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.text.TextInputMappingConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.wso2event.Wso2EventInputMappingConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.type.xml.XMLInputMappingConfigBuilder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigurationValidator {
    private static Log log = LogFactory.getLog(ConfigurationValidator.class);

    public static boolean validateInputEventAdaptor(
            String inputEventAdaptorType) throws EventReceiverConfigurationException {

        InputEventAdaptorService inputEventAdaptorService = EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService();

        List<InputEventAdaptorDto> eventAdaptorInfoList = inputEventAdaptorService.getEventAdaptors();

        if (eventAdaptorInfoList == null || eventAdaptorInfoList.size() == 0) {
            throw new EventReceiverValidationException("Input event adaptor :" + inputEventAdaptorType + " does not exist.", inputEventAdaptorType);
        }

        Iterator<InputEventAdaptorDto> eventAdaIteratorInfoIterator = eventAdaptorInfoList.iterator();
        for (; eventAdaIteratorInfoIterator.hasNext(); ) {
            InputEventAdaptorDto eventAdaptorInfo = eventAdaIteratorInfoIterator.next();
            if (eventAdaptorInfo.getEventAdaptorTypeName().equals(inputEventAdaptorType)) {
                return true;
            }
        }

        throw new EventReceiverValidationException("Input event adaptor :" + inputEventAdaptorType + " does not exist.", inputEventAdaptorType);
    }

    public static void validateEventReceiverConfiguration(OMElement ebConfigOmElement)
            throws EventReceiverConfigurationException {
        if (!ebConfigOmElement.getLocalName().equals(EventReceiverConstants.EB_ELEMENT_ROOT_ELEMENT)) {
            throw new EventReceiverConfigurationException("Invalid event builder configuration.");
        }

        String eventReceiverName = ebConfigOmElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_NAME));
        OMElement fromElement = ebConfigOmElement.getFirstChildWithName(new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_FROM));
        OMElement mappingElement = ebConfigOmElement.getFirstChildWithName(new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_MAPPING));
        OMElement toElement = ebConfigOmElement.getFirstChildWithName(new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_TO));

        if (eventReceiverName == null || eventReceiverName.isEmpty() || fromElement == null || mappingElement == null || toElement == null) {
            throw new EventReceiverConfigurationException("Invalid event builder configuration for event builder: " + eventReceiverName);
        }

        String fromInputEventAdaptorName = fromElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_TA_NAME));
        String fromInputEventAdaptorType = fromElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_TA_TYPE));

        if (fromInputEventAdaptorName == null || fromInputEventAdaptorName.isEmpty() ||
                fromInputEventAdaptorType == null || fromInputEventAdaptorType.isEmpty()) {
            throw new EventReceiverConfigurationException("Invalid event builder configuration for event builder: " + eventReceiverName);
        }

        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = EventReceiverConfigHelper.getInputEventAdaptorConfiguration(fromInputEventAdaptorType);

        Iterator fromElementPropertyIterator = fromElement.getChildrenWithName(
                new QName(EventReceiverConstants.EB_CONF_NS, EventReceiverConstants.EB_ELEMENT_PROPERTY));
        Map<String, String> fromPropertyMap = new HashMap<String, String>();
        while (fromElementPropertyIterator.hasNext()) {
            OMElement fromElementProperty = (OMElement) fromElementPropertyIterator.next();
            String propertyName = fromElementProperty.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_NAME));
            String propertyValue = fromElementProperty.getText();
            fromPropertyMap.put(propertyName, propertyValue);
        }
        for (String propertyKey : inputEventAdaptorConfiguration.getInternalInputEventAdaptorConfiguration().getProperties().keySet()) {
            if (fromPropertyMap.get(propertyKey) == null) {
                throw new EventReceiverConfigurationException("Invalid event builder configuration for event builder: " + eventReceiverName);
            }
        }

        String mappingType = mappingElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_TYPE));
        if (mappingType != null && !mappingType.isEmpty()) {
            validateMappingProperties(mappingElement, mappingType);
        } else {
            throw new EventReceiverConfigurationException("Mapping type not specified for : " + eventReceiverName);
        }

        String toStreamName = toElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_STREAM_NAME));
        String toStreamVersion = toElement.getAttributeValue(new QName(EventReceiverConstants.EB_ATTR_VERSION));

        if (toStreamName == null || toStreamName.isEmpty() || toStreamVersion == null || toStreamVersion.isEmpty()) {
            throw new EventReceiverConfigurationException("Invalid event builder configuration for event builder: " + eventReceiverName);
        }
    }

    public static void validateToStream(String toStreamName, String toStreamVersion, int tenantId) throws EventReceiverConfigurationException {


        EventStreamService eventStreamService = EventReceiverServiceValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(toStreamName, toStreamVersion, tenantId);
            if (streamDefinition != null) {
                return;
            }
        } catch (EventStreamConfigurationException e) {
            throw new EventReceiverConfigurationException("Error while validating stream definition with store : " + e.getMessage(), e);
        }

        throw new EventReceiverStreamValidationException("Stream " + toStreamName + ":" + toStreamVersion + " does not exist",
                toStreamName + ":" + toStreamVersion);
    }

    public static boolean validateSupportedMapping(String inputEventAdaptorType,
                                                   String messageType) {

        InputEventAdaptorService inputEventAdaptorService = EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService();
        InputEventAdaptorDto inputEventAdaptorDto = inputEventAdaptorService.getEventAdaptorDto(inputEventAdaptorType);

        if (inputEventAdaptorDto == null) {
            return false;
        }

        List<String> supportedInputMessageTypes = inputEventAdaptorDto.getSupportedMessageTypes();
        return supportedInputMessageTypes.contains(messageType);
    }

    @SuppressWarnings("unchecked")
    public static void validateMappingProperties(OMElement mappingElement, String mappingType)
            throws EventReceiverConfigurationException {
        if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_WSO2EVENT_MAPPING_TYPE)) {
            Wso2EventInputMappingConfigBuilder.validateWso2EventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_TEXT_MAPPING_TYPE)) {
            TextInputMappingConfigBuilder.validateTextMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_MAP_MAPPING_TYPE)) {
            MapInputMappingConfigBuilder.validateMapEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_XML_MAPPING_TYPE)) {
            XMLInputMappingConfigBuilder.validateXMLEventMapping(mappingElement);
        } else if (mappingType.equalsIgnoreCase(EventReceiverConstants.EB_JSON_MAPPING_TYPE)) {
            JsonInputMappingConfigBuilder.validateJsonEventMapping(mappingElement);
        } else {
            log.info("No validations available for input mapping type :" + mappingType);
        }
    }
}
