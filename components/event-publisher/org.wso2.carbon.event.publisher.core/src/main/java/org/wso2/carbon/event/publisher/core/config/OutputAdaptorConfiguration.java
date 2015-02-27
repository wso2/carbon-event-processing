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
package org.wso2.carbon.event.publisher.core.config;


import java.util.Map;

/**
 * This class contain the configuration details of the event
 */

public class OutputAdaptorConfiguration {

    private String endpointType;
    private String adaptorName;
    private Map<String, String> staticProperties;
    private Map<String, String> dynamicProperties;

    public OutputAdaptorConfiguration() {
    }

    public OutputAdaptorConfiguration(String adaptorName, String endpointType, Map<String, String> staticProperties, Map<String, String> dynamicProperties) {
        this.adaptorName = adaptorName;
        this.endpointType = endpointType;
        this.staticProperties = staticProperties;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public String getAdaptorName() {
        return adaptorName;
    }

    public void setAdaptorName(String adaptorName) {
        this.adaptorName = adaptorName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutputAdaptorConfiguration)) return false;

        OutputAdaptorConfiguration that = (OutputAdaptorConfiguration) o;

        if (!adaptorName.equals(that.adaptorName)) return false;
        if (!endpointType.equals(that.endpointType)) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointType.hashCode();
        result = 31 * result + adaptorName.hashCode();
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}