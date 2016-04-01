namespace java org.wso2.carbon.event.processor.common.storm.manager.service

include "Exception.thrift"

service StormManagerService {
    void registerStormReceiver(1: i32 tenantId, 2: string executionPlanName, 3: string hostName, 4: i32 port) throws (1:Exception.NotStormCoordinatorException nsme),
    void registerCEPPublisher(1: i32 tenantId, 2: string executionPlanName, 3: string hostName, 4: i32 port) throws (1:Exception.NotStormCoordinatorException nsme ),
    string getStormReceiver(1: i32 tenantId, 2: string executionPlanName, 3: string cepReceiverHostName) throws (1:Exception.NotStormCoordinatorException nsme, 2: Exception.EndpointNotFoundException enfe ),
    string getCEPPublisher(1: i32 tenantId, 2: string executionPlanName, 3: string stormPublisherHostName) throws (1:Exception.NotStormCoordinatorException nsme, 2: Exception.EndpointNotFoundException enfe )
}
