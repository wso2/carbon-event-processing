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
package org.wso2.carbon.event.receiver.admin.internal;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EventReceiverAdminConstants {

    public static final Map<AttributeType, String> ATTRIBUTE_TYPE_STRING_MAP = Collections.unmodifiableMap(new HashMap<AttributeType, String>() {{
        put(AttributeType.BOOL, EventReceiverConstants.ATTR_TYPE_BOOL);
        put(AttributeType.STRING, EventReceiverConstants.ATTR_TYPE_STRING);
        put(AttributeType.DOUBLE, EventReceiverConstants.ATTR_TYPE_DOUBLE);
        put(AttributeType.FLOAT, EventReceiverConstants.ATTR_TYPE_FLOAT);
        put(AttributeType.INT, EventReceiverConstants.ATTR_TYPE_INT);
        put(AttributeType.LONG, EventReceiverConstants.ATTR_TYPE_LONG);
    }});
}
