/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.throttle.core.internal;

/**
 * POJO class to represent common_throttle_config.xml
 */
public class ThrottleConfig {
    private String requestStream;
    private String requestStreamID;
    private String eligibilityStream;
    private String throttleStream;
    private String throttleStreamID;
    private String eventTable;
    private String localQuery;
    private String globalQuery;
    private String emittingQuery;

    public ThrottleConfig(String requestStream, String requestStreamID, String eligibilityStream, String throttleStream, String throttleStreamID, String eventTable, String localQuery,
                          String globalQuery, String emittingQuery) {
        this.requestStream = requestStream;
        this.requestStreamID = requestStreamID;
        this.eligibilityStream = eligibilityStream;
        this.throttleStream = throttleStream;
        this.throttleStreamID = throttleStreamID;
        this.eventTable = eventTable;
        this.localQuery = localQuery;
        this.globalQuery = globalQuery;
        this.emittingQuery = emittingQuery;
    }

    public String getRequestStream() {
        return requestStream;
    }

    public String getEligibilityStream() {
        return eligibilityStream;
    }

    public String getEventTable() {
        return eventTable;
    }

    public String getLocalQuery() {
        return localQuery;
    }

    public String getGlobalQuery() {
        return globalQuery;
    }

    public String getRequestStreamID() {
        return requestStreamID;
    }

    public String getThrottleStream() {
        return throttleStream;
    }

    public String getThrottleStreamID() {
        return throttleStreamID;
    }

    public String getEmittingQuery() {
        return emittingQuery;
    }
}
