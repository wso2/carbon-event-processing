package org.wso2.carbon.event.processor.core.internal.storm.status.monitor;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.util.DistributedModeConstants;

public class StormStatusMapListener {

    private static final Log log = LogFactory.getLog(StormStatusMapListener.class);

    private String listenerId;
    private HazelcastInstance hazelcastInstance;
    private StormStatusMonitor stormStatusMonitor;

    public StormStatusMapListener(String executionPlanName, int tenantId, StormStatusMonitor stormStatusMonitor){
        String stormTopologyName = StormTopologyManager.getTopologyName(executionPlanName, tenantId);
        hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
        listenerId = hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP).
                addEntryListener(new EntryListenerImpl(stormTopologyName), stormTopologyName, true);
        this.stormStatusMonitor = stormStatusMonitor;
    }

    /**
     * Clean up method, removing the entry listener.
     */
    public void removeEntryListener(){
        hazelcastInstance.getMap(DistributedModeConstants.STORM_STATUS_MAP).removeEntryListener(listenerId);
    }


    class EntryListenerImpl implements EntryListener{

        String stormTopologyName;

        EntryListenerImpl(String stormTopologyName){
            this.stormTopologyName = stormTopologyName;
        }

        @Override
        public void entryAdded(EntryEvent entryEvent) {
            if(!entryEvent.getMember().localMember()){
                stormStatusMonitor.hazelcastListenerCallback();
            }
        }

        @Override
        public void entryRemoved(EntryEvent entryEvent) {
            //means the manager has removed an entry from the map; so no point of trying to update.
        }

        @Override
        public void entryUpdated(EntryEvent entryEvent) {
            if(!entryEvent.getMember().localMember()){
                stormStatusMonitor.hazelcastListenerCallback();
            }
        }

        @Override
        public void entryEvicted(EntryEvent entryEvent) {
            //Eviction has not been configured so this need not be handled.
            //Even if eviction has been configured, worker does not need to do an update on eviction,
            //  because that update would be of an obsolete execution plan.
        }
    }
}
