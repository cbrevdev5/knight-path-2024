package org.cbrevdev.knightpath.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbAutoGeneratedTimestampAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@DynamoDbBean
public class KnightMove {
    public static final String TABLE_NAME = "knight-move";
    public static final String GSI_INDEX = "knight-move-gsi";

    private String operationId;
    private String source;
    private String target;
    private int numberOfMoves;
    private String shortestPath;
    private String source2Target;
    private boolean unreachable;

    @Getter(onMethod_ = {@DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS), @DynamoDbAutoGeneratedTimestampAttribute})
    private Instant createdAt;

    @Getter(onMethod_ = {@DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS), @DynamoDbAutoGeneratedTimestampAttribute})
    private Instant updatedAt;


    @DynamoDbPartitionKey
    public String getOperationId() {
        return operationId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = GSI_INDEX)
    public String getSource2Target() {
        if (source2Target == null) {
            return generateSource2Target(source, target);
        }
        return source2Target;
    }

    public static String generateSource2Target(String source, String target) {
        return source + "#" + target;
    }
}
