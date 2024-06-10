package org.cbrevdev.knightpath;

import org.cbrevdev.knightpath.common.entity.KnightMove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cbrevdev.knightpath.common.repository.KnightMoveRepository;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnightService {
    private final KnightMoveRepository knightMoveRepository;
    private final SqsClient sqsClient;

    @Value("${amazon.sqs.queue}")
    private String sqsQueueName;

    @Value("${app.message-retry}")
    private int hours;

    public String createOperation(String source, String target) {
        KnightMove knightMove = knightMoveRepository.findBySourceTarget(source, target);
        if (knightMove == null) {
            String operationId = UUID.randomUUID().toString();
            knightMove = new KnightMove();
            knightMove.setOperationId(operationId);
            knightMove.setSource(source);
            knightMove.setTarget(target);
            knightMoveRepository.save(knightMove);
            sendSolveMessage(knightMove);
        } else if (knightMove.getNumberOfMoves() == 0 && shouldTryAgain(knightMove)) {
            sendSolveMessage(knightMove);
        }
        return knightMove.getOperationId();
    }

    private void sendSolveMessage(KnightMove knightMove) {
        String queueUrl = sqsClient.getQueueUrl(
                GetQueueUrlRequest.builder().queueName(sqsQueueName).build()
        ).queueUrl();
        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(knightMove.getOperationId())
                .build();
        sqsClient.sendMessage(messageRequest);
        log.info("Send message for operation: {}", knightMove.getOperationId());
    }

    public KnightMove findByOperationId(String operationId) {
        return knightMoveRepository.findById(operationId);
    }

    private boolean shouldTryAgain(KnightMove knightMove) {
        return knightMove.getCreatedAt().isBefore(Instant.now().minus(hours, ChronoUnit.HOURS));
    }
}
