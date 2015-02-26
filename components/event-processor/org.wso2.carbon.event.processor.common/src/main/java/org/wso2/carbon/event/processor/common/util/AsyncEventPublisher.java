package org.wso2.carbon.event.processor.common.util;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.event.processor.common.storm.config.StormDeploymentConfig;
import org.wso2.carbon.event.processor.common.storm.manager.service.StormManagerService;
import org.wso2.carbon.event.processor.common.transport.client.TCPEventPublisher;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.io.IOException;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Sending events to a remote endpoint asynchronously.
 */
public class AsyncEventPublisher implements EventHandler<AsynchronousEventBuffer.DataHolder> {
    public enum DestinationType{STORM_RECEIVER, CEP_PUBLISHER}

    private transient Logger log = Logger.getLogger(AsyncEventPublisher.class);
    private String logPrefix;

    private String destinationTypeString;
    private String publisherTypeString;
    private DestinationType destinationType;

    private Set<StreamDefinition> streams;
    private String executionPlanName;
    private int tenantId;
    private String thisHostIp;
    private String managerServiceHost;
    private int managerServicePort;
    private StormDeploymentConfig stormDeploymentConfig;

    private TCPEventPublisher tcpEventPublisher = null;
    private EndpointConnectionCreator endpointConnectionCreator;
    private AsynchronousEventBuffer eventSendBuffer = new AsynchronousEventBuffer<Object[]>(1024, this);

    private boolean shutdown = false;

    public AsyncEventPublisher(DestinationType destinationType, Set<StreamDefinition> streams,
                               String managerServiceIp, int managerServicePort, String executionPlanName, int tenantId, StormDeploymentConfig stormDeploymentConfig){
        this.destinationType = destinationType;
        this.streams = streams;
        this.executionPlanName = executionPlanName;
        this.tenantId = tenantId;
        this.managerServiceHost = managerServiceIp;
        this.managerServicePort = managerServicePort;
        this.stormDeploymentConfig = stormDeploymentConfig;
        this.endpointConnectionCreator = new EndpointConnectionCreator();
        this.destinationTypeString = (destinationType == DestinationType.STORM_RECEIVER) ? "Storm Receiver" : "CEP Publisher";
        this.publisherTypeString = (destinationType == DestinationType.STORM_RECEIVER) ? "CEP Receiver" : "Publisher Bolt";
        this.logPrefix = "[" + publisherTypeString + "| ExecPlan:" + executionPlanName + ", TenantID:" + tenantId + "]";
    }

    /**
     * Initialize and try to make a connection with remote endpoint
     * @param sync is this is set to true returns only after obtaining a connection to the remote endpoint. Otherwise initialization happens on a newly spawned
     *             thread and this method returns immediately.
     */
    public void initializeConnection(boolean sync){
        try {
            this.thisHostIp = Utils.findAddress("localhost");

            if (sync){
                endpointConnectionCreator.establishConnection();
            }else{
                Thread thread = new Thread(endpointConnectionCreator);
                thread.start();
            }
        } catch (SocketException e) {
            log.error(logPrefix + "Error while trying to obtain this host IP address", e);
        }
    }

    /**
     * Add event to the outbound event buffer and this call will return. Event will be sent asynchronously by disruptor consumer thread
     * via AsyncEventPublisher#onEvent.
     * @param eventData
     * @param streamId
     */
    public void sendEvent(Object[] eventData, String streamId){
        eventSendBuffer.addEvent(eventData, streamId);
    }

    /**
     * Callback from disruptor for the consumer to consume data. This is where events are actually dispatched to the remote end.
     * If an exception occurs when trying send data it will keep trying to send for ever until succeeds. Returns only
     * after sending the event.
     * @param dataHolder
     * @param sequence
     * @param endOfBatch
     */
    @Override
    public void onEvent(AsynchronousEventBuffer.DataHolder dataHolder, long sequence, boolean endOfBatch){
        while (tcpEventPublisher == null){
            log.info(logPrefix + "Can't send event.TCP event publisher not initialized. Waiting " + stormDeploymentConfig.getTransportReconnectInterval() + "s");
            try {
                synchronized (this){
                    if (shutdown){
                        log.info(logPrefix + "Aborting retry to send events. AsyncEventPublisher shut down.");
                        return;
                    }
                }
                Thread.sleep(stormDeploymentConfig.getTransportReconnectInterval());
            } catch (InterruptedException e) {
                //ignore
            }
        }

        // TODO : comment on message lost of the last batch
        try{
            tcpEventPublisher.sendEvent(dataHolder.getStreamId(), (Object[])dataHolder.getData(), endOfBatch);
        } catch (IOException e) {
            log.error(logPrefix + "Error while trying to send event to " + destinationTypeString + " at " + tcpEventPublisher.getHostUrl(), e);
            reconnect();
            onEvent(dataHolder, sequence, endOfBatch);
        }
    }

    /**
     * First tires to reconnect to the already obtained end point. If failed re-initialize the connection.
     */
    private void reconnect(){
        String destinationHostPort = tcpEventPublisher.getHostUrl();
        resetTCPEventPublisher();

        // Retrying to connect to the existing endpoint.
        tcpEventPublisher = endpointConnectionCreator.connectToEndpoint(destinationHostPort, 3);
        if (tcpEventPublisher == null){
            log.error(logPrefix + "Failed to connect to existing " + destinationTypeString + " at " + destinationHostPort + ". Reinitializing");
            initializeConnection(true);
        }
    }

    private void resetTCPEventPublisher(){
        tcpEventPublisher.terminate();
        tcpEventPublisher = null;
    }

    @Override
    protected void finalize(){
        if (tcpEventPublisher != null){
            tcpEventPublisher.terminate();
        }
    }

    public void shutdown(){
        synchronized (this){
            shutdown = true;
        }
        eventSendBuffer.terminate();
    }

    /**
     * Creates connection to remote endpoint. First, talk to manager service to retrieve endpoint for given execution plan and tenant.
     * And then creates a connection to connect to remote endpoint.
     */
    class EndpointConnectionCreator implements Runnable {
        /**
         * Get the IP and the port of CEP Publisher/ Storm Receive by talking to Storm manager service.
         * Returns only after retrieving information from manager service. In case of a failure keep trying
         * to connect to manager service.
         * @return endpoint Host and port in <ip>:<port> format
         */
        public String getEndpointFromManagerService(){
            String endpointHostPort = null;
            do {
                TTransport transport = null;
                try {
                    transport = new TSocket(managerServiceHost, managerServicePort);
                    TProtocol protocol = new TBinaryProtocol(transport);
                    transport.open();
                    StormManagerService.Client client = new StormManagerService.Client(protocol);

                    if (destinationType == DestinationType.CEP_PUBLISHER){
                        endpointHostPort = client.getCEPPublisher(tenantId, executionPlanName, thisHostIp);
                    }else{
                        endpointHostPort = client.getStormReceiver(tenantId, executionPlanName, thisHostIp);
                    }
                    log.info(logPrefix + "Retrieved " + destinationTypeString + " at " + endpointHostPort +" from storm manager service");
                } catch (Exception e) {
                    log.error(logPrefix + "Error while trying retrieve information from storm manager service", e);
                } finally {
                    if (transport != null) {
                        transport.close();
                    }
                }

                synchronized (AsyncEventPublisher.this){
                    if (shutdown){
                        log.info(logPrefix + "Stopping attempting to connect to Storm manager service.Async event publisher is shutdown");
                        return null;
                    }
                }

                if (endpointHostPort == null){
                    try {
                        log.info(logPrefix + "Retrying to retrieve endpoint from manager service in " + stormDeploymentConfig.getManagementReconnectInterval()
                                +  "ms to get a " + destinationTypeString);
                        Thread.sleep(stormDeploymentConfig.getManagementReconnectInterval());
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                }
            } while (endpointHostPort == null);

            return endpointHostPort;
        }

        /**
         * Connect to a given endpoint (i.e. CEP publisher or storm receiver). In case of failure retry to connect. Returns only
         * after connecting to the endpoint or after reaching maximum attempts.
         * @param endpointHostPort Destination Ip and port in <ip>:<port> format
         * @param retryAttempts maximum number of retry attempts. 0 means retry for ever.
         * @return Returns TCPEvent publisher to talk to endpoint or null if reaches maximum number of attempts without succeeding
         */
        public TCPEventPublisher connectToEndpoint(String endpointHostPort, int retryAttempts){
            TCPEventPublisher tcpEventPublisher = null;
            int attemptCount = 0;

            do {
                try {
                    tcpEventPublisher = new TCPEventPublisher(endpointHostPort, true);
                    StringBuilder streamsIDs = new StringBuilder();

                    for (StreamDefinition siddhiStreamDefinition : streams){
                        tcpEventPublisher.addStreamDefinition(siddhiStreamDefinition);
                        streamsIDs.append(siddhiStreamDefinition.getId() + ",");
                    }

                    log.info(logPrefix + "Connected to " + destinationTypeString + " at " + endpointHostPort + " for the Stream(s) " + streamsIDs.toString());
                } catch (IOException e) {
                    log.error(logPrefix + "Error while trying to connect to " + destinationTypeString + " at " + endpointHostPort, e);
                }

                synchronized (AsyncEventPublisher.this){
                    if (shutdown){
                        log.info(logPrefix + "Stopping attempting to connect to endpoint " + endpointHostPort + ". Async event publisher is shutdown");
                        return null;
                    }
                }

                if (tcpEventPublisher == null){
                    ++attemptCount;
                    if (retryAttempts > 0 &&  (attemptCount > retryAttempts)){
                        return null;
                    }
                    try {
                        log.info(logPrefix + "Retrying(" + attemptCount + ") to connect to " + destinationTypeString + " at " + endpointHostPort + " in "
                                + stormDeploymentConfig.getTransportReconnectInterval() + "ms");
                        Thread.sleep(stormDeploymentConfig.getTransportReconnectInterval());
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                }
            } while (tcpEventPublisher == null);

            return tcpEventPublisher;
        }

        /**
         * First connect to the manager service and retrieve endpoint ip and port. Then connect to the endpoint.
         * Returns only after completing these tasks. Keeps trying forever until succeeds.
         */
        public void establishConnection(){
            log.info(logPrefix + "Requesting a " + destinationTypeString + " for " + thisHostIp);
            String endpointHostPort = getEndpointFromManagerService();

            if (endpointHostPort != null){
                tcpEventPublisher = connectToEndpoint(endpointHostPort, 0);
            }
        }

        @Override
        public void run() {
            establishConnection();
        }
    }
}

/**
 * Store events in a disruptor
 * @param <Type> Type of data to be stored in buffer.
 */
class AsynchronousEventBuffer<Type> {
    private Disruptor<DataHolder> disruptor;
    private RingBuffer<DataHolder> ringBuffer;

    /**
     * Creates a AsynchronousEventBuffer instance
     * @param bufferSize size of the buffer
     * @param publishHandler Instance of publish handler which is responsible for consuming events in the buffer
     */
    public AsynchronousEventBuffer(int bufferSize, EventHandler publishHandler){
        this.disruptor = new Disruptor<DataHolder>(new EventFactory<DataHolder>() {
            @Override
            public DataHolder newInstance() {
                return new DataHolder();
            }
        }, bufferSize, Executors.newSingleThreadExecutor());

        this.ringBuffer = disruptor.getRingBuffer();

        this.disruptor.handleEventsWith(publishHandler);

        disruptor.start();
    }

    public void addEvent(Type data, String streamId){
        long sequenceNo = ringBuffer.next();
        try {
            DataHolder existingHolder = ringBuffer.get(sequenceNo);
            existingHolder.setData(data);
            existingHolder.setStreamId(streamId);
        } finally {
            ringBuffer.publish(sequenceNo);
        }
    }

    public void terminate(){
        disruptor.halt();
    }

    class DataHolder{
        Type data;

        String streamId;

        public void setData(Type data){
            this.data = data;
        }

        public Type getData(){
            return data;
        }

        public void setStreamId(String streamId){ this.streamId = streamId; }

        public String getStreamId(){ return streamId; }
    }
}

