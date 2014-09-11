<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
--%>
<%@ page import="org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventBuilderConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.EventInputPropertyConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.builder.stub.types.PropertyDto" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIConstants" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>

<%
    // get required parameters to add a event builder to back end.
    EventBuilderAdminServiceStub stub = EventBuilderUIUtils.getEventBuilderAdminService(config, session, request);
    String eventBuilderName = request.getParameter("eventBuilderName");
    String msg = null;

    EventBuilderConfigurationInfoDto[] eventBuilderConfigurationInfoDtoArray = null;
    if (stub != null) {
        try {
            eventBuilderConfigurationInfoDtoArray = stub.getAllActiveEventBuilderConfigurations();
        } catch (Exception e) {
%>
<script type="text/javascript">
    location.href = 'index.jsp?ordinal=1';</script>
<%
            return;
        }
    }

    if (eventBuilderConfigurationInfoDtoArray != null) {
        for (EventBuilderConfigurationInfoDto eventBuilderConfiguration : eventBuilderConfigurationInfoDtoArray) {
            if (eventBuilderConfiguration.getEventBuilderName().equals(eventBuilderName)) {
                msg = eventBuilderName + " already exists.";
                break;
            }
        }
    }

    if (stub != null) {
        if (msg == null) {
            String inputMappingType = request.getParameter("mappingType");
            String toStreamName = request.getParameter("toStreamName");
            String toStreamVersion = request.getParameter("toStreamVersion");
            String streamNameWithVersion = toStreamName + EventBuilderUIConstants.STREAM_VER_DELIMITER + toStreamVersion;
            String eventAdaptorInfo = request.getParameter("eventAdaptorInfo");
            String[] eventAdaptorNameAndType = eventAdaptorInfo.split("\\$=");
            // property set contains a set of properties, eg; userName$myName|url$http://wso2.org|
            String msgConfigPropertySet = request.getParameter("msgConfigPropertySet");
            PropertyDto[] msgConfigProperties = null;

            if (msgConfigPropertySet != null) {
                String[] properties = msgConfigPropertySet.split("\\|=");
                if (properties != null) {
                    // construct event builder property array for each event builder property
                    msgConfigProperties = new PropertyDto[properties.length];
                    int index = 0;
                    for (String property : properties) {
                        String[] propertyNameAndValue = property.split("\\$=");
                        if (propertyNameAndValue != null) {
                            msgConfigProperties[index] = new PropertyDto();
                            msgConfigProperties[index].setKey(propertyNameAndValue[0].trim());
                            msgConfigProperties[index].setValue(propertyNameAndValue[1].trim());
                            index++;
                        }
                    }
                }
            }

            if (msgConfigProperties == null) {
                msg = "No message configuration properties found.";
%>
<%=msg%>
<%
                return;
            }
            if (inputMappingType.equals("wso2event")) {
                EventInputPropertyConfigurationDto[] metaEbProperties = null;
                EventInputPropertyConfigurationDto[] correlationEbProperties = null;
                EventInputPropertyConfigurationDto[] payloadEbProperties = null;
                String customMapping = request.getParameter("customMappingValue");

                if (customMapping.equalsIgnoreCase(EventBuilderUIConstants.STRING_LITERAL_ENABLE)) {
                    String metaPropertySet = request.getParameter("metaData");

                    if (metaPropertySet != null && !metaPropertySet.isEmpty()) {
                        String[] properties = metaPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            metaEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    metaEbProperties[index] = new EventInputPropertyConfigurationDto();
                                    metaEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                    metaEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                    metaEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                    index++;
                                }
                            }
                        }

                    }
                    String correlationPropertySet = request.getParameter("correlationData");

                    if (correlationPropertySet != null && !correlationPropertySet.isEmpty()) {
                        String[] properties = correlationPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            correlationEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    correlationEbProperties[index] = new EventInputPropertyConfigurationDto();
                                    correlationEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                    correlationEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                    correlationEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                    index++;
                                }
                            }
                        }

                    }
                    String payloadPropertySet = request.getParameter("payloadData");

                    if (payloadPropertySet != null && !payloadPropertySet.isEmpty()) {
                        String[] properties = payloadPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            payloadEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyNameValueAndType = property.split("\\^=");
                                if (propertyNameValueAndType != null) {
                                    payloadEbProperties[index] = new EventInputPropertyConfigurationDto();
                                    payloadEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                    payloadEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                    payloadEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                    index++;
                                }
                            }
                        }

                    }
                }
                stub.deployWso2EventBuilderConfiguration(eventBuilderName,streamNameWithVersion, eventAdaptorNameAndType[0],
                        eventAdaptorNameAndType[1], metaEbProperties, correlationEbProperties, payloadEbProperties,
                        msgConfigProperties, EventBuilderUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
            } else if (inputMappingType.equals("xml")) {

                String prefixPropertySet = request.getParameter("prefixData");
                String parentSelectorXpath = request.getParameter("parentSelectorXpath");
                PropertyDto[] prefixEbProperties = null;
                EventInputPropertyConfigurationDto[] xpathEbProperties = null;

                String customMapping = request.getParameter("customMappingValue");
                if (customMapping.equalsIgnoreCase("enable")) {
                    if (prefixPropertySet != null && !prefixPropertySet.isEmpty()) {
                        String[] properties = prefixPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            prefixEbProperties = new PropertyDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] xpathPrefixAndNs = property.split("\\^=");
                                if (xpathPrefixAndNs != null) {
                                    prefixEbProperties[index] = new PropertyDto();
                                    prefixEbProperties[index].setKey(xpathPrefixAndNs[0].trim());
                                    prefixEbProperties[index].setValue(xpathPrefixAndNs[1].trim());
                                    index++;
                                }
                            }
                        }
                    }
                    String xpathPropertySet = request.getParameter("xpathData");
                    if (xpathPropertySet != null && !xpathPropertySet.isEmpty()) {
                        String[] properties = xpathPropertySet.split("\\$=");
                        if (properties != null) {
                            // construct event builder property array for each event builder property
                            xpathEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                            int index = 0;
                            for (String property : properties) {
                                String[] propertyStringArr = property.split("\\^=");
                                if (propertyStringArr != null) {
                                    xpathEbProperties[index] = new EventInputPropertyConfigurationDto();
                                    xpathEbProperties[index].setName(propertyStringArr[1].trim());
                                    xpathEbProperties[index].setValueOf(propertyStringArr[0].trim());
                                    xpathEbProperties[index].setType(propertyStringArr[2].trim());
                                    if (propertyStringArr.length >= 4) {
                                        xpathEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                    }
                                    index++;
                                }
                            }
                        }

                    }
                }

                stub.deployXmlEventBuilderConfiguration(eventBuilderName, streamNameWithVersion,eventAdaptorNameAndType[0],
                        eventAdaptorNameAndType[1],xpathEbProperties,msgConfigProperties,prefixEbProperties,parentSelectorXpath,
                        EventBuilderUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
            } else if (inputMappingType.equals("map")) {
                String payloadPropertySet = request.getParameter("mapData");
                EventInputPropertyConfigurationDto[] mapEbProperties = null;
                String customMapping = request.getParameter("customMappingValue");
                if (payloadPropertySet != null && !payloadPropertySet.isEmpty()) {
                    String[] properties = payloadPropertySet.split("\\$=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        mapEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyNameValueAndType = property.split("\\^=");
                            if (propertyNameValueAndType != null) {
                                mapEbProperties[index] = new EventInputPropertyConfigurationDto();
                                mapEbProperties[index].setName(propertyNameValueAndType[0].trim());
                                mapEbProperties[index].setValueOf(propertyNameValueAndType[1].trim());
                                mapEbProperties[index].setType(propertyNameValueAndType[2].trim());
                                index++;
                            }
                        }
                    }

                }

                stub.deployMapEventBuilderConfiguration(eventBuilderName, streamNameWithVersion, eventAdaptorNameAndType[0],
                        eventAdaptorNameAndType[1],mapEbProperties,msgConfigProperties,
                        EventBuilderUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
            } else if (inputMappingType.equals("text")) {
                String textPropertySet = request.getParameter("textData");
                EventInputPropertyConfigurationDto[] textEbProperties = null;

                String customMapping = request.getParameter("customMappingValue");
                if (textPropertySet != null && !textPropertySet.isEmpty()) {
                    String[] properties = textPropertySet.split("\\$=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        textEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyStringArr = property.split("\\^=");
                            if (propertyStringArr != null) {
                                textEbProperties[index] = new EventInputPropertyConfigurationDto();
                                textEbProperties[index].setName(propertyStringArr[0].trim());
                                textEbProperties[index].setValueOf(propertyStringArr[1].trim());
                                textEbProperties[index].setType(propertyStringArr[2].trim());
                                if (propertyStringArr.length >= 4) {
                                    textEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                }
                                index++;
                            }
                        }
                    }

                }
                stub.deployTextEventBuilderConfiguration(eventBuilderName, streamNameWithVersion, eventAdaptorNameAndType[0],
                        eventAdaptorNameAndType[1],textEbProperties, msgConfigProperties,
                        EventBuilderUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
            } else if (inputMappingType.equals("json")) {
                String customMapping = request.getParameter("customMappingValue");
                String jsonPropertySet = request.getParameter("jsonData");
                EventInputPropertyConfigurationDto[] jsonEbProperties = null;
                if (jsonPropertySet != null && !jsonPropertySet.isEmpty()) {
                    String[] properties = jsonPropertySet.split("\\*=");
                    if (properties != null) {
                        // construct event builder property array for each event builder property
                        jsonEbProperties = new EventInputPropertyConfigurationDto[properties.length];
                        int index = 0;
                        for (String property : properties) {
                            String[] propertyStringArr = property.split("\\^=");
                            if (propertyStringArr != null) {
                                jsonEbProperties[index] = new EventInputPropertyConfigurationDto();
                                jsonEbProperties[index].setName(propertyStringArr[1].trim());
                                jsonEbProperties[index].setValueOf(propertyStringArr[0].trim());
                                jsonEbProperties[index].setType(propertyStringArr[2].trim());
                                if (propertyStringArr.length >= 4) {
                                    jsonEbProperties[index].setDefaultValue(propertyStringArr[3].trim());
                                }
                                index++;
                            }
                        }
                    }

                }
                stub.deployJsonEventBuilderConfiguration(eventBuilderName, streamNameWithVersion, eventAdaptorNameAndType[0],
                        eventAdaptorNameAndType[1],jsonEbProperties,msgConfigProperties,
                        EventBuilderUIConstants.STRING_LITERAL_ENABLE.equalsIgnoreCase(customMapping));
            }

            msg = "true";
        }
    }
%><%=msg%>
