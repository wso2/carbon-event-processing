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

package org.wso2.carbon.event.simulator.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.simulator.admin.internal.util.EventSimulatorAdminvalueHolder;
import org.wso2.carbon.event.simulator.core.EventDetailsValue;
import org.wso2.carbon.event.simulator.core.EventSimulator;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.simulator.core.EventsDetail;


//import org.wso2.carbon.eventsimulator.core.EventSimulator;

import java.util.Collection;
import java.util.List;


public class EventSimulatorAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventSimulatorAdminService.class);

    public EventStreamInfoDto[] getAllEventStreamInfoDto(){



        EventSimulator eventSimulator= EventSimulatorAdminvalueHolder.getEventSimulator();

        try {
            Collection<StreamDefinition> eventStreamDefinitionList = eventSimulator.getAllEventStreamDefinitions();

            if (eventStreamDefinitionList != null) {

                EventStreamInfoDto[] eventStreamInfoDtos = new EventStreamInfoDto[eventStreamDefinitionList.size()];
                int index = 0;
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    eventStreamInfoDtos[index] = new EventStreamInfoDto();
                    eventStreamInfoDtos[index].setStreamName(streamDefinition.getName());
                    eventStreamInfoDtos[index].setStreamVersion(streamDefinition.getVersion());
                    eventStreamInfoDtos[index].setStreamDefinition(streamDefinition.toString());
                    eventStreamInfoDtos[index].setStreamDescription(streamDefinition.getDescription());

                    // Set Meta attributes to EventStreamInfoDtos
                    List<Attribute> meataDataAttributeList = streamDefinition.getMetaData();


                    if (meataDataAttributeList != null) {
                        EventStreamAttributeDto[] metaDataAttributeArray = new EventStreamAttributeDto[meataDataAttributeList.size()];
                        for (int i = 0; i < metaDataAttributeArray.length; i++) {

                            metaDataAttributeArray[i] = new EventStreamAttributeDto();
                            metaDataAttributeArray[i].setAttributeName(meataDataAttributeList.get(i).getName());
                            metaDataAttributeArray[i].setAttributeType(meataDataAttributeList.get(i).getType().toString());

                        }

                        eventStreamInfoDtos[index].setMetaAttributes(metaDataAttributeArray);
                    }
                    //Set correlation attributes to EventStreamInfoDtos
                    List<Attribute> correlationDataAttributeList = streamDefinition.getCorrelationData();


                    if (correlationDataAttributeList != null) {
                        EventStreamAttributeDto[] correlationDataAttributeArray = new EventStreamAttributeDto[correlationDataAttributeList.size()];

                        for (int j = 0; j < correlationDataAttributeArray.length; j++) {
                            correlationDataAttributeArray[j] = new EventStreamAttributeDto();
                            correlationDataAttributeArray[j].setAttributeName(correlationDataAttributeList.get(j).getName());
                            correlationDataAttributeArray[j].setAttributeType(correlationDataAttributeList.get(j).getType().toString());
                        }

                        eventStreamInfoDtos[index].setCorrelationAttributes(correlationDataAttributeArray);
                    }
                    //Set payload data attributes to EventStreamInfoDtos

                    List<Attribute> payloadDataAttributeList = streamDefinition.getPayloadData();


                    if (payloadDataAttributeList != null) {
                        EventStreamAttributeDto[] payloadDataAttributesArray = new EventStreamAttributeDto[payloadDataAttributeList.size()];
                        for (int k = 0; k < payloadDataAttributesArray.length; k++) {
                            payloadDataAttributesArray[k] = new EventStreamAttributeDto();
                            payloadDataAttributesArray[k].setAttributeName(payloadDataAttributeList.get(k).getName());
                            payloadDataAttributesArray[k].setAttributeType(payloadDataAttributeList.get(k).getType().toString());
                        }

                        eventStreamInfoDtos[index].setPayloadAttributes(payloadDataAttributesArray);
                    }
                    index++;
                }
                return eventStreamInfoDtos;

            } else {
                return new EventStreamInfoDto[0];
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }

        return new EventStreamInfoDto[0];
    }


    public void sendEvent(EventDto eventDetails) throws AxisFault {

        EventStreamAttributeValuesDto[] eventAttributeArray = eventDetails.getAttributes();

        EventSimulator eventSimulator = EventSimulatorAdminvalueHolder.getEventSimulator();

        EventDetailsValue[] eventDetailsvalueArray = new EventDetailsValue[eventAttributeArray.length];

        for (int i = 0; i < eventAttributeArray.length; i++) {

            eventDetailsvalueArray[i] = new EventDetailsValue();

            eventDetailsvalueArray[i].setAttributeName(eventAttributeArray[i].getAttributeName());
            eventDetailsvalueArray[i].setType(eventAttributeArray[i].getType());
            eventDetailsvalueArray[i].setValue(eventAttributeArray[i].getValue());

            if (eventAttributeArray[i].getValue().equals("")) {

                throw new AxisFault("Fill all the attribute fields");
            } else if (eventAttributeArray[i].getType().equals("INT") || eventAttributeArray[i].getType().equals("LONG")) {

                try {
                    int val1 = Integer.parseInt(eventAttributeArray[i].getValue());
                    long val2 = Long.parseLong(eventAttributeArray[i].getValue());
                } catch (NumberFormatException e) {
                    throw new AxisFault("Inappropriate value types for the attribute - " + eventAttributeArray[i].getAttributeName() + " expected " + eventAttributeArray[i].getType() + " : " + e.getMessage(), e);
                }
            } else if (eventAttributeArray[i].getType().equals("DOUBLE") || eventAttributeArray[i].getType().equals("FLOAT")) {
                try {
                    double val1 = Double.parseDouble(eventAttributeArray[i].getValue());
                    float val2 = Float.parseFloat(eventAttributeArray[i].getValue());
                } catch (NumberFormatException e) {
                    throw new AxisFault("Inappropriate value types for the attribute - " + eventAttributeArray[i].getAttributeName() + " expected " + eventAttributeArray[i].getType() + " : " + e.getMessage(), e);
                }
            } else if (eventAttributeArray[i].getType().equals("BOOLEAN")) {
                if (!Boolean.parseBoolean(eventAttributeArray[i].getValue())) {
                    throw new AxisFault("Inappropriate value types for the attribute - " + eventAttributeArray[i].getAttributeName() + " expected " + eventAttributeArray[i].getType());
                }
            }

        }

        EventsDetail eventDetailObject = new EventsDetail();

        eventDetailObject.setEventStreamName(eventDetails.getEventStreamName());
        eventDetailObject.setAttributes(eventDetailsvalueArray);
        eventSimulator.sendEventDetails(eventDetailObject);


    }
}
