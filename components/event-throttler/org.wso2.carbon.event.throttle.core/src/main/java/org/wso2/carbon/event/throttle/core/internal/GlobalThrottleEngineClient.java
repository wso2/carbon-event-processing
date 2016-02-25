/*
 * Copyright (c) 20016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.throttle.core.internal;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.throttle.core.exception.ThrottleConfigurationException;

import java.rmi.RemoteException;

public class GlobalThrottleEngineClient {
    private AuthenticationAdminStub authenticationAdminStub = null;
    private static final Logger log = Logger.getLogger(GlobalThrottleEngineClient.class);

    private String login(GlobalThrottleEngineConfig globalThrottleEngineConfig) throws RemoteException, LoginAuthenticationExceptionException {
        authenticationAdminStub = new AuthenticationAdminStub("https://" + globalThrottleEngineConfig.getHostname() + ":" + globalThrottleEngineConfig
                .getHTTPSPort() + "/services/AuthenticationAdmin");
        String sessionCookie = null;

        if (authenticationAdminStub.login(globalThrottleEngineConfig.getUsername(), globalThrottleEngineConfig.getPassword(), globalThrottleEngineConfig.getHostname())) {
            ServiceContext serviceContext = authenticationAdminStub._getServiceClient().getLastOperationContext()
                    .getServiceContext();
            sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        }
        return sessionCookie;
    }

    private void deploy(String sessionCookie, String executionPlan, GlobalThrottleEngineConfig globalThrottleEngineConfig) throws RemoteException {
        ServiceClient serviceClient;
        Options options;

        EventProcessorAdminServiceStub eventProcessorAdminServiceStub = new EventProcessorAdminServiceStub("https://"
                + globalThrottleEngineConfig.getHostname() + ":" + globalThrottleEngineConfig.getHTTPSPort() + "/services/EventProcessorAdminService");
        serviceClient = eventProcessorAdminServiceStub._getServiceClient();
        options = serviceClient.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        eventProcessorAdminServiceStub.deployExecutionPlan(executionPlan);
    }


    private void logout() throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }


    public void deployExecutionPlan(String executionPlan, GlobalThrottleEngineConfig globalThrottleEngineConfig) throws ThrottleConfigurationException {
        try {
            String sessionID = login(globalThrottleEngineConfig);
            deploy(sessionID, executionPlan, globalThrottleEngineConfig);
            logout();
        } catch (RemoteException e) {
            throw new ThrottleConfigurationException("Error in deploying policy \n" + executionPlan + "\nin global " +
                    "throttling engine",e);
        } catch (LogoutAuthenticationExceptionException e) {
            throw new ThrottleConfigurationException("Error in deploying policy \n" + executionPlan + "\nin global " +
                    "throttling engine",e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new ThrottleConfigurationException("Error in deploying policy \n" + executionPlan + "\nin global " +
                    "throttling engine",e);
        }
    }
}
