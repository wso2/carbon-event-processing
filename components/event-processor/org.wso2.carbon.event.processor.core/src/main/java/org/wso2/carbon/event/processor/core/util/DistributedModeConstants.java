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
