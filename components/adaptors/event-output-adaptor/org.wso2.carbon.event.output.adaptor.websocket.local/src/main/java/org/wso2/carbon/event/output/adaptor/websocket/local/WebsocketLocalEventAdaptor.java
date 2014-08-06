package org.wso2.carbon.event.output.adaptor.websocket.local;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.wso2.carbon.event.output.adaptor.core.AbstractOutputEventAdaptor;
import org.wso2.carbon.event.output.adaptor.core.MessageType;
import org.wso2.carbon.event.output.adaptor.core.Property;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.output.adaptor.core.message.config.OutputEventAdaptorMessageConfiguration;
import org.wso2.carbon.event.output.adaptor.websocket.local.internal.WebsocketLocalOutputCallbackRegisterServiceInternal;
import org.wso2.carbon.event.output.adaptor.websocket.local.internal.ds.WebsocketLocalEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adaptor.websocket.local.internal.util.WebsocketLocalEventAdaptorConstants;

import javax.websocket.*;

public class WebsocketLocalEventAdaptor extends AbstractOutputEventAdaptor{

    private List<Property> outputMessageProps;
    
    private List<String> supportOutputMessageTypes;

	private static WebsocketLocalEventAdaptor instance = new WebsocketLocalEventAdaptor();

    public static WebsocketLocalEventAdaptor getInstance() {
        return instance;
    }

	@Override
	protected String getName() {
		return WebsocketLocalEventAdaptorConstants.ADAPTER_TYPE_WEBSOCKET_LOCAL;
	}

	@Override
	protected List<String> getSupportedOutputMessageTypes() {
		return supportOutputMessageTypes;
	}

	@Override
	protected void init() {
		populateAdapterMessageProps();
        this.supportOutputMessageTypes = new ArrayList<String>();
        this.supportOutputMessageTypes.add(MessageType.XML);
        this.supportOutputMessageTypes.add(MessageType.JSON);
        this.supportOutputMessageTypes.add(MessageType.TEXT);
	}

	@Override
	protected List<Property> getOutputAdaptorProperties() {
		return null;
	}

	@Override
	protected List<Property> getOutputMessageProperties() {
		return outputMessageProps;
	}

	@Override
	protected void publish(
			OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
			Object message,
			OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
			int tenantId) {
        String topic = outputEventAdaptorMessageConfiguration.getOutputMessageProperties().get(WebsocketLocalEventAdaptorConstants.ADAPTER_TOPIC);
        WebsocketLocalOutputCallbackRegisterServiceInternal websocketLocalOutputCallbackRegisterServiceInternal = WebsocketLocalEventAdaptorServiceInternalValueHolder.getWebsocketLocalOutputCallbackRegisterServiceInternal();
        CopyOnWriteArrayList<Session> sessions = websocketLocalOutputCallbackRegisterServiceInternal.getSessions(tenantId, outputEventAdaptorConfiguration.getName(), topic);
        if (sessions != null){
            for (Session session : sessions){
                session.getAsyncRemote().sendText(message.toString());
            }
        }
    }

	@Override
	public void testConnection(
			OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
			int tenantId) {
        throw new TestConnectionUnavailableException("not-available");
	}

    @Override
    public void removeConnectionInfo(
            OutputEventAdaptorMessageConfiguration outputEventAdaptorMessageConfiguration,
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration, int tenantId) {
        //not required
    }
	
    private void populateAdapterMessageProps() {
        this.outputMessageProps = new ArrayList<Property>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.event.output.adaptor.websocket.local.i18n.Resources", Locale.getDefault());

        Property topicProp = new Property(WebsocketLocalEventAdaptorConstants.ADAPTER_TOPIC);
        topicProp.setDisplayName(resourceBundle.getString(WebsocketLocalEventAdaptorConstants.ADAPTER_TOPIC));
        topicProp.setHint(resourceBundle.getString(WebsocketLocalEventAdaptorConstants.ADAPTER_TOPIC_HINT));
        topicProp.setRequired(true);

        this.outputMessageProps.add(topicProp);
    }

}
