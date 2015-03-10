/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org)
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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.event.processor.core.internal.storm.compiler.SiddhiQLStormQuerySplitter;
import org.wso2.siddhi.query.compiler.SiddhiQLLexer;
import org.wso2.siddhi.query.compiler.SiddhiQLParser;
import org.wso2.siddhi.query.compiler.SiddhiQLVisitor;
import org.wso2.siddhi.query.compiler.internal.SiddhiErrorListener;

import java.util.List;

public class StormQuerySplitterTestCase {

    @Test
    public void testSplitting() {
        String source = "from  StockStream[price>3]#window.length(50) select symbol, avg(price) as avgPrice  " +
                "group by symbol  having (price >= 20) insert all events into StockQuote; " +
                "from StockStream[symbol==IBM] select * insert into testStream;";
        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.parse();

        SiddhiQLVisitor eval = new SiddhiQLStormQuerySplitter();
        List<String> queryList = (List<String>) eval.visit(tree);
        Assert.assertEquals("Two separate queries should return", 2, queryList.size());

    }

    @Test
    public void testPartitionSplitting() {
        String executionPlan = "@config(async = 'true')define stream streamA (symbol string, price int);" +
                "partition with (symbol of streamA) begin @info(name = 'query1') from streamA select symbol,price insert into StockQuote ;  " +
                "from StockQuote select symbol,price insert into dummyStock; end; from StockStream[symbol==IBM] select * insert into testStream;";

        ANTLRInputStream input = new ANTLRInputStream(executionPlan);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.parse();

        SiddhiQLVisitor eval = new SiddhiQLStormQuerySplitter();
        List<String> queryList = (List<String>) eval.visit(tree);
        Assert.assertEquals("Two separate queries should return", 2, queryList.size());
    }
}
