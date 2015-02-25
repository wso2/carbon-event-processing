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
package org.wso2.carbon.event.publisher.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfigurationFile;

import java.util.List;

public interface EventPublisherService {


    /**
     * Method used to add a new event publisher configuration
     *
     * @param eventPublisherConfiguration
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void deployEventPublisherConfiguration(
            EventPublisherConfiguration eventPublisherConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * Method used to add a new event publisher configuration by passing in the xml configuration
     *
     * @param eventPublisherConfigXml
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void deployEventPublisherConfiguration(
            String eventPublisherConfigXml,
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * This method used to un-deploy the active event publisher configuration from filesystem
     *
     * @param eventPublisherName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void undeployActiveEventPublisherConfiguration(String eventPublisherName,
                                                         AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * Method used to undeploy inactive event publisher configuration file from filesystem
     *
     * @param fileName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void undeployInactiveEventPublisherConfiguration(String fileName,
                                                           AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * Method used to get edit the inactive event publisher configuration info
     *
     * @param eventPublisherConfiguration
     * @param fileName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void editInactiveEventPublisherConfiguration(
            String eventPublisherConfiguration,
            String fileName,
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * Method used to edit the active event publisher configuration info
     *
     * @param eventPublisherConfiguration
     * @param eventPublisherName
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void editActiveEventPublisherConfiguration(String eventPublisherConfiguration,
                                                     String eventPublisherName,
                                                     AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get the active  event publisher configuration as an object
     *
     * @param eventPublisherName
     * @param tenantId
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public EventPublisherConfiguration getActiveEventPublisherConfiguration(
            String eventPublisherName,
            int tenantId)
            throws EventPublisherConfigurationException;

    /**
     * This method used to get all the active event publisher configuration objects
     *
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public List<EventPublisherConfiguration> getAllActiveEventPublisherConfiguration(
            AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    public List<EventPublisherConfiguration> getAllActiveEventPublisherConfiguration(
            AxisConfiguration axisConfiguration, String streamId)
            throws EventPublisherConfigurationException;


    /**
     * This method used to get all inactive event publisher configuration file objects
     *
     * @param axisConfiguration
     * @return
     */
    public List<EventPublisherConfigurationFile> getAllInactiveEventPublisherConfiguration(
            AxisConfiguration axisConfiguration);


    /**
     * Method used to get the inactive event publisher configuration xml as a string
     *
     * @param filename
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public String getInactiveEventPublisherConfigurationContent(String filename,
                                                               AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get the active event publisher configuration xml as a string
     *
     * @param eventPublisherName
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public String getActiveEventPublisherConfigurationContent(String eventPublisherName,
                                                             AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get all the event streams
     *
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * Method used to get the specific stream definition for a given streamId
     *
     * @param streamNameWithVersion
     * @param axisConfiguration
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get the resource from the registry
     *
     * @param resourcePath
     * @param tenantId
     * @return
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventPublisherConfigurationException;

    /**
     * Method used to enable/disable the statistics for an event publisher
     *
     * @param eventPublisherName the event publisher name to which statistics collecting state should be changed
     * @param axisConfiguration the axis configuration for the calling context
     * @param flag              {@literal true} or {@literal false} specifying whether to enable statistics collection or disable
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void setStatisticsEnabled(String eventPublisherName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventPublisherConfigurationException;

    /**
     * Method used to enable/disable the tracing for an event publisher
     *
     * @param eventPublisherName the event publisher name to which tracing state should be changed
     * @param axisConfiguration the axis configuration for the calling context
     * @param flag              {@literal true} or {@literal false} specifying whether to enable tracing or disable
     * @throws org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException
     *
     */
    public void setTraceEnabled(String eventPublisherName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventPublisherConfigurationException;

    /**
     * Returns the deployment status and dependency information as a formatted string for event publisher associated
     * with the filename specified
     *
     * @param filename the filename of the event publisher
     * @return a string description for the status of the event publisher specified
     */
    public String getEventPublisherStatusAsString(String filename);

    public void deployDefaultEventSender(String streamId, AxisConfiguration axisConfiguration)
            throws EventPublisherConfigurationException;

}
