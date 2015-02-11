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
package org.wso2.carbon.event.notifier.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.notifier.core.OutputEventAdaptorService;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierConfigurationException;
import org.wso2.carbon.event.notifier.core.exception.EventNotifierStreamValidationException;
import org.wso2.carbon.event.notifier.core.internal.OutputMapper;
import org.wso2.carbon.event.notifier.core.internal.ds.EventNotifierServiceValueHolder;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.manager.core.RawEventConsumer;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.*;

public class EventNotifier implements RawEventConsumer {

    private static final Log log = LogFactory.getLog(EventNotifier.class);

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    List<String> dynamicMessagePropertyList = new ArrayList<String>();
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);
    private EventNotifierConfiguration eventNotifierConfiguration = null;
    private int tenantId;
    private Map<String, Integer> propertyPositionMap = new TreeMap<String, Integer>();
    private OutputMapper outputMapper = null;
    private String streamId = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private boolean dynamicMessagePropertyEnabled = false;
    private boolean customMappingEnabled = false;

    public EventNotifier(EventNotifierConfiguration eventNotifierConfiguration)
            throws EventNotifierConfigurationException {

        this.eventNotifierConfiguration = eventNotifierConfiguration;
        this.customMappingEnabled = eventNotifierConfiguration.getOutputMapping().isCustomMappingEnabled();
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String inputStreamName = eventNotifierConfiguration.getFromStreamName();
        String inputStreamVersion = eventNotifierConfiguration.getFromStreamVersion();

        //Stream Definition must same for any event source, There are cannot be different stream definition for same stream id in multiple event sourced
        StreamDefinition inputStreamDefinition = null;

        try {
            inputStreamDefinition = EventNotifierServiceValueHolder.getEventStreamService().getStreamDefinition(inputStreamName, inputStreamVersion, tenantId);

        } catch (EventStreamConfigurationException e) {
            throw new EventNotifierConfigurationException("Cannot retrieve the stream definition from stream store : " + e.getMessage());
        }

        if (inputStreamDefinition == null) {
            throw new EventNotifierConfigurationException("There is no any event stream for the corresponding stream name or version : " + inputStreamName + "-" + inputStreamVersion);
        }
        this.streamId = inputStreamDefinition.getStreamId();
        createPropertyPositionMap(inputStreamDefinition);
        outputMapper = EventNotifierServiceValueHolder.getMappingFactoryMap().get(eventNotifierConfiguration.getOutputMapping().getMappingType()).constructOutputMapper(eventNotifierConfiguration, propertyPositionMap, tenantId, inputStreamDefinition);

        Map<String, String> outputAdaptorProperties = eventNotifierConfiguration.getEndpointAdaptorConfiguration().getOutputAdaptorProperties();
        for (Map.Entry<String, String> entry : outputAdaptorProperties.entrySet()) {
            Map.Entry pairs = (Map.Entry) entry;
            getDynamicOutputMessageProperties(pairs.getValue() != null ? pairs.getValue().toString() : "");
        }

        if (dynamicMessagePropertyList.size() > 0) {
            dynamicMessagePropertyEnabled = true;
        }

        try {
            EventNotifierServiceValueHolder.getEventStreamService().subscribe(this, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventNotifierStreamValidationException("Stream " + streamId + " does not exist", streamId);
        }

        this.traceEnabled = eventNotifierConfiguration.isEnableTracing();
        this.statisticsEnabled = eventNotifierConfiguration.isEnableStatistics();
        if (statisticsEnabled) {
            this.statisticsMonitor = EventNotifierServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventNotifierConstants.EVENT_NOTIFIER, eventNotifierConfiguration.getEventNotifierName(), null);
        }
        if (traceEnabled) {
            this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventNotifierConstants.EVENT_NOTIFIER + " : " + eventNotifierConfiguration.getFromStreamName() + ", before processing " + System.getProperty("line.separator");
            this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventNotifierConstants.EVENT_NOTIFIER + " : " + eventNotifierConfiguration.getFromStreamName() + ", after processing " + System.getProperty("line.separator");
        }
    }

    public EventNotifierConfiguration getEventNotifierConfiguration() {
        return eventNotifierConfiguration;
    }

    public void sendEventData(Object[] eventData) {

        EndpointAdaptorConfiguration endpointAdaptorConfiguration = eventNotifierConfiguration.getEndpointAdaptorConfiguration();
        Map<String, String> outputEventAdaptorPropertyMap = new HashMap<String, String>(endpointAdaptorConfiguration.getOutputAdaptorProperties());
        String endpointType = endpointAdaptorConfiguration.getEndpointType();
        InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();
        internalOutputEventAdaptorConfiguration.setProperties(outputEventAdaptorPropertyMap);

        Object outObject;
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + Arrays.deepToString(eventData));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementResponse();
        }
        try {
            if (customMappingEnabled) {
                outObject = outputMapper.convertToMappedInputEvent(eventData);
            } else {
                outObject = outputMapper.convertToTypedInputEvent(eventData);
            }
        } catch (EventNotifierConfigurationException e) {
            log.error("Cannot send event:" + Arrays.deepToString(eventData) + " from " + eventNotifierConfiguration.getEventNotifierName());
            return;
        }

        if (traceEnabled) {
            if (outObject instanceof Object[]) {
                trace.info(afterTracerPrefix + Arrays.deepToString((Object[]) outObject));
            } else {
                trace.info(afterTracerPrefix + outObject);
            }
        }

        if (dynamicMessagePropertyEnabled) {
            changeDynamicEventAdaptorMessageProperties(eventData, internalOutputEventAdaptorConfiguration);
        }

        OutputEventAdaptorService eventAdaptorService = EventNotifierServiceValueHolder.getOutputEventAdaptorService();
        eventAdaptorService.publish(new EndpointAdaptorConfiguration(eventNotifierConfiguration.getEventNotifierName(),endpointType, internalOutputEventAdaptorConfiguration), outObject, tenantId);

    }

    private void createPropertyPositionMap(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        int propertyCount = 0;
        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                propertyPositionMap.put(EventNotifierConstants.PROPERTY_META_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                propertyPositionMap.put(EventNotifierConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (payloadAttributeList != null) {
            for (Attribute attribute : payloadAttributeList) {
                propertyPositionMap.put(attribute.getName(), propertyCount);
                propertyCount++;
            }
        }
    }

    public String getStreamId() {
        return streamId;
    }

    @Override
    public void consumeEventData(Object[] eventData) {
        sendEventData(eventData);
    }

    private List<String> getDynamicOutputMessageProperties(String messagePropertyValue) {

        String text = messagePropertyValue;

        while (text.contains("{{") && text.indexOf("}}") > 0) {
            dynamicMessagePropertyList.add(text.substring(text.indexOf("{{") + 2, text.indexOf("}}")));
            text = text.substring(text.indexOf("}}") + 2);
        }
        return dynamicMessagePropertyList;
    }

    private void changeDynamicEventAdaptorMessageProperties(Object[] eventData, InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration) {

        for (String dynamicMessageProperty : dynamicMessagePropertyList) {
            if (eventData.length != 0 && dynamicMessageProperty != null) {
                int position = propertyPositionMap.get(dynamicMessageProperty);
                changePropertyValue(position, dynamicMessageProperty, eventData, internalOutputEventAdaptorConfiguration);
            }
        }
    }

    private void changePropertyValue(int position, String messageProperty, Object[] eventData, InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration) {
        Map<String, String> outputMessageProperties = internalOutputEventAdaptorConfiguration.getProperties();

        for (Map.Entry<String, String> entry : outputMessageProperties.entrySet()) {
            String mapValue = "{{" + messageProperty + "}}";
            String regexValue = "\\{\\{" + messageProperty + "\\}\\}";
            String entryValue = entry.getValue();
            if (entryValue != null && entryValue.contains(mapValue)) {
                if (eventData[position] != null) {
                    entry.setValue(entryValue.replaceAll(regexValue, eventData[position].toString()));
                } else {
                    entry.setValue(entryValue.replaceAll(regexValue, ""));
                }
            }
        }

    }

}
