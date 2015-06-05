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

package org.wso2.carbon.event.processor.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.ExecutionPlan;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.manager.core.EventProcessorManagementService;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.processor.manager.core.exception.EventManagementException;
import org.wso2.siddhi.core.util.snapshot.ByteSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarbonEventProcessorManagementService extends EventProcessorManagementService {
    private static final Log log = LogFactory.getLog(CarbonEventProcessorManagementService.class);
    private int tenantId;
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public CarbonEventProcessorManagementService() {
        EventProcessorValueHolder.getEventManagementService().subscribe(this);
        EventProcessorValueHolder.getEventProcessorService().setManagementInfo(EventProcessorValueHolder.getEventManagementService().getManagementModeInfo());
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    public byte[] getState() {
        Map<Integer, ConcurrentHashMap<String, ExecutionPlan>> map
                = EventProcessorValueHolder.getEventProcessorService().getTenantSpecificExecutionPlans();
        HashMap<Integer, HashMap<String, byte[]>> snapshotdata = new HashMap<Integer, HashMap<String, byte[]>>();

        for (Map.Entry<Integer, ConcurrentHashMap<String, ExecutionPlan>> tenantEntry : map.entrySet()) {
            HashMap<String, byte[]> tenantData = new HashMap<String, byte[]>();
            for (Map.Entry<String, ExecutionPlan> executionPlanData : tenantEntry.getValue().entrySet()) {
                tenantData.put(executionPlanData.getKey(), executionPlanData.getValue().getExecutionPlanRuntime().snapshot());
            }
            snapshotdata.put(tenantEntry.getKey(), tenantData);
        }
        return ByteSerializer.OToB(snapshotdata);
    }

    public void restoreState(byte[] bytes) {
        Map<Integer, ConcurrentHashMap<String, ExecutionPlan>> map
                = EventProcessorValueHolder.getEventProcessorService().getTenantSpecificExecutionPlans();
        HashMap<Integer, HashMap<String, byte[]>> snapshotDataList = (HashMap<Integer, HashMap<String, byte[]>>) ByteSerializer.BToO(bytes);

        for (Map.Entry<Integer, HashMap<String, byte[]>> tenantEntry : snapshotDataList.entrySet()) {
            for (Map.Entry<String, byte[]> executionPlanData : tenantEntry.getValue().entrySet()) {
                ConcurrentHashMap<String, ExecutionPlan> executionPlanMap = map.get(tenantEntry.getKey());
                if (executionPlanMap != null) {
                    ExecutionPlan executionPlan = executionPlanMap.get(executionPlanData.getKey());
                    if (executionPlan != null) {
                        executionPlan.getExecutionPlanRuntime().restore(executionPlanData.getValue());
                    } else {
                        throw new EventManagementException("No execution plans with name '" + executionPlanData.getKey() + "' exist for tenant  " + tenantEntry.getKey());
                    }
                } else {
                    throw new EventManagementException("No execution plans exist for tenant  " + tenantEntry.getKey());
                }
            }
        }
    }

    public void pause() {
        readWriteLock.writeLock().lock();
    }

    public void resume() {
        readWriteLock.writeLock().unlock();
    }

    @Override
    public ManagementModeInfo getManagementModeInfo() {
        return EventProcessorValueHolder.getEventProcessorService().getManagementInfo();
    }


    public void persist(){
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId,true);
            EventProcessorValueHolder.getSiddhiManager().persist();
        } catch (Throwable e) {
            log.error("Unable to persist state for tenant :" + tenantId, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
