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
package org.wso2.carbon.event.processor.admin.internal.util;

import org.wso2.carbon.databridge.commons.AttributeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface EventProcessorConstants {

    public static final Map<AttributeType,String> STRING_ATTRIBUTE_TYPE_MAP = Collections.unmodifiableMap(new HashMap<AttributeType,String>() {{
        put(AttributeType.BOOL,"boolean");
        put(AttributeType.STRING,"string");
        put(AttributeType.DOUBLE,"double");
        put(AttributeType.FLOAT,"float");
        put(AttributeType.INT,"int");
        put(AttributeType.LONG,"long");
    }});

}
