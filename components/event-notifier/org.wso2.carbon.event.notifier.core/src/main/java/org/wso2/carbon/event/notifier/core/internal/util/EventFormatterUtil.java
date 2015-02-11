///*
//*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//*
//*  WSO2 Inc. licenses this file to you under the Apache License,
//*  Version 2.0 (the "License"); you may not use this file except
//*  in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*    http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*/
//package org.wso2.carbon.event.notifier.core.internal.util;
//
//import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
//import org.wso2.carbon.event.notifier.core.config.EventNotifierConfiguration;
//import org.wso2.carbon.event.notifier.core.config.EventNotifierConstants;
//import org.wso2.carbon.event.notifier.core.internal.type.text.TextOutputMapping;
//import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
//
//public class EventFormatterUtil {
//
//    public static EventNotifierConfiguration createDefaultEventFormatter(String streamId,
//                                                                         String transportAdaptorName) {
//        String streamName = DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId);
//        String streamVersion = DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId);
//
//        EventNotifierConfiguration eventNotifierConfiguration =
//                new EventNotifierConfiguration();
//
//        eventNotifierConfiguration.setEventFormatterName(streamId.replaceAll(EventNotifierConstants.STREAM_ID_SEPERATOR, EventNotifierConstants.NORMALIZATION_STRING) + EventNotifierConstants.DEFAULT_EVENT_NOTIFIER_POSTFIX);
//        TextOutputMapping textOutputMapping = new TextOutputMapping();
//        textOutputMapping.setCustomMappingEnabled(false);
//        eventNotifierConfiguration.setOutputMapping(textOutputMapping);
//
//        EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
//        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
//        outputEventAdaptorMessageConfiguration.addOutputMessageProperty(EventNotifierConstants.ADAPTOR_MESSAGE_UNIQUE_ID, EventNotifierConstants.ADAPTOR_MESSAGE_UNIQUE_ID_VALUE);
//        endpointAdaptorConfiguration.setOutputEventAdaptorConfiguration(outputEventAdaptorMessageConfiguration);
//        endpointAdaptorConfiguration.setEventAdaptorName(transportAdaptorName);
//        endpointAdaptorConfiguration.setEventAdaptorType(EventNotifierConstants.ADAPTOR_TYPE_LOGGER);
//        eventNotifierConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);
//
//        eventNotifierConfiguration.setFromStreamName(streamName);
//        eventNotifierConfiguration.setFromStreamVersion(streamVersion);
//
//        return eventNotifierConfiguration;
//    }
//}
