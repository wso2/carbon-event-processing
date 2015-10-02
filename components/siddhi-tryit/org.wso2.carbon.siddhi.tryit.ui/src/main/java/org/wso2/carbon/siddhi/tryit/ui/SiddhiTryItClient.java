/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.tryit.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.siddhi.tryit.ui.internal.ds.SiddhiTryItValueHolder;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiddhiTryItClient {

    private static Log log = LogFactory.getLog(SiddhiTryItClient.class);
    private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static Pattern eventPattern = Pattern.compile("(\\S+)=\\[(.*)\\]");
    private static Pattern delayPattern = Pattern.compile("(delay\\()(\\d+)+");
    private String errMsg;

    /**
     * Event stream will be processed according to the specified execution plan
     *
     * @param executionPlan execution plan for siddhi query processing
     * @param eventStream   event stream
     * @param dateTime      date and time to begin the process
     */
    public Map<String, StringBuilder> processData(String executionPlan, String eventStream,
                                                  String dateTime) throws Exception {

        Map<String, StringBuilder> map = new LinkedHashMap<>();
        Map<String, InputHandler> inputHandlerMap = new LinkedHashMap<>();
        long startSetTime = createTimeStamp(dateTime);
        long startSystemTime = System.currentTimeMillis();

        // Create Siddhi Manager and load data sources
        SiddhiManager siddhiManager = new SiddhiManager();
        loadDataSourceConfiguration(siddhiManager);
        ExecutionPlan newExecutionPlan = SiddhiCompiler.parse(executionPlan);
        ExecutionPlanRuntime executionPlanRuntime =
                siddhiManager.createExecutionPlanRuntime(executionPlan);

        //Query Callback
        processQueryCallback(map, newExecutionPlan, executionPlanRuntime);

        //Stream Callback
        processStreamCallback(map, inputHandlerMap, executionPlanRuntime);

        //Event stream processing
        try {
            return processEventStream(eventStream, map, inputHandlerMap, startSetTime, startSystemTime, executionPlanRuntime);
        } catch (Throwable e) {
            //catch throwable since there could be execution plan validation exceptions
            errMsg = "Error occurred while processing. " + e.getMessage();
            log.error(errMsg, e);
            throw new Exception(errMsg, e);
        } finally {
            if (executionPlanRuntime != null) {
                executionPlanRuntime.shutdown();
            }
        }
    }

    /**
     * Process input stream and return a map with query and stream results
     *
     * @param eventStream          input event stream
     * @param map                  string builder object map
     * @param inputHandlerMap      input handler object map
     * @param startSetTime         created time stamp for the given date and time
     * @param startSystemTime      system time at start
     * @param executionPlanRuntime execution plan runtime object
     * @throws Exception
     */
    private Map<String, StringBuilder> processEventStream(String eventStream, Map<String, StringBuilder> map, Map<String,
            InputHandler> inputHandlerMap, long startSetTime, long startSystemTime, ExecutionPlanRuntime executionPlanRuntime) throws Exception {
        String[] inputStreamEventArray = eventStream.split("\\r?\\n");
        executionPlanRuntime.start();

        for (int i = 0; i < inputStreamEventArray.length; i++) {
            Matcher eventPatternMatcher =
                    eventPattern.matcher(inputStreamEventArray[i].trim());
            Matcher delayPatternMatcher = delayPattern.matcher(inputStreamEventArray[i]);

            if (eventPatternMatcher.find()) {
                String inputStreamId = eventPatternMatcher.group(1);
                //create event object
                String[] eventStreamAttributeArray = eventPatternMatcher.group(2).split(",");
                int eventStreamAttributeListSize = eventStreamAttributeArray.length;
                Object[] object = new Object[eventStreamAttributeListSize];
                for (int j = 0; j < eventStreamAttributeListSize; j++) {
                    Attribute.Type attributeType = executionPlanRuntime.getStreamDefinitionMap().get(inputStreamId)
                            .getAttributeList().get(j).getType();
                    switch (attributeType) {
                        case STRING:
                            object[j] = eventStreamAttributeArray[j];
                            break;
                        case INT:
                            object[j] = Integer.parseInt(eventStreamAttributeArray[j]);
                            break;
                        case LONG:
                            object[j] = Long.parseLong(eventStreamAttributeArray[j]);
                            break;
                        case FLOAT:
                            object[j] = Float.parseFloat(eventStreamAttributeArray[j]);
                            break;
                        case DOUBLE:
                            object[j] = Double.parseDouble(eventStreamAttributeArray[j]);
                            break;
                        case BOOL:
                            object[j] = Boolean.parseBoolean(eventStreamAttributeArray[j]);
                            break;
                        case OBJECT:
                            object[j] = eventStreamAttributeArray[j];
                            break;
                    }
                }
                //send events
                for (String key : inputHandlerMap.keySet()) {
                    if (key.equals(inputStreamId)) {
                        inputHandlerMap.get(key).send((startSetTime +
                                (System.currentTimeMillis() -
                                        startSystemTime)), object);
                    }
                }
            } else if (delayPatternMatcher.find()) {
                Thread.sleep(Long.parseLong(delayPatternMatcher.group(2)));
            } else {
                if (!inputStreamEventArray[i].equals("")) {
                    errMsg = "Error in event \"  " +
                            inputStreamEventArray[i] +
                            "\n\"." +
                            " Expected format: &lt;eventStreamName&gt;=[&lt;attribute1&gt;,&lt;attribute2&gt;]";
                    throw new IllegalArgumentException(errMsg);
                }
            }
        }
        Thread.sleep(500);
        return map;
    }

    /**
     * Process stream callback
     *
     * @param map                  string builder object map
     * @param inputHandlerMap      input handler object map
     * @param executionPlanRuntime execution plan runtime object
     */
    private void processStreamCallback(Map<String, StringBuilder> map, Map<String, InputHandler> inputHandlerMap,
                                       ExecutionPlanRuntime executionPlanRuntime) {
        for (AbstractDefinition abstractDefinition : executionPlanRuntime.getStreamDefinitionMap().values()) {
            String streamId = abstractDefinition.getId();

            //create input handler
            InputHandler inputHandler = executionPlanRuntime.getInputHandler(streamId);
            if (!inputHandlerMap.containsKey(streamId)) {
                inputHandlerMap.put(streamId, inputHandler);
            }
            final StringBuilder stringBuilder = new StringBuilder();
            map.put(streamId, stringBuilder);
            executionPlanRuntime.addCallback(streamId, new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    stringBuilder.append(gson.toJson(events));
                }
            });
        }
    }

    /**
     * Process query callback
     *
     * @param map                  string builder object map
     * @param newExecutionPlan     new execution plan passed through siddhi compiler
     * @param executionPlanRuntime execution plan runtime object
     */
    private void processQueryCallback(Map<String, StringBuilder> map, ExecutionPlan newExecutionPlan, ExecutionPlanRuntime executionPlanRuntime) {
        for (ExecutionElement executionElement : newExecutionPlan.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                Query query = (Query) executionElement;
                Element element = AnnotationHelper
                        .getAnnotationElement("info", "name", query.getAnnotations());
                if (element != null) {
                    String queryName = element.getValue();
                    final StringBuilder stringBuilder = new StringBuilder();
                    map.put(":".concat(queryName), stringBuilder);
                    executionPlanRuntime.addCallback(queryName, new QueryCallback() {
                        @Override
                        public void receive(long timeStamp, Event[] inEvents,
                                            Event[] removeEvents) {
                            if (inEvents != null) {
                                stringBuilder.append(gson.toJson(inEvents));
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Create time stamp for the given date and time
     *
     * @param dateTime date and time to begin the process
     */
    private long createTimeStamp(String dateTime) throws ParseException {
        Date date;
        try {
            date = dateFormatter.parse(dateTime);
        } catch (ParseException e) {
            errMsg = "Error occurred while parsing date " + e.getMessage();
            log.error(errMsg, e);
            throw new ParseException(errMsg, e.getErrorOffset());
        }
        long timeStamp = date.getTime();
        return timeStamp;
    }

    /**
     * Load data sources configuration
     *
     * @param siddhiManager SiddhiManager object
     */
    public static void loadDataSourceConfiguration(SiddhiManager siddhiManager) throws DataSourceException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (tenantId > -1) {
                DataSourceManager.getInstance().initTenant(tenantId);
            }
            List<CarbonDataSource> dataSources = SiddhiTryItValueHolder.getDataSourceService().getAllDataSources();
            for (CarbonDataSource cds : dataSources) {
                if (cds.getDSObject() instanceof DataSource) {
                    siddhiManager.setDataSource(cds.getDSMInfo().getName(), (DataSource) cds.getDSObject());
                }
            }
        } catch (DataSourceException e) {
            log.error("Unable to populate the data sources in Siddhi engine.", e);
            throw e;
        }
    }
}

