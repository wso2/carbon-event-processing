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
package org.wso2.carbon.event.formatter.admin.internal;

/**
 * to store Not deployed event formatter configuration file details (filepath & event formatter name)
 */
public class EventFormatterConfigurationFileDto {

    private String fileName;
    private String eventFormatterName;
    private String deploymentStatusMsg;

    public EventFormatterConfigurationFileDto(String fileName, String eventFormatterName, String deploymentStatusMsg) {
        this.fileName = fileName;
        this.eventFormatterName = eventFormatterName;
        this.deploymentStatusMsg = deploymentStatusMsg;
    }

    public String getDeploymentStatusMsg() {
        return deploymentStatusMsg;
    }

    public void setDeploymentStatusMsg(String deploymentStatusMsg) {
        this.deploymentStatusMsg = deploymentStatusMsg;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEventFormatterName() {
        return eventFormatterName;
    }

    public void setEventFormatterName(String eventFormatterName) {
        this.eventFormatterName = eventFormatterName;
    }
}
