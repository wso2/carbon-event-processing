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
package org.wso2.carbon.event.formatter.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;

import java.util.List;

public interface EventFormatterService {


    /**
     * Method used to add a new event formatter configuration
     *
     * @param eventFormatterConfiguration
     * @param axisConfiguration
     * @throws EventFormatterConfigurationException
     *
     */
    public void deployEventFormatterConfiguration(
            EventFormatterConfiguration eventFormatterConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    /**
     * Method used to add a new event formatter configuration by passing in the xml configuration
     *
     * @param eventFormatterConfigXml
     * @param axisConfiguration
     * @throws EventFormatterConfigurationException
     *
     */
    public void deployEventFormatterConfiguration(
            String  eventFormatterConfigXml,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    /**
     * This method used to un-deploy the active event formatter configuration from filesystem
     *
     * @param eventFormatterName
     * @param axisConfiguration
     * @throws EventFormatterConfigurationException
     *
     */
    public void undeployActiveEventFormatterConfiguration(String eventFormatterName,
                                                          AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    /**
     * Method used to undeploy inactive event formatter configuration file from filesystem
     *
     * @param fileName
     * @param axisConfiguration
     * @throws EventFormatterConfigurationException
     *
     */
    public void undeployInactiveEventFormatterConfiguration(String fileName,
                                                            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    /**
     * Method used to get edit the inactive event formatter configuration info
     *
     * @param eventFormatterConfiguration
     * @param fileName
     * @param axisConfiguration
     * @throws EventFormatterConfigurationException
     *
     */
    public void editInactiveEventFormatterConfiguration(
            String eventFormatterConfiguration,
            String fileName,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    /**
     * Method used to edit the active event formatter configuration info
     *
     * @param eventFormatterConfiguration
     * @param eventFormatterName
     * @param axisConfiguration
     * @throws EventFormatterConfigurationException
     *
     */
    public void editActiveEventFormatterConfiguration(String eventFormatterConfiguration,
                                                      String eventFormatterName,
                                                      AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;


    /**
     * Method used to get the active  event formatter configuration as an object
     *
     * @param eventFormatterName
     * @param tenantId
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public EventFormatterConfiguration getActiveEventFormatterConfiguration(
            String eventFormatterName,
            int tenantId)
            throws EventFormatterConfigurationException;

    /**
     * This method used to get all the active event formatter configuration objects
     *
     * @param axisConfiguration
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public List<EventFormatterConfiguration> getAllActiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    public List<EventFormatterConfiguration> getAllActiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration, String streamId)
            throws EventFormatterConfigurationException;


    /**
     * This method used to get all inactive event formatter configuration file objects
     *
     * @param axisConfiguration
     * @return
     */
    public List<EventFormatterConfigurationFile> getAllInactiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration);


    /**
     * Method used to get the inactive event formatter configuration xml as a string
     *
     * @param filename
     * @param axisConfiguration
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public String getInactiveEventFormatterConfigurationContent(String filename,
                                                                AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;


    /**
     * Method used to get the active event formatter configuration xml as a string
     *
     * @param eventFormatterName
     * @param axisConfiguration
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public String getActiveEventFormatterConfigurationContent(String eventFormatterName,
                                                              AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;


    /**
     * Method used to get all the event streams
     *
     * @param axisConfiguration
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

    /**
     * Method used to get the specific stream definition for a given streamId
     *
     * @param streamNameWithVersion
     * @param axisConfiguration
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;


    /**
     * Method used to get the resource from the registry
     *
     * @param resourcePath
     * @param tenantId
     * @return
     * @throws EventFormatterConfigurationException
     *
     */
    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventFormatterConfigurationException;

    /**
     * Method used to enable/disable the statistics for an event formatter
     *
     * @param eventFormatterName the event formatter name to which statistics collecting state should be changed
     * @param axisConfiguration  the axis configuration for the calling context
     * @param flag               {@literal true} or {@literal false} specifying whether to enable statistics collection or disable
     * @throws EventFormatterConfigurationException
     *
     */
    public void setStatisticsEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventFormatterConfigurationException;

    /**
     * Method used to enable/disable the tracing for an event formatter
     *
     * @param eventFormatterName the event formatter name to which tracing state should be changed
     * @param axisConfiguration  the axis configuration for the calling context
     * @param flag               {@literal true} or {@literal false} specifying whether to enable tracing or disable
     * @throws EventFormatterConfigurationException
     *
     */
    public void setTraceEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventFormatterConfigurationException;

    /**
     * Returns the deployment status and dependency information as a formatted string for event formatter associated
     * with the filename specified
     *
     * @param filename the filename of the event formatter
     * @return a string description for the status of the event formatter specified
     */
    public String getEventFormatterStatusAsString(String filename);

    public void deployDefaultEventSender(String streamId, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException;

}
