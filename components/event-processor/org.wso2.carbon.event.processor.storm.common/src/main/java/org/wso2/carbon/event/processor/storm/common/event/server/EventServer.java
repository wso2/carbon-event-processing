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

package org.wso2.carbon.event.processor.storm.common.event.server;

import org.apache.log4j.Logger;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO: Improve event server to handle multiple event streams
public class EventServer {
    private static Logger log = Logger.getLogger(EventServer.class);
    private EventServerConfig eventServerConfig = new EventServerConfig(7211);
    private StreamDefinition streamDefinition;
    private StreamCallback streamCallback;
    private ExecutorService pool;
    private StreamRuntimeInfo streamRuntimeInfo;

    public EventServer(EventServerConfig eventServerConfig, StreamDefinition streamDefinition, StreamCallback streamCallback) {
        this.eventServerConfig = eventServerConfig;
        this.streamDefinition = streamDefinition;
        this.streamCallback = streamCallback;
        this.streamRuntimeInfo = EventServerUtils.createStreamRuntimeInfo(streamDefinition);
        this.pool = Executors.newFixedThreadPool(eventServerConfig.getNumberOfThreads());
    }


    public void start() throws IOException {
        log.info("Starting event listener on " + eventServerConfig.getPort());
        ServerSocket receiverSocket = new ServerSocket(eventServerConfig.getPort());
        while (true) {
            try {
                final Socket connectionSocket = receiverSocket.accept();
                pool.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            BufferedInputStream in = new BufferedInputStream(connectionSocket.getInputStream());

                            while (true) {
                                int streamNameSize = loadData(in) & 0xff;
                                byte[] streamNameData = loadData(in, new byte[streamNameSize]);
//                                System.out.println(new String(streamNameData, 0, streamNameData.length));

                                Object[] event = new Object[streamRuntimeInfo.getNoOfAttributes()];
                                byte[] fixedMessageData = loadData(in, new byte[streamRuntimeInfo.getFixedMessageSize()]);

                                ByteBuffer bbuf = ByteBuffer.wrap(fixedMessageData, 0, fixedMessageData.length);
                                Attribute.Type[] attributeTypes = streamRuntimeInfo.getAttributeTypes();
                                for (int i = 0; i < attributeTypes.length; i++) {
                                    Attribute.Type type = attributeTypes[i];
                                    switch (type) {
                                        case INT:
                                            event[i] = bbuf.getInt();
                                            continue;
                                        case LONG:
                                            event[i] = bbuf.getLong();
                                            continue;
                                        case BOOL:
                                            event[i] = bbuf.get() == 1;
                                            continue;
                                        case FLOAT:
                                            event[i] = bbuf.getFloat();
                                            continue;
                                        case DOUBLE:
                                            event[i] = bbuf.getLong();
                                            continue;
                                        case STRING:
                                            int size = bbuf.getShort() & 0xffff;
                                            byte[] stringData = loadData(in, new byte[size]);
                                            event[i] = new String(stringData, 0, stringData.length);
                                    }
                                }

                                streamCallback.receive(event);
                            }
                        } catch (IOException e) {
                            log.error("Error reading data from receiver socket:" + e.getMessage(), e);
                        }
                    }
                });
            } catch (Throwable e) {
                log.error("Error while listening for data on socket: " + e.getMessage(), e);
            }
        }

    }

    private int loadData(BufferedInputStream in) throws IOException {

        while (true) {
            int byteData = in.read();
            if (byteData != -1) {
                return byteData;
            }
        }
    }

    private byte[] loadData(BufferedInputStream in, byte[] dataArray) throws IOException {

        int start = 0;
        while (true) {
            int readCount = in.read(dataArray, 0, dataArray.length - start);
            start += readCount;
            if (start == dataArray.length) {
                return dataArray;
            }
        }
    }
}
