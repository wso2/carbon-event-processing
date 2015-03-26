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
package org.wso2.carbon.event.execution.manager.core.internal.structure.template;
import org.wso2.carbon.event.execution.manager.core.internal.structure.template.parameter.AND;
import org.wso2.carbon.event.execution.manager.core.internal.structure.template.parameter.OR;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConditionParameters complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ConditionParameters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Parameter" type="{}Parameter"/>
 *         &lt;element name="OR" type="{}OR"/>
 *         &lt;element name="AND" type="{}AND"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConditionParameters", propOrder = {
        "parameter",
        "or",
        "and"
})
public class ConditionParameters {

    @XmlElement(name = "Parameter", required = true)
    protected Parameter parameter;
    @XmlElement(name = "OR", required = true)
    protected OR or;
    @XmlElement(name = "AND", required = true)
    protected AND and;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the parameter property.
     *
     * @return
     *     possible object is
     *     {@link Parameter }
     *
     */
    public Parameter getParameter() {
        return parameter;
    }

    /**
     * Sets the value of the parameter property.
     *
     * @param value
     *     allowed object is
     *     {@link Parameter }
     *
     */
    public void setParameter(Parameter value) {
        this.parameter = value;
    }

    /**
     * Gets the value of the or property.
     *
     * @return
     *     possible object is
     *     {@link OR }
     *
     */
    public OR getOR() {
        return or;
    }

    /**
     * Sets the value of the or property.
     *
     * @param value
     *     allowed object is
     *     {@link OR }
     *
     */
    public void setOR(OR value) {
        this.or = value;
    }

    /**
     * Gets the value of the and property.
     *
     * @return
     *     possible object is
     *     {@link AND }
     *
     */
    public AND getAND() {
        return and;
    }

    /**
     * Sets the value of the and property.
     *
     * @param value
     *     allowed object is
     *     {@link AND }
     *
     */
    public void setAND(AND value) {
        this.and = value;
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

}
