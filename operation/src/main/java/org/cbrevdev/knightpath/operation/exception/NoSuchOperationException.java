package org.cbrevdev.knightpath.operation.exception;

public class NoSuchOperationException extends Exception {
    public NoSuchOperationException(String operationId) {
        super("Cannot find operation with id = " + operationId);
    }
}
