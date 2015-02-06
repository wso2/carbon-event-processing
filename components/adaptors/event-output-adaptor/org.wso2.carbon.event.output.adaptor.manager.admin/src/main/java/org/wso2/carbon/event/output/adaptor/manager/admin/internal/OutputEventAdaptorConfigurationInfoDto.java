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
package org.wso2.carbon.event.output.adaptor.manager.admin.internal;

/**
 * Event Configuration Details are stored (Name and Type) which needed to display in the available event adaptor list
 */
public class OutputEventAdaptorConfigurationInfoDto {
    private String eventAdaptorName;
    private String eventAdaptorType;
    private boolean enableTracing;
    private boolean enableStats;
    private boolean editable;

    public String getEventAdaptorName() {
        return eventAdaptorName;
    }

    public void setEventAdaptorName(String eventAdaptorName) {
        this.eventAdaptorName = eventAdaptorName;
    }

    public String getEventAdaptorType() {
        return eventAdaptorType;
    }

    public void setEventAdaptorType(String eventAdaptorType) {
        this.eventAdaptorType = eventAdaptorType;
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

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }
}
