/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.processor.core.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorManagementService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.storm.manager.StormManagerServer;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.util.concurrent.ScheduledExecutorService;

public class EventProcessorValueHolder {
    private static CarbonEventProcessorService eventProcessorService;
    private static EventStatisticsService eventStatisticsService;
    private static EventManagementService eventManagementService;
    private static EventStreamService eventStreamService;
    private static HazelcastInstance hazelcastInstance;
    private static PersistenceStore persistenceStore;
    private static ScheduledExecutorService scheduledExecutorService;
    private static UserRealm userRealm;
    private static DataSourceService dataSourceService;
    private static ServerConfigurationService serverConfiguration;
    private static ConfigurationContextService configurationContext;
    private static StormManagerServer stormManagerServer;
    private static DistributedConfiguration stormDeploymentConfig;
    private static CarbonEventProcessorManagementService carbonEventProcessorManagementService;

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void registerSiddhiManager(SiddhiManager siddhiManager) {
        EventProcessorValueHolder.siddhiManager = siddhiManager;
    }

    private static SiddhiManager siddhiManager;


    public static void registerEventProcessorService(CarbonEventProcessorService service) {
        eventProcessorService = service;
    }

    public static CarbonEventProcessorService getEventProcessorService() {
        return eventProcessorService;
    }

    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static void registerHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static PersistenceStore getPersistenceStore() {
        return persistenceStore;
    }

    public static void setPersistenceStore(PersistenceStore persistenceStore) {
        EventProcessorValueHolder.persistenceStore = persistenceStore;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        EventProcessorValueHolder.scheduledExecutorService = scheduledExecutorService;
    }
    //
//    public static DataAccessService getDataAccessService() {
//        return dataAccessService;
//    }
//
//    public static void setDataAccessService(DataAccessService dataAccessService) {
//        EventProcessorValueHolder.dataAccessService = dataAccessService;
//    }
//
//    public static ClusterInformation getClusterInformation() {
//        return clusterInformation;
//    }
//
//    public static void setClusterInformation(ClusterInformation clusterInformation) {
//        EventProcessorValueHolder.clusterInformation = clusterInformation;
//    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public static void setUserRealm(UserRealm userRealm) {
        EventProcessorValueHolder.userRealm = userRealm;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        EventProcessorValueHolder.dataSourceService = dataSourceService;
    }


    public static EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.eventStreamService = eventStreamService;
    }

    public static ServerConfigurationService getServerConfiguration() {
        return serverConfiguration;
    }

    public static void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        EventProcessorValueHolder.serverConfiguration = serverConfiguration;
    }

    public static ConfigurationContextService getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContextService configurationContext) {
        EventProcessorValueHolder.configurationContext = configurationContext;
    }

    public static void registerStormManagerServer(StormManagerServer stormManagerServer) {
        EventProcessorValueHolder.stormManagerServer = stormManagerServer;
    }

    public static StormManagerServer getStormManagerServer() {
        return stormManagerServer;
    }

    public static void registerStormDeploymentConfig(DistributedConfiguration stormDeploymentConfig) {
        EventProcessorValueHolder.stormDeploymentConfig = stormDeploymentConfig;
    }

    public static DistributedConfiguration getStormDeploymentConfig() {
        return stormDeploymentConfig;
    }

    public static CarbonEventProcessorManagementService getCarbonEventReceiverManagementService() {
        return carbonEventProcessorManagementService;
    }

    public static void registerEventManagementService(EventManagementService eventManagementService) {
        EventProcessorValueHolder.eventManagementService = eventManagementService;
    }

    public static CarbonEventProcessorManagementService getCarbonEventProcessorManagementService() {
        return carbonEventProcessorManagementService;
    }

    public static EventManagementService getEventManagementService() {
        return eventManagementService;
    }

    public static void registerProcessorManagementService(CarbonEventProcessorManagementService eventProcessorManagementService) {
        EventProcessorValueHolder.carbonEventProcessorManagementService = eventProcessorManagementService;
    }
}
