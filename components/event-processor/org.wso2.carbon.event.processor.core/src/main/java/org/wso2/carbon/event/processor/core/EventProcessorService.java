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
package org.wso2.carbon.event.processor.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.List;
import java.util.Map;

public interface EventProcessorService {


    /**
     * Adds a new execution plan to the system using the execution plan given in string format.
     *
     * @param executionPlan new execution plan as a string
     */
    public void deployExecutionPlan(String executionPlan)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException;

    /**
     * Removes execution plan from the system
     *
     * @param fileName name of the file which contains the execution plan
     */
    public void undeployInactiveExecutionPlan(String fileName) throws
            ExecutionPlanConfigurationException;

    /**
     * Removes execution plan from the system
     *
     * @param planName name of the execution plan
     */
    public void undeployActiveExecutionPlan(String planName) throws
            ExecutionPlanConfigurationException;

    /**
     * Edits execution plan from the system
     *
     * @param executionPlan     the execution plan configuration as an XML string
     * @param executionPlanName the name of the execution plan
     */
    public void editActiveExecutionPlan(String executionPlan,
                                        String executionPlanName)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException;


    /**
     * Edits execution plan from the system
     *
     * @param executionPlan the execution plan configuration as an XML string
     * @param fileName      filename of the configuration for this execution plan
     */
    public void editInactiveExecutionPlan(String executionPlan,
                                          String fileName)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException;

    /**
     * Returns the content of the execution plan identified by the name as an XML string
     *
     * @param name the name of the execution plan
     * @return the content of the exeuction plan configuration
     * @throws ExecutionPlanConfigurationException
     */
    public String getActiveExecutionPlan(String name)
            throws ExecutionPlanConfigurationException;

    /**
     * Returns the file content for the filename specified as a string
     *
     * @param filename filename of the configuration for this execution plan
     * @return the content of the specified filename
     * @throws ExecutionPlanConfigurationException
     */
    public String getInactiveExecutionPlan(String filename)
            throws ExecutionPlanConfigurationException;


    /**
     * Gets all available active execution plan configurations.
     *
     * @param tenantId tenant id of the caller
     * @return a {@link Map} of execution plan name and {@link ExecutionPlanConfiguration} object of active execution plans.
     */
    public Map<String, ExecutionPlanConfiguration> getAllActiveExecutionConfigurations(int tenantId);

    /**
     * @param tenantId tenant id of the caller
     * @param streamId
     * @return
     */
    public Map<String, ExecutionPlanConfiguration> getAllExportedStreamSpecificActiveExecutionConfigurations(int tenantId, String streamId);

    /**
     * @param tenantId tenant id of the caller
     * @param streamId
     * @return
     */
    public Map<String, ExecutionPlanConfiguration> getAllImportedStreamSpecificActiveExecutionConfigurations(int tenantId, String streamId);


    /**
     * Gets an active execution plan configuration for the name passed in.
     *
     * @param planName the name of the execution plan
     * @param tenantId tenant id of the caller
     * @return {@link ExecutionPlanConfiguration} object associated with the passed in name and tenant id
     */
    public ExecutionPlanConfiguration getActiveExecutionPlanConfiguration(String planName, int tenantId);


    /**
     * Gets all available inactive execution plan configurations files.
     *
     * @param tenantId tenant id of the caller
     * @return A {@link List} of {@link ExecutionPlanConfigurationFile} objects for all the inactive execution plans
     * for the specified tenant id
     */
    public List<ExecutionPlanConfigurationFile> getAllInactiveExecutionPlanConfiguration(
            int tenantId);

    /**
     * @param executionPlanName the name of the execution plan
     * @param isEnabled         whether tracing is enabled or not
     * @throws ExecutionPlanConfigurationException
     */
    public void setTracingEnabled(String executionPlanName, boolean isEnabled)
            throws ExecutionPlanConfigurationException;

    /**
     * @param executionPlanName the name of the execution plan
     * @param isEnabled         whether statistics is enabled or not
     * @throws ExecutionPlanConfigurationException
     */
    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled)
            throws ExecutionPlanConfigurationException;


    /**
     * Validates a given execution plan. returns true if valid.
     *
     * @param executionPlan execution plan.
     * @return true if valid.
     */
    public void validateExecutionPlan(String executionPlan)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException;

    /**
     * Fetches all the streams imported and exported by the Siddhi engine for the given execution plan.
     *
     * @param executionPlan siddhi queries.
     * @return a {@link List} of {@link StreamDefinition} objects that represent all the streams imported and exported by Siddhi queries
     * @throws SiddhiParserException
     */
    public List<StreamDefinition> getSiddhiStreams(String executionPlan);
}




