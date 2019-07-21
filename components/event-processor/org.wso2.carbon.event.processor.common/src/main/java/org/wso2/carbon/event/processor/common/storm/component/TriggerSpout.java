package org.wso2.carbon.event.processor.common.storm.component;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Spout to hold a siddhi trigger. There will be one instance of this class per trigger.
 *
 */
public class TriggerSpout extends BaseRichSpout {
    public static final String TRIGGER_TIME_FIELD_NAME = "triggered_time";
    String triggerId;
    String triggerDefinition;
    SpoutOutputCollector outputCollector;
    private transient SiddhiManager siddhiManager;
    private transient ExecutionPlanRuntime executionPlanRuntime;
    private static transient Log log = LogFactory.getLog(TriggerSpout.class);
    String logPrefix;

    public  TriggerSpout(String triggerId, String triggerDefinition, String executionPlanName, int tenantId){
        this.triggerId  = triggerId;
        this.triggerDefinition = triggerDefinition;
        this.logPrefix = "[" + tenantId + ":" + executionPlanName + ":" + triggerId + "] ";
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        List<String> list = new ArrayList<String>();
        list.add(0,"_timestamp");
        list.add(0, TRIGGER_TIME_FIELD_NAME);
        Fields fields = new Fields(list);

        outputFieldsDeclarer.declareStream(triggerId, fields);
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, final SpoutOutputCollector spoutOutputCollector) {
        this.outputCollector = spoutOutputCollector;

        siddhiManager = new SiddhiManager();
        String fullQueryExpression = triggerDefinition;
        executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(fullQueryExpression);

        executionPlanRuntime.addCallback(triggerId, new StreamCallback() {

                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        Object[] eventData = Arrays.copyOf(event.getData(), event.getData().length + 1);
                        eventData[event.getData().length] = event.getTimestamp();
                        outputCollector.emit(triggerId, Arrays.asList(eventData));

                        if (log.isDebugEnabled()) {
                            log.debug(logPrefix + "Trigger Event Emitted :" + Arrays.deepToString(eventData));
                        }
                    }
                }
            });
        executionPlanRuntime.start();
    }

    @Override
    public void nextTuple() {

    }
}
