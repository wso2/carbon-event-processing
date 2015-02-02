package org.wso2.carbon.event.processing.application.deployer.internal;

import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;

public class ServiceHolder {

    private static AbstractStreamDefinitionStore eventStreamStoreService;

    public static void registerEventStreamStoreService(AbstractStreamDefinitionStore eventStreamStoreService) {
        ServiceHolder.eventStreamStoreService = eventStreamStoreService;
    }

    public static AbstractStreamDefinitionStore getEventStreamStoreService() {
        return eventStreamStoreService;
    }
}
