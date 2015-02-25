<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationInfoDto" %>
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

<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">

    <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


    <script type="text/javascript"
            src="../eventpublisher/js/create_event_publisher_helper.js"></script>

    <table id="eventInputTable" class="normal-nopadding"
           style="width:100%">
        <tbody>

        <tr>
            <td class="leftCol-med"><fmt:message
                    key="event.publisher.name"/><span
                    class="required">*</span>
            </td>
            <td><input type="text" name="eventName" id="eventNameId"
                       class="initE"
                       onclick="clearTextIn(this)" onblur="fillTextIn(this)"
                       value=""
                       style="width:75%"/>

                <div class="sectionHelp">
                    <fmt:message key="event.publisher.name.help"/>
                </div>

            </td>
        </tr>
        <tr>
            <td><fmt:message key="event.publisher.endpoint.type"/><span
                    class="required">*</span></td>
            <td><select name="eventTypeFilter"
                        onchange="showEventProperties('<fmt:message key="output.event.all.properties"/>')"
                        id="eventTypeFilter">
                <%
                    EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
                    EventPublisherConfigurationInfoDto[] eventPublisherConfigInfos = stub.getAllActiveEventPublisherConfiguration();
                    EventPublisherPropertyDto[] eventPublisherPropertiesDto = null;
                    EventPublisherConfigurationInfoDto firstEventPublisher = null;
                   // String supportedEventPublisherType = null;
                    if (eventPublisherConfigInfos != null) {
                        firstEventPublisher = eventPublisherConfigInfos[0];
                        eventPublisherPropertiesDto = stub.getEventPublisherAdaptorProperties(firstEventPublisher.getEventPublisherName());
                        for (EventPublisherConfigurationInfoDto eventPublisherConfigInfo : eventPublisherConfigInfos) {
                %>
                <option><%=eventPublisherConfigInfo.getEventPublisherName()%>
                </option>
                <%
                        }
                    }
                %>
            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.publisher.endpoint.type.help"/>
                </div>
            </td>

        </tr>

        <%
            if (eventPublisherPropertiesDto != null) {

        %>
        <tr>
            <td colspan="2"><b>
                <fmt:message key="output.event.all.properties"/> </b>
            </td>
        </tr>

        <%
            }

            if (firstEventPublisher != null) {

        %>


        <%
            //input fields for output event publisher properties
            if (eventPublisherPropertiesDto != null & firstEventPublisher != null) {
                for (int index = 0; index < eventPublisherPropertiesDto.length; index++) {
        %>
        <tr>

            <td class="leftCol-med"><%=eventPublisherPropertiesDto[index].getDisplayName()%>
                <%
                    String propertyId = "outputProperty_";
                    if (eventPublisherPropertiesDto[index].getRequired()) {
                        propertyId = "outputProperty_Required_";

                %>
                <span class="required">*</span>
                <%
                    }
                %>
            </td>
            <%
                String type = "text";
                if (eventPublisherPropertiesDto[index].getSecured()) {
                    type = "password";
                }
            %>

            <td>
                <div class=outputFields>
                    <%

                        if (eventPublisherPropertiesDto[index].getOptions()[0] != null) {

                    %>

                    <select name="<%=eventPublisherPropertiesDto[index].getKey()%>"
                            id="<%=propertyId%><%=index%>">

                        <%
                            for (String property : eventPublisherPropertiesDto[index].getOptions()) {
                                if (property.equals(eventPublisherPropertiesDto[index].getDefaultValue())) {
                        %>
                        <option selected="selected"><%=property%>
                        </option>
                        <% } else { %>
                        <option><%=property%>
                        </option>
                        <% }
                        }%>
                    </select>

                    <% } else { %>
                    <input type="<%=type%>"
                           name="<%=eventPublisherPropertiesDto[index].getKey()%>"
                           id="<%=propertyId%><%=index%>" class="initE"
                           style="width:75%"
                           value="<%= (eventPublisherPropertiesDto[index].getDefaultValue()) != null ? eventPublisherPropertiesDto[index].getDefaultValue() : "" %>"/>

                    <% }

                        if (eventPublisherPropertiesDto[index].getHint() != null) { %>
                    <div class="sectionHelp">
                        <%=eventPublisherPropertiesDto[index].getHint()%>
                    </div>
                    <% } %>
                </div>
            </td>

        </tr>
        <%
                    }
                }
            }
        %>

        </tbody>
    </table>
</fmt:bundle>
