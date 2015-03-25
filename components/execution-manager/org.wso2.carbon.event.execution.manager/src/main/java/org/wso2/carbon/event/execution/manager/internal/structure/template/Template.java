/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.execution.manager.internal.structure.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Template complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Template">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ConditionParameters" type="{}ConditionParameters"/>
 *         &lt;element name="Parameters" type="{}Parameters"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Template", propOrder = {
        "conditionParameters",
        "parameters"
})
public class Template {

    @XmlElement(name = "ConditionParameters", required = true)
    protected ConditionParameters conditionParameters;
    @XmlElement(name = "Parameters", required = true)
    protected Parameters parameters;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Gets the value of the conditionParameters property.
     *
     * @return
     *     possible object is
     *     {@link ConditionParameters }
     *
     */
    public ConditionParameters getConditionParameters() {
        return conditionParameters;
    }

    /**
     * Sets the value of the conditionParameters property.
     *
     * @param value
     *     allowed object is
     *     {@link ConditionParameters }
     *
     */
    public void setConditionParameters(ConditionParameters value) {
        this.conditionParameters = value;
    }

    /**
     * Gets the value of the parameters property.
     *
     * @return
     *     possible object is
     *     {@link Parameters }
     *
     */
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     *
     * @param value
     *     allowed object is
     *     {@link Parameters }
     *
     */
    public void setParameters(Parameters value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
    }

}
