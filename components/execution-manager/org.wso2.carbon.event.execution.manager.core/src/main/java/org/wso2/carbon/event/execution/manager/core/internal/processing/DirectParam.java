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

/**
 * DirectParam class keeps name and replaced value about the direct parameters that is mapped directly
 * with the query expressions,
 */
public class DirectParam {

    private String name;
    private String value;

    /**
     * get name
     *
     * @return direct parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * set name
     *
     * @param name direct parameter name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get value
     *
     * @return direct parameter value
     */
    public String getValue() {
        return value;
    }

    /**
     * set value
     *
     * @param value direct parameter value
     */
    public void setValue(String value) {
        this.value = value;
    }

}