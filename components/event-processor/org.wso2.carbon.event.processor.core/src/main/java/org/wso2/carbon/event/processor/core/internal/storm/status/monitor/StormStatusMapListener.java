package org.wso2.carbon.event.processor.core.internal.storm.status.monitor;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.internal.storm.status.monitor.exception.DeploymentStatusMonitorException;
import org.wso2.carbon.event.processor.core.util.DistributedModeConstants;

public class StormStatusMapListener {

    private final String listenerId;
    private final HazelcastInstance hazelcastInstance;
    private final StormStatusMonitor stormStatusMonitor;

    public StormStatusMapListener(String executionPlanName, int tenantId, StormStatusMonitor stormStatusMonitor)
            throws DeploymentStatusMonitorException {
        hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        if(hazelcastInstance == null) {
            throw new DeploymentStatusMonitorException("Couldn't initialize Distributed Deployment Status monitor as" +
                    " the hazelcast instance is null. Enable clustering and restart the server");    //not giving context info, since this is not a per execution plan or tenant specific exception.
        }
        String stormTopologyName = StormTopologyManager.getTopologyName(executionPlanName, tenantId);
        listenerId = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP).
                addEntryListener(new MapListenerImpl(), stormTopologyName, true);
        this.stormStatusMonitor = stormStatusMonitor;
    }

    /**
     * Clean up method, removing the entry listener.
     */
    public void removeEntryListener(){
        if(hazelcastInstance.getLifecycleService().isRunning()){
            hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP).removeEntryListener(listenerId);
        }
    }

    private class MapListenerImpl implements EntryAddedListener, EntryUpdatedListener{
        @Override
        public void entryAdded(EntryEvent entryEvent) {
            if(!entryEvent.getMember().localMember()){
                stormStatusMonitor.hazelcastListenerCallback();
            }
        }

        @Override
        public void entryUpdated(EntryEvent entryEvent) {
            if(!entryEvent.getMember().localMember()){
                stormStatusMonitor.hazelcastListenerCallback();
            }
        }
    }
}
