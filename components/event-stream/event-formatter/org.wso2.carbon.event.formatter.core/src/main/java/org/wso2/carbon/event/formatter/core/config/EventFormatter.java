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
package org.wso2.carbon.event.formatter.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterStreamValidationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.manager.core.RawEventConsumer;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.*;

public class EventFormatter implements RawEventConsumer {

    private static final Log log = LogFactory.getLog(EventFormatter.class);

    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private final boolean traceEnabled;
    private final boolean statisticsEnabled;
    List<String> dynamicMessagePropertyList = new ArrayList<String>();
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);
    private EventFormatterConfiguration eventFormatterConfiguration = null;
    private int tenantId;
    private Map<String, Integer> propertyPositionMap = new TreeMap<String, Integer>();
    private OutputEventAdaptorConfiguration outputEventAdaptorConfiguration = null;
    private OutputMapper outputMapper = null;
    private String streamId = null;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private boolean dynamicMessagePropertyEnabled = false;
    private boolean customMappingEnabled = false;

    public EventFormatter(EventFormatterConfiguration eventFormatterConfiguration)
            throws EventFormatterConfigurationException {

        this.eventFormatterConfiguration = eventFormatterConfiguration;
        this.customMappingEnabled = eventFormatterConfiguration.getOutputMapping().isCustomMappingEnabled();
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String inputStreamName = eventFormatterConfiguration.getFromStreamName();
        String inputStreamVersion = eventFormatterConfiguration.getFromStreamVersion();

        //Stream Definition must same for any event source, There are cannot be different stream definition for same stream id in multiple event sourced
        StreamDefinition inputStreamDefinition = null;

        try {
            inputStreamDefinition = EventFormatterServiceValueHolder.getEventStreamService().getStreamDefinition(inputStreamName, inputStreamVersion, tenantId);

        } catch (EventStreamConfigurationException e) {
            throw new EventFormatterConfigurationException("Cannot retrieve the stream definition from stream store : " + e.getMessage());
        }

        if (inputStreamDefinition == null) {
            throw new EventFormatterConfigurationException("There is no any event stream for the corresponding stream name or version : " + inputStreamName + "-" + inputStreamVersion);
        }
        this.streamId = inputStreamDefinition.getStreamId();
        createPropertyPositionMap(inputStreamDefinition);
        outputMapper = EventFormatterServiceValueHolder.getMappingFactoryMap().get(eventFormatterConfiguration.getOutputMapping().getMappingType()).constructOutputMapper(eventFormatterConfiguration, propertyPositionMap, tenantId, inputStreamDefinition);
        setOutputEventAdaptorConfiguration(tenantId);

        Map<String, String> messageProperties = eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration().getOutputMessageProperties();
        for (Map.Entry<String, String> entry : messageProperties.entrySet()) {
            Map.Entry pairs = (Map.Entry) entry;
            getDynamicOutputMessageProperties(pairs.getValue() != null ? pairs.getValue().toString() : "");
        }

        if (dynamicMessagePropertyList.size() > 0) {
            dynamicMessagePropertyEnabled = true;
        }

        try {
            EventFormatterServiceValueHolder.getEventStreamService().subscribe(this, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventFormatterStreamValidationException("Stream " + streamId + " does not exist", streamId);
        }

        this.traceEnabled = eventFormatterConfiguration.isEnableTracing();
        this.statisticsEnabled = eventFormatterConfiguration.isEnableStatistics();
        if (statisticsEnabled) {
            this.statisticsMonitor = EventFormatterServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, EventFormatterConstants.EVENT_FORMATTER, eventFormatterConfiguration.getEventFormatterName(), null);
        }
        if (traceEnabled) {
            this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventFormatterConstants.EVENT_FORMATTER + " : " + eventFormatterConfiguration.getFromStreamName() + ", before processing " + System.getProperty("line.separator");
            this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventFormatterConstants.EVENT_FORMATTER + " : " + eventFormatterConfiguration.getFromStreamName() + ", after processing " + System.getProperty("line.separator");
        }
    }

    public EventFormatterConfiguration getEventFormatterConfiguration() {
        return eventFormatterConfiguration;
    }

    public void sendEventData(Object[] eventData) {

        Map<String, String> outputEventAdaptorMessageMap = new HashMap<String, String>(eventFormatterConfiguration.getToPropertyConfiguration().getOutputEventAdaptorMessageConfiguration().getOutputMessageProperties());
        OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration = new OutputEventAdaptorMessageConfiguration();
        outputEventAdaptorMessageConfiguration.setOutputMessageProperties(outputEventAdaptorMessageMap);

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
        } catch (EventFormatterConfigurationException e) {
            log.error("Cannot send event:" + Arrays.deepToString(eventData) + " from " + eventFormatterConfiguration.getEventFormatterName());
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
            changeDynamicEventAdaptorMessageProperties(eventData, outputEventAdaptorMessageConfiguration);
        }

        OutputEventAdaptorService eventAdaptorService = EventFormatterServiceValueHolder.getOutputEventAdaptorService();
        eventAdaptorService.publish(outputEventAdaptorConfiguration, outputEventAdaptorMessageConfiguration, outObject, tenantId);

    }

    private void setOutputEventAdaptorConfiguration(int tenantId)
            throws EventFormatterConfigurationException {
        OutputEventAdaptorManagerService eventAdaptorManagerService = EventFormatterServiceValueHolder.getOutputEventAdaptorManagerService();

        try {
            this.outputEventAdaptorConfiguration = eventAdaptorManagerService.getActiveOutputEventAdaptorConfiguration(eventFormatterConfiguration.getToPropertyConfiguration().getEventAdaptorName(), tenantId);
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the output event adaptor configuration of : " + eventFormatterConfiguration.getToPropertyConfiguration().getEventAdaptorName(), e);
        }

    }

    private void createPropertyPositionMap(StreamDefinition streamDefinition) {
        List<Attribute> metaAttributeList = streamDefinition.getMetaData();
        List<Attribute> correlationAttributeList = streamDefinition.getCorrelationData();
        List<Attribute> payloadAttributeList = streamDefinition.getPayloadData();

        int propertyCount = 0;
        if (metaAttributeList != null) {
            for (Attribute attribute : metaAttributeList) {
                propertyPositionMap.put(EventFormatterConstants.PROPERTY_META_PREFIX + attribute.getName(), propertyCount);
                propertyCount++;
            }
        }

        if (correlationAttributeList != null) {
            for (Attribute attribute : correlationAttributeList) {
                propertyPositionMap.put(EventFormatterConstants.PROPERTY_CORRELATION_PREFIX + attribute.getName(), propertyCount);
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

    private void changeDynamicEventAdaptorMessageProperties(Object[] eventData, OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration) {

        for (String dynamicMessageProperty : dynamicMessagePropertyList) {
            if (eventData.length != 0 && dynamicMessageProperty != null) {
                int position = propertyPositionMap.get(dynamicMessageProperty);
                changePropertyValue(position, dynamicMessageProperty, eventData, outputEventAdaptorMessageConfiguration);
            }
        }
    }

    private void changePropertyValue(int position, String messageProperty, Object[] eventData, OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration) {
        Map<String, String> outputMessageProperties = outputEventAdaptorMessageConfiguration.getOutputMessageProperties();

        for (Map.Entry<String, String> entry : outputMessageProperties.entrySet()) {
            String mapValue = "{{" + messageProperty + "}}";
            String regexValue = "\\{\\{" + messageProperty + "\\}\\}";
            String entryValue = entry.getValue();
            if (entryValue != null && entryValue.contains(mapValue)) {
                if(eventData[position] != null){
                    entry.setValue(entryValue.replaceAll(regexValue, eventData[position].toString()));
                }else {
                    entry.setValue(entryValue.replaceAll(regexValue, ""));
                }
            }
        }

    }

    public OutputEventAdaptorConfiguration getOutputEventAdaptorConfiguration() {
        return outputEventAdaptorConfiguration;
    }
}
