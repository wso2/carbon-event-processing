/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org)
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
package org.wso2.carbon.event.processor.core.internal.storm.util;

import org.wso2.carbon.event.processor.core.exception.StormQueryConstructionException;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.RangePartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Class to hold necessary information regarding query grouping which is needed
 * for Storm query plan generation. Here single query is treated as a query group
 * with group id = query name. Also a Partition is considered as a group.
 */
public class QueryGroupInfoHolder {
    private String groupId;
    private List<String> stringQueries;
    private List<ExecutionElementInfoHolder> executionElements;
    private Set<String> inputDefinitionIds;
    private Set<String> outputDefinitionIds;
    private Map<String, String> partitionFieldMap = null;

    public QueryGroupInfoHolder(String groupId) {
        this.groupId = groupId;
        stringQueries = new ArrayList<String>();
        executionElements = new ArrayList<ExecutionElementInfoHolder>();
        inputDefinitionIds = new HashSet<String>();
        outputDefinitionIds = new HashSet<String>();
    }

    public void addQueryString(String query) {
        stringQueries.add(query);
    }

    public void addExecutionElement(ExecutionElementInfoHolder infoHolder) throws StormQueryConstructionException {
        if (infoHolder.getExecutionElement() instanceof Query) {
            inputDefinitionIds.addAll(((Query) infoHolder.getExecutionElement()).getInputStream().getUniqueStreamIds());
            outputDefinitionIds.add(((Query) infoHolder.getExecutionElement()).getOutputStream().getId());
        } else {
            Partition partition = (Partition) infoHolder.getExecutionElement();
            for (Query query : partition.getQueryList()) {
                for (String id : query.getInputStream().getUniqueStreamIds()) {
                    if (!id.contains("#")) {  //if not an inner stream
                        inputDefinitionIds.add(id);
                    }
                }
                outputDefinitionIds.add(query.getOutputStream().getId());
            }
            for (PartitionType type : partition.getPartitionTypeMap().values()) {
                if (type instanceof RangePartitionType) {
                    throw new StormQueryConstructionException("Error in deploying Partition:" + this.getStringQueries
                            ().get(0) + " Range partitioning is not supported in distributed deployment");
                } else if (type instanceof ValuePartitionType) {
                    if (partitionFieldMap == null) {
                        partitionFieldMap = new HashMap<String, String>();
                    }
                    if (((ValuePartitionType) type).getExpression() instanceof Variable) {
                        Variable variable = (Variable) ((ValuePartitionType) type).getExpression();
                        partitionFieldMap.put(type.getStreamId(), variable.getAttributeName());
                    } else {
                        throw new StormQueryConstructionException("Error in deploying partition:" + this
                                .getStringQueries().get(0) + ". Only Expressions of Type Variable will be admitted" +
                                " for distributed processing");
                    }
                }
            }
        }
        executionElements.add(infoHolder);
    }

    public List<String> getStringQueries() {
        return stringQueries;
    }

    public List<ExecutionElementInfoHolder> getExecutionElements() {
        return executionElements;
    }

    public Set<String> getInputDefinitionIds() {
        return inputDefinitionIds;
    }

    public Set<String> getOutputDefinitionIds() {
        return outputDefinitionIds;
    }

    public Map<String, String> getPartitionFieldMap() {
        return partitionFieldMap;
    }
}
