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
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.internal.processing.DomainInformation;
import org.wso2.carbon.event.execution.manager.core.internal.processing.Processing;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

public class ExecutionManagerAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(ExecutionManagerAdminService.class);
    private DomainInformation domainInformation;
    private ExecutionManager executionManager;

    /**
     * Default Constructor
     */
    public ExecutionManagerAdminService() {
        domainInformation = new DomainInformation();
        executionManager = new ExecutionManager();
    }

    /**
     * return all available template domain information
     *
     * @return all template domain information
     * @throws org.apache.axis2.AxisFault
     */
    public DomainInfoDTO[] getAllDomainInfoDTO() throws AxisFault {

        return domainInformation.getAllDomainInfo();
    }

    /**
     * return details for a given template domain name
     *
     * @param domainName template domain name
     * @return template domain full details
     * @throws AxisFault
     */
    public String getTemplateDomain(String domainName) throws AxisFault {
        return domainInformation.getSpecificDomainInfo(domainName);
    }

    /**
     * deploy an execution plan
     *
     * @param executionPlanConfigurationXml execution plan xml content
     * @throws AxisFault
     */
    public void deployTemplateConfig(String executionPlanConfigurationXml) throws AxisFault {
        log.debug("deployTemplateConfig");
        log.debug("Template Wiring: " + executionPlanConfigurationXml);
        EventProcessorService eventProcessorService = ExecutionManagerValueHolder.getEventProcessorService();
        if (eventProcessorService != null) {
            try {
                Processing processConfig = new Processing();

                String executionPlan = processConfig.getExecutionPlan(executionPlanConfigurationXml);
                log.debug("Execution Plan: " + executionPlan);


                eventProcessorService.deployExecutionPlanConfiguration(executionPlan, getAxisConfig());
            } catch (ExecutionPlanConfigurationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            } catch (ExecutionPlanDependencyValidationException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("EventProcessorService is not available for EventProcessorAdminService in runtime!");
        }

    }

    /**
     * return details of all the available template configurations
     *
     * @return array of DomainConfigInfoDTO
     * @throws AxisFault
     */
    public DomainConfigInfoDTO[] getAllDomainConfigInfoDTO() throws AxisFault {
        return executionManager.getAllTemplateConfig();
    }

    /**
     * get the content of a specified template configuration
     *
     * @param configName template configuration name
     * @return xml content of the template configuration as a string value
     */
    public String getDomainConfig(String configName) {
        return executionManager.getTemplateConfig(configName);
    }

    /**
     * delete a template configuration
     *
     * @param configName template configuration name
     * @throws AxisFault
     */
    public void deleteConfigInfo(String configName) throws AxisFault {
        executionManager.deleteTemplateConfig(configName);
    }

}
