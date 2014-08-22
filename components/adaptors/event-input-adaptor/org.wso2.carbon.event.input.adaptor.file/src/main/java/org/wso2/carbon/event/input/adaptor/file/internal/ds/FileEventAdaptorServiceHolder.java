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
package org.wso2.carbon.event.input.adaptor.file.internal.ds;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.file.internal.listener.LateStartAdaptorListener;
import org.wso2.carbon.event.input.adaptor.file.internal.util.FileEventAdaptorConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

public class FileEventAdaptorServiceHolder {


    private static ConfigurationContextService configurationContextService;
    private static List<LateStartAdaptorListener> lateStartAdaptorListeners = new ArrayList<LateStartAdaptorListener>();
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(FileEventAdaptorServiceHolder.class);

    public static void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        FileEventAdaptorServiceHolder.configurationContextService = configurationContextService;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("File input event adaptor waiting for dependent configurations to load");
                    Thread.sleep(FileEventAdaptorConstants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4);
                    loadLateStartEventAdaptors();
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    public static void unregisterConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        FileEventAdaptorServiceHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void addLateStartAdaptorListener(LateStartAdaptorListener lateStartAdaptorListener) {
        lateStartAdaptorListeners.add(lateStartAdaptorListener);
    }

    public static void loadLateStartEventAdaptors(){
        for (LateStartAdaptorListener lateStartAdaptorListener : lateStartAdaptorListeners){
            lateStartAdaptorListener.tryStartAdaptor();
        }
    }


}
