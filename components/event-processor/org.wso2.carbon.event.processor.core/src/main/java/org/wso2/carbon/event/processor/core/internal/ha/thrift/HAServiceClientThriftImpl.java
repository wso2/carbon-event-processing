package org.wso2.carbon.event.processor.core.internal.ha.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.event.processor.core.internal.ha.CEPMembership;
import org.wso2.carbon.event.processor.core.internal.ha.HAServiceClient;
import org.wso2.carbon.event.processor.core.internal.ha.SnapshotData;
import org.wso2.carbon.event.processor.core.internal.ha.server.utils.HostAddressFinder;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.InternalServerException;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.exception.NotAnActiveMemberException;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.service.HAManagementService;

import java.net.SocketException;

public class HAServiceClientThriftImpl implements HAServiceClient {

    private static final Log log = LogFactory.getLog(HAServiceClientThriftImpl.class);

    @Override
    public SnapshotData getSnapshot(int tenantId, String executionPlan, CEPMembership activeCepMembership, CEPMembership currentCepMembership) throws Exception {


        TTransport receiverTransport = null;
        receiverTransport = new TSocket((activeCepMembership.getHost()), activeCepMembership.getPort());
        TProtocol protocol = new TBinaryProtocol(receiverTransport);
        HAManagementService.Client client = new HAManagementService.Client(protocol);
        receiverTransport.open();

        org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership cepMembershipOut = new org.wso2.carbon.event.processor.core.internal.ha.thrift.data.CEPMembership();
        cepMembershipOut.setHost(currentCepMembership.getHost());
        cepMembershipOut.setPort(currentCepMembership.getPort());

        try {
            log.info("Requesting snapshot from "+activeCepMembership.getHost()+":"+activeCepMembership.getPort()+" for tenant:"+tenantId+" on:"+executionPlan);

            org.wso2.carbon.event.processor.core.internal.ha.thrift.data.SnapshotData snapshotDataIn = client.takeSnapshot(tenantId, executionPlan, cepMembershipOut);
            log.info("Snapshot received for tenant:"+tenantId+" on:"+executionPlan);

            return new SnapshotData(snapshotDataIn.getNextEventData(), snapshotDataIn.getStates());

        } catch (NotAnActiveMemberException e) {
            throw new Exception("CEP member :" + activeCepMembership.getHost() + ":" + activeCepMembership.getPort() + " is not an ActiveMember, " + e.getMessage());
        } catch (InternalServerException e) {
            throw new Exception("Internal server error occurred at CEP member :" + activeCepMembership.getHost() + ":" + activeCepMembership.getPort() + ", " + e.getMessage());
        } catch (TException e) {
            throw new Exception("Thrift error occurred when communicating to CEP member :" + activeCepMembership.getHost() + ":" + activeCepMembership.getPort() + ", " + e.getMessage());
        }
    }
}
