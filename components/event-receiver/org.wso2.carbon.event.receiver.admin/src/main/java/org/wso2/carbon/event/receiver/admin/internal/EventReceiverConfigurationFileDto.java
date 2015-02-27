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
 * to store Not deployed event receiver configuration file details (filepath & event receiver name)
 */
public class EventReceiverConfigurationFileDto {

    private String filename;
    private String eventReceiverName;
    private String deploymentStatusMsg;

    public EventReceiverConfigurationFileDto(String filename, String eventReceiverName, String deploymentStatusMsg) {
        this.filename = filename;
        this.eventReceiverName = eventReceiverName;
        this.deploymentStatusMsg = deploymentStatusMsg;
    }

    public String getDeploymentStatusMsg() {
        return deploymentStatusMsg;
    }

    public void setDeploymentStatusMsg(String deploymentStatusMsg) {
        this.deploymentStatusMsg = deploymentStatusMsg;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getEventReceiverName() {
        return eventReceiverName;
    }

    public void setEventReceiverName(String eventReceiverName) {
        this.eventReceiverName = eventReceiverName;
    }
}
