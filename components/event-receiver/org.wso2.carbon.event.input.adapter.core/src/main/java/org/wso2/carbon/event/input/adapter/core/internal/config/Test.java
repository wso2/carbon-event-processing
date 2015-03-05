/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.event.input.adapter.core.internal.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 3/2/15.
 */
public class Test {
    public static void main(String[] args) {
        populateJaxbMappings();
    }

    private static void populateJaxbMappings() {

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(AdapterConfigs.class);
//            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Marshaller unmarshaller = jaxbContext.createMarshaller();
            AdapterConfigs adapterConfigs=new AdapterConfigs() ;
            AdapterConfig adapterConfig=new AdapterConfig();
            adapterConfig.setType("Type1");
            List<Property> propertyList=new ArrayList<Property>();
          Property property=  new Property();
            property.setKey("key1");
            property.setValue("val1");
            adapterConfig.setGlobalProperties(propertyList);

            propertyList.add(property);

            List<AdapterConfig> adapterConfigList=new ArrayList<AdapterConfig>();
            adapterConfigList.add(adapterConfig) ;
               adapterConfigs.setAdapterConfigs(adapterConfigList);

//            String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FILE_SPECIFIC_PATH + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FILE_NAME;
            String path = "/Users/suho/wso2/src/wso2/carbon-event-processing/components/event-publisher/org.wso2.carbon.event.input.adapter.core/target/foo.xml";
            File configFile = new File(path);
            unmarshaller.marshal(adapterConfigs,configFile);
//            if (!configFile.exists()) {
//                throw new EndpointAdaptorProcessingException("The " + RDBMSEventAdaptorConstants.ADAPTOR_GENERIC_RDBMS_FILE_NAME + " can not found in " + path);
//            }
//            unmarshaller.marshal(configFile);
//            Mappings mappings = (Mappings) unmarshaller.unmarshal(configFile);
//            Map<String, Mapping> dbMap = new HashMap<String, Mapping>();
//            List<Mapping> mappingList = mappings.getMapping();
//
//            for (Mapping mapping : mappingList) {
//                dbMap.put(mapping.getDb(), mapping);
//            }
//
//            //Constructs a map to contain all db wise elements and there values
//            for (Mapping mapping : mappingList) {
//                if (mapping.getDb() != null) {
//                    Mapping defaultMapping = dbMap.get(null);
//                    Mapping specificMapping = dbMap.get(mapping.getDb());
//                    List<Element> defaultElementList = defaultMapping.getElements().getElementList();
//                    Map<String, String> elementMappings = new HashMap<String, String>();
//                    for (Element element : defaultElementList) {
//                        //Check if the mapping is present in the specific mapping
//                        Element elementDetails = null;
//                        if (specificMapping.getElements().getElementList() != null) {
//                            elementDetails = specificMapping.getElements().getType(element.getKey());
//                        }
//                        //If a specific mapping is not found then use the default mapping
//                        if (elementDetails == null) {
//                            elementDetails = defaultMapping.getElements().getType(element.getKey());
//                        }
//                        elementMappings.put(elementDetails.getKey(), elementDetails.getValue());
//                    }
//                    dbTypeMappings.put(mapping.getDb(), elementMappings);
//                }
//            }
        } catch (JAXBException e) {
            e.printStackTrace();
//            throw new EndpointAdaptorProcessingException(e.getMessage(), e);
        }
    }
}
