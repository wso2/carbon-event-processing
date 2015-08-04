package org.wso2.carbon.event.processor.core.util;

public final class EventProcessorDistributedModeConstants {

    public static final String STORM_STATUS_MAP = "org.wso2.cep.org.wso2.carbon.event.processor.core.storm.status.execution.plan.ui";

    /**
     * These states are different to the states in Storm terminology, except for ACTIVE
     */
    public enum TopologyState{
        ACTIVE,                 //Indicates that the topology was found to be in ACTIVE state (as in Storm terminology) in the storm cluster.
        REMOVED,                //Indicates that no such topology exist in the storm cluster.
        NOT_INITIALIZED         //Indicates the topology status has not been queried yet from Storm.
    }

    public static final int LOCK_TIMEOUT = 1000;   //lock timeout in seconds.

    public final static String ERROR_LOCK_ACQUISITION_FAILED_FOR_TOPOLOGY_STATUS = "Couldn't acquire hazelcast lock for updating the topology status. " +
            "The 'Status in Storm' printed on the execution plan list may be wrong until the execution plan is redeployed.";
    public final static String ERROR_LOCK_ACQUISITION_FAILED_FOR_CONNECTED_PUBLISHING_BOLTS = "Couldn't acquire hazelcast lock for updating the connected publisher bolts count. " +
            "The 'Status in Storm' printed on the execution plan list may be wrong until the execution plan is redeployed.";
    public final static String ERROR_LOCK_ACQUISITION_FAILED_FOR_REQUIRED_PUBLISHING_BOLTS = "Couldn't acquire hazelcast lock for updating the required no of publisher bolts. " +
            "The 'Status in Storm' printed on the execution plan list may be wrong until the execution plan is redeployed.";
    public final static String ERROR_LOCK_ACQUISITION_FAILED_FOR_CONNECTED_CEP_RECEIVERS = "Couldn't acquire hazelcast lock for updating the connected CEP Receivers count. " +
            "The 'Status in Storm' printed on the execution plan list may be wrong until the execution plan is redeployed.";
    public final static String ERROR_LOCK_ACQUISITION_FAILED_FOR_REQUIRED_CEP_RECEIVERS = "Couldn't acquire hazelcast lock for updating the required no of CEP Receivers. " +
            "The 'Status in Storm' printed on the execution plan list may be wrong until the execution plan is redeployed.";
}
