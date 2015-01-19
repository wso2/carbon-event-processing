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
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class StormQueryPlanBuilder {
    private static Document document;
    private static Element rootElement;
    private static Element processorInputStream;
    private static Element processorOutputStream;
    private static SiddhiManager mockSiddhiManager;     //to be used in later stages of development

    public static void init(List<StreamDefinition> streamDefinitions) throws ParserConfigurationException {
        mockSiddhiManager = new SiddhiManager(new SiddhiConfiguration());
        for (StreamDefinition definition : streamDefinitions) {
            mockSiddhiManager.defineStream(definition);
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();
        rootElement = document.createElement(EventProcessorConstants.STORM_QUERY_PLAN);
        document.appendChild(rootElement);
        processorInputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        processorOutputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
    }

    public static Document constructStormQueryPlanXML(ExecutionPlanConfiguration configuration, List<StreamDefinition> streamDefinitions) throws StormDeploymentException {
        Element receiverElement;
        Element processorElement;
        Element publisherElement;
        try {
            init(streamDefinitions);
            receiverElement = constructReceiverElement(configuration.getImportedStreams(), streamDefinitions);
            publisherElement = constructPublisherElement(configuration.getExportedStreams(),
                    streamDefinitions);
            processorElement = constructProcessorElement(configuration.getQueryExpressions());
        } catch (ParserConfigurationException e) {
            throw new StormDeploymentException("Error when creating storm query configuration.", e);
        } catch (EventStreamConfigurationException e) {
            throw new StormDeploymentException("Error when retrieving stream definitions in order to create storm " +
                    "query configuration", e);
        }
        rootElement.appendChild(receiverElement);
        rootElement.appendChild(processorElement);
        rootElement.appendChild(publisherElement);
        return document;
    }

    private static Element constructProcessorElement(String queryExpressions) {     //Will be assuming simple query scenario for initial development
        Element processor = document.createElement(EventProcessorConstants.EVENT_PROCESSOR_TAG);
        processor.setAttribute("name", EventProcessorConstants.SIDDHI_BOLT);
        processor.setAttribute("parallel", "3");      //TODO:get from config
        processor.appendChild(processorInputStream);
        Element queries = document.createElement(EventProcessorConstants.QUERIES);
        queries.setTextContent(queryExpressions);
        processor.appendChild(queries);
        processor.appendChild(processorOutputStream);
        return processor;
    }

    private static Element constructReceiverElement(List<StreamConfiguration> importedStreams,
                                                    List<StreamDefinition> streamDefinitions) throws EventStreamConfigurationException {
        Element receiverElement = document.createElement(EventProcessorConstants.EVENT_RECEIVER);
        receiverElement.setAttribute("name", EventProcessorConstants.EVENT_RECEIVER_SPOUT);
        receiverElement.setAttribute("parallel", "2");      //TODO:get from config
        Element streams = document.createElement(EventProcessorConstants.STREAMS);
        for (StreamConfiguration config : importedStreams) {
            Element stream = getStreamElement(findStreamDefinition(config.getSiddhiStreamName(), streamDefinitions));
            streams.appendChild(stream);
            Element clonedStream = (Element) stream.cloneNode(true);            //cloning to reuse
            processorInputStream.appendChild(clonedStream);
        }
        receiverElement.appendChild(streams);
        return receiverElement;
    }

    private static Element constructPublisherElement(List<StreamConfiguration> exportedStreams, List<StreamDefinition> streamDefinitions) throws EventStreamConfigurationException {
        Element publisherElement = document.createElement(EventProcessorConstants.EVENT_PUBLISHER);
        Element publisherInputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        Element publisherOutputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
        publisherElement.setAttribute("name", EventProcessorConstants.EVENT_PUBLISHER_BOLT);
        publisherElement.setAttribute("parallel", "3");      //TODO:get from config
        for (StreamConfiguration config : exportedStreams) {
            Element stream = getStreamElement(findStreamDefinition(config.getSiddhiStreamName(), streamDefinitions));
            publisherOutputStream.appendChild(stream);
            Element clonedStream = (Element) stream.cloneNode(true);
            publisherInputStream.appendChild(clonedStream);
            Element clonedStream1 = (Element) stream.cloneNode(true);
            processorOutputStream.appendChild(clonedStream1);
        }
        publisherElement.appendChild(publisherInputStream);
        publisherElement.appendChild(publisherOutputStream);
        return publisherElement;
    }

    private static StreamDefinition findStreamDefinition(String siddhiStreamName, List<StreamDefinition> streamDefinitions) throws EventStreamConfigurationException {
        for (StreamDefinition definition : streamDefinitions) {
            if (definition.getStreamId().equals(siddhiStreamName)) {
                return definition;
            }
        }
        throw new EventStreamConfigurationException("No stream definition found for stream Id " +
                siddhiStreamName);
    }

    private static Element getStreamElement(StreamDefinition streamDefinition) throws
            EventStreamConfigurationException {
        String definitionQuery = constructDefinitionString(streamDefinition);
        Element stream = document.createElement(EventProcessorConstants.STREAM);
        stream.setTextContent(definitionQuery);
        return stream;
    }

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
