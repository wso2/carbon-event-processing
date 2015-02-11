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
package org.wso2.carbon.event.notifier.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorNotificationListener;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;


public class EventAdaptorNotificationListenerImpl
        implements OutputEventAdaptorNotificationListener {

    private static final Log log = LogFactory.getLog(EventAdaptorNotificationListenerImpl.class);

    @Override
    public void configurationAdded(int tenantId, String eventAdaptorName) {

        CarbonEventNotifierService carbonEventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();

        try {
            carbonEventNotifierService.activateInactiveEventFormatterConfigurationForAdaptor(tenantId, eventAdaptorName);
        } catch (EventNotifierConfigurationException e) {
            log.error("Exception occurred while re-deploying the Event notifier configuration files");
        }
    }

    @Override
    public void configurationRemoved(int tenantId, String eventAdaptorName) {
        CarbonEventNotifierService carbonEventNotifierService = EventNotifierServiceValueHolder.getCarbonEventNotifierService();

        try {
            carbonEventNotifierService.deactivateActiveEventFormatterConfigurationForAdaptor(tenantId, eventAdaptorName);
        } catch (EventNotifierConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event notifier configuration files");
        }
    }
}
