package org.wso2.carbon.event.processor.storm.topology.component;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Bold which runs Siddhi engine
 */

public class SiddhiBolt extends BaseBasicBolt{
	private transient Logger log = Logger.getLogger(SiddhiBolt.class); 
	private transient SiddhiManager siddhiManager;

    /**
     * Exported stream IDs. Must declare output filed for each exported stream
     */
    private String[] exportedStreamIds;
    /**
     * All stream definitions and partition definitions(if any)
     */
	private String[] definitions;
    /**
     * Queries to be executed in Siddhi.
     */
	private String[] queries;

    private BasicOutputCollector collector;

    /**
     * Bolt which runs the Siddhi engine.
     * @param definitions - All stream and partition definitions
     * @param queries - Siddhi queries
     * @param exportedSiddhiStreamIds - Exported stream names
     */
    public SiddhiBolt(String[] definitions, String[] queries, String[] exportedSiddhiStreamIds){
        this.definitions = definitions;
        this.queries = queries;
        this.exportedStreamIds = exportedSiddhiStreamIds;
        init();
    }

    /**
     * Bolt get saved and reloaded, this to redo the configurations.
     */
    private void init(){
        siddhiManager = new SiddhiManager(new SiddhiConfiguration());
        log = Logger.getLogger(SiddhiBolt.class);

        if(definitions != null){
            for(String definition: definitions){

                if(definition.contains("define stream")){
        			siddhiManager.defineStream(definition);
        		}else if(definition.contains("define partition")){
            		siddhiManager.definePartition(definition);
        		}else{
        			throw new RuntimeException("Invalid definition : " + definition);
        		}
        	}
        }

        if (queries != null){

            for(String query: queries){
        		siddhiManager.addQuery(query);
        	}
        }

        for(final String streamId:  exportedStreamIds){
            log.info("Adding callback for stream - " +  streamId);
			siddhiManager.addCallback(streamId, new StreamCallback() {
                @Override
                public void receive(Event[] events) {

                    for (Event event : events) {
                        collector.emit(event.getStreamId(), Arrays.asList(event.getData()));
                        if (log.isDebugEnabled()){
                            log.debug("Sending Processed event : " + event.getStreamId() + "=>" + event.toString());
                        }
                    }
                }
            });
		}
	}

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
	}

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
        if(siddhiManager == null){
            init();
        }

        try {
            this.collector = collector;
            InputHandler inputHandler = siddhiManager.getInputHandler(tuple.getSourceStreamId());

            if(inputHandler != null){
				inputHandler.send(tuple.getValues().toArray());	
			}else{
				log.warn("Event received for unknown stream " + tuple.getSourceStreamId() + ". Discarding the event :" + tuple.toString());
			}
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		if(siddhiManager == null){
			init();
		}

        // Declaring output fileds for each exported stream ID
		for(String streamId:  exportedStreamIds){
			StreamDefinition streamDefinition = siddhiManager.getStreamDefinition(streamId);

            if(streamDefinition == null){
				throw new RuntimeException("Cannot find exported stream - " + streamId);
			}
			List<String> list = new ArrayList<String>();

            for(Attribute attribute: streamDefinition.getAttributeList()){
				list.add(attribute.getName());
			}
            Fields fields = new Fields(list);
		    declarer.declareStream(streamId, fields);
            log.info("Declaring output field for stream -" + streamId);
		}
	}
}
