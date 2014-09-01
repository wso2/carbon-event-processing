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
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventstream.detail"
            resourceBundle="org.wso2.carbon.event.stream.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>


    <link type="text/css" href="css/eventStream.css" rel="stylesheet"/>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
    <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
    <script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
    <script type="text/javascript"
            src="../eventstream/js/create_eventStream_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript" src="../eventstream/js/vkbeautify.0.99.00.beta.js"></script>

    <%
        String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
    %>

    <script type="text/javascript">
        jQuery(document).ready(function () {
            formatSampleEvent();
        });

        function formatSampleEvent() {
            var selectedIndex = document.getElementById("sampleEventTypeFilter").selectedIndex;
            var eventType = document.getElementById("sampleEventTypeFilter").options[selectedIndex].text;

            var sampleEvent = document.getElementById("sampleEventText").value.trim();

            if (eventType == "xml") {
                jQuery('#sampleEventText').val(vkbeautify.xml(sampleEvent.trim()));
            }
            else if (eventType == "json") {
                jQuery('#sampleEventText').val(vkbeautify.json(sampleEvent.trim()));
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="event.stream.details"/><%=eventStreamWithVersion%>
        </h2>

        <div id="workArea">
            <form name="eventStreamInfo" action="index.jsp?ordinal=1" method="post"
                  id="showEventStream">
                <table id="eventStreamInfoTable" class="styledLeft"
                       style="width:100%">

                    <thead>
                    <tr>
                        <th><fmt:message key="event.stream.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <table id="eventStreamInputTable" class="normal-nopadding"
                                   style="width:100%">

                                <tbody>
                                <%

                                    if (eventStreamWithVersion != null) {

                                        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);


                                        String[] eventAdaptorPropertiesDto = stub.getStreamDetailsForStreamId(eventStreamWithVersion);
                                %>
                                <tr>
                                    <td class="leftCol-small">Event Stream Definition</td>
                                    <td><textArea class="expandedTextarea" id="streamDefinitionText"
                                                  name="streamDefinitionText"
                                                  readonly="true"
                                                  cols="120"
                                                  style="height:350px;"><%=eventAdaptorPropertiesDto[0]%>
                                    </textArea></td>


                                </tr>
                                <tr>
                                    <td>Create Sample Event</td>
                                    <td><select name="sampleEventTypeFilter"
                                                id="sampleEventTypeFilter">
                                        <option>xml</option>
                                        <option>json</option>
                                        <option>text</option>
                                    </select>
                                        <input type="button"
                                               value="<fmt:message key="generate.event"/>"
                                               onclick="generateEvent('<%=eventStreamWithVersion%>')"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td><textArea class="expandedTextarea" id="sampleEventText"
                                                  name="sampleEventText"
                                                  readonly="true"
                                                  cols="120"><%=eventAdaptorPropertiesDto[1]%>
                                    </textArea>
                                    </td>
                                </tr>
                                <%
                                    }
                                %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>

                </table>

            </form>
        </div>
    </div>
</fmt:bundle>