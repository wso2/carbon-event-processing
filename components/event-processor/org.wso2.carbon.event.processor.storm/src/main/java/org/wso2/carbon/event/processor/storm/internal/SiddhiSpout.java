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


package org.wso2.carbon.event.processor.storm.internal;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.storm.QueuedEventSource;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;
import org.wso2.carbon.event.processor.storm.internal.stream.EventConsumer;
import org.wso2.carbon.event.processor.storm.internal.stream.EventJunction;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SiddhiSpout extends BaseRichSpout {
    private static transient Log log = LogFactory.getLog(SiddhiSpout.class);
    private final int tenantId;
    private transient QueuedEventSource inputEventSource;
    private SpoutOutputCollector _collector;
    private List<String> attributeNames;
    private String exportedSiddhiStreamId;
    private String exportedTopLevelStreamId;
    private boolean useDefaultAsStreamName = false;

    public SiddhiSpout(StreamDefinition siddhiStreamDefinition, QueuedEventSource inputEventSource, String topLevelStreamId) {
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        this.inputEventSource = inputEventSource;
        this.exportedSiddhiStreamId = siddhiStreamDefinition.getStreamId();
        this.exportedTopLevelStreamId = topLevelStreamId;
        List<Attribute> attributeList = siddhiStreamDefinition.getAttributeList();
        this.attributeNames = new ArrayList<String>(attributeList.size());
        for (Attribute attribute : attributeList) {
            attributeNames.add(attribute.getName());
        }
    }

    public String getExportedSiddhiStreamId() {
        return exportedSiddhiStreamId;
    }

    public QueuedEventSource getInputEventSource() {
        if (inputEventSource == null) {
            EventJunction eventJunction = StormProcessorValueHolder.getStormProcessorService().getEventJunction(exportedTopLevelStreamId, tenantId);
            for (EventConsumer eventConsumer : eventJunction.getAllEventConsumers()) {
                if (eventConsumer instanceof QueuedEventSource) {
                    this.inputEventSource = (QueuedEventSource) eventConsumer;
                    break;
                }
            }
        }
        return inputEventSource;
    }

    public boolean isUseDefaultAsStreamName() {
        return useDefaultAsStreamName;
    }

    public void setUseDefaultAsStreamName(boolean useDefaultAsStreamName) {
        this.useDefaultAsStreamName = useDefaultAsStreamName;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (useDefaultAsStreamName) {
            declarer.declare(new Fields(attributeNames));
        } else {
            declarer.declareStream(exportedSiddhiStreamId, new Fields(attributeNames));
        }
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this._collector = collector;
    }

    @Override
    public void nextTuple() {
        try {
            Object[] nextEvent = getInputEventSource().getEvent();
            if (nextEvent != null) {
                if (useDefaultAsStreamName) {
                    _collector.emit(Arrays.asList(nextEvent));
                } else {
                    _collector.emit(exportedSiddhiStreamId, Arrays.asList(nextEvent));
                }
            } else {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            log.error("Thread interrupted while sleeping as a matter of courtesy for stream : " + exportedSiddhiStreamId, e);
        }
    }

}
