/*
 *
 *  Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.output.adapter.ui.internal.ds;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.event.output.adapter.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a holder of type UIOutputCallbackRegisterServiceImpl.
 */
public final class UIEventAdaptorServiceInternalValueHolder {

    private static UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl;
    private static Map<String, String> outputEventStreamMap = new HashMap<String, String>();
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedList<Object>>> tenantSpecificStreamMap
            = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedList<Object>>>();
    private static HttpService httpService;
    private static RealmService realmService;

    public static void registerUIOutputCallbackRegisterServiceInternal(
            UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl) {
        UIEventAdaptorServiceInternalValueHolder.UIOutputCallbackRegisterServiceImpl =
                UIOutputCallbackRegisterServiceImpl;
    }

    public static UIOutputCallbackControllerServiceImpl getUIOutputCallbackRegisterServiceImpl() {
        return UIEventAdaptorServiceInternalValueHolder.UIOutputCallbackRegisterServiceImpl;
    }

    public static Map<String, String> getOutputEventStreamMap() {
        return outputEventStreamMap;
    }

    public static void registerHTTPService(
            HttpService httpService) {
        UIEventAdaptorServiceInternalValueHolder.httpService = httpService;
    }

    public static HttpService getHTTPService() {
        return httpService;
    }

    public static void registerRealmService(
            RealmService realmService) {
        UIEventAdaptorServiceInternalValueHolder.realmService = realmService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedList<Object>>>
    getTenantSpecificStreamMap() {
        return tenantSpecificStreamMap;
    }
}
