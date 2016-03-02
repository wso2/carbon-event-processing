/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.throttle.core;

import org.wso2.carbon.event.throttle.core.exception.ThrottleConfigurationException;

public interface ThrottlerService {

    /**
     * Returns whether the given throttleRequest is throttled.
     *
     * @param throttleRequest User request which needs to be checked whether throttled
     * @return Throttle status for current throttleRequest
     */
    public boolean isThrottled(Object[] throttleRequest);

    /**
     * Deploys the provided query under provided name in global throttling engine. Local validation is done prior to
     * deploying. If throttling policy with the same name exists in global throttling engine will edit/replace it.
     * Else will deploy as a new policy. If anything fails will throw {@link ThrottleConfigurationException}
     * @param name Name of the throttling policy
     * @param query Query which represents the throttling policy
     * @throws ThrottleConfigurationException
     */
    public void deployGlobalThrottlingPolicy(String name, String query) throws ThrottleConfigurationException;
}
