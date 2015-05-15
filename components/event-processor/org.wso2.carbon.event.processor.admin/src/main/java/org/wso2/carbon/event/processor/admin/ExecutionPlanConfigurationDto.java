/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.processor.admin;

public class ExecutionPlanConfigurationDto {

    private String name;
    private String description;
    private StreamConfigurationDto[] importedStreams;
    private StreamConfigurationDto[] exportedStreams;
    private String executionPlan;
    private boolean tracingEnabled;
    private boolean statisticsEnabled;
    private boolean editable;


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

    public String getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(String executionPlan) {
        this.executionPlan = executionPlan;
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
