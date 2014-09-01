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
package org.wso2.carbon.event.processor.admin;

/**
 * Represents mapping between a siddhi stream and a cep stream.
 */
public class StreamConfigurationDto {

    private String streamId;

    // for imported streams : as
    // for exported streams : valueOf
    private String siddhiStreamName;

    public StreamConfigurationDto() {
    }


    public StreamConfigurationDto(String streamId, String siddhiStreamName) {
        this.siddhiStreamName = siddhiStreamName;
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }


    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getSiddhiStreamName() {
        return siddhiStreamName;
    }

    public void setSiddhiStreamName(String siddhiStreamName) {
        this.siddhiStreamName = siddhiStreamName;
    }


}
