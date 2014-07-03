package org.wso2.carbon.event.processor.core.internal.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.manager.core.SiddhiEventConsumer;
import org.wso2.siddhi.core.event.Event;

import java.util.Arrays;

/**
 * Abstract class for classes which feeds incoming events to Siddhi.
 */
public abstract class AbstractSiddhiInputEventDispatcher implements SiddhiEventConsumer {
    private Logger trace = Logger.getLogger(EventProcessorConstants.EVENT_TRACE_LOGGER);
    private static Log log = LogFactory.getLog(AbstractSiddhiInputEventDispatcher.class);

    protected final String streamId;
    protected String siddhiStreamId;
    protected Object owner;
    protected final int tenantId;
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private EventStatisticsMonitor statisticsMonitor;
    private String tracerPrefix = "";

    public AbstractSiddhiInputEventDispatcher(String streamId, String siddhiStreamId, ExecutionPlanConfiguration executionPlanConfiguration, int tenantId) {
        this.streamId = streamId;
        this.siddhiStreamId = siddhiStreamId;
        this.owner = executionPlanConfiguration;
        this.tenantId = tenantId;
        this.traceEnabled = executionPlanConfiguration.isTracingEnabled();
        this.statisticsEnabled = executionPlanConfiguration.isStatisticsEnabled();
        if (statisticsEnabled) {
            statisticsMonitor = EventProcessorValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventProcessorConstants.EVENT_PROCESSOR, executionPlanConfiguration.getName(), streamId + " (" + siddhiStreamId + ")");
        }
        if (traceEnabled) {
            this.tracerPrefix = "TenantId=" + tenantId + " : " + EventProcessorConstants.EVENT_PROCESSOR + " : " + executionPlanConfiguration.getName() + "," + streamId + " (" + siddhiStreamId + "), before processing " + System.getProperty("line.separator");
        }
    }

    @Override
    public String getStreamId() {
        return streamId;
    }

    @Override
    public void consumeEvents(Event[] events) {
        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(events));
        }
        for (Event event : events) {
            try {
                if (statisticsEnabled) {
                    statisticsMonitor.incrementRequest();
                }
                sendEvent(event.getData());
            } catch (InterruptedException e) {
                log.error("Error in dispatching events " + Arrays.deepToString(events) + " to Siddhi stream :" + siddhiStreamId);
            }
        }
    }

    @Override
    public void consumeEventData(Object[] eventData) {
        try {
            if (traceEnabled) {
                trace.info(tracerPrefix + Arrays.deepToString(eventData));
            }
            if (statisticsEnabled) {
                statisticsMonitor.incrementRequest();
            }
            sendEvent(eventData);
        } catch (InterruptedException e) {
            log.error("Error in dispatching event data " + Arrays.deepToString(eventData) + " to Siddhi stream :" + siddhiStreamId);
        }
    }

    public String getExecutionPlanName(){
        return ((ExecutionPlanConfiguration)owner).getName();
    }

    /**
     * When an event is received this method will be called. Implement how the event must be dispatched to Siddhi
     * @param event Event object
     * @throws InterruptedException
     */
    public abstract void sendEvent(Event event) throws InterruptedException;

    /**
     * When an event is received this method will be called. Implement how the event must be dispatched to Siddhi
     * @param event Event data
     * @throws InterruptedException
     */
    public abstract void sendEvent(Object[] eventData) throws InterruptedException;
}
