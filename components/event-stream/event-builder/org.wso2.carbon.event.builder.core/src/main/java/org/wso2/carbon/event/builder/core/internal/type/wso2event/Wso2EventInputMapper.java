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
package org.wso2.carbon.event.builder.core.internal.type.wso2event;

import com.google.common.collect.ObjectArrays;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderProcessingException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderStreamValidationException;
import org.wso2.carbon.event.builder.core.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.*;

public class Wso2EventInputMapper implements InputMapper {
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private StreamDefinition exportedStreamDefinition = null;
    private StreamDefinition importedStreamDefinition = null;
    private Map<InputDataType, int[]> inputDataTypeSpecificPositionMap = null;

    public Wso2EventInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                                StreamDefinition exportedStreamDefinition)
            throws EventBuilderConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        this.eventBuilderConfiguration = eventBuilderConfiguration;
        //TODO It is better if logic to check from stream definition store can be moved outside of input mapper.
        Map<String, String> inputMessageProperties = eventBuilderConfiguration.getInputStreamConfiguration()
                .getInputEventAdaptorMessageConfiguration().getInputMessageProperties();
        String fromStreamName = inputMessageProperties.get(EventBuilderConstants.ADAPTOR_MESSAGE_STREAM_NAME);
        String fromStreamVersion = inputMessageProperties.get(EventBuilderConstants.ADAPTOR_MESSAGE_STREAM_VERSION);
        if (fromStreamName == null || fromStreamVersion == null || (fromStreamName.isEmpty()) || (fromStreamVersion.isEmpty())) {
            throw new EventBuilderConfigurationException("Required message properties stream name and stream version not found");
        }
        EventStreamService eventStreamService = EventBuilderServiceValueHolder.getEventStreamService();

        try {
            this.importedStreamDefinition = eventStreamService.getStreamDefinition(fromStreamName, fromStreamVersion, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventBuilderStreamValidationException("Error while retrieving stream definition : " + e.getMessage(), fromStreamName + ":" + fromStreamVersion);
        }

        validateInputStreamAttributes();

        if (importedStreamDefinition != null && eventBuilderConfiguration.getInputMapping().isCustomMappingEnabled()) {
            Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) this.eventBuilderConfiguration.getInputMapping();
            this.exportedStreamDefinition = exportedStreamDefinition;

            this.inputDataTypeSpecificPositionMap = new HashMap<InputDataType, int[]>();
            Map<Integer, Integer> payloadDataMap = new TreeMap<Integer, Integer>();
            Map<Integer, Integer> metaDataMap = new TreeMap<Integer, Integer>();
            Map<Integer, Integer> correlationDataMap = new TreeMap<Integer, Integer>();

            List<Attribute> allAttributes = new ArrayList<Attribute>();
            if (importedStreamDefinition.getMetaData() != null && !importedStreamDefinition.getMetaData().isEmpty()) {
                allAttributes.addAll(importedStreamDefinition.getMetaData());
            }
            if (importedStreamDefinition.getCorrelationData() != null && !importedStreamDefinition.getCorrelationData().isEmpty()) {
                allAttributes.addAll(importedStreamDefinition.getCorrelationData());
            }
            if (importedStreamDefinition.getPayloadData() != null && !importedStreamDefinition.getPayloadData().isEmpty()) {
                allAttributes.addAll(importedStreamDefinition.getPayloadData());
            }
            int metaCount = 0, correlationCount = 0, payloadCount = 0;
            for (InputMappingAttribute inputMappingAttribute : wso2EventInputMapping.getInputMappingAttributes()) {
                if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.META_DATA_PREFIX)) {
                    for (int i = 0; i < allAttributes.size(); i++) {
                        if (allAttributes.get(i).getName().equals(inputMappingAttribute.getFromElementKey())) {
                            metaDataMap.put(metaCount, i);
                            break;
                        }
                    }
                    if (metaDataMap.get(metaCount++) == null) {
                        this.inputDataTypeSpecificPositionMap = null;
                        throw new EventBuilderStreamValidationException("Cannot find a corresponding meta data input attribute '"
                                + inputMappingAttribute.getFromElementKey() + "' in stream with id " + importedStreamDefinition.getStreamId(), importedStreamDefinition.getStreamId());
                    }
                } else if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX)) {
                    for (int i = 0; i < allAttributes.size(); i++) {
                        if (allAttributes.get(i).getName().equals(inputMappingAttribute.getFromElementKey())) {
                            correlationDataMap.put(correlationCount, i);
                            break;
                        }
                    }
                    if (correlationDataMap.get(correlationCount++) == null) {
                        this.inputDataTypeSpecificPositionMap = null;
                        throw new EventBuilderStreamValidationException("Cannot find a corresponding correlation data input attribute '"
                                + inputMappingAttribute.getFromElementKey() + "' in stream with id " + importedStreamDefinition.getStreamId(), importedStreamDefinition.getStreamId());
                    }
                } else {
                    for (int i = 0; i < allAttributes.size(); i++) {
                        if (allAttributes.get(i).getName().equals(inputMappingAttribute.getFromElementKey())) {
                            payloadDataMap.put(payloadCount, i);
                            break;
                        }
                    }
                    if (payloadDataMap.get(payloadCount++) == null) {
                        this.inputDataTypeSpecificPositionMap = null;
                        throw new EventBuilderStreamValidationException("Cannot find a corresponding payload data input attribute '"
                                + inputMappingAttribute.getFromElementKey() + "' in stream with id : " + importedStreamDefinition.getStreamId(), importedStreamDefinition.getStreamId());
                    }
                }
            }
            int[] metaPositions = new int[metaDataMap.size()];
            for (int i = 0; i < metaPositions.length; i++) {
                metaPositions[i] = metaDataMap.get(i);
            }
            inputDataTypeSpecificPositionMap.put(InputDataType.META_DATA, metaPositions);
            int[] correlationPositions = new int[correlationDataMap.size()];
            for (int i = 0; i < correlationPositions.length; i++) {
                correlationPositions[i] = correlationDataMap.get(i);
            }
            inputDataTypeSpecificPositionMap.put(InputDataType.CORRELATION_DATA, correlationPositions);
            int[] payloadPositions = new int[payloadDataMap.size()];
            for (int i = 0; i < payloadPositions.length; i++) {
                payloadPositions[i] = payloadDataMap.get(i);
            }
            inputDataTypeSpecificPositionMap.put(InputDataType.PAYLOAD_DATA, payloadPositions);

        } else if (importedStreamDefinition != null && (!eventBuilderConfiguration.getInputMapping().isCustomMappingEnabled())) {
            if (importedStreamDefinition.getCorrelationData() != null ? !importedStreamDefinition.getCorrelationData().equals(exportedStreamDefinition.getCorrelationData()) : exportedStreamDefinition.getCorrelationData() != null) {
                throw new EventBuilderStreamValidationException("Input stream definition : " + importedStreamDefinition + " not matching with output stream definition : " + exportedStreamDefinition + " to create pass-through link ", importedStreamDefinition.getStreamId());
            }
            if (importedStreamDefinition.getMetaData() != null ? !importedStreamDefinition.getMetaData().equals(exportedStreamDefinition.getMetaData()) : exportedStreamDefinition.getMetaData() != null) {
                throw new EventBuilderStreamValidationException("Input stream definition : " + importedStreamDefinition + " not matching with output stream definition : " + exportedStreamDefinition + " to create pass-through link ", importedStreamDefinition.getStreamId());
            }
            if (importedStreamDefinition.getPayloadData() != null ? !importedStreamDefinition.getPayloadData().equals(exportedStreamDefinition.getPayloadData()) : exportedStreamDefinition.getPayloadData() != null) {
                throw new EventBuilderStreamValidationException("Input stream definition : " + importedStreamDefinition + " not matching with output stream definition : " + exportedStreamDefinition + " to create pass-through link ", importedStreamDefinition.getStreamId());
            }
        }
    }

    private void validateInputStreamAttributes()
            throws EventBuilderConfigurationException {
        if (this.importedStreamDefinition != null) {
            Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();
            List<InputMappingAttribute> inputMappingAttributes = wso2EventInputMapping.getInputMappingAttributes();
            List<String> inputStreamMetaAttributeNames = getAttributeNamesList(importedStreamDefinition.getMetaData());
            List<String> inputStreamCorrelationAttributeNames = getAttributeNamesList(importedStreamDefinition.getCorrelationData());
            List<String> inputStreamPayloadAttributeNames = getAttributeNamesList(importedStreamDefinition.getPayloadData());

            for (InputMappingAttribute inputMappingAttribute : inputMappingAttributes) {
                if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.META_DATA_PREFIX)
                        && !inputStreamMetaAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                    throw new EventBuilderStreamValidationException("Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ", importedStreamDefinition.getStreamId());
                } else if (inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX)
                        && !inputStreamCorrelationAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                    throw new EventBuilderStreamValidationException("Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ", importedStreamDefinition.getStreamId());
                } else if (!inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.META_DATA_PREFIX) && !inputMappingAttribute.getToElementKey().startsWith(EventBuilderConstants.CORRELATION_DATA_PREFIX) && !inputStreamPayloadAttributeNames.contains(inputMappingAttribute.getFromElementKey())) {
                    throw new EventBuilderStreamValidationException("Property " + inputMappingAttribute.getFromElementKey() + " is not in the input stream definition. ", importedStreamDefinition.getStreamId());
                }
            }
        }
    }

    private List<String> getAttributeNamesList(List<Attribute> attributeList) {
        List<String> attributeNamesList = new ArrayList<String>();
        if (attributeList != null) {
            for (Attribute attribute : attributeList) {
                attributeNamesList.add(attribute.getName());
            }
        }

        return attributeNamesList;
    }

    //TODO Profile performance of this method
    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderProcessingException {
        Object[] outObjArray = null;
        if (obj instanceof Event) {
            Event event = (Event) obj;
            Map<String, String> arbitraryMap = event.getArbitraryDataMap();
            if (arbitraryMap != null && !arbitraryMap.isEmpty()) {
                outObjArray = processArbitraryMap(event);
            } else if (inputDataTypeSpecificPositionMap != null) {
                List<Object> outObjList = new ArrayList<Object>();
                Object[] inEventArray = new Object[0];
                if (event.getMetaData() != null) {
                    inEventArray = ObjectArrays.concat(inEventArray, event.getMetaData(), Object.class);
                }
                if (event.getCorrelationData() != null) {
                    inEventArray = ObjectArrays.concat(inEventArray, event.getCorrelationData(), Object.class);
                }
                if (event.getPayloadData() != null) {
                    inEventArray = ObjectArrays.concat(inEventArray, event.getPayloadData(), Object.class);
                }
                int[] metaPositions = inputDataTypeSpecificPositionMap.get(InputDataType.META_DATA);
                for (int metaPosition : metaPositions) {
                    outObjList.add(inEventArray[metaPosition]);
                }
                int[] correlationPositions = inputDataTypeSpecificPositionMap.get(InputDataType.CORRELATION_DATA);
                for (int correlationPosition : correlationPositions) {
                    outObjList.add(inEventArray[correlationPosition]);
                }
                int[] payloadPositions = inputDataTypeSpecificPositionMap.get(InputDataType.PAYLOAD_DATA);
                for (int payloadPosition : payloadPositions) {
                    outObjList.add(inEventArray[payloadPosition]);
                }
                outObjArray = outObjList.toArray();
            }
        }
        return outObjArray;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderProcessingException {
        Object[] outObjArray = null;
        if (obj instanceof Event) {
            Event inputEvent = (Event) obj;
            Object[] metaCorrArray = ObjectArrays.concat(inputEvent.getMetaData() != null ? inputEvent.getMetaData() : new Object[0]
                    , inputEvent.getCorrelationData() != null ? inputEvent.getCorrelationData() : new Object[0], Object.class);
            outObjArray = ObjectArrays.concat
                    (metaCorrArray, inputEvent.getPayloadData() != null ? inputEvent.getPayloadData() : new Object[0], Object.class);
        }
        return outObjArray;
    }

    @Override
    public Attribute[] getOutputAttributes() {
        Wso2EventInputMapping wso2EventInputMapping = (Wso2EventInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = wso2EventInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

    private Object[] processArbitraryMap(Event inEvent) {
        Map<String, String> arbitraryMap = inEvent.getArbitraryDataMap();
        Object[] metaData = new Object[exportedStreamDefinition.getMetaData().size()];
        Object[] correlationData = new Object[exportedStreamDefinition.getCorrelationData().size()];
        Object[] payloadData = new Object[exportedStreamDefinition.getPayloadData().size()];
        for (int i = 0; i < metaData.length; i++) {
            Attribute metaAttribute = exportedStreamDefinition.getMetaData().get(i);
            String value = arbitraryMap.get(EventBuilderConstants.META_DATA_PREFIX + metaAttribute.getName());
            if (value != null) {
                Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, metaAttribute.getType());
                metaData[i] = attributeObj;
            } else {
                metaData[i] = inEvent.getMetaData()[i] != null ? inEvent.getMetaData()[i] : null;
            }
        }
        for (int i = 0; i < correlationData.length; i++) {
            Attribute correlationAttribute = exportedStreamDefinition.getCorrelationData().get(i);
            String value = arbitraryMap.get(EventBuilderConstants.CORRELATION_DATA_PREFIX + correlationAttribute.getName());
            if (value != null) {
                Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, correlationAttribute.getType());
                correlationData[i] = attributeObj;
            } else {
                correlationData[i] = inEvent.getCorrelationData()[i] != null ? inEvent.getCorrelationData()[i] : null;
            }
        }
        for (int i = 0; i < payloadData.length; i++) {
            Attribute payloadAttribute = exportedStreamDefinition.getPayloadData().get(i);
            String value = arbitraryMap.get(payloadAttribute.getName());
            if (value != null) {
                Object attributeObj = EventBuilderUtil.getConvertedAttributeObject(value, payloadAttribute.getType());
                payloadData[i] = attributeObj;
            } else {
                payloadData[i] = inEvent.getPayloadData()[i] != null ? inEvent.getPayloadData()[i] : null;
            }
        }
        int indexCount = 0;
        Object[] outObjArray = new Object[metaData.length + correlationData.length + payloadData.length];
        for (Object attributeObj : metaData) {
            outObjArray[indexCount++] = attributeObj;
        }
        for (Object attributeObj : correlationData) {
            outObjArray[indexCount++] = attributeObj;
        }
        for (Object attributeObj : payloadData) {
            outObjArray[indexCount++] = attributeObj;
        }

        return outObjArray;
    }

    private enum InputDataType {
        META_DATA, CORRELATION_DATA, PAYLOAD_DATA
    }
}
