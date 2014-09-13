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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO Fix event client to support multiple streams
public class TCPEventClient {
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static Logger log = Logger.getLogger(TCPEventClient.class);
    private final String hostUrl;
    private Map<String, StreamRuntimeInfo> streamRuntimeInfoMap;
    private OutputStream outputStream;
    private Socket clientSocket;


    public TCPEventClient(String hostUrl) throws IOException {
        this.hostUrl = hostUrl;
        this.streamRuntimeInfoMap = new ConcurrentHashMap<String, StreamRuntimeInfo>();
        String[] hp = hostUrl.split(":");
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);
        this.clientSocket = new Socket(host, port);
        this.outputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        log.info("Client configured to send events to " + hostUrl);
    }

    public void addStreamDefinition(StreamDefinition streamDefinition) {
        streamRuntimeInfoMap.put(streamDefinition.getStreamId(), EventServerUtils.createStreamRuntimeInfo(streamDefinition));
        log.info("Stream definition added for stream: " + streamDefinition.getStreamId());
    }

    public void close() {
        try {
            outputStream.flush();
            clientSocket.close();
        } catch (IOException e) {
            log.warn("Error while closing stream to " + hostUrl + " : " + e.getMessage(), e);
        }
    }


    public synchronized void sendEvent(String streamId, Object[] event) throws IOException {
        StreamRuntimeInfo streamRuntimeInfo = streamRuntimeInfoMap.get(streamId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte streamIdSize = streamRuntimeInfo.getStreamIdSize();
        ByteBuffer buf = ByteBuffer.allocate(streamRuntimeInfo.getFixedMessageSize() + streamIdSize + 1);
        buf.put(streamIdSize);
        buf.put((streamRuntimeInfo.getStreamId()).getBytes(DEFAULT_CHARSET));

        int[] stringDataIndex = new int[streamRuntimeInfo.getNoOfStringAttributes()];
        int stringIndex = 0;
        int stringSize = 0;
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
                    short length = (short) ((String) event[i]).length();
                    buf.putShort(length);
                    stringDataIndex[stringIndex] = i;
                    stringIndex++;
                    stringSize += length;
            }
        }
        out.write(buf.array());

        buf = ByteBuffer.allocate(stringSize);
        for (int aStringIndex : stringDataIndex) {
            buf.put(((String) event[aStringIndex]).getBytes(DEFAULT_CHARSET));
        }
        out.write(buf.array());

        outputStream.write(out.toByteArray());
        outputStream.flush();

    }
}
