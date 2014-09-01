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
package org.wso2.carbon.event.input.adaptor.email.internal.ds;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.email.internal.LateStartAdaptorListener;
import org.wso2.carbon.event.input.adaptor.email.internal.util.EmailEventAdaptorConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

/**
 * common place to hold some OSGI bundle references.
 */
public final class EmailEventAdaptorServiceValueHolder {

    private static ConfigurationContextService configurationContextService;
    private static List<LateStartAdaptorListener> lateStartAdaptorListeners = new ArrayList<LateStartAdaptorListener>();
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(EmailEventAdaptorServiceDS.class);

    private EmailEventAdaptorServiceValueHolder() {
    }

    public static void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EmailEventAdaptorServiceValueHolder.configurationContextService = configurationContextService;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Email input event adaptor waiting for dependent configurations to load");
                    Thread.sleep(EmailEventAdaptorConstants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4);
                    loadLateStartEventAdaptors();
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    public static void unregisterConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EmailEventAdaptorServiceValueHolder.configurationContextService = configurationContextService;
    }

    public static void addLateStartAdaptorListener(
            LateStartAdaptorListener lateStartAdaptorListener) {
        lateStartAdaptorListeners.add(lateStartAdaptorListener);
    }

    public static void loadLateStartEventAdaptors() {
        for (LateStartAdaptorListener lateStartAdaptorListener : lateStartAdaptorListeners) {
            lateStartAdaptorListener.tryStartAdaptor();
        }
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return EmailEventAdaptorServiceValueHolder.configurationContextService;
    }

}
