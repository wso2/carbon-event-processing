<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.event.stream.manager.ui.i18n.Resources">

    <carbon:breadcrumb
            label="eventstream.list"
            resourceBundle="org.wso2.carbon.event.stream.manager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <%
        String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
        String loadingCondition = "importedStreams";
    %>

    <div id="middle">
        <h2><fmt:message key="title.event.out.flow"/> (<a
                href="eventStreamDetails.jsp?ordinal=1&eventStreamWithVersion=<%=eventStreamWithVersion%>"><%=eventStreamWithVersion%>
        </a>) </h2>

        <div id="workArea">

            <table style="width:100%" id="outFlowDetails" class="styledLeft">

                <tbody>

                <tr>
                    <td class="formRaw">
                        <p><b><fmt:message key="external.event.outflows"/></b></p>
                        <jsp:include page="../eventformatter/event_formatter_outFlows.jsp"
                                     flush="true">
                            <jsp:param name="eventStreamWithVersion"
                                       value="<%=eventStreamWithVersion%>"/>
                        </jsp:include>
                        <p><b><fmt:message key="internal.event.outflows"/></b></p>
                        <jsp:include page="../eventprocessor/inner_index.jsp" flush="true">
                            <jsp:param name="eventStreamWithVersion"
                                       value="<%=eventStreamWithVersion%>"/>
                            <jsp:param name="loadingCondition" value="<%=loadingCondition%>"/>
                        </jsp:include>

                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

</fmt:bundle>
