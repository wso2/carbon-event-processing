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
package org.wso2.carbon.event.receiver.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.util.List;

public interface EventReceiverService {

    /**
     * Updates the event receiver with the given syntax
     *
     * @param eventReceiverConfigXml the XML configuration of the event receiver as a string
     * @param axisConfiguration     the axis configuration of the particular tenant to which this event receiver belongs
     */
    public void editInactiveEventReceiverConfiguration(String eventReceiverConfigXml,
                                                      String filename,
                                                      AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Updates the event receiver according to the passed in {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration}
     *
     * @param originalEventReceiverName the original name of the event receiver
     * @param axisConfiguration        the axis configuration of the tenant which owns the event receiver
     */
    public void editActiveEventReceiverConfiguration(String eventReceiverConfigXml,
                                                    String originalEventReceiverName,
                                                    AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Getting all the event receiver configuration instance details.
     *
     * @param tenantId tenant id of caller
     * @return - list of available event configuration
     */
    public List<EventReceiverConfiguration> getAllActiveEventReceiverConfigurations(int tenantId);

    /**
     * Getting all event receiver configurations specific for a stream
     *
     * @param streamId stream id for which event receiver configurations are needed
     * @param tenantId tenant id of caller
     * @return  the event receiver configuration
     */
    public List<EventReceiverConfiguration> getAllStreamSpecificActiveEventReceiverConfigurations(
            String streamId, int tenantId);

    /**
     * Returns the {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration} for the event receiver with given name
     *
     * @param eventReceiverName the event receiver name
     * @param tenantId         the tenant id
     * @return {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration} that is associated with the event receiver of the name passed in
     */
    public EventReceiverConfiguration getActiveEventReceiverConfiguration(String eventReceiverName,
                                                                        int tenantId);




//
//
//    /**
//     * Returns a list of supported mapping types
//     *
//     * @param eventAdaptorName the event adaptor name
//     * @param tenantId         the tenant id to which this event adaptor belongs to
//     * @return a list of strings that represent supported mappings by the EventReceiverService
//     */
//    @Deprecated
//    public List<String> getSupportedInputMappingTypes(String eventAdaptorName, int tenantId);





    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of EventReceiverConfigurationFile
     */
    public List<EventReceiverConfigurationFile> getAllInactiveEventReceiverConfigurations(
            AxisConfiguration axisConfiguration);

    /**
     * Returns the event receiver XML configuration for the given event receiver name and tenant id
     *
     * @param eventReceiverName  the name of the event receiver
     * @param axisConfiguration the axis configuration of the caller
     * @return the XML configuration syntax as a string
     */
    public String getActiveEventReceiverConfigurationContent(String eventReceiverName,
                                                            AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Returns the event receiver XML configuration for the given filePath and tenant id
     *
     * @return the XML configuration syntax as a string
     */
    public String getInactiveEventReceiverConfigurationContent(String filename,
                                                              AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Undeploys an active event receiver configuration of the given name for the axis configuration
     * and deletes it from the file system.
     *
     * @param eventReceiverName  the event receiver name
     * @param axisConfiguration the axis configuration
     */
    public void undeployActiveEventReceiverConfiguration(String eventReceiverName,
                                                        AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Removes the event receiver configuration file from the file system and memory
     *
     * @param filename          the name of the event receiver configuration file
     * @param axisConfiguration the tenant id of the tenant which owns this event receiver
     */
    public void undeployInactiveEventReceiverConfiguration(String filename,
                                                          AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Deploys an event receiver configuration and saves the associated configuration file to the filesystem.
     *
     * @param eventReceiverConfiguration the {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration} object
     * @param axisConfiguration         the axis configuration
     */
    public void deployEventReceiverConfiguration(
            EventReceiverConfiguration eventReceiverConfiguration,
            AxisConfiguration axisConfiguration) throws EventReceiverConfigurationException;

    /**
     * Deploys an event receiver configuration and saves the associated configuration file to the filesystem.
     *
     * @param eventReceiverConfiguration the {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration} object
     */
    public void deployEventReceiverConfiguration(
            EventReceiverConfiguration eventReceiverConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Enable or disable tracing for the event receiver of given name
     *
     * @param eventReceiverName  event receiver name
     * @param traceEnabled      {@code true} or {@code false} specifying whether trace is enabled or not
     * @param axisConfiguration axis configuration
     */
    public void setTraceEnabled(String eventReceiverName, boolean traceEnabled,
                                AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Enable or disable statistics for the event receiver of given name
     *
     * @param eventReceiverName  event receiver name
     * @param statisticsEnabled {@code true} or {@code false} specifying whether statistics is enabled or not
     * @param axisConfiguration axis configuration
     */
    public void setStatisticsEnabled(String eventReceiverName, boolean statisticsEnabled,
                                     AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    /**
     * Returns the deployment status and dependency information as a formatted string for event receiver associated
     * with the filename specified
     *
     * @param filename the filename of the event receiver
     * @return a string description for the status of the event receiver specified
     */
    @Deprecated
    public String getEventReceiverStatusAsString(String filename);

    /**
     * Creates a default event receiver for given stream and deploys
     *
     * @param streamId the stream id for which default event receiver should be created
     * @param axisConfiguration the axis configuration of the caller
     * @throws org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException
     */
    public void deployDefaultEventReceiver(String streamId,
                                          AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException;

    public void deployEventReceiverConfiguration(String eventReceiverConfigXml, AxisConfiguration axisConfiguration) throws EventReceiverConfigurationException;
}
