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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ExecutionPlan_QNAME = new QName("", "executionPlan");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExecutionPlan }
     * 
     */
    public ExecutionPlan createExecutionPlan() {
        return new ExecutionPlan();
    }

    /**
     * Create an instance of {@link Stream }
     * 
     */
    public Stream createStream() {
        return new Stream();
    }

    /**
     * Create an instance of {@link SiddhiConfiguration }
     * 
     */
    public SiddhiConfiguration createSiddhiConfiguration() {
        return new SiddhiConfiguration();
    }

    /**
     * Create an instance of {@link ImportedStreams }
     * 
     */
    public ImportedStreams createImportedStreams() {
        return new ImportedStreams();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link ExportedStreams }
     * 
     */
    public ExportedStreams createExportedStreams() {
        return new ExportedStreams();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link ExecutionPlan }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "executionPlan")
    public JAXBElement<ExecutionPlan> createExecutionPlan(ExecutionPlan value) {
        return new JAXBElement<ExecutionPlan>(_ExecutionPlan_QNAME, ExecutionPlan.class, null, value);
    }

}
