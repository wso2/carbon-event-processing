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

<%@ page import="org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.publisher.ui.EventPublisherUIUtils" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.publisher.stub.types.EventPublisherConfigurationFileDto" %>

<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">

    <carbon:breadcrumb
            label="list"
            resourceBundle="org.wso2.carbon.event.publisher.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript">

        var ENABLE = "enable";
        var DISABLE = "disable";
        var STAT = "statistics";
        var TRACE = "Tracing";

        function doDelete(eventName) {
            var theform = document.getElementById('deleteForm');
            theform.eventname.value = eventName;
            theform.submit();
        }

        function disableStat(eventPublisherName) {
            jQuery.ajax({
                type: 'POST',
                url: 'stat_tracing-ajaxprocessor.jsp',
                data: 'eventPublisherName=' + eventPublisherName + '&action=disableStat',
                async: false,
                success: function (msg) {
                    handleCallback(eventPublisherName, DISABLE, STAT);
                },
                error: function (msg) {
                    CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                    ' ' + eventPublisherName);
                }
            });
        }

        function enableStat(eventPublisherName) {
            jQuery.ajax({
                type: 'POST',
                url: 'stat_tracing-ajaxprocessor.jsp',
                data: 'eventPublisherName=' + eventPublisherName + '&action=enableStat',
                async: false,
                success: function (msg) {
                    handleCallback(eventPublisherName, ENABLE, STAT);
                },
                error: function (msg) {
                    CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                    ' ' + eventPublisherName);
                }
            });
        }

        function handleCallback(eventPublisher, action, type) {
            var element;
            if (action == "enable") {
                if (type == "statistics") {
                    element = document.getElementById("disableStat" + eventPublisher);
                    element.style.display = "";
                    element = document.getElementById("enableStat" + eventPublisher);
                    element.style.display = "none";
                } else {
                    element = document.getElementById("disableTracing" + eventPublisher);
                    element.style.display = "";
                    element = document.getElementById("enableTracing" + eventPublisher);
                    element.style.display = "none";
                }
            } else {
                if (type == "statistics") {
                    element = document.getElementById("disableStat" + eventPublisher);
                    element.style.display = "none";
                    element = document.getElementById("enableStat" + eventPublisher);
                    element.style.display = "";
                } else {
                    element = document.getElementById("disableTracing" + eventPublisher);
                    element.style.display = "none";
                    element = document.getElementById("enableTracing" + eventPublisher);
                    element.style.display = "";
                }
            }
        }

        function enableTracing(eventPublisherName) {
            jQuery.ajax({
                type: 'POST',
                url: 'stat_tracing-ajaxprocessor.jsp',
                data: 'eventPublisherName=' + eventPublisherName + '&action=enableTracing',
                async: false,
                success: function (msg) {
                    handleCallback(eventPublisherName, ENABLE, TRACE);
                },
                error: function (msg) {
                    CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                    ' ' + eventPublisherName);
                }
            });
        }

        function disableTracing(eventPublisherName) {
            jQuery.ajax({
                type: 'POST',
                url: 'stat_tracing-ajaxprocessor.jsp',
                data: 'eventPublisherName=' + eventPublisherName + '&action=disableTracing',
                async: false,
                success: function (msg) {
                    handleCallback(eventPublisherName, DISABLE, TRACE);
                },
                error: function (msg) {
                    CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                    ' ' + eventPublisherName);
                }
            });
        }

    </script>
    <%
        String eventName = request.getParameter("eventname");
        int totalEventPublishers = 0;
        int totalNotDeployedEventPublishers = 0;
        if (eventName != null) {
            EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
            stub.undeployActiveEventPublisherConfiguration(eventName);
    %>
    <script type="text/javascript">CARBON.showInfoDialog('Event publisher successfully deleted.');</script>
    <%
        }

        EventPublisherAdminServiceStub stub = EventPublisherUIUtils.getEventPublisherAdminService(config, session, request);
        EventPublisherConfigurationInfoDto[] eventDetailsArray = stub.getAllActiveEventPublisherConfiguration();
        if (eventDetailsArray != null) {
            totalEventPublishers = eventDetailsArray.length;
        }

        EventPublisherConfigurationFileDto[] notDeployedEventPublisherConfigurationFiles = stub.getAllInactiveEventPublisherConfiguration();
        if (notDeployedEventPublisherConfigurationFiles != null) {
            totalNotDeployedEventPublishers = notDeployedEventPublisherConfigurationFiles.length;
        }

    %>

    <div id="middle">
    <h2><fmt:message key="available.event.publishers"/></h2>
    <a href="create_eventPublisher.jsp?ordinal=1"
       style="background-image:url(images/add.gif);"
       class="icon-link">
        Add Event Publisher
    </a>
    <br/> <br/>

    <div id="workArea">

        <%=totalEventPublishers%> <fmt:message
            key="active.event.publishers"/> <% if (totalNotDeployedEventPublishers > 0) { %><a
            href="event_publisher_files_details.jsp?ordinal=1"><%=totalNotDeployedEventPublishers%>
        <fmt:message
                key="inactive.event.publishers"/></a><% } else {%><%=totalNotDeployedEventPublishers%>
        <fmt:message key="inactive.event.publishers"/> <% } %>
        <br/> <br/>

        <table class="styledLeft">
            <%

                if (eventDetailsArray != null) {
            %>

            <thead>
            <tr>
                <th><fmt:message key="event.publisher.name"/></th>
                <th><fmt:message key="event.publisher.endpoint.type"/></th>
                <th width="420px"><fmt:message key="actions"/></th>
            </tr>
            </thead>
            <tbody>
                    <%
                for (EventPublisherConfigurationInfoDto eventDetails : eventDetailsArray) {
            %>
            <tr>
                <td>
                    <a href="event_details.jsp?ordinal=1&eventName=<%=eventDetails.getEventPublisherName()%>&eventType=<%=eventDetails.getOutputEndpointType()%>"><%=eventDetails.getEventPublisherName()%>
                    </a>

                </td>
                <td><%=eventDetails.getEventPublisherName()%>
                </td>
                <td>
                    <%--<% if (eventDetails.getEditable()) { %> todo fix --%>
                    <% if (eventDetails.getEnableStats()) { %>
                    <div class="inlineDiv">
                        <div id="disableStat<%= eventDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="disableStat('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                        <div id="enableStat<%= eventDetails.getEventPublisherName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enableStat('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableStat<%= eventDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="enableStat('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                                    key="stat.enable.link"/></a>
                        </div>
                        <div id="disableStat<%= eventDetails.getEventPublisherName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disableStat('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                                    key="stat.disable.link"/></a>
                        </div>
                    </div>
                    <% }
                        if (eventDetails.getEnableTracing()) {%>
                    <div class="inlineDiv">
                        <div id="disableTracing<%= eventDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="disableTracing('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                        <div id="enableTracing<%= eventDetails.getEventPublisherName()%>"
                             style="display:none;">
                            <a href="#"
                               onclick="enableTracing('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                    </div>
                    <% } else { %>
                    <div class="inlineDiv">
                        <div id="enableTracing<%= eventDetails.getEventPublisherName()%>">
                            <a href="#"
                               onclick="enableTracing('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                                    key="trace.enable.link"/></a>
                        </div>
                        <div id="disableTracing<%= eventDetails.getEventPublisherName()%>"
                             style="display:none">
                            <a href="#"
                               onclick="disableTracing('<%= eventDetails.getEventPublisherName() %>')"
                               class="icon-link"
                               style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                                    key="trace.disable.link"/></a>
                        </div>
                    </div>

                    <% } %>
                    <a style="background-image: url(../admin/images/delete.gif);"
                       class="icon-link"
                       onclick="doDelete('<%=eventDetails.getEventPublisherName()%>')"><font
                            color="#4682b4">Delete</font></a>
                    <a style="background-image: url(../admin/images/edit.gif);"
                       class="icon-link"
                       href="edit_event_details.jsp?ordinal=1&eventName=<%=eventDetails.getEventPublisherName()%>"><font
                            color="#4682b4">Edit</font></a>
                    <% } %>

                </td>
            </tr>
                    <%
                    } /*} todo fix */ else {%>

            <tbody>
            <tr>
                <td class="formRaw">
                    <table id="noEventPublisherTable" class="normal-nopadding"
                           style="width:100%">
                        <tbody>

                        <tr>
                            <td class="leftCol-med" colspan="2"><fmt:message
                                    key="empty.event.publisher.msg"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
            <% }
            %>
            </tbody>
        </table>

        <div>
            <br/>

            <form id="deleteForm" name="input" action="" method="post"><input type="HIDDEN"
                                                                              name="eventname"
                                                                              value=""/></form>
        </div>
    </div>


    <script type="text/javascript">
        alternateTableRows('expiredsubscriptions', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('validsubscriptions', 'tableEvenRow', 'tableOddRow');
    </script>

</fmt:bundle>
