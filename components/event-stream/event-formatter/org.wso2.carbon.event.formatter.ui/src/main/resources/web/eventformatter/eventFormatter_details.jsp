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
<%@ page
        import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>

<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterConfigurationDto" %>
<%@ page
        import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventOutputPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.ToPropertyConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.notifier.ui.EventNotifierUIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">

<carbon:breadcrumb
        label="event.formatter.details.breabcrumb"
        resourceBundle="org.wso2.carbon.event.formatter.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<link endpointType="text/css" href="css/eventNotifier.css" rel="stylesheet"/>
<script endpointType="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script endpointType="text/javascript" src="../admin/js/cookies.js"></script>
<script endpointType="text/javascript" src="../admin/js/main.js"></script>
<script endpointType="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script endpointType="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script endpointType="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
<script endpointType="text/javascript" src="../eventformatter/js/registry-browser.js"></script>

<script endpointType="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script endpointType="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" endpointType="text/css" href="../resources/css/registry.css"/>


<%
    EventFormatterAdminServiceStub stub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
%>
<script language="javascript">

    function clearTextIn(obj) {
        if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
            YAHOO.util.Dom.removeClass(obj, 'initE');
            YAHOO.util.Dom.addClass(obj, 'normalE');
            textValue = obj.value;
            obj.value = "";
        }
    }
    function fillTextIn(obj) {
        if (obj.value == "") {
            obj.value = textValue;
            if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
                YAHOO.util.Dom.removeClass(obj, 'normalE');
                YAHOO.util.Dom.addClass(obj, 'initE');
            }
        }
    }
</script>


<div id="middle">
<h2><fmt:message key="event.formatter.details"/></h2>

<div id="workArea">

<form name="inputForm" action="index.jsp?ordinal=1" method="post">
<table style="width:100%" id="eventFormatterAdd" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="title.event.formatter.details"/></th>
</tr>
</thead>
<% String eventNotifierName = request.getParameter("eventNotifierName");
    if (eventNotifierName != null) {
        EventFormatterConfigurationDto eventNotifierConfigurationDto = stub.getActiveEventFormatterConfiguration(eventNotifierName);
%>
<tbody>
<tr>
<td class="formRaw">
<table id="eventFormatterInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.formatter.name"/><span class="required">*</span>
    </td>
    <td><input endpointType="text" name="eventNotifierName" id="eventFormatterId"
               class="initE"

               value="<%= eventNotifierConfigurationDto.getEventFormatterName()%>"
               style="width:75%" disabled="disabled"/>
    </td>
</tr>


<tr>
    <td><fmt:message key="event.stream.name"/><span class="required">*</span></td>
    <td><select name="streamNameFilter" id="streamNameFilter" disabled="disabled">
        <%

        %>
        <option><%=eventNotifierConfigurationDto.getFromStreamNameWithVersion()%>
        </option>


    </select>

    </td>

</tr>
<tr>
    <td>
        <fmt:message key="stream.attributes"/>
    </td>
    <td>
        <textArea class="expandedTextarea" id="streamDefinitionText" name="streamDefinitionText"
                  readonly="true"
                  cols="60"
                  disabled="disabled"><%=eventNotifierConfigurationDto.getStreamDefinition()%>
        </textArea>
    </td>

</tr>

<tr>
    <td><fmt:message key="event.adaptor.name"/><span class="required">*</span></td>
    <td><select name="eventAdaptorNameFilter"
                id="eventAdaptorNameFilter" disabled="disabled">
        <option><%=eventNotifierConfigurationDto.getToPropertyConfigurationDto().getEventAdaptorName()%>
        </option>
    </select>
    </td>
</tr>

<tr>
    <td><fmt:message key="mapping.endpointType"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                id="mappingTypeFilter" disabled="disabled">
        <option><%=eventNotifierConfigurationDto.getMappingType()%>
        </option>
    </select>
    </td>

</tr>

<tr>
<td class="formRaw" colspan="2">
<div id="outerDiv">
<%
    if (eventNotifierConfigurationDto.getMappingType().equalsIgnoreCase("wso2event")) {
%>
<div id="innerDiv1">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputWSO2EventMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="wso2event.mapping"/>
            </td>
        </tr>
        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.endpointType.meta"/></h6>
                <% if (eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventOutputPropertyConfigurationDto() != null && eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventOutputPropertyConfigurationDto()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.endpointType"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getMetaWSO2EventOutputPropertyConfigurationDto();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getType()%>
                        </td>
                    </tr>
                    <% } %>

                </table>
                <% } else { %>
                <div class="noDataDiv-plain" id="noOutputMetaData">
                    <fmt:message key="no.meta.defined.message"/>
                </div>
                <% } %>
            </td>
        </tr>


        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.endpointType.correlation"/></h6>
                <% if (eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventOutputPropertyConfigurationDto() != null && eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventOutputPropertyConfigurationDto()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputCorrelationDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.endpointType"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getCorrelationWSO2EventOutputPropertyConfigurationDto();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getType()%>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <% } else {%>
                <div class="noDataDiv-plain" id="noOutputCorrelationData">
                    <fmt:message key="no.correlation.defined.message"/>
                </div>
                <% } %>
            </td>
        </tr>

        <tr name="outputWSO2EventMapping">
            <td colspan="2">
                <h6><fmt:message key="property.data.endpointType.payload"/></h6>
                <% if (eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventOutputPropertyConfigurationDto() != null && eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventOutputPropertyConfigurationDto()[0] != null) { %>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputPayloadDataTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th class="leftCol-med"><fmt:message key="property.endpointType"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventNotifierConfigurationDto.getWso2EventOutputMappingDto().getPayloadWSO2EventOutputPropertyConfigurationDto();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getType()%>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <% } else { %>
                <div class="noDataDiv-plain" id="noOutputPayloadData">
                    <fmt:message key="no.payload.defined.message"/>
                </div>
                <% } %>
            </td>
        </tr>

        </tbody>
    </table>
</div>

<%
} else if (eventNotifierConfigurationDto.getMappingType().equalsIgnoreCase("text")) {
%>

<div id="innerDiv2">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputTextMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="text.mapping"/>
            </td>
        </tr>
        <% if (!(eventNotifierConfigurationDto.getTextOutputMappingDto().getRegistryResource())) {%>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_text" endpointType="radio" checked="checked" value="content"
                       name="inline_text" disabled="disabled">
                <label for="inline_text"><fmt:message key="inline.input"/></label>
                <input id="registry_text" endpointType="radio" value="reg" name="registry_text"
                       disabled="disabled">
                <label for="registry_text"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputTextMappingInline" id="outputTextMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="textSourceText" name="textSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
    height: 150px; margin-top: 5px;"
                              name="textSource"
                              rows="30"
                              disabled="disabled"><%= eventNotifierConfigurationDto.getTextOutputMappingDto().getMappingText() %>
                    </textarea>
                </p>
            </td>
        </tr>
        <% } else { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_text_reg" endpointType="radio" value="content"
                       name="inline_text" disabled="disabled">
                <label for="inline_text_reg"><fmt:message key="inline.input"/></label>
                <input id="registry_text_reg" endpointType="radio" value="reg" name="registry_text"
                       disabled="disabled" checked="checked">
                <label for="registry_text_reg"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputTextMappingRegistry" id="outputTextMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input endpointType="text" id="textSourceRegistry" disabled="disabled" class="initE"
                       value="<%=eventNotifierConfigurationDto.getTextOutputMappingDto().getMappingText() !=null ? eventNotifierConfigurationDto.getTextOutputMappingDto().getMappingText() : ""%>"
                       style="width:100%"/>
            </td>

            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%
} else if (eventNotifierConfigurationDto.getMappingType().equalsIgnoreCase("xml")) {
%>

<div id="innerDiv3">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputXMLMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="xml.mapping"/>
            </td>
        </tr>
        <% if (!eventNotifierConfigurationDto.getXmlOutputMappingDto().getRegistryResource()) { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml" endpointType="radio" checked="checked" value="content"
                       name="inline_xml" disabled="disabled">
                <label for="inline_xml"><fmt:message key="inline.input"/></label>
                <input id="registry_xml" endpointType="radio" value="reg" name="registry_xml"
                       disabled="disabled">
                <label for="registry_xml"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingInline" id="outputXMLMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="xmlSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="xmlSource"
                              rows="30"
                              disabled="disabled"><%=eventNotifierConfigurationDto.getXmlOutputMappingDto().getMappingXMLText() != null ? eventNotifierConfigurationDto.getXmlOutputMappingDto().getMappingXMLText() : ""%>
                    </textarea>
                </p>
            </td>
        </tr>
        <% } else { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml_reg" endpointType="radio" value="content"
                       name="inline_xml" disabled="disabled">
                <label for="inline_xml_reg"><fmt:message key="inline.input"/></label>
                <input id="registry_xml_reg" endpointType="radio" value="reg" name="registry_xml"
                       disabled="disabled" checked="checked">
                <label for="registry_xml_reg"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingRegistry" id="outputXMLMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input endpointType="text" id="xmlSourceRegistry" disabled="disabled" class="initE" value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%
} else if (eventNotifierConfigurationDto.getMappingType().equalsIgnoreCase("map")) {
%>

<div id="innerDiv4">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputMapMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="map.mapping"/>
            </td>
        </tr>
        <% if (eventNotifierConfigurationDto.getMapOutputMappingDto().getOutputPropertyConfiguration() != null && eventNotifierConfigurationDto.getMapOutputMappingDto().getOutputPropertyConfiguration()[0] != null) { %>
        <tr name="outputMapMapping">
            <td colspan="2">
                <table class="styledLeft noBorders spacer-bot" id="outputMapPropertiesTable">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    </thead>
                    <% EventOutputPropertyDto[] eventOutputPropertyDtos = eventNotifierConfigurationDto.getMapOutputMappingDto().getOutputPropertyConfiguration();
                        for (EventOutputPropertyDto eventOutputPropertyDto : eventOutputPropertyDtos) { %>
                    <tr>
                        <td class="property-names"><%=eventOutputPropertyDto.getName()%>
                        </td>
                        <td class="property-names"><%=eventOutputPropertyDto.getValueOf()%>
                        </td>
                    </tr>
                    <% } %>
                </table>
                <% } else { %>
                <div class="noDataDiv-plain" id="noOutputMapProperties">
                    <fmt:message key="no.map.properties.defined"/>
                </div>
                <% } %>
            </td>
        </tr>

        </tbody>
    </table>
</div>
<%
} else if (eventNotifierConfigurationDto.getMappingType().equalsIgnoreCase("json")) {
%>

<div id="innerDiv5">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputJSONMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="json.mapping"/>
            </td>
        </tr>
        <% if (!eventNotifierConfigurationDto.getJsonOutputMappingDto().getRegistryResource()) { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json" endpointType="radio" checked="checked" value="content"
                       name="inline_json" disabled="disabled">
                <label for="inline_json"><fmt:message key="inline.input"/></label>
                <input id="registry_json" endpointType="radio" value="reg" name="registry_json"
                       disabled="disabled">
                <label for="registry_json"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingInline" id="outputJSONMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="jsonSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="jsonSource"
                              rows="30"
                              disabled="disabled"><%=eventNotifierConfigurationDto.getJsonOutputMappingDto().getMappingText()!=null ? eventNotifierConfigurationDto.getJsonOutputMappingDto().getMappingText() : ""%>
                    </textarea>
                </p>
            </td>
        </tr>
        <% } else { %>
        <tr>
            <td colspan="1" class="leftCol-med"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json_text" endpointType="radio" value="content"
                       name="inline_json_text" disabled="disabled">
                <label for="inline_json_text"><fmt:message key="inline.input"/></label>
                <input id="registry_json_reg" endpointType="radio" value="reg" name="registry_json"
                       disabled="disabled" checked="checked">
                <label for="registry_json_reg"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingRegistry" id="outputJSONMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input endpointType="text" id="jsonSourceRegistry" disabled="disabled" class="initE"
                       value="<%=eventNotifierConfigurationDto.getJsonOutputMappingDto().getMappingText() != null ? eventNotifierConfigurationDto.getJsonOutputMappingDto().getMappingText() : ""%>"
                       style="width:100%"/>
            </td>
            <td colspan="1" class="nopadding" style="border:none">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>

<%
    }
%>

</div>
</td>
</tr>

<%
    ToPropertyConfigurationDto endpointPropertyConfigurationDto = eventNotifierConfigurationDto.getToPropertyConfigurationDto();
    if (endpointPropertyConfigurationDto != null && endpointPropertyConfigurationDto.getOutputEventAdaptorMessageConfiguration()[0] != null) {

        for (int index = 0; index < endpointPropertyConfigurationDto.getOutputEventAdaptorMessageConfiguration().length; index++) {
            EventFormatterPropertyDto[] eventFormatterPropertyDto = endpointPropertyConfigurationDto.getOutputEventAdaptorMessageConfiguration();
%>
<tr>


    <td class="leftCol-med"><%=eventFormatterPropertyDto[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventFormatterPropertyDto[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String endpointType = "text";
        if (eventFormatterPropertyDto[index].getSecured()) {
            endpointType = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventFormatterPropertyDto[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventFormatterPropertyDto[index].getKey()%>"
                    id="<%=propertyId%><%=index%>" disabled="disabled">

                <%
                    for (String property : eventFormatterPropertyDto[index].getOptions()) {
                        if (property.equals(eventFormatterPropertyDto[index].getValue())) {
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
            <input endpointType="<%=endpointType%>"
                   name="<%=eventFormatterPropertyDto[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= eventFormatterPropertyDto[index].getValue() != null ? eventFormatterPropertyDto[index].getValue() : "" %>" disabled="disabled"/>

            <% } %>


        </div>
    </td>

</tr>
<%
        }
    }


%>


</tbody>
</table>
</td>
</tr>
<%
    }
%>
</tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
