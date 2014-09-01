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
package org.wso2.carbon.event.processor.storm.internal.listener;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.storm.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;
import org.wso2.carbon.event.processor.storm.internal.stream.EventJunction;
import org.wso2.carbon.event.processor.storm.internal.stream.EventProducer;
import org.wso2.carbon.event.processor.storm.internal.util.StormProcessorConstants;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;

import java.util.Arrays;

public class StormTerminalBolt extends BaseBasicBolt implements EventProducer {
    private final int tenantId;
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    private transient Logger trace = Logger.getLogger(StormProcessorConstants.EVENT_TRACE_LOGGER);
    private transient EventJunction eventJunction;
    private transient EventStatisticsMonitor statisticsMonitor;
    private String tracerPrefix;
    private String siddhiStreamId;
    private String streamId;
    private String executionPlanName;

    public StormTerminalBolt(String siddhiStreamId, String streamId, ExecutionPlanConfiguration executionPlanConfiguration) {
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        this.traceEnabled = executionPlanConfiguration.isTracingEnabled();
        this.statisticsEnabled = executionPlanConfiguration.isStatisticsEnabled();
        this.executionPlanName = executionPlanConfiguration.getName();
        this.siddhiStreamId = siddhiStreamId;
        this.streamId = streamId;
        init();
    }

    public String getStreamId() {
        return streamId;
    }

    public String getSiddhiStreamId() {
        return siddhiStreamId;
    }

    private void init() {
        this.eventJunction = StormProcessorValueHolder.getStormProcessorService().getEventJunction(this.streamId, this.tenantId);
        if (statisticsEnabled) {
            statisticsMonitor = StormProcessorValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, StormProcessorConstants.STORM_PROCESSOR,
                    this.executionPlanName, eventJunction.getStreamDefinition().getStreamId());
        }
        if (traceEnabled) {
            trace = Logger.getLogger(StormProcessorConstants.EVENT_TRACE_LOGGER);
            this.tracerPrefix = "TenantId=" + tenantId + " : " + StormProcessorConstants.STORM_PROCESSOR + " : " + this.executionPlanName
                    + "," + eventJunction.getStreamDefinition().getStreamId() + ", after processing " + System.getProperty("line.separator");
        }
    }

    @Override
    public Object getOwner() {
        if (eventJunction == null) {
            init();
        }
        return eventJunction;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        //We are not publishing anything back to storm. So ignore this method implementation.
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        if (eventJunction == null) {
            init();
        }
        if (traceEnabled) {
            trace.info(tracerPrefix + Arrays.deepToString(tuple.getValues().toArray()));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementResponse();
        }
        /**
         * Setting tenant id here because sometimes Siddhi creates its own threads, which does not
         * have tenant information initialized. These method calls can be a performance hit,
         * which needs to be profiled properly. Please update this comment one day after the
         * profiling is done properly.
         */
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(this.tenantId);
        eventJunction.dispatchEvent(tuple.getValues().toArray());
    }
}
