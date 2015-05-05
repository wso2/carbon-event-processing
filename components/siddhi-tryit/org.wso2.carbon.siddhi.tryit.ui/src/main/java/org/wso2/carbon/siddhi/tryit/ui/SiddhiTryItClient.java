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

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
//import org.wso2.siddhi.core.util.EventPrinter;

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiddhiTryItClient {

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    //Map
    private Map<String, StringBuilder> map = new LinkedHashMap<String, StringBuilder>();

    //processData method
    public Map<String, StringBuilder> processData(String executionPlan, String eventStream) throws InterruptedException {

        // Create Siddhi Manager
        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlan newExecutionPlan = SiddhiCompiler.parse(executionPlan);
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        //variables
        String queryName;
        Pattern pattern;
        Matcher matcher;
        int eventStreamAttributeListSize;
        String eventStreamAttributeArray[];

        //query call back
        for (int i = 0; i < newExecutionPlan.getExecutionElementList().size(); i++) {
            System.out.println("i: " + i);
            Query query = (Query) (newExecutionPlan.getExecutionElementList().get(i));

            //extract query name
            if (query.getAnnotations().size() > 0) {
                queryName = query.getAnnotations().get(0).getElement("name");
            } else {
                queryName = "";
            }


            if (!queryName.equals("")) {
                final StringBuilder stringBuilder = new StringBuilder();
                map.put(queryName, stringBuilder);
                stringBuilder.append(queryName);

                executionPlanRuntime.addCallback(queryName, new QueryCallback() {
                    @Override
                    public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                        //EventPrinter.print(timeStamp, inEvents, removeEvents);
                        stringBuilder.append(gson.toJson(inEvents));
                    }
                });
            } else {
                System.out.println("No query name defined!");


            }


        }

        //stream call bak
        for (int j = 0; j < newExecutionPlan.getExecutionElementList().size(); j++) {
            System.out.println("j: " + j);
            Query query = (Query) (newExecutionPlan.getExecutionElementList().get(j));
            //extract stream name
            String outputStreamName = query.getOutputStream().getId();

            final StringBuilder stringBuilder = new StringBuilder();
            map.put(outputStreamName, stringBuilder);
            stringBuilder.append(outputStreamName);
            executionPlanRuntime.addCallback(outputStreamName, new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    //EventPrinter.print(events);
                    stringBuilder.append(gson.toJson(events));
                }
            });
        }

        //extracting input stream name
        String inputStreamName;
        //inputStreamEventArray holds
        String inputStreamEventArray[] = eventStream.split("\\r?\\n");

        //loops for each event in a stream
        for (int k = 0; k < inputStreamEventArray.length; k++) {
            System.out.println("k: " + k);
            pattern = Pattern.compile("(\\S+)=\\[(.*)\\]");
            matcher = pattern.matcher(inputStreamEventArray[k]);
            if (matcher.find()) {
                inputStreamName = matcher.group(1);
                InputHandler inputHandler = executionPlanRuntime.getInputHandler(inputStreamName);

                eventStreamAttributeArray = matcher.group(2).split(",");

                eventStreamAttributeListSize = eventStreamAttributeArray.length;
                Object object[] = new Object[eventStreamAttributeListSize];
                for (int l = 0; l < eventStreamAttributeListSize; l++) {
                    //get attribute type from the execution plan
                    Attribute.Type attributeType = executionPlanRuntime.getStreamDefinitionMap().get(inputStreamName).getAttributeList().get(l).getType();
                    //getting the type of plan definition. hav to cast the stream attribute to the enum type and then create the object array and send

                    switch (attributeType) {
                        case STRING:
                            object[l] = eventStreamAttributeArray[l];
                            System.out.println("object string: " + object[l]);
                            break;
                        case INT:
                            object[l] = Integer.parseInt(eventStreamAttributeArray[l]);
                            System.out.println("object integer: " + object[l]);
                            break;
                        case LONG:
                            object[l] = Long.parseLong(eventStreamAttributeArray[l]);
                            System.out.println("object long: " + object[l]);
                            break;
                        case FLOAT:
                            object[l] = Float.parseFloat(eventStreamAttributeArray[l]);
                            System.out.println("object float: " + object[l]);
                            break;
                        case DOUBLE:
                            object[l] = Double.parseDouble(eventStreamAttributeArray[l]);
                            System.out.println("objectdouble: " + object[l]);
                            break;
                        case BOOL:
                            object[l] = Boolean.parseBoolean(eventStreamAttributeArray[l]);
                            System.out.println("object boolean: " + object[l]);
                            break;
                        case OBJECT:
                            object[l] = (Object) eventStreamAttributeArray[l];
                            System.out.println("object object: " + object[l]);
                            break;
                        default:
                            System.out.println("No matching attribute type");
                    }

                }
                if(k==0)
                {
                    executionPlanRuntime.start();
                }
                inputHandler.send(object);
                Thread.sleep(500);

            } else
                System.out.println("Please define the event stream in correct format");

        }

        return map;

    }


}

