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
package org.wso2.carbon.event.formatter.core.exception;

/**
 * If there is a problem when creating the event builder configuration.
 */
public class EventFormatterStreamValidationException extends RuntimeException {

    private String dependency;

    public EventFormatterStreamValidationException() {

    }

    public EventFormatterStreamValidationException(String message) {
        super(message);
    }

    public EventFormatterStreamValidationException(String message, String dependency) {
        super(message);
        this.dependency = dependency;
    }

    public EventFormatterStreamValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventFormatterStreamValidationException(String message, String dependency,
                                                   Throwable cause) {
        super(message, cause);
        this.dependency = dependency;
    }

    public EventFormatterStreamValidationException(Throwable cause) {
        super(cause);
    }

    public String getDependency() {
        return dependency;
    }
}
