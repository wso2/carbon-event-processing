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
package org.wso2.carbon.event.processor.core.internal.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExcecutionPlanRuntimeException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.internal.ds.EventProcessorValueHolder;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAttributeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventProcessorUtil {
    private static Log log = LogFactory.getLog(EventProcessorUtil.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    public static StreamDefinition convertToDatabridgeStreamDefinition(
            org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition,
            StreamConfiguration streamConfiguration) {
        StreamDefinition databridgeDefinition = null;
        try {
            databridgeDefinition = new StreamDefinition(streamConfiguration.getName(), streamConfiguration.getVersion());
        } catch (MalformedStreamDefinitionException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        List<org.wso2.carbon.databridge.commons.Attribute> payload = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();
        List<org.wso2.carbon.databridge.commons.Attribute> meta = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();
        List<org.wso2.carbon.databridge.commons.Attribute> correlation = new ArrayList<org.wso2.carbon.databridge.commons.Attribute>();
        if (siddhiStreamDefinition.getAttributeList() != null) {
            for (Attribute attribute : siddhiStreamDefinition.getAttributeList()) {
                if (attribute.getName().startsWith("meta_")) {
                    meta.add(convertToDatabridgeAttribute(attribute, "meta_"));
                } else if (attribute.getName().startsWith("correlation_")) {
                    correlation.add(convertToDatabridgeAttribute(attribute, "correlation_"));
                } else {
                    payload.add(convertToDatabridgeAttribute(attribute, null));

                }
            }
        }
        if (!payload.isEmpty()) {
            databridgeDefinition.setPayloadData(payload);
        }
        if (!meta.isEmpty()) {
            databridgeDefinition.setMetaData(meta);
        }
        if (!correlation.isEmpty()) {
            databridgeDefinition.setCorrelationData(correlation);
        }
        return databridgeDefinition;
    }

    public static org.wso2.siddhi.query.api.definition.StreamDefinition convertToSiddhiStreamDefinition(
            StreamDefinition streamDefinition, String siddhiStreamName) throws EventStreamConfigurationException {
        org.wso2.siddhi.query.api.definition.StreamDefinition siddhiStreamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
        siddhiStreamDefinition.setId(siddhiStreamName);
        try {
            if (streamDefinition.getMetaData() != null) {
                for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getMetaData()) {
                    Attribute siddhiAttribute = convertToSiddhiAttribute(attribute, EventProcessorConstants.META + EventProcessorConstants.ATTRIBUTE_SEPARATOR);
                    siddhiStreamDefinition.attribute(siddhiAttribute.getName(), siddhiAttribute.getType());
                }
            }

            if (streamDefinition.getCorrelationData() != null) {
                for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getCorrelationData()) {
                    Attribute siddhiAttribute = convertToSiddhiAttribute(attribute, EventProcessorConstants.CORRELATION + EventProcessorConstants.ATTRIBUTE_SEPARATOR);
                    siddhiStreamDefinition.attribute(siddhiAttribute.getName(), siddhiAttribute.getType());
                }
            }

            if (streamDefinition.getPayloadData() != null) {
                for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getPayloadData()) {
                    siddhiStreamDefinition.attribute(attribute.getName(), convertToSiddhiAttribute(attribute, "").getType());
                }
            }

            return siddhiStreamDefinition;
        } catch (DuplicateAttributeException ex) {
            throw new EventStreamConfigurationException(ex.getMessage(), ex);
        }

    }

    public static org.wso2.carbon.databridge.commons.Attribute convertToDatabridgeAttribute(
            Attribute attribute, String prefixToDrop) {
        AttributeType type;
        switch (attribute.getType()) {
            case LONG:
                type = AttributeType.LONG;
                break;
            case INT:
                type = AttributeType.INT;
                break;
            case FLOAT:
                type = AttributeType.FLOAT;
                break;
            case DOUBLE:
                type = AttributeType.DOUBLE;
                break;
            case BOOL:
                type = AttributeType.BOOL;
                break;
            default:
                type = AttributeType.STRING;
                break;
        }
        String name = (prefixToDrop != null) ? attribute.getName().replaceFirst(prefixToDrop, "") : attribute.getName();
        return new org.wso2.carbon.databridge.commons.Attribute(name, type);

    }

    public static Attribute convertToSiddhiAttribute(org.wso2.carbon.databridge.commons.Attribute attribute, String prefix) {
        Attribute.Type type;
        switch (attribute.getType()) {
            case LONG:
                type = Attribute.Type.LONG;
                break;
            case INT:
                type = Attribute.Type.INT;
                break;
            case FLOAT:
                type = Attribute.Type.FLOAT;
                break;
            case DOUBLE:
                type = Attribute.Type.DOUBLE;
                break;
            case BOOL:
                type = Attribute.Type.BOOL;
                break;
            default:
                type = Attribute.Type.STRING;
                break;
        }
        return new Attribute(prefix + attribute.getName(), type);

    }

    public static String formatXml(String unformattedXml) throws
            ExecutionPlanConfigurationException {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException(e);
        }
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        org.apache.xerces.util.SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }


    private static Document parseXmlFile(String in) throws ExecutionPlanConfigurationException {
        try {
            DocumentBuilderFactory dbf = getSecuredDocumentBuilder();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new ExecutionPlanConfigurationException(e);
        } catch (SAXException e) {
            throw new ExecutionPlanConfigurationException(e);
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException(e);
        }
    }

    public static String getStreamId(String streamName, String version) {
        return streamName + EventProcessorConstants.STREAM_SEPARATOR + version;
    }

    public static String getStreamName(String streamId) {
        return streamId.split(EventProcessorConstants.STREAM_SEPARATOR)[0];
    }

    public static String getDefinitionString(StreamDefinition streamDefinition, String siddhiStreamName) {
        StringBuilder builder = new StringBuilder();
        builder.append(EventProcessorConstants.DEFINE_STREAM);
        builder.append(siddhiStreamName);
        builder.append(EventProcessorConstants.OPENING_BRACKETS);
        if (streamDefinition.getMetaData() != null) {
            for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getMetaData()) {
                builder.append(EventProcessorConstants.META + EventProcessorConstants.ATTRIBUTE_SEPARATOR + attribute
                        .getName() + EventProcessorConstants.SPACE + attribute.getType().toString().toLowerCase() +
                        EventProcessorConstants.COMMA);
            }
        }

        if (streamDefinition.getCorrelationData() != null) {
            for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getCorrelationData()) {
                builder.append(EventProcessorConstants.CORRELATION + EventProcessorConstants.ATTRIBUTE_SEPARATOR + attribute
                        .getName() + EventProcessorConstants.SPACE + attribute.getType().toString().toLowerCase() +
                        EventProcessorConstants.COMMA);
            }
        }

        if (streamDefinition.getPayloadData() != null) {
            for (org.wso2.carbon.databridge.commons.Attribute attribute : streamDefinition.getPayloadData()) {
                builder.append(attribute.getName() + EventProcessorConstants.SPACE + attribute.getType().toString()
                        .toLowerCase() + EventProcessorConstants.COMMA);
            }
        }
        builder.deleteCharAt(builder.length() - 2);         //remove last comma
        builder.append(EventProcessorConstants.CLOSING_BRACKETS);
        return builder.toString();
    }

    /**
     * Construct Stream Definition query string for a given Siddhi Stream Definition
     *
     * @param siddhiStreamDefinition
     * @return
     */
    public static String getDefinitionString(org.wso2.siddhi.query.api.definition.AbstractDefinition siddhiStreamDefinition) {
        StringBuilder builder = new StringBuilder();
        builder.append(EventProcessorConstants.DEFINE_STREAM);
        builder.append(siddhiStreamDefinition.getId());
        builder.append(EventProcessorConstants.OPENING_BRACKETS);
        for (Attribute attribute : siddhiStreamDefinition.getAttributeList()) {
            builder.append(attribute.getName() + EventProcessorConstants.SPACE + attribute.getType().toString().toLowerCase() +
                    EventProcessorConstants.COMMA);
        }
        builder.deleteCharAt(builder.length() - 2);         //remove last comma
        builder.append(EventProcessorConstants.CLOSING_BRACKETS);
        return builder.toString();
    }

    public static String getTriggerDefinitionString(TriggerDefinition triggerDefinition){
        StringBuilder builder = new StringBuilder();
        builder.append(EventProcessorConstants.DEFINE_TRIGGER);
        builder.append(triggerDefinition.getId());


        Long atEvery = triggerDefinition.getAtEvery();
        if (atEvery != null){
            builder.append(EventProcessorConstants.TRIGGER_AT_EVERY);
            builder.append(atEvery/1000); // All time units are in milliseconds in trigger definition object. Convert it into seconds
            builder.append(EventProcessorConstants.SECOND);
        } else{
            builder.append(EventProcessorConstants.TRIGGER_AT);
            builder.append("'" + triggerDefinition.getAt() + "'");
        }

        builder.append(EventProcessorConstants.SEMICOLEN);
        return builder.toString();
    }

    /**
     * Constructs full query expression as String
     *
     * @param importDefinitions List of imported definitions
     * @param exportDefinitions List of exported definitions
     * @param queryExpressions  query expression given in the ExecutionPlanConfiguration
     * @return
     */
    public static String constructQueryExpression(String executionPlanName, List<String> importDefinitions, List<String> exportDefinitions,
                                                  String queryExpressions) {
        StringBuilder builder = new StringBuilder();

        if (executionPlanName != null && executionPlanName.length() > 0) {
            builder.append("@plan:").append(SiddhiConstants.ANNOTATION_NAME).append("('").append(executionPlanName).append("')");
        }
        for (String definition : importDefinitions) {
            builder.append(definition);
        }

        for (String definition : exportDefinitions) {
            builder.append(definition);
        }
        builder.append(queryExpressions);
        return builder.toString();
    }

    public static AxisConfiguration getAxisConfiguration() {
        AxisConfiguration axisConfiguration = null;
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfiguration = EventProcessorValueHolder.getConfigurationContext().
                    getServerConfigContext().getAxisConfiguration();
        } else {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            ConfigurationContext configurationContext = EventProcessorValueHolder.getTenantConfig(tenantId);
            if (configurationContext != null) {
                axisConfiguration = configurationContext.getAxisConfiguration();
            } else {
                throw new ExcecutionPlanRuntimeException("Tenant configuration not found for tenant id: " + tenantId);
            }
        }
        return axisConfiguration;
    }

    public static Event getWso2Event(org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition, int metaAttrCount,
                                     int correlationAttrCount, int payloadAttrCount, long timestamp, Object[] data) {
        int dataLength = data.length;
        Object[] metaAttrArray = new Object[metaAttrCount];
        Object[] correlationAttrArray = new Object[correlationAttrCount];
        Object[] payloadAttrArray = new Object[payloadAttrCount];
        Map<String, String> arbitraryDataMap = null;
        if ((metaAttrCount + correlationAttrCount + payloadAttrCount + 1) == dataLength) {
            dataLength = data.length - 1;
            arbitraryDataMap = (Map<String, String>) data[dataLength];
        }
        int attrIndex = 0;
        for (int i = 0; i < metaAttrCount; i++) {
            metaAttrArray[i] = data[attrIndex++];
        }
        for (int j = 0; j < correlationAttrCount; j++) {
            correlationAttrArray[j] = data[attrIndex++];
        }
        for (int k = 0; k < payloadAttrCount; k++) {
            payloadAttrArray[k] = data[attrIndex++];
        }

        return new Event(streamDefinition.getStreamId(), timestamp, metaAttrArray, correlationAttrArray, payloadAttrArray, arbitraryDataMap);
    }

    public static List<Event> getWso2Events(org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition,
                                            int metaAttrCount, int correlationAttrCount, int payloadAttrCount,
                                            org.wso2.siddhi.core.event.Event[] events) {
        List<Event> eventList = new ArrayList<>();
        for (org.wso2.siddhi.core.event.Event event : events) {
            eventList.add(getWso2Event(streamDefinition, metaAttrCount, correlationAttrCount, payloadAttrCount,
                    event.getTimestamp(), event.getData()));
        }

        return eventList;
    }

    public static void validatePath(String fileName) throws ExecutionPlanConfigurationException {
        if (fileName.contains("../") || fileName.contains("..\\")) {
            throw new ExecutionPlanConfigurationException("File name contains restricted path elements. " + fileName);
        }
    }
}
