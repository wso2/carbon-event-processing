/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.siddhi.geo.event.notifier;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import java.util.HashMap;

public class NotifyAlert extends FunctionExecutor{

    private Attribute.Type returnType = Attribute.Type.BOOL;
    private HashMap<String, String> informationBuffer = new HashMap<String, String>();

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "geoDashboard:needToNotify(id,information) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "geoDashboard:needToNotify(id,information) function, " + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[1].getReturnType().toString());
            }
        }else if (attributeExpressionExecutors.length == 3) {
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of " +
                        "geoDashboard:needToNotify(id,information,triggerFirst) function, " +
                        "" + "required " + Attribute.Type.STRING +
                        " but found " + attributeExpressionExecutors[0].getReturnType().toString());
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of " +
                        "geoDashboard:needToNotify(id,information,triggerFirst) function, " +
                        "" + "required " + Attribute.Type.STRING +" but found " + attributeExpressionExecutors[1]
                        .getReturnType().toString());
            }
            if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the third argument of " +
                        "geoDashboard:needToNotify(id,information,triggerFirst) function, " +
                        "" + "required " + Attribute.Type.STRING +" but found " + attributeExpressionExecutors[2]
                        .getReturnType().toString());
            }
        }else {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to geoDashboard:needToNotify() " +
                    "function, required 2,3, but found " + attributeExpressionExecutors.length);
        }

    }

    @Override
    protected Object execute(Object[] data) {
        boolean returnValue = false;
        if (data.length == 2) {

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to geoDashboard:needToNotify(id," +
                        "information) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to geoDashboard:needToNotify(id," +
                        "information) function" + ". Second " + "argument cannot be null");
            }

            String id = (String) data[0];
            String currentInformation = (String) data[1];
            if (informationBuffer.containsKey(id) && !informationBuffer.get(id).equals(currentInformation)) {
                returnValue = true;
            }
            informationBuffer.put(id, currentInformation);
        }
        if (data.length == 3) {

            if (data[0] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to geoDashboard:needToNotify(id," +
                        "information,triggerFirst) function" + ". First " + "argument cannot be null");
            }
            if (data[1] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to geoDashboard:needToNotify(id," +
                        "information,triggerFirst) function" + ". Second " + "argument cannot be null");
            }
            if (data[2] == null) {
                throw new ExecutionPlanRuntimeException("Invalid input given to geoDashboard:needToNotify(id," +
                        "information,triggerFirst) function" + ". Third " + "argument cannot be null");
            }
        }
        if (data.length == 3) {
            returnValue = Boolean.valueOf(((String)data[2]).equals("sendFirst"));
        }
        String id = (String) data[0];
        String currentInformation = (String) data[1];

        if (informationBuffer.containsKey(id) && !informationBuffer.get(id).equals(currentInformation)) {
            returnValue = true;
        }
        informationBuffer.put(id, currentInformation);
        return returnValue;
    }

    @Override
    protected Object execute(Object data) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
