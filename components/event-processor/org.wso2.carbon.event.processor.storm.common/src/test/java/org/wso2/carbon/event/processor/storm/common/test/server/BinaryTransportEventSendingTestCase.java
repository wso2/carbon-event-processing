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

public class BinaryTransportEventSendingTestCase {

    @Test
    public void testEventSendingToServer() {

        StreamDefinition streamDefinition = new StreamDefinition().name("TestStream")
                .attribute("att1", Attribute.Type.INT)
                .attribute("att2", Attribute.Type.FLOAT)
                .attribute("att3", Attribute.Type.STRING)
                .attribute("att4", Attribute.Type.INT);

        BinaryTransportEventServer binaryTransportEventServer = new BinaryTransportEventServer(new EventServerConfig(7612), new StreamCallback() {
            @Override
            public void receive(String streamId, Object[] event) {
                System.out.println("Stream ID: " + streamId);
                System.out.println("Event: " + Arrays.deepToString(event));
            }
        });
        try {
            binaryTransportEventServer.subscribe(streamDefinition);
            binaryTransportEventServer.start();
            Thread.sleep(1000);
            sendEventsViaClient();
            Thread.sleep(5000);
            System.out.println("Shutting down server...");
            binaryTransportEventServer.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendEventsViaClient() {
        StreamDefinition streamDefinition = new StreamDefinition().name("TestStream")
                .attribute("att1", Attribute.Type.INT)
                .attribute("att2", Attribute.Type.FLOAT)
                .attribute("att3", Attribute.Type.STRING)
                .attribute("att4", Attribute.Type.INT);

        EventClient eventClient;
        try {
            eventClient = new EventClient("localhost:7612");
            eventClient.addStreamDefinition(streamDefinition);
            Thread.sleep(1000);
            System.out.println("Start testing");
            Random random = new Random();

            for (int i = 0; i < 100; i++) {
                eventClient.sendEvent(streamDefinition.getStreamId(), new Object[]{random.nextInt(), random.nextFloat(), "Abcdefghijklmnop" + random.nextLong(), random.nextInt()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
