/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.tryit.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiddhiTryItClient {

    private static Log log = LogFactory.getLog(SiddhiTryItClient.class);
    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private String errMsg;

    /**
     * Event stream will be processed according to the specified execution plan
     *
     * @param executionPlan execution plan for siddhi query processing
     * @param eventStream   event stream
     * @param dateTime      date and time to begin the process
     */
    public Map<String, StringBuilder> processData(String executionPlan, String eventStream, String dateTime) throws ParseException {

        Map<String, StringBuilder> map = new LinkedHashMap<String, StringBuilder>();
        long beginSetTime = createTimeStamp(dateTime);
        long beginSystemTime = System.currentTimeMillis();
        String queryName = "";

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan newExecutionPlan = SiddhiCompiler.parse(executionPlan);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        //Query Callback
        for (int i = 0; i < newExecutionPlan.getExecutionElementList().size(); i++) {
            Query query = (Query) (newExecutionPlan.getExecutionElementList().get(i));

            if (query.getAnnotations().size() > 0) {
                queryName = query.getAnnotations().get(0).getElement("name");
            } else {
                queryName = "";
            }

            if (!queryName.equals("")) {
                final StringBuilder stringBuilder = new StringBuilder();
                map.put(queryName, stringBuilder);

                executionPlanRuntime.addCallback(queryName, new QueryCallback() {
                    @Override
                    public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                        stringBuilder.append(gson.toJson(inEvents));
                    }
                });
            } else {
                System.out.println("No query name defined!"); //todo
            }
        }

        //Stream Callback
        for (int j = 0; j < newExecutionPlan.getExecutionElementList().size(); j++) {
            Query query = (Query) (newExecutionPlan.getExecutionElementList().get(j));
            String outputStreamName = query.getOutputStream().getId();

            final StringBuilder stringBuilder = new StringBuilder();
            map.put(outputStreamName, stringBuilder);

            executionPlanRuntime.addCallback(outputStreamName, new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    stringBuilder.append(gson.toJson(events));
                }
            });
        }

        Pattern pattern1, patter2;
        Matcher matcher1, matcher2;
        int eventStreamAttributeListSize;
        String[] eventStreamAttributeArray;
        String inputStreamName;

        //Send event stream
        String[] inputStreamEventArray = eventStream.split("\\r?\\n");
        for (int k = 0; k < inputStreamEventArray.length; k++) {

            pattern1 = Pattern.compile("(\\S+)=\\[(.*)\\]");
            matcher1 = pattern1.matcher(inputStreamEventArray[k]);
            patter2 = Pattern.compile("\\d\\w+");
            matcher2 = patter2.matcher(inputStreamEventArray[k]);

            if (matcher1.find()) {
                inputStreamName = matcher1.group(1);
                InputHandler inputHandler = executionPlanRuntime.getInputHandler(inputStreamName);

                eventStreamAttributeArray = matcher1.group(2).split(",");
                eventStreamAttributeListSize = eventStreamAttributeArray.length;
                Object object[] = new Object[eventStreamAttributeListSize];
                for (int l = 0; l < eventStreamAttributeListSize; l++) {
                    Attribute.Type attributeType = executionPlanRuntime.getStreamDefinitionMap().get(inputStreamName).getAttributeList().get(l).getType();
                    switch (attributeType) {
                        case STRING:
                            object[l] = eventStreamAttributeArray[l];
                            break;
                        case INT:
                            object[l] = Integer.parseInt(eventStreamAttributeArray[l]);
                            break;
                        case LONG:
                            object[l] = Long.parseLong(eventStreamAttributeArray[l]);
                            break;
                        case FLOAT:
                            object[l] = Float.parseFloat(eventStreamAttributeArray[l]);
                            break;
                        case DOUBLE:
                            object[l] = Double.parseDouble(eventStreamAttributeArray[l]);
                            break;
                        case BOOL:
                            object[l] = Boolean.parseBoolean(eventStreamAttributeArray[l]);
                            break;
                        case OBJECT:
                            object[l] = (Object) eventStreamAttributeArray[l];
                            break;
                        default:
                            System.out.println("No matching attribute type"); //todo
                    }
                }
                if (k == 0) {
                    executionPlanRuntime.start();
                    try {
                        inputHandler.send(beginSetTime, object);
                    } catch (InterruptedException e) {
                        errMsg = "Internal error occured while sending events to Siddhi" + e.getMessage();
                        log.error(errMsg, e);
                    }
                } else {
                    try {
                        inputHandler.send((beginSetTime + (System.currentTimeMillis() - beginSystemTime)), object);
                    } catch (InterruptedException e) {
                        errMsg = "Internal error occured while sending events to Siddhi" + e.getMessage();
                        log.error(errMsg, e);
                    }
                }
            } else if (matcher2.find()) {
                try {
                    Thread.sleep(Long.parseLong(matcher2.group(0)));
                } catch (InterruptedException e) {
                    log.error(e);
                }

            } else
                System.out.println("No match"); //todo
        }

        //To pause till all the events are passed //todo
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error(e);
        }
        return map;
    }

    /**
     * Create time stamp for the given date and time
     *
     * @param dateTime date and time to begin the process
     */
    private long createTimeStamp(String dateTime) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = null;
        try {
            date = dateFormatter.parse(dateTime);
        } catch (ParseException e) {
            errMsg = "Error occured while parsing data" + e.getMessage();
            log.error(errMsg, e);
        }
        long timeStamp = date.getTime();
        return timeStamp;
    }

}

