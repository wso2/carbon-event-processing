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
package org.wso2.carbon.event.output.adaptor.websocket.local.internal.ds;

import org.wso2.carbon.event.output.adaptor.websocket.local.internal.WebsocketLocalOutputCallbackRegisterServiceInternal;

/**
 * Creates a holder of type WebsocketLocalOutputCallbackRegisterServiceInternal.
 */
public final class WebsocketLocalEventAdaptorServiceInternalValueHolder {

    private static WebsocketLocalOutputCallbackRegisterServiceInternal websocketLocalOutputCallbackRegisterServiceInternal;

    public static void registerWebsocketOutputCallbackRegisterServiceInternal(WebsocketLocalOutputCallbackRegisterServiceInternal websocketLocalOutputCallbackRegisterServiceInternal) {
        WebsocketLocalEventAdaptorServiceInternalValueHolder.websocketLocalOutputCallbackRegisterServiceInternal = websocketLocalOutputCallbackRegisterServiceInternal;
    }

    public static WebsocketLocalOutputCallbackRegisterServiceInternal getWebsocketLocalOutputCallbackRegisterServiceInternal() {
        return WebsocketLocalEventAdaptorServiceInternalValueHolder.websocketLocalOutputCallbackRegisterServiceInternal;
    }
}
