package org.wso2.carbon.event.processor.storm.internal;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.log4j.Logger;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.storm.exception.StormConfigurationException;
import org.wso2.carbon.event.processor.storm.internal.ds.StormProcessorValueHolder;
import org.wso2.carbon.event.processor.storm.internal.stream.EventProducer;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SiddhiBolt extends BaseBasicBolt implements EventProducer {
    private static transient Logger log = Logger.getLogger(SiddhiBolt.class);
    private transient SiddhiManager siddhiManager;
    private String queryRef;
    //TODO some of these members can be made final
    private int tenantId;
    private String importedTopLevelStreamId;
    private String inputSiddhiStreamId;
    private String[] outputSiddhiStreamIds;
    private BasicOutputCollector collector;
    private boolean useDefaultAsStreamName = false;
    private String componentID;

    public SiddhiBolt() throws StormConfigurationException {
        init();
    }

    public SiddhiBolt(String queryRef, String inputSiddhiStreamId, String[] outputSiddhiStreamIds, String importedTopLevelStreamId) throws StormConfigurationException {
        this.importedTopLevelStreamId = importedTopLevelStreamId;
        this.queryRef = queryRef;
        this.inputSiddhiStreamId = inputSiddhiStreamId;
        this.outputSiddhiStreamIds = outputSiddhiStreamIds;
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        init();
    }

    public String[] getOutputSiddhiStreamIds() {
        return outputSiddhiStreamIds;
    }

    public String getImportedTopLevelStreamId() {
        return importedTopLevelStreamId;
    }

    public String getInputSiddhiStreamId() {
        return inputSiddhiStreamId;
    }

    public void init() throws StormConfigurationException {
        SiddhiConfiguration configuration = new SiddhiConfiguration();
        SiddhiManager siddhiManager = new SiddhiManager(configuration);
        if (this.inputSiddhiStreamId != null) {
            StreamDefinition inputSiddhiStreamDef = StormProcessorValueHolder.getStormProcessorService().getSiddhiStreamDefinition(inputSiddhiStreamId, tenantId);
            if (inputSiddhiStreamDef != null) {
                siddhiManager.defineStream(inputSiddhiStreamDef);
            } else {
                throw new StormConfigurationException("Could not find stream definition for stream with id: " + inputSiddhiStreamId);
            }
        }
        if (this.queryRef != null) {
            Query query = StormProcessorValueHolder.getStormProcessorService().getQuery(queryRef, tenantId);
            if (query != null) {
                siddhiManager.addQuery(query);
            } else {
                throw new StormConfigurationException("Could not find query for reference: " + queryRef);
            }
        }
/*
        if (this.outputEventJunctions == null) {
            this.outputEventJunctions = new ArrayList<EventJunction>();
            for (String streamId : outputSiddhiStreamIds) {
                EventJunction eventJunction = StormProcessorValueHolder.getStormProcessorService().getEventJunction(streamId, tenantId);
                if (eventJunction != null) {
                    outputEventJunctions.add(eventJunction);
                } else {
                    log.warn("Could not find event junction for stream :" + streamId + " [" + this.componentID + "]");
                }
            }
        }
*/

        this.siddhiManager = siddhiManager;
        for (final String streamId : outputSiddhiStreamIds) {
            siddhiManager.addCallback(streamId, new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    for (Event event : events) {
                        List<Object> asList = Arrays.asList(event.getData());
                        if (log.isDebugEnabled()) {
                            log.debug(componentID + ">Siddhi: Emit Event " + event);
                        }
                        if (useDefaultAsStreamName) {
                            getCollector().emit(asList);
                        } else {
                            getCollector().emit(streamId, asList);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        this.componentID = context.getThisComponentId();
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        try {
            String streamID = tuple.getSourceStreamId();

            this.collector = collector;
            if (siddhiManager == null) {
                //Bolt get saved and reloaded, this handle that condition
                try {
                    init();
                } catch (StormConfigurationException e) {
                    log.error("Error when trying to re-initialize this bolt:" + e.getMessage(), e);
                }
            }

            InputHandler inputHandler = siddhiManager.getInputHandler(streamID);
            if (inputHandler == null) {
                //If we cannot find a stream with the given name, we will try using the component ID.
                //Since most Storm programs are written only using component IDs and not streams, this
                //code added to handle this case
                inputHandler = siddhiManager.getInputHandler(tuple.getSourceComponent());
            }
            if (inputHandler != null) {
                if (log.isDebugEnabled()) {
                    log.debug(componentID + ">Siddhi: Received Event " + tuple);
                }
                inputHandler.send(tuple.getValues().toArray());
            } else {
                throw new RuntimeException("Input handler for stream " + streamID + " not found");
            }

        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        if (siddhiManager == null) {
            try {
                init();
            } catch (StormConfigurationException e) {
                log.error("Error trying to re-initialize this bolt: " + e.getMessage(), e);
            }
        }

        boolean useDefault = (outputSiddhiStreamIds.length == 1) && useDefaultAsStreamName;

        for (String streamId : outputSiddhiStreamIds) {
            StreamDefinition streamDefinition = siddhiManager.getStreamDefinition(streamId);
            if (streamDefinition == null) {
                throw new RuntimeException("Cannot find stream " + streamId + " " + this);
            }
            List<Attribute> attributeList = streamDefinition.getAttributeList();
            List<String> list = new ArrayList<String>();
            for (Attribute attribute : attributeList) {
                list.add(attribute.getName());
            }
            Fields feilds = new Fields(list);
            if (useDefault) {
                //this is so that most common Storm topologies would work with
                //Siddhi bolt.  See setUseDefaultAsStreamName(..)
                log.info(componentID + ">Siddhi: declaring stream as default");
                declarer.declare(feilds);
            } else {
                declarer.declareStream(streamId, feilds);
            }

        }
    }

    public BasicOutputCollector getCollector() {
        return collector;
    }

    public boolean isUseDefaultAsStreamName() {
        return useDefaultAsStreamName;
    }

    /**
     * When this is true and there is only one stream to expose, Siddhi bolt will export the resulting stream as default.
     * This set to true by default.
     *
     * @param useDefaultAsStreamName set to {@code true} to use default stream.
     */
    public void setUseDefaultAsStreamName(boolean useDefaultAsStreamName) {
        this.useDefaultAsStreamName = useDefaultAsStreamName;
    }

    @Override
    public Object getOwner() {
        return componentID;
    }
}
