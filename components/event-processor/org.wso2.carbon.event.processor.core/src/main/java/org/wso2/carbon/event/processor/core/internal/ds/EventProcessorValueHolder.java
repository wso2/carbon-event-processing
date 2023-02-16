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
package org.wso2.carbon.event.processor.core.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorManagementService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.config.PersistenceConfiguration;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.concurrent.ConcurrentHashMap;

public class EventProcessorValueHolder {
    private static CarbonEventProcessorService eventProcessorService;
    private static EventManagementService eventManagementService;
    private static EventStreamService eventStreamService;
    private static HazelcastInstance hazelcastInstance;
    private static UserRealm userRealm;
    private static DataSourceService dataSourceService;
    private static ServerConfigurationService serverConfiguration;
    private static ConfigurationContextService configurationContext;
    private static PersistenceConfiguration persistenceConfiguration;
    private static CarbonEventProcessorManagementService carbonEventProcessorManagementService;
    private static SiddhiManager siddhiManager;
    private static ConcurrentHashMap<Integer, ConfigurationContext> tenantConfigs = new ConcurrentHashMap<>();
    private static boolean globalStatisticsEnabled;

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void registerSiddhiManager(SiddhiManager siddhiManager) {
        EventProcessorValueHolder.siddhiManager = siddhiManager;
    }

    public static void registerEventProcessorService(CarbonEventProcessorService service) {
        eventProcessorService = service;
    }

    public static CarbonEventProcessorService getEventProcessorService() {
        return eventProcessorService;
    }

    public static void registerHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

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


    public static void registerPersistenceConfiguration(PersistenceConfiguration persistenceConfiguration) {
        EventProcessorValueHolder.persistenceConfiguration = persistenceConfiguration;
    }

    public static PersistenceConfiguration getPersistenceConfiguration() {
        return persistenceConfiguration;
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

    public static void addTenantConfig(int tenantId, ConfigurationContext configurationContext){
        tenantConfigs.putIfAbsent(tenantId, configurationContext);
    }

    public static ConfigurationContext getTenantConfig(int tenantId){
        return tenantConfigs.get(tenantId);
    }

    public static boolean isGlobalStatisticsEnabled() {
        return globalStatisticsEnabled;
    }

    public static void setGlobalStatisticsEnabled(boolean globalStatisticsEnabled) {
        EventProcessorValueHolder.globalStatisticsEnabled = globalStatisticsEnabled;
    }
}
