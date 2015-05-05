<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.wso2.carbon.siddhi.tryit.ui.SiddhiTryItClient" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>

<%


    String executionplan = request.getParameter("executionplan");
    String eventStream = request.getParameter("eventstream");

    SiddhiTryItClient siddhiTryItClientObject = new SiddhiTryItClient();

    Map<String, StringBuilder> map = siddhiTryItClientObject.processData(executionplan,eventStream);
    //System.out.println("map.values.tostring: "+map.values().toString());
//    for(int i=0;i<map.size();i++){
//        System.out.println("stream result " + i+":"+ map.values().toString());
//    }

    String resultData = map.values().toString();
    //System.out.println("map values jsp: " + resultData);
%>
<%=resultData%>










































































































































































































































































