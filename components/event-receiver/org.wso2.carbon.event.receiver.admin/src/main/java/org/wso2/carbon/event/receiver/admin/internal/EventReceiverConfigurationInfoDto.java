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
package org.wso2.carbon.event.receiver.admin.internal;

/**
 * Event Formatter Configuration Details are stored in this class
 */

public class EventReceiverConfigurationInfoDto {

    private String eventReceiverName;
    private String inputMappingType;
    private String inputEventAdaptorType;
    private String toStreamId;
    private boolean enableTracing;
    private boolean enableStats;

    public String getInputEventAdaptorType() {
        return inputEventAdaptorType;
    }

    public void setInputEventAdaptorType(String inputEventAdaptorType) {
        this.inputEventAdaptorType = inputEventAdaptorType;
    }

    public String getEventReceiverName() {
        return eventReceiverName;
    }

    public void setEventReceiverName(String eventReceiverName) {
        this.eventReceiverName = eventReceiverName;
    }

    public String getInputMappingType() {
        return inputMappingType;
    }

    public void setInputMappingType(String inputMappingType) {
        this.inputMappingType = inputMappingType;
    }

    public String getToStreamId() {
        return toStreamId;
    }

    public void setToStreamId(String toStreamId) {
        this.toStreamId = toStreamId;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}
