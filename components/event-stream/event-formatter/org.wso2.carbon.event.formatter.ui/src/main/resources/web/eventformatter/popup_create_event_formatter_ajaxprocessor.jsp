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
<%@ page import="org.wso2.carbon.event.formatter.stub.EventFormatterAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.formatter.stub.types.EventFormatterPropertyDto" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIConstants" %>
<%@ page import="org.wso2.carbon.event.formatter.ui.EventFormatterUIUtils" %>
<%@ page import="org.wso2.carbon.event.output.adaptor.manager.stub.OutputEventAdaptorManagerAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.output.adaptor.manager.stub.types.OutputEventAdaptorConfigurationInfoDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamDefinitionDto" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.formatter.ui.i18n.Resources">
<script type="text/javascript" src="../eventformatter/js/event_formatter.js"></script>
<script type="text/javascript" src="../eventformatter/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript"
        src="../eventformatter/js/create_eventFormatter_helper.js"></script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        showMappingContext();
    });

</script>

<div id="middle">
<h2><fmt:message key="title.event.formatter.create"/></h2>

<div id="workArea">

<form name="inputForm" action="#" method="post" id="addEventFormatter">
<table style="width:100%" id="eventFormatterAdd" class="styledLeft">
<%
    EventFormatterAdminServiceStub eventFormatterAdminServiceStub = EventFormatterUIUtils.getEventFormatterAdminService(config, session, request);
    OutputEventAdaptorManagerAdminServiceStub outputEventAdaptorManagerAdminServiceStub =
            EventFormatterUIUtils.getOutputEventManagerAdminService(config, session, request);
    OutputEventAdaptorConfigurationInfoDto[] outputEventAdaptorInfoArray = outputEventAdaptorManagerAdminServiceStub.getAllActiveOutputEventAdaptorConfiguration();
    String streamId = request.getParameter("streamId");
    String redirectPage = request.getParameter("redirectPage");
    if (streamId != null) {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = EventFormatterUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);
        EventStreamAttributeDto[] metaAttributeList = streamDefinitionDto.getMetaData();
        EventStreamAttributeDto[] correlationAttributeList = streamDefinitionDto.getCorrelationData();
        EventStreamAttributeDto[] payloadAttributeList = streamDefinitionDto.getPayloadData();

        String attributes = "";

        if (metaAttributeList != null && metaAttributeList.length > 0) {
            for (EventStreamAttributeDto attribute : metaAttributeList) {
                attributes += EventFormatterUIConstants.PROPERTY_META_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (correlationAttributeList != null) {
            for (EventStreamAttributeDto attribute : correlationAttributeList) {
                attributes += EventFormatterUIConstants.PROPERTY_CORRELATION_PREFIX + attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }
        if (payloadAttributeList != null) {
            for (EventStreamAttributeDto attribute : payloadAttributeList) {
                attributes += attribute.getAttributeName() + " " + attribute.getAttributeType() + ", \n";
            }
        }

        if (!attributes.equals("")) {
            attributes = attributes.substring(0, attributes.lastIndexOf(","));
        }

        String streamDefinition = attributes;

        List<String> attributeList = EventFormatterUIUtils.getAttributeListWithPrefix(streamDefinitionDto);
%>
<br/>
<thead>
<tr>
    <th><fmt:message key="title.event.formatter.details"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">
<table id="eventFormatterInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.formatter.name"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventFormatterName" id="eventFormatterId"
               class="initE"

               value=""
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.formatter.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td colspan="2">
        <b><fmt:message key="from.heading"/></b>
    </td>
</tr>

<tr>
    <td>
        <fmt:message key="stream.attributes"/>
    </td>
    <td>
        <textArea class="expandedTextarea" id="streamDefinitionText" name="streamDefinitionText"
                  readonly="true"
                  cols="60"><%=streamDefinition%>
        </textArea>

    </td>

</tr>

<tr>
    <td><fmt:message key="event.adaptor.name"/><span class="required">*</span></td>
    <td>
        <table>
            <td class="custom-noPadding" width="60%"><select name="eventAdaptorNameFilter"
                                                             onchange="loadEventAdaptorRelatedProperties('<fmt:message
                                                                     key="to.heading"/>')"
                                                             id="eventAdaptorNameFilter">
                <%
                    String firstEventAdaptorName = null;

                    if (outputEventAdaptorInfoArray != null) {
                        firstEventAdaptorName = outputEventAdaptorInfoArray[0].getEventAdaptorName();
                        for (OutputEventAdaptorConfigurationInfoDto outputEventAdaptorInfoDto : outputEventAdaptorInfoArray) {
                %>
                <option value="<%=outputEventAdaptorInfoDto.getEventAdaptorName() + "$=" + outputEventAdaptorInfoDto.getEventAdaptorType()%>"><%=outputEventAdaptorInfoDto.getEventAdaptorName()%>
                </option>
                <%
                        }
                    }
                %>

            </select>

                <div class="sectionHelp">
                    <fmt:message key="event.adaptor.name.help"/>
                </div>
            </td>
            <td width="40%" id="addOutputEventAdaptorTD" class="custom-noPadding"></td>
        </table>
    </td>

</tr>

<%
    EventFormatterPropertyDto[] eventFormatterProperties = eventFormatterAdminServiceStub.getEventFormatterMessageProperties(firstEventAdaptorName);
    if (eventFormatterProperties != null) {
%>
<tr>
    <td>
        <b><fmt:message key="to.heading"/></b>
    </td>
</tr>
<%
    for (int index = 0; index < eventFormatterProperties.length; index++) {
%>
<tr>


    <td class="leftCol-med"><%=eventFormatterProperties[index].getDisplayName()%>
        <%
            String propertyId = "property_";
            if (eventFormatterProperties[index].getRequired()) {
                propertyId = "property_Required_";

        %>
        <span class="required">*</span>
        <%
            }
        %>
    </td>
    <%
        String type = "text";
        if (eventFormatterProperties[index].getSecured()) {
            type = "password";
        }
    %>

    <td>
        <div class=outputFields>
            <%
                if (eventFormatterProperties[index].getOptions()[0] != null) {
            %>

            <select name="<%=eventFormatterProperties[index].getKey()%>"
                    id="<%=propertyId%><%=index%>">

                <%
                    for (String property : eventFormatterProperties[index].getOptions()) {
                        if (property.equals(eventFormatterProperties[index].getDefaultValue())) {
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
                   name="<%=eventFormatterProperties[index].getKey()%>"
                   id="<%=propertyId%><%=index%>" class="initE"
                   style="width:75%"
                   value="<%= (eventFormatterProperties[index].getDefaultValue()) != null ? eventFormatterProperties[index].getDefaultValue() : "" %>"
                    />

            <% }

                if (eventFormatterProperties[index].getHint() != null) { %>
            <div class="sectionHelp">
                <%=eventFormatterProperties[index].getHint()%>
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
    <td colspan="2">
        <b><fmt:message key="mapping.heading"/></b>
    </td>
</tr>

<tr>
    <td><fmt:message key="mapping.type"/><span class="required">*</span></td>
    <td><select name="mappingTypeFilter"
                onchange="showMappingContext()" id="mappingTypeFilter">
        <%
            String[] mappingTypes = outputEventAdaptorManagerAdminServiceStub.getSupportedMappingTypes(firstEventAdaptorName);


            if (mappingTypes != null) {
                for (String mappingType : mappingTypes) {
        %>
        <option><%=mappingType%>
        </option>
        <%
                }
            }
        %>

    </select>

        <div class="sectionHelp">
            <fmt:message key="mapping.type.help"/>
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
<td class="formRaw" colspan="2">
<div id="outerDiv" style="display:none">

<div id="innerDiv1" style="display:none">

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

                <h6><fmt:message key="property.data.type.meta"/></h6>
                <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputMetaData">
                    <fmt:message key="no.meta.defined.message"/>
                </div>
                <table id="addMetaData" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputMetaDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :
                        </td>
                        <td>
                            <select id="outputMetaDataPropValueOf">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addOutputWSO2EventProperty('Meta')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>


        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.correlation"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputCorrelationDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputCorrelationData">
                    <fmt:message key="no.correlation.defined.message"/>
                </div>
                <table id="addCorrelationData" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputCorrelationDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :
                        </td>
                        <td>
                            <select id="outputCorrelationDataPropValueOf">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addOutputWSO2EventProperty('Correlation')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr name="outputWSO2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.payload"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="outputPayloadDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputPayloadData">
                    <fmt:message key="no.payload.defined.message"/>
                </div>
                <table id="addPayloadData" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputPayloadDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :
                        </td>
                        <td>
                            <select id="outputPayloadDataPropValueOf">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addOutputWSO2EventProperty('Payload')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</div>


<div id="innerDiv2" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputTextMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="text.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_text" type="radio" checked="checked" value="content"
                       name="inline_text" onclick="enable_disable_Registry(this)">
                <label for="inline_text"><fmt:message key="inline.input"/></label>
                <input id="registry_text" type="radio" value="reg" name="registry_text"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_text"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputTextMappingInline" id="outputTextMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="textSourceText" name="textSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
    height: 150px; margin-top: 5px;"
                              name="textSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputTextMappingRegistry" style="display:none" id="outputTextMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1"><input type="text" id="textSourceRegistry" disabled="disabled"
                                   class="initE"
                                   value=""
                                   style="width:100%"/></td>

            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('textSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('textSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>

<div id="innerDiv3" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputXMLMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="xml.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml" type="radio" checked="checked" value="content"
                       name="inline_xml" onclick="enable_disable_Registry(this)">
                <label for="inline_xml"><fmt:message key="inline.input"/></label>
                <input id="registry_xml" type="radio" value="reg" name="registry_xml"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_xml"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingInline" id="outputXMLMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="xmlSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="xmlSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputXMLMappingRegistry" style="display:none" id="outputXMLMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="xmlSourceRegistry" disabled="disabled" class="initE" value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('xmlSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('xmlSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>


<div id="innerDiv4" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputMapMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="map.mapping"/>
            </td>
        </tr>
        <tr name="outputMapMapping">
            <td colspan="2">

                <table class="styledLeft noBorders spacer-bot" id="outputMapPropertiesTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message key="property.name"/></th>
                    <th class="leftCol-med"><fmt:message key="property.value.of"/></th>
                    <th><fmt:message key="actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noOutputMapProperties">
                    <fmt:message key="no.map.properties.defined"/>
                </div>
                <table id="addOutputMapProperties" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="property.name"/> :</td>
                        <td>
                            <input type="text" id="outputMapPropName"/>
                        </td>
                        <td class="col-small"><fmt:message key="property.value.of"/> :</td>
                        <td>
                            <select id="outputMapPropValueOf">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>
                        </td>
                        <td><input type="button" class="button" value="<fmt:message key="add"/>"
                                   onclick="addOutputMapProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</div>

<div id="innerDiv5" style="display:none">
    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputJSONMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="json.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json" type="radio" checked="checked" value="content"
                       name="inline_json" onclick="enable_disable_Registry(this)">
                <label for="inline_json"><fmt:message key="inline.input"/></label>
                <input id="registry_json" type="radio" value="reg" name="registry_json"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_json"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingInline" id="outputJSONMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="jsonSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="jsonSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputJSONMappingRegistry" style="display:none" id="outputJSONMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="jsonSourceRegistry" disabled="disabled" class="initE"
                       value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('jsonSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('jsonSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        </tbody>
    </table>
</div>


</div>
</td>
</tr>

</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="add.event.formatter"/>"
               onclick="addEventFormatterViaPopup(document.getElementById('addEventFormatter') ,'<%=streamId%>','<%=redirectPage%>')"/>
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
                <td class="leftCol-med" colspan="2">Event Streams or Output
                                                    Event Adaptors are not available, Please create
                                                    an Output Event Adaptor to continue...
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