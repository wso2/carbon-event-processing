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
package org.wso2.carbon.event.builder.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderProcessingException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderStreamValidationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.ReflectionBasedObjectSupplier;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathData;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import javax.xml.stream.XMLStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XMLInputMapper implements InputMapper {

    private static final Log log = LogFactory.getLog(XMLInputMapper.class);
    private EventBuilderConfiguration eventBuilderConfiguration = null;
    private List<XPathData> attributeXpathList = null;
    private List<XPathDefinition> xPathDefinitions = null;
    private ReflectionBasedObjectSupplier reflectionBasedObjectSupplier = new ReflectionBasedObjectSupplier();
    private AXIOMXPath parentSelectorXpath = null;

    public XMLInputMapper(EventBuilderConfiguration eventBuilderConfiguration,
                          StreamDefinition exportedStreamDefinition)
            throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;

        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof XMLInputMapping) {
            XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
            if (xmlInputMapping.isCustomMappingEnabled()) {
                try {

                    List<XPathDefinition> xPathDefinitions = xmlInputMapping.getXPathDefinitions();
                    XPathData[] xpathDataArray = new XPathData[xmlInputMapping.getInputMappingAttributes().size()];
                    for (InputMappingAttribute inputMappingAttribute : xmlInputMapping.getInputMappingAttributes()) {
                        String xpathExpr = inputMappingAttribute.getFromElementKey();

                        AXIOMXPath xpath = new AXIOMXPath(xpathExpr);
                        for (XPathDefinition xPathDefinition : xPathDefinitions) {
                            if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                                xpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                            }
                        }
                        String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(inputMappingAttribute.getToElementType());
                        int position = EventBuilderUtil.getAttributePosition(inputMappingAttribute.getToElementKey(), exportedStreamDefinition);
                        if (position < 0 || position > xpathDataArray.length) {
                            throw new EventBuilderStreamValidationException("Could not determine the stream position for attribute : "
                                    + inputMappingAttribute.getToElementKey() + " in stream exported by event builder "
                                    + exportedStreamDefinition.getStreamId(),exportedStreamDefinition.getStreamId());
                        }
                        xpathDataArray[position] = new XPathData(xpath, type, inputMappingAttribute.getDefaultValue());
                    }
                    attributeXpathList = Arrays.asList(xpathDataArray);
                    if (xmlInputMapping.getParentSelectorXpath() != null && !xmlInputMapping.getParentSelectorXpath().isEmpty()) {
                        this.parentSelectorXpath = new AXIOMXPath(xmlInputMapping.getParentSelectorXpath());
                        for (XPathDefinition xPathDefinition : xPathDefinitions) {
                            if (xPathDefinition != null && !xPathDefinition.isEmpty()) {
                                this.parentSelectorXpath.addNamespace(xPathDefinition.getPrefix(), xPathDefinition.getNamespaceUri());
                            }
                        }
                    }
                } catch (JaxenException e) {
                    throw new EventBuilderConfigurationException("Error parsing XPath expression: " + e.getMessage(), e);
                }
            } else {

                try {
                    this.parentSelectorXpath = new AXIOMXPath("//" + EventBuilderConstants.MULTIPLE_EVENTS_PARENT_TAG);
                    attributeXpathList = new ArrayList<XPathData>();
                    if (exportedStreamDefinition.getMetaData() != null) {
                        for (Attribute attribute : exportedStreamDefinition.getMetaData()) {
                            String xpathExpr = "//" + EventBuilderConstants.EVENT_META_TAG;
                            AXIOMXPath xpath = new AXIOMXPath(xpathExpr + "/" + attribute.getName());
                            String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(attribute.getType());
                            attributeXpathList.add(new XPathData(xpath, type, null));

                        }
                    }

                    if (exportedStreamDefinition.getCorrelationData() != null) {
                        for (Attribute attribute : exportedStreamDefinition.getCorrelationData()) {
                            String xpathExpr = "//" + EventBuilderConstants.EVENT_CORRELATION_TAG;
                            AXIOMXPath xpath = new AXIOMXPath(xpathExpr + "/" + attribute.getName());
                            String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(attribute.getType());
                            attributeXpathList.add(new XPathData(xpath, type, null));

                        }
                    }

                    if (exportedStreamDefinition.getPayloadData() != null) {
                        for (Attribute attribute : exportedStreamDefinition.getPayloadData()) {
                            String xpathExpr = "//" + EventBuilderConstants.EVENT_PAYLOAD_TAG;
                            AXIOMXPath xpath = new AXIOMXPath(xpathExpr + "/" + attribute.getName());
                            String type = EventBuilderConstants.ATTRIBUTE_TYPE_CLASS_TYPE_MAP.get(attribute.getType());
                            attributeXpathList.add(new XPathData(xpath, type, null));

                        }
                    }
                } catch (JaxenException e) {
                    throw new EventBuilderConfigurationException("Error parsing XPath expression: " + e.getMessage(), e);
                }
            }
        }

    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderProcessingException {
        if (this.parentSelectorXpath != null) {
            return processMultipleEvents(obj);
        } else {
            return processSingleEvent(obj);
        }
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderProcessingException {
        if (this.parentSelectorXpath != null) {
            return processMultipleEvents(obj);
        } else {
            return processSingleEvent(obj);
        }
    }

    @Override
    public Attribute[] getOutputAttributes() {
        XMLInputMapping xmlInputMapping = (XMLInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = xmlInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

    private Object[][] processMultipleEvents(Object obj) throws EventBuilderProcessingException {
        if (obj instanceof String) {
            String textMessage = (String) obj;
            try {
                obj = AXIOMUtil.stringToOM(textMessage);
            } catch (XMLStreamException e) {
                throw new EventBuilderProcessingException("Error parsing incoming XML event : " + e.getMessage(), e);
            }
        }
        if (obj instanceof OMElement) {
            OMElement events;
            try {
                events = (OMElement) this.parentSelectorXpath.selectSingleNode(obj);
                if(events==null){
                    throw new RuntimeException("Parent Selector XPath \""+parentSelectorXpath.toString()+"\" cannot be processed on event:"+ obj.toString());
                }

                List<Object[]> objArrayList = new ArrayList<Object[]>();
                Iterator childIterator = events.getChildElements();
                while (childIterator.hasNext()) {
                    Object eventObj = childIterator.next();
                    objArrayList.add(processSingleEvent(eventObj));
                    /**
                     * Usually the global lookup '//' is used in the XPATH expression which works fine for 'single event mode'.
                     * However, if global lookup is used, it will return the first element from the whole document as specified in
                     * XPATH-2.0 Specification. Therefore the same XPATH expression that works fine in 'single event mode' will
                     * always return the first element of a batch in 'batch mode'. Therefore to return what the
                     * user expects, each child element is removed after sending to simulate an iteration for the
                     * global lookup.
                     */
                    childIterator.remove();
                }
                return objArrayList.toArray(new Object[objArrayList.size()][]);
            } catch (JaxenException e) {
                throw new EventBuilderProcessingException("Unable to parse XPath for parent selector: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private Object[] processSingleEvent(Object obj) throws EventBuilderProcessingException {
        Object[] outObjArray = null;
        OMElement eventOMElement = null;
        if (obj instanceof String) {
            String textMessage = (String) obj;
            try {
                eventOMElement = AXIOMUtil.stringToOM(textMessage);
            } catch (XMLStreamException e) {
                throw new EventBuilderProcessingException("Error parsing incoming XML event : " + e.getMessage(), e);
            }
        } else if (obj instanceof OMElement) {
            eventOMElement = (OMElement) obj;
        }

        if (eventOMElement != null) {
            OMNamespace omNamespace = null;
            if (this.xPathDefinitions == null || this.xPathDefinitions.isEmpty()) {
                omNamespace = eventOMElement.getNamespace();
            }
            List<Object> objList = new ArrayList<Object>();
            for (XPathData xpathData : attributeXpathList) {
                AXIOMXPath xpath = xpathData.getXpath();
                OMElement omElementResult = null;
                String type = xpathData.getType();
                try {
                    if (omNamespace != null) {
                        xpath.addNamespaces(eventOMElement);
                    }
                    omElementResult = (OMElement) xpath.selectSingleNode(eventOMElement);
                    Class<?> beanClass = Class.forName(type);
                    Object returnedObj = null;
                    if (omElementResult != null) {
                        returnedObj = BeanUtil.deserialize(beanClass,
                                omElementResult, reflectionBasedObjectSupplier, null);
                    } else if (xpathData.getDefaultValue() != null) {
                        if (!beanClass.equals(String.class)) {
                            Class<?> stringClass = String.class;
                            Method valueOfMethod = beanClass.getMethod("valueOf", stringClass);
                            returnedObj = valueOfMethod.invoke(null, xpathData.getDefaultValue());
                        } else {
                            returnedObj = xpathData.getDefaultValue();
                        }
//                        throw new  EventBuilderProcessingException ("Unable to parse XPath to retrieve required attribute. Sending defaults.");
//                        log.warn();
                    } else {
                        throw new  EventBuilderProcessingException ("Unable to parse XPath "+xpathData.getXpath()+" to retrieve required attribute.");
                    }
                    objList.add(returnedObj);
                } catch (JaxenException e) {
                    throw new EventBuilderProcessingException("Error parsing xpath for " + xpath, e);
                } catch (ClassNotFoundException e) {
                    throw new EventBuilderProcessingException("Cannot find specified class for type " + type);
                } catch (AxisFault axisFault) {
                    throw new EventBuilderProcessingException("Error de-serializing OMElement " + omElementResult, axisFault);
                } catch (NoSuchMethodException e) {
                    throw new EventBuilderProcessingException("Error trying to convert default value to specified target type.", e);
                } catch (InvocationTargetException e) {
                    throw new EventBuilderProcessingException("Error trying to convert default value to specified target type.", e);
                } catch (IllegalAccessException e) {
                    throw new EventBuilderProcessingException("Error trying to convert default value to specified target type.", e);
                }
            }
            outObjArray = objList.toArray(new Object[objList.size()]);
        }
        return outObjArray;
    }
}
