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
package org.wso2.carbon.event.processor.core.internal.ha.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.wso2.carbon.event.processor.core.ExecutionPlan;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ha.HAManager;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.service.HAManagementService;

public class HAManagementServiceImpl implements HAManagementService.Iface {

    private static final Log log = LogFactory.getLog(HAManagementServiceImpl.class);

    private CarbonEventProcessorService carbonEventProcessorService;

    public HAManagementServiceImpl(CarbonEventProcessorService carbonEventProcessorService) {

        this.carbonEventProcessorService = carbonEventProcessorService;
    }

    @Override
    public SnapshotData takeSnapshot(int tenantId, String executionPlanName, CEPMembership passiveMember) throws NotAnActiveMemberException, InternalServerException, TException {
        try {
            ExecutionPlan executionPlan = carbonEventProcessorService.getActiveExecutionPlan(executionPlanName, tenantId);

            HAManager haManager = executionPlan.getHaManager();
            if (!haManager.isActiveMember()) {
                throw new NotAnActiveMemberException("ExecutionPlanName:" + executionPlanName + " not active on tenant:" + tenantId);
            }
            org.wso2.carbon.event.processor.core.internal.ha.SnapshotData snapshotData = haManager.getActiveSnapshotData();

            SnapshotData snapshotDataOut = new SnapshotData();
            snapshotDataOut.setStates(snapshotData.getStates());
            snapshotDataOut.setNextEventData(snapshotData.getNextEventData());

            log.info("Snapshot provided to "+passiveMember.getHost()+":"+passiveMember.getPort()+" for tenant:"+tenantId+" on:"+executionPlanName);

            return snapshotDataOut;
        } catch (Throwable t) {
            throw new InternalServerException(t.getMessage());
        }
    }
}
