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
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.persistence.CassandraPersistenceStore;
import org.wso2.carbon.event.processor.core.internal.storm.manager.StormManagerServer;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.persistence.PersistenceStore;

public class EventProcessorValueHolder {
    private static CarbonEventProcessorService eventProcessorService;
    private static EventStatisticsService eventStatisticsService;
    private static EventStreamService eventStreamService;
    private static HazelcastInstance hazelcastInstance;
    private static PersistenceStore persistenceStore;
    private static DataAccessService dataAccessService;
    private static ClusterInformation clusterInformation;
    private static UserRealm userRealm;
    private static DataSourceService dataSourceService;
    private static ServerConfigurationService serverConfiguration;
    private static ConfigurationContextService configurationContext;
    private static AbstractStreamDefinitionStore streamDefinitionStore;
    private static StormManagerServer stormManagerServer;

    public static AbstractStreamDefinitionStore getStreamDefinitionStore() {
        return streamDefinitionStore;
    }

    public static void registerStreamDefinitionStore(AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        EventProcessorValueHolder.streamDefinitionStore = abstractStreamDefinitionStore;
    }

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

    public static void setPersistenceStore(CassandraPersistenceStore persistenceStore) {
        EventProcessorValueHolder.persistenceStore = persistenceStore;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        EventProcessorValueHolder.dataAccessService = dataAccessService;
    }

    public static ClusterInformation getClusterInformation() {
        return clusterInformation;
    }

    public static void setClusterInformation(ClusterInformation clusterInformation) {
        EventProcessorValueHolder.clusterInformation = clusterInformation;
    }

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

    public static void registerEventStreamManagerService(EventStreamService eventStreamService) {
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
}
