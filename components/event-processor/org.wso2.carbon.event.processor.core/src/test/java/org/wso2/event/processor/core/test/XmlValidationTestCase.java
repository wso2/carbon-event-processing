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

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XmlValidationTestCase {
    private static final Log log = LogFactory.getLog(XmlValidationTestCase.class);
    private static final String XML_FILE = "<executionPlan name=\"KPIAnalyzer\" xmlns=\"http://wso2.org/carbon/eventprocessor\">\n" +
            "    <description>\n" +
            "        Notifies when a user purchases more then 3 phones for the total price higher than $2500.\n" +
            "    </description>\n" +
            "    <siddhiConfiguration>\n" +
            "        <property name=\"siddhi.persistence.snapshot.time.interval.minutes\">0</property>\n" +
            "        <property name=\"siddhi.enable.distributed.processing\">false</property>\n" +
            "    </siddhiConfiguration>\n" +
            "\n" +
            "\n" +
            "    <importedStreams>\n" +
            "        <stream name=\"stockStream\" version=\"1.2.0\" as=\"someName\"/>\n" +
            "        <!--todo add other parameters-->\n" +
            "        <stream name=\"phoneRetailStream\"  version=\"1.2.0\"/>\n" +
            "    </importedStreams>\n" +
            "\n" +
            "\n" +
            "    <queryExpressions>\n" +
            "        from someName[totalPrice>200 and quantity>1]#window.tableExt:persistence(\"tableWindow\",\n" +
            "        \"cepdb3.ceptable10\", \"add\", buyer)\n" +
            "        insert expired-events into highPurchaseStream\n" +
            "        buyer,brand, quantity, totalPrice;\n" +
            "    </queryExpressions>\n" +
            "\n" +
            "\n" +
            "    <exportedStreams>\n" +
            "        <stream valueOf=\"highPurchaseStream\" name=\"newname\" version=\"1.2.0\"/>\n" +
            "    </exportedStreams>\n" +
            "\n" +
            "</executionPlan>\n";

//    @Test
//    public void testOMElementValidation() throws ExecutionPlanConfigurationException, DeploymentException {
//        OMElement om = getExecutionPlanOMElement(XML_FILE);
//        EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(om);
//    }

    @Test
    public void testOMElementParsing() throws DeploymentException, ExecutionPlanConfigurationException {
        OMElement om = getExecutionPlanOMElement(XML_FILE);
        ExecutionPlanConfiguration config = EventProcessorConfigurationHelper.fromOM(om);
        Assert.assertNotNull("Converting OM element to execution plan configuration failed!", config);
        // converting back to xml
        OMElement out = EventProcessorConfigurationHelper.toOM(config);
        Assert.assertNotNull("Converting the exeuction plan to om element failed!", out);
    }

    private OMElement getExecutionPlanOMElement(String file)
            throws DeploymentException {
        OMElement executionPlanElement;
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(file.getBytes());
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            executionPlanElement = builder.getDocumentElement();
            executionPlanElement.build();

        } catch (Exception e) {
            String errorMessage = "unable to parse xml";
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return executionPlanElement;
    }
}
