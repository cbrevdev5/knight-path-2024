package org.cbrevdev.knightpath;

import lombok.extern.slf4j.Slf4j;
import org.cbrevdev.knightpath.common.entity.KnightMove;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.extensions.AutoGeneratedTimestampRecordExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.utils.StringUtils;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAsync
@Slf4j
public class AppConfig {
    @Value("${amazon.dynamodb.tablename-prefix}")
    private String tableNamePrefix;

    @Value("${amazon.dynamodb.tablename-suffix}")
    private String tableNameSuffix;

    @Value("${amazon.dynamodb.region}")
    private String region;

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.dynamodb.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.dynamodb.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.sqs.endpoint}")
    private String sqsEndpoint;

    @Value("${amazon.sqs.accesskey}")
    private String sqsAccessKey;

    @Value("${amazon.sqs.secretkey}")
    private String sqsSecretKey;

    @Value("${amazon.sqs.region}")
    private String sqsRegion;

    @Bean
    public DynamoDbClient getDynamoDbClient() {
        log.info("Creating DynamoDbClient for {}", (tableNamePrefix + KnightMove.TABLE_NAME + tableNameSuffix));

        if (!StringUtils.isBlank(amazonAWSAccessKey) && !StringUtils.isBlank(amazonAWSSecretKey) ) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(amazonAWSAccessKey, amazonAWSSecretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            return DynamoDbClient.builder()
                    .endpointOverride(URI.create(amazonDynamoDBEndpoint))
                    .region(Region.of(region))
                    .httpClient(UrlConnectionHttpClient.create())
                    .credentialsProvider(credentialsProvider).build();
        }

        return DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
                .extensions(AutoGeneratedTimestampRecordExtension.create())
                .dynamoDbClient(getDynamoDbClient())
                .build();
    }

    @Profile("local")
    @Bean
    public List<DynamoDbTable<?>> createTables(DynamoDbEnhancedClient enhancedClient, DynamoDbClient client) {
        String tableName = tableNamePrefix + KnightMove.TABLE_NAME + tableNameSuffix;

        DescribeTableRequest tableRequest = DescribeTableRequest.builder().tableName(tableName).build();

        DynamoDbTable<KnightMove> dynamoDbTable = enhancedClient.table(tableName, TableSchema.fromBean(KnightMove.class));

        try {
            dynamoDbTable.deleteTable();
            client.waiter().waitUntilTableNotExists(tableRequest);
        } catch (ResourceNotFoundException e) {
            // Do nothing
        }

        // Create New Tables
        try {
            ProvisionedThroughput tableIO = ProvisionedThroughput.builder().readCapacityUnits(5L)
                    .writeCapacityUnits(6L).build();
            CreateTableEnhancedRequest tableConfig = CreateTableEnhancedRequest.builder()
                    .globalSecondaryIndices(createGlobalSecondaryIndex(KnightMove.GSI_INDEX, tableIO))
                    .provisionedThroughput(tableIO)
                    .build();
            dynamoDbTable.createTable(tableConfig);
            client.waiter().waitUntilTableExists(tableRequest);
        } catch (ResourceInUseException e) {
            log.info("createTables(): Table already exist, skipping.");
        }

        List<DynamoDbTable<?>> tables = new ArrayList<>();
        tables.add(dynamoDbTable);

        return tables;
    }

    private EnhancedGlobalSecondaryIndex createGlobalSecondaryIndex(String indexName, ProvisionedThroughput tableIO) {
        Projection projection = Projection.builder()
                .projectionType(ProjectionType.INCLUDE)
                .nonKeyAttributes("createdAt", "numberOfMoves")
                .build();
        return EnhancedGlobalSecondaryIndex.builder()
                .indexName(indexName)
                .provisionedThroughput(tableIO)
                .projection(projection)
                .build();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())
                        .allowedOrigins("*")
                        .allowedHeaders(HttpHeaders.CONTENT_TYPE);
            }
        };
    }

    @Bean
    public SqsClient sqsClient() {
        if (!StringUtils.isBlank(sqsAccessKey) && !StringUtils.isBlank(sqsSecretKey) ) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(sqsAccessKey, sqsSecretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            return SqsClient.builder()
                    .endpointOverride(URI.create(sqsEndpoint))
                    .region(Region.of(sqsRegion))
                    .credentialsProvider(credentialsProvider)
                    .build();
        }
        return SqsClient.builder()
                .region(Region.of(sqsRegion))
                .build();
    }
}
