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
package org.wso2.carbon.event.processor.core.internal.ha;

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.HAServiceClientThriftImpl;
import org.wso2.siddhi.core.ExecutionPlanRuntime;

import java.util.*;
import java.util.concurrent.*;

public class HAManager {
    private static final Log log = LogFactory.getLog(HAManager.class);

    private final HazelcastInstance hazelcastInstance;
    private final String executionPlanName;
    private final int tenantId;
    private final ExecutionPlanRuntime executionPlanRuntime;
    private final int inputProcessors;
    private final CEPMembership currentCepMembershipInfo;
    private boolean activeLockAcquired;
    private boolean passiveLockAcquired;
    private ILock activeLock;
    private ILock passiveLock;
    private IMap<String, CEPMembership> membershipMap;
    private final Map<String, SiddhiHAInputEventDispatcher> inputEventDispatcherMap = new HashMap<String, SiddhiHAInputEventDispatcher>();
    private List<SiddhiHAOutputStreamListener> streamCallbackList = new ArrayList<SiddhiHAOutputStreamListener>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    private ThreadPoolExecutor processThreadPoolExecutor;
    private ThreadBarrier threadBarrier = new ThreadBarrier();
    private Future stateChanger = null;
    private static final String HA_PREFIX = "org.wso2.cep.ha";
    private static String activeId;
    private static String passiveId;


    public HAManager(HazelcastInstance hazelcastInstance, String executionPlanName, int tenantId, ExecutionPlanRuntime executionPlanRuntime, int inputProcessors, CEPMembership currentCepMembershipInfo) {
        this.hazelcastInstance = hazelcastInstance;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.executionPlanRuntime = executionPlanRuntime;
        this.inputProcessors = inputProcessors;
        this.currentCepMembershipInfo = currentCepMembershipInfo;
        activeId = "Active:" + tenantId + ":" + executionPlanName;
        passiveId = "Passive:" + tenantId + ":" + executionPlanName;
        activeLock = hazelcastInstance.getLock(HA_PREFIX + ":" + tenantId + ":" + executionPlanName + ":ActiveLock");
        passiveLock = hazelcastInstance.getLock(HA_PREFIX + ":" + tenantId + ":" + executionPlanName + ":PassiveLock");
        processThreadPoolExecutor = new ThreadPoolExecutor(inputProcessors, inputProcessors,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                if (!activeLockAcquired) {
                    tryChangeState();
                }
            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }
        });

        membershipMap = hazelcastInstance.getMap(HA_PREFIX + ":MembershipMap");
        membershipMap.addEntryListener(new EntryAdapter<String, CEPMembership>() {

            @Override
            public void entryRemoved(EntryEvent<String, CEPMembership> stringCEPMembershipEntryEvent) {
                tryChangeState();
            }

        }, activeId, false);

    }

    public void init() {
        tryChangeState();
        if (!activeLockAcquired) {
            scheduledThreadPoolExecutor.execute(new PeriodicStateChanger());
        }
    }

    private void tryChangeState() {
        if (!passiveLockAcquired) {
            if (passiveLock.tryLock()) {
                passiveLockAcquired = true;
                if (activeLock.tryLock()) {
                    activeLockAcquired = true;
                    becomeActive();
                } else {
                    becomePassive();
                }
            }
        } else if (!activeLockAcquired) {
            if (activeLock.tryLock()) {
                activeLockAcquired = true;
                becomeActive();
            }
        }

    }

    private void becomePassive() {
        membershipMap.put(passiveId, currentCepMembershipInfo);

        threadBarrier.close();

        for (SiddhiHAOutputStreamListener streamCallback : streamCallbackList) {
            streamCallback.setDrop(true);
        }

        CEPMembership cepMembership = membershipMap.get(activeId);

        HAServiceClient haServiceClient = new HAServiceClientThriftImpl();

        SnapshotData snapshotData = null;
        try {
            snapshotData = haServiceClient.getSnapshot(tenantId, executionPlanName, cepMembership, currentCepMembershipInfo);
        } catch (Exception e) {
            log.error("Error in becoming the passive member for " + executionPlanName + " on tenant:" + tenantId + ", " + e.getMessage(), e);
            threadBarrier.open();

            return;
        }

        int count = 0;
        while (count < 1000) {
            if (threadBarrier.getBlockedThreads().longValue() == inputProcessors) {
                break;
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count++;
        }

        try {
            executionPlanRuntime.restore(snapshotData.getStates());
            byte[] eventData = snapshotData.getNextEventData();
            HashMap<String, Object[]> eventMap = (HashMap<String, Object[]>) ByteSerializer.BToO(eventData);
            for (Map.Entry<String, Object[]> entry : eventMap.entrySet()) {
                SiddhiHAInputEventDispatcher inputEventDispatcher = inputEventDispatcherMap.get(entry.getKey());
                if(inputEventDispatcher==null){
                    throw new Exception(entry.getKey() +" stream mismatched with the Active Node " + executionPlanName +" execution plan for tenant:" + tenantId );
                }
                BlockingQueue<Object[]> eventQueue = inputEventDispatcher.getEventQueue();
                Object[] activeEventData = entry.getValue();
                Object[] passiveEventData = eventQueue.peek();
                while (!Arrays.equals(passiveEventData, activeEventData)) {
                    eventQueue.remove();
                    passiveEventData = eventQueue.peek();
                }
            }

        } catch (Throwable t) {
            log.error("Syncing failed when becoming a Passive Node for tenant:" + tenantId + " on:" + executionPlanName +" execution plan", t);

        }

        threadBarrier.open();
        log.info("Became Passive Member for tenant:" + tenantId + " on:" + executionPlanName);

    }

    private void becomeActive() {
        membershipMap.remove(passiveId);
        membershipMap.put(activeId, currentCepMembershipInfo);
        for (SiddhiHAOutputStreamListener streamCallback : streamCallbackList) {
            streamCallback.setDrop(false);
        }
        passiveLock.forceUnlock();
        log.info("Became Active Member for tenant:" + tenantId + " on:" + executionPlanName);

    }

    public ExecutorService getProcessThreadPoolExecutor() {
        return processThreadPoolExecutor;
    }

    public ThreadBarrier getThreadBarrier() {
        return threadBarrier;
    }

    public void addStreamCallback(SiddhiHAOutputStreamListener streamCallback) {
        streamCallbackList.add(streamCallback);
    }

    public void addInputEventDispatcher(String streamId, SiddhiHAInputEventDispatcher eventDispatcher) {
        inputEventDispatcherMap.put(streamId, eventDispatcher);
    }

    public boolean isActiveMember() {
        return activeLockAcquired;
    }

    public void shutdown() {
        if (passiveLockAcquired) {
            membershipMap.remove(passiveId);
            passiveLock.forceUnlock();
        }
        if (activeLockAcquired) {
            activeLock.forceUnlock();
            membershipMap.remove(activeId);
        }
    }

    class PeriodicStateChanger implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            tryChangeState();
            if (!activeLockAcquired) {
                stateChanger = scheduledThreadPoolExecutor.schedule(this, 15, TimeUnit.SECONDS);
            }
        }
    }

    public SnapshotData getActiveSnapshotData() {

        threadBarrier.close();

        SnapshotData snapshotData = new SnapshotData();

        HashMap<String, Object[]> eventMap = new HashMap<String, Object[]>();
        for (Map.Entry<String, SiddhiHAInputEventDispatcher> entry : inputEventDispatcherMap.entrySet()) {
            Object[] activeEventData = entry.getValue().getEventQueue().peek();
            eventMap.put(entry.getKey(), activeEventData);

        }

        snapshotData.setNextEventData(ByteSerializer.OToB(eventMap));
        snapshotData.setStates(executionPlanRuntime.snapshot());

        threadBarrier.open();
        return snapshotData;
    }


}
