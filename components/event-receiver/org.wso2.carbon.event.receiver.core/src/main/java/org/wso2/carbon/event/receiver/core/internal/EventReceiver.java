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
package org.wso2.carbon.event.receiver.core.internal;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.receiver.core.InputEventAdaptorListener;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.receiver.core.config.InputMapper;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverProcessingException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.exception.InputEventAdaptorEventProcessingException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverRuntimeValidator;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.stream.manager.core.EventProducer;
import org.wso2.carbon.event.stream.manager.core.EventProducerCallback;

import java.util.Arrays;
import java.util.List;

public class EventReceiver implements EventProducer {

    private static final Log log = LogFactory.getLog(EventReceiver.class);
    private boolean traceEnabled = false;
    private boolean statisticsEnabled = false;
    private boolean customMappingEnabled = false;
    private Logger trace = Logger.getLogger(EventReceiverConstants.EVENT_TRACE_LOGGER);
    private EventReceiverConfiguration eventReceiverConfiguration = null;
    private AxisConfiguration axisConfiguration;
    private StreamDefinition exportedStreamDefinition;
    private InputMapper inputMapper = null;
    private String subscriptionId;
    private EventStatisticsMonitor statisticsMonitor;
    private String beforeTracerPrefix;
    private String afterTracerPrefix;
    private EventProducerCallback callBack;

    public EventReceiver(EventReceiverConfiguration eventReceiverConfiguration,
                         StreamDefinition exportedStreamDefinition,
                         AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {
        this.axisConfiguration = axisConfiguration;
        this.eventReceiverConfiguration = eventReceiverConfiguration;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (this.eventReceiverConfiguration != null) {
            this.traceEnabled = eventReceiverConfiguration.isTraceEnabled();
            this.statisticsEnabled = eventReceiverConfiguration.isStatisticsEnabled();
            this.customMappingEnabled = eventReceiverConfiguration.getInputMapping().isCustomMappingEnabled();
            String mappingType = this.eventReceiverConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(this.eventReceiverConfiguration, exportedStreamDefinition);

            // The input mapper should not be null. For configurations where custom mapping is disabled,
            // an input mapper would be created without the mapping details
            if (this.inputMapper != null) {
                if (customMappingEnabled) {
                    EventReceiverRuntimeValidator.validateExportedStream(eventReceiverConfiguration, exportedStreamDefinition, this.inputMapper);
                }
                this.exportedStreamDefinition = exportedStreamDefinition;
            } else {
                throw new EventReceiverConfigurationException("Could not create input mapper for mapping type "
                        + mappingType + " for event builder :" + eventReceiverConfiguration.getEventReceiverName());
            }

            // Initialize tracer and statistics.
            if (statisticsEnabled) {
                this.statisticsMonitor = EventReceiverServiceValueHolder.getEventStatisticsService().getEventStatisticMonitor(
                        tenantId, EventReceiverConstants.EVENT_BUILDER, eventReceiverConfiguration.getEventReceiverName(), null);
            }
            if (traceEnabled) {
                this.beforeTracerPrefix = "TenantId=" + tenantId + " : " + EventReceiverConstants.EVENT_BUILDER + " : "
                        + eventReceiverConfiguration.getEventReceiverName() + ", before processing " + System.getProperty("line.separator");
                this.afterTracerPrefix = "TenantId=" + tenantId + " : " + EventReceiverConstants.EVENT_BUILDER + " : "
                        + eventReceiverConfiguration.getEventReceiverName() + " : " + EventReceiverConstants.EVENT_STREAM + " : "
                        + EventReceiverUtil.getExportedStreamIdFrom(eventReceiverConfiguration) + " , after processing " + System.getProperty("line.separator");
            }
        }
    }

    public int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Returns the stream definition that is exported by this event builder.
     * This stream definition will available to any object that consumes the event builder service
     * (e.g. EventProcessors)
     *
     * @return the {@link org.wso2.carbon.databridge.commons.StreamDefinition} of the stream that will be
     *         sending out events from this event builder
     */
    public StreamDefinition getExportedStreamDefinition() {
        return exportedStreamDefinition;
    }

    /**
     * Returns the event builder configuration associated with this event builder
     *
     * @return the {@link EventReceiverConfiguration} instance
     */
    public EventReceiverConfiguration getEventReceiverConfiguration() {
        return this.eventReceiverConfiguration;
    }

    /**
     * Subscribes to a event adaptor according to the current event builder configuration
     */
    public void subscribeToEventAdaptor() throws EventReceiverConfigurationException {
        if (this.eventReceiverConfiguration == null || this.inputMapper == null) {
            throw new EventReceiverConfigurationException("Cannot subscribe to input event adaptor. Event builder has not been initialized properly.");
        }
        if (this.subscriptionId == null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String inputEventAdaptorName = eventReceiverConfiguration.getInputEventAdaptorConfiguration().getInputEventAdaptorName();
            try {

                if (this.customMappingEnabled) {
                    this.subscriptionId = EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService().subscribe(
                            eventReceiverConfiguration.getInputEventAdaptorConfiguration(),
                            new MappedEventListenerImpl(), axisConfiguration);
                } else {
                    this.subscriptionId = EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService().subscribe(
                            eventReceiverConfiguration.getInputEventAdaptorConfiguration(),
                            new TypedEventListenerImpl(), axisConfiguration);
                }
            } catch (InputEventAdaptorEventProcessingException e) {
                throw new EventReceiverValidationException("Cannot subscribe to input event adaptor :" + inputEventAdaptorName + ", error processing connection by adaptor.", inputEventAdaptorName, e);
            }
        }
    }

    /**
     * Unsubscribes from the input event adaptor that corresponds to the passed in configuration
     *
     * @param inputEventAdaptorConfiguration the configuration of the input event adaptor from which unsubscribing happens
     */
    public void unsubscribeFromEventAdaptor(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration)
            throws EventReceiverConfigurationException {
        if (inputEventAdaptorConfiguration != null && this.subscriptionId != null) {
            EventReceiverServiceValueHolder.getCarbonInputEventAdaptorService().unsubscribe(
                    eventReceiverConfiguration.getInputEventAdaptorConfiguration(),
                    axisConfiguration, this.subscriptionId);
        }
        this.subscriptionId = null;
    }

    protected void processMappedEvent(Object object) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + object.toString());
        }

        try {
            if (object instanceof List) {
                sendEventList((List<Event>) object);
            } else {
                Object convertedEvent = this.inputMapper.convertToMappedInputEvent(object);
                if (convertedEvent != null) {
                    if (convertedEvent instanceof Object[][]) {
                        Object[][] arrayOfEvents = (Object[][]) convertedEvent;
                        for (Object[] outObjArray : arrayOfEvents) {
                            sendEvent(outObjArray);
                        }
                    } else {
                        sendEvent((Object[]) convertedEvent);
                    }
                } else {
                    log.warn("Dropping the empty/null event, Event does not matched with mapping");
                }
            }
        } catch (EventReceiverProcessingException e) {
            log.error("Dropping event, Error processing event : " + e.getMessage(), e);
        }

    }

    protected void processTypedEvent(Object obj) {
        if (traceEnabled) {
            trace.info(beforeTracerPrefix + obj.toString());
        }
        Object convertedEvent = null;
        try {
            if (obj instanceof List) {
                sendEventList((List<Event>) obj);
            } else {
                convertedEvent = this.inputMapper.convertToTypedInputEvent(obj);
                if (convertedEvent instanceof Object[][]) {
                    Object[][] arrayOfEvents = (Object[][]) convertedEvent;
                    for (Object[] outObjArray : arrayOfEvents) {
                        sendEvent(outObjArray);
                    }
                } else {
                    sendEvent((Object[]) convertedEvent);
                }
            }
        } catch (EventReceiverProcessingException e) {
            log.error("Dropping event, Error processing event: " + e.getMessage(), e);
        }
    }

    protected void sendEvent(Object[] outObjArray) {
        if (traceEnabled) {
            trace.info(afterTracerPrefix + Arrays.toString(outObjArray));
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        this.callBack.sendEventData(outObjArray);
    }

    protected void sendEventList(List<Event> events) {
        if (traceEnabled) {
            trace.info(afterTracerPrefix + events);
        }
        if (statisticsEnabled) {
            statisticsMonitor.incrementRequest();
        }
        this.callBack.sendEvents(events);
    }

    protected void defineEventStream(Object definition) throws EventReceiverConfigurationException {
        if (log.isDebugEnabled()) {
            log.debug("EventReceiver: " + eventReceiverConfiguration.getEventReceiverName() + ", notifying event definition addition :" + definition.toString());
        }
        if (definition instanceof StreamDefinition) {
            StreamDefinition inputStreamDefinition = (StreamDefinition) definition;
            String mappingType = eventReceiverConfiguration.getInputMapping().getMappingType();
            this.inputMapper = EventReceiverServiceValueHolder.getMappingFactoryMap().get(mappingType).constructInputMapper(eventReceiverConfiguration, exportedStreamDefinition);
        }
    }

    protected void removeEventStream(Object definition) {
        if (log.isDebugEnabled()) {
            log.debug("EventReceiver: " + eventReceiverConfiguration.getEventReceiverName() + ", notifying event definition addition :" + definition.toString());
        }
        this.inputMapper = null;
    }

    @Override
    public String getStreamId() {
        return exportedStreamDefinition.getStreamId();
    }

    @Override
    public void setCallBack(EventProducerCallback callBack) {
        this.callBack = callBack;
    }

    private class MappedEventListenerImpl extends InputEventAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            try {
                defineEventStream(o);
            } catch (EventReceiverConfigurationException e) {
                log.error("Error in adding event definition : " + e.getMessage(), e);
            }
        }

        @Override
        public void removeEventDefinition(Object o) {
            removeEventStream(o);


        }

        @Override
        public void onEvent(Object o) {
            processMappedEvent(o);
        }
    }

    private class TypedEventListenerImpl extends InputEventAdaptorListener {

        @Override
        public void addEventDefinition(Object o) {
            try {
                defineEventStream(o);
            } catch (EventReceiverConfigurationException e) {
                log.error("Error in adding event definition : " + e.getMessage(), e);
            }
        }

        @Override
        public void removeEventDefinition(Object o) {
            removeEventStream(o);
        }

        @Override
        public void onEvent(Object o) {
            processTypedEvent(o);
        }
    }
}
