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
package org.wso2.carbon.event.notifier.core.config;


import java.util.Map;

/**
 * This class contain the configuration details of the event
 */

public class EndpointAdaptorConfiguration {

    private String endpointType;

    private String endpointAdaptorName;

    private InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = null;


    public EndpointAdaptorConfiguration() {
    }


    public EndpointAdaptorConfiguration(String endpointAdaptorName, String endpointType, InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration) {
        this.endpointAdaptorName = endpointAdaptorName;
        this.endpointType = endpointType;
        this.internalOutputEventAdaptorConfiguration = internalOutputEventAdaptorConfiguration;
    }

    public InternalOutputEventAdaptorConfiguration getOutputAdaptorConfiguration() {
        return internalOutputEventAdaptorConfiguration;
    }

    public void setOutputAdaptorConfiguration(
            InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration) {
        this.internalOutputEventAdaptorConfiguration = internalOutputEventAdaptorConfiguration;
    }


    public Map<String, String> getOutputAdaptorProperties() {
        if (internalOutputEventAdaptorConfiguration != null) {
            return internalOutputEventAdaptorConfiguration.getProperties();
        }
        return null;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getEndpointAdaptorName() {
        return endpointAdaptorName;
    }

    public void setEndpointAdaptorName(String endpointAdaptorName) {
        this.endpointAdaptorName = endpointAdaptorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EndpointAdaptorConfiguration)) {
            return false;
        }

        EndpointAdaptorConfiguration that = (EndpointAdaptorConfiguration) o;

        if (internalOutputEventAdaptorConfiguration != null ? !internalOutputEventAdaptorConfiguration.equals(that.internalOutputEventAdaptorConfiguration) : that.internalOutputEventAdaptorConfiguration != null) {
            return false;
        }

        if (!endpointAdaptorName.equals(that.endpointAdaptorName)) {
            return false;
        }

        if (!endpointType.equals(that.endpointType)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointAdaptorName.hashCode();
        result = 31 * result + endpointType.hashCode();
        result = 31 * result + (internalOutputEventAdaptorConfiguration != null ? internalOutputEventAdaptorConfiguration.hashCode() : 0);
        return result;
    }
}