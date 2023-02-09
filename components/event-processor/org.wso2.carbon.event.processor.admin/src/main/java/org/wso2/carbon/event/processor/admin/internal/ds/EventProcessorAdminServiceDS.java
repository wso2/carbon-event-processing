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
package org.wso2.carbon.event.processor.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;


/**
 * This class is used to get the EventProcessor service.
 */
@Component(
        name = "eventProcessorAdmin.component",
        immediate = true)
public class EventProcessorAdminServiceDS {

    @Activate
    protected void activate(ComponentContext context) {

    }

    @Reference(
            name = "eventProcessorService.service",
            service = org.wso2.carbon.event.processor.core.EventProcessorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventProcessorService")
    public void setEventProcessorService(EventProcessorService eventProcessorService) {

        EventProcessorAdminValueHolder.registerEventProcessorService(eventProcessorService);
    }

    public void unsetEventProcessorService(EventProcessorService eventProcessorService) {

        EventProcessorAdminValueHolder.registerEventProcessorService(null);
    }
}
