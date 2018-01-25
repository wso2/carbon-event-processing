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
package org.wso2.carbon.event.processor.core.internal.storm;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.common.util.AsyncEventPublisher;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.listener.AbstractSiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.siddhi.core.event.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Publishes events of a stream to the event receiver spout running on Storm. There will be SiddhiStormInputEventDispatcher
 * instance for each imported stream of execution plan
 */
public class SiddhiStormInputEventDispatcher extends AbstractSiddhiInputEventDispatcher{
    private static final Log log = LogFactory.getLog(SiddhiStormInputEventDispatcher.class);

    private final DistributedConfiguration stormDeploymentConfig;

    private org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition;
    private String logPrefix;
    private ExecutorService executorService;
    private AsyncEventPublisher asyncEventPublisher;
    private final ConnectionCallback connectionCallback;

    public SiddhiStormInputEventDispatcher(StreamDefinition streamDefinition, String siddhiStreamId,
                                           ExecutionPlanConfiguration executionPlanConfiguration, int tenantId,
                                           DistributedConfiguration stormDeploymentConfig,
                                           ConnectionCallback connectionCallback) {
        super(streamDefinition.getStreamId(), siddhiStreamId, executionPlanConfiguration, tenantId);
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.connectionCallback = connectionCallback;
        this.executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().
                setNameFormat("Thread pool- component - SiddhiStormInputEventDispatcher.executorService;" +
                "tenantId - " + tenantId + ";executionPlanName - " + executionPlanConfiguration.getName() +
                ";streamDefinition - " + streamDefinition.getStreamId()).build());
        init(streamDefinition, siddhiStreamId, executionPlanConfiguration);
    }

    private void init(StreamDefinition streamDefinition, String siddhiStreamName, ExecutionPlanConfiguration executionPlanConfiguration) {
        logPrefix = "[CEP Receiver|ExecPlan:" + executionPlanConfiguration.getName() + ", Tenant:" + tenantId + ", Stream:" + siddhiStreamName + "] ";

        try {
            this.siddhiStreamDefinition = EventProcessorUtil.convertToSiddhiStreamDefinition(streamDefinition, siddhiStreamName);
            Set<org.wso2.siddhi.query.api.definition.StreamDefinition> streamDefinitions = new HashSet<>();
            streamDefinitions.add(siddhiStreamDefinition);

            asyncEventPublisher = new AsyncEventPublisher(AsyncEventPublisher.DestinationType.STORM_RECEIVER,
                                                          streamDefinitions,
                                                          stormDeploymentConfig.getManagers(),
                                                          executionPlanConfiguration.getName(),
                                                          tenantId,
                                                          stormDeploymentConfig,
                                                          this.connectionCallback);

            asyncEventPublisher.initializeConnection(false);
        } catch (Exception e) {
            log.error(logPrefix + "Failed to start event listener", e);
        }
    }

    @Override
    public void sendEvent(Event event) throws InterruptedException {
        asyncEventPublisher.sendEvent(event.getData(),event.getTimestamp(), this.siddhiStreamDefinition.getId());
    }

    @Override
    public void shutdown() {
        asyncEventPublisher.shutdown();
        executorService.shutdown();
    }
}
