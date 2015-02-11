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
package org.wso2.carbon.event.notifier.core.internal.type.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConfiguration;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierStreamValidationException;
import org.wso2.carbon.event.notifier.core.internal.OutputMapper;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONOutputMapper implements OutputMapper {

    private List<String> mappingTextList;
    private EventNotifierConfiguration eventNotifierConfiguration = null;
    private Map<String, Integer> propertyPositionMap = null;
    private final StreamDefinition streamDefinition;

    public JSONOutputMapper(EventNotifierConfiguration eventNotifierConfiguration,
                            Map<String, Integer> propertyPositionMap, int tenantId,
                            StreamDefinition streamDefinition)
            throws EventNotifierConfigurationException {
        this.eventNotifierConfiguration = eventNotifierConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        this.streamDefinition = streamDefinition;
        if (eventNotifierConfiguration.getOutputMapping().isCustomMappingEnabled()) {
            validateStreamDefinitionWithOutputProperties(tenantId);
        } else {
            generateJsonEventTemplate(streamDefinition);
        }
    }


    private List<String> getOutputMappingPropertyList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0) {
            mappingTextList.add(text.substring(text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) + 2, text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX)));
            text = text.substring(text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) + 2);
        }
        return mappingTextList;
    }

    public void setMappingTextList(String mappingText) {

        List<String> mappingTextList = new ArrayList<String>();
        String text = mappingText;

        mappingTextList.clear();
        while (text.contains(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) && text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) > 0) {
            mappingTextList.add(text.substring(0, text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX)));
            mappingTextList.add(text.substring(text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX) + 2, text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX)));
            text = text.substring(text.indexOf(EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX) + 2);
        }
        mappingTextList.add(text);
        this.mappingTextList = mappingTextList;
    }

    @Override
    public Object convertToMappedInputEvent(Object[] eventData)
            throws EventNotifierConfigurationException {
        StringBuilder eventText = new StringBuilder(mappingTextList.get(0));
        for (int i = 1, size = mappingTextList.size(); i < size; i++) {
            if (i % 2 == 0) {
                eventText.append(mappingTextList.get(i));
            } else {
                eventText.append(getPropertyValue(eventData, mappingTextList.get(i)));
            }
        }

        String jsonEvent = eventText.toString();

        if (jsonEvent.contains("\"null\"")) {
            jsonEvent = jsonEvent.replaceAll("\"null\"", "null");
        }

        try {
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(jsonEvent).toString();
        } catch (JsonSyntaxException e) {
            throw new EventNotifierConfigurationException("Not valid JSON object : " + e.getMessage(), e);
        }
    }

    @Override
    public Object convertToTypedInputEvent(Object[] eventData)
            throws EventNotifierConfigurationException {
        return convertToMappedInputEvent(eventData);
    }

    private void validateStreamDefinitionWithOutputProperties(int tenantId)
            throws EventNotifierConfigurationException {

        JSONOutputMapping jsonOutputMapping = ((JSONOutputMapping) eventNotifierConfiguration.getOutputMapping());
        String actualMappingText = jsonOutputMapping.getMappingText();
        if (jsonOutputMapping.isRegistryResource()) {
            actualMappingText = EventNotifierServiceValueHolder.getCarbonEventNotifierService().getRegistryResourceContent(jsonOutputMapping.getMappingText(), tenantId);
        }

        setMappingTextList(actualMappingText);
        List<String> mappingProperties = getOutputMappingPropertyList(actualMappingText);

        Iterator<String> mappingTextListIterator = mappingProperties.iterator();
        for (; mappingTextListIterator.hasNext(); ) {
            String property = mappingTextListIterator.next();
            if (!propertyPositionMap.containsKey(property)) {
                throw new EventNotifierStreamValidationException("Property " + property + " is not in the input stream definition.", streamDefinition.getStreamId());
            }
        }

    }

    private String getPropertyValue(Object[] eventData, String mappingProperty) {
        if (eventData.length != 0) {
            int position = propertyPositionMap.get(mappingProperty);
            Object data = eventData[position];
            if (data != null) {
                return data.toString();
            }
        }
        return null;
    }

    private void generateJsonEventTemplate(StreamDefinition streamDefinition) {

        JsonObject jsonEventObject = new JsonObject();
        JsonObject innerParentObject = new JsonObject();

        List<Attribute> metaDatAttributes = streamDefinition.getMetaData();
        if (metaDatAttributes != null && metaDatAttributes.size() > 0) {
            innerParentObject.add(EventNotifierConstants.EVENT_META_TAG, createPropertyElement(EventNotifierConstants.PROPERTY_META_PREFIX, metaDatAttributes));
        }

        List<Attribute> correlationAttributes = streamDefinition.getCorrelationData();
        if (correlationAttributes != null && correlationAttributes.size() > 0) {
            innerParentObject.add(EventNotifierConstants.EVENT_CORRELATION_TAG, createPropertyElement(EventNotifierConstants.PROPERTY_CORRELATION_PREFIX, correlationAttributes));
        }

        List<Attribute> payloadAttributes = streamDefinition.getPayloadData();
        if (payloadAttributes != null && payloadAttributes.size() > 0) {
            innerParentObject.add(EventNotifierConstants.EVENT_PAYLOAD_TAG, createPropertyElement("", payloadAttributes));
        }

        jsonEventObject.add(EventNotifierConstants.EVENT_PARENT_TAG, innerParentObject);
        setMappingTextList(jsonEventObject.toString());

    }

    private static JsonObject createPropertyElement(String dataPrefix,
                                                    List<Attribute> attributeList) {

        JsonObject innerObject = new JsonObject();
        for (Attribute attribute : attributeList) {
            innerObject.addProperty(attribute.getName(), EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_PREFIX + dataPrefix + attribute.getName() + EventNotifierConstants.TEMPLATE_EVENT_ATTRIBUTE_POSTFIX);
        }
        return innerObject;
    }


}
