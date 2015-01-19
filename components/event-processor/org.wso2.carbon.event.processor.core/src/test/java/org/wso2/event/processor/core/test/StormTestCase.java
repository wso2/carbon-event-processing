/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.event.processor.core.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormQueryPlanBuilder;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StormTestCase {

    @Test
    public void testStormQueryPlanBuilder() throws Exception {
        SiddhiManager mockSiddhiManager = new SiddhiManager(new SiddhiConfiguration());
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addExportedStream(new StreamConfiguration("test2", "1.0.0", "filteredStatStream"));
        configuration.setQueryExpressions("from analyticsStats[meta_ipAdd != '192.168.1.1']#window.time(5 min) " +
                "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                "insert into filteredStatStream;");
        mockSiddhiManager.defineStream("define stream analyticsStats (meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long,userID string, searchTerms string );");
        mockSiddhiManager.addQuery("from analyticsStats[meta_ipAdd != '192.168.1.1']#window.time(5 min) " +
                "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                "insert into filteredStatStream;");
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, mockSiddhiManager.getStreamDefinitions());
        /*OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement domDoc = BeanUtil.convertDOMtoOM(factory, document);*/
        OMElement queryElement = XMLUtils.toOM(document.getDocumentElement());

        Iterator<OMElement> iterator = queryElement.getChildrenWithName(new QName("event-receiver"));
        while (iterator.hasNext()) {
            OMElement eventReceiverElement = iterator.next();
            List<String> streamDefinitions = getStreamDefinitions(eventReceiverElement.getFirstChildWithName(new QName
                    ("streams")));
            Assert.assertTrue(streamDefinitions.size() == 1);
            Assert.assertEquals(streamDefinitions.get(0), "define stream analyticsStats ( meta_ipAdd string, " +
                    "meta_index long, " +
                    "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );");
        }
        iterator = queryElement.getChildrenWithName(new QName("event-publisher"));
        while (iterator.hasNext()) {
            OMElement eventPublisherElement = iterator.next();
            List<String> streamDefinitions = getStreamDefinitions(eventPublisherElement.getFirstChildWithName(new QName
                    ("input-streams")));
            Assert.assertTrue(streamDefinitions.size() == 1);
            Assert.assertEquals(streamDefinitions.get(0), "define stream filteredStatStream ( meta_ipAdd string, " +
                    "meta_index long, meta_timestamp long, meta_nanoTime long, userID string );");
        }
        iterator = queryElement.getChildrenWithName(new QName("event-processor"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String query = eventProcessorElement.getFirstChildWithName(new QName
                    ("queries")).getText();
            Assert.assertEquals(query, "from analyticsStats[meta_ipAdd != '192.168.1.1']#window.time(5 min) " +
                    "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                    "insert into filteredStatStream;");
        }
    }

    private static List<String> getStreamDefinitions(OMElement streamsElement) {
        List<String> inputStreamDefinitions = new ArrayList<String>();
        Iterator<OMElement> inputStreamIterator = streamsElement.getChildrenWithName(new QName("stream"));
        while (inputStreamIterator.hasNext()) {
            OMElement inputStreamElement = inputStreamIterator.next();
            String streamDefinition = inputStreamElement.getText();
            inputStreamDefinitions.add(streamDefinition);
        }
        return inputStreamDefinitions;
    }
}