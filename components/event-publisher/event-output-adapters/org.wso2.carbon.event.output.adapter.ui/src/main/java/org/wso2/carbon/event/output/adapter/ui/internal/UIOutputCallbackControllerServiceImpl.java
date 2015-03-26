/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.ui.internal;

import com.google.gson.JsonObject;
import org.wso2.carbon.event.output.adapter.ui.UIOutputCallbackControllerService;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;
import javax.websocket.Session;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service implementation class which exposes to front end
 *
 */
public class UIOutputCallbackControllerServiceImpl implements UIOutputCallbackControllerService {

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>>
            outputEventAdaptorSessionMap;

    public UIOutputCallbackControllerServiceImpl(){
        outputEventAdaptorSessionMap =
                new ConcurrentHashMap<Integer, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>>();
    }


    /**
     * Used to subscribe the session id and stream id for later web socket connectivity
     *
     * @param tenantId - Tenant id of the user.
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @param session - Session which user registered.
     * @return
     */
    public void subscribeWebsocket(int tenantId, String streamName, String version, Session session) {

        if(version == null || " ".equals(version)){
            version = UIEventAdapterConstants.ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION;
        }
        String streamId = streamName + UIEventAdapterConstants.ADAPTER_UI_COLON + version;
        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> tenantSpecificAdaptorMap =
                outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap == null) {
            tenantSpecificAdaptorMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>();
            if (null != outputEventAdaptorSessionMap.putIfAbsent(tenantId, tenantSpecificAdaptorMap)){
                tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
            }
        }
        CopyOnWriteArrayList<Session> adapterSpecificSessions = tenantSpecificAdaptorMap.get(streamId);
        if (adapterSpecificSessions == null){
            adapterSpecificSessions = new CopyOnWriteArrayList<Session>();
            if (null != tenantSpecificAdaptorMap.putIfAbsent(streamId,adapterSpecificSessions)){
                adapterSpecificSessions = tenantSpecificAdaptorMap.get(streamId);
            }
        }
        adapterSpecificSessions.add(session);
    }

    /**
     * Used to return registered sessions per streamId
     *
     * @param tenantId - Tenant id of the user.
     * @param streamId - Stream name and version which user register to.
     * @return the sessions list.
     */
    public CopyOnWriteArrayList<Session> getSessions(int tenantId, String streamId){
        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> tenantSpecificAdaptorMap =
                outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            CopyOnWriteArrayList<Session> adapterSpecificSessions = tenantSpecificAdaptorMap.get(streamId);
            return adapterSpecificSessions;
        }
        return null;
    }

    /**
     * Used to return events per streamId
     *
     * @param tenanId - Tenant id of the user.
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @return the events list.
     */
    public LinkedList<Object> getEvents(int tenanId, String streamName, String version){

        ConcurrentHashMap<String, LinkedList<Object>> tenantSpecificStreamMap =
                UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamMap().get(tenanId);

        if(tenantSpecificStreamMap != null){
            String streamId = streamName + UIEventAdapterConstants.ADAPTER_UI_COLON + version;
            LinkedList<Object> streamSpecificEvents = tenantSpecificStreamMap.get(streamId);
            return streamSpecificEvents;
        }
        return null;
    }

    /**
     * Used to return events per streamId
     *
     * @param tenantId - Tenant id of the user.
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @param session - Session which user subscribed to.
     * @return the events list.
     */
    public void unsubscribeWebsocket(int tenantId, String streamName, String version, Session session) {

        if(version == null || " ".equals(version)){
            version = UIEventAdapterConstants.ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION;
        }
        String id = streamName + UIEventAdapterConstants.ADAPTER_UI_COLON + version;

        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            CopyOnWriteArrayList<Session> adapterSpecificSessions = tenantSpecificAdaptorMap.get(id);
            if (adapterSpecificSessions != null) {
                Session sessionToRemove = null;
                for (Iterator<Session> iterator = adapterSpecificSessions.iterator(); iterator.hasNext(); ) {
                    Session thisSession = iterator.next();
                    if (session.getId().equals(thisSession.getId())) {
                        sessionToRemove = session;
                        break;
                    }
                }
                if (sessionToRemove != null) {
                    adapterSpecificSessions.remove(sessionToRemove);
                }
            }
        }
    }

    /**
     * Used to return events per http GET request.
     *
     * @param tenantId - Tenant id of the user.
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @param lastUpdatedTime - Last dispatched events time.
     * @return the events list.
     */
    @Override
    public JsonObject retrieveEvents(int tenantId, String streamName, String version, String lastUpdatedTime) {

        LinkedList<Object> allEvents = getEvents(tenantId, streamName, version);
        //List<Object> eventsListToBeSent;
        Object lastEventTime;
        JsonObject eventsData;
        Boolean eventsExists = false;

        if(allEvents != null){
            eventsData = new JsonObject();
            if(allEvents.size() != 0){
                eventsExists = true;
                Boolean firstFilteredValue = true;
                long sentTimeStamp = Long.parseLong(lastUpdatedTime);
                //eventsListToBeSent = new ArrayList<Object>();

                StringBuilder allEventsAsString = new StringBuilder("[");
                // set Iterator as descending
                Iterator iterator = allEvents.descendingIterator();

                while (iterator.hasNext()) {

                    Object[] eventValues = (Object[]) iterator.next();
                    long eventTimeStamp = (Long) eventValues[UIEventAdapterConstants.INDEX_ONE];
                    if(sentTimeStamp < eventTimeStamp){

                        if(!firstFilteredValue){
                            allEventsAsString.append(",");
                        }
                        firstFilteredValue = false;
                        List<Object> listOfEventData = (List<Object>) eventValues[UIEventAdapterConstants.INDEX_ZERO];
                        allEventsAsString.append("[");
                        for(int cnt =0;cnt<listOfEventData.size();cnt++){

                            Object[] eventValueComponent = (Object[]) listOfEventData.get(cnt);
                            for(int i=0;i<eventValueComponent.length;i++){

                                allEventsAsString.append("\"");
                                allEventsAsString.append(eventValueComponent[i]);
                                allEventsAsString.append("\"");
                                if(cnt != (listOfEventData.size()-1)){
                                    allEventsAsString.append(",");
                                } else{
                                    if(i != (eventValueComponent.length-1)){
                                        allEventsAsString.append(",");
                                    }
                                }

                            }
                        }
                        allEventsAsString.append("]");
                    }
                }
                allEventsAsString.append("]");
                Object[] lastObj = (Object[]) allEvents.getLast();
                lastEventTime = lastObj[UIEventAdapterConstants.INDEX_ONE];
                eventsData.addProperty("eventsExists",eventsExists);
                eventsData.addProperty("lastEventTime",String.valueOf(lastEventTime));
                eventsData.addProperty("events",allEventsAsString.toString());
            } else {
                eventsData.addProperty("eventsExists",eventsExists);
            }
            return eventsData;
        }
        return null;
    }
}
