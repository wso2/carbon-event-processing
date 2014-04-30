<%@ page import="org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.manager.ui.EventStreamUIUtils" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamDefinitionDto" %>
<%@ page import="org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.stream.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="add"
        resourceBundle="org.wso2.carbon.event.stream.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
<script type="text/javascript" src="../eventstream/js/registry-browser.js"></script>

<link type="text/css" href="css/eventStream.css" rel="stylesheet"/>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="js/event_stream.js"></script>
<script type="text/javascript"
        src="js/create_eventStream_helper.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<% String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");

    EventStreamAdminServiceStub eventStreamAdminServiceStub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);
    EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(eventStreamWithVersion);

%>

<div id="middle">
<h2><fmt:message key="title.event.stream.edit"/></h2>

<div id="workArea">

<form name="inputForm" action="index.jsp?ordinal=1" method="post" id="editEventStream">
<table style="width:100%" id="eventStreamEdit" class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="title.event.stream.details"/></th>
</tr>
</thead>
<tbody>
<tr>
<td class="formRaw">
<table id="eventStreamInputTable" class="normal-nopadding"
       style="width:100%">
<tbody>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.name"/><span
            class="required">*</span>
    </td>
    <td><input type="text" name="eventStreamName" id="eventStreamNameId"
               class="initE"

               value="<%=streamDefinitionDto.getName()%>"
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.stream.name.help"/>
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.version"/><span class="required">*</span>
    </td>
    <td><input type="text" name="eventStreamVersion" id="eventStreamVersionId"
               class="initE"

               value="<%=streamDefinitionDto.getVersion()%>"
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.stream.version.help"/>
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.description"/>
    </td>
    <td><input type="text" name="eventStreamDescription" id="eventStreamDescription"
               class="initE"

               value="<%=streamDefinitionDto.getDescription() != null ? streamDefinitionDto.getDescription() : "" %>"
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.stream.description.help"/>
        </div>
    </td>
</tr>

<tr>
    <td class="leftCol-med"><fmt:message key="event.stream.nickname"/>
    </td>
    <td><input type="text" name="eventStreamNickName" id="eventStreamNickName"
               class="initE"

               value="<%=streamDefinitionDto.getNickName() != null ? streamDefinitionDto.getNickName() : "" %>"
               style="width:75%"/>

        <div class="sectionHelp">
            <fmt:message key="event.stream.nickname.help"/>
        </div>
    </td>
</tr>

<tr>
<td colspan="2">
<div id="innerDiv4">
<table class="styledLeft noBorders spacer-bot"
       style="width:100%">
<tbody>
<tr name="streamAttributes">
    <td colspan="2" class="middle-header">
        <fmt:message key="stream.attributes"/>
    </td>
</tr>

<tr name="streamAttributes">
    <td colspan="2">

        <h6><fmt:message key="attribute.data.type.meta"/></h6>
        <% if (streamDefinitionDto.getMetaData() != null && streamDefinitionDto.getMetaData().length > 0) {

        %>
        <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <% for (EventStreamAttributeDto metaData : streamDefinitionDto.getMetaData()) {
            %>
            <tr>
                <td class="property-names"><%=metaData.getAttributeName()%>
                </td>
                <td class="property-names"><%=metaData.getAttributeType()%>
                </td>
                <td class="property-names"><a class="icon-link"
                                              style="background-image:url(../admin/images/delete.gif)"
                                              onclick="removeStreamAttribute(this,'meta')">Delete</a>
                </td>
            </tr>
            <%}%>
        </table>
        <%
        } else

        {
        %>
        <table class="styledLeft noBorders spacer-bot" id="outputMetaDataTable"
               style="display: none">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputMetaData">
            <fmt:message key="no.meta.attributes.defined"/>
        </div>
        <% } %>
        <table id="addMetaData" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="attribute.name"/> :</td>
                <td>
                    <input type="text" id="outputMetaDataPropName"/>
                </td>
                <td class="col-small"><fmt:message key="attribute.type"/> :
                </td>
                <td>
                    <select id="outputMetaDataPropType">
                        <option value="int">int</option>
                        <option value="long">long</option>
                        <option value="double">double</option>
                        <option value="float">float</option>
                        <option value="string">string</option>
                        <option value="boolean">boolean</option>
                    </select>
                </td>
                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addStreamAttribute('Meta')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>


<tr name="streamAttributes">
    <td colspan="2">

        <h6><fmt:message key="attribute.data.type.correlation"/></h6>
        <% if (streamDefinitionDto.getCorrelationData() != null && streamDefinitionDto.getCorrelationData().length > 0) {

        %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputCorrelationDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <% for (EventStreamAttributeDto correlationData : streamDefinitionDto.getCorrelationData()) {
            %>
            <tr>
                <td class="property-names"><%=correlationData.getAttributeName()%>
                </td>
                <td class="property-names"><%=correlationData.getAttributeType()%>
                </td>
                <td class="property-names"><a class="icon-link"
                                              style="background-image:url(../admin/images/delete.gif)"
                                              onclick="removeStreamAttribute(this,'Correlation')">Delete</a>
                </td>
            </tr>
            <%}%>
        </table>
        <% } else { %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputCorrelationDataTable" style="display: none">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputCorrelationData">
            <fmt:message key="no.correlation.attributes.defined"/>
        </div>
        <% } %>
        <table id="addCorrelationData" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="attribute.name"/> :</td>
                <td>
                    <input type="text" id="outputCorrelationDataPropName"/>
                </td>
                <td class="col-small"><fmt:message key="attribute.type"/> :
                </td>
                <td>
                    <select id="outputCorrelationDataPropType">
                        <option value="int">int</option>
                        <option value="long">long</option>
                        <option value="double">double</option>
                        <option value="float">float</option>
                        <option value="string">string</option>
                        <option value="boolean">boolean</option>
                    </select>
                </td>
                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addStreamAttribute('Correlation')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>

<tr name="streamAttributes">
    <td colspan="2">

        <h6><fmt:message key="attribute.data.type.payload"/></h6>
        <% if (streamDefinitionDto.getPayloadData() != null && streamDefinitionDto.getPayloadData().length > 0) {

        %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputPayloadDataTable">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>

            <% for (EventStreamAttributeDto payloadData : streamDefinitionDto.getPayloadData()) {
            %>
            <tr>
                <td class="property-names"><%=payloadData.getAttributeName()%>
                </td>
                <td class="property-names"><%=payloadData.getAttributeType()%>
                </td>
                <td class="property-names"><a class="icon-link"
                                              style="background-image:url(../admin/images/delete.gif)"
                                              onclick="removeStreamAttribute(this,'Payload')">Delete</a>
                </td>
            </tr>
            <%}%>

        </table>
        <% } else { %>
        <table class="styledLeft noBorders spacer-bot"
               id="outputPayloadDataTable" style="display: none;">
            <thead>
            <th class="leftCol-med"><fmt:message key="attribute.name"/></th>
            <th class="leftCol-med"><fmt:message key="attribute.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputPayloadData">
            <fmt:message key="no.payload.attributes.defined"/>
        </div>
        <% } %>

        <table id="addPayloadData" class="normal">
            <tbody>
            <tr>
                <td class="col-small"><fmt:message key="attribute.name"/> :</td>
                <td>
                    <input type="text" id="outputPayloadDataPropName"/>
                </td>
                <td class="col-small"><fmt:message key="attribute.type"/> :
                </td>
                <td>
                    <select id="outputPayloadDataPropType">
                        <option value="int">int</option>
                        <option value="long">long</option>
                        <option value="double">double</option>
                        <option value="float">float</option>
                        <option value="string">string</option>
                        <option value="boolean">boolean</option>
                    </select>
                </td>
                <td><input type="button" class="button"
                           value="<fmt:message key="add"/>"
                           onclick="addStreamAttribute('Payload')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>
</table>
</div>
</td>
</tr>

</tbody>
</table>
</td>
</tr>
<tr>
    <td class="buttonRow">
        <input type="button" value="<fmt:message key="edit.event.stream"/>"
               onclick="addEventStream(document.getElementById('editEventStream'),'edit', '<%=eventStreamWithVersion%>')"/>
    </td>
</tr>
</tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
