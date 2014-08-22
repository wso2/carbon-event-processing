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
package org.wso2.carbon.event.formatter.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorNotificationListener;


public class EventAdaptorNotificationListenerImpl
        implements OutputEventAdaptorNotificationListener {

    private static final Log log = LogFactory.getLog(EventAdaptorNotificationListenerImpl.class);

    @Override
    public void configurationAdded(int tenantId, String eventAdaptorName) {

        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();

        try {
            carbonEventFormatterService.activateInactiveEventFormatterConfigurationForAdaptor(tenantId, eventAdaptorName);
        } catch (EventFormatterConfigurationException e) {
            log.error("Exception occurred while re-deploying the Event formatter configuration files");
        }
    }

    @Override
    public void configurationRemoved(int tenantId, String eventAdaptorName) {
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();

        try {
            carbonEventFormatterService.deactivateActiveEventFormatterConfigurationForAdaptor(tenantId, eventAdaptorName);
        } catch (EventFormatterConfigurationException e) {
            log.error("Exception occurred while un-deploying the Event formatter configuration files");
        }
    }
}
