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
package org.wso2.carbon.event.builder.core.internal.type.map;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderProcessingException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderStreamValidationException;
import org.wso2.carbon.event.builder.core.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInputMapper implements InputMapper {
    private int noMetaData;
    private int noCorrelationData;
    private int noPayloadData;
    private StreamDefinition streamDefinition;
    private Object[] attributePositionKeyMap = null;
    private EventBuilderConfiguration eventBuilderConfiguration = null;

    public MapInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                          StreamDefinition streamDefinition)
            throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        this.streamDefinition = streamDefinition;

        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof MapInputMapping) {
            MapInputMapping mapInputMapping = (MapInputMapping) eventBuilderConfiguration.getInputMapping();
            if (mapInputMapping.isCustomMappingEnabled()) {

                Map<Integer, Object> positionKeyMap = new HashMap<Integer, Object>();
                for (InputMappingAttribute inputMappingAttribute : mapInputMapping.getInputMappingAttributes()) {
                    positionKeyMap.put(inputMappingAttribute.getToStreamPosition(), inputMappingAttribute.getFromElementKey());
                    if (positionKeyMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                        this.attributePositionKeyMap = null;
                        throw new EventBuilderStreamValidationException("Error creating map mapping. '"+inputMappingAttribute.getToElementKey()+"' position not found.",streamDefinition.getStreamId());
                    }
                }
                this.attributePositionKeyMap = new Object[positionKeyMap.size()];
                for (int i = 0; i < attributePositionKeyMap.length; i++) {
                    attributePositionKeyMap[i] = positionKeyMap.get(i);
                }
            } else {
                this.noMetaData = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
                this.noCorrelationData += streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
                this.noPayloadData += streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
            }

        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderProcessingException {
        if (attributePositionKeyMap == null) {
            throw new EventBuilderProcessingException("Input mapping is not available for the current input stream definition:");
        }
        Object[] outObjArray;
        if (obj instanceof Map) {
            Map eventMap = (Map) obj;
            List<Object> outObjList = new ArrayList<Object>();
            for (int i = 0; i < this.attributePositionKeyMap.length; i++) {
                outObjList.add(eventMap.get(this.attributePositionKeyMap[i]));
            }
            outObjArray = outObjList.toArray();
        } else {
            throw new EventBuilderProcessingException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }

        return outObjArray;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderProcessingException {

        Object attributeArray[] = new Object[noMetaData + noCorrelationData + noPayloadData];
        int attributeCount = 0;
        if (obj instanceof Map) {
            Map<Object, Object> eventMap = (Map<Object, Object>) obj;

            for (Map.Entry<Object, Object> eventAttribute : eventMap.entrySet()) {
                boolean setFlag = false;

                if (noMetaData > 0) {
                    for (Attribute metaData : streamDefinition.getMetaData()) {
                        if (eventAttribute.getKey().equals(EventBuilderConstants.META_DATA_PREFIX + metaData.getName())) {
                            attributeArray[attributeCount++] = eventAttribute.getValue();
                            setFlag = true;
                            break;
                        }
                    }
                }

                if (noCorrelationData > 0 && !setFlag) {
                    for (Attribute correlationData : streamDefinition.getCorrelationData()) {
                        if (eventAttribute.getKey().equals(EventBuilderConstants.CORRELATION_DATA_PREFIX + correlationData.getName())) {
                            attributeArray[attributeCount++] = eventAttribute.getValue();
                            setFlag = true;
                            break;
                        }
                    }
                }

                if (noPayloadData > 0 && !setFlag) {
                    for (Attribute payloadData : streamDefinition.getPayloadData()) {
                        if (eventAttribute.getKey().equals(payloadData.getName())) {
                            attributeArray[attributeCount++] = eventAttribute.getValue();
                            break;
                        }
                    }
                }

            }

            if (noMetaData + noCorrelationData + noPayloadData != attributeCount) {
                throw new EventBuilderProcessingException("Event attributes are not matching with the stream : " + this.eventBuilderConfiguration.getToStreamName() + ":" + eventBuilderConfiguration.getToStreamVersion());
            }

        } else {
            throw new EventBuilderProcessingException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }

        return attributeArray;

    }

    @Override
    public Attribute[] getOutputAttributes() {
        MapInputMapping mapInputMapping = (MapInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = mapInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

}
