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
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorManagementService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.listener.EventStreamListenerImpl;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.internal.storm.manager.StormManagerServer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.PersistenceConfiguration;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.siddhi.metrics.core.SiddhiMetricsFactory;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.StatisticsConfiguration;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

/**
 * @scr.component name="eventProcessorService.component" immediate="true"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="eventManagement.service"
 * interface="org.wso2.carbon.event.processor.manager.core.EventManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventManagementService" unbind="unsetEventManagementService"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 * @scr.reference name="user.realm.delegating" interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1" policy="dynamic" bind="setUserRealm" unbind="unsetUserRealm"
 * @scr.reference name="org.wso2.carbon.ndatasource" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic"  bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="configuration.context"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1" policy="dynamic"  bind="setConfigurationContext" unbind="unsetConfigurationContext"
 */
public class EventProcessorServiceDS {
    private static final Log log = LogFactory.getLog(EventProcessorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventProcessorService carbonEventProcessorService = new CarbonEventProcessorService();
            EventProcessorValueHolder.registerEventProcessorService(carbonEventProcessorService);

            CarbonEventProcessorManagementService carbonEventReceiverManagementService = new CarbonEventProcessorManagementService();
            EventProcessorValueHolder.registerProcessorManagementService(carbonEventReceiverManagementService);

            DistributedConfiguration stormDeploymentConfig = carbonEventProcessorService.getManagementInfo().getDistributedConfiguration();
            if (stormDeploymentConfig != null) {
                EventProcessorValueHolder.registerStormDeploymentConfiguration(stormDeploymentConfig);
                EventProcessorValueHolder.registerStormTopologyManager(new StormTopologyManager());
                if (stormDeploymentConfig.isManagerNode()) {
                    StormManagerServer stormManagerServer = new StormManagerServer(stormDeploymentConfig.getLocalManagerConfig().getHostName(),
                            stormDeploymentConfig.getLocalManagerConfig().getPort());
                    EventProcessorValueHolder.registerStormManagerServer(stormManagerServer);
                }
            }

            context.getBundleContext().registerService(EventProcessorService.class.getName(), carbonEventProcessorService, null);
            context.getBundleContext().registerService(EventStreamListener.class.getName(), new EventStreamListenerImpl(), null);

            SiddhiManager siddhiManager = new SiddhiManager();
            EventProcessorValueHolder.registerSiddhiManager(siddhiManager);

            PersistenceConfiguration persistConfig = carbonEventProcessorService.getManagementInfo().getPersistenceConfiguration();
            if(persistConfig != null) {
                Class clazz = Class.forName(persistConfig.getPersistenceClass());
                PersistenceStore persistenceStore = (PersistenceStore) clazz.newInstance();
                siddhiManager.setPersistenceStore(persistenceStore);
                persistenceStore.setProperties(persistConfig.getPropertiesMap());
                EventProcessorValueHolder.registerPersistenceConfiguration(persistConfig);
            }

            StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
            statisticsConfiguration.setMatricPrefix(EventProcessorConstants.METRIC_PREFIX);
            siddhiManager.setStatisticsConfiguration(statisticsConfiguration);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorService");
            }

        } catch (Throwable e) {
            log.error("Could not create EventProcessorService: " + e.getMessage(), e);
        }

    }

    protected void deactivate(ComponentContext context) {

        try {
            StormManagerServer stormManagerServer = EventProcessorValueHolder.getStormManagerServer();
            if (stormManagerServer != null) {
                stormManagerServer.stop();
            }
        } catch (RuntimeException e) {
            log.error("Error in stopping Storm Manager Service : " + e.getMessage(), e);
        }
        EventProcessorValueHolder.getEventProcessorService().shutdown();
    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(null);
    }


    protected void setEventStreamService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.registerEventStreamService(null);
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.registerHazelcastInstance(hazelcastInstance);

        StormManagerServer stormManagerServer = EventProcessorValueHolder.getStormManagerServer();
        if (stormManagerServer != null) {
            stormManagerServer.setHzaelCastInstance(hazelcastInstance);
            stormManagerServer.tryBecomeCoordinator();
        }

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                StormManagerServer stormManagerServer = EventProcessorValueHolder.getStormManagerServer();
                if (stormManagerServer != null) {
                    stormManagerServer.verifyState();
                }
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                StormManagerServer stormManagerServer = EventProcessorValueHolder.getStormManagerServer();
                if (stormManagerServer != null) {
                    stormManagerServer.tryBecomeCoordinator();
                }
            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }

        });

        EventProcessorValueHolder.getEventProcessorService().notifyServiceAvailability(EventProcessorConstants.HAZELCAST_INSTANCE);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.registerHazelcastInstance(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        EventProcessorValueHolder.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm userRealm) {
        EventProcessorValueHolder.setUserRealm(null);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        EventProcessorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        EventProcessorValueHolder.setDataSourceService(null);
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        EventProcessorValueHolder.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        EventProcessorValueHolder.setServerConfiguration(null);
    }

    protected void setConfigurationContext(ConfigurationContextService configurationContext) {
        EventProcessorValueHolder.setConfigurationContext(configurationContext);
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {
        EventProcessorValueHolder.setConfigurationContext(null);
    }

    protected void setEventManagementService(EventManagementService eventManagementService) {
        EventProcessorValueHolder.registerEventManagementService(eventManagementService);

    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {
        EventProcessorValueHolder.registerEventManagementService(null);
    }
}
