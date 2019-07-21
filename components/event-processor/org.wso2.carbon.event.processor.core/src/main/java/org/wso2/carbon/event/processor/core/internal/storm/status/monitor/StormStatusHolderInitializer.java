/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.storm.status.monitor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.util.DistributedModeConstants;
import org.wso2.carbon.event.processor.core.util.ExecutionPlanStatusHolder;

/**
 * Utility to initialize the statusHolder.
 */
public class StormStatusHolderInitializer {
    private static Log log = LogFactory.getLog(StormStatusHolderInitializer.class);

    public static void initializeStatusHolder(String executionPlanName, int tenantId,
                                              int parallel) {
        String stormTopologyName = StormTopologyManager.getTopologyName(executionPlanName, tenantId);

        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null && hazelcastInstance.getLifecycleService().isRunning()) {
            IMap<String, ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
            ExecutionPlanStatusHolder executionPlanStatusHolder = new ExecutionPlanStatusHolder(parallel);
            executionPlanStatusHolderIMap.put(stormTopologyName, executionPlanStatusHolder);
        } else {
            log.error("Couldn't initialize status info object for execution plan: " + executionPlanName +
                      ", for tenant-ID: " + tenantId
                      + " as the hazelcast instance is not active or not available.");
        }
    }
}
