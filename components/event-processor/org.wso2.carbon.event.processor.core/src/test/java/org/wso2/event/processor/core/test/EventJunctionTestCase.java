package org.wso2.event.processor.core.test;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;

import java.util.ArrayList;
import java.util.List;

public class EventJunctionTestCase {
    private static final Log log = LogFactory.getLog(EventJunctionTestCase.class);


    @Test
    public void testStreamDefinitionConversion() throws MalformedStreamDefinitionException {
        StreamDefinition streamDef = new StreamDefinition("stockStream", "1.1.0");
        List<Attribute> meta = new ArrayList<Attribute>(1);
        meta.add(new Attribute("symbol", AttributeType.STRING));
        List<Attribute> payload = new ArrayList<Attribute>(1);
        payload.add(new Attribute("price", AttributeType.DOUBLE));
        streamDef.setMetaData(meta);
        streamDef.setPayloadData(payload);

        org.wso2.siddhi.query.api.definition.StreamDefinition siddhiDefinition = EventProcessorUtil.convertToSiddhiStreamDefinition(streamDef, new StreamConfiguration("stockStream", "1.1.0"));
        Assert.assertEquals(siddhiDefinition.getAttributeList().size(), 2);
        log.info(siddhiDefinition);

/*
        StreamDefinition databrigeDefinition = EventProcessorUtil.convertToDatabridgeStreamDefinition(siddhiDefinition, "stockStream/1.1.0");
        Assert.assertEquals(databrigeDefinition.getPayloadData().size(), 2);
        log.info(databrigeDefinition);
*/
    }


}
