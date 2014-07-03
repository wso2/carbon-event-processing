package org.wso2.carbon.event.processor.storm.topology.component;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.thrift.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceClient;
import org.wso2.carbon.event.processor.storm.common.client.ManagerServiceClientCallback;
import org.wso2.carbon.event.processor.storm.topology.util.SiddhiUtils;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.util.collection.Pair;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

;

/**
 * Publish events processed by Siddhi engine to CEP publisher
 */
public class EventPublisherBolt extends BaseBasicBolt implements ManagerServiceClientCallback {
    private transient Logger log = Logger.getLogger(EventPublisherBolt.class);
    /**
     * Exported stream IDs. Must declare output filed for each exported stream
     */
    private String[] exportedStreamIDs;
    /**
     * All stream definitions processed
     */
    private String[] streamDefinitions;
    /**
     * Quarries processed by Siddhi engine. Required to extract field definitions of implicitly declared stream
     * definitions
     */
    private String[] queries;
    /**
     * Keep track of relevant data bridge stream id for a given Siddhi stream id
     */
    private Map<String, org.wso2.carbon.databridge.commons.StreamDefinition> siddhiStreamIdToDataBridgeStream
            = new HashMap<String, org.wso2.carbon.databridge.commons.StreamDefinition>();

    private transient AsyncDataPublisher dataPublisher = null;

    private String executionPlanName = "Login_Info_Analyzer";

    private String logPrefix;

    private int tenantId = -1234;
    /**
     * CEP Manager service host
     */
    private String cepManagerHost;
    /**
     * CEP manager service port
     */
    private int cepManagerPort;

    SiddhiManager siddhiManager;

    public EventPublisherBolt(String cepManagerHost, int cepManagerPort, String trustStorePath, String[] streamDefinitions, String[] queries, String[] exportedStreamIDs){
        this.exportedStreamIDs = exportedStreamIDs;
        this.streamDefinitions = streamDefinitions;
        this.cepManagerHost = cepManagerHost;
        this.cepManagerPort = cepManagerPort;
        this.queries = queries;
        logPrefix = "{" + executionPlanName + ":" + tenantId +"}";
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);//"/home/sajith/wso2cep-4.0.0-SNAPSHOT/samples/producers/performance-test/src/main/resources/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
        if (siddhiManager == null){
            init(); // TODO : Understand why this init is required
        }

        if (dataPublisher != null){
            org.wso2.carbon.databridge.commons.StreamDefinition databridgeStream = siddhiStreamIdToDataBridgeStream.get(tuple.getSourceStreamId());

            if (databridgeStream != null){
                try {
                    if (log.isDebugEnabled()){
                        log.debug(logPrefix + "Event published to CEP Publisher =>" + tuple.toString());
                    }
                    dataPublisher.publish(databridgeStream.getName(), databridgeStream.getVersion(), null, null, tuple.getValues().toArray());
                } catch (AgentException e) {
                   log.error(logPrefix + "Error while publishing event to CEP publisher" , e);
                }
            }else{
                log.warn(logPrefix + "Tuple received for unknown stream " + tuple.getSourceStreamId() + ". Discarding event : " + tuple.toString());
            }
        }else{
            log.warn("Dropping the event since the data publisher is not yet initialized for " + executionPlanName + ":" + tenantId);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        init();
    }

    private void init(){
        // TODO : remove siddhi related stream definitions. Use only exported streams
        log = Logger.getLogger(EventPublisherBolt.class);
        siddhiManager = new SiddhiManager(new SiddhiConfiguration());

        if(streamDefinitions != null){
            for(String definition: streamDefinitions){
                if(definition.contains("define stream")){
                    siddhiManager.defineStream(definition);
                }else if(definition.contains("define partition")){
                    siddhiManager.definePartition(definition);
                }else{
                    throw new RuntimeException("Invalid definition : "+ definition);
                }
            }
        }

        if (queries != null){
            for(String query: queries){
                siddhiManager.addQuery(query);
            }
        }

        String thisHostIp = null;
        try {
           thisHostIp =  HostAddressFinder.findAddress("localhost");
        } catch (SocketException e) {
            log.error("Cannot find IP address of the host");
        }

        for (String streamDefinitionId : exportedStreamIDs){
            StreamDefinition siddhiStreamDefinition = siddhiManager.getStreamDefinition(streamDefinitionId);
            org.wso2.carbon.databridge.commons.StreamDefinition databridgeStreamDefinition = SiddhiUtils.toFlatDataBridgeStreamDefinition(siddhiStreamDefinition);
            siddhiStreamIdToDataBridgeStream.put(siddhiStreamDefinition.getStreamId(), databridgeStreamDefinition);
        }
        // Connecting to CEP manager service to get details of CEP publisher
        ManagerServiceClient client = new ManagerServiceClient(cepManagerHost, cepManagerPort, this);
        client.getCepPublisher(executionPlanName, tenantId, 30, thisHostIp);
    }

    @Override
    public void OnResponseReceived(Pair<String, Integer> endpoint) {
        synchronized (this){
            dataPublisher = new AsyncDataPublisher("tcp://" + endpoint.getOne() + ":" + endpoint.getTwo(), "admin", "admin");

            for (Map.Entry<String, org.wso2.carbon.databridge.commons.StreamDefinition> entry :  siddhiStreamIdToDataBridgeStream.entrySet()){
                dataPublisher.addStreamDefinition(entry.getValue());
                log.info(logPrefix + "Data bridge stream '" + entry.getValue().getStreamId() + "' defined for Siddhi stream '" + entry.getValue().getStreamId() + "'");
            }
            log.info(logPrefix + "EventPublisherBolt connecting to CEP Publisher at " + endpoint.getOne() + ":" + endpoint.getTwo());
        }
    }
}
