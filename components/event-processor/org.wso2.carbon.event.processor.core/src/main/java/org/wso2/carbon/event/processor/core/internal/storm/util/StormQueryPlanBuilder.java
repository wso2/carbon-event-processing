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
import org.wso2.carbon.event.processor.core.exception.StormQueryConstructionException;
import org.wso2.carbon.event.processor.core.internal.storm.compiler.SiddhiQLStormQuerySplitter;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class StormQueryPlanBuilder {

    /**
     * Gets Siddhi queries and construct storm query plan which can be used to build a storm topology.
     * query plan essentially comprise of three main elements. Receiver element, event processor element and
     * publisher element. Each of that will be constructed in separate methods and integrated here.
     *
     * @param configuration     Execution plan configuration
     * @return
     * @throws StormQueryConstructionException
     */
    public static Document constructStormQueryPlanXML(ExecutionPlanConfiguration configuration,
                                                      List<String> importStreams, List<String> exportStreams)
            throws StormQueryConstructionException {
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
            throw new StormQueryConstructionException("Error when creating storm query configuration.", e);
        } catch (EventStreamConfigurationException e) {
            throw new StormQueryConstructionException("Error when retrieving stream definitions in order to create storm " +
                    "query configuration", e);
        } catch (SiddhiParserException e) {
            throw new StormQueryConstructionException("Provided Siddhi query contains errors", e);
        }
        return document;
    }

    /**
     * Create receiver element. Assume that imported streams contains all the receiver elements.
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
        receiverElement.setAttribute(EventProcessorConstants.PARALLEL, "1");    //todo configure
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
        publisherElement.setAttribute(EventProcessorConstants.PARALLEL, "1"); //todo load from config
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
     * Construct and return a list of event-processor elements. Method can handle even if there are definitions
     * in the query string. queryList is used to get the map between query object and query string.
     *
     *
     * @param document
     * @param queryExpressions user provided query string
     * @return
     */
    private static List<Element> constructProcessorElement(Document document, String queryExpressions,
                                                           List<String> importedStreams, List<String> exportedStreams)
            throws SiddhiParserException, StormQueryConstructionException {
        SiddhiManager mockSiddhiManager = new SiddhiManager();
        String fullQueryExpression = EventProcessorUtil.constructQueryExpression(importedStreams, exportedStreams,
                queryExpressions);
        ExecutionPlanRuntime executionPlanRuntime = mockSiddhiManager.createExecutionPlanRuntime(fullQueryExpression);

        List<Element> processorElementList = new ArrayList<Element>();
        List<String> stringQueryList = SiddhiQLStormQuerySplitter.split(queryExpressions);

        ExecutionPlan executionPlan = SiddhiCompiler.parse(queryExpressions);
        List<ExecutionElement> executionElements = executionPlan.getExecutionElementList();
        Map<String, QueryGroupInfoHolder> groupIdToQueryMap = getGroupIdToQueryMap(executionElements,
                stringQueryList, exportedStreams);
        for (Map.Entry entry : groupIdToQueryMap.entrySet()) {
            String name = (String) entry.getKey();
            QueryGroupInfoHolder infoHolder = (QueryGroupInfoHolder) entry.getValue();
            ParallelismInfoHolder holder = getParallelismForGroup((String) entry.getKey(),
                    infoHolder.getExecutionElements());
            Element processor = document.createElement(EventProcessorConstants.EVENT_PROCESSOR_TAG);
            setAttributes(processor, name, holder);
            Element inputStream = getProcessorInputStream(document, new ArrayList<String>(infoHolder
                    .getInputDefinitionIds()), executionPlanRuntime, infoHolder.getPartitionFieldMap());
            processor.appendChild(inputStream);
            Element queries = document.createElement(EventProcessorConstants.QUERIES);
            String stringQueries = getQueryString(((QueryGroupInfoHolder) entry.getValue()).getStringQueries());
            queries.setTextContent(stringQueries);
            processor.appendChild(queries);
            Element outputStream = getProcessorOutputStream(document, new ArrayList<String>(((QueryGroupInfoHolder)
                    entry.getValue()).getOutputDefinitionIds()), executionPlanRuntime);
            processor.appendChild(outputStream);
            processorElementList.add(processor);
        }
        return processorElementList;
    }

    /**
     * Method to add attributes to processor element
     *
     * @param processor
     * @param name
     * @param holder
     */
    private static void setAttributes(Element processor, String name, ParallelismInfoHolder holder) throws
            StormQueryConstructionException {
        String parallel = String.valueOf(holder.getParallelism());
        Boolean enforceParallelism = holder.getIsEnforced();
        processor.setAttribute(EventProcessorConstants.NAME, name);
        processor.setAttribute(EventProcessorConstants.PARALLEL, parallel);
        processor.setAttribute(EventProcessorConstants.ENFORCE_PARALLELISM, String.valueOf(enforceParallelism));
    }

    /**
     * Creates query group id to queries map where queries with same group id will be put into single
     * QueryGroupInfoHolder. Also duplicate and inner stream definitions of each query group will be removed.
     *
     * @param executionElements
     * @param stringQueryList
     * @param exportedStreams
     * @return
     */
    private static Map<String, QueryGroupInfoHolder> getGroupIdToQueryMap(List<ExecutionElement> executionElements,
                                                                          List<String> stringQueryList,
                                                                          List<String> exportedStreams) throws
            StormQueryConstructionException {
        Map<String, QueryGroupInfoHolder> groupIdToQueryMap = new HashMap<String, QueryGroupInfoHolder>();
        for (int i = 0; i < executionElements.size(); i++) {
            String name = getName(executionElements.get(i).getAnnotations());
            String groupId = getExecuteGroup(executionElements.get(i).getAnnotations());
            if (groupId == null) {
                groupId = name;
            }
            int parallel = getParallelism(executionElements.get(i).getAnnotations());

            if (executionElements.get(i) instanceof Query) {
                Query query = (Query) executionElements.get(i);
                Boolean enforceParallelism = validateParallelism(query, parallel, stringQueryList.get(i));
                QueryGroupInfoHolder infoHolder = groupIdToQueryMap.get(groupId);
                if (infoHolder != null) {
                    infoHolder.addExecutionElement(new ExecutionElementInfoHolder(query, parallel, enforceParallelism));
                    infoHolder.addQueryString(stringQueryList.get(i));
                } else {
                    infoHolder = new QueryGroupInfoHolder(groupId);
                    infoHolder.addQueryString(stringQueryList.get(i));
                    infoHolder.addExecutionElement(new ExecutionElementInfoHolder(query, parallel, enforceParallelism));
                    groupIdToQueryMap.put(groupId, infoHolder);
                }
            } else {
                Partition partition = (Partition) executionElements.get(i);
                for (Query query : partition.getQueryList()) {
                    validateParallelism(query, -1, stringQueryList.get(i));
                }
                QueryGroupInfoHolder infoHolder = groupIdToQueryMap.get(groupId);
                if (infoHolder != null) {
                    throw new StormQueryConstructionException("Error deploying partition " + groupId + ". Query, " +
                            "Partition or execute group of same name has been defined earlier");
                } else {
                    infoHolder = new QueryGroupInfoHolder(groupId);
                    infoHolder.addExecutionElement(new ExecutionElementInfoHolder(partition, parallel, false));
                    infoHolder.addQueryString(stringQueryList.get(i));
                    groupIdToQueryMap.put(groupId, infoHolder);
                }
            }
        }

        List<String> exportedStreamIds = new ArrayList<String>(exportedStreams.size());
        for (String definitionString : exportedStreams) {
            StreamDefinition definition = SiddhiCompiler.parseStreamDefinition(definitionString);
            exportedStreamIds.add(definition.getId());
        }
        removeUnusedStreams(groupIdToQueryMap, exportedStreamIds);
        return groupIdToQueryMap;
    }

    /**
     * Queries like window and joins can not operate in parallel since they are stateful queries. So we are
     * validating parallelism of those stateful queries.
     *
     * @param query       query to be validated
     * @param parallel    user defined parallelism hint
     * @param queryString
     * @throws StormQueryConstructionException
     */
    private static Boolean validateParallelism(Query query, int parallel,
                                               String queryString) throws StormQueryConstructionException {
        if (parallel != -1) {    //if not a partition
            if (!(query.getInputStream() instanceof BasicSingleInputStream)) {      //if window/join/pattern query
                if (parallel > 1) {
                    throw new StormQueryConstructionException("Error in deploying query: " + queryString + " Parallelism has " +
                            "to be 1 for window, join and pattern queries. Partitioning can be used to facilitate such scenarios");
                } else {
                    return true;
                }
            } else {      //if simple filter query
                return false;
            }
        } else {    //if partition
            for (Annotation annotation : query.getAnnotations()) {
                if (annotation.getName().equals(EventProcessorConstants.DIST)) {
                    throw new StormQueryConstructionException("Error in deploying query: " + queryString + ". Query level" +
                            " @dist type annotations are not supported for queries inside partitions. Please resubmit" +
                            " the execution plan moving those annotation to Partition level.");
                }
            }
            return false;
        }
    }

    /**
     * Remove output streams which are only inner streams.
     *
     * @param groupIdToQueryMap
     * @param exportedStreams
     */
    private static void removeUnusedStreams(Map<String, QueryGroupInfoHolder> groupIdToQueryMap,
                                            List<String> exportedStreams) {
        for (Map.Entry entry : groupIdToQueryMap.entrySet()) {
            QueryGroupInfoHolder holder = (QueryGroupInfoHolder) entry.getValue();
            if (holder.getInputDefinitionIds().size() > 1) {
                Iterator<String> iterator = holder.getInputDefinitionIds().iterator();
                while (iterator.hasNext()) {
                    String streamId = iterator.next();
                    if (holder.getOutputDefinitionIds().contains(streamId)) {
                        iterator.remove();
                        continue;
                    }
                }
            }
            if (holder.getOutputDefinitionIds().size() > 1) {
                Iterator<String> iterator = holder.getOutputDefinitionIds().iterator();
                while (iterator.hasNext()) {
                    Boolean isUnused = true;
                    String streamId = iterator.next();
                    if (exportedStreams.contains(streamId)) {
                        continue;
                    }
                    for (Map.Entry entry2 : groupIdToQueryMap.entrySet()) {
                        //If any other query group does not use this output stream as an input stream
                        if ((!entry.getKey().equals(entry2.getKey())) && (((QueryGroupInfoHolder) entry2.getValue())
                                .getInputDefinitionIds().contains(streamId))) {
                            isUnused = false;
                            break;
                        }
                    }
                    if (isUnused) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Traverse annotations and returns the name
     *
     * @param annotations
     * @return
     */
    private static String getName(List<Annotation> annotations) {
        String name = UUID.randomUUID().toString();
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.getName().equals(EventProcessorConstants.NAME)) {
                    name = annotation.getElements().get(0).getValue();
                }
            }
        }
        return name;
    }

    /**
     * Traverse the annotation and returns the parallelism hint
     *
     * @param annotations
     * @return
     */
    private static int getParallelism(List<Annotation> annotations) {
        int parallelism = 1;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.getName().equals(EventProcessorConstants.DIST)) {
                    if (annotation.getElement(EventProcessorConstants.PARALLEL) != null) {
                        parallelism = Integer.parseInt(annotation.getElement(EventProcessorConstants.PARALLEL));
                        if (parallelism == 0) {
                            parallelism = 1;
                        }
                    }
                }
            }
        }
        return parallelism;
    }

    /**
     * Traverse the annotation and returns the execute group id
     *
     * @param annotations
     * @return
     */
    private static String getExecuteGroup(List<Annotation> annotations) {
        String id = null;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if (annotation.getName().equals(EventProcessorConstants.DIST)) {
                    if (annotation.getElement(EventProcessorConstants.EXEC_GROUP) != null) {
                        id = annotation.getElement(EventProcessorConstants.EXEC_GROUP);
                    }
                }
            }
        }
        return id;
    }

    private static String getQueryString(List<String> stringQueries) {
        StringBuilder builder = new StringBuilder();
        for (String query : stringQueries) {
            builder.append(query.trim() + ";");
        }
        return builder.toString();
    }

    /**
     * Method to define and validate group parallelism.
     *
     * @param groupId
     * @param executionElementHolders
     * @return
     * @throws StormQueryConstructionException
     */
    private static ParallelismInfoHolder getParallelismForGroup(String groupId, List<ExecutionElementInfoHolder>
            executionElementHolders) throws StormQueryConstructionException {
        Boolean isEnforced = false;
        Set<Integer> parallelism = new HashSet<Integer>();
        for (ExecutionElementInfoHolder element : executionElementHolders) {
            parallelism.add(element.getParallelismInfoHolder().getParallelism());
            if (element.getParallelismInfoHolder().getIsEnforced()) {
                isEnforced = true;
            }
        }
        if (parallelism.size() == 1) {
            return new ParallelismInfoHolder(parallelism.iterator().next(), isEnforced);
        } else {
            throw new StormQueryConstructionException("Parallelism for each query in a query group should be same. " +
                    "Multiple parallel values encountered in query group " + groupId);
        }
    }

    /**
     * Create output stream element for event processor element
     *
     * @param streamIds
     * @param executionPlanRuntime
     * @return
     */
    private static Element getProcessorOutputStream(Document document, List<String> streamIds,
                                                    ExecutionPlanRuntime executionPlanRuntime) {
        Element outputStream = document.createElement(EventProcessorConstants.OUTPUT_STREAMS);
        for (String streamId : streamIds) {
            Element stream = getStreamElement(document, EventProcessorUtil.getDefinitionString(executionPlanRuntime
                    .getStreamDefinitionMap().get(streamId)));
            outputStream.appendChild(stream);
        }
        return outputStream;
    }

    /**
     * Create input stream element for event processor element
     * @param streamIds
     * @param executionPlanRuntime
     * @param partitionFieldMap
     */
    private static Element getProcessorInputStream(Document document, List<String> streamIds,
                                                   ExecutionPlanRuntime executionPlanRuntime, Map<String,
            String> partitionFieldMap) {
        Element inputStream = document.createElement(EventProcessorConstants.INPUT_STREAMS);
        for (String streamId : streamIds) {
            Element stream = getStreamElement(document, EventProcessorUtil.getDefinitionString(executionPlanRuntime
                    .getStreamDefinitionMap().get(streamId)));
            if (partitionFieldMap != null) {
                String attribute = partitionFieldMap.get(streamId);
                if (attribute != null) {
                    stream.setAttribute(EventProcessorConstants.PARTITION, attribute);
                }
            }
            inputStream.appendChild(stream);
        }
        return inputStream;
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
