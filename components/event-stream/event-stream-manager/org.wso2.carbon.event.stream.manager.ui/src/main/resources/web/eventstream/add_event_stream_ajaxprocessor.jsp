<!--
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
-->
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>

<%

    String msg = null;
    try {
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);

        String eventStreamName = request.getParameter("eventStreamName");
        String eventStreamVersion = request.getParameter("eventStreamVersion");
        String eventStreamDescription = request.getParameter("eventStreamDescription");
        String eventStreamNickName = request.getParameter("eventStreamNickName");

        String metaDataSet = request.getParameter("metaData");
        EventStreamAttributeDto[] metaWSO2EventAttributeDtos = null;

        if (metaDataSet != null && !metaDataSet.equals("")) {
            String[] properties = metaDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                metaWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        metaWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        metaWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        metaWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }

        String correlationDataSet = request.getParameter("correlationData");
        EventStreamAttributeDto[] correlationWSO2EventAttributeDtos = null;

        if (correlationDataSet != null && !correlationDataSet.equals("")) {
            String[] properties = correlationDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                correlationWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        correlationWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        correlationWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        correlationWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }

        String payloadDataSet = request.getParameter("payloadData");
        EventStreamAttributeDto[] payloadWSO2EventAttributeDtos = null;

        if (payloadDataSet != null && !payloadDataSet.equals("")) {
            String[] properties = payloadDataSet.split("\\$=");
            if (properties != null) {
                // construct property array for each property
                payloadWSO2EventAttributeDtos = new EventStreamAttributeDto[properties.length];
                int index = 0;
                for (String property : properties) {
                    String[] propertyConfiguration = property.split("\\^=");
                    if (propertyConfiguration != null) {
                        payloadWSO2EventAttributeDtos[index] = new EventStreamAttributeDto();
                        payloadWSO2EventAttributeDtos[index].setAttributeName(propertyConfiguration[0].trim());
                        payloadWSO2EventAttributeDtos[index].setAttributeType(propertyConfiguration[1].trim());
                        index++;
                    }
                }

            }
        }

        stub.addEventStreamInfo(eventStreamName, eventStreamVersion, metaWSO2EventAttributeDtos, correlationWSO2EventAttributeDtos, payloadWSO2EventAttributeDtos, eventStreamDescription, eventStreamNickName);
        msg = "true";

    } catch (Exception e) {
        msg = e.getMessage();
    }
%>
<%=msg%>
