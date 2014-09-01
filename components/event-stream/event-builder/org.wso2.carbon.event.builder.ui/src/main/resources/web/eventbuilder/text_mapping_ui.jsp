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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.event.builder.ui.EventBuilderUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamDefinitionDto" %>
<%@ page import="java.util.List" %>

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">
    <link type="text/css" href="../eventbuilder/css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>
    <%
        String streamId = request.getParameter("streamNameWithVersion");
        EventStreamAdminServiceStub eventStreamAdminServiceStub = EventBuilderUIUtils.getEventStreamAdminService(config, session, request);
        EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(streamId);
        List<String> attributeList = EventBuilderUIUtils.getAttributeListWithPrefix(streamDefinitionDto);

    %>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr fromElementKey="inputTextMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.builder.mapping.text"/>
            </td>
        </tr>
        <tr fromElementKey="inputTextMapping">
            <td colspan="2">
                <h6><fmt:message key="regex.definition.header"/></h6>
                <table class="styledLeft noBorders spacer-bot" id="inputRegexDefTable"
                       style="display:none">
                    <thead>
                    <th><fmt:message
                            key="event.builder.regex.expr"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputRegexDefTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputRegex">
                    No Regular Expressions Defined
                </div>
                <table id="addRegexDefinition" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="event.builder.regex.expr"/> : <span
                                class="required">*</span>
                        </td>
                        <td>
                            <input type="text" id="inputRegexDef"/>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputRegexDef()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        <tr fromElementKey="inputTextMapping">
            <td colspan="2">

                <h6><fmt:message key="text.mapping.header"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="inputTextMappingTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.regex"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.valueof"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.type"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.default"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputTextMappingTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputProperties">
                    No Text Mappings Available
                </div>
                <table id="addTextMappingTable" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.regex"/> :
                            <select id="inputPropertyValue">
                                <option value="">No regular expression defined</option>
                            </select>
                        </td>
                        <td class="col-small"><fmt:message key="event.builder.property.valueof"/> :
                        </td>
                        <td>
                            <select id="inputPropertyName">
                                <% for (String attributeData : attributeList) {
                                    String[] attributeValues = attributeData.split(" ");
                                %>
                                <option value="<%=attributeValues[0]%>"><%=attributeValues[0]%>
                                </option>
                                <% }%>
                            </select>

                        </td>
                        <td><fmt:message key="event.builder.property.type"/>:
                            <select id="inputPropertyType">
                                <option value="int">int</option>
                                <option value="long">long</option>
                                <option value="double">double</option>
                                <option value="float">float</option>
                                <option value="string">string</option>
                                <option value="boolean">boolean</option>
                            </select>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.default"/></td>
                        <td><input type="text" id="inputPropertyDefault"/></td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputTextProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</fmt:bundle>