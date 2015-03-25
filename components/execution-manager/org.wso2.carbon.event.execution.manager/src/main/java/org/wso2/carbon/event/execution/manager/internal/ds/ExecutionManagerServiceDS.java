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
package org.wso2.carbon.event.execution.manager.internal.ds;


import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * This class is used to get the EventProcessor service.
 *
 * @scr.component name="eventProcessorAdmin.component" immediate="true"
 * @scr.reference name="eventProcessorService.service"
 * interface="org.wso2.carbon.event.processor.core.EventProcessorService" cardinality="1..1"
 * policy="dynamic" bind="setEventProcessorService" unbind="unsetEventProcessorService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */

public class ExecutionManagerServiceDS {
    protected void activate(ComponentContext context) {

    }

    public void setEventProcessorService(EventProcessorService eventProcessorService) {
        ExecutionManagerValueHolder.registerEventProcessorService(eventProcessorService);
    }

    public void unsetEventProcessorService(EventProcessorService eventProcessorService) {
        ExecutionManagerValueHolder.registerEventProcessorService(null);

    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        ExecutionManagerValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ExecutionManagerValueHolder.unSetRegistryService();
    }
}
