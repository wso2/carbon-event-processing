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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.apache.log4j.Logger;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;

import java.net.InetSocketAddress;

public class StormManagerServer {

    private static Logger log = Logger.getLogger(StormManagerServer.class);
    private TThreadPoolServer stormManagerServer;
    private StormManagerServiceImpl stormManagerService;

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

        } catch (TTransportException e) {
            log.error("Cannot start Storm Manager Server on " + hostName + ":" + port, e);
        }
    }

    /**
     * To stop the server
     */
    public void stop() {
        stormManagerServer.stop();
    }

    public void setStormManager(boolean stormManager) {
        stormManagerService.setStormManager(stormManager);
    }

    public boolean isStormManager() {
        return stormManagerService.isStormManager();
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


    public void tryBecomeCoordinator() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if (hazelcastInstance != null) {
            ILock lock = hazelcastInstance.getLock("StormManager");
            boolean isCoordinator = lock.tryLock();
            log.info("Node became Storm Coordinator");
            setStormManager(isCoordinator);
        }
    }


}
