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
package org.wso2.carbon.event.output.adaptor.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.core.internal.CarbonOutputEventAdaptorService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="output.event.adaptor.service.tracker.component" immediate="true"
 * @scr.reference name="output.event.adaptor.tracker.service"
 * interface="org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory" cardinality="0..n"
 * policy="dynamic" bind="setEventAdaptorType" unbind="unSetEventAdaptorType"
 */
public class OutputEventAdaptorServiceTrackerDS {

    private static final Log log = LogFactory.getLog(OutputEventAdaptorServiceTrackerDS.class);
    public static List<OutputEventAdaptorFactory> outputEventAdaptorFactories = new ArrayList<OutputEventAdaptorFactory>();

    /**
     * initialize the Event Adaptor Manager core service here.
     *
     * @param context the component context that will be passed in from the OSGi environment at activation
     */
    protected void activate(ComponentContext context) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output event adaptor tracker service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output event adaptor tracker service ", e);
        }
    }

    protected void setEventAdaptorType(
            OutputEventAdaptorFactory outputEventAdaptorFactory) {
        try {
            if (OutputEventAdaptorServiceValueHolder.getCarbonOutputEventAdaptorService() != null) {
                ((CarbonOutputEventAdaptorService) OutputEventAdaptorServiceValueHolder.getCarbonOutputEventAdaptorService()).registerEventAdaptor(outputEventAdaptorFactory.getEventAdaptor());
            } else {
                outputEventAdaptorFactories.add(outputEventAdaptorFactory);
            }
        } catch (Throwable t) {
            String outputEventAdaptorFactoryClassName = "Unknown";
            if (outputEventAdaptorFactory != null) {
                outputEventAdaptorFactoryClassName = outputEventAdaptorFactory.getClass().getName();
            }
            log.error("Unexpected error at initializing output event adaptor factory "
                    + outputEventAdaptorFactoryClassName + ": " + t.getMessage(), t);
        }
    }

    protected void unSetEventAdaptorType(
            OutputEventAdaptorFactory outputEventAdaptorFactory) {
        ((CarbonOutputEventAdaptorService) OutputEventAdaptorServiceValueHolder.getCarbonOutputEventAdaptorService()).unRegisterEventAdaptor(outputEventAdaptorFactory.getEventAdaptor());
    }
}

