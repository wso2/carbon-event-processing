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

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
//import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfigReader;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ha.server.HAManagementServer;
import org.wso2.carbon.event.processor.core.internal.listener.EventStreamListenerImpl;
import org.wso2.carbon.event.processor.core.internal.storm.manager.StormManagerServer;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.SiddhiManager;

import java.io.File;

/**
 * @scr.component name="eventProcessorService.component" immediate="true"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 * @scr.reference name="stream.definitionStore.service"
 * interface="org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamStoreService" unbind="unsetEventStreamStoreService"
 * @scr.reference name="eventStreamManager.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamManagerService" unbind="unsetEventStreamManagerService"
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

            new HAManagementServer(carbonEventProcessorService);

            String stormConfigDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "cep" + File.separator + "storm";
            StormDeploymentConfig stormDeploymentConfig = StormDeploymentConfigReader.loadConfigurations(stormConfigDirPath);
            if (stormDeploymentConfig != null) {
                EventProcessorValueHolder.registerStormDeploymentConfig(stormDeploymentConfig);
                if (stormDeploymentConfig.isManagerNode()) {
                    StormManagerServer stormManagerServer = new StormManagerServer(stormDeploymentConfig.getLocalManagerConfig().getHostName(), stormDeploymentConfig.getLocalManagerConfig().getPort());
                    EventProcessorValueHolder.registerStormManagerServer(stormManagerServer);
                }
            }


            context.getBundleContext().registerService(EventProcessorService.class.getName(), carbonEventProcessorService, null);
            EventProcessorValueHolder.getEventStreamService().registerEventStreamListener(new EventStreamListenerImpl());

            SiddhiManager siddhiManager = new SiddhiManager();
            EventProcessorValueHolder.registerSiddhiManager(siddhiManager);
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

    }

    protected void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    protected void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventProcessorValueHolder.registerEventStatisticsService(null);
    }

    protected void setEventStreamStoreService(AbstractStreamDefinitionStore streamDefinitionStore) {
        EventProcessorValueHolder.registerStreamDefinitionStore(streamDefinitionStore);
    }

    protected void unsetEventStreamStoreService(AbstractStreamDefinitionStore streamDefinitionStore) {
        EventProcessorValueHolder.registerStreamDefinitionStore(null);
    }

    protected void setEventStreamManagerService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.registerEventStreamManagerService(eventStreamService);
    }

    protected void unsetEventStreamManagerService(EventStreamService eventStreamService) {
        EventProcessorValueHolder.registerEventStreamManagerService(null);
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventProcessorValueHolder.registerHazelcastInstance(hazelcastInstance);

        StormManagerServer stormManagerServer = EventProcessorValueHolder.getStormManagerServer();
        if (stormManagerServer != null) {
            stormManagerServer.tryBecomeCoordinator();
        }

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

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
}
