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
package org.wso2.carbon.event.builder.admin.internal;

/**
 * to store Not deployed event builder configuration file details (filepath & event builder name)
 */
public class EventBuilderConfigurationFileDto {

    private String filename;
    private String eventBuilderName;
    private String deploymentStatusMsg;

    public EventBuilderConfigurationFileDto(String filename, String eventBuilderName, String deploymentStatusMsg) {
        this.filename = filename;
        this.eventBuilderName = eventBuilderName;
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

    public String getEventBuilderName() {
        return eventBuilderName;
    }

    public void setEventBuilderName(String eventBuilderName) {
        this.eventBuilderName = eventBuilderName;
    }
}
