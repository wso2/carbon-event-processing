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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorManagementService;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.listener.EventStreamListenerImpl;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.config.PersistenceConfiguration;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.siddhi.metrics.core.SiddhiMetricsFactory;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.StatisticsConfiguration;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
        name = "eventProcessorService.component",
        immediate = true)
public class EventProcessorServiceDS {

    private static final Log log = LogFactory.getLog(EventProcessorServiceDS.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            checkIsStatsEnabled();
            CarbonEventProcessorService carbonEventProcessorService = new CarbonEventProcessorService();
            EventProcessorValueHolder.registerEventProcessorService(carbonEventProcessorService);
            CarbonEventProcessorManagementService carbonEventReceiverManagementService = new
                    CarbonEventProcessorManagementService();
            EventProcessorValueHolder.registerProcessorManagementService(carbonEventReceiverManagementService);
            context.getBundleContext().registerService(EventProcessorService.class.getName(),
                    carbonEventProcessorService, null);
            context.getBundleContext().registerService(EventStreamListener.class.getName(), new
                    EventStreamListenerImpl(), null);
            SiddhiManager siddhiManager = new SiddhiManager();
            EventProcessorValueHolder.registerSiddhiManager(siddhiManager);
            PersistenceConfiguration persistConfig = carbonEventProcessorService.getManagementInfo()
                    .getPersistenceConfiguration();
            if (persistConfig != null) {
                Class clazz = Class.forName(persistConfig.getPersistenceClass());
                PersistenceStore persistenceStore = (PersistenceStore) clazz.newInstance();
                siddhiManager.setPersistenceStore(persistenceStore);
                persistenceStore.setProperties(persistConfig.getPropertiesMap());
                EventProcessorValueHolder.registerPersistenceConfiguration(persistConfig);
            }
            StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory
                    (EventProcessorValueHolder.isGlobalStatisticsEnabled()));
            statisticsConfiguration.setMatricPrefix(EventProcessorConstants.METRIC_PREFIX);
            siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorService");
            }
        } catch (Throwable e) {
            log.error("Could not create EventProcessorService: " + e.getMessage(), e);
        }
    }

    protected void checkIsStatsEnabled() {

        ServerConfiguration config = ServerConfiguration.getInstance();
        String confStatisticsReporterDisabled = config.getFirstProperty("StatisticsReporterDisabled");
        if (!"".equals(confStatisticsReporterDisabled)) {
            boolean disabled = Boolean.valueOf(confStatisticsReporterDisabled);
            if (disabled) {
                return;
            }
        }
        EventProcessorValueHolder.setGlobalStatisticsEnabled(true);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        EventProcessorValueHolder.getEventProcessorService().shutdown();
    }

    @Reference(
            name = "eventStreamManager.service",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {

        EventProcessorValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        EventProcessorValueHolder.registerEventStreamService(null);
    }

    @Reference(
            name = "user.realm.delegating",
            service = org.wso2.carbon.user.core.UserRealm.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserRealm")
    protected void setUserRealm(UserRealm userRealm) {

        EventProcessorValueHolder.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm userRealm) {

        EventProcessorValueHolder.setUserRealm(null);
    }

    @Reference(
            name = "org.wso2.carbon.ndatasource",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {

        EventProcessorValueHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {

        EventProcessorValueHolder.setDataSourceService(null);
    }

    @Reference(
            name = "server.configuration",
            service = org.wso2.carbon.base.api.ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfiguration")
    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {

        EventProcessorValueHolder.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {

        EventProcessorValueHolder.setServerConfiguration(null);
    }

    @Reference(
            name = "configuration.context",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContext")
    protected void setConfigurationContext(ConfigurationContextService configurationContext) {

        EventProcessorValueHolder.setConfigurationContext(configurationContext);
    }

    protected void unsetConfigurationContext(ConfigurationContextService configurationContext) {

        EventProcessorValueHolder.setConfigurationContext(null);
    }

    @Reference(
            name = "eventManagement.service",
            service = org.wso2.carbon.event.processor.manager.core.EventManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventManagementService")
    protected void setEventManagementService(EventManagementService eventManagementService) {

        EventProcessorValueHolder.registerEventManagementService(eventManagementService);
    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {

        EventProcessorValueHolder.registerEventManagementService(null);
    }
}
