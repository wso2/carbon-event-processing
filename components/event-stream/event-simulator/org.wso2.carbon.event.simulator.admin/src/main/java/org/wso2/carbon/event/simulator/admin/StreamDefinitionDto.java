/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.admin;


public class StreamDefinitionDto {

    private String name;
    private String version;
    private String description;
    private String nickName;
    private String[] metaData;
    private String[] correlationData;
    private String[] payloadData;

    public String[] getMetaData() {
        return metaData;
    }

    public void setMetaData(String[] metaData) {
        this.metaData = metaData;
    }

    public String[] getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(String[] correlationData) {
        this.correlationData = correlationData;
    }

    public String[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(String[] payloadData) {
        this.payloadData = payloadData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
