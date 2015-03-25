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
package org.wso2.carbon.event.execution.manager.internal.processing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.execution.manager.internal.structure.template.ObjectFactory;
import org.wso2.carbon.event.execution.manager.internal.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.internal.structure.domain.TemplateDomain;
import org.wso2.carbon.event.execution.manager.internal.structure.template.TemplateConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/*
  class that read template domain structures and template configuration structures using JAXB and return each class object
 */
public class ReadTemplateStructures {
    private static final Log log = LogFactory.getLog(Processing.class);

    /**
     * get template configuration object when the object's xml content is given
     *
     * @param fileContent xml file content
     * @return template configuration object
     */
    public TemplateConfig getTemplateConfig(String fileContent) throws JAXBException {

            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            @SuppressWarnings("unchecked")
            JAXBElement<TemplateConfig> unmarshalledObject  =
                    ((JAXBElement<TemplateConfig>) unmarshaller.unmarshal(new ByteArrayInputStream(fileContent.getBytes(Charset.forName("UTF-8")))));

        return unmarshalledObject.getValue();
    }

    /**
     * get template domain object when the domain name is given
     *
     * @param fileName domain name
     * @return template domain object
     */
    public TemplateDomain getTemplateDomain(String fileName) throws JAXBException {

            DomainInformation domainInformation = new DomainInformation();
            String fileContent = domainInformation.getSpecificDomainInfo(fileName);

            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            @SuppressWarnings("unchecked")
            JAXBElement<TemplateDomain> unmarshalledObject =
                    ((JAXBElement<TemplateDomain>) unmarshaller
                            .unmarshal(new ByteArrayInputStream(
                                    fileContent.getBytes(Charset.forName("UTF-8")))));

        return unmarshalledObject.getValue();
    }

    /**
     * return template query content when the template domain and name is given
     *
     * @param templateDomain template domain object
     * @param templateName   template name
     * @return template query
     */
    public String getTemplateQuery(TemplateDomain templateDomain,
                                   String templateName) {
        String query = "";
        for (Template template : templateDomain.getTemplate()) {
            if (template.getName().equals(templateName)) {
                query = template.getTemplateQuery();
                break;
            }
        }
        return query;
    }
}
