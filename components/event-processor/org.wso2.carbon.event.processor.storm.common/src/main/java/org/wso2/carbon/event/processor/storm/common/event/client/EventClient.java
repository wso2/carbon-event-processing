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

package org.wso2.carbon.event.processor.storm.common.event.client;

import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.storm.common.event.server.EventServerUtils;
import org.wso2.carbon.event.processor.storm.common.event.server.StreamRuntimeInfo;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class EventClient {
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static Logger log = Logger.getLogger(EventClient.class);
    private final String hostUrl;
    private final StreamDefinition streamDefinition;
    private final StreamRuntimeInfo streamRuntimeInfo;
    private OutputStream outputStream;
    private Socket clientSocket;


    public EventClient(String hostUrl, StreamDefinition streamDefinition) throws Exception {
        this.hostUrl = hostUrl;
        this.streamDefinition = streamDefinition;
        this.streamRuntimeInfo = EventServerUtils.createStreamRuntimeInfo(streamDefinition);

        log.info("Client configured to send events to " + hostUrl);
        String[] hp = hostUrl.split(":");
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);
        this.clientSocket = new Socket(host, port);
        this.outputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
    }

    public void close() {
        try {
            outputStream.flush();
            clientSocket.close();
        } catch (IOException e) {
            log.warn("Error while closing stream for sending events: " + e.getMessage(), e);
        }
    }


    public void sendEvent(Object[] event) throws IOException {
        outputStream.write((byte) streamRuntimeInfo.getStreamId().length());
        outputStream.write((streamRuntimeInfo.getStreamId()).getBytes(DEFAULT_CHARSET));

        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize());
        int[] stringDataIndex = new int[streamRuntimeInfo.getNoOfStringAttributes()];
        int stringIndex = 0;
        Attribute.Type[] types = streamRuntimeInfo.getAttributeTypes();
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            Attribute.Type type = types[i];
            switch (type) {
                case INT:
                    buf.putInt((Integer) event[i]);
                    continue;
                case LONG:
                    buf.putLong((Long) event[i]);
                    continue;
                case BOOL:
                    buf.put((byte) (((Boolean) event[i]) ? 1 : 0));
                    continue;
                case FLOAT:
                    buf.putFloat((Float) event[i]);
                    continue;
                case DOUBLE:
                    buf.putDouble((Double) event[i]);
                    continue;
                case STRING:
                    buf.putShort((short) ((String) event[i]).length());
                    stringDataIndex[stringIndex] = i;
                    stringIndex++;
            }
        }

        outputStream.write(buf.array());
        for (int aStringIndex : stringDataIndex) {
            outputStream.write(((String) event[aStringIndex]).getBytes(DEFAULT_CHARSET));
        }
        outputStream.flush();

    }
}
