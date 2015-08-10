package org.wso2.carbon.event.processor.core.util;

public final class DistributedModeConstants {

    public static final String STORM_STATUS_MAP = "org.wso2.cep.org.wso2.carbon.event.processor.core.storm.status.execution.plan.ui";

    /**
     * These states are different to the states in Storm terminology, except for ACTIVE
     */
    public enum TopologyState{
        UNKNOWN,            //Topology status has not been queried yet from Storm.
        CLEANING,           //StormTopologyManager is cleaning an existing topology with the same name, to deploy this topology.
        DEPLOYING,          //StormTopologyManager is in the process of deploying this  topology
        ACTIVE,             //Indicates that the topology was found to be in ACTIVE state (as in Storm terminology) in the storm cluster.
    }
}
