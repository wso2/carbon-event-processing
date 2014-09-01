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
package org.wso2.carbon.event.formatter.core.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;

import java.util.Map;


public interface OutputMapperFactory {

    OutputMapping constructOutputMapping(OMElement omElement)
            throws EventFormatterConfigurationException;

    OMElement constructOutputMappingOM(OutputMapping outputMapping, OMFactory factory)
            throws EventFormatterConfigurationException;

    OutputMapper constructOutputMapper(EventFormatterConfiguration eventFormatterConfiguration,
                                       Map<String, Integer> propositionMap, int tenantId,
                                       StreamDefinition streamDefinition)
            throws EventFormatterConfigurationException;


}
