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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.output.adaptor.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="add"
            resourceBundle="org.wso2.carbon.event.output.adaptor.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript"
            src="../outputeventadaptormanager/js/create_event_adaptor_helper.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>

    <div id="middle">
        <h2><fmt:message key="event.adaptor.create"/></h2>

        <div id="workArea">

            <form name="inputForm" action="index.jsp?ordinal=1" method="post" id="addEvent">
                <table style="width:100%" id="eventAdd" class="styledLeft">
                    <thead>
                    <tr>
                        <th><fmt:message key="event.adaptor.details"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRaw">
                            <%@include file="inner_event_adaptor_ui.jsp" %>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="button" value="<fmt:message key="add.event.adaptor"/>"
                                   onclick="addEvent(document.getElementById('addEvent'))"/>
                            <input type="button" value="<fmt:message key="test.connection"/>"
                                   onclick="testConnection(document.getElementById('addEvent'))"/>
                        </td>
                    </tr>
                    </tbody>
                </table>


            </form>
        </div>
    </div>
</fmt:bundle>
