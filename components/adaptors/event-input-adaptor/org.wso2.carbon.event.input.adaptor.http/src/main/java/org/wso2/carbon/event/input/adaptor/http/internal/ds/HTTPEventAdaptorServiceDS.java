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

package org.wso2.carbon.event.input.adaptor.http.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.http.HTTPEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.http.HTTPMessageServlet;
import org.wso2.carbon.event.input.adaptor.http.internal.util.HTTPEventAdaptorConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import java.util.Hashtable;

/**
 * @scr.component name="input.httpEventAdaptorService.component" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService" unbind="unsetHttpService"
 */


public class HTTPEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(HTTPEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */


    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorFactory httpEventEventAdaptorFactory = new HTTPEventAdaptorFactory();
            context.getBundleContext().registerService(InputEventAdaptorFactory.class.getName(), httpEventEventAdaptorFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input HTTP adaptor service");
            }

        } catch (RuntimeException e) {
            log.error("Can not create the input HTTP adaptor service ", e);
        }
    }

    protected void setRealmService(RealmService realmService) {
        HTTPEventAdaptorServiceValueHolder.registerRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        HTTPEventAdaptorServiceValueHolder.registerRealmService(null);
    }

    protected void setHttpService(HttpService httpService) {
        HTTPEventAdaptorServiceValueHolder.registerHTTPService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {
        HTTPEventAdaptorServiceValueHolder.registerHTTPService(null);
    }

    public static void registerDynamicEndpoint(String adaptorName, String topic, int tenantId) {

        HttpService httpService = HTTPEventAdaptorServiceValueHolder.getHTTPService();

        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                httpService.registerServlet(HTTPEventAdaptorConstants.ENDPOINT_PREFIX + adaptorName + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + topic,
                                            new HTTPMessageServlet(adaptorName, topic, tenantId ),
                                            new Hashtable(),
                                            httpService.createDefaultHttpContext());
            } else {
                httpService.registerServlet(HTTPEventAdaptorConstants.ENDPOINT_PREFIX + HTTPEventAdaptorConstants.ENDPOINT_TENANT_KEY + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + tenantDomain + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + adaptorName + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + topic,
                                            new HTTPMessageServlet(adaptorName, topic, tenantId),
                                            new Hashtable(),
                                            httpService.createDefaultHttpContext());
            }
        } catch (ServletException e) {
            log.error(e);
        } catch (NamespaceException e) {
            log.error(e);
        }
    }

    public static void unregisterDynamicEndpoint(String adaptorName, String topic) {
        HttpService httpService = HTTPEventAdaptorServiceValueHolder.getHTTPService();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            httpService.unregister(HTTPEventAdaptorConstants.ENDPOINT_PREFIX + adaptorName + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + topic);
        } else {
            httpService.unregister(HTTPEventAdaptorConstants.ENDPOINT_PREFIX + HTTPEventAdaptorConstants.ENDPOINT_TENANT_KEY + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + tenantDomain + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + adaptorName + HTTPEventAdaptorConstants.ENDPOINT_URL_SEPARATOR + topic);
        }
    }

}
