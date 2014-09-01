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
package org.wso2.carbon.event.processor.storm.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.receive.EventReceiver;
import org.wso2.carbon.event.processor.api.send.EventProducerStreamNotificationListener;
import org.wso2.carbon.event.processor.storm.internal.CarbonStormProcessorService;
import org.wso2.carbon.event.processor.storm.internal.persistence.CassandraPersistenceStore;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.siddhi.core.persistence.PersistenceStore;

import java.util.ArrayList;
import java.util.List;

public class StormProcessorValueHolder {
    private static CarbonStormProcessorService stormProcessorService;
    private static List<EventReceiver> eventReceiverList = new ArrayList<EventReceiver>();
    private static EventProducerStreamNotificationListener eventProducerStreamNotificationListener;
    private static EventStatisticsService eventStatisticsService;
    private static EventStreamService eventStreamService;
    private static DataAccessService dataAccessService;
    private static HazelcastInstance hazelcastInstance;
    private static PersistenceStore persistenceStore;
    private static ClusterInformation clusterInformation;
    private static UserRealm userRealm;
    private static DataSourceService dataSourceService;
    private static List<PassthroughSenderConfigurator> passthroughSenderConfiguratorList = new ArrayList<PassthroughSenderConfigurator>();

    public static void registerStormProcessorService(CarbonStormProcessorService service) {
        stormProcessorService = service;
    }

    public static CarbonStormProcessorService getStormProcessorService() {
        return stormProcessorService;
    }

    public static void addEventReceiver(EventReceiver eventReceiver) {
        eventReceiverList.add(eventReceiver);
    }

    public static void removeEventReceiver(EventReceiver eventReceiver) {
        eventReceiverList.remove(eventReceiver);
    }

    public static List<EventReceiver> getEventReceiverList() {
        return eventReceiverList;
    }

    public static void registerHazelcastInstance(HazelcastInstance hazelcastInstance) {
        StormProcessorValueHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static void registerNotificationListener(EventProducerStreamNotificationListener eventProducerStreamNotificationListener) {
        StormProcessorValueHolder.eventProducerStreamNotificationListener = eventProducerStreamNotificationListener;
    }

    public static EventProducerStreamNotificationListener getNotificationListener() {
        return eventProducerStreamNotificationListener;
    }

    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        StormProcessorValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setDataAccessService(DataAccessService dataAccessService) {
        StormProcessorValueHolder.dataAccessService = dataAccessService;
    }

    public static PersistenceStore getPersistenceStore() {
        return persistenceStore;
    }

    public static void setPersistenceStore(CassandraPersistenceStore persistenceStore) {
        StormProcessorValueHolder.persistenceStore = persistenceStore;
    }

    public static ClusterInformation getClusterInformation() {
        return clusterInformation;
    }

    public static void setClusterInformation(ClusterInformation clusterInformation) {
        StormProcessorValueHolder.clusterInformation = clusterInformation;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public static void setUserRealm(UserRealm userRealm) {
        StormProcessorValueHolder.userRealm = userRealm;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        StormProcessorValueHolder.dataSourceService = dataSourceService;
    }

    public static EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public static void registerEventStreamManagerService(EventStreamService eventStreamService) {
        StormProcessorValueHolder.eventStreamService = eventStreamService;
    }

    public static void addPassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        passthroughSenderConfiguratorList.add(passthroughSenderConfigurator);
    }

    public static void removePassthroughSenderConfigurator(PassthroughSenderConfigurator passthroughSenderConfigurator) {
        passthroughSenderConfiguratorList.remove(passthroughSenderConfigurator);
    }

    public static List<PassthroughSenderConfigurator> getPassthroughSenderConfiguratorList() {
        return passthroughSenderConfiguratorList;
    }

}
