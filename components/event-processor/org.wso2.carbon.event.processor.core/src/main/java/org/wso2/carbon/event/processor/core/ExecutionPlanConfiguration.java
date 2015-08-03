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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.StormTopologyManager;
import org.wso2.carbon.event.processor.core.util.EventProcessorDistributedModeConstants;
import org.wso2.carbon.event.processor.core.util.ExecutionPlanStatusHolder;

import java.util.List;
import java.util.Vector;


public class ExecutionPlanConfiguration {
    private String name;
    private String description;
    private boolean isTracingEnabled;
    private boolean isStatisticsEnabled;
    private List<StreamConfiguration> importedStreams;
    private List<StreamConfiguration> exportedStreams;
    private String executionPlan;
    private boolean editable;

    private static Logger log = Logger.getLogger(ExecutionPlanConfiguration.class);    //todo: remove later and organize imports

    public ExecutionPlanConfiguration() {
        importedStreams = new Vector<StreamConfiguration>();
        exportedStreams = new Vector<StreamConfiguration>();
        Thread t = new Thread() {
            public void run() {
                while(true){
                    try {
                        sleep(2000);
                        HazelcastInstance hazelcastInstance = EventProcessorValueHolder.getHazelcastInstance();
                        IMap<String,ExecutionPlanStatusHolder> executionPlanStatusHolderIMap = hazelcastInstance.getMap(EventProcessorDistributedModeConstants.STORM_STATUS_MAP);

                        String stormTopologyName = StormTopologyManager.getTopologyName("PreprocessStats", -1234);
                        ExecutionPlanStatusHolder executionPlanStatusHolder =
                                executionPlanStatusHolderIMap.get(stormTopologyName);

                        if(executionPlanStatusHolder == null){
                            executionPlanStatusHolder = new ExecutionPlanStatusHolder();
                            executionPlanStatusHolderIMap.putIfAbsent(stormTopologyName, executionPlanStatusHolder);
                        }
                        log.info("---------------------------------------------CEP RECEIVERS COUNT=" + executionPlanStatusHolder.getConnectedCepReceiversCount()
                                + "/" + executionPlanStatusHolder.getRequiredCepReceiversCount());
                        log.info("---------------------------------------------BOLTS COUNT=" + executionPlanStatusHolder.getConnectedPublisherBoltsCount()
                                + "/" + executionPlanStatusHolder.getRequiredPublisherBoltsCount());
                        log.info("---------------------------------------------TOPOLOGY STATE=" + executionPlanStatusHolder.getTopologyState().toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        };
        t.start();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StreamConfiguration> getImportedStreams() {
        return importedStreams;
    }

    public List<StreamConfiguration> getExportedStreams() {
        return exportedStreams;
    }

    public String getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(String executionPlan) {
        this.executionPlan = executionPlan;
    }

    public void addImportedStream(StreamConfiguration stream) {
        this.importedStreams.add(stream);
    }

    public void addExportedStream(StreamConfiguration stream) {
        this.exportedStreams.add(stream);
    }

    public void removeExportedStream(String stream) {
        this.exportedStreams.remove(stream);
    }

    public void removeImportedStream(String stream) {
        this.importedStreams.remove(stream);
    }

    public boolean isTracingEnabled() {
        return isTracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        isTracingEnabled = tracingEnabled;
    }

    public boolean isStatisticsEnabled() {
        return isStatisticsEnabled;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        isStatisticsEnabled = statisticsEnabled;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }
}
