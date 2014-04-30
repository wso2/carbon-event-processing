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
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.snapshot.SnapshotObject;

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
