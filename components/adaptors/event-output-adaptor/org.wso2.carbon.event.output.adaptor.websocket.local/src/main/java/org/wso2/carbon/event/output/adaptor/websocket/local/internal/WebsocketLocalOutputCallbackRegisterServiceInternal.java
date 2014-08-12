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
package org.wso2.carbon.event.output.adaptor.websocket.local.internal;

import org.wso2.carbon.event.output.adaptor.websocket.local.WebsocketLocalOutputCallbackRegisterService;

import javax.websocket.Session;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebsocketLocalOutputCallbackRegisterServiceInternal implements WebsocketLocalOutputCallbackRegisterService{

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>>> outputEventAdaptorSessionMap;               //TODO should this be static? I think no, because we're using a value holder to ensure that a single instance is used throughout.

    //todo create an interface with only sub & unsub and put the implemn. in the internal packg.
    public WebsocketLocalOutputCallbackRegisterServiceInternal(){
        outputEventAdaptorSessionMap =
                new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>>>();
    }


    public void subscribe(int tenantId, String adaptorName, String topic, Session session) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap == null) {
            tenantSpecificAdaptorMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>>();
            if (null != outputEventAdaptorSessionMap.putIfAbsent(tenantId, tenantSpecificAdaptorMap)){
                tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
            }
        }
        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(adaptorName);
        if (adaptorSpecificTopicMap == null) {
            adaptorSpecificTopicMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>();
            if (null != tenantSpecificAdaptorMap.putIfAbsent(adaptorName, adaptorSpecificTopicMap)){
                adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(adaptorName);
            }
        }
        CopyOnWriteArrayList<Session> topicSpecificSessions = adaptorSpecificTopicMap.get(topic);
        if (topicSpecificSessions == null){
            topicSpecificSessions = new CopyOnWriteArrayList<Session>();
            if (null != adaptorSpecificTopicMap.putIfAbsent(topic,topicSpecificSessions)){
                topicSpecificSessions = adaptorSpecificTopicMap.get(topic);
            }
        }
        topicSpecificSessions.add(session);
    }


    public CopyOnWriteArrayList<Session> getSessions(int tenantId, String adaptorName, String topic){
        Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            Map<String, CopyOnWriteArrayList<Session>> adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(adaptorName);
            if (adaptorSpecificTopicMap != null) {
                CopyOnWriteArrayList<Session> topicSpecificSessions = adaptorSpecificTopicMap.get(topic);
                return topicSpecificSessions;
            }
        }
        return null;
    }

    public void unsubscribe(int tenantId, String adaptorName, String topic, Session session) {
        Map<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            Map<String, CopyOnWriteArrayList<Session>> adaptorSpecificTopicMap = tenantSpecificAdaptorMap.get(adaptorName);
            if (adaptorSpecificTopicMap != null) {
                CopyOnWriteArrayList<Session> topicSpecificSessions = adaptorSpecificTopicMap.get(topic);
                if (topicSpecificSessions != null) {
                    Session sessionToRemove = null;
                    for (Iterator<Session> iterator = topicSpecificSessions.iterator(); iterator.hasNext(); ) {
                        Session thisSession = iterator.next();
                        if (session.getId().equals(thisSession.getId())) {
                            sessionToRemove = session;
                            break;
                        }
                    }
                    if (sessionToRemove != null){
                        topicSpecificSessions.remove(sessionToRemove);
                    }
                }
            }
        }
    }
}
