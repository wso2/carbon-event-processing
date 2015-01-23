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
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.StormDeploymentException;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StormQueryPlanBuilder {
    private static Document document;
    private static Element rootElement;
    private static SiddhiManager mockSiddhiManager;
    private static List<String> stringQueryList = new ArrayList<String>();
    private static List<ExecutionPlan> queryList = new ArrayList<ExecutionPlan>(stringQueryList.size());

    private static void init(List<StreamDefinition> streamDefinitions) throws ParserConfigurationException {
        mockSiddhiManager = new SiddhiManager(new SiddhiConfiguration());
        for (StreamDefinition definition : streamDefinitions) {
            mockSiddhiManager.defineStream(definition);
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        rootElement = document.createElement(EventProcessorConstants.STORM_QUERY_PLAN);
        document.appendChild(rootElement);
    }

    /**
     * Gets Siddhi queries and construct storm query plan which can be used to build a storm topology.
     * query plan essentially comprise of three main elements. Receiver element, event processor element and
     * publisher element. Each of that will be constructed in separate methods and integrated here.
     *
     * @param configuration     Execution plan configuration
     * @param streamDefinitions List of stream definitions
     * @return
     * @throws StormDeploymentException
     */
    public static Document constructStormQueryPlanXML(ExecutionPlanConfiguration configuration, List<StreamDefinition> streamDefinitions) throws StormDeploymentException {
        Element receiverElement;
        List<Element> processorElements;
        Element publisherElement;
        try {
            init(streamDefinitions);
            receiverElement = constructReceiverElement(configuration.getImportedStreams(), streamDefinitions);
            publisherElement = constructPublisherElement(configuration.getExportedStreams(),
                    streamDefinitions);
            processorElements = constructProcessorElement(configuration.getQueryExpressions());
        } catch (ParserConfigurationException e) {
            throw new StormDeploymentException("Error when creating storm query configuration.", e);
        } catch (EventStreamConfigurationException e) {
            throw new StormDeploymentException("Error when retrieving stream definitions in order to create storm " +
                    "query configuration", e);
        } catch (SiddhiParserException e) {
            throw new StormDeploymentException("Provided Siddhi query contains errors", e);
        }
        rootElement.appendChild(receiverElement);
        for (Element processorElement : processorElements) {
            rootElement.appendChild(processorElement);
        }
        rootElement.appendChild(publisherElement);
        return document;
    }

    /**
     * Construct and return a list of event-processor elements. Method can handle even if there are definitions
     * in the query string. queryList is used to get the map between query object and query string.
     *
     * @param queryExpressions user provided query string
     * @return
     */
    private static List<Element> constructProcessorElement(String queryExpressions) throws SiddhiParserException {
        List<Element> processorElementList = new ArrayList<Element>();
        stringQueryList = Arrays.asList(queryExpressions.split(";"));
        mockSiddhiManager.addExecutionPlan(queryExpressions);
        queryList = SiddhiCompiler.parse(queryExpressions);

        for (int i = 0; i < queryList.size(); i++) {
            if (queryList.get(i) instanceof Query) {
                Element processor = document.createElement(EventProcessorConstants.EVENT_PROCESSOR_TAG);
                processor.setAttribute(EventProcessorConstants.NAME, EventProcessorConstants.SIDDHI_BOLT + i);
                processor.setAttribute(EventProcessorConstants.PARALLEL, "1");      //TODO:get from config
                Element inputStream = getProcessorInputStream(((Query) queryList.get(i)).getInputStream().getStreamIds());
                processor.appendChild(inputStream);
                Element queries = document.createElement(EventProcessorConstants.QUERIES);
                queries.setTextContent(stringQueryList.get(i).trim() + ";");
                processor.appendChild(queries);
                Element outputStream = getProcessorOutputStream(((Query) queryList.get(i)).getOutputStream()
                        .getStreamId());
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
     * @return
     */
    private static Element getProcessorOutputStream(String streamId) {
        Element outputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
        Element stream = getStreamElement(mockSiddhiManager.getStreamDefinition(streamId));
        outputStream.appendChild(stream);
        return outputStream;
    }

    /**
     * Create input stream element for event processor element
     *
     * @param streamIds
     * @return
     */
    private static Element getProcessorInputStream(List<String> streamIds) {
        Element inputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        for (String streamId : streamIds) {
            Element stream = getStreamElement(mockSiddhiManager.getStreamDefinition(streamId));
            inputStream.appendChild(stream);
        }
        return inputStream;
    }

    /**
     * Create receiver element. Assume that imported streams contains all the receiver elements.
     *
     * @param importedStreams
     * @param streamDefinitions
     * @return
     * @throws EventStreamConfigurationException
     */
    private static Element constructReceiverElement(List<StreamConfiguration> importedStreams,
                                                    List<StreamDefinition> streamDefinitions) throws EventStreamConfigurationException {
        Element receiverElement = document.createElement(EventProcessorConstants.EVENT_RECEIVER);
        receiverElement.setAttribute(EventProcessorConstants.NAME, EventProcessorConstants.EVENT_RECEIVER_SPOUT);
        receiverElement.setAttribute(EventProcessorConstants.PARALLEL, "2");
        Element streams = document.createElement(EventProcessorConstants.STREAMS);
        for (StreamConfiguration config : importedStreams) {
            Element stream = getStreamElement(findStreamDefinition(config.getSiddhiStreamName(), streamDefinitions));
            streams.appendChild(stream);
        }
        receiverElement.appendChild(streams);
        return receiverElement;
    }

    /**
     * Create publisher element. Assumes that exported streams contains all publisher streams.
     *
     * @param exportedStreams
     * @param streamDefinitions
     * @return
     * @throws EventStreamConfigurationException
     */
    private static Element constructPublisherElement(List<StreamConfiguration> exportedStreams, List<StreamDefinition> streamDefinitions) throws EventStreamConfigurationException {
        Element publisherElement = document.createElement(EventProcessorConstants.EVENT_PUBLISHER);
        Element publisherInputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        Element publisherOutputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
        publisherElement.setAttribute(EventProcessorConstants.NAME, EventProcessorConstants.EVENT_PUBLISHER_BOLT);
        publisherElement.setAttribute(EventProcessorConstants.PARALLEL, "3");
        for (StreamConfiguration config : exportedStreams) {
            Element stream = getStreamElement(findStreamDefinition(config.getSiddhiStreamName(), streamDefinitions));
            publisherOutputStream.appendChild(stream);
            Element clonedStream = (Element) stream.cloneNode(true);
            publisherInputStream.appendChild(clonedStream);
        }
        publisherElement.appendChild(publisherInputStream);
        publisherElement.appendChild(publisherOutputStream);
        return publisherElement;
    }

    /**
     * Util method to find a stream definition from a list
     *
     * @param siddhiStreamName
     * @param streamDefinitions
     * @return
     * @throws EventStreamConfigurationException
     */
    private static StreamDefinition findStreamDefinition(String siddhiStreamName, List<StreamDefinition> streamDefinitions) throws EventStreamConfigurationException {
        for (StreamDefinition definition : streamDefinitions) {
            if (definition.getStreamId().equals(siddhiStreamName)) {
                return definition;
            }
        }
        throw new EventStreamConfigurationException("No stream definition found for stream Id " +
                siddhiStreamName);
    }

    /**
     * Create stream element when provided with a stream definition
     *
     * @param streamDefinition
     * @return
     */
    private static Element getStreamElement(StreamDefinition streamDefinition) {
        String definitionQuery = constructDefinitionString(streamDefinition);
        Element stream = document.createElement(EventProcessorConstants.STREAM);
        stream.setTextContent(definitionQuery);
        return stream;
    }

    /**
     * Create definition string when provided with a stream definition
     *
     * @param definition
     * @return
     */
    private static String constructDefinitionString(StreamDefinition definition) {
        StringBuilder builder = new StringBuilder();
        builder.append(EventProcessorConstants.DEFINE_STREAM);
        builder.append(definition.getStreamId());
        builder.append(EventProcessorConstants.OPENING_BRACKETS);
        for (Attribute attribute : definition.getAttributeList()) {
            builder.append(attribute.getName() + EventProcessorConstants.SPACE + attribute.getType().toString().toLowerCase() +
                    EventProcessorConstants.COMMA);
        }
        builder.deleteCharAt(builder.length() - 2);         //remove last comma
        builder.append(EventProcessorConstants.CLOSING_BRACKETS);
        return builder.toString();
    }

}
