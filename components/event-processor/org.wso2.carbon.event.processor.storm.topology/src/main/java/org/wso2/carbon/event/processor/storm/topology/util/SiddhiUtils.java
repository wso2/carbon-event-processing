/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.storm.topology.util;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

public class SiddhiUtils {
    public static List<StreamDefinition> toSiddhiStreamDefinitions(String[] streamDefinitionString) {
        SiddhiManager siddhiManager = new SiddhiManager(new SiddhiConfiguration());

        for (String streamDefinition : streamDefinitionString) {
            if (streamDefinition.contains("define stream")) {
                siddhiManager.defineStream(streamDefinition);
            } else {
                throw new RuntimeException("Invalid stream definition Definition :" + streamDefinition);
            }
        }
        return siddhiManager.getStreamDefinitions();
    }

    public static List<org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition> toInternalStreamDefinitions(List<StreamDefinition> siddhiStreamDefinitions) {
        List<org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition> internalStreamDefinitions = new ArrayList<org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition>();

        for (StreamDefinition siddhiStreamDefinition : siddhiStreamDefinitions) {
            internalStreamDefinitions.add(convertToInternalStream(siddhiStreamDefinition));
        }

        return internalStreamDefinitions;
    }

    public static org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition convertToInternalStream(org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition) {
        org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition internalStreamDefn = new org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition();
        internalStreamDefn.setStreamId(siddhiStreamDefinition.getStreamId());
        for (org.wso2.siddhi.query.api.definition.Attribute attribute : siddhiStreamDefinition.getAttributeList()) {
            internalStreamDefn.addAttribute(attribute.getName(), convertAttributeType(attribute.getType()));
        }
        return internalStreamDefn;
    }

    private static org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type convertAttributeType(org.wso2.siddhi.query.api.definition.Attribute.Type type) {
        switch (type) {
            case INT:
                return org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type.INTEGER;
            case LONG:
                return org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type.LONG;
            case BOOL:
                return org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type.BOOLEAN;
            case FLOAT:
                return org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type.FLOAT;
            case DOUBLE:
                return org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type.DOUBLE;
            case STRING:
                return org.wso2.carbon.event.processor.storm.common.event.server.StreamDefinition.Type.STRING;
            default:
                return null;
        }
    }


    public static org.wso2.carbon.databridge.commons.StreamDefinition toFlatDataBridgeStreamDefinition(StreamDefinition siddhiStreamDefinition) {
        org.wso2.carbon.databridge.commons.StreamDefinition databridgeStreamDefinition = new
                org.wso2.carbon.databridge.commons.StreamDefinition(siddhiStreamDefinition.getStreamId());

        List<org.wso2.carbon.databridge.commons.Attribute> databridgeAttributes = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();

        for (Attribute attribute : siddhiStreamDefinition.getAttributeList()) {
            databridgeAttributes.add(SiddhiUtils.convertToDatabridgeAttribute(attribute));
        }
        databridgeStreamDefinition.setPayloadData(databridgeAttributes);

        return databridgeStreamDefinition;
    }

    public static org.wso2.carbon.databridge.commons.Attribute convertToDatabridgeAttribute(Attribute attribute) {
        AttributeType type;
        switch (attribute.getType()) {
            case LONG:
                type = AttributeType.LONG;
                break;
            case INT:
                type = AttributeType.INT;
                break;
            case FLOAT:
                type = AttributeType.FLOAT;
                break;
            case DOUBLE:
                type = AttributeType.DOUBLE;
                break;
            case BOOL:
                type = AttributeType.BOOL;
                break;
            default:
                type = AttributeType.STRING;
                break;
        }
        return new org.wso2.carbon.databridge.commons.Attribute(attribute.getName(), type);
    }

    public static String getSiddhiStreamName(String databridgeStreamID) {
        // Remove the version of data bridge stream Id to get Siddhi stream Id
        return databridgeStreamID.substring(0, databridgeStreamID.indexOf(":"));
    }
}
