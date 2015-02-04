package org.wso2.carbon.event.processing.application.deployer.internal;


import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;

import org.wso2.carbon.event.processing.application.deployer.EventProcessingAppDeployer;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * @scr.component name="event.processing.application.deployer" immediate="true"
 * @scr.reference name="stream.definitionStore.service"
 * interface="org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamStoreService" unbind="unsetEventStreamStoreService"
 */
public class EventProcessingAppDeployerDS {

    private static Log log = LogFactory.getLog(EventProcessingAppDeployerDS.class);

    private static Map<String, List<Feature>> requiredFeatures;

    private static ServiceRegistration appHandlerRegistration;

    public static Map<String, List<Feature>> getRequiredFeatures() {
        return requiredFeatures;
    }

    protected void activate(ComponentContext ctxt) {
        try {
            // Register EventProcessing deployer as an OSGi service
            EventProcessingAppDeployer cepDeployer = new EventProcessingAppDeployer();
            appHandlerRegistration = ctxt.getBundleContext().registerService(
                    AppDeploymentHandler.class.getName(), cepDeployer, null);

            // read required-features.xml
            URL reqFeaturesResource = ctxt.getBundleContext().getBundle()
                    .getResource(AppDeployerConstants.REQ_FEATURES_XML);
            if (reqFeaturesResource != null) {
                InputStream xmlStream = reqFeaturesResource.openStream();
                requiredFeatures = AppDeployerUtils
                        .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
            }
        } catch (Throwable e) {
            log.error("Failed to activate EventProcessing Application Deployer", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // Unregister the OSGi service
        if (appHandlerRegistration != null) {
            appHandlerRegistration.unregister();
        }
    }

    protected void setEventStreamStoreService(AbstractStreamDefinitionStore eventStreamStoreService) {
        ServiceHolder.registerEventStreamStoreService(eventStreamStoreService);
    }

    protected void unsetEventStreamStoreService(AbstractStreamDefinitionStore eventStreamStoreService) {
         ServiceHolder.registerEventStreamStoreService(null);
    }
}
