package org.cbrevdev.knightpath.operation.exception;

import java.util.List;

public class BatchFailedException extends RuntimeException {
    public BatchFailedException(List<String> failedOperationIds) {
        super(String.join("; ", failedOperationIds));
    }
}
