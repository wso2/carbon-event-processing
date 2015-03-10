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
package org.wso2.carbon.event.processor.core.internal.storm.compiler;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.wso2.siddhi.query.compiler.SiddhiQLBaseVisitor;
import org.wso2.siddhi.query.compiler.SiddhiQLLexer;
import org.wso2.siddhi.query.compiler.SiddhiQLParser;
import org.wso2.siddhi.query.compiler.SiddhiQLVisitor;
import org.wso2.siddhi.query.compiler.internal.SiddhiErrorListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor implementation for Storm Compiler
 */
public class SiddhiQLStormQuerySplitter extends SiddhiQLBaseVisitor {

    public static List<String> split(String source) {
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
        return queryList;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitParse(@NotNull SiddhiQLParser.ParseContext ctx) {
        return visit(ctx.execution_plan());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public List<String> visitExecution_plan(@NotNull SiddhiQLParser.Execution_planContext ctx) {
        List<String> stringQueryList =  new ArrayList<String>();
        for (SiddhiQLParser.Execution_elementContext executionElementContext : ctx.execution_element()) {
            String query =  (String) visit(executionElementContext);
            stringQueryList.add(query);
        }
        return stringQueryList;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Returns the string rule related for this section.</p>
     *
     * @param ctx
     */
    @Override
    public String visitQuery(@NotNull SiddhiQLParser.QueryContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval interval = new Interval(a,b);
        return ctx.start.getInputStream().getText(interval);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * <p>Returns the string rule related for this section.</p>
     *
     * @param ctx
     */
    @Override
    public String visitPartition(@NotNull SiddhiQLParser.PartitionContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval interval = new Interval(a,b);
        return ctx.start.getInputStream().getText(interval);
    }
}
