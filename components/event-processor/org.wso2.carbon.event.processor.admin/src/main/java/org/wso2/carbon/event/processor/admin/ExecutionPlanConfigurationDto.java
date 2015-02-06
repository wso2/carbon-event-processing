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

public class ExecutionPlanConfigurationDto {

    private String name;
    private String description;
    private SiddhiConfigurationDto[] siddhiConfigurations;
    private StreamConfigurationDto[] importedStreams;
    private StreamConfigurationDto[] exportedStreams;
    private String queryExpressions;
    private boolean tracingEnabled;
    private boolean statisticsEnabled;
    private boolean editable;

    public SiddhiConfigurationDto[] getSiddhiConfigurations() {
        return siddhiConfigurations;
    }

    public void setSiddhiConfigurations(SiddhiConfigurationDto[] siddhiConfigurations) {
        this.siddhiConfigurations = siddhiConfigurations;
    }

    public StreamConfigurationDto[] getImportedStreams() {
        return importedStreams;
    }

    public void setImportedStreams(StreamConfigurationDto[] importedStreams) {
        this.importedStreams = importedStreams;
    }

    public StreamConfigurationDto[] getExportedStreams() {
        return exportedStreams;
    }

    public void setExportedStreams(StreamConfigurationDto[] exportedStreams) {
        this.exportedStreams = exportedStreams;
    }

    public String getQueryExpressions() {
        return queryExpressions;
    }

    public void setQueryExpressions(String queryExpressions) {
        this.queryExpressions = queryExpressions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public boolean isStatisticsEnabled() {
        return statisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }
}
