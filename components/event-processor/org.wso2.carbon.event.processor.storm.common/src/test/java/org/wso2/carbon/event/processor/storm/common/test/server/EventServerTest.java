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

import org.wso2.carbon.event.processor.storm.common.event.server.BinaryTransportEventServer;
import org.wso2.carbon.event.processor.storm.common.event.server.EventServerConfig;
import org.wso2.carbon.event.processor.storm.common.event.server.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.Arrays;

public class EventServerTest {


    public static void main(String[] args) throws Exception {

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
        binaryTransportEventServer.subscribe(streamDefinition);
        binaryTransportEventServer.start();

        Thread.sleep(100000);
        System.out.println("Shutting down server...");
        binaryTransportEventServer.shutdown();
    }
}
