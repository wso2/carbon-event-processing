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

import org.wso2.carbon.event.execution.manager.internal.structure.template.Template;

/*
  This class is used to keep information about template and its query listed in the configuration xml that
  is exchanged between frontend and backend
 */
public class WiredTemplates {

    private Template template;
    private String templateQuery;

    /**
     * constructor
     */
    public WiredTemplates() {
    }

    /**
     * constructor
     *
     * @param template      Template object
     * @param templateQuery template query
     */
    public WiredTemplates(Template template, String templateQuery) {
        this.template = template;
        this.templateQuery = templateQuery;
    }

    /**
     * get template object
     *
     * @return template object
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * get template query
     *
     * @return template query
     */
    public String getTemplateQuery() {
        return templateQuery;
    }

    /**
     * set template object
     *
     * @param template template object
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * set template object
     *
     * @param templateQuery template object
     */
    public void setTemplateQuery(String templateQuery) {
        this.templateQuery = templateQuery;
    }
}