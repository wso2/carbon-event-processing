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
package org.wso2.carbon.event.notifier.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.core.config.EventNotifierConfiguration;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.internal.util.EventNotifierConfigurationFile;

import java.util.List;

public interface EventNotifierService {


    /**
     * Method used to add a new event notifier configuration
     *
     * @param eventNotifierConfiguration
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void deployEventNotifierConfiguration(
            EventNotifierConfiguration eventNotifierConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    /**
     * Method used to add a new event notifier configuration by passing in the xml configuration
     *
     * @param eventNotifierConfigXml
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void deployEventNotifierConfiguration(
            String eventNotifierConfigXml,
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    /**
     * This method used to un-deploy the active event notifier configuration from filesystem
     *
     * @param eventNotifierName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void undeployActiveEventNotifierConfiguration(String eventNotifierName,
                                                         AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    /**
     * Method used to undeploy inactive event notifier configuration file from filesystem
     *
     * @param fileName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void undeployInactiveEventNotifierConfiguration(String fileName,
                                                           AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    /**
     * Method used to get edit the inactive event notifier configuration info
     *
     * @param eventNotifierConfiguration
     * @param fileName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void editInactiveEventNotifierConfiguration(
            String eventNotifierConfiguration,
            String fileName,
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    /**
     * Method used to edit the active event notifier configuration info
     *
     * @param eventNotifierConfiguration
     * @param eventNotifierName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void editActiveEventNotifierConfiguration(String eventNotifierConfiguration,
                                                     String eventNotifierName,
                                                     AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;


    /**
     * Method used to get the active  event notifier configuration as an object
     *
     * @param eventNotifierName
     * @param tenantId
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public EventNotifierConfiguration getActiveEventNotifierConfiguration(
            String eventNotifierName,
            int tenantId)
            throws EventNotifierConfigurationException;

    /**
     * This method used to get all the active event notifier configuration objects
     *
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public List<EventNotifierConfiguration> getAllActiveEventNotifierConfiguration(
            AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    public List<EventNotifierConfiguration> getAllActiveEventNotifierConfiguration(
            AxisConfiguration axisConfiguration, String streamId)
            throws EventNotifierConfigurationException;


    /**
     * This method used to get all inactive event notifier configuration file objects
     *
     * @param axisConfiguration
     * @return
     */
    public List<EventNotifierConfigurationFile> getAllInactiveEventNotifierConfiguration(
            AxisConfiguration axisConfiguration);


    /**
     * Method used to get the inactive event notifier configuration xml as a string
     *
     * @param filename
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public String getInactiveEventNotifierConfigurationContent(String filename,
                                                               AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;


    /**
     * Method used to get the active event notifier configuration xml as a string
     *
     * @param eventNotifierName
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public String getActiveEventNotifierConfigurationContent(String eventNotifierName,
                                                             AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;


    /**
     * Method used to get all the event streams
     *
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

    /**
     * Method used to get the specific stream definition for a given streamId
     *
     * @param streamNameWithVersion
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;


    /**
     * Method used to get the resource from the registry
     *
     * @param resourcePath
     * @param tenantId
     * @return
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventNotifierConfigurationException;

    /**
     * Method used to enable/disable the statistics for an event notifier
     *
     * @param eventNotifierName the event notifier name to which statistics collecting state should be changed
     * @param axisConfiguration the axis configuration for the calling context
     * @param flag              {@literal true} or {@literal false} specifying whether to enable statistics collection or disable
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void setStatisticsEnabled(String eventNotifierName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventNotifierConfigurationException;

    /**
     * Method used to enable/disable the tracing for an event notifier
     *
     * @param eventNotifierName the event notifier name to which tracing state should be changed
     * @param axisConfiguration the axis configuration for the calling context
     * @param flag              {@literal true} or {@literal false} specifying whether to enable tracing or disable
     * @throws org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException
     *
     */
    public void setTraceEnabled(String eventNotifierName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventNotifierConfigurationException;

    /**
     * Returns the deployment status and dependency information as a formatted string for event notifier associated
     * with the filename specified
     *
     * @param filename the filename of the event notifier
     * @return a string description for the status of the event notifier specified
     */
    public String getEventNotifierStatusAsString(String filename);

    public void deployDefaultEventSender(String streamId, AxisConfiguration axisConfiguration)
            throws EventNotifierConfigurationException;

}
