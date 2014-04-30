/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.core.EventDetailsValue;
import org.wso2.carbon.event.simulator.core.EventSimulator;
import org.wso2.carbon.event.simulator.core.EventsDetail;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.Collection;
import java.util.HashMap;


public class CarbonEventSimulator implements EventSimulator {

    private static final Log log = LogFactory.getLog(CarbonEventSimulator.class);
    private HashMap<String, EventStreamProducer> eventProducerMap = new HashMap<String, EventStreamProducer>();


    public Collection<StreamDefinition> getAllEventStreamDefinitions() {
        try {
            EventStreamService Eventstreamservice = EventSimulatorValueHolder.getEventStreamService();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return Eventstreamservice.getAllStreamDefinitions(tenantId);

        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void sendEventDetails(EventsDetail eventDetail) {


        EventDetailsValue[] attributesValues = eventDetail.getAttributes();

        EventStreamService eventstreamservice = EventSimulatorValueHolder.getEventStreamService();


        Object[] dataObjects = new Object[attributesValues.length];

        for (int i = 0; i < dataObjects.length; i++) {

            dataObjects[i] = new Object();

            if (attributesValues[i].getType().equals("STRING")) {

                dataObjects[i] = attributesValues[i].getValue();
            } else if (attributesValues[i].getType().equals("INT")) {

                int val = Integer.parseInt(attributesValues[i].getValue());
                dataObjects[i] = val;
            } else if (attributesValues[i].getType().equals("LONG")) {

                long val = Long.parseLong(attributesValues[i].getValue());
                dataObjects[i] = val;
            } else if (attributesValues[i].getType().equals("DOUBLE")) {

                double val = Double.parseDouble(attributesValues[i].getValue());
                dataObjects[i] = val;
            } else if (attributesValues[i].getType().equals("FLOAT")) {

                float val = Float.parseFloat(attributesValues[i].getValue());
                dataObjects[i] = val;
            } else if (attributesValues[i].getType().equals("BOOLEAN")) {

                boolean val = Boolean.parseBoolean(attributesValues[i].getValue());
                dataObjects[i] = val;
            }
        }

        if (eventProducerMap.get(eventDetail.getEventStreamName()) != null) {

            EventStreamProducer eventProducer = eventProducerMap.get(eventDetail.getEventStreamName());
            eventProducer.sendData(dataObjects);

        } else {
            EventStreamProducer eventStreamProducer = new EventStreamProducer();

            eventStreamProducer.setStreamID(eventDetail.getEventStreamName());

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            try {
                eventstreamservice.subscribe(eventStreamProducer, tenantId);

            } catch (EventStreamConfigurationException e) {
                e.printStackTrace();
            }

            eventProducerMap.put(eventDetail.getEventStreamName(), eventStreamProducer);
            eventStreamProducer.sendData(dataObjects);

        }

    }


}
