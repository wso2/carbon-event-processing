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
package org.wso2.carbon.event.processor.core.internal.listener;

import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.manager.core.EventProducer;
import org.wso2.carbon.event.stream.manager.core.EventProducerCallback;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.util.Arrays;

public class SiddhiOutputStreamListener extends StreamCallback implements EventProducer {
    protected final String siddhiStreamName;
    protected final int tenantId;
    protected final boolean traceEnabled;
    protected final boolean statisticsEnabled;
    private final String streamId;
    protected EventStatisticsMonitor statisticsMonitor;
    protected String tracerPrefix;
    protected EventProducerCallback eventProducerCallback;
    private Logger trace = Logger.getLogger(EventProcessorConstants.EVENT_TRACE_LOGGER);

    public SiddhiOutputStreamListener(String siddhiStreamName, String streamId, ExecutionPlanConfiguration executionPlanConfiguration, int tenantId) {
        this.streamId = streamId;
        this.tenantId = tenantId;
        this.siddhiStreamName = siddhiStreamName;
        this.traceEnabled = executionPlanConfiguration.isTracingEnabled();
        this.statisticsEnabled = executionPlanConfiguration.isStatisticsEnabled();
        if (statisticsEnabled) {
            statisticsMonitor = EventProcessorValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventProcessorConstants.EVENT_PROCESSOR, executionPlanConfiguration.getName(), streamId + " (" + siddhiStreamName + ")");
        }
        if (traceEnabled) {
            this.tracerPrefix = "TenantId=" + tenantId + " : " + EventProcessorConstants.EVENT_PROCESSOR + " : " + executionPlanConfiguration.getName() + "," + streamId + " (" + siddhiStreamName + "), after processing " + System.getProperty("line.separator");
        }
    }

    @Override
    public void receive(Event[] events) {
        try {
            /**
             * Setting tenant id here because sometimes Siddhi creates its own threads, which does not
             * have tenant information initialized. These method calls can be a performance hit,
             * which needs to be profiled properly. Please update this comment one day after the
             * profiling is done properly.
             */
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(this.tenantId);

            if (traceEnabled) {
                trace.info(tracerPrefix + Arrays.deepToString(events));
            }
            if (statisticsEnabled) {
                for (Object obj : events) {
                    statisticsMonitor.incrementResponse();
                }
            }
            eventProducerCallback.sendEvents(events);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void sendEventData(Object[] eventData){
        try {
            /**
             * Setting tenant id here because sometimes Siddhi creates its own threads, which does not
             * have tenant information initialized. These method calls can be a performance hit,
             * which needs to be profiled properly. Please update this comment one day after the
             * profiling is done properly.
             */
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            privilegedCarbonContext.setTenantId(this.tenantId);

            if (traceEnabled) {
                trace.info(tracerPrefix + eventData);
            }
            if (statisticsEnabled) {
                statisticsMonitor.incrementResponse();
            }
            eventProducerCallback.sendEventData(eventData);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public void setCallBack(EventProducerCallback eventProducerCallback) {
        this.eventProducerCallback = eventProducerCallback;
    }
}
