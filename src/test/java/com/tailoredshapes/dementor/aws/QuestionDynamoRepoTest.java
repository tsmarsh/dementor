package com.tailoredshapes.dementor.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuestionDynamoRepoTest {
    private static LocalStackContainer localstack;
    private DynamoDbClient dynamoDbClient;
    private QuestionDynamoRepo repo;
    private final String tableName = "your-table-name";

    @BeforeAll
    public static void setUpClass() {
        localstack = new LocalStackContainer(
                DockerImageName.parse("localstack/localstack:2.2.0")).withServices(
                LocalStackContainer.Service.DYNAMODB
        ).withStartupTimeout(Duration.ofMinutes(2));
        localstack.start();
    }

    @AfterAll
    public static void tearDownClass() {
        localstack.stop();
    }

    @BeforeEach
    public void setUp() {
        dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                ).region(Region.of(localstack.getRegion()))
                .build();
        repo = new QuestionDynamoRepo(tableName, dynamoDbClient);

        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName("your-table-name")
                .keySchema(KeySchemaElement.builder()
                        .attributeName("PK")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("PK")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .build();

        dynamoDbClient.createTable(createTableRequest);

        // Add a few questions to simulate real data
        putQuestion("Q#1", "Question 1 text", "Category 1", 0);
        putQuestion("Q#2", "Question 2 text", "Category 1", 1);
        putQuestion("Q#3", "Question 3 text", "Category 2", 2);
    }

    @Test
    public void testGetNextQuestion() {
        String nextQuestion = repo.getNextQuestion(1234L);
        assertEquals("Question 1 text", nextQuestion);

        nextQuestion = repo.getNextQuestion(1234L);
        assertEquals("Question 2 text", nextQuestion);

        nextQuestion = repo.getNextQuestion(1234L);
        assertEquals("Question 3 text", nextQuestion);
    }

    private void putQuestion(String pk, String questionText, String category, int order) {
        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName("your-table-name")
                .item(Map.of(
                        "PK", AttributeValue.builder().s(pk).build(),
                        "QuestionText", AttributeValue.builder().s(questionText).build(),
                        "Category", AttributeValue.builder().s(category).build(),
                        "Order", AttributeValue.builder().n(String.valueOf(order)).build()
                ))
                .build();

        dynamoDbClient.putItem(putRequest);
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources.
        dynamoDbClient.close();
    }


    // Other test cases.
}
