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
//package org.wso2.carbon.event.publisher.core.internal.util;
//
//import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
//import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
//import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
//import org.wso2.carbon.event.publisher.core.config.mapping.TextOutputMapping;
//import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
//
//public class EventFormatterUtil {
//
//    public static EventPublisherConfiguration createDefaultEventFormatter(String streamId,
//                                                                         String transportAdaptorName) {
//        String streamName = DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId);
//        String streamVersion = DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId);
//
//        EventPublisherConfiguration eventPublisherConfiguration =
//                new EventPublisherConfiguration();
//
//        eventPublisherConfiguration.setEventPublisherName(streamId.replaceAll(EventPublisherConstants.STREAM_ID_SEPERATOR, EventPublisherConstants.NORMALIZATION_STRING) + EventPublisherConstants.DEFAULT_EVENT_PUBLISHER_POSTFIX);
//        TextOutputMapping textOutputMapping = new TextOutputMapping();
//        textOutputMapping.setCustomMappingEnabled(false);
//        eventPublisherConfiguration.setOutputMapping(textOutputMapping);
//
//        EndpointAdaptorConfiguration endpointAdaptorConfiguration = new EndpointAdaptorConfiguration();
//        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
//        outputEventAdaptorMessageConfiguration.addOutputMessageProperty(EventPublisherConstants.ADAPTOR_MESSAGE_UNIQUE_ID, EventPublisherConstants.ADAPTOR_MESSAGE_UNIQUE_ID_VALUE);
//        endpointAdaptorConfiguration.setOutputEventAdaptorConfiguration(outputEventAdaptorMessageConfiguration);
//        endpointAdaptorConfiguration.setEventAdaptorName(transportAdaptorName);
//        endpointAdaptorConfiguration.setEventAdaptorType(EventPublisherConstants.ADAPTOR_TYPE_LOGGER);
//        eventPublisherConfiguration.setEndpointAdaptorConfiguration(endpointAdaptorConfiguration);
//
//        eventPublisherConfiguration.setFromStreamName(streamName);
//        eventPublisherConfiguration.setFromStreamVersion(streamVersion);
//
//        return eventPublisherConfiguration;
//    }
//}
