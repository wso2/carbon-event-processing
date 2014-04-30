package org.wso2.carbon.event.stream.manager.core.internal.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by sajith on 4/9/14.
 */
public class TenantDefaultArtifactDeployer {
    public static final String DEFAULT_CONF_LOCATION = CarbonUtils.getCarbonHome() + File.separatorChar + "repository" + File.separatorChar +
            "conf" + File.separatorChar + "cep" + File.separatorChar + "default-artifacts";

    private static final Log log = LogFactory.getLog(TenantDefaultArtifactDeployer.class);

    public static void deployDefaultArtifactsForTenant(int tenantId){
        try {
            File defaultArtifactDir = new File(DEFAULT_CONF_LOCATION);

            if (!defaultArtifactDir.exists() || !defaultArtifactDir.isDirectory()){
                log.warn("Default artifacts are not available at  " + DEFAULT_CONF_LOCATION);
                return;
            }

            FileUtils.copyDirectory(new File(DEFAULT_CONF_LOCATION), new File(MultitenantUtils.getAxis2RepositoryPath(tenantId)));
            log.info("Successfully deployed default artifacts for tenant id " + tenantId);

        } catch (IOException e) {
            log.warn("Could not deploy default artifacts for the tenant : " + e.getMessage(), e);
        }
    }
}
