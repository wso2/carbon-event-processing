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
package org.wso2.carbon.event.execution.manager;

/**
 * represent template configuration information
 */
public class DomainConfigInfoDTO {

    private String name;
    private String type;
    private String description;

    /**
     * constructor
     *
     * @param name        template configuration name
     * @param type        template configuration type
     * @param description template configuration description
     */
    public DomainConfigInfoDTO(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    /**
     * get template configuration name
     *
     * @return template configuration name
     */
    public String getName() {
        return name;
    }

    /**
     * set template configuration name
     *
     * @param name template configuration name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get template configuration description
     *
     * @return template configuration description
     */
    public String getDescription() {
        return description;
    }

    /**
     * set template configuration description
     *
     * @param description template configuration description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * get template configuration type
     *
     * @return template configuration type
     */
    public String getType() {
        return type;
    }

    /**
     * set template configuration type
     *
     * @param type template configuration type
     */
    public void setType(String type) {
        this.type = type;
    }
}
