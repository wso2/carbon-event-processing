package org.wso2.carbon.event.processing.application.deployer.exceptions;

public class EventStreamDeploymentException extends RuntimeException {
    public EventStreamDeploymentException(String s, Exception e) {
        super(s,e);
    }
}
