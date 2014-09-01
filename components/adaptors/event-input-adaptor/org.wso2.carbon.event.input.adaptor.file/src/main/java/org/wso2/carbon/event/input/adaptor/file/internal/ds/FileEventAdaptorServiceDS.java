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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.file.FileEventAdaptorFactory;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="input.FileEventAdaptorService.component" immediate="true"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */


public class FileEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(FileEventAdaptorServiceDS.class);

    /**
     * initialize the file adaptor service
     *
     * @param context
     */
    protected void activate(ComponentContext context) {
        try {
            InputEventAdaptorFactory testInEventAdaptorFactory = new FileEventAdaptorFactory();
            context.getBundleContext().registerService(InputEventAdaptorFactory.class.getName(), testInEventAdaptorFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the TailFile input event adaptor service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the TailFile input event adaptor service ", e);
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        FileEventAdaptorServiceHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        FileEventAdaptorServiceHolder.unregisterConfigurationContextService(configurationContextService);
    }

}
