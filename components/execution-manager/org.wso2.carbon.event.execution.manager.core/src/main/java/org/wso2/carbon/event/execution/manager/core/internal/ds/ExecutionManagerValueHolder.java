/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.execution.manager.core.internal.ds;

import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;

public class ExecutionManagerValueHolder {

    private static RegistryService registryService;
    private static EventStreamService eventStreamService;
    private static ExecutionManagerService executionManagerService;
    private static EventProcessorService eventProcessorService;


    public static void setRegistryService(RegistryService registryService) {
        ExecutionManagerValueHolder.registryService = registryService;
    }

    public static void unSetRegistryService() {
        ExecutionManagerValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return ExecutionManagerValueHolder.registryService;
    }

    public static void registerRegistryService(RegistryService registryService) {
        ExecutionManagerValueHolder.registryService = registryService;
    }

    public static ExecutionManagerService getExecutionManagerService() {
        return ExecutionManagerValueHolder.executionManagerService;
    }

    public static void setExecutionManagerService(ExecutionManagerService executionManagerService) {
        ExecutionManagerValueHolder.executionManagerService = executionManagerService;
    }

    public static void registerEventStreamService(EventStreamService eventBuilderService) {
        ExecutionManagerValueHolder.eventStreamService = eventBuilderService;
    }

    public static EventStreamService getEventStreamService() {
        return ExecutionManagerValueHolder.eventStreamService;
    }

    public static EventProcessorService getEventProcessorService() {
        return ExecutionManagerValueHolder.eventProcessorService;
    }

    public static void registerEventProcessorService(EventProcessorService eventProcessorService) {
        ExecutionManagerValueHolder.eventProcessorService = eventProcessorService;
    }

}
