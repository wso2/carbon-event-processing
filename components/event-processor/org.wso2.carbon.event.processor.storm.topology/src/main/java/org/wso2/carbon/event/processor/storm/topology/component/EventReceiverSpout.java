package org.wso2.carbon.event.processor.storm.topology.component;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.Utils.AgentSession;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.authentication.AuthenticationHandler;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceClient;
import org.wso2.carbon.event.processor.storm.common.util.StormUtils;
import org.wso2.carbon.event.processor.storm.topology.util.SiddhiUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Receive events from CEP receivers through data bridge thrift receiver and pass through
 * the events as tuples to the connected component(i.e. Siddhi Bolt).
 */
public class EventReceiverSpout extends BaseRichSpout implements AgentCallback {
    private static transient Logger log = Logger.getLogger(EventReceiverSpout.class);
    /**
     * Listening port of the thrift receiver
     */
    int listeningPort;

    private String thisHostIp;

     /**
      * Siddhi stream definitions of all incoming streams. Required to declare output fields
      */
    private String[] incomingStreamDefinitions;

    /**
     * Stream IDs of incoming streams
     */
    private HashSet<String> incomingStreamIDs = new HashSet<String>();

    /**
     * Store received events until nextTuple is called. This list has to be synchronized since
     * this is filled by the receiver thread of data bridge and consumed by the nextTuple which
     * runs on the worker thread of spout.
     */
    private transient ConcurrentLinkedQueue<Event> storedEvents = null;

    private transient ThriftDataReceiver thriftDataReceiver = null;

    private SpoutOutputCollector spoutOutputCollector = null;

    private String executionPlanName = "Login_Info_Analyzer";

    private int tenantId = -1234;

    private int cepMangerPort;

    private String cepMangerHost;

    private String logPrefix;

    private int minListeningPort;
    private int maxListeningPort;

    /**
     * Receives events from the CEP Receiver through Thrift using data bridge and pass through the events
     * to a downstream component as tupels.
     * @param listeningPort - port of the Thrift server
     * @param incomingStreamDefinitions - Incoming Siddhi stream definitions
     */
    public EventReceiverSpout(int minListeningPort, int maxListeningPort, String keyStorePath, String cepManagerHost, int cepManagerPort, String[] incomingStreamDefinitions){
        this.minListeningPort = minListeningPort;
        this.maxListeningPort = maxListeningPort;
        this.cepMangerHost = cepManagerHost;
        this.cepMangerPort = cepManagerPort;
        this.incomingStreamDefinitions = incomingStreamDefinitions;
        logPrefix = "{" + executionPlanName + ":" + tenantId + "}";

        System.setProperty("Security.KeyStore.Location", keyStorePath); //"/home/sajith/wso2cep-4.0.0-SNAPSHOT/samples/producers/performance-test/src/main/resources/wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
    }

    @Override
    public void definedStream(org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition, int tenantId) {
        // Streams must be defined when submitting the topology. Storm does not support dynamic stream definitions.
        log.info(logPrefix + streamDefinition.getStreamId() + "Internal data bridge stream defined for tenant " + tenantId + " by CEP Receiver");
    }

    @Override
    public void removeStream(org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition, int tenantId) {
        // Streams can be only defined when submitting topology. Can't be removed or added dynamically.
        // Storm does not support dynamic additions or removal of streams
        log.warn(logPrefix + streamDefinition.getStreamId() + " data bridge stream removed for tenant " + tenantId + ". But it will not be removed from storm topology");
    }

    /**
     * Callback of data bridge to notify the receiving of events.
     */
    @Override
    public void receive(List<Event> events, Credentials credentials) {
        // Store events till next nextTuple is called.
        storedEvents.addAll(events);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // Declaring all incoming streams as output streams because this spouts role is to pass through all the incoming events as tuples.
        List<StreamDefinition> streamDefinitions = SiddhiUtils.toSiddhiStreamDefinitions(incomingStreamDefinitions);
        for (StreamDefinition siddhiStreamDefinition : streamDefinitions){
            Fields fields = new Fields(siddhiStreamDefinition.getAttributeNameArray());
            outputFieldsDeclarer.declareStream(siddhiStreamDefinition.getStreamId(), fields);

            incomingStreamIDs.add(siddhiStreamDefinition.getStreamId());
            log.info(logPrefix + "Declaring output fields for stream - " + siddhiStreamDefinition.getStreamId());
        }
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        this.storedEvents = new ConcurrentLinkedQueue<Event>();

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
            thriftDataReceiver = new ThriftDataReceiver(listeningPort, databridge);
            thisHostIp = HostAddressFinder.findAddress("localhost");
            thriftDataReceiver.start(thisHostIp);
            log.info(logPrefix + "EventReceiverSpout starting to listen for events");
            registerWithCepMangerService();
        } catch (SocketException e) {
            log.error(logPrefix + "Failed to start Thrift listener", e);
        } catch (DataBridgeException e) {
            log.error(logPrefix + "Failed to start Thrift listener", e);
        }
    }

    private void registerWithCepMangerService(){
        log.info("Registering Storm receiver for " + executionPlanName + ":" + tenantId + " at " + thisHostIp + ":" + listeningPort);
        ManagerServiceClient client = new ManagerServiceClient(cepMangerHost, cepMangerPort, null);
        client.registerStormReceiver(executionPlanName, tenantId, thisHostIp, listeningPort, 20);
    }

    @Override
    public void nextTuple() {
        Event event = storedEvents.poll();
        if (event != null){
            final String siddhiStreamName = SiddhiUtils.getSiddhiStreamName(event.getStreamId());

            if (incomingStreamIDs.contains(siddhiStreamName)){
                if (log.isDebugEnabled()){
                    log.debug(logPrefix + "Sending event : " + siddhiStreamName + "=>" + event.toString());
                }
                spoutOutputCollector.emit(siddhiStreamName, Arrays.asList(event.getPayloadData()));
            }else{
                log.warn(logPrefix + "Event received for unknown stream : " + siddhiStreamName);
            }
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
