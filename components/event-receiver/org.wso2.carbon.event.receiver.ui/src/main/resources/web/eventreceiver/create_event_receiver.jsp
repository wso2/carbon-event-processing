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
<%@ page import="org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.receiver.ui.EventReceiverUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.DetailInputAdapterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.receiver.stub.types.InputAdapterConfigurationDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.receiver.ui.i18n.Resources">
<script type="text/javascript" src="../eventreceiver/js/event_receiver.js"></script>
<script type="text/javascript" src="../eventreceiver/js/create_eventReceiver_helper.js"></script>
<script type="text/javascript" src="../eventreceiver/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript"
        src="../eventreceiver/js/create_eventReceiver_helper.js"></script>

<div id="middle">
<h2><fmt:message key="title.event.receiver.create"/></h2>

<div id="workArea">

<form name="inputForm" action="#" method="post" id="addEventReceiver">
<table style="width:100%" id="eventReceiverAdd" class="styledLeft">
<%
    EventReceiverAdminServiceStub eventReceiverAdminServiceStub = EventReceiverUIUtils.getEventReceiverAdminService(config, session, request);
    String[] inputAdapterTypes = eventReceiverAdminServiceStub.getAllInputAdapterTypes();
    String streamId = request.getParameter("streamId");
    String redirectPage = request.getParameter("redirectPage");

    EventStreamAdminServiceStub eventStreamAdminServiceStub = EventReceiverUIUtils.getEventStreamAdminService(config, session, request);
    String[] streamIds = eventStreamAdminServiceStub.getStreamNames();
    if (streamId == null && streamIds != null && streamIds.length > 0) {
        streamId = streamIds[0];
    }
    if (streamId != null) {
%>
<br/>
<thead>
<tr>
    <th><fmt:message key="title.event.receiver.details"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">
<table id="eventReceiverInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.receiver.name"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventReceiverName" id="eventReceiverId"
               class="initE"

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.receiver.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>
<tr>
    <td><fmt:message key="event.adapter.type"/><span class="required">*</span></td>
    <td>
        <table>
            <td class="custom-noPadding" width="60%"><select name="eventAdapterTypeFilter"
                                                             onchange="loadEventAdapterRelatedProperties('<fmt:message
                                                                     key="to.heading"/>')"
                                                             id="eventAdapterTypeFilter">
                <%
                    String firstEventAdapterType = null;

                    if (inputAdapterTypes != null) {
                        firstEventAdapterType = inputAdapterTypes[0];
                        for (String inputAdapterType : inputAdapterTypes) {
                %>
                <option value="<%=inputAdapterType%>"><%=inputAdapterType%>
                </option>
                <%
                        }
                    }
                %>

            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.adapter.type.help"/>
                </div>
            </td>
            <td width="40%" id="addInputEventAdapterTD" class="custom-noPadding"></td>
        </table>
    </td>
</tr>
<%
    InputAdapterConfigurationDto inputAdapterConfigurationDto = eventReceiverAdminServiceStub.getInputAdapterConfigurationSchema(firstEventAdapterType);
    if (inputAdapterConfigurationDto != null) {
%>
<%
    DetailInputAdapterPropertyDto[] eventAdapterProperties = inputAdapterConfigurationDto.getInputEventAdapterProperties();
    if (eventAdapterProperties != null && eventAdapterProperties.length > 0) {
%>
<tr>
    <td>
        <b><i><span style="color: #666666; "><fmt:message key="static.properties.heading"/></span></i></b>
    </td>
</tr>
<%
    for (int index = 0; index < eventAdapterProperties.length; index++) {
%>
<tr>
    <td class="leftCol-med"><%=eventAdapterProperties[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventAdapterProperties[index].getRequired()) {
                propertyId = "property_Required_";
        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventAdapterProperties[index].getSecured()) {
            type = "password";
        }
    %>
    <td>
        <div class=inputFields>
            <%
                if (eventAdapterProperties[index].getOptions()[0] != null) {
            %>
            <select name="<%=eventAdapterProperties[index].getKey()%>"
                    id="<%=propertyId%><%=index%>">
                <%
                    for (String property : eventAdapterProperties[index].getOptions()) {
                        if (property.equals(eventAdapterProperties[index].getDefaultValue())) {
                %>
                <option selected="selected"><%=property%>
                </option>
                <% } else { %>
                <option><%=property%>
                </option>
                <% }
                } %>
            </select>

            <% } else { %>
            <input type="<%=type%>"
                   name="<%=eventAdapterProperties[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= (eventAdapterProperties[index].getDefaultValue()) != null ? eventAdapterProperties[index].getDefaultValue() : "" %>"
                    />

            <% }

                if (eventAdapterProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=eventAdapterProperties[index].getHint()%>
            </div>
            <% } %>
        </div>
    </td>
</tr>
<%
        }
    }
%>
<tr>
    <td>
        <b><fmt:message key="to.heading"/></b>
    </td>
</tr>
<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamIdFilter"
                onchange="loadMappingUiElements()" id="streamIdFilter">
        <%
            if (streamIds != null) {
                for (String aStreamId : streamIds) {
        %>
        <option><%=aStreamId%>
        </option>
        <%
                }
            }
        %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="event.stream.name.help"/>
        </div>
    </td>

</tr>

<%--<tr>--%>
    <%--<td>--%>
        <%--<fmt:message key="stream.attributes"/>--%>
    <%--</td>--%>
    <%--<td>--%>
        <%--<textArea class="expandedTextarea" id="streamDefinitionText" name="streamDefinitionText"--%>
                  <%--readonly="true"--%>
                  <%--cols="60"><%=streamDefinition%>--%>
        <%--</textArea>--%>

    <%--</td>--%>

<%--</tr>--%>

<tr>
    <td colspan="2">
        <b><fmt:message key="mapping.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="message.format"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                onchange="loadMappingUiElements()" id="mappingTypeFilter">
        <%
            String[] messageFormats = inputAdapterConfigurationDto.getSupportedMessageFormats();

            String firstMappingTypeName = null;
            if (messageFormats != null) {
                firstMappingTypeName = messageFormats[0];
                for (String mappingType : messageFormats) {
        %>
        <option><%=mappingType%>
        </option>
        <%
                }

        %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="message.format.help"/>
        </div>
    </td>

</tr>


<tr>
    <td><a href="#"
           style="background-image:url(images/add.gif);"
           class="icon-link" onclick="handleAdvancedMapping()">
        Advanced
    </a></td>
</tr>
<tr>

    <td id="mappingUiTd" colspan="2">
        <div id="outerDiv" style="display:none">

            <%
                if (firstMappingTypeName != null) {
                    if (firstMappingTypeName.equals("wso2event")) {
            %>
            <jsp:include page="wso2event_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("xml")) {
            %>
            <jsp:include page="xml_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("map")) {
            %>
            <jsp:include page="map_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("text")) {
            %>
            <jsp:include page="text_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
            } else if (firstMappingTypeName.equals("json")) {
            %>
            <jsp:include page="json_mapping_ui.jsp" flush="true">
                <jsp:param name="streamNameWithVersion"
                           value="<%=streamId%>"/>
            </jsp:include>
            <%
                            }
                        }
                    }
                }
            %>
        </div>
    </td>

</tr>
<%--<tr>--%>
<%--<td class="formRaw" colspan="2">--%>
<%--<div id="outerDiv" style="display:none">--%>

<%--<div id="innerDiv1" style="display:none">--%>

    <%--<table class="styledLeft noBorders spacer-bot"--%>
           <%--style="width:100%">--%>
        <%--<tbody>--%>
        <%--<tr name="inputWSO2EventMapping">--%>
            <%--<td colspan="2" class="middle-header">--%>
                <%--<fmt:message key="wso2event.mapping"/>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputWSO2EventMapping">--%>
            <%--<td colspan="2">--%>

                <%--<h6><fmt:message key="property.data.type.meta"/></h6>--%>
                <%--<table class="styledLeft noBorders spacer-bot" id="inputMetaDataTable"--%>
                       <%--style="display:none">--%>
                    <%--<thead>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.name"/></th>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.value.of"/></th>--%>
                    <%--<th><fmt:message key="actions"/></th>--%>
                    <%--</thead>--%>
                <%--</table>--%>
                <%--<div class="noDataDiv-plain" id="noInputMetaData">--%>
                    <%--<fmt:message key="no.meta.defined.message"/>--%>
                <%--</div>--%>
                <%--<table id="addMetaData" class="normal">--%>
                    <%--<tbody>--%>
                    <%--<tr>--%>
                        <%--<td class="col-small"><fmt:message key="property.name"/> :</td>--%>
                        <%--<td>--%>
                            <%--<input type="text" id="inputMetaDataPropName"/>--%>
                        <%--</td>--%>
                        <%--<td class="col-small"><fmt:message key="property.value.of"/> :--%>
                        <%--</td>--%>
                        <%--<td>--%>
                            <%--<select id="inputMetaDataPropValueOf">--%>
                                <%--<% for (String attributeData : attributeList) {--%>
                                    <%--String[] attributeValues = attributeData.split(" ");--%>
                                <%--%>--%>
                                <%--<option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>--%>
                                <%--</option>--%>
                                <%--<% }%>--%>
                            <%--</select>--%>
                        <%--</td>--%>
                        <%--<td><input type="button" class="button"--%>
                                   <%--value="<fmt:message key="add"/>"--%>
                                   <%--onclick="addInputWSO2EventProperty('Meta')"/>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</td>--%>
        <%--</tr>--%>


        <%--<tr name="inputWSO2EventMapping">--%>
            <%--<td colspan="2">--%>

                <%--<h6><fmt:message key="property.data.type.correlation"/></h6>--%>
                <%--<table class="styledLeft noBorders spacer-bot"--%>
                       <%--id="inputCorrelationDataTable" style="display:none">--%>
                    <%--<thead>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.name"/></th>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.value.of"/></th>--%>
                    <%--<th><fmt:message key="actions"/></th>--%>
                    <%--</thead>--%>
                <%--</table>--%>
                <%--<div class="noDataDiv-plain" id="noInputCorrelationData">--%>
                    <%--<fmt:message key="no.correlation.defined.message"/>--%>
                <%--</div>--%>
                <%--<table id="addCorrelationData" class="normal">--%>
                    <%--<tbody>--%>
                    <%--<tr>--%>
                        <%--<td class="col-small"><fmt:message key="property.name"/> :</td>--%>
                        <%--<td>--%>
                            <%--<input type="text" id="inputCorrelationDataPropName"/>--%>
                        <%--</td>--%>
                        <%--<td class="col-small"><fmt:message key="property.value.of"/> :--%>
                        <%--</td>--%>
                        <%--<td>--%>
                            <%--<select id="inputCorrelationDataPropValueOf">--%>
                                <%--<% for (String attributeData : attributeList) {--%>
                                    <%--String[] attributeValues = attributeData.split(" ");--%>
                                <%--%>--%>
                                <%--<option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>--%>
                                <%--</option>--%>
                                <%--<% }%>--%>
                            <%--</select>--%>
                        <%--</td>--%>
                        <%--<td><input type="button" class="button"--%>
                                   <%--value="<fmt:message key="add"/>"--%>
                                   <%--onclick="addInputWSO2EventProperty('Correlation')"/>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputWSO2EventMapping">--%>
            <%--<td colspan="2">--%>

                <%--<h6><fmt:message key="property.data.type.payload"/></h6>--%>
                <%--<table class="styledLeft noBorders spacer-bot"--%>
                       <%--id="inputPayloadDataTable" style="display:none">--%>
                    <%--<thead>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.name"/></th>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.value.of"/></th>--%>
                    <%--<th><fmt:message key="actions"/></th>--%>
                    <%--</thead>--%>
                <%--</table>--%>
                <%--<div class="noDataDiv-plain" id="noInputPayloadData">--%>
                    <%--<fmt:message key="no.payload.defined.message"/>--%>
                <%--</div>--%>
                <%--<table id="addPayloadData" class="normal">--%>
                    <%--<tbody>--%>
                    <%--<tr>--%>
                        <%--<td class="col-small"><fmt:message key="property.name"/> :</td>--%>
                        <%--<td>--%>
                            <%--<input type="text" id="inputPayloadDataPropName"/>--%>
                        <%--</td>--%>
                        <%--<td class="col-small"><fmt:message key="property.value.of"/> :--%>
                        <%--</td>--%>
                        <%--<td>--%>
                            <%--<select id="inputPayloadDataPropValueOf">--%>
                                <%--<% for (String attributeData : attributeList) {--%>
                                    <%--String[] attributeValues = attributeData.split(" ");--%>
                                <%--%>--%>
                                <%--<option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>--%>
                                <%--</option>--%>
                                <%--<% }%>--%>
                            <%--</select>--%>
                        <%--</td>--%>
                        <%--<td><input type="button" class="button"--%>
                                   <%--value="<fmt:message key="add"/>"--%>
                                   <%--onclick="addInputWSO2EventProperty('Payload')"/>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</td>--%>
        <%--</tr>--%>

        <%--</tbody>--%>
    <%--</table>--%>
<%--</div>--%>


<%--<div id="innerDiv2" style="display:none">--%>
    <%--<table class="styledLeft noBorders spacer-bot"--%>
           <%--style="width:100%">--%>
        <%--<tbody>--%>
        <%--<tr name="inputTextMapping">--%>
            <%--<td colspan="3" class="middle-header">--%>
                <%--<fmt:message key="text.mapping"/>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
            <%--<td class="leftCol-med" colspan="1"><fmt:message key="input.mapping.content"/><span--%>
                    <%--class="required">*</span></td>--%>
            <%--<td colspan="2">--%>
                <%--<input id="inline_text" type="radio" checked="checked" value="content"--%>
                       <%--name="inline_text" onclick="enable_disable_Registry(this)">--%>
                <%--<label for="inline_text"><fmt:message key="inline.input"/></label>--%>
                <%--<input id="registry_text" type="radio" value="reg" name="registry_text"--%>
                       <%--onclick="enable_disable_Registry(this)">--%>
                <%--<label for="registry_text"><fmt:message key="registry.input"/></label>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputTextMappingInline" id="inputTextMappingInline">--%>
            <%--<td colspan="3">--%>
                <%--<p>--%>
                    <%--<textarea id="textSourceText" name="textSourceText"--%>
                              <%--style="border:solid 1px rgb(204, 204, 204); width: 99%;--%>
    <%--height: 150px; margin-top: 5px;"--%>
                              <%--name="textSource" rows="30"></textarea>--%>
                <%--</p>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputTextMappingRegistry" style="display:none" id="inputTextMappingRegistry">--%>
            <%--<td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span--%>
                    <%--class="required">*</span></td>--%>
            <%--<td colspan="1"><input type="text" id="textSourceRegistry" disabled="disabled"--%>
                                   <%--class="initE"--%>
                                   <%--value=""--%>
                                   <%--style="width:100%"/></td>--%>

            <%--<td class="nopadding" style="border:none" colspan="1">--%>
                <%--<a href="#registryBrowserLink" class="registry-picker-icon-link"--%>
                   <%--style="padding-left:20px"--%>
                   <%--onclick="showRegistryBrowser('textSourceRegistry','/_system/config');"><fmt:message--%>
                        <%--key="conf.registry"/></a>--%>
                <%--<a href="#registryBrowserLink"--%>
                   <%--class="registry-picker-icon-link"--%>
                   <%--style="padding-left:20px"--%>
                   <%--onclick="showRegistryBrowser('textSourceRegistry', '/_system/governance');"><fmt:message--%>
                        <%--key="gov.registry"/></a>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--</tbody>--%>
    <%--</table>--%>
<%--</div>--%>

<%--<div id="innerDiv3" style="display:none">--%>
    <%--<table class="styledLeft noBorders spacer-bot"--%>
           <%--style="width:100%">--%>
        <%--<tbody>--%>
        <%--<tr name="inputXMLMapping">--%>
            <%--<td colspan="3" class="middle-header">--%>
                <%--<fmt:message key="xml.mapping"/>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
            <%--<td class="leftCol-med" colspan="1"><fmt:message key="input.mapping.content"/><span--%>
                    <%--class="required">*</span></td>--%>
            <%--<td colspan="2">--%>
                <%--<input id="inline_xml" type="radio" checked="checked" value="content"--%>
                       <%--name="inline_xml" onclick="enable_disable_Registry(this)">--%>
                <%--<label for="inline_xml"><fmt:message key="inline.input"/></label>--%>
                <%--<input id="registry_xml" type="radio" value="reg" name="registry_xml"--%>
                       <%--onclick="enable_disable_Registry(this)">--%>
                <%--<label for="registry_xml"><fmt:message key="registry.input"/></label>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputXMLMappingInline" id="inputXMLMappingInline">--%>
            <%--<td colspan="3">--%>
                <%--<p>--%>
                    <%--<textarea id="xmlSourceText"--%>
                              <%--style="border:solid 1px rgb(204, 204, 204); width: 99%;--%>
                                     <%--height: 150px; margin-top: 5px;"--%>
                              <%--name="xmlSource" rows="30"></textarea>--%>
                <%--</p>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputXMLMappingRegistry" style="display:none" id="inputXMLMappingRegistry">--%>
            <%--<td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span--%>
                    <%--class="required">*</span></td>--%>
            <%--<td colspan="1">--%>
                <%--<input type="text" id="xmlSourceRegistry" disabled="disabled" class="initE" value=""--%>
                       <%--style="width:100%"/>--%>
            <%--</td>--%>
            <%--<td class="nopadding" style="border:none" colspan="1">--%>
                <%--<a href="#registryBrowserLink" class="registry-picker-icon-link"--%>
                   <%--style="padding-left:20px"--%>
                   <%--onclick="showRegistryBrowser('xmlSourceRegistry','/_system/config');"><fmt:message--%>
                        <%--key="conf.registry"/></a>--%>
                <%--<a href="#registryBrowserLink"--%>
                   <%--class="registry-picker-icon-link"--%>
                   <%--style="padding-left:20px"--%>
                   <%--onclick="showRegistryBrowser('xmlSourceRegistry', '/_system/governance');"><fmt:message--%>
                        <%--key="gov.registry"/></a>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--</tbody>--%>
    <%--</table>--%>
<%--</div>--%>


<%--<div id="innerDiv4" style="display:none">--%>
    <%--<table class="styledLeft noBorders spacer-bot"--%>
           <%--style="width:100%">--%>
        <%--<tbody>--%>
        <%--<tr name="inputMapMapping">--%>
            <%--<td colspan="2" class="middle-header">--%>
                <%--<fmt:message key="map.mapping"/>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputMapMapping">--%>
            <%--<td colspan="2">--%>

                <%--<table class="styledLeft noBorders spacer-bot" id="inputMapPropertiesTable"--%>
                       <%--style="display:none">--%>
                    <%--<thead>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.name"/></th>--%>
                    <%--<th class="leftCol-med"><fmt:message key="property.value.of"/></th>--%>
                    <%--<th><fmt:message key="actions"/></th>--%>
                    <%--</thead>--%>
                <%--</table>--%>
                <%--<div class="noDataDiv-plain" id="noInputMapProperties">--%>
                    <%--<fmt:message key="no.map.properties.defined"/>--%>
                <%--</div>--%>
                <%--<table id="addInputMapProperties" class="normal">--%>
                    <%--<tbody>--%>
                    <%--<tr>--%>
                        <%--<td class="col-small"><fmt:message key="property.name"/> :</td>--%>
                        <%--<td>--%>
                            <%--<input type="text" id="inputMapPropName"/>--%>
                        <%--</td>--%>
                        <%--<td class="col-small"><fmt:message key="property.value.of"/> :</td>--%>
                        <%--<td>--%>
                            <%--<select id="inputMapPropValueOf">--%>
                                <%--<% for (String attributeData : attributeList) {--%>
                                    <%--String[] attributeValues = attributeData.split(" ");--%>
                                <%--%>--%>
                                <%--<option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>--%>
                                <%--</option>--%>
                                <%--<% }%>--%>
                            <%--</select>--%>
                        <%--</td>--%>
                        <%--<td><input type="button" class="button" value="<fmt:message key="add"/>"--%>
                                   <%--onclick="addInputMapProperty()"/>--%>
                        <%--</td>--%>
                    <%--</tr>--%>
                    <%--</tbody>--%>
                <%--</table>--%>
            <%--</td>--%>
        <%--</tr>--%>

        <%--</tbody>--%>
    <%--</table>--%>
<%--</div>--%>

<%--<div id="innerDiv5" style="display:none">--%>
    <%--<table class="styledLeft noBorders spacer-bot"--%>
           <%--style="width:100%">--%>
        <%--<tbody>--%>
        <%--<tr name="inputJSONMapping">--%>
            <%--<td colspan="3" class="middle-header">--%>
                <%--<fmt:message key="json.mapping"/>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr>--%>
            <%--<td class="leftCol-med" colspan="1"><fmt:message key="input.mapping.content"/><span--%>
                    <%--class="required">*</span></td>--%>
            <%--<td colspan="2">--%>
                <%--<input id="inline_json" type="radio" checked="checked" value="content"--%>
                       <%--name="inline_json" onclick="enable_disable_Registry(this)">--%>
                <%--<label for="inline_json"><fmt:message key="inline.input"/></label>--%>
                <%--<input id="registry_json" type="radio" value="reg" name="registry_json"--%>
                       <%--onclick="enable_disable_Registry(this)">--%>
                <%--<label for="registry_json"><fmt:message key="registry.input"/></label>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputJSONMappingInline" id="inputJSONMappingInline">--%>
            <%--<td colspan="3">--%>
                <%--<p>--%>
                    <%--<textarea id="jsonSourceText"--%>
                              <%--style="border:solid 1px rgb(204, 204, 204); width: 99%;--%>
                                     <%--height: 150px; margin-top: 5px;"--%>
                              <%--name="jsonSource" rows="30"></textarea>--%>
                <%--</p>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--<tr name="inputJSONMappingRegistry" style="display:none" id="inputJSONMappingRegistry">--%>
            <%--<td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span--%>
                    <%--class="required">*</span></td>--%>
            <%--<td colspan="1">--%>
                <%--<input type="text" id="jsonSourceRegistry" disabled="disabled" class="initE"--%>
                       <%--value=""--%>
                       <%--style="width:100%"/>--%>
            <%--</td>--%>
            <%--<td class="nopadding" style="border:none" colspan="1">--%>
                <%--<a href="#registryBrowserLink" class="registry-picker-icon-link"--%>
                   <%--style="padding-left:20px"--%>
                   <%--onclick="showRegistryBrowser('jsonSourceRegistry','/_system/config');"><fmt:message--%>
                        <%--key="conf.registry"/></a>--%>
                <%--<a href="#registryBrowserLink"--%>
                   <%--class="registry-picker-icon-link"--%>
                   <%--style="padding-left:20px"--%>
                   <%--onclick="showRegistryBrowser('jsonSourceRegistry', '/_system/governance');"><fmt:message--%>
                        <%--key="gov.registry"/></a>--%>
            <%--</td>--%>
        <%--</tr>--%>
        <%--</tbody>--%>
    <%--</table>--%>
<%--</div>--%>


<%--</div>--%>
<%--</td>--%>
<%--</tr>--%>

</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="add.event.receiver"/>"
               onclick="addEventReceiverViaPopup(document.getElementById('addEventReceiver') ,'<%=streamId%>')"/>
    </td>
</tr>
</tbody>
<% } else { %>
<tbody>
<tr>
    <td class="formRaw">
        <table id="noEventBuilderInputTable" class="normal-nopadding"
               style="width:100%">
            <tbody>

            <tr>
                <td class="leftCol-med" colspan="2">Event Streams or Input
                                                    Event Adapters are not available, Please create
                                                    an Input Event Adapter to continue...
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
<% } %>
</table>
</form>
</div>
</div>
</fmt:bundle>