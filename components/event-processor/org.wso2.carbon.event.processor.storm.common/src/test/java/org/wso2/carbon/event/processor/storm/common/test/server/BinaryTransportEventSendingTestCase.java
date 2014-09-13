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

package org.wso2.carbon.event.processor.storm.common.test.server;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.event.processor.storm.common.event.client.EventClient;
import org.wso2.carbon.event.processor.storm.common.event.server.BinaryTransportEventServer;
import org.wso2.carbon.event.processor.storm.common.event.server.EventServerConfig;
import org.wso2.carbon.event.processor.storm.common.event.server.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class BinaryTransportEventSendingTestCase {
    private static final Log log = LogFactory.getLog(BinaryTransportEventSendingTestCase.class);

    public static final int EVENTS_PER_CLIENT = 10;
    public static final int TOTAL_CLIENTS = 15;

    private ExecutorService threadPool;

    @Before
    public void initialize() {
        threadPool = Executors.newFixedThreadPool(20);
    }

    @Test
    public void testEventSendingToServer() {

        StreamDefinition streamDefinition = new StreamDefinition().name("TestStream")
                .attribute("att1", Attribute.Type.INT)
                .attribute("att2", Attribute.Type.FLOAT)
                .attribute("att3", Attribute.Type.STRING)
                .attribute("att4", Attribute.Type.INT);

        TestStreamCallback streamCallback = new TestStreamCallback();
        BinaryTransportEventServer binaryTransportEventServer = new BinaryTransportEventServer(new EventServerConfig(7612), streamCallback);
        try {
            binaryTransportEventServer.subscribe(streamDefinition);
            binaryTransportEventServer.start();
            Thread.sleep(1000);
            for (int i = 0; i < TOTAL_CLIENTS; i++) {
                threadPool.submit(new ClientThread());
            }
            while (streamCallback.getEventCount() < TOTAL_CLIENTS * EVENTS_PER_CLIENT) {
                Thread.sleep(5000);
            }
            Assert.assertEquals(TOTAL_CLIENTS * EVENTS_PER_CLIENT, streamCallback.getEventCount());
            log.info("Shutting down server...");
            binaryTransportEventServer.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class TestStreamCallback implements StreamCallback {
        AtomicInteger eventCount = new AtomicInteger();

        @Override
        public void receive(String streamId, Object[] event) {
            log.info("Event count:" + eventCount.incrementAndGet() + ", Stream ID: " + streamId + ", Event: " + Arrays.deepToString(event));
        }

        public int getEventCount() {
            return eventCount.get();
        }
    }

    private class ClientThread implements Runnable {

        @Override
        public void run() {
            StreamDefinition streamDefinition = new StreamDefinition().name("TestStream")
                    .attribute("att1", Attribute.Type.INT)
                    .attribute("att2", Attribute.Type.FLOAT)
                    .attribute("att3", Attribute.Type.STRING)
                    .attribute("att4", Attribute.Type.INT);

            EventClient eventClient = null;
            try {
                eventClient = new EventClient("localhost:7612");
                eventClient.addStreamDefinition(streamDefinition);
                Thread.sleep(1000);
                log.info("Starting event client to send events to localhost:7612");
                Random random = new Random();

                for (int i = 0; i < EVENTS_PER_CLIENT; i++) {
                    eventClient.sendEvent(streamDefinition.getStreamId(), new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(eventClient != null) {
                    eventClient.close();
                }
            }
        }
    }
}
