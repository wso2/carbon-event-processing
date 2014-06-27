/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.mqtt.internal.util;


public final class MQTTEventAdaptorConstants {

    private MQTTEventAdaptorConstants() {
    }

    public static final String ADAPTOR_TYPE_MQTT = "mqtt";
    public static final String ADAPTOR_CONF_URL = "url";
    public static final String ADAPTOR_CONF_USERNAME = "username";
    public static final String ADAPTOR_CONF_USERNAME_HINT = "username.hint";
    public static final String ADAPTOR_CONF_PASSWORD = "password";
    public static final String ADAPTOR_CONF_PASSWORD_HINT = "password.hint";
    public static final String ADAPTOR_CONF_URL_HINT = "url.hint";
    public static final String ADAPTOR_MESSAGE_TOPIC = "topic";
    public static final String ADAPTOR_MESSAGE_CLIENTID = "clientId";
    public static final int AXIS_TIME_INTERVAL_IN_MILLISECONDS = 10000;
    public static final String ADAPTOR_CONF_CLEAN_SESSION = "cleanSession";
    public static final String ADAPTOR_CONF_CLEAN_SESSION_HINT = "cleanSession.hint";
    public static final String ADAPTOR_CONF_KEEP_ALIVE = "keepAlive";


}
