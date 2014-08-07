/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.processor.core.internal.storm;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiverFactory;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.internal.listener.SiddhiOutputStreamListener;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceClient;
import org.wso2.carbon.event.processor.storm.common.helper.StormDeploymentConfiguration;
import org.wso2.carbon.event.processor.storm.common.util.StormUtils;
import org.wso2.carbon.user.api.UserStoreException;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

/**
 * Receives events from the Event publisher bolt running on storm. There will be one SindhiStormOutputEventListener instance
 * per execution plan per tenant (all exported streams of execution plan are handled form a single instance). When events are
 * received from storm, the event  will be directed to the relevant output stream listener depending on the stream to forward
 * the event to the relevant output adaptor for the stream.
 */
public class SiddhiStormOutputEventListener implements AgentCallback {
    private static Logger log = Logger.getLogger(SiddhiStormOutputEventListener.class);
    private ExecutionPlanConfiguration executionPlanConfiguration;
    private ThriftDataReceiver thriftDataReceiver;
    private int listeningPort;
    private int tenantId;
    private String thisHostIp;
    private int cepMangerPort = StormDeploymentConfiguration.getCepManagerPort();
    private String cepMangerHost = StormDeploymentConfiguration.getCepManagerHost();
    private HashMap<String, SiddhiOutputStreamListener> streamNameToOutputStreamListener = new HashMap<String, SiddhiOutputStreamListener>();
    private static final String DEFAULT_STREAM_VERSION = "1.0.0";
    private int minListeningPort = StormDeploymentConfiguration.getMinListingPort();
    private int maxListeningPort = StormDeploymentConfiguration.getMaxListeningPort();

    public SiddhiStormOutputEventListener(ExecutionPlanConfiguration executionPlanConfiguration, int tenantId){
        this.executionPlanConfiguration = executionPlanConfiguration;
        this.tenantId = tenantId;
        init();
    }

    public void registerOutputStreamListener(String siddhiStreamName, SiddhiOutputStreamListener outputStreamListener){
        log.info("Registering Output stream listener for Siddhi stream " + siddhiStreamName);
        streamNameToOutputStreamListener.put(siddhiStreamName + ":" + DEFAULT_STREAM_VERSION, outputStreamListener);
    }

    @Override
    public void definedStream(StreamDefinition streamDefinition, int tenantId) {
        log.info(streamDefinition.getStreamId() + " Internal data bridge stream defined for tenant " + tenantId + " to communicate with storm.");
    }

    @Override
    public void removeStream(StreamDefinition streamDefinition, int tenantId) {
        // This will never happen since storm bolt does not remove streams once defined.
        log.warn(streamDefinition.getStreamId() + " data bridge stream removed for tenant " + tenantId);
    }

    @Override
    public void receive(List<Event> events, Credentials credentials) {
        for (Event event: events){
            SiddhiOutputStreamListener outputStreamListener = streamNameToOutputStreamListener.get(event.getStreamId());

            if (outputStreamListener != null){
                outputStreamListener.sendEventData(event.getPayloadData());
            }else{
                log.error("Cannot find output event listener for stream " + event.getStreamId() + " in execution plan " + executionPlanConfiguration.getName()
                        + " of tenant " + tenantId);
            }
        }
    }

    private void registerWithCepMangerService(){
        log.info("Registering CEP Publisher for " + executionPlanConfiguration.getName() + ":" + tenantId + " at " + thisHostIp + ":" + listeningPort);
        ManagerServiceClient client = new ManagerServiceClient(cepMangerHost, cepMangerPort, null);
        client.registerCepPublisher(executionPlanConfiguration.getName(), tenantId, thisHostIp, listeningPort, StormDeploymentConfiguration.getReconnectInterval());
    }

    private void init(){
        log.info("[CEP Publisher]Initializing Storm output event listener for execution plan '" + executionPlanConfiguration.getName() + "'"
                 + "(TenantID=" + tenantId + ")");

        DataBridge databridge = new DataBridge(new AuthenticationHandler() {
            @Override
            public boolean authenticate(String userName, String password) { return true;}
            @Override
            public String getTenantDomain(String userName) { return "admin";}
            @Override
            public int getTenantId(String s) throws UserStoreException { return -1234;}
            @Override
            public void initContext(AgentSession agentSession) {}
            @Override
            public void destroyContext(AgentSession agentSession) {}
        }, new InMemoryStreamDefinitionStore());

        databridge.subscribe(this);
        try {
            selectPort();
            thriftDataReceiver = new ThriftDataReceiverFactory().createAgentServer(listeningPort, databridge);
            thisHostIp = HostAddressFinder.findAddress("localhost");
            thriftDataReceiver.start(thisHostIp);
            registerWithCepMangerService();
        } catch (SocketException e) {
            log.error("Failed to start Thrift listener for execution plan :" + executionPlanConfiguration.getName(), e);
        } catch (DataBridgeException e) {
            log.error("Failed to start Thrift listener for execution plan :" + executionPlanConfiguration.getName(), e);
        }
    }

    private void selectPort(){
        for (int i = minListeningPort; i <= maxListeningPort; i++){
            if (!StormUtils.isPortUsed(i)){
                listeningPort = i;
                break;
            }
        }
    }


}
