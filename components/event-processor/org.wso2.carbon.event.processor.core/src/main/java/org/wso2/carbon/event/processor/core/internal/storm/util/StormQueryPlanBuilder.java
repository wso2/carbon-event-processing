/*
 * Copyright (c) 2014 - 2015, WSO2 Inc. (http://www.wso2.org)
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
package org.wso2.carbon.event.processor.core.internal.storm.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.exception.StormDeploymentException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

public class StormQueryPlanBuilder {

    /**
     * Gets Siddhi queries and construct storm query plan which can be used to build a storm topology.
     * query plan essentially comprise of three main elements. Receiver element, event processor element and
     * publisher element. Each of that will be constructed in separate methods and integrated here.
     *
     * @param configuration     Execution plan configuration
     * @return
     * @throws StormDeploymentException
     */
    public static Document constructStormQueryPlanXML(ExecutionPlanConfiguration configuration,
                                                      List<String> importStreams, List<String> exportStreams)
            throws StormDeploymentException {
        Document document;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            Element rootElement = document.createElement(EventProcessorConstants.STORM_QUERY_PLAN);
            document.appendChild(rootElement);

            Element receiverElement;
            List<Element> processorElements;
            Element publisherElement;

            receiverElement = constructReceiverElement(document, importStreams);
            publisherElement = constructPublisherElement(document, exportStreams);
            processorElements = constructProcessorElement(document, configuration.getQueryExpressions(), importStreams,
                    exportStreams);

            rootElement.appendChild(receiverElement);
            for (Element processorElement : processorElements) {
                rootElement.appendChild(processorElement);
            }
            rootElement.appendChild(publisherElement);
        } catch (ParserConfigurationException e) {
            throw new StormDeploymentException("Error when creating storm query configuration.", e);
        } catch (EventStreamConfigurationException e) {
            throw new StormDeploymentException("Error when retrieving stream definitions in order to create storm " +
                    "query configuration", e);
        } catch (SiddhiParserException e) {
            throw new StormDeploymentException("Provided Siddhi query contains errors", e);
        }
        return document;
    }

    /**
     * Construct and return a list of event-processor elements. Method can handle even if there are definitions
     * in the query string. queryList is used to get the map between query object and query string.
     *
     *
     * @param document
     * @param queryExpressions user provided query string
     * @return
     */
    private static List<Element> constructProcessorElement(Document document, String queryExpressions, List<String> importedStreams,
                                                           List<String> exportedStreams)
            throws SiddhiParserException {
        List<Element> processorElementList = new ArrayList<Element>();
        List<String> stringQueryList = Arrays.asList(queryExpressions.split(";"));
        String fullQueryExpression = EventProcessorUtil.constructQueryExpression(importedStreams, exportedStreams,
                queryExpressions);
        ExecutionPlanRuntime executionPlanRuntime = EventProcessorValueHolder.getSiddhiManager()
                .createExecutionPlanRuntime(fullQueryExpression);
        ExecutionPlan executionPlan = SiddhiCompiler.parse(queryExpressions);
        List<ExecutionElement> executionElements = executionPlan.getExecutionElementList();
        for (int i = 0; i < executionElements.size(); i++) {
            if (executionElements.get(i) instanceof Query) {    //todo partition and all
                Element processor = document.createElement(EventProcessorConstants.EVENT_PROCESSOR_TAG);
                processor.setAttribute(EventProcessorConstants.NAME, EventProcessorConstants.SIDDHI_BOLT + i);
                processor.setAttribute(EventProcessorConstants.PARALLEL, "1");      //TODO:get from config
                Element inputStream = getProcessorInputStream(document, ((Query) executionElements.get(i))
                        .getInputStream()
                        .getAllStreamIds(), executionPlanRuntime);
                processor.appendChild(inputStream);
                Element queries = document.createElement(EventProcessorConstants.QUERIES);
                queries.setTextContent(stringQueryList.get(i).trim() + ";");
                processor.appendChild(queries);
                Element outputStream = getProcessorOutputStream(document, ((Query) executionElements.get(i))
                        .getOutputStream().getId(), executionPlanRuntime);
                processor.appendChild(outputStream);
                processorElementList.add(processor);
            }
        }
        return processorElementList;
    }

    /**
     * Create output stream element for event processor element
     *
     * @param streamId
     * @param executionPlanRuntime
     * @return
     */
    private static Element getProcessorOutputStream(Document document, String streamId, ExecutionPlanRuntime executionPlanRuntime) {
        Element outputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
        Element stream = getStreamElement(document, EventProcessorUtil.getDefinitionString(executionPlanRuntime
                .getStreamDefinitionMap().get(streamId)));
        outputStream.appendChild(stream);
        return outputStream;
    }

    /**
     * Create input stream element for event processor element
     *
     * @param streamIds
     * @param executionPlanRuntime
     * @return
     */
    private static Element getProcessorInputStream(Document document, List<String> streamIds,
                                                   ExecutionPlanRuntime executionPlanRuntime) {
        Element inputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        for (String streamId : streamIds) {
            Element stream = getStreamElement(document, EventProcessorUtil.getDefinitionString(executionPlanRuntime
                    .getStreamDefinitionMap().get(streamId)));
            inputStream.appendChild(stream);
        }
        return inputStream;
    }

    /**
     * Create receiver element. Assume that imported streams contains all the receiver elements.
     *
     *
     * @param document
     * @param importedStreams
     * @return
     * @throws EventStreamConfigurationException
     */
    private static Element constructReceiverElement(Document document, List<String> importedStreams)
            throws EventStreamConfigurationException {
        Element receiverElement = document.createElement(EventProcessorConstants.EVENT_RECEIVER);
        receiverElement.setAttribute(EventProcessorConstants.NAME, EventProcessorConstants.EVENT_RECEIVER_SPOUT);
        receiverElement.setAttribute(EventProcessorConstants.PARALLEL, "2");    //todo configure
        Element streams = document.createElement(EventProcessorConstants.STREAMS);
        for (String definition : importedStreams) {
            Element stream = getStreamElement(document, definition);
            streams.appendChild(stream);
        }
        receiverElement.appendChild(streams);
        return receiverElement;
    }

    /**
     * Create publisher element. Assumes that exported streams contains all publisher streams.
     *
     * @param exportedStreams
     * @return
     * @throws EventStreamConfigurationException
     */
    private static Element constructPublisherElement(Document document, List<String> exportedStreams)
            throws EventStreamConfigurationException {
        Element publisherElement = document.createElement(EventProcessorConstants.EVENT_PUBLISHER);
        Element publisherInputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        Element publisherOutputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
        publisherElement.setAttribute(EventProcessorConstants.NAME, EventProcessorConstants.EVENT_PUBLISHER_BOLT);
        publisherElement.setAttribute(EventProcessorConstants.PARALLEL, "3"); //todo load from config
        for (String definition : exportedStreams) {
            Element stream = getStreamElement(document, definition);
            publisherOutputStream.appendChild(stream);
            Element clonedStream = (Element) stream.cloneNode(true);
            publisherInputStream.appendChild(clonedStream);
        }
        publisherElement.appendChild(publisherInputStream);
        publisherElement.appendChild(publisherOutputStream);
        return publisherElement;
    }

    /**
     * Create stream element when provided with a stream definition
     *
     * @return
     */
    private static Element getStreamElement(Document document, String definitionQuery) {
        Element stream = document.createElement(EventProcessorConstants.STREAM);
        stream.setTextContent(definitionQuery);
        return stream;
    }

}
