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
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EventProcessorHelper {

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
            executionPlanConfiguration.setExecutionPlan(((OMElement) queryIterator.next()).getText());
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
        factory.createOMText(queries, executionPlanConfiguration.getExecutionPlan(),
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

    /**
     * Returns the execution plan name
     * @param executionPlanAsString executionPlan (taken from code mirror) as a string
     * @return execution plan name as given in @Plan:name('MyPlanName'). Returns null in the absence of @Plan:name('MyPlanName')
     */
    public static String getExecutionPlanName(String executionPlanAsString){
        String executionPlanName = null;
        ExecutionPlan executionPlan = SiddhiCompiler.parse(executionPlanAsString);
        executionPlanName = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME, null, executionPlan.getAnnotations()).getValue();
        return executionPlanName;
    }

    public static void validateExecutionPlan(String executionPlan, int tenantId) throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {
        String planName;
        int i = 0;      //this is maintained for giving more context info in error messages, when throwing exceptions.
        ArrayList<String> importedStreams = new ArrayList<String>();
        ArrayList<String> exportedStreams = new ArrayList<String>();
        Pattern databridgeStreamNamePattern = Pattern.compile(EventProcessorConstants.DATABRIDGE_STREAM_REGEX);
        Pattern streamVersionPattern = Pattern.compile(EventProcessorConstants.STREAM_VER_REGEX);

        ExecutionPlan parsedExecPlan = SiddhiCompiler.parse(executionPlan);
        Element element = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_NAME_NAME, null, parsedExecPlan.getAnnotations());
        if (element == null) {                                                                        // check if plan name is given
            throw new ExecutionPlanConfigurationException("Execution plan name is not given. Please specify execution plan name using the annotation " +
                    "'@Plan:name('executionPlanNameHere')");
        }
        planName = element.getValue();
        if (planName.equals("")) {
            throw new ExecutionPlanConfigurationException("Execution plan name is empty. Hence the plan is invalid");
        }
        if (planName.trim().contains(" ")) {
            throw new ExecutionPlanConfigurationException("Execution plan name '" + planName + "' contains whitespaces. Please remove whitespaces.");
        }

        for (Map.Entry<String, org.wso2.siddhi.query.api.definition.StreamDefinition> entry : parsedExecPlan.getStreamDefinitionMap().entrySet()) {
            Element importElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_IMPORT, null, entry.getValue().getAnnotations());
            Element exportElement = AnnotationHelper.getAnnotationElement(EventProcessorConstants.ANNOTATION_EXPORT, null, entry.getValue().getAnnotations());
            if (importElement == null && exportElement == null) {                                        // check if each stream definition has either import or export annotation
                throw new ExecutionPlanConfigurationException("Missing required annotation in " + i + "th of the " + parsedExecPlan.getStreamDefinitionMap().size() +
                        "stream definition, with stream id '" + entry.getKey() + "'. Stream definition should have" +
                        " either @Import or @Export annotation.");
            }
            if (importElement != null) {                              //Treating import & export cases separately to give more specific error messages.
                String importElementValue = importElement.getValue();
                if (importElementValue == "") {
                    throw new ExecutionPlanConfigurationException("Imported stream cannot be empty as in '@Import('')'. " +
                            "Please correct " + i + "th of the " + parsedExecPlan.getStreamDefinitionMap().size() +
                            "stream definition, with stream id '" + entry.getKey());
                }
                String[] streamIdComponents = importElementValue.split(EventProcessorConstants.STREAM_SEPARATOR);
                if (streamIdComponents.length != 2) {
                    throw new ExecutionPlanConfigurationException("Found malformed @Import element '" + importElementValue + "'. " +
                            "@Import annotation should take the form '@Import('streamName:StreamVersion')'. " +
                            "There should be one colon, separating the streamName and its version");
                }
                if ((!databridgeStreamNamePattern.matcher(streamIdComponents[0].trim()).matches())) {
                    throw new ExecutionPlanConfigurationException("Invalid imported stream name[" + streamIdComponents[0] + "] in execution plan:" + planName +
                            ". Stream name should match the regex '" + EventProcessorConstants.DATABRIDGE_STREAM_REGEX + "'");
                }
                Matcher m = streamVersionPattern.matcher(streamIdComponents[1].trim());
                if (!m.matches()) {
                    throw new ExecutionPlanConfigurationException("Invalid stream version [" + streamIdComponents[1] + "] for stream name " + streamIdComponents[0] + " in execution plan: " + planName +
                            ". Stream version should match the regex '" + EventProcessorConstants.STREAM_VER_REGEX + "'");
                }
                validateIfStreamExists(streamIdComponents[0], streamIdComponents[1], tenantId);     // check if each Imported/Exported stream has actually being defined
                if (exportedStreams.contains(importElementValue)) {                                   // check if same stream has been imported and exported.
                    throw new ExecutionPlanConfigurationException("Imported stream '" + importElementValue + "' is also among the exported streams. Hence the execution plan is invalid");
                }
                importedStreams.add(importElementValue);
            } else {
                String exportElementValue = exportElement.getValue();
                if (exportElementValue == "") {
                    throw new ExecutionPlanConfigurationException("Exported stream cannot be empty as in '@Export('')'. " +
                            "Please correct " + i + "th of the " + parsedExecPlan.getStreamDefinitionMap().size() +
                            "stream definition, with stream id '" + entry.getKey());
                }
                String[] streamIdComponents = exportElementValue.split(EventProcessorConstants.STREAM_SEPARATOR);
                if (streamIdComponents.length != 2) {
                    throw new ExecutionPlanConfigurationException("Found malformed @Export element '" + exportElementValue + "'. " +
                            "@Export annotation should take the form '@Export('streamName:StreamVersion')'. " +
                            "There should be one colon, separating the streamName and its version");
                }
                if ((!databridgeStreamNamePattern.matcher(streamIdComponents[0].trim()).matches())) {
                    throw new ExecutionPlanConfigurationException("Invalid exported stream name[" + streamIdComponents[0] + "] in execution plan:" + planName +
                            ". Stream name should match the regex '" + EventProcessorConstants.DATABRIDGE_STREAM_REGEX + "'");
                }
                Matcher m = streamVersionPattern.matcher(streamIdComponents[1].trim());
                if (!m.matches()) {
                    throw new ExecutionPlanConfigurationException("Invalid stream version [" + streamIdComponents[1] + "] for stream name " + streamIdComponents[0] + " in execution plan: " + planName +
                            ". Stream version should match the regex '" + EventProcessorConstants.STREAM_VER_REGEX + "'");
                }
                validateIfStreamExists(streamIdComponents[0], streamIdComponents[1], tenantId);
                if (importedStreams.contains(exportElementValue)) {
                    throw new ExecutionPlanConfigurationException("Exported stream '" + exportElementValue + "' is also among the imported streams. Hence the execution plan is invalid");
                }
                exportedStreams.add(exportElementValue);
            }
            i++;
        }
    }


    private static boolean validateIfStreamExists(String streamName, String streamVersion,
                                                  int tenantId)
            throws ExecutionPlanConfigurationException, ExecutionPlanDependencyValidationException {

        EventStreamService eventStreamService = EventProcessorValueHolder.getEventStreamService();
        try {
            StreamDefinition streamDefinition = eventStreamService.getStreamDefinition(streamName, streamVersion);
            if (streamDefinition != null) {
                return true;
            }
        } catch (EventStreamConfigurationException e) {
            throw new ExecutionPlanConfigurationException("Error while validating stream definition with store : " + e.getMessage(), e);
        }
        throw new ExecutionPlanDependencyValidationException(streamName + ":" + streamVersion, "Stream " + streamName + ":" + streamVersion + " does not exist");


    }

    /**
     * Sets an annotation name for a given execution plan to be true or false.
     * For example, when an execution plan has the statement "@Plan:statistics('false')" and false need to be set to true,
     * then this helper method can be used.
     * @param executionPlan Existing execution plan, either having the annotation name set to be true/false,
     *                      or the annotation name is not present in the execution plan at all.
     * @param annotationName The annotation name which needs to be set to true/false.
     *                       For example, in Siddhi statement @Plan:name('false'), 'name' will be the annotation name.
     * @param isAnnotationNameTrue Whether the annotation name need to be set to true or false.
     * @return New execution plan with the given plan annotation name set to be true.
     */
    public static String setExecutionPlanAnnotationName(String executionPlan, String annotationName, boolean isAnnotationNameTrue){
        String newExecutionPlan = null;
        String planHeader = "";
        String planBody = "";
        String planHeaderLineRegex = EventProcessorConstants.PLAN_HEADER_LINE_REGEX;

        String regexToBeReplaced = "^\\s*"+        //beginning of line with zero or more whitespaces
                EventProcessorConstants.ANNOTATION_TOKEN_AT +
                EventProcessorConstants.ANNOTATION_PLAN +
                EventProcessorConstants.ANNOTATION_TOKEN_COLON +
                annotationName +
                "\\" + EventProcessorConstants.ANNOTATION_TOKEN_OPENING_BRACKET +    //bracket is escaped, because the literal is meant.
                EventProcessorConstants.SIDDHI_SINGLE_QUOTE + !isAnnotationNameTrue + EventProcessorConstants.SIDDHI_SINGLE_QUOTE +
                "\\" + EventProcessorConstants.ANNOTATION_TOKEN_CLOSING_BRACKET;     //bracket is escaped, because the literal is meant.

        String replacement = EventProcessorConstants.ANNOTATION_TOKEN_AT +
                EventProcessorConstants.ANNOTATION_PLAN +
                EventProcessorConstants.ANNOTATION_TOKEN_COLON +
                annotationName +
                EventProcessorConstants.ANNOTATION_TOKEN_OPENING_BRACKET +
                EventProcessorConstants.SIDDHI_SINGLE_QUOTE + isAnnotationNameTrue + EventProcessorConstants.SIDDHI_SINGLE_QUOTE +
                EventProcessorConstants.ANNOTATION_TOKEN_CLOSING_BRACKET;

        Matcher matcher = Pattern.compile(regexToBeReplaced, Pattern.MULTILINE).matcher(executionPlan);

        if(matcher.find()){   //statement with annotation name set to false, is already in the plan; In that case, false will be replaced with true.

            //finding the whitespaces given by the user before "@Plan:name()" statement and prepending those at replacement.
            String[] matchSplitArray = matcher.group().split(EventProcessorConstants.ANNOTATION_TOKEN_AT);
            String whitespaces = "";
            if(matchSplitArray.length > 1){
                whitespaces += matchSplitArray[0];
            }

            replacement = whitespaces + replacement;
            newExecutionPlan = matcher.replaceFirst(replacement);

        } else {       //statement with annotation name is not there in the plan; it'll be inserted.
            String[] planHeaderArray = executionPlan.split(EventProcessorConstants.SIDDHI_LINE_SEPARATER);
            for(int i=0; i<planHeaderArray.length; i++){
                if(planHeaderArray[i].matches(planHeaderLineRegex)){
                    if(planHeaderArray[i].matches(EventProcessorConstants.END_OF_PLAN_HEADER_COMMENT_REGEX)){
                        break;
                    }
                    planHeader += planHeaderArray[i] + EventProcessorConstants.SIDDHI_LINE_SEPARATER;
                } else {
                    break;
                }
            }
            planBody = executionPlan.replace(planHeader, "");
            newExecutionPlan = planHeader + replacement + EventProcessorConstants.SIDDHI_LINE_SEPARATER +
                    EventProcessorConstants.SIDDHI_LINE_SEPARATER + planBody;
        }
        return newExecutionPlan;
    }
}
