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
package org.wso2.carbon.event.processor.storm;

import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Map;

// acts as a holder for the components of a query plan.
public class ExecutionPlan {
    private SiddhiManager siddhiManager;
    private ExecutionPlanConfiguration executionPlanConfiguration;
    private Map<String, StreamDefinition> importedStreamDefinitions;
    private Map<String, StreamDefinition> exportedStreamDefinitions;
    private String name;

    public ExecutionPlan() {

    }

    public ExecutionPlan(String name, SiddhiManager siddhiManager,
                         ExecutionPlanConfiguration executionPlanConfiguration) {
        this.siddhiManager = siddhiManager;
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public void setSiddhiManager(SiddhiManager siddhiManager) {
        this.siddhiManager = siddhiManager;
    }

    public ExecutionPlanConfiguration getExecutionPlanConfiguration() {
        return executionPlanConfiguration;
    }

    public void setExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration) {
        this.executionPlanConfiguration = executionPlanConfiguration;
    }

    public Map<String, StreamDefinition> getImportedStreamDefinitions() {
        return importedStreamDefinitions;
    }

    public void setImportedStreamDefinitions(
            Map<String, StreamDefinition> importedStreamDefinitions) {
        this.importedStreamDefinitions = importedStreamDefinitions;
    }

    public Map<String, StreamDefinition> getExportedStreamDefinitions() {
        return exportedStreamDefinitions;
    }

    public void setExportedStreamDefinitions(
            Map<String, StreamDefinition> exportedStreamDefinitions) {
        this.exportedStreamDefinitions = exportedStreamDefinitions;
    }

    public void shutdown() {
        siddhiManager.shutdown();
    }
}
