package org.wso2.carbon.event.processor.core.util;

import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * Holds the status relating to an execution plan
 */
public class ExecutionPlanStatusHolder implements Serializable {

    //todo: Add debug logs

    private static Logger log = Logger.getLogger(ExecutionPlanStatusHolder.class);

    private EventProcessorDistributedModeConstants.TopologyState topologyState = EventProcessorDistributedModeConstants.TopologyState.NOT_INITIALIZED;
    private int connectedCepReceiversCount = 0;
    private int requiredCepReceiversCount = 0;
    private int connectedPublisherBoltsCount = 0;
    private int requiredPublisherBoltsCount = 0;

    public EventProcessorDistributedModeConstants.TopologyState getTopologyState() {
        return topologyState;
    }

    public void setStormTopologyStatus(EventProcessorDistributedModeConstants.TopologyState topologyState){
        this.topologyState = topologyState;
    }

    public int getConnectedCepReceiversCount() {
        return connectedCepReceiversCount;
    }

    public int getConnectedPublisherBoltsCount() {
        return connectedPublisherBoltsCount;
    }

    public int getRequiredCepReceiversCount() {
        return requiredCepReceiversCount;
    }

    public int getRequiredPublisherBoltsCount() {
        return requiredPublisherBoltsCount;
    }

    public void setRequiredCepReceiversCount(int requiredCepReceiversCount) {
        this.requiredCepReceiversCount += requiredCepReceiversCount;           // += is put because multiple workers could invoke setRequiredCepReceiversCount()
    }

    public void setRequiredPublisherBoltsCount(int requiredPublisherBoltsCount) {
        this.requiredPublisherBoltsCount += requiredPublisherBoltsCount;      // += is put in case multiple managers (if it's allowed) could invoke setRequiredPublisherBoltsCount()
    }

    public void incrementConnectedCEPReceiversCount(){
        connectedCepReceiversCount++;
    }

    public void decrementConnectedCEPReceiversCount(){
        if(connectedCepReceiversCount > 0) {
            connectedCepReceiversCount--;
        }
    }

    public void incrementConnectedPublisherBoltsCount(){
        connectedPublisherBoltsCount++;
    }

    public void decrementConnectedPublisherBoltsCount(){
        if(connectedPublisherBoltsCount > 0) {
            connectedPublisherBoltsCount--;
        }
    }

    public String getExecutionPlanStatus(){
        String topologyStatus = null;
        switch (this.getTopologyState()) {
            case ACTIVE:
                topologyStatus = "Topology was found to be in ACTIVE state in the storm cluster.\n";
                break;
            case REMOVED:
                topologyStatus = "Topology has been removed from the storm cluster.\n";
                break;
            case NOT_INITIALIZED:
                topologyStatus = "Starting to query the topology status from Storm...";
                break;
        }
        String cepReceiverStatus = this.getConnectedCepReceiversCount() + " of " + this.getRequiredCepReceiversCount() +
                " CEP Receivers are connected to Storm Receivers.\n";
        String stormPublishingBoltStatus = this.getConnectedPublisherBoltsCount() + " of " + this.getRequiredPublisherBoltsCount() +
                " Storm Publishing Bolts are connected to CEP Publishers.\n";
        return topologyStatus + cepReceiverStatus + stormPublishingBoltStatus;
    }
}
