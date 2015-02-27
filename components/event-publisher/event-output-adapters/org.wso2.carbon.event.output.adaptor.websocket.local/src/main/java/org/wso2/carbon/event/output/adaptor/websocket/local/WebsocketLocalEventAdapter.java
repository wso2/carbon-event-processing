/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adaptor.websocket.local;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.wso2.carbon.event.publisher.core.adapter.AbstractOutputEventAdapter;
import org.wso2.carbon.event.publisher.core.MessageType;
import org.wso2.carbon.event.publisher.core.Property;
import org.wso2.carbon.event.publisher.core.config.OutputAdaptorConfiguration;
import org.wso2.carbon.event.publisher.core.exception.TestConnectionUnavailableException;
import org.wso2.carbon.event.output.adaptor.websocket.local.internal.WebsocketLocalOutputCallbackRegisterServiceInternal;
import org.wso2.carbon.event.output.adaptor.websocket.local.internal.ds.WebsocketLocalEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adaptor.websocket.local.internal.util.WebsocketLocalEventAdaptorConstants;

import javax.websocket.*;

public class WebsocketLocalEventAdapter extends AbstractOutputEventAdapter {

    private List<Property> outputMessageProps;
    
    private List<String> supportOutputMessageTypes;

	private static WebsocketLocalEventAdapter instance = new WebsocketLocalEventAdapter();

    public static WebsocketLocalEventAdapter getInstance() {
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
	protected void publish(
			Object message,
			OutputAdaptorConfiguration outputAdaptorConfiguration,
			int tenantId) {
        String topic = outputAdaptorConfiguration.getEndpointAdaptorProperties().get(WebsocketLocalEventAdaptorConstants.ADAPTER_TOPIC);
        WebsocketLocalOutputCallbackRegisterServiceInternal websocketLocalOutputCallbackRegisterServiceInternal = WebsocketLocalEventAdaptorServiceInternalValueHolder.getWebsocketLocalOutputCallbackRegisterServiceInternal();
        CopyOnWriteArrayList<Session> sessions = websocketLocalOutputCallbackRegisterServiceInternal.getSessions(tenantId, outputAdaptorConfiguration.getAdaptorName(), topic);
        if (sessions != null){
            for (Session session : sessions){
                synchronized (session){
                    session.getAsyncRemote().sendText(message.toString());  //this method call was synchronized to fix CEP-996
                }
            }
        }
    }

	@Override
	public void testConnection(
			OutputAdaptorConfiguration outputAdaptorConfiguration,
			int tenantId) {
        throw new TestConnectionUnavailableException("not-available");
	}

    @Override
    public void removeConnectionInfo(
            OutputAdaptorConfiguration outputAdaptorConfiguration, int tenantId) {
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
