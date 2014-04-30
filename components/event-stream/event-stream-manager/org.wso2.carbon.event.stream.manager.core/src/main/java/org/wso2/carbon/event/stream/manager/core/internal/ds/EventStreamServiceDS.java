package org.wso2.carbon.event.stream.manager.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.manager.core.internal.stream.DataBridgeStreamAddRemoveListenerImpl;
import org.wso2.carbon.event.stream.manager.core.internal.util.EventStreamConfigurationHelper;
import org.wso2.carbon.event.stream.manager.core.internal.util.helper.TenantMgtListenerImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="eventStreamService.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="stream.definitionStore.service"
 * interface="org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamStoreService" unbind="unsetEventStreamStoreService"
 */
public class EventStreamServiceDS {
    private static final Log log = LogFactory.getLog(EventStreamServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventStreamService carbonEventStreamService = createEventStreamManagerService();
            EventStreamServiceValueHolder.getStreamDefinitionStore().subscribe(new DataBridgeStreamAddRemoveListenerImpl(carbonEventStreamService));

            loadEventStreamsFromConfigFile();
            context.getBundleContext().registerService(EventStreamService.class.getName(), carbonEventStreamService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventStreamService");
            }

            TenantMgtListenerImpl tenantMgtListenerImpl = new TenantMgtListenerImpl();
            context.getBundleContext().registerService(TenantMgtListener.class.getName(), tenantMgtListenerImpl, null);

        } catch (RuntimeException e) {
            log.error("Could not create EventStreamService : " + e.getMessage(), e);
        } catch (EventStreamConfigurationException e) {
            log.error("Could not create EventStreamService : " + e.getMessage(), e);
        }
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        EventStreamServiceValueHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        EventStreamServiceValueHolder.unSetRegistryService();
    }

    protected void setRealmService(RealmService realmService) {
        EventStreamServiceValueHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        EventStreamServiceValueHolder.setRealmService(null);
    }

    private CarbonEventStreamService createEventStreamManagerService()
            throws EventStreamConfigurationException {
        CarbonEventStreamService carbonEventStreamService = new CarbonEventStreamService();
        EventStreamServiceValueHolder.setCarbonEventStreamService(carbonEventStreamService);
        return carbonEventStreamService;
    }

    private void loadEventStreamsFromConfigFile() {
        try {
            EventStreamConfigurationHelper.loadEventStreamDefinitionFromConfigurationFile();
        } catch (EventStreamConfigurationException e) {
            log.error("Could not load event streams from config file : " + e.getMessage(), e);
        }
    }

    protected void setEventStreamStoreService(
            AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        EventStreamServiceValueHolder.setStreamDefinitionStore(abstractStreamDefinitionStore);
    }

    protected void unsetEventStreamStoreService(
            AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        EventStreamServiceValueHolder.setStreamDefinitionStore(null);
    }
}
