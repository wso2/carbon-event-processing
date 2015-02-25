<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EndpointPropertyConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventPublisherPropertyDto" %>
<%--
  ~ Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">

    <carbon:breadcrumb
            label="details"
            resourceBundle="org.wso2.carbon.event.publisher.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
        <h2><fmt:message key="event.publisher.details"/></h2>

        <div id="workArea">
            <table id="eventInputTable" class="styledLeft"
                   style="width:100%">
                <tbody>
                <%
                    String eventName = request.getParameter("eventName");
                    String eventType = request.getParameter("eventType");
                    if (eventName != null) {
                        EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
                        EventPublisherConfigurationDto eventPublisherConfigurationDto = stub.getActiveEventPublisherConfiguration(eventName);
                        EndpointPropertyConfigurationDto endpointPropertyConfigurationDto = eventPublisherConfigurationDto.getEndpointPropertyConfigurationDto();
                        EventPublisherPropertyDto[] eventPublisherPropertyDtos = endpointPropertyConfigurationDto.getOutputEventAdaptorConfiguration();


                %>
                <tr>
                    <td class="leftCol-small"><fmt:message key="event.adaptor.name"/></td>
                    <td><input type="text" name="eventName" id="eventNameId"
                               value=" <%=eventName%>"
                               disabled="true"
                               style="width:75%"/></td>

                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="event.adaptor.type"/></td>
                    <td><select name="eventTypeFilter"
                                disabled="true">
                        <option><%=eventType%>
                        </option>
                    </select>
                    </td>
                </tr>
                <%

                    if (eventPublisherPropertyDtos != null) {
                        for (EventPublisherPropertyDto EventPublisherPropertyDto : eventPublisherPropertyDtos) {

                %>

                <tr>
                    <td><%=EventPublisherPropertyDto.getDisplayName()%>
                    </td>
                    <%
                        if (!EventPublisherPropertyDto.getSecured()) {
                    %>
                    <td><input type="input" value="<%=EventPublisherPropertyDto.getValue()!=null?EventPublisherPropertyDto.getValue():""%>"
                               disabled="true"
                               style="width:75%"/>
                    </td>
                    <%
                    } else { %>
                    <td><input type="password" value="<%=EventPublisherPropertyDto.getValue()!=null?EventPublisherPropertyDto.getValue():""%>"
                               disabled="true"
                               style="width:75%"/>
                    </td>
                    <%
                        }
                    %>
                </tr>
                <%

                            }
                        }
                    }

                %>

                </tbody>
            </table>


        </div>
    </div>
</fmt:bundle>