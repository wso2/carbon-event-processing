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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.common.util.AsyncEventPublisher;
import org.wso2.carbon.event.processor.core.util.EventProcessorDistributedModeConstants;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.util.ExecutionPlanStatusHolder;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.listener.AbstractSiddhiInputEventDispatcher;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;
import org.wso2.carbon.event.processor.manager.core.config.DistributedConfiguration;
import org.wso2.siddhi.core.event.Event;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Publishes events of a stream to the event receiver spout running on Storm. There will be SiddhiStormInputEventDispatcher
 * instance for each imported stream of execution plan
 */
public class SiddhiStormInputEventDispatcher extends AbstractSiddhiInputEventDispatcher implements ConnectionCallback {
    private static final Log log = LogFactory.getLog(SiddhiStormInputEventDispatcher.class);

    private final DistributedConfiguration stormDeploymentConfig;
    private final ExecutionPlanConfiguration executionPlanConfiguration;

    private org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition;
    private String logPrefix;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AsyncEventPublisher asyncEventPublisher;

    private final String stormTopologyName;
    private final String keyExecutionPlanStatusHolder;

    public SiddhiStormInputEventDispatcher(StreamDefinition streamDefinition, String siddhiStreamId,
                                           ExecutionPlanConfiguration executionPlanConfiguration, int tenantId,
                                           DistributedConfiguration stormDeploymentConfig) {
        super(streamDefinition.getStreamId(), siddhiStreamId, executionPlanConfiguration, tenantId);
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.stormTopologyName = StormTopologyManager.getTopologyName(executionPlanConfiguration.getName(), tenantId);
        this.keyExecutionPlanStatusHolder = EventProcessorDistributedModeConstants.STORM_STATUS_MAP + "." + stormTopologyName;
        init(streamDefinition, siddhiStreamId, executionPlanConfiguration);
    }

    private void init(StreamDefinition streamDefinition, String siddhiStreamName, ExecutionPlanConfiguration executionPlanConfiguration) {
        logPrefix = "[CEP Receiver|ExecPlan:" + executionPlanConfiguration.getName() + ", Tenant:" + tenantId + ", Stream:" + siddhiStreamName + "] ";

        try {
            this.siddhiStreamDefinition = EventProcessorUtil.convertToSiddhiStreamDefinition(streamDefinition, siddhiStreamName);
            Set<org.wso2.siddhi.query.api.definition.StreamDefinition> streamDefinitions = new HashSet<org.wso2.siddhi.query.api.definition.StreamDefinition>();
            streamDefinitions.add(siddhiStreamDefinition);

            asyncEventPublisher = new AsyncEventPublisher(AsyncEventPublisher.DestinationType.STORM_RECEIVER,
                                                          streamDefinitions,
                                                          stormDeploymentConfig.getManagers(),
                                                          executionPlanConfiguration.getName(),
                                                          tenantId,
                                                          stormDeploymentConfig,
                                                          this);

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

    @Override
    public void onConnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(EventProcessorDistributedModeConstants.STORM_STATUS_MAP);
        try {
            if (executionPlanStatusHolderIMap.tryLock(keyExecutionPlanStatusHolder, EventProcessorDistributedModeConstants.LOCK_TIMEOUT, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        executionPlanStatusHolder = new ExecutionPlanStatusHolder();
                        executionPlanStatusHolderIMap.putIfAbsent(stormTopologyName, executionPlanStatusHolder);
                    }
                    executionPlanStatusHolder.incrementConnectedCEPReceiversCount();
                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                } finally {
                    executionPlanStatusHolderIMap.unlock(keyExecutionPlanStatusHolder);
                }
            } else {
                log.error(EventProcessorDistributedModeConstants.ERROR_LOCK_ACQUISITION_FAILED_FOR_CONNECTED_CEP_RECEIVERS);
            }
        } catch (InterruptedException e) {
            log.error(EventProcessorDistributedModeConstants.ERROR_LOCK_ACQUISITION_FAILED_FOR_CONNECTED_CEP_RECEIVERS, e);
        }
    }

    @Override
    public void onClose() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(EventProcessorDistributedModeConstants.STORM_STATUS_MAP);
        try {
            if (executionPlanStatusHolderIMap.tryLock(keyExecutionPlanStatusHolder, EventProcessorDistributedModeConstants.LOCK_TIMEOUT, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        executionPlanStatusHolder = new ExecutionPlanStatusHolder();
                        executionPlanStatusHolderIMap.putIfAbsent(stormTopologyName, executionPlanStatusHolder);
                    }
                    executionPlanStatusHolder.decrementConnectedCEPReceiversCount();
                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                } finally {
                    executionPlanStatusHolderIMap.unlock(keyExecutionPlanStatusHolder);
                }
            } else {
                log.error(EventProcessorDistributedModeConstants.ERROR_LOCK_ACQUISITION_FAILED_FOR_CONNECTED_CEP_RECEIVERS);
            }
        } catch (InterruptedException e) {
            log.error(EventProcessorDistributedModeConstants.ERROR_LOCK_ACQUISITION_FAILED_FOR_CONNECTED_CEP_RECEIVERS, e);
        }
    }
}
