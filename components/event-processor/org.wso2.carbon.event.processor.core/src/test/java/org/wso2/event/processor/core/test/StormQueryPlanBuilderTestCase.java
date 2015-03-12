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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.StormDeploymentException;
import org.wso2.carbon.event.processor.core.exception.StormQueryConstructionException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.storm.util.StormQueryPlanBuilder;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StormQueryPlanBuilderTestCase {

    //private static final String singleQuery =

    @Test
    public void testSingleQuery() throws Exception {

        List<String> importedDefinition = new ArrayList<String>(1);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addExportedStream(new StreamConfiguration("test2", "1.0.0", "filteredStatStream"));
        configuration.setQueryExpressions("@name('query1') @dist(parallel='1') from analyticsStats[meta_ipAdd != '192" +
                ".168.1.1']#window.time(5 min) " +
                "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                "insert into filteredStatStream;");
        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String filteredAnalyticStats = "define stream filteredStatStream ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string );";
        importedDefinition.add(analyticStats);
        exportedDefinition.add(filteredAnalyticStats);
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition,
                exportedDefinition);
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
            String name = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants.NAME));
            Assert.assertEquals("query1", name);
            String parallelism = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants.PARALLEL));
            Assert.assertEquals("1", parallelism);
            String isEnforced = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants
                    .ENFORCE_PARALLELISM));
            Assert.assertEquals("true", isEnforced);
            String query = eventProcessorElement.getFirstChildWithName(new QName
                    ("queries")).getText();
            Assert.assertEquals(query, "@name('query1') @dist(parallel='1') from analyticsStats[meta_ipAdd != '192" +
                    ".168.1.1']#window.time(5 min) " +
                    "select meta_ipAdd, meta_index, meta_timestamp, meta_nanoTime, userID " +
                    "insert into filteredStatStream;");
        }
    }

    @Test
    public void testMultipleQueryWithAnnotations() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='2') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='2') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "@name('query4') @dist(parallel='1') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') @dist(parallel='5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition,
                exportedDefinition);
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
            Assert.assertEquals(streamDefinitions.get(0), "define stream fortuneCompanyStream ( price int, " +
                    "symbol string, count long );");
        }

        //Assert event processor elements
        List<String> queryList = Arrays.asList(queryExpression.split(";"));
        iterator = queryElement.getChildrenWithName(new QName("event-processor"));
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String query = eventProcessorElement.getFirstChildWithName(new QName
                    ("queries")).getText();
            int index = Integer.parseInt(eventProcessorElement.getAttributeValue(new QName("name")).substring("query"
                    .length()));
            Assert.assertEquals(query, queryList.get(index - 1).trim() + ";");
        }
    }

    @Test
    public void testQueryGrouping() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='1') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='1', execGroup='1') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1', execGroup='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "@name('query4') @dist(parallel='1') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') @dist(parallel='5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition,
                exportedDefinition);
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
            Assert.assertEquals(streamDefinitions.get(0), "define stream fortuneCompanyStream ( price int, " +
                    "symbol string, count long );");
        }

        //Assert event processor elements
        iterator = queryElement.getChildrenWithName(new QName("event-processor"));
        int count = 0;
        while (iterator.hasNext()) {
            OMElement eventProcessorElement = iterator.next();
            String name = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants.NAME));
            if(name.equals("1")){
                String isEnforced = eventProcessorElement.getAttributeValue(new QName(EventProcessorConstants
                        .ENFORCE_PARALLELISM));
                Assert.assertEquals("true", isEnforced);
            }
            count++;
        }
        Assert.assertEquals("There should be only 4 processor elements", 4, count);
    }


    @Test(expected = StormQueryConstructionException.class)
    public void testMultipleParallelismInSingleGroup() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='1') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='2', execGroup='1') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1', execGroup='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "@name('query4') @dist(parallel='1') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') @dist(parallel='5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition, exportedDefinition);
    }

    @Test(expected = StormQueryConstructionException.class)
    public void testMultipleParallelismInStateQueries() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='1') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='1', execGroup='1') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1', execGroup='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "@name('query4') @dist(parallel='2') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') @dist(parallel='5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition, exportedDefinition);
    }

    @Test
    public void testPartitioningQuery() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='1') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='1', execGroup='1') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1', execGroup='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "partition with (symbol of joinStream) begin "+
                "@name('query4') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n"+
                "end;";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        Document document = StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition,
                exportedDefinition);
        OMElement queryElement = XMLUtils.toOM(document.getDocumentElement());
        Iterator<OMElement> iterator = queryElement.getChildrenWithName(new QName("event-processor"));
        int count = 0;
        int partitionCount = 0;
        String partitionAttribute = null;
        while (iterator.hasNext()) {
            count++;
            OMElement eventProcessorElement = iterator.next();
            OMElement inputStreams = eventProcessorElement.getFirstChildWithName(new QName(EventProcessorConstants.INPUT_STREAMS));
            Iterator<OMElement> iterator2 = inputStreams.getChildrenWithName(new QName (EventProcessorConstants.STREAM));
            while(iterator2.hasNext()){
                OMElement streamElement = iterator2.next();
                if (streamElement.getAttribute(new QName(EventProcessorConstants.PARTITION)) != null){
                    partitionCount++;
                    partitionAttribute = streamElement.getAttribute(new QName(EventProcessorConstants.PARTITION))
                            .getAttributeValue();
                }
            }
        }
        Assert.assertEquals("There should be only 4 processor elements", 3, count);
        Assert.assertEquals("There should be only 1 partition", 1, partitionCount);
        Assert.assertNotNull("Partition attribute should not be null", partitionAttribute);
        Assert.assertEquals("Partition attribute should be correctly set", "symbol", partitionAttribute);
    }

    @Test(expected = StormQueryConstructionException.class)
    public void testPartitioningWithDistAnnotationsQuery() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='1') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='1', execGroup='1') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1', execGroup='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "partition with (symbol of joinStream) begin "+
                "@name('query4') @dist(parallel='2') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') @dist(parallel='5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n"+
                "end;";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition, exportedDefinition);

    }

    @Test(expected = StormQueryConstructionException.class)
    public void testPartitioningWithGroupingQuery() throws Exception {
        List<String> importedDefinition = new ArrayList<String>(2);
        List<String> exportedDefinition = new ArrayList<String>(1);
        ExecutionPlanConfiguration configuration = new ExecutionPlanConfiguration();
        configuration.addImportedStream(new StreamConfiguration("test1", "1.0.0", "analyticsStats"));
        configuration.addImportedStream(new StreamConfiguration("test2", "1.0.0", "stockQuote"));
        configuration.addExportedStream(new StreamConfiguration("test3", "1.0.0", "fortuneCompanyStream"));
        String queryExpression = "@name('query1') @dist(parallel='1') from analyticsStats[searchTerms==\"google\" or " +
                "searchTerms==\"wso2\" or " +
                "searchTerms==\"msft\" or searchTerms==\"oracle\"]\n" +
                "select userID, searchTerms as symbol\n" +
                "insert into filteredStatStream;\n" +
                "\n" +
                "@name('query2') @dist(parallel='1', execGroup='1') from stockQuote[price>100]\n" +
                "select price, symbol\n" +
                "insert into highStockQuote;\n" +
                "\n" +
                "@name('query3') @dist(parallel='1', execGroup='1') from highStockQuote#window.time(5 min) as h join " +
                "filteredStatStream#window.time(5 min) as f   \n" +
                "on h.symbol==f.symbol\n" +
                "select h.price as price, h.symbol as symbol, f.userID as userid\n" +
                "insert into joinStream;\n" +
                "\n" +
                "@dist(execGroup='1') partition with (symbol of joinStream) begin "+
                "@name('query4') from joinStream#window.time(5 min)\n" +
                "select price, symbol, count(userid) as count\n" +
                "insert into countedStream;\n" +
                "\n" +
                "@name('query5') from countedStream[count>10]   \n" +
                "select price, symbol, count\n" +
                "insert into fortuneCompanyStream;\n"+
                "end;";
        configuration.setQueryExpressions(queryExpression);

        String analyticStats = "define stream analyticsStats ( meta_ipAdd string, meta_index long, " +
                "meta_timestamp long, meta_nanoTime long, userID string, searchTerms string );";
        String stockQuotes = "define stream stockQuote ( price int, symbol string );";
        String filteredAnalyticStats = "define stream fortuneCompanyStream ( price int, symbol string, count long );";
        importedDefinition.add(analyticStats);
        importedDefinition.add(stockQuotes);
        exportedDefinition.add(filteredAnalyticStats);
        StormQueryPlanBuilder.constructStormQueryPlanXML(configuration, importedDefinition, exportedDefinition);

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