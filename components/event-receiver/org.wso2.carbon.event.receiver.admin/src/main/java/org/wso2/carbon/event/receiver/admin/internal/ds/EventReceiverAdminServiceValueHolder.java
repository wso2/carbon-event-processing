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
package org.wso2.carbon.event.receiver.admin.internal.ds;

import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorService;

public class EventReceiverAdminServiceValueHolder {

    private static EventReceiverService eventReceiverService;
    private static InputEventAdaptorService inputEventAdaptorService;

    public static InputEventAdaptorService getInputEventAdaptorService() {
        return inputEventAdaptorService;
    }

    public static void registerInputEventAdaptorService(
            InputEventAdaptorService InputEventAdaptorService) {
        EventReceiverAdminServiceValueHolder.inputEventAdaptorService = InputEventAdaptorService;
    }

    public static void registerEventReceiverService(EventReceiverService eventReceiverService) {
        EventReceiverAdminServiceValueHolder.eventReceiverService = eventReceiverService;
    }

    public static EventReceiverService getEventReceiverService() {
        return EventReceiverAdminServiceValueHolder.eventReceiverService;
    }

}
