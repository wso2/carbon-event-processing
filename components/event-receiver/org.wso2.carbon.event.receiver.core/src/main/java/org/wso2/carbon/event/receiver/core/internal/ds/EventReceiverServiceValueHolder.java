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
package org.wso2.carbon.event.receiver.core.internal.ds;

import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.MessageType;
import org.wso2.carbon.event.receiver.core.config.InputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.CarbonInputEventAdaptorService;
import org.wso2.carbon.event.receiver.core.internal.type.json.JsonInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.map.MapInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.text.TextInputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.wso2event.Wso2InputMapperFactory;
import org.wso2.carbon.event.receiver.core.internal.type.xml.XMLInputMapperFactory;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.concurrent.ConcurrentHashMap;

public class EventReceiverServiceValueHolder {
    private static EventReceiverService eventReceiverService;
    private static CarbonInputEventAdaptorService inputEventAdaptorService;
    private static CarbonEventReceiverService carbonEventReceiverService;
    private static RegistryService registryService;
    private static EventStreamService eventStreamService;
    private static ConcurrentHashMap<String, InputMapperFactory> mappingFactoryMap;
    private static DataBridgeReceiverService dataBridgeReceiverService;
    private static ConfigurationContextService configurationContextService;

    static {
        mappingFactoryMap = new ConcurrentHashMap<String, InputMapperFactory>();
        mappingFactoryMap.put(MessageType.MAP, new MapInputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextInputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new Wso2InputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLInputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JsonInputMapperFactory());
    }

    private static EventStatisticsService eventStatisticsService;

    private EventReceiverServiceValueHolder() {

    }

    public static ConcurrentHashMap<String, InputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static CarbonEventReceiverService getCarbonEventReceiverService() {
        return carbonEventReceiverService;
    }

    public static void registerEventReceiverService(EventReceiverService eventReceiverService) {
        EventReceiverServiceValueHolder.eventReceiverService = eventReceiverService;
        if (eventReceiverService instanceof CarbonEventReceiverService) {
            EventReceiverServiceValueHolder.carbonEventReceiverService = (CarbonEventReceiverService) eventReceiverService;
        }
    }

    public static EventReceiverService getEventReceiverService() {
        return EventReceiverServiceValueHolder.eventReceiverService;
    }

    public static void registerCarbonInputEventAdaptorService(
            CarbonInputEventAdaptorService inputEventAdaptorService) {
        EventReceiverServiceValueHolder.inputEventAdaptorService = inputEventAdaptorService;
    }

    public static CarbonInputEventAdaptorService getCarbonInputEventAdaptorService() {
        return EventReceiverServiceValueHolder.inputEventAdaptorService;
    }

    public static void registerEventStatisticsService(
            EventStatisticsService eventStatisticsService) {
        EventReceiverServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static void registerRegistryService(RegistryService registryService) {
        EventReceiverServiceValueHolder.registryService = registryService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void registerEventStreamService(EventStreamService eventStreamService) {
        EventReceiverServiceValueHolder.eventStreamService = eventStreamService;
    }

    public static EventStreamService getEventStreamService() {
        return EventReceiverServiceValueHolder.eventStreamService;
    }

    public static void registerDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        EventReceiverServiceValueHolder.dataBridgeReceiverService = dataBridgeReceiverService;
    }

    public static DataBridgeReceiverService getDataBridgeReceiverService() {
        return dataBridgeReceiverService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventReceiverServiceValueHolder.configurationContextService = configurationContextService;
    }

}
