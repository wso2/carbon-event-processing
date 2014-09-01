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
package org.wso2.carbon.event.processor.storm.test;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.wso2.carbon.event.processor.storm.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.storm.internal.SiddhiSpout;
import org.wso2.carbon.event.processor.storm.internal.listener.ConsumingQueuedEventSource;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;

public class SiddhiSpoutTestCase {

    private static final Log log = LogFactory.getLog(SiddhiSpoutTestCase.class);
    private static final String TOPOLOGY_WORD_COUNT = "topology.word.count";
    private static boolean eventsReceived = false;
    private static int eventCount = 0;

    @Ignore
    @Test
    public void testSiddhiSpout() throws AlreadyAliveException, InvalidTopologyException, InterruptedException {
        eventsReceived = false;
        ExecutionPlanConfiguration executionPlanConfiguration = new ExecutionPlanConfiguration();
        StreamDefinition siddhiStreamDef = new StreamDefinition().name("wordStream").attribute("word", Attribute.Type.STRING);
        ConsumingQueuedEventSource eventSource = new ConsumingQueuedEventSource(siddhiStreamDef, executionPlanConfiguration);
        SiddhiSpout siddhiSpout = new SiddhiSpout(siddhiStreamDef, eventSource);
        siddhiSpout.setUseDefaultAsStreamName(false);

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("siddhi-spout", siddhiSpout);
        //builder.setBolt("count", wordCount, 12).fieldsGrouping("siddhi-spout", new Fields("word"));
        builder.setBolt("count", new WordCount(), 8).fieldsGrouping("siddhi-spout", "wordStream", new Fields("word"));

        Config conf = new Config();
        conf.setDebug(false);

        conf.setMaxTaskParallelism(3);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("word-count", conf, builder.createTopology());
        eventSource.consumeEvents(new Object[][]{{"GOOG"}, {"WSO2"}, {"FB"}});
        Thread.sleep(10000);
        Assert.assertTrue("No events received.", eventsReceived);
        Assert.assertTrue("Event count is zero", eventCount > 0);
        cluster.shutdown();
    }

    public static class WordCount extends BaseBasicBolt {
        Map<String, Integer> counts = new HashMap<String, Integer>();

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            String word = tuple.getString(0);
            if (log.isDebugEnabled()) {
                log.debug("Received event: " + tuple.toString());
            }
            eventsReceived = true;
            Integer count = counts.get(word);
            if (count == null)
                count = 0;
            count++;
            eventCount++;
            collector.emit(new Values(word, count));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word", "count"));
        }

        @Override
        public Map<String, Object> getComponentConfiguration() {
            Map<String, Object> conf = new HashMap<String, Object>();
            conf.put(TOPOLOGY_WORD_COUNT, counts);
            return conf;
        }
    }

}
