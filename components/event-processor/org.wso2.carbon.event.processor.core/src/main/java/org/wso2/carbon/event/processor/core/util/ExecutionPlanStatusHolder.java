package org.wso2.carbon.event.processor.core.util;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the status relating to an execution plan
 */
public class ExecutionPlanStatusHolder implements Serializable {


    private static Logger log = Logger.getLogger(ExecutionPlanStatusHolder.class);

    private DistributedModeConstants.TopologyState topologyState = DistributedModeConstants.TopologyState.UNKNOWN;

    private Map<String,Integer> publisherBoltsMap = new HashMap<String,Integer>();         // < IP , #publisherBolts >
    private Map<String,Integer[]> cepReceiversMap = new HashMap<String,Integer[]>();            // < IP, #pendingReceiverConnections >

    private int requiredPublisherBoltsCount = 0;

    public ExecutionPlanStatusHolder(int requiredPublisherBolts) {
        this.requiredPublisherBoltsCount = requiredPublisherBolts;
    }

    public DistributedModeConstants.TopologyState getTopologyState() {
        return topologyState;
    }

    public void setStormTopologyStatus(DistributedModeConstants.TopologyState topologyState){
        this.topologyState = topologyState;
    }

    public int getRequiredPublisherBoltsCount() {
        return requiredPublisherBoltsCount;
    }

    public void setCEPReceiverStatus(String hostIp, int connected, int required){
        log.info("------------------------------------ setCEPReceiverStatus " + connected + "/" + required);
        cepReceiversMap.put(hostIp,new Integer[]{connected,required});
    }

    public void setConnectedPublisherBoltsCount(String hostIp, int connectedCount){
        log.info("------------------------------------ setConnectedPublisherBoltsCount " + connectedCount);
        publisherBoltsMap.put(hostIp,connectedCount);
    }

    public String getExecutionPlanStatus(){
        String topologyStatus = this.getTopologyState().toString() + "\n";

        //cep receiver status
        String receiverStatus = "CEP Receivers > ";
        String receiverStatusDetails = "";
        int totalPendingConnections = 0;
        for (Map.Entry<String,Integer[]> entry : cepReceiversMap.entrySet()){
            totalPendingConnections += entry.getValue()[1] - entry.getValue()[0];
            receiverStatusDetails += entry.getKey() + " -> " + entry.getValue()[0] + "/" + entry.getValue()[1] + "\n";
        }
        if(cepReceiversMap.size() == 0){
            receiverStatus += "No receivers found. \n";
        } else if(totalPendingConnections == 0){
            receiverStatus += "Connected. \n";
        } else {
            receiverStatus += "Pending connections: \n" + receiverStatusDetails + "\n";
        }

        //publishing bolts status
        int boltsConnected = 0;
        String stormPublishingBoltStatus = "Storm Publishers > ";
        for (Map.Entry<String,Integer> entry : publisherBoltsMap.entrySet()){
            boltsConnected += entry.getValue();
        }
        int requiredPublisherBoltsCount = getRequiredPublisherBoltsCount();
        if(requiredPublisherBoltsCount - boltsConnected == 0){
            stormPublishingBoltStatus += "Connected. \n";
        } else {
            stormPublishingBoltStatus += "Pending connections: " + (requiredPublisherBoltsCount - boltsConnected)
                    + "/" + requiredPublisherBoltsCount + "\n";
        }
        return topologyStatus + receiverStatus + stormPublishingBoltStatus;
    }
}
