<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.processor.stub.types.ExecutionPlanConfigurationDto" %>
<%@ page import="org.wso2.carbon.event.processor.ui.EventProcessorUIUtils" %>
<%@ page import="org.wso2.carbon.event.processor.ui.executionPlan.flow.ExecutionPlanFlow" %>

<fmt:bundle basename="org.wso2.carbon.event.processor.ui.i18n.Resources">

<carbon:breadcrumb
        label="details"
        resourceBundle="org.wso2.carbon.event.processor.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../eventprocessor/js/execution_plans.js"></script>
<script type="text/javascript" src="../eventprocessor/js/eventprocessor_constants.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<link rel="stylesheet" href="../eventprocessor/css/exeFlow.css">
<script src="https://d3js.org/d3.v3.min.js" charset="utf-8"></script>
<script src="../eventprocessor/js/dagre-d3.js"></script>

<link type="text/css" href="../resources/css/registry.css" rel="stylesheet"/>

<script type="text/javascript">
    function doDeleteExecutionPlan(executionPlan) {
        CARBON.showConfirmationDialog("Are you sure want to delete execution plan:" + executionPlan,
                function () {
                    new Ajax.Request('../eventprocessor/delete_execution_plan_ajaxprocessor.jsp', {
                        method: 'POST',
                        asynchronous: false,
                        parameters: {
                            executionPlan: executionPlan
                        }, onSuccess: function (msg) {
                            if ("success" == msg.responseText.trim()) {
                                CARBON.showInfoDialog("Execution plan successfully deleted.", function () {
                                    window.location.href = "../eventprocessor/index.jsp?region=region1&item=execution_plan_menu.jsp";
                                });
                            } else {
                                CARBON.showErrorDialog("Failed to delete execution plan, Exception: " + msg.responseText.trim());
                            }
                        }
                    })
                }, null, null);
    }
</script>

<%--<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">--%>
<%
    String executionPlanName = request.getParameter("execPlanName");
    String executionPlanPath = request.getParameter("execPlanPath");
    String executionPlan = request.getParameter("execPlan");

    EventProcessorAdminServiceStub processorAdminServiceStub = EventProcessorUIUtils.getEventProcessorAdminService(config, session, request);
    ExecutionPlanConfigurationDto configurationDto = processorAdminServiceStub.getActiveExecutionPlanConfiguration(executionPlan);
    String planFlow = configurationDto.getExecutionPlan();

    ExecutionPlanFlow exePlanFlow = new ExecutionPlanFlow();
    String executionPlan_nodes = exePlanFlow.get_executionPlanFlow (planFlow) ;
%>

<script type="text/javascript">
    window.onload = function () {
            tryDrawProcessingFlowInfo();
            tryDraw();
        };
    function tryDrawProcessingFlowInfo() {
        var g = new dagreD3.graphlib.Graph().setGraph({});
        g.setNode("1", {labelType: "html",label: '<div id=flowdivInfo onmouseover= "" style="cursor: pointer;"> <span class="typeInfo typeInfo-I"></span><span  name="nameElement" class="nameInfo" style="margin-right: 10px;">Import Stream</span></div>'});
        g.setNode("2", {labelType: "html",label: '<div id=flowdivInfo onmouseover= "" style="cursor: pointer;"> <span class="typeInfo typeInfo-E"></span><span  name="nameElement" class="nameInfo" style="margin-right: 10px;">Export Stream</span></div>'});
        g.setNode("3", {labelType: "html",label: '<div id=flowdivInfo onmouseover= "" style="cursor: pointer;"> <span class="typeInfo typeInfo-S"></span><span  name="nameElement" class="nameInfo" style="margin-right: 10px;">Stream</span></div>'});
        g.setNode("4", {labelType: "html",label: '<div id=flowdivInfo onmouseover= "" style="cursor: pointer;"> <span class="typeInfo typeInfo-T"></span><span  name="nameElement" class="nameInfo" style="margin-right: 10px;">Table</span></div>'});
        g.setNode("5", {labelType: "html",label: '<div id=flowdivInfo onmouseover= "" style="cursor: pointer;"> <span class="typeInfo typeInfo-Q"></span><span  name="nameElement" class="nameInfo" style="margin-right: 10px;">Query</span></div>'});
        g.setNode("6", {labelType: "html",label: '<div id=flowdivInfo onmouseover= "" style="cursor: pointer;"> <span class="typeInfo typeInfo-PW"></span><span  name="nameElement" class="nameInfo" style="margin-right: 10px;">Partition</span></div>'});
    //left to right layout
        g.setGraph({
        nodesep: 70,
        ranksep: 50,
        marginx: 20,
        marginy: 20
        });
        g.nodes().forEach(function(v) {
          var node = g.node(v);
          node.padding = 0;
        });
        var render = new dagreD3.render();
        var svg = d3.select('#flowdivInfo'),
            svgGroup = svg.append('g');
        render(svgGroup, g);
    }
    function tryDraw() {
        // Create a new directed graph
        var g = new dagreD3.graphlib.Graph({compound:true})
          .setGraph({})
          .setDefaultEdgeLabel(function() { return {}; });
        var exeFlow = <%=executionPlan_nodes %>.replace(/\n/g, "\\n").replace(/\'/g, "");
        var queryFlow = jQuery.parseJSON(exeFlow);
        //create nodes
        var nodes = queryFlow.nodes;
        for (var i = 0; i < nodes.length; i++) {
            if(nodes[i].nodeTable == 'T'){
                nodes[i].nodeclass = 'T';
            }
            g.setNode(nodes[i].id, {labelType: "html",label: '<div id=flowdiv onmouseover= "" style="cursor: pointer;"data-toggle="tooltip" title=\'' + nodes[i].toolTip + '\'><span class="type type-'+ nodes[i].nodeclass + '"></span><span  name="nameElement" class="name" style="margin-right: 10px;">' + nodes[i].label + '</span></div>'});
        }
        g.nodes().forEach(function(v) {
            var node = g.node(v);
            //Round the corner's of each node
            node.rx = node.ry = 5;
            //remove the space around the content
            node.padding = 0;
        });
        // node which used to group
        var partitionWith = queryFlow.partitionWith;
        for (var i = 0; i < partitionWith.length; i++) {
            g.setNode(partitionWith[i].id, {labelType: "html",label:'<div onmouseover= "" style="cursor: pointer;"data-toggle="tooltip" title=\'' +partitionWith[i].toolTip +'\'> <span  name="nameElement" class="name" style="margin-right: 10px;">' + partitionWith[i].label + '</span></div>', clusterLabelPos: 'bottom', style: 'fill: #d3d7e8',  });
        }
        //set parent
        for (var i = 0; i < nodes.length; i++) {
            for (var k = 0; k < partitionWith.length; k++) {
                if(partitionWith[k].id == nodes[i].parent) {
                    g.setParent(nodes[i].id,partitionWith[k].id);
                }
            }
        }
        //create edges
        var edges = queryFlow.edges;
        for (var i = 0; i < edges.length; i++) {
            g.setEdge(edges[i].from, edges[i].to, {});
        }
        //left to right layout
        g.setGraph({
            nodesep: 70,
            ranksep: 50,
            rankdir: "LR",
            marginx: 20,
            marginy: 20
        });
        // Create the renderer
        var render = new dagreD3.render();
        // Set up an SVG group so that we can translate the final graph.
        var svg = d3.select('#flowdiv'),
            svgGroup = svg.append("g").attr("transform","translate(0,0)scale(0.75)");
        // Run the renderer. This is what draws the final graph.
        render(svgGroup, g);
        // Set up zoom support
        var zoom = d3.behavior.zoom().on("zoom", function() {
              svgGroup.attr("transform", "translate(" + d3.event.translate + ")" +
                                      "scale(" + d3.event.scale + ")");
            });
        svg.call(zoom);
        // Zoom and scale to fit
        var graphWidth = g.graph().width + 80;
        var graphHeight = g.graph().height + 40;
        var width = parseInt(svg.style("width").replace(/px/, ""));
        var height = parseInt(svg.style("height").replace(/px/, ""));
        var zoomScale = Math.min(width / graphWidth, height / graphHeight);
        var translate = [(width/2) - ((graphWidth*zoomScale)/2), (height/2) - ((graphHeight*zoomScale)/2)];
        zoom.translate(translate);
        zoom.scale(zoomScale);
        zoom.event(isUpdate ? svg.transition().duration(500) : d3.select("svg"));
        // Center the graph
        var xCenterOffset = (svg.attr("width") - g.graph().width) / 2;
        svgGroup.attr("transform", "translate(" + xCenterOffset + ", 20)");
        svg.attr("height", g.graph().height + 40);
    }
 </script>

<div id="middle">
<h2 style="padding-bottom: 7px">Event Processor Details
    <span style="float: right; font-size:75%">
        <% if (configurationDto.getEditable()) { %>
            <% if (configurationDto.getStatisticsEnabled()) {%>
            <div style="display: inline-block">
                <div id="disableStat<%= configurationDto.getName()%>">
                    <a href="#"
                       onclick="disableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
                <div id="enableStat<%= configurationDto.getName()%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div style="display: inline-block">
                <div id="enableStat<%= configurationDto.getName()%>">
                    <a href="#"
                       onclick="enableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon-disabled.gif);"><fmt:message
                            key="stat.enable.link"/></a>
                </div>
                <div id="disableStat<%= configurationDto.getName()%>"
                     style="display:none">
                    <a href="#"
                       onclick="disableStat('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/static-icon.gif);"><fmt:message
                            key="stat.disable.link"/></a>
                </div>
            </div>
            <% }
                if (configurationDto.getTracingEnabled()) {%>
            <div style="display: inline-block">
                <div id="disableTracing<%= configurationDto.getName()%>">
                    <a href="#"
                       onclick="disableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
                <div id="enableTracing<%= configurationDto.getName()%>"
                     style="display:none;">
                    <a href="#"
                       onclick="enableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
            </div>
            <% } else { %>
            <div style="display: inline-block">
                <div id="enableTracing<%= configurationDto.getName() %>">
                    <a href="#"
                       onclick="enableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon-disabled.gif);"><fmt:message
                            key="trace.enable.link"/></a>
                </div>
                <div id="disableTracing<%= configurationDto.getName() %>"
                     style="display:none">
                    <a href="#"
                       onclick="disableTracing('<%= configurationDto.getName() %>')"
                       class="icon-link"
                       style="background-image:url(../admin/images/trace-icon.gif);"><fmt:message
                            key="trace.disable.link"/></a>
                </div>
            </div>

            <% } %>

            <div style="display: inline-block">
                <a style="background-image: url(../admin/images/delete.gif);"
                   class="icon-link"
                   onclick="doDeleteExecutionPlan('<%=configurationDto.getName()%>')"><font
                        color="#4682b4">Delete</font></a>
            </div>
            <div style="display: inline-block">
                <a style="background-image: url(../admin/images/edit.gif);"
                   class="icon-link"
                   href="../eventprocessor/edit_execution_plan.jsp?ordinal=1&execPlanName=<%=configurationDto.getName()%>"><font
                        color="#4682b4">Edit</font></a>
            </div>

            <% } else { %>
            <div style="display: inline-block">
                <div id="cappArtifact<%= configurationDto.getName()%>">
                    <div style="background-image: url(images/capp.gif);" class="icon-nolink-nofloat">
                        <fmt:message key="capp.artifact.message"/></div>
                </div>
            </div>

            <% } %>
    </span>
</h2>

<div id="workArea">
<table style="width:100%" id="eventProcessorDetails" class="styledLeft noBorders spacer-bot">
<tbody>
<tr>
<td>

<table width="100%" style="border: 0px" >

    <%--code mirror code--%>

<link rel="stylesheet" href="../eventprocessor/css/codemirror.css"/>
<script src="../eventprocessor/js/codemirror.js"></script>
<script src="../eventprocessor/js/sql.js"></script>

<style>
    .CodeMirror {
        border-top: 1px solid #cccccc;
        border-bottom: 1px solid black;
    }
</style>

<script>
    var init = function () {
        var mime = MIME_TYPE_SIDDHI_QL;

        // get mime type
        if (window.location.href.indexOf('mime=') > -1) {
            mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
        }

        window.queryEditor = CodeMirror.fromTextArea(document.getElementById('queryExpressions'), {
            mode: mime,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            readOnly: true
        });
    };
</script>

<script type="text/javascript">
    jQuery(document).ready(function () {
        init();
    });
</script>

    <%--Code mirror code end--%>
<tr>
     <td>
  	<b> Execution Plan Flow </b>
    </td>
</tr>
<tr>
    <td colspan="2" >
	    <div>
            <svg id=flowdivInfo  width=100% height=60 style="border: 0px"> </svg>
        </div>
     </td>
</tr>
<tr>
     <td colspan="2">
	    <div>
            <svg id=flowdiv width=100% height=350 ></svg>
        </div>
     </td>
</tr>
<tr>
    <td colspan="2">
        <b><fmt:message key="execution.plan"/></b>
    </td>
</tr>
<%--imported stream mappings--%>
<tr>
    <td colspan="2">
        <style>
            div#workArea table#streamDefinitionsTable tbody tr td {
                padding-left: 45px !important;
            }
        </style>
        <table width="100%" style="border: 1px solid #cccccc">
                <%--query expressions--%>
            <tr>
                <td>
                    <textarea class="queryExpressionsTextArea" style="width:100%; height: 110px"
                              id="queryExpressions"
                              name="queryExpressions" readonly><%= configurationDto.getExecutionPlan() %>
                    </textarea>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</tbody>
</table>
</div>
</div>
</fmt:bundle>