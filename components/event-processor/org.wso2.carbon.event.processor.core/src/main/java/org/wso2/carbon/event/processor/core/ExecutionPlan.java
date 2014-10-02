/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.core;

import org.wso2.carbon.event.processor.core.internal.ha.HAManager;
import org.wso2.carbon.event.processor.core.internal.storm.SiddhiStormOutputEventListener;
import org.wso2.carbon.event.stream.manager.core.EventProducer;
import org.wso2.carbon.event.stream.manager.core.SiddhiEventConsumer;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.ArrayList;
import java.util.List;

// acts as a holder for the components of a query plan.
public class ExecutionPlan {
    private SiddhiManager siddhiManager;
    private ExecutionPlanConfiguration executionPlanConfiguration;
    private String name;
    private HAManager haManager;
    private List<EventProducer> eventProducers = new ArrayList<EventProducer>();
    private List<SiddhiEventConsumer> siddhiEventConsumers = new ArrayList<SiddhiEventConsumer>();
    private SiddhiStormOutputEventListener stormOutputListener;


    public ExecutionPlan(String name, SiddhiManager siddhiManager,
                         ExecutionPlanConfiguration executionPlanConfiguration, HAManager haManager) {
        this.siddhiManager = siddhiManager;
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.name = name;
        this.haManager = haManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public void setSiddhiManager(SiddhiManager siddhiManager) {
        this.siddhiManager = siddhiManager;
    }

    public ExecutionPlanConfiguration getExecutionPlanConfiguration() {
        return executionPlanConfiguration;
    }

    public void setExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration) {
        this.executionPlanConfiguration = executionPlanConfiguration;
    }

    public HAManager getHaManager() {
        return haManager;
    }

    public void setHaManager(HAManager haManager) {
        this.haManager = haManager;
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
        if(stormOutputListener!=null){
            stormOutputListener.shutdown();
        }
        for(SiddhiEventConsumer siddhiEventConsumer:siddhiEventConsumers){
            siddhiEventConsumer.shutdown();
        }
        siddhiManager.shutdown();
    }

    public void addProducer(EventProducer producer) {
        eventProducers.add(producer);
    }

    public void addConsumer(SiddhiEventConsumer eventConsumer) {
        siddhiEventConsumers.add(eventConsumer);

    }

    public List<EventProducer> getEventProducers() {
        return eventProducers;
    }

    public List<SiddhiEventConsumer> getSiddhiEventConsumers() {
        return siddhiEventConsumers;
    }

    public void addStormOutputListener(SiddhiStormOutputEventListener stormOutputListener) {
        this.stormOutputListener = stormOutputListener;
    }
}
