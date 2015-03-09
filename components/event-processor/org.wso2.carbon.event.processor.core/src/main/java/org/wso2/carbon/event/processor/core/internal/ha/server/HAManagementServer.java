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
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.event.processor.core.internal.CarbonEventProcessorService;
import org.wso2.carbon.event.processor.core.internal.ha.CEPMembership;
import org.wso2.carbon.event.processor.core.internal.ha.server.utils.HAManagementServerBuilder;
import org.wso2.carbon.event.processor.core.internal.ha.server.utils.HAManagementServerConstants;
import org.wso2.carbon.event.processor.core.internal.ha.thrift.service.HAManagementService;
import org.wso2.carbon.event.processor.common.util.Utils;

import java.net.InetSocketAddress;

public class HAManagementServer {
    private static final Log log = LogFactory.getLog(HAManagementServer.class);


    public HAManagementServer(CarbonEventProcessorService carbonEventProcessorService) {

        try {
            int portOffset = HAManagementServerBuilder.readPortOffset();
            HAManagementServerConfiguration haManagementServerConfiguration = new HAManagementServerConfiguration(HAManagementServerConstants.DEFAULT_RECEIVER_PORT + portOffset);
            HAManagementServerBuilder.populateConfigurations(portOffset, haManagementServerConfiguration, HAManagementServerBuilder.loadConfigXML());


            String hostName = haManagementServerConfiguration.getReceiverHostName();
            if (null == hostName) {
                hostName = Utils.findAddress("localhost");
            }
            haManagementServerConfiguration.setReceiverHostName(hostName);
            carbonEventProcessorService.addCurrentCEPMembership(new CEPMembership(hostName, haManagementServerConfiguration.getDataReceiverPort()));

            start(haManagementServerConfiguration, carbonEventProcessorService);

        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        } catch (Throwable e) {
            log.error("Error in starting Agent Server ", e);
        }
    }

    private void start(HAManagementServerConfiguration haManagementServerConfiguration, CarbonEventProcessorService carbonEventProcessorService) throws Exception {
        try {
            TServerSocket serverTransport = new TServerSocket(
                    new InetSocketAddress(haManagementServerConfiguration.getReceiverHostName(), haManagementServerConfiguration.getDataReceiverPort()));
            HAManagementService.Processor<HAManagementServiceImpl> processor =
                    new HAManagementService.Processor<HAManagementServiceImpl>(
                            new HAManagementServiceImpl(carbonEventProcessorService));
            TThreadPoolServer dataReceiverServer = new TThreadPoolServer(
                    new TThreadPoolServer.Args(serverTransport).processor(processor));
            Thread thread = new Thread(new ServerThread(dataReceiverServer));
            log.info("CEP HA Management Thrift Server started on " + haManagementServerConfiguration.getReceiverHostName() + ":" + haManagementServerConfiguration.getDataReceiverPort());
            thread.start();
        } catch (TTransportException e) {
            throw new Exception("Cannot start CEP HA Management Thrift server on port " + haManagementServerConfiguration.getDataReceiverPort() +
                    " on host " + haManagementServerConfiguration.getReceiverHostName(), e);
        }
    }

    static class ServerThread implements Runnable {
        private TServer server;

        ServerThread(TServer server) {
            this.server = server;
        }

        public void run() {
            this.server.serve();
        }
    }

}
