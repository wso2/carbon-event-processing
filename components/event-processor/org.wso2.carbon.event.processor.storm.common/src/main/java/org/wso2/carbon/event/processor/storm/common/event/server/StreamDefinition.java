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

package org.wso2.carbon.event.processor.storm.common.event.server;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by suho on 6/5/14.
 */
public class StreamDefinition {

    public enum Type {
        INTEGER, LONG, BOOLEAN, FLOAT, DOUBLE, STRING
    }

    private String streamId;
    private List<Attribute> attributeList = new ArrayList<Attribute>();

    public class Attribute {
        private String name;
        private Type type;

        private Attribute(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }
    }

    public String getStreamId() {
        return streamId;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public void setAttributeList(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public void addAttribute(String name, Type type) {
        this.attributeList.add(new Attribute(name, type));
    }


}
