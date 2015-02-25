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
package org.wso2.carbon.event.output.adaptor.email.internal.util;


public final class EmailEventAdaptorConstants {

    private EmailEventAdaptorConstants() {
    }

    public static final String ADAPTOR_TYPE_EMAIL = "email";

    public static final String ADAPTOR_MESSAGE_EMAIL_ADDRESS = "email.address";
    public static final String ADAPTOR_MESSAGE_EMAIL_SUBJECT = "email.subject";

    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 20;

}
