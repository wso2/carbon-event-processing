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
package org.wso2.carbon.event.input.adaptor.kafka.internal.util;

public final class ConsumerKafkaConstants {

    private ConsumerKafkaConstants() {
    }

    public final static String ADAPTOR_TYPE_KAFKA = "kafka";
    public final static String ADAPTOR_SUSCRIBER_TOPIC = "topic";
    public final static String ADAPTOR_SUSCRIBER_GROUP_ID = "group.id";
    public final static String ADAPTOR_SUSCRIBER_GROUP_ID_hint = "group.id.hint";
    public final static String ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT = "zookeeper.id";
    public final static String ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT_HINT = "zookeeper.id.hint";
    public final static String ADAPTOR_SUSCRIBER_THREADS = "threads";
    public final static String ADAPTOR_SUSCRIBER_THREADS_HINT = "threads.hint";
    public final static String ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES="optional.configuration";
    public final static String ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES_HINT="optional.configuration.hint";
    public static final int AXIS_TIME_INTERVAL_IN_MILLISECONDS = 10000;

}
