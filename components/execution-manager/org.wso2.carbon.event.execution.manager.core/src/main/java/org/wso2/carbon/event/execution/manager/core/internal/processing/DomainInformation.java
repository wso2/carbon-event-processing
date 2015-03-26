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

package org.wso2.carbon.event.execution.manager.core.internal.processing;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.execution.manager.core.DomainInfoDTO;
import org.wso2.carbon.event.execution.manager.core.internal.structure.domain.ObjectFactory;
import org.wso2.carbon.event.execution.manager.core.internal.structure.domain.TemplateDomain;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DomainInformation {
    private static final Log log = LogFactory.getLog(DomainInformation.class);
    //list to keep track of template domain information
    private List<DomainInfoDTO> domainInfoList;

    /**
     * read all TemplateDomains and returns the name and its description as an array of the domain objects
     *
     * @return DomainInfoDTO object array
     */
    public DomainInfoDTO[] getAllDomainInfo() {
        //get template domain file path
        String filePath = ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH;
        File folder = new File(filePath);
        //list of domain objects
        domainInfoList = new ArrayList<DomainInfoDTO>();
        searchFiles(folder);
        //convert DomainInfoDTO object list to an array
        DomainInfoDTO[] allDomainInfo = new DomainInfoDTO[domainInfoList.size()];
        allDomainInfo = domainInfoList.toArray(allDomainInfo);
        return allDomainInfo;
    }

    /**
     * Search through the specified folder for each templateDomain,covert the files using JAXB,set the name
     * and description for each DomainInfoDTO type object
     *
     * @param folder-path to the cep directory where the templateDomain files are stored
     */
    public void searchFiles(final File folder) {
        try {
            //traverse through the folder
            for (final File fileEntry : folder.listFiles()) {
                //get the file content as a string
                if (fileEntry.isFile()) {
                    String fileContent = new Scanner(fileEntry)
                            .useDelimiter("\\A").next();
                    log.debug(fileContent);
                    //create a JAXB instance for unmarshelling
                    JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    @SuppressWarnings("unchecked")
                    //create a TemplateDomain object from the unmarsheller
                            JAXBElement<TemplateDomain> unmarshalledObject =
                            ((JAXBElement<TemplateDomain>) unmarshaller
                                    .unmarshal(new ByteArrayInputStream(fileContent.getBytes(
                                            Charset.forName(ExecutionManagerConstants.DEFAULT_CHARSET)))));
                    TemplateDomain templateDomain = unmarshalledObject.getValue();
                    //initiate the domainInfoDTO object
                    DomainInfoDTO domainInfoDTO = new DomainInfoDTO();
                    //set name of the template domain
                    domainInfoDTO.setName(templateDomain.getName());
                    //set description of the template domain
                    domainInfoDTO.setDescription(templateDomain.getDescription());
                    //add the domainInfoDTO object to the list
                    domainInfoList.add(domainInfoDTO);
                }
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        catch (JAXBException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * get information of a specific domain
     *
     * @param domainName template domain name
     * @return content of the TemplateDomain file
     */
    public String getSpecificDomainInfo(String domainName) {
        String fileName = ExecutionManagerConstants.PATH_SEPARATOR + domainName
                + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
        String fileContent = "";
        //path to the location where the TemplateDomain files are stored
        String filePath = ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH + fileName;
        try {
            //create an input stream from the file
            InputStream inputStream = new FileInputStream(new File(filePath));
            //convert input stream to string
            fileContent = IOUtils.toString(inputStream, ExecutionManagerConstants.DEFAULT_CHARSET);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return fileContent;
    }
}