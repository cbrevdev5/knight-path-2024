package org.cbrevdev.knightpath.common.repository;

import org.cbrevdev.knightpath.common.entity.KnightMove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Optional;

@Repository
@Slf4j
public class KnightMoveRepository {
    private final DynamoDbTable<KnightMove> table;

    public KnightMoveRepository(
            @Value("${amazon.dynamodb.tablename-prefix}") String tableNamePrefix,
            @Value("${amazon.dynamodb.tablename-suffix}") String tableNameSuffix,
            DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        String tableName = tableNamePrefix + KnightMove.TABLE_NAME + tableNameSuffix;
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(KnightMove.class));
    }

    public KnightMove findById(String operationId) {
        Key key = Key.builder()
                .partitionValue(operationId)
                .build();
        return table.getItem(key);
    }

    public KnightMove findBySourceTarget(String source, String target) {
        String gsi = KnightMove.generateSource2Target(source, target);
        Key key = Key.builder().partitionValue(gsi).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        Iterable<Page<KnightMove>> byGSI = table.index(KnightMove.GSI_INDEX).query(queryConditional);
        return iterateToFirst(byGSI);
    }

    private KnightMove iterateToFirst(Iterable<Page<KnightMove>> results) {
        var iterator = results.iterator();
        while (iterator.hasNext()) {
            var page = iterator.next();
            var items = page.items();
            Optional<KnightMove> first = items.stream().findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }

    public KnightMove save(KnightMove knightMove) {
        return table.updateItem(knightMove);
    }

    public void delete(String operationId) {
        Key key = Key.builder()
                .partitionValue(operationId)
                .build();
        table.deleteItem(key);
    }
}
