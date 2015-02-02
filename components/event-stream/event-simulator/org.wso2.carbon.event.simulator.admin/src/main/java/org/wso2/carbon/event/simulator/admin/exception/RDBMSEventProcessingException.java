package org.wso2.carbon.event.simulator.admin.exception;

/**
 * this class represents the Exception which can be when events are processing
 */
public class RDBMSEventProcessingException extends Exception{

    public RDBMSEventProcessingException() {
    }

    public RDBMSEventProcessingException(String message) {
        super(message);
    }

    public RDBMSEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RDBMSEventProcessingException(Throwable cause) {
        super(cause);
    }
}
