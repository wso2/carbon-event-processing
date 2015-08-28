package org.wso2.carbon.event.processor.core.internal.storm.status.monitor.exception;

public class DeploymentStatusMonitorException extends Exception{

    public DeploymentStatusMonitorException(){
    }

    public DeploymentStatusMonitorException(String message){
        super(message);
    }
}
