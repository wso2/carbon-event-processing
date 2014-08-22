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
package org.wso2.carbon.event.processor.core.internal.ha;

import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.siddhi.core.event.Event;

import java.util.Arrays;

public class SiddhiHAOutputStreamListener extends SiddhiOutputStreamListener {
    private Logger trace = Logger.getLogger(EventProcessorConstants.EVENT_TRACE_LOGGER);
    private String dropped = " (Dropped)";
    private volatile boolean isDrop;

    public SiddhiHAOutputStreamListener(String siddhiStreamName, String streamId, ExecutionPlanConfiguration executionPlanConfiguration, int tenantId) {
        super(siddhiStreamName, streamId, executionPlanConfiguration, tenantId);
    }

    public boolean isDrop() {
        return isDrop;
    }

    public void setDrop(boolean isDrop) {
        this.isDrop = isDrop;
    }

    @Override
    public void receive(Event[] events) {
        if (isDrop) {
            if (traceEnabled) {
                trace.info(tracerPrefix + Arrays.deepToString(events) + dropped);
            }
            if (statisticsEnabled) {
                for (Object obj : events) {
                    statisticsMonitor.incrementResponse();
                }
            }
        } else {
            if (traceEnabled) {
                trace.info(tracerPrefix + Arrays.deepToString(events));
            }
            if (statisticsEnabled) {
                for (Object obj : events) {
                    statisticsMonitor.incrementResponse();
                }
            }
            /**
             * Setting tenant id here because sometimes Siddhi creates its own threads, which does not
             * have tenant information initialized. These method calls can be a performance hit,
             * which needs to be profiled properly. Please update this comment one day after the
             * profiling is done properly.
             */
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(this.tenantId);
            eventProducerCallback.sendEvents(events);
        }


    }


}
