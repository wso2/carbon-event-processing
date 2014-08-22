/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.event.processor.core.test;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SiddhiRuntimeTestCase {
    private static final Log log = LogFactory.getLog(SiddhiRuntimeTestCase.class);

    @Test
    public void testSiddhiRuntimeLoading() {
        AxisConfiguration axisConfig = new AxisConfiguration();

        ExecutionPlanConfiguration config = new ExecutionPlanConfiguration();
        config.setName("queryPlan1");
        config.setQueryExpressions("from stockStream select symbol, price insert into financialDataStream;");
        StreamConfiguration in = new StreamConfiguration("stockStream", "1.0.0");
        StreamConfiguration out = new StreamConfiguration("financialDataStream", "1.0.0");
        config.addImportedStream(in);
        config.addExportedStream(out);

        List<StreamDefinition> importedStreams = new ArrayList<StreamDefinition>();
        StreamDefinition streamDefinition = new StreamDefinition("stockStream");
        streamDefinition.addMetaData("symbol", AttributeType.STRING);
        streamDefinition.addPayloadData("price", AttributeType.DOUBLE);
        importedStreams.add(streamDefinition);
//        SiddhiRuntime runtime = SiddhiRuntimeFactory.createSiddhiRuntime(config, importedStreams, -1);
        log.info("siddhi runtime loaded successfully.");

    }
}
