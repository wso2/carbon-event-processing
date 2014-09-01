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
package org.wso2.carbon.event.input.adaptor.kafka;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerKafkaAdaptor {
    private final ConsumerConnector consumer;
    private final String topic;
    private ExecutorService executor;

    public ConsumerKafkaAdaptor(String a_topic,
                                ConsumerConfig conf) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(conf);
        this.topic = a_topic;
    }

    public void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void run(int a_numThreads, InputEventAdaptorListener a_brokerListener) {
        try {
            Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
            topicCountMap.put(topic, a_numThreads);
            Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
            List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

            // now launch all the threads
            //
            executor = Executors.newFixedThreadPool(a_numThreads);

            // now create an object to consume the messages
            //
            for (final KafkaStream stream : streams) {

                executor.submit(new KafkaConsumer(stream, a_brokerListener));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}