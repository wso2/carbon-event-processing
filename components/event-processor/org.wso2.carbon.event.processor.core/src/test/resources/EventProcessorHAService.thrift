namespace java org.wso2.carbon.event.processor.core.internal.ha.thrift.service

include "Data.thrift"
include "Exception.thrift"

service HAManagementService {
    Data.SnapshotData takeSnapshot(1: i32 tenantId, 2: string executionPlan,  3: Data.CEPMembership passiveMember ) throws  (1:Exception.NotAnActiveMemberException anme, 2:Exception.InternalServerException ise)

}