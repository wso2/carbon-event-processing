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

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMapper;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventReceiverRuntimeValidator {

    public static void validateExportedStream(EventReceiverConfiguration eventReceiverConfiguration,
                                              StreamDefinition exportedStreamDefinition,
                                              InputMapper inputMapper) {
        if (eventReceiverConfiguration != null && exportedStreamDefinition != null) {
            if (eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled()) {
                String streamId = exportedStreamDefinition.getStreamId();
                if (inputMapper.getOutputAttributes() == null || inputMapper.getOutputAttributes().length == 0) {
                    throw new EventReceiverStreamValidationException("The input mapper is not exporting any output attributes for stream " + streamId);
                }
                List<Attribute> outputAttributes = new ArrayList<Attribute>(Arrays.asList(inputMapper.getOutputAttributes()));
                List<Attribute> metaAttributeList = exportedStreamDefinition.getMetaData();
                if (metaAttributeList != null) {
                    for (Attribute attribute : metaAttributeList) {
                        Attribute prependedAttribute = new Attribute(EventReceiverConstants.META_DATA_PREFIX + attribute.getName(), attribute.getType());
                        if (!outputAttributes.contains(prependedAttribute)) {
                            throw new EventReceiverStreamValidationException("The meta data attribute '" + attribute.getName()
                                    + "' in stream : '" + streamId + "' cannot be found under attributes exported by this event receiver mapping", streamId);
                        } else {
                            outputAttributes.remove(prependedAttribute);
                        }
                    }
                }
                List<Attribute> correlationAttributeList = exportedStreamDefinition.getCorrelationData();
                if (correlationAttributeList != null) {
                    for (Attribute attribute : correlationAttributeList) {
                        Attribute prependedAttribute = new Attribute(EventReceiverConstants.CORRELATION_DATA_PREFIX + attribute.getName(), attribute.getType());
                        if (!outputAttributes.contains(prependedAttribute)) {
                            throw new EventReceiverStreamValidationException("The correlation data attribute '" + attribute.getName()
                                    + "' in stream : '" + streamId + "' cannot be found under attributes exported by this event receiver mapping", streamId);
                        } else {
                            outputAttributes.remove(prependedAttribute);
                        }
                    }
                }
                List<Attribute> payloadAttributeList = exportedStreamDefinition.getPayloadData();
                if (payloadAttributeList != null) {
                    for (Attribute attribute : payloadAttributeList) {
                        if (!outputAttributes.contains(attribute)) {
                            throw new EventReceiverStreamValidationException("The payload data attribute '" + attribute.getName()
                                    + "' in stream : '" + streamId + "' cannot be found under attributes exported by this event receiver mapping", streamId);
                        } else {
                            outputAttributes.remove(attribute);
                        }
                    }
                }
                if (outputAttributes.size() > 0) {
                    throw new EventReceiverStreamValidationException("The attribute '" + outputAttributes.get(0).getName()
                            + "' exported by this event receiver mapping cannot be found not in : '" + streamId + "'", streamId);

                }
            }
        }
    }
}
