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

package org.wso2.carbon.event.processor.core.internal.storm.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import org.apache.log4j.Logger;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class StormManagerServer {

    private static final String STORM_HZ_MAP_ACTIVE_MANAGER_KEY = "storm.hazelcast.map.active.manager.key";
    private static final String STORM_ROLE_TO_MEMBERSHIP_HZ_MAP = "storm.role.membership.hazelcast.map";

    private static Logger log = Logger.getLogger(StormManagerServer.class);
    private TThreadPoolServer stormManagerServer;
    private StormManagerServiceImpl stormManagerService;
    private IMap<String, String> roleToMembershipMap;
    HazelcastInstance hazelcastInstance;
    private String myHazelcastId;
    private Future stateChecker = null;
    private  ScheduledExecutorService executorService;

    public StormManagerServer(String hostName, int port) {

        try {
            stormManagerService = new StormManagerServiceImpl(hostName + ":" + port);
            TServerSocket serverTransport = new TServerSocket(
                    new InetSocketAddress(hostName, port));
            StormManagerService.Processor<StormManagerServiceImpl> processor =
                    new StormManagerService.Processor<StormManagerServiceImpl>(stormManagerService);
            stormManagerServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(stormManagerServer));
            thread.start();

            log.info("CEP Storm Management Thrift Server started on " + hostName + ":" + port);
            executorService = new ScheduledThreadPoolExecutor(3,new ThreadFactoryBuilder().
                    setNameFormat("Thread pool- component - StormManagerServer.executorService").build());
        } catch (TTransportException e) {
            log.error("Cannot start Storm Manager Server on " + hostName + ":" + port, e);
        }
    }

    public void setHzaelCastInstance(HazelcastInstance hazelcastInstance){
        this.hazelcastInstance = hazelcastInstance;
        this.roleToMembershipMap = hazelcastInstance.getMap(STORM_ROLE_TO_MEMBERSHIP_HZ_MAP);
        myHazelcastId = hazelcastInstance.getCluster().getLocalMember().getUuid();

    }

    /**
     * To stop the server
     */
    public void stop() {
        stormManagerServer.stop();
        executorService.shutdown();

        if (stateChecker != null){
            stateChecker.cancel(false);
        }
    }

    public void onExecutionPlanRemove(String excPlanName, int tenantId){
        // Delete all end points of the removed execution plan from manager service.
        stormManagerService.deleteExecPlanEndpoints(tenantId, excPlanName);
    }

    public void setStormCoordinator(boolean isCoordinator) {
        stormManagerService.setStormCoordinator(isCoordinator);

        if (!isCoordinator){
            stateChecker = executorService.schedule(new PeriodicStateChanger(), 10000, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isStormCoordinator() {
        return stormManagerService.isStormCoordinator();
    }

    static class ServerThread implements Runnable {
        private TServer server;

        ServerThread(TServer server) {
            this.server = server;
        }

        public void run() {
            this.server.serve();
        }
    }

    public void verifyState(){
        if (isStormCoordinator() && roleToMembershipMap != null &&
                roleToMembershipMap.get(STORM_HZ_MAP_ACTIVE_MANAGER_KEY) != null &&
                !roleToMembershipMap.get(STORM_HZ_MAP_ACTIVE_MANAGER_KEY).equals(myHazelcastId)){

            log.info("Resigning as storm coordinator as there's another storm coordinator available in the cluster with member id "
                    + roleToMembershipMap.get(STORM_HZ_MAP_ACTIVE_MANAGER_KEY));

            setStormCoordinator(false);
        }
    }

    public synchronized void tryBecomeCoordinator() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null) {
            if(!isStormCoordinator()) {
                ILock lock = hazelcastInstance.getLock("StormCoordinator");
                boolean isCoordinator = lock.tryLock();
                setStormCoordinator(isCoordinator);
                if (isCoordinator) {
                    log.info("Node became the Storm coordinator with member id " + myHazelcastId);
                    if (roleToMembershipMap != null){
                        roleToMembershipMap.put(STORM_HZ_MAP_ACTIVE_MANAGER_KEY, myHazelcastId);
                    }
                }
            }
        }
    }

    class PeriodicStateChanger implements Runnable {

        @Override
        public void run() {
            tryBecomeCoordinator();
        }
    }


}
