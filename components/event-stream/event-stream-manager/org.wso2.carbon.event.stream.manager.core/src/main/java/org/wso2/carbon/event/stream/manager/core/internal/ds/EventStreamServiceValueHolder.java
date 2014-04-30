package org.wso2.carbon.event.stream.manager.core.internal.ds;

import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.stream.manager.core.internal.CarbonEventStreamService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

public class EventStreamServiceValueHolder {

    private static RegistryService registryService;
    private static CarbonEventStreamService carbonEventStreamService;
    private static AbstractStreamDefinitionStore abstractStreamDefinitionStore;
    private static RealmService realmService;

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
            AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        EventStreamServiceValueHolder.abstractStreamDefinitionStore = abstractStreamDefinitionStore;
    }

    public static AbstractStreamDefinitionStore getStreamDefinitionStore() {
        return EventStreamServiceValueHolder.abstractStreamDefinitionStore;
    }
}
