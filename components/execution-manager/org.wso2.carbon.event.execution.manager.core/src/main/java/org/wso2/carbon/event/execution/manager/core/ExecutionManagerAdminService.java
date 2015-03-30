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
package org.wso2.carbon.event.execution.manager.core;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.execution.manager.core.dto.ConfigDTO;
import org.wso2.carbon.event.execution.manager.core.dto.DomainConfigInfoDTO;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.internal.processing.DomainInformation;

public class ExecutionManagerAdminService extends AbstractAdmin {

    /**
     * Default Constructor
     */
    public ExecutionManagerAdminService() {

    }

    /**
     * return all available template domain information
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public DomainConfigInfoDTO[] getAllTemplateDomains() throws AxisFault {

        return ExecutionManagerValueHolder.getDomainInformation().getAllDomainInfo();
    }

    /**
     * return details for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public DomainConfigInfoDTO getTemplateDomain(String domainName) throws AxisFault {
        return ExecutionManagerValueHolder.getDomainInformation().getSpecificDomainInfo(domainName);
    }

    /**
     * Delete specified configuration
     *
     * @param configName configuration name which needs to be deleted
     */
    public void deleteTemplateConfig(String configName) {
        ExecutionManagerValueHolder.getDomainInformation().deleteTemplateConfig(configName);
    }

    /**
     * Create or update specified configuration
     *
     * @param configuration configuration data transfer object
     */
    public void saveTemplateConfig(String domainName, ConfigDTO configuration) {
        ExecutionManagerValueHolder.getDomainInformation().saveTemplateConfig(domainName, configuration);
    }


}
