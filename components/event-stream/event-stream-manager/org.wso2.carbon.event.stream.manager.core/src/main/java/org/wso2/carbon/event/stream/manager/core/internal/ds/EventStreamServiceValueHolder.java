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
package org.wso2.carbon.event.stream.manager.core.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.event.stream.manager.core.StreamDefinitionStore;
import org.wso2.carbon.event.stream.manager.core.internal.CarbonEventStreamService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class EventStreamServiceValueHolder {

    private static RegistryService registryService;
    private static CarbonEventStreamService carbonEventStreamService;
    private static StreamDefinitionStore streamDefinitionStore;
    private static RealmService realmService;
    private static HazelcastInstance hazelcastInstance;
    private static ConfigurationContextService configurationContextService;

    private EventStreamServiceValueHolder() {

    }

    public static void unSetRegistryService() {
        EventStreamServiceValueHolder.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return EventStreamServiceValueHolder.registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        EventStreamServiceValueHolder.registryService = registryService;
    }

    public static Registry getRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static void unSetEventStreamService() {
        EventStreamServiceValueHolder.carbonEventStreamService = null;
    }

    public static CarbonEventStreamService getCarbonEventStreamService() {
        return EventStreamServiceValueHolder.carbonEventStreamService;
    }

    public static void setCarbonEventStreamService(
            CarbonEventStreamService carbonEventStreamService) {
        EventStreamServiceValueHolder.carbonEventStreamService = carbonEventStreamService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        EventStreamServiceValueHolder.realmService = realmService;
    }

    public static void setStreamDefinitionStore(
            StreamDefinitionStore streamDefinitionStore) {
        EventStreamServiceValueHolder.streamDefinitionStore = streamDefinitionStore;
    }

    public static StreamDefinitionStore getStreamDefinitionStore() {
        return EventStreamServiceValueHolder.streamDefinitionStore;
    }

    public static void registerHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventStreamServiceValueHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static void registerConfigurationContextService(ConfigurationContextService configurationContextService) {
        EventStreamServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }
}
