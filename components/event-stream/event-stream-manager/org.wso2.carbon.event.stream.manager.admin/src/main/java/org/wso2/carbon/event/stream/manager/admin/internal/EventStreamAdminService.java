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
package org.wso2.carbon.event.stream.manager.admin.internal;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.manager.admin.internal.util.EventAttributeTypeConstants;
import org.wso2.carbon.event.stream.manager.admin.internal.util.EventStreamAdminServiceValueHolder;
import org.wso2.carbon.event.stream.manager.admin.internal.util.EventStreamManagerConstants;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.Collection;
import java.util.List;

public class EventStreamAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventStreamAdminService.class);

    public void addEventStreamInfo(String eventStreamName,
                                   String eventStreamVersion,
                                   EventStreamAttributeDto[] metaEventStreamAttributeDtos,
                                   EventStreamAttributeDto[] correlationEventStreamAttributeDtos,
                                   EventStreamAttributeDto[] payloadEventStreamAttributeDtos,
                                   String eventStreamDescription, String eventStreamNickName)
            throws AxisFault {

        if ((eventStreamName != null) && (!eventStreamName.isEmpty())) {
            if ((eventStreamVersion != null) && (!eventStreamVersion.isEmpty())) {

                try {
                    StreamDefinition streamDefinition = new StreamDefinition(eventStreamName, eventStreamVersion);
                    streamDefinition.setDescription(eventStreamDescription);
                    streamDefinition.setNickName(eventStreamNickName);
                    if (metaEventStreamAttributeDtos != null) {
                        for (EventStreamAttributeDto eventStreamAttributeDto : metaEventStreamAttributeDtos) {
                            streamDefinition.addMetaData(eventStreamAttributeDto.getAttributeName(), EventAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(eventStreamAttributeDto.getAttributeType()));
                        }
                    }
                    if (correlationEventStreamAttributeDtos != null) {
                        for (EventStreamAttributeDto eventStreamAttributeDto : correlationEventStreamAttributeDtos) {
                            streamDefinition.addCorrelationData(eventStreamAttributeDto.getAttributeName(), EventAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(eventStreamAttributeDto.getAttributeType()));
                        }
                    }
                    if (payloadEventStreamAttributeDtos != null) {
                        for (EventStreamAttributeDto eventStreamAttributeDto : payloadEventStreamAttributeDtos) {
                            streamDefinition.addPayloadData(eventStreamAttributeDto.getAttributeName(), EventAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(eventStreamAttributeDto.getAttributeType()));
                        }
                    }
                    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                    EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
                    eventStreamService.addEventStreamDefinition(streamDefinition, tenantId);

                } catch (MalformedStreamDefinitionException e) {
                    throw new AxisFault("Not a valid stream definition " + e.getMessage());
                } catch (EventStreamConfigurationException e) {
                    throw new AxisFault(e.getMessage() + " : " + e);
                }


            } else {
                throw new AxisFault("Not a valid event stream version");
            }


        } else {
            throw new AxisFault("Not a valid event stream name");
        }
    }


    public void editEventStreamInfo(String oldStreamId, String eventStreamName,
                                    String eventStreamVersion,
                                    EventStreamAttributeDto[] metaEventStreamAttributeDtos,
                                    EventStreamAttributeDto[] correlationEventStreamAttributeDtos,
                                    EventStreamAttributeDto[] payloadEventStreamAttributeDtos,
                                    String eventStreamDescription, String eventStreamNickName)
            throws AxisFault {

        if ((eventStreamName != null) && (!eventStreamName.isEmpty())) {
            if ((eventStreamVersion != null) && (!eventStreamVersion.isEmpty())) {

                String[] oldStreamProperties = oldStreamId.split(":");

                try {
                    StreamDefinition streamDefinition = new StreamDefinition(eventStreamName, eventStreamVersion);
                    streamDefinition.setDescription(eventStreamDescription);
                    streamDefinition.setNickName(eventStreamNickName);
                    if (metaEventStreamAttributeDtos != null) {
                        for (EventStreamAttributeDto eventStreamAttributeDto : metaEventStreamAttributeDtos) {
                            streamDefinition.addMetaData(eventStreamAttributeDto.getAttributeName(), EventAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(eventStreamAttributeDto.getAttributeType()));
                        }
                    }
                    if (correlationEventStreamAttributeDtos != null) {
                        for (EventStreamAttributeDto eventStreamAttributeDto : correlationEventStreamAttributeDtos) {
                            streamDefinition.addCorrelationData(eventStreamAttributeDto.getAttributeName(), EventAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(eventStreamAttributeDto.getAttributeType()));
                        }
                    }
                    if (payloadEventStreamAttributeDtos != null) {
                        for (EventStreamAttributeDto eventStreamAttributeDto : payloadEventStreamAttributeDtos) {
                            streamDefinition.addPayloadData(eventStreamAttributeDto.getAttributeName(), EventAttributeTypeConstants.STRING_ATTRIBUTE_TYPE_MAP.get(eventStreamAttributeDto.getAttributeType()));
                        }
                    }
                    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

                    EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
                    eventStreamService.removeEventStreamDefinition(oldStreamProperties[0], oldStreamProperties[1], tenantId);
                    eventStreamService.addEventStreamDefinition(streamDefinition, tenantId);

                } catch (MalformedStreamDefinitionException e) {
                    throw new AxisFault("Not a valid stream definition " + e.getMessage());
                } catch (EventStreamConfigurationException e) {
                    throw new AxisFault(e.getMessage() + " : " + e);
                }


            } else {
                throw new AxisFault("Not a valid event stream version");
            }


        } else {
            throw new AxisFault("Not a valid event stream name");
        }
    }


    public void removeEventStreamInfo(String eventStreamName,
                                      String eventStreamVersion)
            throws AxisFault {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if ((eventStreamName != null) && (!eventStreamName.isEmpty())) {
            if ((eventStreamVersion != null) && (!eventStreamVersion.isEmpty())) {
                EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
                try {
                    eventStreamService.removeEventStreamDefinition(eventStreamName, eventStreamVersion, tenantId);
                } catch (EventStreamConfigurationException e) {
                    throw new AxisFault(e.getMessage() + " : " + e.toString());
                }

            } else {
                throw new AxisFault("Not a valid event stream version");
            }
        } else {
            throw new AxisFault("Not a valid event stream name");
        }
    }

    public EventStreamInfoDto[] getAllEventStreamInfoDto() throws AxisFault {

        EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Collection<StreamDefinition> eventStreamDefinitionList = eventStreamService.getAllStreamDefinitions(tenantId);
            if (eventStreamDefinitionList != null) {
                EventStreamInfoDto[] eventStreamInfoDtos = new EventStreamInfoDto[eventStreamDefinitionList.size()];
                int index = 0;
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    eventStreamInfoDtos[index] = new EventStreamInfoDto();
                    eventStreamInfoDtos[index].setStreamName(streamDefinition.getName());
                    eventStreamInfoDtos[index].setStreamVersion(streamDefinition.getVersion());
                    eventStreamInfoDtos[index].setStreamDefinition(streamDefinition.toString());
                    eventStreamInfoDtos[index].setStreamDescription(streamDefinition.getDescription());
                    index++;
                }
                return eventStreamInfoDtos;
            } else {
                return new EventStreamInfoDto[0];
            }

        } catch (EventStreamConfigurationException e) {
            throw new AxisFault("Error while retrieving event streams from store : " + e.getMessage(), e);
        }
    }

    public String[] getStreamNames() throws AxisFault {
        EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
        if (eventStreamService != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            try {
                List<String> streamIdList = eventStreamService.getStreamIds(tenantId);
                if (streamIdList != null) {
                    String[] streamIdArray = new String[streamIdList.size()];
                    for (int i = 0; i < streamIdList.size(); i++) {
                        streamIdArray[i] = streamIdList.get(i);
                    }
                    return streamIdArray;
                }
            } catch (EventStreamConfigurationException e) {
                throw new AxisFault("Error while retrieving stream names from store : " + e.getMessage(), e);
            }
        }
        return new String[0];
    }

    public String[] getStreamDetailsForStreamId(String streamId) throws AxisFault {
        EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
        if (eventStreamService != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = eventStreamService.getStreamDefinition(streamId, tenantId);
                String[] streamDetails = new String[2];
                streamDetails[0] = streamDefinition.toString();
                streamDetails[1] = generateSampleEvent(streamId, EventAttributeTypeConstants.xmlEvent);
                return streamDetails;
            } catch (EventStreamConfigurationException e) {
                throw new AxisFault("Error while retrieving stream definition from store : " + e.getMessage(), e);
            }
        }
        return new String[0];
    }

    public String generateSampleEvent(String streamId, String eventType) throws AxisFault {

        EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
        if (eventStreamService != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            try {
                return eventStreamService.generateSampleEvent(streamId, eventType, tenantId);
            } catch (EventStreamConfigurationException e) {
                throw new AxisFault("Error while generating sample event");
            }
        }


        return "";
    }

    public String getStreamDefinitionAsString(String streamId) throws AxisFault {
        EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
        if (eventStreamService != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = eventStreamService.getStreamDefinition(streamId, tenantId);
            } catch (EventStreamConfigurationException e) {
                throw new AxisFault("Error while retrieving stream definition from store : " + e.getMessage(), e);
            }

            String definitionString = "";
            boolean appendComma = false;

            if (streamDefinition.getMetaData() != null) {

                for (Attribute attribute : streamDefinition.getMetaData()) {
                    if (appendComma) {
                        definitionString = definitionString + ", ";
                    }
                    definitionString = definitionString + EventStreamManagerConstants.META + EventStreamManagerConstants.ATTRIBUTE_SEPARATOR + attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    appendComma = true;
                }
            }
            if (streamDefinition.getCorrelationData() != null) {

                for (Attribute attribute : streamDefinition.getCorrelationData()) {
                    if (appendComma) {
                        definitionString = definitionString + ", ";
                    }
                    definitionString = definitionString + EventStreamManagerConstants.CORRELATION + EventStreamManagerConstants.ATTRIBUTE_SEPARATOR + attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    appendComma = true;
                }
            }
            if (streamDefinition.getPayloadData() != null) {

                for (Attribute attribute : streamDefinition.getPayloadData()) {
                    if (appendComma) {
                        definitionString = definitionString + ", ";
                    }
                    definitionString = definitionString + attribute.getName() + " " + attribute.getType().name().toLowerCase();
                    appendComma = true;
                }
            }
            return definitionString;
        }
        return null;
    }

    public EventStreamDefinitionDto getStreamDefinitionDto(String streamId) throws AxisFault {

        EventStreamService eventStreamService = EventStreamAdminServiceValueHolder.getEventStreamService();
        if (eventStreamService != null) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = eventStreamService.getStreamDefinition(streamId, tenantId);
                EventStreamDefinitionDto dto = new EventStreamDefinitionDto();
                dto.setName(streamDefinition.getName());
                dto.setVersion(streamDefinition.getVersion());
                dto.setDescription(streamDefinition.getDescription());
                dto.setNickName(streamDefinition.getNickName());
                dto.setMetaData(convertAttributeList(streamDefinition.getMetaData()));
                dto.setCorrelationData(convertAttributeList(streamDefinition.getCorrelationData()));
                dto.setPayloadData(convertAttributeList(streamDefinition.getPayloadData()));
                return dto;

            } catch (EventStreamConfigurationException e) {
                throw new AxisFault("Error while retrieving stream definition from store : " + e.getMessage(), e);
            }

        }
        return null;
    }

    private EventStreamAttributeDto[] convertAttributeList(
            List<org.wso2.carbon.databridge.commons.Attribute> attributeList) {
        if (attributeList != null) {
            EventStreamAttributeDto[] convertedAttributes = new EventStreamAttributeDto[attributeList.size()];
            int i = 0;
            for (org.wso2.carbon.databridge.commons.Attribute attribute : attributeList) {
                convertedAttributes[i] = new EventStreamAttributeDto();
                convertedAttributes[i].setAttributeName(attribute.getName());
                convertedAttributes[i].setAttributeType(attribute.getType().toString().toLowerCase());
                i++;
            }
            return convertedAttributes;
        }
        return new EventStreamAttributeDto[0];
    }


}