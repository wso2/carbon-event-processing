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
package org.wso2.carbon.event.builder.core.internal.type.json;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderProcessingException;
import org.wso2.carbon.event.builder.core.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(JsonInputMapper.class);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<JsonPathData> attributeJsonPathDataList = new ArrayList<JsonPathData>();
    private int noMetaData;
    private int noCorrelationData;
    private int noPayloadData;
    private StreamDefinition streamDefinition;

    public JsonInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                           StreamDefinition streamDefinition)
            throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.streamDefinition = streamDefinition;

        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof JsonInputMapping) {
            JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
            if (jsonInputMapping.isCustomMappingEnabled()) {
                createAttributeJsonPathList(streamDefinition,jsonInputMapping.getInputMappingAttributes());
            } else {
                this.noMetaData = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
                this.noCorrelationData += streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
                this.noPayloadData += streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
            }
        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderProcessingException {
        Object outObject = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            if (jsonString.startsWith(EventBuilderConstants.JSON_ARRAY_START_CHAR)) {
                outObject = processMultipleEvents(obj);
            } else {
                outObject = processSingleEvent(obj);
            }
        }
        return outObject;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderProcessingException {

        Object outObject = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            if (jsonString.startsWith(EventBuilderConstants.JSON_ARRAY_START_CHAR)) {
                outObject = processTypedMultipleEvents(obj);
            } else {
                outObject = processTypedSingleEvent(obj);
            }
        }
        return outObject;

    }

    @Override
    public Attribute[] getOutputAttributes() {
        JsonInputMapping jsonInputMapping = (JsonInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = jsonInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

    private Object[][] processMultipleEvents(Object obj) throws EventBuilderProcessingException {
        Object[][] objArray = null;
        if (obj instanceof String) {
            String events = (String) obj;
            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(events);
                objArray = new Object[jsonArray.length()][];
                for (int i = 0; i < jsonArray.length(); i++) {
                    objArray[i] = processSingleEvent(jsonArray.getJSONObject(i).toString());
                }
            } catch (JSONException e) {
                throw new EventBuilderProcessingException("Error in parsing JSON: ", e);
            }
        }
        return objArray;
    }

    private Object[] processSingleEvent(Object obj) throws EventBuilderProcessingException {
        Object[] outObjArray = null;
        if (obj instanceof String) {
            String jsonString = (String) obj;
            List<Object> objList = new ArrayList<Object>();
            for (JsonPathData jsonPathData : attributeJsonPathDataList) {
                JsonPath jsonPath = jsonPathData.getJsonPath();
                AttributeType type = jsonPathData.getType();
                try {
                    Object resultObject = jsonPath.read(jsonString);
                    Object returnedObj = null;
                    if (resultObject != null) {
                        returnedObj = getPropertyValue(resultObject, type);
                    } else if (jsonPathData.getDefaultValue() != null && !jsonPathData.getDefaultValue().isEmpty()) {
                        returnedObj = getPropertyValue(jsonPathData.getDefaultValue(), type);
                        log.warn("Unable to parse JSONPath to retrieve required attribute. Sending defaults.");
                    } else {
                        log.warn("Unable to parse JSONPath to retrieve required attribute. Skipping to next attribute.");
                    }
                    objList.add(returnedObj);
                } catch (ClassCastException e) {
                    log.warn("Unable to cast the input data to required type :" + type);
                } catch (InvalidPathException e) {
                    log.warn("Could not find any matches for the incoming event with JSONPath : " + jsonPath.toString());
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        return outObjArray;
    }

    private Object[][] processTypedMultipleEvents(Object obj)
            throws EventBuilderProcessingException {
        Object[][] objArray = null;
        String events = (String) obj;
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(events);
            objArray = new Object[jsonArray.length()][];
            for (int i = 0; i < jsonArray.length(); i++) {
                objArray[i] = processTypedSingleEvent(jsonArray.getJSONObject(i).toString());
            }
        } catch (JSONException e) {
            throw new EventBuilderProcessingException("Error in parsing JSON: ", e);
        }
        return objArray;
    }

    private Object[] processTypedSingleEvent(Object obj)
            throws EventBuilderProcessingException {

        Object attributeArray[] = new Object[noMetaData + noCorrelationData + noPayloadData];
        int attributeCount = 0;
        if (obj instanceof String) {
            String jsonString = (String) obj;

            if (noMetaData > 0) {
                for (Attribute metaData : streamDefinition.getMetaData()) {
                    JsonPath jsonPath = JsonPath.compile("$." + EventBuilderConstants.EVENT_PARENT_TAG + "." + EventBuilderConstants.EVENT_META_TAG);
                    Map<Object, Object> eventMap = jsonPath.read(jsonString);
                    if (eventMap == null) {
                        throw new EventBuilderProcessingException("Missing event MetaData attributes, Event does not match with the stream : " + this.eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion());
                    }
                    for (Map.Entry<Object, Object> eventAttribute : eventMap.entrySet()) {
                        if (eventAttribute.getKey().equals(metaData.getName())) {
                            attributeArray[attributeCount++] = getPropertyValue(eventAttribute.getValue(), metaData.getType());
                        }
                    }

                }
            }
            if (noCorrelationData > 0) {
                for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                    JsonPath jsonPath = JsonPath.compile("$." + EventBuilderConstants.EVENT_PARENT_TAG + "." + EventBuilderConstants.EVENT_CORRELATION_TAG);
                    Map<Object, Object> eventMap = jsonPath.read(jsonString);
                    if (eventMap == null) {
                        throw new EventBuilderProcessingException("Missing CorrelationData attributes, Event does not match with the stream : " + this.eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion());
                    }
                    for (Map.Entry<Object, Object> eventAttribute : eventMap.entrySet()) {

                        if (eventAttribute.getKey().equals(correlationData.getName())) {
                            attributeArray[attributeCount++] = getPropertyValue(eventAttribute.getValue(), correlationData.getType());
                        }
                    }

                }
            }
            if (noPayloadData > 0) {
                for (Attribute payloadData : streamDefinition.getPayloadData()) {
                    JsonPath jsonPath = JsonPath.compile("$." + EventBuilderConstants.EVENT_PARENT_TAG + "." + EventBuilderConstants.EVENT_PAYLOAD_TAG);
                    Map<Object, Object> eventMap = jsonPath.read(jsonString);
                    if (eventMap == null) {
                        throw new EventBuilderProcessingException("Missing PayloadData attributes, Event does not match with the stream : " + this.eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion());
                    }
                    for (Map.Entry<Object, Object> eventAttribute : eventMap.entrySet()) {

                        if (eventAttribute.getKey().equals(payloadData.getName())) {
                            attributeArray[attributeCount++] = getPropertyValue(eventAttribute.getValue(), payloadData.getType());
                        }
                    }

                }
            }
            if (noMetaData + noCorrelationData + noPayloadData != attributeCount) {
                throw new EventBuilderProcessingException("Event attributes are not matching with the stream : " + this.eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion());
            }
        }
        return attributeArray;
    }


    private Object getPropertyValue(Object propertyValue, AttributeType attributeType) {
        if (AttributeType.BOOL.equals(attributeType)) {
            return Boolean.parseBoolean(propertyValue.toString());
        } else if (AttributeType.DOUBLE.equals(attributeType)) {
            return Double.parseDouble(propertyValue.toString());
        } else if (AttributeType.FLOAT.equals(attributeType)) {
            return Float.parseFloat(propertyValue.toString());
        } else if (AttributeType.INT.equals(attributeType)) {
            return Integer.parseInt(propertyValue.toString());
        } else if (AttributeType.LONG.equals(attributeType)) {
            return Long.parseLong(propertyValue.toString());
        } else {
            return propertyValue.toString();
        }
    }

    private class JsonPathData {
        private AttributeType type;
        private JsonPath jsonPath;
        private String defaultValue;

        public JsonPathData(JsonPath jsonPath, AttributeType type, String defaultValue) {
            this.type = type;
            this.jsonPath = jsonPath;
            this.defaultValue = defaultValue;
        }

        private String getDefaultValue() {
            return defaultValue;
        }

        private JsonPath getJsonPath() {
            return jsonPath;
        }

        private AttributeType getType() {
            return type;
        }
    }

    private void createAttributeJsonPathList(StreamDefinition streamDefinition,
                                             List<InputMappingAttribute> inputMappingAttributeList) {
        if (streamDefinition.getMetaData() != null && streamDefinition.getMetaData().size() > 0) {
            for (Attribute metaData : streamDefinition.getMetaData()) {
                InputMappingAttribute inputMappingAttribute = getInputMappingAttribute(EventBuilderConstants.META_DATA_PREFIX + metaData.getName(), inputMappingAttributeList);
                attributeJsonPathDataList.add(new JsonPathData(JsonPath.compile(inputMappingAttribute.getFromElementKey()), inputMappingAttribute.getToElementType(), inputMappingAttribute.getDefaultValue()));
            }
        }

        if (streamDefinition.getCorrelationData() != null &&streamDefinition.getCorrelationData().size() > 0) {
            for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                InputMappingAttribute inputMappingAttribute = getInputMappingAttribute(EventBuilderConstants.CORRELATION_DATA_PREFIX + correlationData.getName(), inputMappingAttributeList);
                attributeJsonPathDataList.add(new JsonPathData(JsonPath.compile(inputMappingAttribute.getFromElementKey()), inputMappingAttribute.getToElementType(), inputMappingAttribute.getDefaultValue()));

            }
        }

        if (streamDefinition.getPayloadData() != null && streamDefinition.getPayloadData().size() > 0) {
            for (Attribute payloadData : streamDefinition.getPayloadData()) {
                InputMappingAttribute inputMappingAttribute = getInputMappingAttribute(payloadData.getName(), inputMappingAttributeList);
                attributeJsonPathDataList.add(new JsonPathData(JsonPath.compile(inputMappingAttribute.getFromElementKey()), inputMappingAttribute.getToElementType(), inputMappingAttribute.getDefaultValue()));

            }
        }
    }


    private InputMappingAttribute getInputMappingAttribute(String mappingAttribute,
                                                           List<InputMappingAttribute> inputMappingAttributeList) {
        for (InputMappingAttribute inputMappingAttribute : inputMappingAttributeList) {
            if (inputMappingAttribute.getToElementKey().equals(mappingAttribute)) {
                return inputMappingAttribute;
            }
        }

        log.error("Json input mapping attribute not found");
        return null;
    }

}
