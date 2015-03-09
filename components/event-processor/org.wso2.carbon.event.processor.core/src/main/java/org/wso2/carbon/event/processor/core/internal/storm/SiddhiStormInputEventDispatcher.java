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
package org.wso2.carbon.event.processor.core.internal.storm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.common.util.AsyncEventPublisher;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.listener.AbstractSiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.siddhi.core.event.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Publishes events of a stream to the event receiver spout running on Storm. There will be SiddhiStormInputEventDispatcher
 * instance for each imported stream of execution plan
 */
public class SiddhiStormInputEventDispatcher extends AbstractSiddhiInputEventDispatcher {
    private static final Log log = LogFactory.getLog(SiddhiStormInputEventDispatcher.class);
    private final StormDeploymentConfig stormDeploymentConfig;
    private final ExecutionPlanConfiguration executionPlanConfiguration;

    private org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition;
    private String logPrefix;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AsyncEventPublisher asyncEventPublisher;

    public SiddhiStormInputEventDispatcher(StreamDefinition streamDefinition, String siddhiStreamId,
                                           ExecutionPlanConfiguration executionPlanConfiguration, int tenantId,
                                           StormDeploymentConfig stormDeploymentConfig) {
        super(streamDefinition.getStreamId(), siddhiStreamId, executionPlanConfiguration, tenantId);
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.stormDeploymentConfig = stormDeploymentConfig;
        init(streamDefinition, siddhiStreamId, executionPlanConfiguration);
    }

    private void init(StreamDefinition streamDefinition, String siddhiStreamName, ExecutionPlanConfiguration executionPlanConfiguration) {
        logPrefix = "[CEP Receiver|ExecPlan:" + executionPlanConfiguration.getName() + ", Tenant:" + tenantId + ", Stream:" + siddhiStreamName + "]";

        try {
            this.siddhiStreamDefinition = EventProcessorUtil.convertToSiddhiStreamDefinition(streamDefinition, siddhiStreamName);
            Set<org.wso2.siddhi.query.api.definition.StreamDefinition> streamDefinitions = new HashSet<org.wso2.siddhi.query.api.definition.StreamDefinition>();
            streamDefinitions.add(siddhiStreamDefinition);

            asyncEventPublisher = new AsyncEventPublisher(AsyncEventPublisher.DestinationType.STORM_RECEIVER,
                                                          streamDefinitions,
                                                          stormDeploymentConfig.getManagers().get(0).getHostName(),
                                                          stormDeploymentConfig.getManagers().get(0).getPort(),
                                                          executionPlanConfiguration.getName(),
                                                          tenantId,
                                                          stormDeploymentConfig);

            asyncEventPublisher.initializeConnection(false);
        } catch (Exception e) {
            log.error(logPrefix + "Failed to start event listener", e);
        }
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        sendEvent(event.getData());
    }

    @Override
    public void sendEvent(Object[] eventData) throws InterruptedException {
        asyncEventPublisher.sendEvent(eventData, this.siddhiStreamDefinition.getId());
    }

    @Override
    public void shutdown() {
        asyncEventPublisher.shutdown();
        executorService.shutdown();
    }
}
