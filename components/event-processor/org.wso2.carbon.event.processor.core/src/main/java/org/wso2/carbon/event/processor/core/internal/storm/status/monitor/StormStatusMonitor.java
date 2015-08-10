package org.wso2.carbon.event.processor.core.internal.storm.status.monitor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.util.DistributedModeConstants;
import org.wso2.carbon.event.processor.core.util.ExecutionPlanStatusHolder;
import org.wso2.carbon.event.processor.manager.commons.transport.server.ConnectionCallback;

import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StormStatusMonitor implements ConnectionCallback{

    private static final Log log = LogFactory.getLog(StormStatusMonitor.class);

    private AtomicInteger connectedCepReceiversCount;
    private int importedStreamsCount = 0;

    private AtomicInteger connectedPublisherBoltsCount;


    private String stormTopologyName;
    private String executionPlanName;
    private int tenantId;
    private String executionPlanStatusHolderKey;
    private String hostIp = null;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final int lockTimeout;
    private String tenantDomain;

    public StormStatusMonitor(int tenantId, String executionPlanName, int importedStreamsCount){
        tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        connectedCepReceiversCount = new AtomicInteger(0);
        connectedPublisherBoltsCount = new AtomicInteger(0);
        try {
            hostIp = HostAddressFinder.findAddress("localhost");
        } catch (SocketException e) {
            //do nothing. Let this be retried in the callbacks.
        }
        this.importedStreamsCount = importedStreamsCount;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.stormTopologyName = StormTopologyManager.getTopologyName(executionPlanName, tenantId);
        this.executionPlanStatusHolderKey = DistributedModeConstants.STORM_STATUS_MAP + "." + stormTopologyName;
        lockTimeout = EventProcessorValueHolder.getStormDeploymentConfiguration().getLockTimeout();
        executorService.execute(new GlobalStatUpdater());    //todo:shutdown! done.
    }

    @Override
    public void onCepReceiverConnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
        try {
            if(hostIp == null){
                hostIp = HostAddressFinder.findAddress("localhost");
            }
            if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        log.error("Couldn't increment connected CEP receivers count for execution plan:" + executionPlanName +
                                ", for tenant-domain:" + tenantDomain
                                + " as the ExecutionPlanStatusHolder is null.");
                    } else {
                        executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.incrementAndGet(), importedStreamsCount);
                        executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                    }
                } finally {
                    executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                }
            } else {
                log.error("Couldn't increment connected CEP receivers count for execution plan:" + executionPlanName +
                        ", for tenant-domain:" + tenantDomain
                        + " as the hazelcast lock acquisition failed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Couldn't increment connected CEP receivers count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the hazelcast lock acquisition was interrupted.", e);
        } catch (SocketException e) {
            log.error("Couldn't increment connected CEP receivers count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the host IP couldn't be found for this node.", e);
        }
    }

    @Override
    public void onCepReceiverDisconnect() {
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
        try {
            if(hostIp == null){
                hostIp = HostAddressFinder.findAddress("localhost");
            }
            if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        log.error("Couldn't decrement connected CEP receivers count for execution plan:" + executionPlanName +
                                ", for tenant-domain:" + tenantDomain
                                + " as the ExecutionPlanStatusHolder is null.");
                    }
                    executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.decrementAndGet(), importedStreamsCount);
                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                } finally {
                    executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                }
            } else {
                log.error("Couldn't decrement connected CEP receivers count for execution plan:" + executionPlanName +
                        ", for tenant-domain:" + tenantDomain
                        + " as the hazelcast lock acquisition failed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Couldn't decrement connected CEP receivers count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the hazelcast lock acquisition was interrupted.", e);
        }  catch (SocketException e) {
            log.error("Couldn't decrement connected CEP receivers count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the host IP couldn't be found for this node.", e);
        }
    }

    @Override
    public void onPublisherBoltConnect() {
        log.info("-----------------------------------------------StormOutputListener onConnect() invoked");
        int currentCount = connectedPublisherBoltsCount.incrementAndGet();
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
        try {
            if(hostIp == null){
                hostIp = HostAddressFinder.findAddress("localhost");
            }
            if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        log.error("Couldn't increment connected publisher bolts count for execution plan:" + executionPlanName +
                                ", for tenant-domain:" + tenantDomain
                                + " as the ExecutionPlanStatusHolder is null.");
                    }
                    executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp,currentCount);
                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                } finally {
                    executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                }
            } else {
                log.error("Couldn't increment connected publisher bolts count for execution plan:" + executionPlanName +
                        ", for tenant-domain:" + tenantDomain
                        + " as the hazelcast lock acquisition failed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Couldn't increment connected publisher bolts count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the hazelcast lock acquisition was interrupted.", e);
        } catch (SocketException e) {
            log.error("Couldn't increment connected publisher bolts count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the host IP couldn't be found for this node.", e);
        }
    }

    @Override
    public void onPublisherBoltDisconnect() {
        int currentCount = connectedPublisherBoltsCount.decrementAndGet();
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
        try {
            if(hostIp == null){
                hostIp = HostAddressFinder.findAddress("localhost");
            }
            if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        log.error("Couldn't decrement connected publisher bolts count for execution plan:" + executionPlanName +
                                ", for tenant-domain:" + tenantDomain
                                + " as the ExecutionPlanStatusHolder is null.");
                    }
                    executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp,currentCount);
                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                } finally {
                    executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                }
            } else {
                log.error("Couldn't decrement connected publisher bolts count for execution plan:" + executionPlanName +
                        ", for tenant-domain:" + tenantDomain
                        + " as the hazelcast lock acquisition failed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Couldn't decrement connected publisher bolts count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the hazelcast lock acquisition was interrupted.", e);
        } catch (SocketException e) {
            log.error("Couldn't decrement connected publisher bolts count for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the host IP couldn't be found for this node.", e);
        }
    }

    public void hazelcastListenerCallback(){
        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
        try {
            if(hostIp == null){
                hostIp = HostAddressFinder.findAddress("localhost");
            }
            if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.SECONDS)){
                try {
                    ExecutionPlanStatusHolder executionPlanStatusHolder =
                            executionPlanStatusHolderIMap.get(stormTopologyName);
                    if(executionPlanStatusHolder == null){
                        log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                                ", for tenant-domain:" + tenantDomain
                                + " as the ExecutionPlanStatusHolder is null.");
                    }
                    executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.get(), importedStreamsCount);
                    executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp, connectedPublisherBoltsCount.get());
                    executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                } finally {
                    executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                }
            } else {
                log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                        ", for tenant-domain:" + tenantDomain
                        + " as the hazelcast lock acquisition failed.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the hazelcast lock acquisition was interrupted.", e);
        }  catch (SocketException e) {
            log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                    ", for tenant-domain:" + tenantDomain
                    + " as the host IP couldn't be found for this node.", e);
        }
    }

    public void shutdown(){
        executorService.shutdown();
    }

    /**
     * Updates the ExecutionPlanStatusHolder periodically.
     */
    class GlobalStatUpdater implements Runnable{

        private final int updateRate;

        GlobalStatUpdater(){
            updateRate = EventProcessorValueHolder.getStormDeploymentConfiguration().getUpdateRate();
        }

        @Override
        public void run() {
            while (true) {

                /**
                 * Update
                 */
                HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
                IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP);
                try {
                    if(hostIp == null){
                        hostIp = HostAddressFinder.findAddress("localhost");
                    }
                    if (executionPlanStatusHolderIMap.tryLock(executionPlanStatusHolderKey, lockTimeout, TimeUnit.SECONDS)){
                        try {
                            ExecutionPlanStatusHolder executionPlanStatusHolder =
                                    executionPlanStatusHolderIMap.get(stormTopologyName);
                            if(executionPlanStatusHolder == null){
                                log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                                        ", for tenant-domain:" + tenantDomain
                                        + " as the ExecutionPlanStatusHolder is null.");
                            }
                            executionPlanStatusHolder.setCEPReceiverStatus(hostIp, connectedCepReceiversCount.get(), importedStreamsCount);
                            executionPlanStatusHolder.setConnectedPublisherBoltsCount(hostIp, connectedPublisherBoltsCount.get());
                            executionPlanStatusHolderIMap.replace(stormTopologyName, executionPlanStatusHolder);
                        } finally {
                            executionPlanStatusHolderIMap.unlock(executionPlanStatusHolderKey);
                        }
                    } else {
                        log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                                ", for tenant-domain:" + tenantDomain
                                + " as the hazelcast lock acquisition failed.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                            ", for tenant-domain:" + tenantDomain
                            + " as the hazelcast lock acquisition was interrupted.", e);
                }  catch (SocketException e) {
                    log.error("Couldn't update distributed deployment status for execution plan:" + executionPlanName +
                            ", for tenant-domain:" + tenantDomain
                            + " as the host IP couldn't be found for this node.", e);
                }

                /**
                 * Sleep
                 */
                try {
                    Thread.sleep(updateRate);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
