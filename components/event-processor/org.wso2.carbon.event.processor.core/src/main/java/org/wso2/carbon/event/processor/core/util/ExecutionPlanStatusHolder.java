/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.event.processor.core.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the status relating to an execution plan
 */
public class ExecutionPlanStatusHolder implements Serializable {

    private DistributedModeConstants.TopologyState topologyState = DistributedModeConstants.TopologyState.UNKNOWN;

    private final Map<String,Integer> publisherBoltsMap = new HashMap<>();         // < IP , #publisherBolts >
    private final Map<String,Integer[]> cepReceiversMap = new HashMap<>();            // < IP, #pendingReceiverConnections >

    private int requiredPublisherBoltsCount = 0;

    public ExecutionPlanStatusHolder(int requiredPublisherBolts) {
        this.requiredPublisherBoltsCount = requiredPublisherBolts;
    }

    DistributedModeConstants.TopologyState getTopologyState() {
        return topologyState;
    }

    public void setStormTopologyStatus(DistributedModeConstants.TopologyState topologyState){
        this.topologyState = topologyState;
    }

    int getRequiredPublisherBoltsCount() {
        return requiredPublisherBoltsCount;
    }

    public void setCEPReceiverStatus(String hostIp, int connected, int required){
        cepReceiversMap.put(hostIp,new Integer[]{connected,required});
    }

    public void setConnectedPublisherBoltsCount(String hostIp, int connectedCount){
        publisherBoltsMap.put(hostIp,connectedCount);
    }

    public String getExecutionPlanStatus(){
        String topologyStatus = "Storm topology : " + this.getTopologyState().toString() + "\n";

        //cep receiver status
        String receiverStatus = "Inflow connections : ";

        if(cepReceiversMap.size() == 0){
            receiverStatus += "no receivers found \n";
        } else {
            receiverStatus += "\n";
            for (Map.Entry<String,Integer[]> entry : cepReceiversMap.entrySet()){
                if(entry.getValue()[1] - entry.getValue()[0] == 0){
                    receiverStatus += "\t" + entry.getKey() + " : all established\n";
                } else {
                    receiverStatus += "\t" + entry.getKey() + " : " + entry.getValue()[0] + "/" + entry.getValue()[1] + " established\n";
                }
            }
        }

        //publishing bolts status
        int boltsConnected = 0;
        String stormPublishingBoltStatus = "Outflow connections : ";
        for (Map.Entry<String,Integer> entry : publisherBoltsMap.entrySet()){
            boltsConnected += entry.getValue();
        }
        int requiredPublisherBoltsCount = getRequiredPublisherBoltsCount();
        if(requiredPublisherBoltsCount - boltsConnected == 0){
            stormPublishingBoltStatus += " all established\n";
        } else {
            stormPublishingBoltStatus += boltsConnected + "/" + requiredPublisherBoltsCount + " established\n";
        }
        return topologyStatus + receiverStatus + stormPublishingBoltStatus;
    }
}
