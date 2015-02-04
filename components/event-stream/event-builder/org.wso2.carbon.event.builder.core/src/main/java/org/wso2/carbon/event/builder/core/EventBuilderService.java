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
package org.wso2.carbon.event.builder.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfigurationFile;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;

import java.util.List;

public interface EventBuilderService {

    /**
     * Updates the event builder with the given syntax
     *
     * @param eventBuilderConfigXml the XML configuration of the event builder as a string
     * @param axisConfiguration     the axis configuration of the particular tenant to which this event builder belongs
     */
    public void editInactiveEventBuilderConfiguration(String eventBuilderConfigXml,
                                                      String filename,
                                                      AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Updates the event builder according to the passed in {@link EventBuilderConfiguration}
     *
     * @param originalEventBuilderName the original name of the event builder
     * @param axisConfiguration        the axis configuration of the tenant which owns the event builder
     */
    public void editActiveEventBuilderConfiguration(String eventBuilderConfigXml,
                                                    String originalEventBuilderName,
                                                    AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Getting all the event builder configuration instance details.
     *
     * @param tenantId tenant id of caller
     * @return - list of available event configuration
     */
    public List<EventBuilderConfiguration> getAllActiveEventBuilderConfigurations(int tenantId);

    /**
     * Getting all event builder configurations specific for a stream
     *
     * @param streamId stream id for which event builder configurations are needed
     * @param tenantId tenant id of caller
     * @return  the event builder configuration
     */
    public List<EventBuilderConfiguration> getAllStreamSpecificActiveEventBuilderConfigurations(
            String streamId, int tenantId);

    /**
     * Returns the {@link EventBuilderConfiguration} for the event builder with given name
     *
     * @param eventBuilderName the event builder name
     * @param tenantId         the tenant id
     * @return {@link EventBuilderConfiguration} that is associated with the event builder of the name passed in
     */
    public EventBuilderConfiguration getActiveEventBuilderConfiguration(String eventBuilderName,
                                                                        int tenantId);

    /**
     * Returns a list of supported mapping types
     *
     * @param eventAdaptorName the event adaptor name
     * @param tenantId         the tenant id to which this event adaptor belongs to
     * @return a list of strings that represent supported mappings by the EventBuilderService
     */
    @Deprecated
    public List<String> getSupportedInputMappingTypes(String eventAdaptorName, int tenantId);

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of EventBuilderConfigurationFile
     */
    public List<EventBuilderConfigurationFile> getAllInactiveEventBuilderConfigurations(
            AxisConfiguration axisConfiguration);

    /**
     * Returns the event builder XML configuration for the given event builder name and tenant id
     *
     * @param eventBuilderName  the name of the event builder
     * @param axisConfiguration the axis configuration of the caller
     * @return the XML configuration syntax as a string
     */
    public String getActiveEventBuilderConfigurationContent(String eventBuilderName,
                                                            AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Returns the event builder XML configuration for the given filePath and tenant id
     *
     * @return the XML configuration syntax as a string
     */
    public String getInactiveEventBuilderConfigurationContent(String filename,
                                                              AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Undeploys an active event builder configuration of the given name for the axis configuration
     * and deletes it from the file system.
     *
     * @param eventBuilderName  the event builder name
     * @param axisConfiguration the axis configuration
     */
    public void undeployActiveEventBuilderConfiguration(String eventBuilderName,
                                                        AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Removes the event builder configuration file from the file system and memory
     *
     * @param filename          the name of the event builder configuration file
     * @param axisConfiguration the tenant id of the tenant which owns this event builder
     */
    public void undeployInactiveEventBuilderConfiguration(String filename,
                                                          AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Deploys an event builder configuration and saves the associated configuration file to the filesystem.
     *
     * @param eventBuilderConfiguration the {@link EventBuilderConfiguration} object
     * @param axisConfiguration         the axis configuration
     */
    public void deployEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration,
            AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;

    /**
     * Deploys an event builder configuration and saves the associated configuration file to the filesystem.
     *
     * @param eventBuilderConfiguration the {@link EventBuilderConfiguration} object
     */
    public void deployEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Enable or disable tracing for the event builder of given name
     *
     * @param eventBuilderName  event builder name
     * @param traceEnabled      {@code true} or {@code false} specifying whether trace is enabled or not
     * @param axisConfiguration axis configuration
     */
    public void setTraceEnabled(String eventBuilderName, boolean traceEnabled,
                                AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Enable or disable statistics for the event builder of given name
     *
     * @param eventBuilderName  event builder name
     * @param statisticsEnabled {@code true} or {@code false} specifying whether statistics is enabled or not
     * @param axisConfiguration axis configuration
     */
    public void setStatisticsEnabled(String eventBuilderName, boolean statisticsEnabled,
                                     AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    /**
     * Returns the deployment status and dependency information as a formatted string for event builder associated
     * with the filename specified
     *
     * @param filename the filename of the event builder
     * @return a string description for the status of the event builder specified
     */
    @Deprecated
    public String getEventBuilderStatusAsString(String filename);

    /**
     * Creates a default event builder for given stream and deploys
     *
     * @param streamId the stream id for which default event builder should be created
     * @param axisConfiguration the axis configuration of the caller
     * @throws EventBuilderConfigurationException
     */
    public void deployDefaultEventBuilder(String streamId,
                                          AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException;

    public void deployEventBuilderConfiguration(String eventBuilderConfigXml, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException;
}
