package org.wso2.carbon.event.builder.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.builder.stub.EventBuilderAdminServiceStub;
import org.wso2.carbon.event.input.adaptor.manager.stub.InputEventAdaptorManagerAdminServiceStub;
import org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.manager.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class EventBuilderUIUtils {
    public static EventBuilderAdminServiceStub getEventBuilderAdminService(ServletConfig config,
                                                                           HttpSession session,
                                                                           HttpServletRequest request)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "EventBuilderAdminService.EventBuilderAdminServiceHttpsSoap12Endpoint";
        EventBuilderAdminServiceStub stub = new EventBuilderAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    public static EventStreamAdminServiceStub getEventStreamAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "EventStreamAdminService.EventStreamAdminServiceHttpsSoap12Endpoint";
        EventStreamAdminServiceStub stub = new EventStreamAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    public static InputEventAdaptorManagerAdminServiceStub getInputEventAdaptorManagerAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "InputEventAdaptorManagerAdminService.InputEventAdaptorManagerAdminServiceHttpsSoap12Endpoint";
        InputEventAdaptorManagerAdminServiceStub stub = new InputEventAdaptorManagerAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    public static List<String> getAttributeListWithPrefix(EventStreamDefinitionDto streamDefinitionDto) {
        List<String> attributeList = new ArrayList<String>();

        if (streamDefinitionDto.getMetaData() != null) {
            for (EventStreamAttributeDto metaData : streamDefinitionDto.getMetaData()) {
                attributeList.add("meta_" + metaData.getAttributeName() + " " + metaData.getAttributeType());
            }
        }

        if (streamDefinitionDto.getCorrelationData() != null) {
            for (EventStreamAttributeDto correlationData : streamDefinitionDto.getCorrelationData()) {
                attributeList.add("correlation_" + correlationData.getAttributeName() + " " + correlationData.getAttributeType());
            }
        }

        if (streamDefinitionDto.getPayloadData() != null) {
            for (EventStreamAttributeDto payloadData : streamDefinitionDto.getPayloadData()) {
                attributeList.add(payloadData.getAttributeName() + " " + payloadData.getAttributeType());
            }
        }
        return attributeList;
    }

}
