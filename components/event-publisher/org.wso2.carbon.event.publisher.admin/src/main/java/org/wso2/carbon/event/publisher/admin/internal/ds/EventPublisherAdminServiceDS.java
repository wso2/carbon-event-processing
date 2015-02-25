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
package org.wso2.carbon.event.publisher.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.publisher.admin.internal.util.EventPublisherAdminServiceValueHolder;
import org.wso2.carbon.event.publisher.core.EventPublisherService;
import org.wso2.carbon.event.publisher.core.OutputEventAdaptorService;

/**
 * This class is used to get the EventPublisher service.
 *
 * @scr.component name="eventPublisherAdmin.component" immediate="true"
 * @scr.reference name="endpoint.adaptor.service"
 * interface="org.wso2.carbon.event.publisher.core.OutputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEndpointAdaptorService" unbind="unSetEndpointAdaptorService"
 * @scr.reference name="eventPublisher.service"
 * interface="org.wso2.carbon.event.publisher.core.EventPublisherService" cardinality="1..1"
 * policy="dynamic" bind="setEventPublisherService" unbind="unSetEventPublisherService"
 */
public class EventPublisherAdminServiceDS {

    protected void activate(ComponentContext context) {

    }

    protected void setEndpointAdaptorService(
            OutputEventAdaptorService outputEventAdaptorService) {
        EventPublisherAdminServiceValueHolder.registerEventAdaptorService(outputEventAdaptorService);
    }

    protected void unSetEndpointAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventPublisherAdminServiceValueHolder.registerEventAdaptorService(null);

    }

    protected void setEventPublisherService(EventPublisherService eventPublisherService) {
        EventPublisherAdminServiceValueHolder.registerPublisherService(eventPublisherService);
    }

    protected void unSetEventPublisherService(EventPublisherService eventPublisherService) {
        EventPublisherAdminServiceValueHolder.registerEventAdaptorService(null);
    }


}
