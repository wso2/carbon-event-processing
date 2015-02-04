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
