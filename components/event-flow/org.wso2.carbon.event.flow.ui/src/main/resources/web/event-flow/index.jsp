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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.event.flow.ui.client.EventFlowAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.event.flow.ui.i18n.Resources">
<carbon:breadcrumb
		label="eventflowmenutext"
		resourceBundle="org.wso2.carbon.event.flow.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />
<script src="../carbon/global-params.js" type="text/javascript"></script>
<script type="text/javascript" src="js/d3.v3.min.js"></script>
<script type="text/javascript" src="js/dagre-d3.min.js"></script>
<script type="text/javascript" src="js/graphlib-dot.min.js"></script>

<style>

    .type-ES {
        background-color: #54c082;
    }

    .type-EP  {
        background-color: #a33c74;
    }

    .type-IEA  {
        background-color: #3d35d5;
    }

    .type-OEA {
        background-color: #4cb5f4;
    }

    .type-EB  {
        background-color: #FF8A3C;
    }

    .type-EF {
        background-color: #ffc719;
    }


    .type-ES-info {
        background-image: url(../eventstream/images/event-stream.png);
    }

    .type-EP-info   {
        background-image: url(../eventprocessor/images/executionPlan.gif);

    }

    .type-IEA-info   {
        background-image: url(../inputeventadaptormanager/images/eventAdaptor.gif);
    }

    .type-OEA-info  {
        background-image: url(../outputeventadaptormanager/images/eventAdaptor.gif);
    }

    .type-EB-info   {
        background-image:url(../eventstream/images/eventBuilder.gif);
    }

    .type-EF-info  {
        background-image:url(../eventstream/images/event_formatter.gif);
    }

    .node g div {
        height: 30px;
        line-height: 30px;
    }

    .name-info{
        white-space:nowrap;
        font-weight: bold;
    }

    .name{
        white-space:nowrap;
    }

    .type {
        height: 30px;
        width: 10px;
        display: block;
        float: left;
        border-top-left-radius: 5px;
        border-bottom-left-radius: 5px;
        margin-right: 4px;
    }

    .infoType {
        background-repeat: no-repeat;
        background-position: 5px 50%;
        width: 25px;
    }

    svg {
        overflow: hidden;
    }

    text {
        font-weight: 300;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serf;
    }

    .node rect {
        stroke-width: 1px;
        stroke: #707070;
        fill: #F0F0F0 ;
    }

    .edgeLabel rect {
        fill: #fff;
    }

    .edgePath path {
        stroke: #333;
        stroke-width: 1.5px;
        fill: none;
    }

</style>


<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EventFlowAdminServiceClient client;
    String[] logs = new String[0];
    try {
        client = new EventFlowAdminServiceClient(cookie, backendServerURL, configContext,
                                                  request.getLocale());


            String eventFlow = client.getEventFlow();

%>
<script type="text/javascript">
    var eventFlow= jQuery.parseJSON( <%=eventFlow%> );
</script>
<%
    } catch (Exception e) {
%>
<script type="text/javascript">
   window.location.href = "../admin/error.jsp";
</script>
<%
    }
%>

<script type="text/javascript">


wso2.wsf.Util.initURLs();

var frondendURL = wso2.wsf.Util.getServerURL() + "/";

window.onload=function(){tryDrawProcessingFlowInfo();tryDraw();removeMargins();};

function tryDrawProcessingFlowInfo() {

    var g = new dagreD3.Digraph();

    g.addNode("InputEventAdaptors", { label: '<div onclick="location.href = \'../inputeventadaptormanager/index.jsp \';" onmouseover="" style="cursor: pointer;" ><span class="infoType type type-IEA  type-IEA-info"></span><span name="nameElement" style="margin-right: 25px;" class="name-info" ><fmt:message key="input.event.adaptors"/></span></div>' });
    g.addNode("EventBuilders", { label: '<div onclick="location.href = \'../eventbuilder/index.jsp \';" onmouseover="" style="cursor: pointer;"><span class="infoType type type-EB  type-EB-info"></span><span name="nameElement" style="margin-right: 25px;" class="name-info" ><fmt:message key="event.builders"/></span></div>' });
    g.addNode("EventStreams", { label: '<div onclick="location.href = \'../eventstream/index.jsp \';" onmouseover="" style="cursor: pointer;"><span class="infoType type type-ES  type-ES-info"></span><span name="nameElement" style="margin-right: 25px;" class="name-info" ><fmt:message key="event.streams"/></span></div>' });
    g.addNode("EventFormatters", { label: '<div onclick="location.href = \'../eventformatter/index.jsp \';" onmouseover="" style="cursor: pointer;"><span class="infoType type type-EF  type-EF-info"></span><span name="nameElement" style="margin-right: 25px;" class="name-info" ><fmt:message key="event.formatters"/></span></div>' });
    g.addNode("OutputEventAdaptors", { label: '<div onclick="location.href = \'../outputeventadaptormanager/index.jsp \';" onmouseover="" style="cursor: pointer;"><span class="infoType type type-OEA  type-OEA-info"></span><span name="nameElement" style="margin-right: 25px;" class="name-info" ><fmt:message key="output.event.adaptors"/></span></div>' });
    g.addNode("ExecutionPlans", { label: '<div onclick="location.href = \'../eventprocessor/index.jsp \';" onmouseover="" style="cursor: pointer;"><span class="infoType type type-EP type-EP-info"></span><span name="nameElement" style="margin-right: 25px;" class="name-info" ><fmt:message key="execution.plans"/></span></div>' });

    g.addEdge(null, "InputEventAdaptors", "EventBuilders", { style: 'stroke: #7a0177; stroke-width: 2px;' });
    g.addEdge(null, "EventBuilders", "EventStreams", { style: 'stroke: #7a0177; stroke-width: 2px;' });
    g.addEdge(null, "EventStreams", "EventFormatters", { style: 'stroke: #7a0177; stroke-width: 2px;' });
    g.addEdge(null, "EventFormatters", "OutputEventAdaptors", { style: 'stroke: #7a0177; stroke-width: 2px;' });
    g.addEdge(null, "EventStreams", "ExecutionPlans", { style: 'stroke: #7a0177; stroke-width: 2px;' });
    g.addEdge(null, "ExecutionPlans", "EventStreams", { style: 'stroke: #7a0177; stroke-width: 2px;' });

    var renderer = new dagreD3.Renderer();

    var svg = d3.select("#flowInfo");
    var svgGroup = d3.select("#flowInfo g");

    renderer.zoom(false);

    var layout = dagreD3.layout()
            .nodeSep(5)
            .edgeSep(30)
            .rankSep(40)
            .rankDir("LR");
    renderer.layout(layout);

    layout = renderer.run(g, svgGroup);

    svg.attr("width", layout.graph().width).attr("height", layout.graph().height);

}

function tryDraw() {

    var g = new dagreD3.Digraph();

    var nodes = eventFlow.nodes;
    for (var i = 0; i < nodes.length; i++) {
        g.addNode(nodes[i].id, { label: '<div onclick="location.href = \''+nodes[i].url+' \';" onmouseover="" style="cursor: pointer;" ><span class="type type-' + nodes[i].nodeclass + '"></span><span name="nameElement" class="name" style="margin-right: 10px;">' + nodes[i].label + '</span></div>' });
    }

    var edges = eventFlow.edges;
    for (i = 0; i < edges.length; i++) {
        g.addEdge(null, edges[i].from, edges[i].to);
    }

    var renderer = new dagreD3.Renderer();

    var svg = d3.select("#flowData");
    var svgGroup = d3.select("#flowData g");

    // Set initial zoom to 75%
    var initialScale = 0.75;
    var oldZoom = renderer.zoom();
    renderer.zoom(function (graph, svg) {
        var zoom = oldZoom(graph, svg);

        // We must set the zoom and then trigger the zoom event to synchronize
        // D3 and the DOM.
        zoom.scale(initialScale).event(svg);
        return zoom;
    });

    var layout = dagreD3.layout()
            .nodeSep(10)
            .edgeSep(10)
            .rankDir("LR");
    renderer.layout(layout);

    layout = renderer.run(g, svgGroup);

    svg.transition().duration(500)
            .attr("width", layout.graph().width * initialScale + 40)
            .attr("height", layout.graph().height * initialScale + 40);

}

function removeMargins() {

    var nameElements = document.getElementsByName("nameElement");
    for (var i = 0; i < nameElements.length; i++) {
        nameElements[i].style.cssText = "margin-right: 0px; ";
    }

}

</script>

<div id="middle">
    <h2><fmt:message key="event.flow"/></h2>

    <div id="workArea">
        <div id="flowdivInfo">
            <svg id="flowInfo" width=100% height=100%>
                <g transform="translate(0,0)scale(0.75)"></g>
            </svg>

        </div>
       <h2></h2>
        <div id="flowdiv">
            <svg id="flowData" width=100% height=100% style="border: 1px solid #999;">
                <g transform="translate(20, 20)"></g>
            </svg>
        </div>
    </div>
</div>
</fmt:bundle>
