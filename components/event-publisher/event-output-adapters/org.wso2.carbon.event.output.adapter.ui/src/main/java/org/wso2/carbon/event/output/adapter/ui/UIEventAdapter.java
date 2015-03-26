/*
 *
 *  Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.output.adapter.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Contains the life cycle of executions regarding the UI Adapter
 *
 */

public class UIEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(UIEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private int tenantID;
    private String streamId;
    private LinkedList<Object> streamSpecificEvents;

    public UIEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        this.tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    @Override
    public void init() throws OutputEventAdapterException {

        Map<String, String> eventOutputAdapterMap = UIEventAdaptorServiceInternalValueHolder.getOutputEventStreamMap();

        if(eventAdapterConfiguration.getStaticProperties().get(UIEventAdapterConstants
                .ADAPTER_UI_OUTPUT_STREAM_VERSION) == null || " ".equals(eventAdapterConfiguration
                .getStaticProperties().get(UIEventAdapterConstants
                        .ADAPTER_UI_OUTPUT_STREAM_VERSION))){
            eventAdapterConfiguration.getStaticProperties().put(UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_VERSION,
                    UIEventAdapterConstants.ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION);

        }

        streamId = eventAdapterConfiguration.getStaticProperties().get(
                UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_NAME) + UIEventAdapterConstants.ADAPTER_UI_COLON +
                eventAdapterConfiguration.getStaticProperties().get(UIEventAdapterConstants
                        .ADAPTER_UI_OUTPUT_STREAM_VERSION);
        String adapterName = eventOutputAdapterMap.get(streamId);

        if(adapterName != null){
            throw new OutputEventAdapterRuntimeException("An Output ui event adapter \""+ adapterName +"\" is already" +
                    " exist for stream id \""+ streamId + "\"");
        } else{
            eventOutputAdapterMap.put(streamId,eventAdapterConfiguration.getName());

            ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedList<Object>>> tenantSpecificStreamMap =
                    UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamMap();
            ConcurrentHashMap<String, LinkedList<Object>> streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantID);

            if(streamSpecificEventsMap == null){
                streamSpecificEventsMap = new ConcurrentHashMap<String, LinkedList<Object>>();
                if (null != tenantSpecificStreamMap.putIfAbsent(tenantID, streamSpecificEventsMap)){
                    streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantID);
                }
            }
            streamSpecificEvents = streamSpecificEventsMap.get(streamId);

            if (streamSpecificEvents == null){
                streamSpecificEvents = new LinkedList<Object>();
                if (null != streamSpecificEventsMap.putIfAbsent(streamId,streamSpecificEvents)){
                    streamSpecificEvents = streamSpecificEventsMap.get(streamId);
                }
            }
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        //Not needed
    }

    @Override
    public void connect() {
        //Not needed
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        UIOutputCallbackControllerServiceImpl uiOutputCallbackControllerServiceImpl =
                UIEventAdaptorServiceInternalValueHolder
                        .getUIOutputCallbackRegisterServiceImpl();
        CopyOnWriteArrayList<Session> sessions = uiOutputCallbackControllerServiceImpl.getSessions(tenantID, streamId);

        populateEventsMap(message);

        if (sessions != null){
            if (message instanceof Object[]) {
                //TODO: send message in one send() operation by defining a new events-schema.
                for (Object object : (Object[])message){
                    for (Session session : sessions){
                        synchronized (session){
                            Event event = (Event) ((Object) object);
                            StringBuilder allEventsAsString = new StringBuilder("[[");
                            Boolean eventsExists = false;

                            if(event.getMetaData() != null){

                                Object[] metaData = event.getMetaData();
                                eventsExists = true;
                                for(int i=0;i < metaData.length;i++){
                                    allEventsAsString.append("\"");
                                    allEventsAsString.append(metaData[i]);
                                    allEventsAsString.append("\"");
                                    if(i != (metaData.length-1)){
                                        allEventsAsString.append(",");
                                    }
                                }
                            }

                            if(event.getCorrelationData() != null){
                                Object[] correlationData = event.getCorrelationData();

                                if(eventsExists){
                                    allEventsAsString.append(",");
                                } else{
                                    eventsExists = true;
                                }
                                for(int i=0;i < correlationData.length;i++){
                                    allEventsAsString.append("\"");
                                    allEventsAsString.append(correlationData[i]);
                                    allEventsAsString.append("\"");
                                    if(i != (correlationData.length-1)){
                                        allEventsAsString.append(",");
                                    }
                                }
                            }

                            if(event.getPayloadData() != null){

                                Object[] payloadData = event.getPayloadData();
                                if(eventsExists){
                                    allEventsAsString.append(",");
                                } else{
                                    eventsExists = true;
                                }
                                for(int i=0;i < payloadData.length;i++){
                                    allEventsAsString.append("\"");
                                    allEventsAsString.append(payloadData[i]);
                                    allEventsAsString.append("\"");
                                    if(i != (payloadData.length-1)){
                                        allEventsAsString.append(",");
                                    }
                                }
                            }
                            allEventsAsString.append("]]");
                            session.getAsyncRemote().sendText(allEventsAsString.toString());  //this method call was
                            // synchronized to fix CEP-996
                        }
                    }
                }
            } else {
                for (Session session : sessions){
                    synchronized (session){
                        Event event = (Event) ((Object) message);
                        StringBuilder allEventsAsString = new StringBuilder("[[");
                        Boolean eventsExists = false;

                        if(event.getMetaData() != null){

                            Object[] metaData = event.getMetaData();
                            eventsExists = true;
                            for(int i=0;i < metaData.length;i++){
                                allEventsAsString.append("\"");
                                allEventsAsString.append(metaData[i]);
                                allEventsAsString.append("\"");
                                if(i != (metaData.length-1)){
                                    allEventsAsString.append(",");
                                }
                            }
                        }

                        if(event.getCorrelationData() != null){
                            Object[] correlationData = event.getCorrelationData();

                            if(eventsExists){
                                allEventsAsString.append(",");
                            } else{
                                eventsExists = true;
                            }
                            for(int i=0;i < correlationData.length;i++){
                                allEventsAsString.append("\"");
                                allEventsAsString.append(correlationData[i]);
                                allEventsAsString.append("\"");
                                if(i != (correlationData.length-1)){
                                    allEventsAsString.append(",");
                                }
                            }
                        }

                        if(event.getPayloadData() != null){

                            Object[] payloadData = event.getPayloadData();
                            if(eventsExists){
                                allEventsAsString.append(",");
                            } else{
                                eventsExists = true;
                            }
                            for(int i=0;i < payloadData.length;i++){
                                allEventsAsString.append("\"");
                                allEventsAsString.append(payloadData[i]);
                                allEventsAsString.append("\"");
                                if(i != (payloadData.length-1)){
                                    allEventsAsString.append(",");
                                }
                            }
                        }

                        allEventsAsString.append("]]");
                        session.getAsyncRemote().sendText(allEventsAsString.toString());  //this method call was
                        // synchronized to fix CEP-996
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Dropping the message: '"+message+"', since no clients have being registered to receive " +
                        "events from ui adapter: '"+ eventAdapterConfiguration.getName()+ "', " +
                        "for tenant ID: "+tenantID);
            }
        }
    }

    /**
     * Used to store all the retrieved events in a LinkList
     *
     * @param message - contains the event message.
     * @return
     */
    public void populateEventsMap(Object message){

        if (message instanceof Object[]) {

            for (Object object : (Object[])message){

                if(streamSpecificEvents.size() == UIEventAdapterConstants.EVENTS_QUEUE_SIZE){
                    streamSpecificEvents.removeFirst();
                }
                Object[] eventValues = new Object[2];
                Event event = (Event) ((Object) object);
                List<Object> eventComponatList = new ArrayList<Object>();

                if(event.getMetaData() != null){
                    eventComponatList.add(event.getMetaData());
                }
                if(event.getCorrelationData() != null) {
                    eventComponatList.add(event.getCorrelationData());
                }
                if(event.getPayloadData() != null) {
                    eventComponatList.add(event.getPayloadData());
                }
                eventValues[UIEventAdapterConstants.INDEX_ZERO] = eventComponatList;
                eventValues[UIEventAdapterConstants.INDEX_ONE] = System.currentTimeMillis();
                streamSpecificEvents.add(eventValues);
            }
        } else {

            if(streamSpecificEvents.size() == UIEventAdapterConstants.EVENTS_QUEUE_SIZE){
                streamSpecificEvents.removeFirst();
            }
            Object[] eventValues = new Object[2];

            Event event = (Event) ((Object) message);
            List<Object> eventComponatList = new ArrayList<Object>();

            if(event.getMetaData() != null){
                eventComponatList.add(event.getMetaData());
            }
            if(event.getCorrelationData() != null) {
                eventComponatList.add(event.getCorrelationData());
            }
            if(event.getPayloadData() != null) {
                eventComponatList.add(event.getPayloadData());
            }
            eventValues[UIEventAdapterConstants.INDEX_ZERO] = eventComponatList;
            eventValues[UIEventAdapterConstants.INDEX_ONE] = System.currentTimeMillis();
            streamSpecificEvents.add(eventValues);
        }
    }

    @Override
    public void disconnect() {
        //Not needed
    }

    @Override
    public void destroy() {

        //Removing outputadapter and streamId
        Map<String, String> eventOutputAdapterMap = UIEventAdaptorServiceInternalValueHolder.getOutputEventStreamMap();
        eventOutputAdapterMap.remove(streamId);

        //Removing the streamId and events registered for the output adapter
        UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamMap().get(tenantID).remove(streamId);
    }
}

