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

package org.wso2.carbon.event.throttle.core.internal.util;

import org.wso2.carbon.event.throttle.core.internal.Policy;
import org.wso2.carbon.event.throttle.core.internal.ThrottleConfig;

import java.text.MessageFormat;

/**
 * This class constructs the Global Execution Plans representing policy configurations.
 */
public class GlobalPolicyGenerator {
    private ThrottleConfig throttleConfig;
    //Altering following two strings without prior knowledge will break the throttle flow.
    private String generalPolicyTemplate = "/* Enter a unique ExecutionPlan */\n" +
            "@Plan:name(''{0}'')\n" +
            "\n" +
            "/* Enter a unique description for ExecutionPlan */\n" +
            "-- @Plan:description(''ExecutionPlan for {0}'')\n" +
            "\n" +
            "/* define streams/tables and write queries here ... */\n" +
            "\n" +
            "@Import(''{1}'')\n" +
            "{2}\n" +
            "\n" +
            "@Export(''{3}'')\n" +
            "{4}\n" +
            "\n" +
            "/*Eligibility Query*/\n" +
            "{5}\n" +
            "\n" +
            "/*Decision Query*/\n" +
            "{6}\n" +
            "\n" +
            "/*Emitting query*/\n" +
            "{7}";

    private String commonPolicyTemplate = "/* Enter a unique ExecutionPlan */\n" +
            "@Plan:name(''{0}'')\n" +
            "\n" +
            "/* Enter a unique description for ExecutionPlan */\n" +
            "-- @Plan:description(''ExecutionPlan for {0}'')\n" +
            "\n" +
            "/* define streams/tables and write queries here ... */\n" +
            "\n" +
            "@Import(''{1}'')\n" +
            "{2}\n" +
            "\n" +
            "{3}\n" +
            "\n" +
            "{4}";

    public GlobalPolicyGenerator(ThrottleConfig throttleConfig){
        this.throttleConfig = throttleConfig;
    }

    /**
     * Returns Global Common Execution Plan. Each throttling request will be finally handled with this Execution Plan.
     * @return Global Common Execution Plan
     */
    public String getCommonPolicyPlan(){
        String executionPlan = MessageFormat.format(commonPolicyTemplate, "commonThrottlingExecutionPlan",
                throttleConfig.getThrottleStreamID(), throttleConfig.getThrottleStream(),
                throttleConfig.getEventTable(), throttleConfig.getGlobalQuery());
        return executionPlan;
    }

    /**
     * Returns global execution plan for given throttling policies.
     * @param policy Throttling policy for which execution plan should be built.
     * @return Global execution plan
     */
    public String getCustomPolicyPlan(Policy policy){
        String executionPlan = MessageFormat.format(generalPolicyTemplate, policy.getName(),
                throttleConfig.getRequestStreamID(), throttleConfig.getRequestStream(),
                throttleConfig.getThrottleStreamID(), throttleConfig.getThrottleStream(),
                policy.getEligibilityQuery(), policy.getDecisionQuery(), throttleConfig.getEmittingQuery());
        return executionPlan;
    }
}
