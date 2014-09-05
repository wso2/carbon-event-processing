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

package org.wso2.event.processor.core.test.event.server;

import org.wso2.carbon.event.processor.core.internal.event.server.EventServer;
import org.wso2.carbon.event.processor.core.internal.event.server.EventServerConfig;
import org.wso2.carbon.event.processor.core.internal.event.server.StreamCallback;
import org.wso2.carbon.event.processor.core.internal.event.server.StreamDefinition;

import java.util.Arrays;

public class EventServerTest {


    public static void main(String[] args) throws Exception {

        StreamDefinition streamDefinition = new StreamDefinition();
        streamDefinition.setStreamId("TestStream");
        streamDefinition.addAttribute("att1", StreamDefinition.Type.INTEGER);
        streamDefinition.addAttribute("att2", StreamDefinition.Type.FLOAT);
        streamDefinition.addAttribute("att3", StreamDefinition.Type.STRING);
        streamDefinition.addAttribute("att4", StreamDefinition.Type.INTEGER);

        EventServer eventServer = new EventServer(new EventServerConfig(7612), streamDefinition, new StreamCallback() {
            @Override
            public void receive(Object[] event) {
                System.out.println(Arrays.deepToString(event));
            }
        });

        eventServer.start();

        Thread.sleep(10000);
    }
}
