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
 * Event Receiver Configuration Details are stored in this class
 */
public class EventReceiverConfigurationDto {
    private String eventReceiverConfigName;
    private String inputMappingType;
    private String inputEventAdaptorType;
    private String toStreamName;
    private String toStreamVersion;
    private String parentSelectorXpath;
    private boolean traceEnabled;
    private boolean statisticsEnabled;
    private boolean customMappingEnabled;
    private EventInputPropertyConfigurationDto[] metaEventReceiverProperties;
    private EventInputPropertyConfigurationDto[] correlationEventReceiverProperties;
    private EventInputPropertyConfigurationDto[] payloadEventReceiverProperties;
    private EventInputPropertyConfigurationDto[] xpathDefinitions;
    private EventReceiverAdaptorPropertyDto[] eventReceiverMessageProperties;

    public String getParentSelectorXpath() {
        return parentSelectorXpath;
    }

    public void setParentSelectorXpath(String parentSelectorXpath) {
        this.parentSelectorXpath = parentSelectorXpath;
    }

    public EventInputPropertyConfigurationDto[] getXpathDefinitions() {
        return xpathDefinitions;
    }

    public void setXpathDefinitions(EventInputPropertyConfigurationDto[] xpathDefinitions) {
        this.xpathDefinitions = xpathDefinitions;
    }

    public boolean isCustomMappingEnabled() {
        return customMappingEnabled;
    }

    public void setCustomMappingEnabled(boolean customMappingEnabled) {
        this.customMappingEnabled = customMappingEnabled;
    }

    public EventReceiverAdaptorPropertyDto[] getEventReceiverMessageProperties() {
        return eventReceiverMessageProperties;
    }

    public void setEventReceiverMessageProperties(EventReceiverAdaptorPropertyDto[] eventReceiverMessageProperties) {
        this.eventReceiverMessageProperties = eventReceiverMessageProperties;
    }

    public EventInputPropertyConfigurationDto[] getCorrelationEventReceiverProperties() {
        return correlationEventReceiverProperties;
    }

    public void setCorrelationEventReceiverProperties(EventInputPropertyConfigurationDto[] correlationEventReceiverProperties) {
        this.correlationEventReceiverProperties = correlationEventReceiverProperties;
    }

    public EventInputPropertyConfigurationDto[] getPayloadEventReceiverProperties() {
        return payloadEventReceiverProperties;
    }

    public void setPayloadEventReceiverProperties(EventInputPropertyConfigurationDto[] payloadEventReceiverProperties) {
        this.payloadEventReceiverProperties = payloadEventReceiverProperties;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public String getToStreamName() {
        return toStreamName;
    }

    public void setToStreamName(String toStreamName) {
        this.toStreamName = toStreamName;
    }

    public String getToStreamVersion() {
        return toStreamVersion;
    }

    public void setToStreamVersion(String toStreamVersion) {
        this.toStreamVersion = toStreamVersion;
    }

    public String getInputEventAdaptorType() {
        return inputEventAdaptorType;
    }

    public void setInputEventAdaptorType(String InputEventAdaptorType) {
        this.inputEventAdaptorType = InputEventAdaptorType;
    }

    public String getInputMappingType() {
        return inputMappingType;
    }

    public void setInputMappingType(String inputMappingType) {
        this.inputMappingType = inputMappingType;
    }

    public String getEventReceiverConfigName() {
        return eventReceiverConfigName;
    }

    public void setEventReceiverConfigName(String eventReceiverConfigName) {
        this.eventReceiverConfigName = eventReceiverConfigName;
    }

    public EventInputPropertyConfigurationDto[] getMetaEventReceiverProperties() {
        return this.metaEventReceiverProperties;
    }

    public void setMetaEventReceiverProperties(EventInputPropertyConfigurationDto[] eventReceiverProperties) {
        this.metaEventReceiverProperties = eventReceiverProperties;
    }
}
