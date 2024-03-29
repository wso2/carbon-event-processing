/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.processor.core;

import org.wso2.carbon.event.stream.core.EventProducer;
import org.wso2.carbon.event.stream.core.SiddhiEventConsumer;
import org.wso2.siddhi.core.ExecutionPlanRuntime;

import java.util.ArrayList;
import java.util.List;

// acts as a holder for the components of a query plan.
public class ExecutionPlan {
    private ExecutionPlanRuntime executionPlanRuntime;
    private ExecutionPlanConfiguration executionPlanConfiguration;
    private String name;
    private List<EventProducer> eventProducers = new ArrayList<EventProducer>();
    private List<SiddhiEventConsumer> siddhiEventConsumers = new ArrayList<SiddhiEventConsumer>();


    public ExecutionPlan(String name, ExecutionPlanRuntime executionPlanRuntime,
                         ExecutionPlanConfiguration executionPlanConfiguration) {
        this.executionPlanRuntime = executionPlanRuntime;
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExecutionPlanRuntime getExecutionPlanRuntime() {
        return executionPlanRuntime;
    }

    public void setExecutionPlanRuntime(ExecutionPlanRuntime executionPlanRuntime) {
        this.executionPlanRuntime = executionPlanRuntime;
    }

    public ExecutionPlanConfiguration getExecutionPlanConfiguration() {
        return executionPlanConfiguration;
    }

    public void setExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration) {
        this.executionPlanConfiguration = executionPlanConfiguration;
    }



    public void shutdown() {
        for (SiddhiEventConsumer siddhiEventConsumer : siddhiEventConsumers) {
            siddhiEventConsumer.shutdown();
        }

        executionPlanRuntime.shutdown();
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

}
