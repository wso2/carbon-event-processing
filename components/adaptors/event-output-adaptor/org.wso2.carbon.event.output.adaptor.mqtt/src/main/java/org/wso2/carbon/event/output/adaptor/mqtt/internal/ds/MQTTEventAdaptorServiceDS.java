/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.output.adaptor.mqtt.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.mqtt.MQTTEventAdaptorFactory;


/**
 * @scr.component name="output.mqttEventAdaptorService.component" immediate="true"
 */


public class MQTTEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(MQTTEventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdaptorFactory mqttEventAdaptorFactory = new MQTTEventAdaptorFactory();
            context.getBundleContext().registerService(OutputEventAdaptorFactory.class.getName(), mqttEventAdaptorFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output MQTT event adaptor service ");
            }
        } catch (RuntimeException e) {
            log.error("Can not deployed the output MQTT event adaptor service ", e);
        }
    }

}
