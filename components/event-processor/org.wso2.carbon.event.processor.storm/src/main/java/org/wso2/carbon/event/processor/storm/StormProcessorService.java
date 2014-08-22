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

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.storm.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.storm.exception.ExecutionPlanDependencyValidationException;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import java.util.List;
import java.util.Map;

public interface StormProcessorService {

    /**
     * Adds a new execution plan to the system.
     *
     * @param executionPlanConfiguration new execution plan configuration.
     */
    public void deployExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration,
            AxisConfiguration axisConfiguration)
            throws ExecutionPlanDependencyValidationException, ExecutionPlanConfigurationException;

    /**
     * Removes execution plan from the system
     *
     * @param fileName
     * @param axisConfiguration
     */
    public void undeployInactiveExecutionPlanConfiguration(String fileName,
                                                           AxisConfiguration axisConfiguration) throws
            ExecutionPlanConfigurationException;

    /**
     * Removes execution plan from the system
     *
     * @param name
     * @param axisConfiguration
     */
    public void undeployActiveExecutionPlanConfiguration(String name,
                                                         AxisConfiguration axisConfiguration) throws
            ExecutionPlanConfigurationException;

    /**
     * Edits execution plan from the system
     *
     * @param executionPlanConfiguration
     * @param executionPlanName
     * @param axisConfiguration
     */
    public void editActiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                     String executionPlanName,
                                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    /**
     * Edits execution plan from the system
     *
     * @param executionPlanConfiguration
     * @param fileName
     * @param axisConfiguration
     */
    public void editInactiveExecutionPlanConfiguration(String executionPlanConfiguration,
                                                       String fileName,
                                                       AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    /**
     * @param name
     * @param axisConfiguration
     * @return
     * @throws ExecutionPlanConfigurationException
     *
     */
    public String getActiveExecutionPlanConfigurationContent(String name, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    /**
     * @param filename
     * @param axisConfiguration
     * @return
     * @throws ExecutionPlanConfigurationException
     *
     */
    public String getInactiveExecutionPlanConfigurationContent(String filename, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    /**
     * Gets all available active execution plan configurations.
     *
     * @return
     */
    public Map<String, ExecutionPlanConfiguration> getAllActiveExecutionConfigurations(int tenantId);

    /**
     * Gets a active execution plan configurations.
     *
     * @return
     */
    public ExecutionPlanConfiguration getActiveExecutionConfiguration(String name, int tenantId);

    /**
     * Gets all available inactive execution plan configurations files.
     *
     * @return
     */
    public List<ExecutionPlanConfigurationFile> getAllInactiveExecutionPlanConfiguration(
            int tenantId);

    /**
     * @param executionPlanName
     * @param isEnabled
     * @param axisConfiguration
     * @throws ExecutionPlanConfigurationException
     *
     */
    public void setTracingEnabled(String executionPlanName, boolean isEnabled,
                                  AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    /**
     * @param executionPlanName
     * @param isEnabled
     * @param axisConfiguration
     * @throws ExecutionPlanConfigurationException
     *
     */
    public void setStatisticsEnabled(String executionPlanName, boolean isEnabled,
                                     AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException;

    /**
     * Validates a given set of siddhi query expressions. returns true if valid.
     *
     * @param inputStreamDefinitions input streams required by queries.
     * @param queryExpressions       siddhi queries.
     * @return true if valid.
     * @throws org.wso2.siddhi.query.compiler.exception.SiddhiParserException
     *
     */
    public boolean validateSiddhiQueries(String[] inputStreamDefinitions, String queryExpressions) throws
            SiddhiParserException;

    /**
     * Fetches all the streams imported and exported by the Siddhi engine for the given set of queries.
     *
     * @param inputStreamDefinitions input streams required by queries
     * @param queryExpressions       siddhi queries.
     * @return a {@link List} of {@link org.wso2.carbon.databridge.commons.StreamDefinition} objects that represent all the streams imported and exported by Siddhi queries
     * @throws SiddhiParserException
     */
    public List<StreamDefinition> getSiddhiStreams(String[] inputStreamDefinitions, String queryExpressions) throws
            SiddhiParserException;

    /**
     * Returns the deployment status and dependency information as a formatted string for execution plan associated
     * with the filename specified
     *
     * @param filename the filename of the execution plan
     * @return a string description for the status of the execution plan specified
     */
    public String getExecutionPlanStatusAsString(String filename);

}
