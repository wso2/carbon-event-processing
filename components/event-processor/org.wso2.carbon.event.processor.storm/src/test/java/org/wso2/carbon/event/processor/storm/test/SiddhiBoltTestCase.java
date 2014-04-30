/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.storm.test;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.event.processor.storm.exception.StormConfigurationException;
import org.wso2.carbon.event.processor.storm.internal.SiddhiBolt;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class SiddhiBoltTestCase {

    private static final Log log = LogFactory.getLog(SiddhiBoltTestCase.class);
    private static final String TOPOLOGY_SENTENCE_COUNT = "topology.word.count";

    private static boolean eventsReceived = false;
    private static AtomicInteger eventCount = new AtomicInteger(0);

    @Test
    public void testSiddhiBolt() throws AlreadyAliveException, InvalidTopologyException, InterruptedException, StormConfigurationException {
        //TODO A storm manager is needed with the tight coupling of Siddhi API and Siddhi Bolt.
        eventsReceived = false;
        eventCount.set(0);
        String executionPlan = "from inputSentences select * insert into outStream";
        String[] exportedStreams = new String[]{"outStream"};
        String siddhiStreamDef = "define stream inputSentences (sentence string)";
        SiddhiBolt siddhiBolt = new SiddhiBolt();
        siddhiBolt.setUseDefaultAsStreamName(false);
        GenericConsumer genericConsumer = new GenericConsumer();

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("test-spout", new RandomSentenceSpout(), 2);
        builder.setBolt("siddhi-bolt", siddhiBolt, 3).fieldsGrouping("test-spout", "inputSentences", new Fields("sentence"));
        builder.setBolt("test-consumer", genericConsumer, 1).shuffleGrouping("siddhi-bolt", "outStream");

        Config conf = new Config();
        conf.setDebug(false);

        conf.setMaxTaskParallelism(3);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("sentence-count", conf, builder.createTopology());
        Thread.sleep(10000);
        Assert.assertTrue("Events not received!", eventsReceived);
        Assert.assertTrue("Count is zero!", eventCount.get() > 0);
        cluster.shutdown();

//        cluster.shutdown();
    }

    public static class RandomSentenceSpout extends BaseRichSpout {
        SpoutOutputCollector _collector;
        Random _rand;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            _collector = collector;
            _rand = new Random();
        }

        @Override
        public void nextTuple() {
            Utils.sleep(100);
            String[] sentences = new String[]{"the cow jumped over the moon", "an apple a day keeps the doctor away",
                    "four score and seven years ago", "snow white and the seven dwarfs", "i am at two with nature"};
            String sentence = sentences[_rand.nextInt(sentences.length)];
            _collector.emit("inputSentences", new Values(sentence));
        }

        @Override
        public void ack(Object id) {
        }

        @Override
        public void fail(Object id) {
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declareStream("inputSentences", new Fields("sentence"));
        }

    }

    public static class GenericConsumer extends BaseBasicBolt {
        Map<String, Integer> counts = new HashMap<String, Integer>();

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            String event = tuple.getString(0);
            if (event != null) {
                eventsReceived = true;
                eventCount.incrementAndGet();
            }
            counts.put(event, eventCount.get());
            collector.emit(new Values(event, eventCount.get()));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("sentence", "count"));
        }

        @Override
        public Map<String, Object> getComponentConfiguration() {
            Map<String, Object> conf = new HashMap<String, Object>();
            conf.put(TOPOLOGY_SENTENCE_COUNT, counts);
            return conf;
        }
    }


}
