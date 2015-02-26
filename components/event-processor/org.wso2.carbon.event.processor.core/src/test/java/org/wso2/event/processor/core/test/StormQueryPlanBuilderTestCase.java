/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org)
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

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormQueryPlanBuilder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StormQueryPlanBuilderTestCase {

    @Test
    public void testSingleQuery() throws Exception {
        /*List<String> importedDefinition = new ArrayList<String>(1);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addExportedStream(new StreamConfiguration("test2", "1.0.0", "filteredStatStream"));
        configuration.setQueryExpressions("from analyticsStats[meta_ipAdd != '192.168.1.1']#window.time(5 min) " +
                "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                "insert into filteredStatStream;");
        StreamDefinition analyticsStats = new StreamDefinition().id("analyticsStats").attribute("meta_ipAdd",
                Attribute.Type.STRING).attribute("meta_index", Attribute.Type.LONG).attribute("meta_timestamp",
                Attribute.Type.LONG).attribute("meta_nanoTime", Attribute.Type.LONG).attribute("userID",
                Attribute.Type.STRING).attribute("searchTerms", Attribute.Type.STRING);
        StreamDefinition filteredStatStream = new StreamDefinition().id("filteredStatStream").attribute("meta_ipAdd",
                Attribute.Type.STRING).attribute("meta_index", Attribute.Type.LONG).attribute("meta_timestamp",
                Attribute.Type.LONG).attribute("meta_nanoTime", Attribute.Type.LONG).attribute("userID",
                Attribute.Type.STRING);
        *//*List<StreamDefinition> definitions = new ArrayList<StreamDefinition>(2);
        definitions.add(analyticsStats);
        definitions.add(filteredStatStream);*//*
        String analyticStats = "define stream filteredStatStream ( meta_ipAdd string,meta_index long, meta_timestamp long, meta_nanoTime long, userID string );";
        String filteredAnalyticStats = "define stream filteredStatStream ( meta_ipAdd string,meta_index long, meta_timestamp long, meta_nanoTime long, userID string );";
        importedDefinition.add(analyticStats);
        exportedDefinition.add(filteredAnalyticStats);
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition, exportedDefinition);
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
        }*/
    }

    @Test
    public void testMultipleQuery() throws Exception {
        /*List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "from analyticsStats[searchTerms==\"google\" or searchTerms==\"wso2\" or searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "from highStockQuote#window.time(5 min) as h join filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n";
        configuration.setQueryExpressions(queryExpression);
        StreamDefinition analyticsStats = new StreamDefinition().id("analyticsStats").attribute("meta_ipAdd",
                Attribute.Type.STRING).attribute("meta_index", Attribute.Type.LONG).attribute("meta_timestamp",
                Attribute.Type.LONG).attribute("meta_nanoTime", Attribute.Type.LONG).attribute("userID",
                Attribute.Type.STRING).attribute("searchTerms", Attribute.Type.STRING);
        StreamDefinition stockStream = new StreamDefinition().id("stockQuote").attribute("price",
                Attribute.Type.INT).attribute("symbol", Attribute.Type.STRING);
        StreamDefinition fortuneCompanyStream = new StreamDefinition().id("fortuneCompanyStream").attribute("price",
                Attribute.Type.INT).attribute("symbol", Attribute.Type.STRING).attribute("count", Attribute.Type.LONG);
        *//*List<StreamDefinition> definitions = new ArrayList<StreamDefinition>(3);
        definitions.add(analyticsStats);
        definitions.add(stockStream);
        definitions.add(fortuneCompanyStream);*//*

        String analyticStats = "define stream filteredStatStream ( meta_ipAdd string,meta_index long, meta_timestamp long, meta_nanoTime long, userID string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream filteredStatStream ( meta_ipAdd string,meta_index long, meta_timestamp long, meta_nanoTime long, userID string );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition, exportedDefinition);
        OMElement queryElement = XMLUtils.toOM(document.getDocumentElement());

        //Assert receiver element
        Iterator<OMElement> iterator = queryElement.getChildrenWithName(new QName("event-receiver"));
        while (iterator.hasNext()) {
            OMElement eventReceiverElement = iterator.next();
            List<String> streamDefinitions = getStreamDefinitions(eventReceiverElement.getFirstChildWithName(new QName
                    ("streams")));
            Assert.assertTrue(streamDefinitions.size() == 2);
            Assert.assertEquals(streamDefinitions.get(0), "define stream analyticsStats ( meta_ipAdd string, " +
                    "meta_index long, " +
                    "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );");
            Assert.assertEquals(streamDefinitions.get(1), "define stream stockQuote ( price int, symbol string );");
        }

        //Assert publisher element
        iterator = queryElement.getChildrenWithName(new QName("event-publisher"));
        while (iterator.hasNext()) {
            OMElement eventPublisherElement = iterator.next();
            List<String> streamDefinitions = getStreamDefinitions(eventPublisherElement.getFirstChildWithName(new QName
                    ("input-streams")));
            Assert.assertTrue(streamDefinitions.size() == 1);
            Assert.assertEquals(streamDefinitions.get(0), "define stream fortuneCompanyStream ( price int, symbol string, count long );");
        }

        //Assert event processor elements
        List<String> queryList = Arrays.asList(queryExpression.split(";"));
        iterator = queryElement.getChildrenWithName(new QName("event-processor"));
        for (int i = 0; iterator.hasNext(); i++) {
            OMElement eventProcessorElement = iterator.next();
            String query = eventProcessorElement.getFirstChildWithName(new QName
                    ("queries")).getText();
            Assert.assertEquals(query, queryList.get(i).trim() + ";");
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
        return inputStreamDefinitions;*/
    }
}