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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for or complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="or">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="or" type="{}or" maxOccurs="unbounded"/>
 *         &lt;element name="and" type="{}and" maxOccurs="unbounded"/>
 *         &lt;element name="template" type="{}templateObject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "or", propOrder = {
    "or",
    "and",
    "template"
})
public class Or {

    @XmlElement(required = true)
    protected List<Or> or;
    @XmlElement(required = true)
    protected List<And> and;
    @XmlElement(required = true)
    protected List<TemplateObject> template;
    @XmlAttribute(name = "type", required = true)
    protected String type;

    /**
     * Gets the value of the or property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the or property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Or }
     * 
     * 
     */
    public List<Or> getOr() {
        if (or == null) {
            or = new ArrayList<Or>();
        }
        return this.or;
    }

    /**
     * Gets the value of the and property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the and property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link And }
     * 
     * 
     */
    public List<And> getAnd() {
        if (and == null) {
            and = new ArrayList<And>();
        }
        return this.and;
    }

    /**
     * Gets the value of the template property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the template property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTemplate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TemplateObject }
     * 
     * 
     */
    public List<TemplateObject> getTemplate() {
        if (template == null) {
            template = new ArrayList<TemplateObject>();
        }
        return this.template;
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
