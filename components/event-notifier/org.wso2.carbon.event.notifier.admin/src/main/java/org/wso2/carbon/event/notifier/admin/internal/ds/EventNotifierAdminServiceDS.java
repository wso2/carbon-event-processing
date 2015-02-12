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
package org.wso2.carbon.event.notifier.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.notifier.admin.internal.util.EventNotifierAdminServiceValueHolder;
import org.wso2.carbon.event.notifier.core.EventNotifierService;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;

/**
 * This class is used to get the EventNotifier service.
 *
 * @scr.component name="eventNotifierAdmin.component" immediate="true"
 * @scr.reference name="endpoint.adaptor.service"
 * interface="org.wso2.carbon.event.notifier.core.OutputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEndpointAdaptorService" unbind="unSetEndpointAdaptorService"
 * @scr.reference name="eventNotifier.service"
 * interface="org.wso2.carbon.event.notifier.core.EventNotifierService" cardinality="1..1"
 * policy="dynamic" bind="setEventNotifierService" unbind="unSetEventNotifierService"
 */
public class EventNotifierAdminServiceDS {

    protected void activate(ComponentContext context) {

    }

    protected void setEndpointAdaptorService(
            OutputEventAdaptorService outputEventAdaptorService) {
        EventNotifierAdminServiceValueHolder.registerEventAdaptorService(outputEventAdaptorService);
    }

    protected void unSetEndpointAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventNotifierAdminServiceValueHolder.registerEventAdaptorService(null);

    }

    protected void setEventNotifierService(EventNotifierService eventNotifierService) {
        EventNotifierAdminServiceValueHolder.registerNotifierService(eventNotifierService);
    }

    protected void unSetEventNotifierService(EventNotifierService eventNotifierService) {
        EventNotifierAdminServiceValueHolder.registerEventAdaptorService(null);
    }


}
