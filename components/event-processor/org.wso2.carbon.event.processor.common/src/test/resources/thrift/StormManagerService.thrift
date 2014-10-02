namespace java org.wso2.carbon.event.processor.storm.common.manager.service

include "Exception.thrift"

service StormManagerService {
    void registerStormReceiver(1: i32 tenantId, 2: string executionPlanName, 3: string hostName, 4: i32 port) throws (1:Exception.NotStormManagerException nsme),
    void registerCEPPublisher(1: i32 tenantId, 2: string executionPlanName, 3: string hostName, 4: i32 port) throws (1:Exception.NotStormManagerException nsme ),
    string getStormReceiver(1: i32 tenantId, 2: string executionPlanName, 3: string cepReceiverHostName) throws (1:Exception.NotStormManagerException nsme, 2: Exception.EndpointNotFoundException enfe ),
    string getCEPPublisher(1: i32 tenantId, 2: string executionPlanName, 3: string stormPublisherHostName) throws (1:Exception.NotStormManagerException nsme, 2: Exception.EndpointNotFoundException enfe )
}
