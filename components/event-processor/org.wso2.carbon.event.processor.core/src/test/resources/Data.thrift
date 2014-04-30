namespace java org.wso2.carbon.event.processor.core.internal.ha.thrift.data


struct CEPMembership{
    1: optional string host;
    2: optional i32 port;
}


struct SnapshotData{
    1: optional binary states;
    2: optional binary nextEventData;
}


