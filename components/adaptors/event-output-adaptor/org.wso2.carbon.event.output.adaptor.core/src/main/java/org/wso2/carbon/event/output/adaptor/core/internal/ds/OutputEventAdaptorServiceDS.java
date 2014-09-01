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
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.internal.CarbonOutputEventAdaptorService;
import org.wso2.carbon.event.statistics.EventStatisticsService;

import java.util.List;


/**
 * @scr.component name="output.event.adaptor.service.component" immediate="true"
 * @scr.reference name="eventStatistics.service"
 * interface="org.wso2.carbon.event.statistics.EventStatisticsService" cardinality="1..1"
 * policy="dynamic" bind="setEventStatisticsService" unbind="unsetEventStatisticsService"
 */
public class OutputEventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(OutputEventAdaptorServiceDS.class);

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdaptorService outputEventAdaptorService = new CarbonOutputEventAdaptorService();
            OutputEventAdaptorServiceValueHolder.registerCarbonEventService(outputEventAdaptorService);

            context.getBundleContext().registerService(OutputEventAdaptorService.class.getName(), outputEventAdaptorService, null);
            OutputEventAdaptorServiceValueHolder.registerComponentContext(context);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output event adaptor service");
            }
            registerEventAdaptorTypes();
        } catch (RuntimeException e) {
            log.error("Can not create output event adaptor service ", e);
        }
    }


    public void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        OutputEventAdaptorServiceValueHolder.registerEventStatisticsService(eventStatisticsService);
    }

    public void unsetEventStatisticsService(EventStatisticsService eventStatisticsService) {
        OutputEventAdaptorServiceValueHolder.registerEventStatisticsService(null);
    }

    private void registerEventAdaptorTypes() {

        List<OutputEventAdaptorFactory> outputEventAdaptorFactories = OutputEventAdaptorServiceTrackerDS.outputEventAdaptorFactories;

        for (OutputEventAdaptorFactory outputEventAdaptorFactory : outputEventAdaptorFactories) {
            ((CarbonOutputEventAdaptorService) OutputEventAdaptorServiceValueHolder.getCarbonOutputEventAdaptorService()).registerEventAdaptor(outputEventAdaptorFactory.getEventAdaptor());
        }
    }


}
