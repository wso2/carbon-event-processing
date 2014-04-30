package org.wso2.carbon.event.stream.manager.core.internal.util.helper;

import org.wso2.carbon.event.stream.manager.core.internal.util.TenantDefaultArtifactDeployer;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;


/**
 * Created by sajith on 4/9/14.
 */
public class TenantMgtListenerImpl implements TenantMgtListener{
    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        TenantDefaultArtifactDeployer.deployDefaultArtifactsForTenant(tenantInfoBean.getTenantId());
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
    }

    @Override
    public void onTenantRename(int tenantId, String s, String s2) throws StratosException {
    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {
    }

    @Override
    public void onTenantActivation(int tenantId) throws StratosException {
    }

    @Override
    public void onTenantDeactivation(int tenantId) throws StratosException {
    }

    @Override
    public void onSubscriptionPlanChange(int tenantId, String s, String s2) throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return 0;
    }
}
