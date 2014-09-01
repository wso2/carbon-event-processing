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
package org.wso2.carbon.event.stream.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.stream.manager.admin.internal.util.EventStreamAdminServiceValueHolder;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;

/**
 * This class is used to get the EventFormatter service.
 *
 * @scr.component name="eventStreamAdmin.component" immediate="true"
 * @scr.reference name="eventStreamService.service"
 * interface="org.wso2.carbon.event.stream.manager.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 */
public class EventStreamAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setEventStreamService(
            EventStreamService eventStreamService) {
        EventStreamAdminServiceValueHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(
            EventStreamService eventStreamService) {

    }


}
