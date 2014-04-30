/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.event.processor.storm.internal.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.storm.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.storm.QueuedEventSource;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;
import org.wso2.carbon.event.processor.storm.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.storm.internal.util.StormProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsumingQueuedEventSource implements EventConsumer, QueuedEventSource {
    private static Log log = LogFactory.getLog(ConsumingQueuedEventSource.class);
    private final StreamDefinition streamDefinition;
    private final int tenantId;
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private Logger trace = Logger.getLogger(StormProcessorConstants.EVENT_TRACE_LOGGER);
    private Object owner;
    private EventStatisticsMonitor statisticsMonitor;
    private String tracerPrefix = "";
    private ConcurrentLinkedQueue<Object[]> eventQueue;

    public ConsumingQueuedEventSource(StreamDefinition streamDefinition, ExecutionPlanConfiguration executionPlanConfiguration) {
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        this.streamDefinition = streamDefinition;
        this.owner = executionPlanConfiguration;
        this.eventQueue = new ConcurrentLinkedQueue<Object[]>();
        this.traceEnabled = executionPlanConfiguration.isTracingEnabled();
        this.statisticsEnabled = executionPlanConfiguration.isStatisticsEnabled();
        if (statisticsEnabled) {
            statisticsMonitor = StormProcessorValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, StormProcessorConstants.STORM_PROCESSOR, executionPlanConfiguration.getName(), streamDefinition.getStreamId());
        }
        if (traceEnabled) {
            this.tracerPrefix = "TenantId=" + tenantId + " : " + StormProcessorConstants.STORM_PROCESSOR + " : " + executionPlanConfiguration.getName() + "," + streamDefinition + ", before processing " + System.getProperty("line.separator");
        }
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    @Override
    public void consumeEvents(Object[][] events) {

        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(events));
        }

        for (Object[] eventData : events) {
            if (statisticsEnabled) {
                statisticsMonitor.incrementRequest();
            }
            eventQueue.offer(eventData);
        }

    }

    @Override
    public void consumeEvents(Event[] events) {
        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(events));
        }
        if (statisticsEnabled) {
            for (Object obj : events) {
                statisticsMonitor.incrementRequest();
            }
        }
        for (Event event : events) {
            eventQueue.offer(event.getData());
        }
    }

    @Override
    public void consumeEvent(Object[] eventData) {
        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(eventData));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        eventQueue.offer(eventData);
    }

    @Override
    public void consumeEvent(Event event) {
        if (traceEnabled) {
            trace.info(tracerPrefix + event);
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        eventQueue.offer(event.getData());
    }

    @Override
    public Object getOwner() {
        return owner;
    }

    @Override
    public Object[] getEvent() {
        return eventQueue.poll();
    }

    @Override
    public List<Object[]> getAllEvents() {
        List<Object[]> eventList = new ArrayList<Object[]>(StormProcessorConstants.MAX_BATCH_SIZE);
        Object[] currentEvent;
        int batchCount = 0;
        while ((currentEvent = eventQueue.poll()) != null && batchCount++ < StormProcessorConstants.MAX_BATCH_SIZE) {
            eventList.add(currentEvent);
        }

        return eventList;
    }

    @Override
    public String getStreamId() {
        return this.streamDefinition.getStreamId();
    }
}
