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
package org.wso2.carbon.event.execution.manager.internal.structure.execution;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stream complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stream">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="as" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="valueOf" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stream")
public class Stream {

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "as")
    protected String as;
    @XmlAttribute(name = "valueOf")
    protected String valueOf;

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
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the as property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAs() {
        return as;
    }

    /**
     * Sets the value of the as property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAs(String value) {
        this.as = value;
    }

    /**
     * Gets the value of the valueOf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueOf() {
        return valueOf;
    }

    /**
     * Sets the value of the valueOf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueOf(String value) {
        this.valueOf = value;
    }

}
