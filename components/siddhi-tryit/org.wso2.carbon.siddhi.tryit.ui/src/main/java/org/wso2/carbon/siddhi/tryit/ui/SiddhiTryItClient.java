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
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiddhiTryItClient {

	private static Log log = LogFactory.getLog(SiddhiTryItClient.class);
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private String errMsg = "";

	/**
	 * Event stream will be processed according to the specified execution plan
	 *
	 * @param executionPlan execution plan for siddhi query processing
	 * @param eventStream   event stream
	 * @param dateTime      date and time to begin the process
	 */
	public Map<String, StringBuilder> processData(String executionPlan, String eventStream,
	                                              String dateTime) throws Exception {

		Map<String, StringBuilder> map = new LinkedHashMap<String, StringBuilder>();
		long beginSetTime = createTimeStamp(dateTime);
		long beginSystemTime = System.currentTimeMillis();

		// Create Siddhi Manager
		SiddhiManager siddhiManager = new SiddhiManager();
		ExecutionPlan newExecutionPlan = SiddhiCompiler.parse(executionPlan);
		ExecutionPlanRuntime executionPlanRuntime =
				siddhiManager.createExecutionPlanRuntime(executionPlan);

		//Query Callback
		for (int i = 0; i < newExecutionPlan.getExecutionElementList().size(); i++) {
			String queryName = "";
			ExecutionElement executionElement = newExecutionPlan.getExecutionElementList().get(i);

			if(executionElement instanceof Query)   {
				Query query = (Query) executionElement;
				if (query.getAnnotations().size() > 0) {
					queryName = query.getAnnotations().get(0).getElement("name");
					if (!queryName.equals("")) {
						final StringBuilder stringBuilder = new StringBuilder();
						map.put(queryName, stringBuilder);
						executionPlanRuntime.addCallback(queryName, new QueryCallback() {
							@Override public void receive(long timeStamp, Event[] inEvents,
							                              Event[] removeEvents) {
								stringBuilder.append(gson.toJson(inEvents));
							}
						});
					}
				}
			}
		}
		//Stream Callback
		for(AbstractDefinition abstractDefinition:executionPlanRuntime.getStreamDefinitionMap().values()){
			String streamName=abstractDefinition.getId();
			final StringBuilder stringBuilder = new StringBuilder();
			map.put(streamName, stringBuilder);
			executionPlanRuntime.addCallback(streamName, new StreamCallback() {
				@Override public void receive(Event[] events) {
					stringBuilder.append(gson.toJson(events));
				}
			});
		}

		Pattern eventPattern, delayPattern;
		Matcher eventPatternMatcher, delayPatternMatcher;
		int eventStreamAttributeListSize;
		String[] eventStreamAttributeArray;
		String inputStreamName;

		//Send event stream
		try {
			String[] inputStreamEventArray = eventStream.split("\\r?\\n");

			for (int k = 0; k < inputStreamEventArray.length; k++) {
				eventPattern = Pattern.compile("(\\S+)=\\[(.*)\\]");
				eventPatternMatcher = eventPattern.matcher(
						inputStreamEventArray[k].replaceAll("\\s", ""));
				delayPattern = Pattern.compile("(delay\\()(\\d)+");
				delayPatternMatcher = delayPattern.matcher(inputStreamEventArray[k]);

				if (eventPatternMatcher.find()) {
					inputStreamName = eventPatternMatcher.group(1);
					InputHandler inputHandler =
							executionPlanRuntime.getInputHandler(inputStreamName);

					eventStreamAttributeArray = eventPatternMatcher.group(2).split(",");
					eventStreamAttributeListSize = eventStreamAttributeArray.length;

					Object object[] = new Object[eventStreamAttributeListSize];
					for (int l = 0; l < eventStreamAttributeListSize; l++) {
						Attribute.Type attributeType =
								executionPlanRuntime.getStreamDefinitionMap().get(inputStreamName)
								                    .getAttributeList().get(l).getType();
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
								object[l] = eventStreamAttributeArray[l];
								break;
						}
					}
					if (k == 0) {
						executionPlanRuntime.start();
						inputHandler.send(beginSetTime, object);
					} else {
						inputHandler.send((beginSetTime +
						                   (System.currentTimeMillis() - beginSystemTime)), object);
					}
				} else if (delayPatternMatcher.find()) {
					Thread.sleep(Long.parseLong(delayPatternMatcher.group(2)));
				} else {
					if (!inputStreamEventArray[k].equals("")) {
						executionPlanRuntime.shutdown();
						errMsg = "You have an error in your event stream \"  " +
						         inputStreamEventArray[k] +
						         "\n\"." +
						         " Expected format: &lt;eventStreamName&gt;=[&lt;attribute1&gt;,&lt;attribute2&gt;]";
						throw new Exception(errMsg);
					}
				}
			}
			Thread.sleep(500);
			return map;
		} catch (Exception e) {
			errMsg = "Error occurred while processing. " + e.getMessage();
			log.error(errMsg, e);
			throw new Exception(errMsg, e);
		} finally {
			executionPlanRuntime.shutdown();
		}
	}

	/**
	 * Create time stamp for the given date and time
	 *
	 * @param dateTime date and time to begin the process
	 */
	private long createTimeStamp(String dateTime) throws Exception {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date;
		try {
			date = dateFormatter.parse(dateTime);
		} catch (Exception e) {
			errMsg = "Error occurred while parsing date " + e.getMessage();
			log.error(errMsg, e);
			throw new Exception(errMsg, e);
		}
		long timeStamp = date.getTime();
		return timeStamp;
	}
}

