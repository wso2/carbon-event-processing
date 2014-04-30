<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<script type="text/javascript" src="js/d3.v3.js"></script>
<script type="text/javascript" src="js/dagre-d3.min.js"></script>
<script type="text/javascript" src="js/graphlib-dot.min.js"></script>

<style>

    g.type-ES > rect {
        fill: #00e8ba;
    }

    g.type-EP > rect {
        fill: #c0699e;
    }

    g.type-IEA > rect {
        fill: #4395cb;
    }

    g.type-OEA > rect {
        fill: #4cb5f4;
    }

    g.type-EB > rect {
        fill: #e3b217;
    }

    g.type-EF > rect {
        fill: #ffc719;
    }

    svg {
        border: 1px solid #999;
        overflow: hidden;
    }

    text {
        font-weight: 300;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serf;
        font-size: 14px;
    }

    .node rect {
        stroke: #999;
        stroke-width: 1px;
        fill: #fff;
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

window.onload=function(){tryDraw();};

function tryDraw() {
    var g = new dagreD3.Digraph();
    var nodes= eventFlow.nodes;
    for (var i=0; i<nodes.length;i++){
        g.addNode(nodes[i].id, { label: "<div style='padding: 10px;'>"+nodes[i].label+"</div>" , nodeclass: "type-"+nodes[i].nodeclass });

    }

    var edges= eventFlow.edges;
    for (i=0; i<edges.length;i++){
        g.addEdge(null, edges[i].from, edges[i].to);
    }

    var renderer = new dagreD3.Renderer();
    var oldDrawNodes = renderer.drawNodes();
    renderer.drawNodes(function (graph, root) {
        var svgNodes = oldDrawNodes(graph, root);
        svgNodes.each(function (u) {
            d3.select(this).classed(graph.node(u).nodeclass, true);
        });
        return svgNodes;
    });

    var svg = d3.select("svg");

    var layout = dagreD3.layout()
            .nodeSep(10)
            .edgeSep(10)
            .rankDir("LR");
    renderer.layout(layout);

    renderer.transition(transition);

      var layout = renderer.run(g, d3.select("svg g"));

    transition(d3.select("svg"))
            .attr("width", layout.graph().width + 40)
            .attr("height", layout.graph().height + 40);

    d3.select("svg")
            .call(d3.behavior.zoom().on("zoom", function () {
                var ev = d3.event;
                svg.select("g")
                        .attr("transform", "translate(" + ev.translate + ") scale(" + ev.scale + ")");
            }));

}

// Custom transition function
function transition(selection) {
    return selection.transition().duration(500);
}

</script>

<div id="middle">
    <h2><fmt:message key="event.flow"/></h2>

    <div id="workArea">
        <div id="flowdiv">
            <svg width=100% height=600>
                <g transform="translate(20, 20)"/>
            </svg>
        </div>
    </div>
</div>
</fmt:bundle>
