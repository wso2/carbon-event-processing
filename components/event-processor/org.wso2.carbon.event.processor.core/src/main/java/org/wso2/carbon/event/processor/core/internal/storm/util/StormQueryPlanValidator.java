package org.wso2.carbon.event.processor.core.internal.storm.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.event.processor.core.exception.StormQueryConstructionException;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;

import javax.xml.xpath.*;
import java.util.HashSet;
import java.util.Set;

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class StormQueryPlanValidator {

    private static XPathFactory xPathfactory = XPathFactory.newInstance();

    /**
     * Validate the query plan
     * @param queryPlan XML document of query plan
     * @throws StormQueryConstructionException
     */
    public static void validateQueryPlan(Document queryPlan) throws StormQueryConstructionException {
       validatePublishingStreams(queryPlan);
    }

    /**
     * Validates input streams of event-publisher. Each input stream of event-publisher MUST must be emitted by at least one spout or bolt.
     * Therefore, each input stream MUST be listed under output-streams of at least one event-processor or under event-receiver
     * @param queryPlan XML document of query plan
     * @throws StormQueryConstructionException
     */
    private static void validatePublishingStreams(Document queryPlan) throws StormQueryConstructionException {
        try {
            Set<String> allEmittedStreams = new HashSet<>();
            allEmittedStreams.addAll(extractStreamIds(getEventProcessorOutputStreams(queryPlan)));
            allEmittedStreams.addAll(extractStreamIds(getEventReceiverStreams(queryPlan)));

            for (String publisherInputStream : extractStreamIds(getEventPublisherInputStreams(queryPlan))){

                if (!allEmittedStreams.contains(publisherInputStream)){
                    throw new StormQueryConstructionException("Event publisher bolt(s) trying to consume stream '" + publisherInputStream + "', but it's " +
                            "not produced by any bolt/spout.");
                }
            }
        } catch (XPathExpressionException e) {
            throw new StormQueryConstructionException("Failed to validate query plan. Error :" + e.getMessage());
        }
    }

    /**
     *  Fetch stream definitions of event-receiver element
     */
    private static Set<String> getEventReceiverStreams(Document queryPlan) throws XPathExpressionException {
        XPath xpath = xPathfactory.newXPath();
        String xPathExpression = "/" + EventProcessorConstants.STORM_QUERY_PLAN + "/" + EventProcessorConstants.EVENT_RECEIVER
                + "/" + EventProcessorConstants.STREAMS + "/" + EventProcessorConstants.STREAM;

        XPathExpression expr = xpath.compile(xPathExpression);
        NodeList list = (NodeList) expr.evaluate(queryPlan, XPathConstants.NODESET);

        Set<String> result = new HashSet<>();
        for (int i = 0; i < list.getLength(); i++){
            Node node = list.item(i);
            result.add(node.getTextContent());
        }

        return result;
    }

    /**
     *  Fetch all output stream definitions of all event-processor's elements
     */
    private static Set<String> getEventProcessorOutputStreams(Document queryPlan) throws XPathExpressionException {
        XPath xpath = xPathfactory.newXPath();
        String xPathExpression = "/" + EventProcessorConstants.STORM_QUERY_PLAN + "/" + EventProcessorConstants.EVENT_PROCESSOR_TAG
                + "/" + EventProcessorConstants.OUTPUT_STREAMS + "/" + EventProcessorConstants.STREAM;

        XPathExpression expr = xpath.compile(xPathExpression);
        NodeList list = (NodeList) expr.evaluate(queryPlan, XPathConstants.NODESET);

        Set<String> result = new HashSet<>();
        for (int i = 0; i < list.getLength(); i++){
            Node node = list.item(i);
            result.add(node.getTextContent());
        }

        return result;
    }


    /**
     *  Fetch input stream definitions of event-publisher element
     */
    private static Set<String> getEventPublisherInputStreams(Document queryPlan) throws XPathExpressionException {
        XPath xpath = xPathfactory.newXPath();
        String xPathExpression = "/" + EventProcessorConstants.STORM_QUERY_PLAN + "/" + EventProcessorConstants.EVENT_PUBLISHER
                + "/" + EventProcessorConstants.INPUT_STREAMS + "/" + EventProcessorConstants.STREAM;

        XPathExpression expr = xpath.compile(xPathExpression);
        NodeList list = (NodeList) expr.evaluate(queryPlan, XPathConstants.NODESET);

        Set<String> result = new HashSet<>();
        for (int i = 0; i < list.getLength(); i++){
            Node node = list.item(i);
            result.add(node.getTextContent());
        }

        return result;
    }

    private static Set<String> extractStreamIds(Set<String> streamDefinitions){
        Set<String> streamIds = new HashSet<>();
        for (String streamDefinition : streamDefinitions){
            streamIds.add(extractStreamId(streamDefinition));
        }

        return streamIds;
    }

    private static String extractStreamId(String streamDefinition){
        return streamDefinition.split(" ")[2];
    }
}
