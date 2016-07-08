/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.simulator.core.internal.util;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.core.exception.EventSimulatorRuntimeException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EventSimulatorUtil {

    public static Event getWso2Event(StreamDefinition streamDefinition, long timestamp, Object[] data) {
        int metaAttrCount = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
        int correlationAttrCount = streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
        int payloadAttrCount = streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
        Object[] metaAttrArray = new Object[metaAttrCount];
        Object[] correlationAttrArray = new Object[correlationAttrCount];
        Object[] payloadAttrArray = new Object[payloadAttrCount];
        for (int i = 0; i < data.length; i++) {
            if (i < metaAttrCount) {
                metaAttrArray[i] = data[i];
            } else if (i < metaAttrCount + correlationAttrCount) {
                correlationAttrArray[i - metaAttrCount] = data[i];
            } else {
                payloadAttrArray[i - (metaAttrCount + correlationAttrCount)] = data[i];
            }
        }
        return new Event(streamDefinition.getStreamId(), timestamp, metaAttrArray, correlationAttrArray, payloadAttrArray);
    }


    /**
     * Validate the given file path is in the parent directory itself.
     *
     * @param parentDirectory
     * @param filePath
     */
    public static void validatePath(String parentDirectory, String filePath) {
        Path parentPath = Paths.get(parentDirectory);
        Path subPath = Paths.get(filePath).normalize();
        if (!subPath.normalize().startsWith(parentPath)) {
            throw new EventSimulatorRuntimeException("File path is invalid: " + filePath);
        }
    }
}
