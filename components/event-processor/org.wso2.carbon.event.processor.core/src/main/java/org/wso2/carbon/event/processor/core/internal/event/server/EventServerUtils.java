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

package org.wso2.carbon.event.processor.core.internal.event.server;

/**
 * Created by suho on 6/5/14.
 */
public class EventServerUtils {


    public static StreamRuntimeInfo createStreamRuntimeInfo(StreamDefinition streamDefinition) {
        StreamRuntimeInfo streamRuntimeInfo= new StreamRuntimeInfo(streamDefinition.getStreamId());

        int messageSize=0;
        int stringAttributes=0;
        StreamDefinition.Type[] attributeTypes=new StreamDefinition.Type[streamDefinition.getAttributeList().size()];

        java.util.List<StreamDefinition.Attribute> attributeList = streamDefinition.getAttributeList();
        for (int i = 0; i < attributeList.size(); i++) {
            StreamDefinition.Attribute attribute = attributeList.get(i);
            switch (attribute.getType()) {

                case INTEGER:
                    messageSize += 4;
                    attributeTypes[i]= StreamDefinition.Type.INTEGER;
                    break;
                case LONG:
                    messageSize += 8;
                    attributeTypes[i]= StreamDefinition.Type.LONG;
                    break;
                case BOOLEAN:
                    messageSize += 1;
                    attributeTypes[i]= StreamDefinition.Type.BOOLEAN;
                    break;
                case FLOAT:
                    messageSize += 4;
                    attributeTypes[i]= StreamDefinition.Type.FLOAT;
                    break;
                case DOUBLE:
                    messageSize += 8;
                    attributeTypes[i]= StreamDefinition.Type.DOUBLE;
                    break;
                case STRING:
                    messageSize += 2;
                    stringAttributes++;
                    attributeTypes[i]= StreamDefinition.Type.STRING;
                    break;
            }
        }
        streamRuntimeInfo.setFixedMessageSize(messageSize);
        streamRuntimeInfo.setNoOfStringAttributes(stringAttributes);
        streamRuntimeInfo.setNoOfAttributes(streamDefinition.getAttributeList().size());
        streamRuntimeInfo.setAttributeTypes(attributeTypes);

        return streamRuntimeInfo;
    }
}
