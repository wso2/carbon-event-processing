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
package org.wso2.carbon.event.processor.core.internal.util.helper;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EventProcessorConfigurationHelper {

    public static final String SIDDHI_STREAM_REGEX = "[a-zA-Z0-9_]+";
    public static final String DATABRIDGE_STREAM_REGEX = "[a-zA-Z0-9_\\.]+";
    public static final String STREAM_VER_REGEX = "([0-9]*)\\.([0-9]*)\\.([0-9]*)";

    public static ExecutionPlanConfiguration fromOM(OMElement executionPlanConfigElement) throws ExecutionPlanConfigurationException {
        if (!(executionPlanConfigElement.getQName().getLocalPart()).equals(EventProcessorConstants.EP_ELE_ROOT_ELEMENT)) {
            throw new ExecutionPlanConfigurationException("Wrong execution plan configuration file, Invalid root element " + executionPlanConfigElement.getQName());
        }
        ExecutionPlanConfiguration executionPlanConfiguration = new ExecutionPlanConfiguration();
        executionPlanConfiguration.setName(executionPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)));


        Iterator descIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_DESC));
        if (descIterator.hasNext()) {
            OMElement descriptionElement = (OMElement) descIterator.next();
            executionPlanConfiguration.setDescription(descriptionElement.getText());
        }

        Iterator siddhiConfigIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_SIDDHI_CONFIG));
        while (siddhiConfigIterator.hasNext()) {
            Iterator siddhiConfigPropertyIterator = ((OMElement) siddhiConfigIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_PROPERTY));
            while (siddhiConfigPropertyIterator.hasNext()) {
                OMElement configPropertyElement = (OMElement) siddhiConfigPropertyIterator.next();
                executionPlanConfiguration.addSiddhiConfigurationProperty(configPropertyElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), configPropertyElement.getText());
            }
        }

        Iterator allImportedStreamsIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_IMP_STREAMS));
        while (allImportedStreamsIterator.hasNext()) {
            Iterator importedStreamIterator = ((OMElement) allImportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (importedStreamIterator.hasNext()) {
                OMElement importedStream = (OMElement) importedStreamIterator.next();
                String version = importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VERSION));
                StreamConfiguration streamConfiguration;
                if (version != null) {
                    streamConfiguration = new StreamConfiguration(importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), version);
                } else {
                    streamConfiguration = new StreamConfiguration(importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)));
                }
                OMAttribute as = importedStream.getAttribute(new QName(EventProcessorConstants.EP_ATTR_AS));
                if (as != null && as.getAttributeValue() != null && as.getAttributeValue().trim().length() > 0) {
                    streamConfiguration.setSiddhiStreamName(as.getAttributeValue());
                }

                executionPlanConfiguration.addImportedStream(streamConfiguration); // todo validate
            }
        }

        Iterator allExportedStreamsIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_EXP_STREAMS));
        while (allExportedStreamsIterator.hasNext()) {
            Iterator exportedStreamIterator = ((OMElement) allExportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (exportedStreamIterator.hasNext()) {
                OMElement exportedStream = (OMElement) exportedStreamIterator.next();
                StreamConfiguration streamConfiguration = new StreamConfiguration(exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VERSION)), exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VALUEOF)));
                executionPlanConfiguration.addExportedStream(streamConfiguration);
                OMAttribute valueOf = exportedStream.getAttribute(new QName(EventProcessorConstants.EP_ATTR_VALUEOF));
                if (valueOf != null && valueOf.getAttributeValue() != null && valueOf.getAttributeValue().trim().length() > 0) {
                    streamConfiguration.setSiddhiStreamName(valueOf.getAttributeValue());
                }
            }
        }

        Iterator queryIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_QUERIES));
        if (queryIterator.hasNext()) {
            executionPlanConfiguration.setQueryExpressions(((OMElement) queryIterator.next()).getText());
        }

        if (executionPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_STATISTICS)) != null && executionPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_STATISTICS)).equals(EventProcessorConstants.EP_ENABLE)) {
            executionPlanConfiguration.setStatisticsEnabled(true);
        }

        if (executionPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_TRACING)) != null && executionPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_TRACING)).equals(EventProcessorConstants.EP_ENABLE)) {
            executionPlanConfiguration.setTracingEnabled(true);
        }
        return executionPlanConfiguration;
    }

    public static OMElement toOM(ExecutionPlanConfiguration executionPlanConfiguration) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement executionPlan = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_ROOT_ELEMENT));
        executionPlan.declareDefaultNamespace(EventProcessorConstants.EP_CONF_NS);
        executionPlan.addAttribute(EventProcessorConstants.EP_ATTR_NAME, executionPlanConfiguration.getName(), null);

        OMElement description = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_DESC));
        description.setNamespace(executionPlan.getDefaultNamespace());
        description.setText(executionPlanConfiguration.getDescription());
        executionPlan.addChild(description);

        OMElement siddhiConfiguration = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_SIDDHI_CONFIG));
        siddhiConfiguration.setNamespace(executionPlan.getDefaultNamespace());
        for (Map.Entry<String, String> entry : executionPlanConfiguration.getSiddhiConfigurationProperties().entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_PROPERTY));
            propertyElement.setNamespace(executionPlan.getDefaultNamespace());
            propertyElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, entry.getKey(), null);
            propertyElement.setText(entry.getValue());
            siddhiConfiguration.addChild(propertyElement);
        }
        executionPlan.addChild(siddhiConfiguration);

        OMElement importedStreams = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_IMP_STREAMS));
        importedStreams.setNamespace(executionPlan.getDefaultNamespace());
        for (StreamConfiguration stream : executionPlanConfiguration.getImportedStreams()) {
            OMElement streamElement = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_STREAM));
            streamElement.setNamespace(executionPlan.getDefaultNamespace());
            streamElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, stream.getName(), null);
            if (stream.getSiddhiStreamName() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_AS, stream.getSiddhiStreamName(), null);
            }
            if (stream.getVersion() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_VERSION, stream.getVersion(), null);
            }

            importedStreams.addChild(streamElement);
        }
        executionPlan.addChild(importedStreams);

        OMElement queries = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_QUERIES));
        queries.setNamespace(executionPlan.getDefaultNamespace());
        factory.createOMText(queries, executionPlanConfiguration.getQueryExpressions(),
                XMLStreamReader.CDATA);
        executionPlan.addChild(queries);

        OMElement exportedStreams = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_EXP_STREAMS));
        exportedStreams.setNamespace(executionPlan.getDefaultNamespace());
        for (StreamConfiguration stream : executionPlanConfiguration.getExportedStreams()) {
            OMElement streamElement = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_STREAM));
            streamElement.setNamespace(executionPlan.getDefaultNamespace());
            streamElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, stream.getName(), null);
            if (stream.getSiddhiStreamName() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_VALUEOF, stream.getSiddhiStreamName(), null);
            }
            if (stream.getVersion() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_VERSION, stream.getVersion(), null);
            }

            exportedStreams.addChild(streamElement);
        }
        executionPlan.addChild(exportedStreams);

        if (executionPlanConfiguration.isStatisticsEnabled()) {
            executionPlan.addAttribute(EventProcessorConstants.EP_ATTR_STATISTICS, EventProcessorConstants.EP_ENABLE, null);
        } else {
            executionPlan.addAttribute(EventProcessorConstants.EP_ATTR_STATISTICS, EventProcessorConstants.EP_DISABLE, null);
        }

        if (executionPlanConfiguration.isTracingEnabled()) {
            executionPlan.addAttribute(EventProcessorConstants.EP_ATTR_TRACING, EventProcessorConstants.EP_ENABLE, null);
        } else {
            executionPlan.addAttribute(EventProcessorConstants.EP_ATTR_TRACING, EventProcessorConstants.EP_DISABLE, null);
        }

        return executionPlan;
    }

    public static void validateExecutionPlanConfiguration(OMElement executionPlanConfigElement, int tenantId) throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {
        if (!executionPlanConfigElement.getQName().getLocalPart().equals(EventProcessorConstants.EP_ELE_ROOT_ELEMENT)) {
            throw new ExecutionPlanConfigurationException("Invalid root element expected:" + EventProcessorConstants.EP_ELE_ROOT_ELEMENT + " found:" + executionPlanConfigElement.getQName().getLocalPart());
        }

        String name = null;
        try {
            name = executionPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME));
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Execution plan name can't be null.");
        }
        if (name == null) {
            throw new ExecutionPlanConfigurationException("Execution plan name can't be null.");
        } else if (name.trim().contains(" ")) {
            throw new ExecutionPlanConfigurationException("Execution plan name can't have spaces.");
        }

        Iterator descIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_DESC));
        if (!descIterator.hasNext()) {
            throw new ExecutionPlanConfigurationException("No description available:" + name);
        }

        HashMap<String, String> siddhiConfigParams = new HashMap<String, String>();
        Iterator siddhiConfigIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_SIDDHI_CONFIG));
        while (siddhiConfigIterator.hasNext()) {
            Iterator siddhiConfigPropertyIterator = ((OMElement) siddhiConfigIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_PROPERTY));
            while (siddhiConfigPropertyIterator.hasNext()) {
                OMElement configPropertyElement = (OMElement) siddhiConfigPropertyIterator.next();
                siddhiConfigParams.put(configPropertyElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), configPropertyElement.getText());
            }
        }

        int siddhiSnapshotTime = 0;
        try {
            siddhiSnapshotTime = Integer.parseInt(siddhiConfigParams.get(EventProcessorConstants.SIDDHI_SNAPSHOT_INTERVAL).trim());
            if (siddhiSnapshotTime < 0) {
                throw new ExecutionPlanConfigurationException("Invalid Siddhi snapshot time interval in execution plan:" + name);
            }
            // TODO enable when distributed processing is available.
//            Boolean.parseBoolean(siddhiConfigParams.get(EventProcessorConstants.SIDDHI_DISTRIBUTED_PROCESSING).trim());
        } catch (NumberFormatException e) {
            throw new ExecutionPlanConfigurationException("Invalid Siddhi snapshot time interval specified in execution plan : " + name);
        }

        Pattern siddhiStreamNamePattern = Pattern.compile(SIDDHI_STREAM_REGEX);
        Pattern databridgeStreamNamePattern = Pattern.compile(DATABRIDGE_STREAM_REGEX);
        Pattern streamVersionPattern = Pattern.compile(STREAM_VER_REGEX);
        Iterator allImportedStreamsIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_IMP_STREAMS));
        while (allImportedStreamsIterator.hasNext()) {
            Iterator importedStreamIterator = ((OMElement) allImportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (importedStreamIterator.hasNext()) {
                OMElement importedStream = (OMElement) importedStreamIterator.next();
                String version = importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VERSION));
                if (version != null && version.trim().length() > 0) {
                    Matcher m = streamVersionPattern.matcher(version.trim());
                    if (!m.matches()) {
                        throw new ExecutionPlanConfigurationException("Invalid stream version [" + version + "] in execution plan: " + name);
                    }
                }
                String streamName = importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME));
                if (streamName == null || streamName.length() < 1 ||
                        (!databridgeStreamNamePattern.matcher(streamName.trim()).matches())) {
                    throw new ExecutionPlanConfigurationException("Invalid imported stream name[" + streamName + "] in execution plan:" + name);
                }

                validateStreamDetails(streamName, version, tenantId);
                OMAttribute as = importedStream.getAttribute(new QName(EventProcessorConstants.EP_ATTR_AS));
                if (as != null && as.getAttributeValue() != null) {
                    if (!siddhiStreamNamePattern.matcher(as.getAttributeValue().trim()).matches()) {
                        throw new ExecutionPlanConfigurationException("Invalid imported stream name as [" + streamName + "] in execution plan:" + name);
                    }
                }
            }
        }

        Iterator allExportedStreamsIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_EXP_STREAMS));
        while (allExportedStreamsIterator.hasNext()) {
            Iterator exportedStreamIterator = ((OMElement) allExportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (exportedStreamIterator.hasNext()) {
                OMElement exportedStream = (OMElement) exportedStreamIterator.next();

                OMAttribute valueOf = exportedStream.getAttribute(new QName(EventProcessorConstants.EP_ATTR_VALUEOF));
                if (valueOf == null || valueOf.getAttributeValue() == null ||
                        valueOf.getAttributeValue().trim().length() < 1 ||
                        (!siddhiStreamNamePattern.matcher(valueOf.getAttributeValue().trim()).matches())) {
                    throw new ExecutionPlanConfigurationException("Invalid exported stream valueOf [" + valueOf.getAttributeValue() + "] in execution plan:" + name);

                }

                String version = exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VERSION));
                if (version != null && version.trim().length() > 0) {
                    if (!streamVersionPattern.matcher(version.trim()).matches()) {
                        throw new ExecutionPlanConfigurationException("Invalid stream version [" + version + "] in execution plan: " + name);
                    }
                }

                String streamName = exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME));
                if (streamName != null && streamName.length() > 0) {
                    if (!databridgeStreamNamePattern.matcher(streamName.trim()).matches()) {
                        throw new ExecutionPlanConfigurationException("Invalid exported stream name[" + streamName + "] in execution plan:" + name);
                    }
                }

                validateStreamDetails(streamName, version, tenantId);
            }
        }

        Iterator queryIterator = executionPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_QUERIES));
        if (queryIterator.hasNext()) {
            String query = ((OMElement) queryIterator.next()).getText();
            if (query == null || query.trim().length() < 1)
                throw new ExecutionPlanConfigurationException("Invalid execution plan with no queries: " + name);
        } else {
            throw new ExecutionPlanConfigurationException("Invalid execution plan with no queries: " + name);

        }
    }

    public static String getExecutionPlanName(OMElement executionPlanOMElement) {
        return executionPlanOMElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME));
    }


    private static boolean validateStreamDetails(String streamName, String streamVersion,
                                                 int tenantId)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {

        EventStreamService eventStreamService = EventProcessorValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(streamName, streamVersion, tenantId);
            if (streamDefinition != null) {
                return true;
            }
        } catch (EventStreamConfigurationException e) {
            throw new ExecutionPlanConfigurationException("Error while validating stream definition with store : " + e.getMessage(), e);
        }
        throw new ExecutionPlanDependencyValidationException(streamName + ":" + streamVersion, "Stream " + streamName + ":" + streamVersion + " does not exist");


    }
}
