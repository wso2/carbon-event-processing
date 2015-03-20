package org.wso2.carbon.event.simulator.admin.exception;

/**
 * this class represents the Exception which can cause when establishing RDBMS Database Connection
 */
public class RDBMSConnectionException extends Exception {

    public RDBMSConnectionException() {
    }

    public RDBMSConnectionException(String message) {
        super(message);
    }

    public RDBMSConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RDBMSConnectionException(Throwable cause) {
        super(cause);
    }
}
