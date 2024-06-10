package org.cbrevdev.knightpath.operation;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import java.util.List;

public class OperationFunction implements RequestHandler<SQSEvent, Void> {
    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        String[] operationIds = new String[records.size()];
        int index = 0;
        context.getLogger().log("Start processing " + records.size() + " operations.");
        for (SQSEvent.SQSMessage msg : records) {
            operationIds[index++] = msg.getBody();
        }
        context.getLogger().log("OperationIds: " + String.join(", ", operationIds));
        OperationApplication.main(operationIds);
        context.getLogger().log("Completed.");
        return null;
    }
}
