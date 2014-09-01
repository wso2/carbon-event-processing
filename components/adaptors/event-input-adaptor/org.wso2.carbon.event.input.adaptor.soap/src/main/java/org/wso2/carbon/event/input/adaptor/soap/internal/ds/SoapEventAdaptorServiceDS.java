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
package org.wso2.carbon.event.input.adaptor.soap.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.soap.SoapEventAdaptorFactory;


/**
 * @scr.component name="input.SoapAdaptorService.component" immediate="true"
 */


public class SoapEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(SoapEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorFactory wsEventLocalEventAdaptorFactory = new SoapEventAdaptorFactory();
            context.getBundleContext().registerService(InputEventAdaptorFactory.class.getName(), wsEventLocalEventAdaptorFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input Soap adaptor service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the input Soap adaptor service ", e);
        }
    }

}
