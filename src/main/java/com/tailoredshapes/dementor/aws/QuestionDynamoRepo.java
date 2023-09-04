package com.tailoredshapes.dementor.aws;

import com.tailoredshapes.dementor.QuestionRepo;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.File;
import java.util.Map;

public class QuestionDynamoRepo implements QuestionRepo {

    private final String tableName;
    private final DynamoDbClient dynamoDb;

    private record Question( String pk, String questionText, String category, int order){}

    private record QuestionAssignment(String pk, String chatId, String questionId, String answerId, int timestamp){}
    private record Answer(String pk, String chatId, String answerText, String answerFile, int timestamp, boolean deleted){}

    public QuestionDynamoRepo(String tableName, DynamoDbClient dynamoDb){
        this.tableName = tableName;
        this.dynamoDb = dynamoDb;
    }

    @Override
    public String getNextQuestion(long chatId) {
        int order = 0;
        Question question = getLatest(chatId);
        if(question != null){
            order = question.order;
        }
        Question next = getNextQuestionByOrder(order);
        if(next != null){
            assignQuestionToChat(chatId, next);
        }
        return next.questionText;
    }

    @Override
    public String undoQuestion(long chatId) {
        QuestionAssignment qa = getLatestAnswered(chatId);
        Answer a = getAnswer(qa.answerId);
        Question q = getQuestion(qa.questionId);

        deleteAnswer(a);
        assignQuestionToChat(chatId, q);

        return q.questionText;
    }

    @Override
    public void answerQuestion(long chatId, String answer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void audiblyAnswerQuestion(long chatId, File audio) {
        throw new UnsupportedOperationException();
    }

    public Question getLatest(long chatId) {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("PK = :pk")
                .expressionAttributeValues(Map.of(":pk", AttributeValue.builder().s("QA#" + chatId).build()))
                //.expressionAttributeNames(Map.of("#o", "Order"))
                .limit(1) // Get the latest record only
                .scanIndexForward(false) // Order by sort key descending
                .build();

        QueryResponse queryResponse = dynamoDb.query(queryRequest);
        // Assuming that the question's order is stored in the question assignment record
        if (queryResponse.items().isEmpty()) return null;

        Map<String, AttributeValue> item = queryResponse.items().get(0);
        return new Question(
                item.get("PK").s(),
                item.get("QuestionText").s(),
                item.get("Category").s(),
                Integer.parseInt(item.get("Order").n())
        );
    }

    public Question getNextQuestionByOrder(int order) {
        // Assuming that the order is a global secondary index
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("OrderIndex")
                .keyConditionExpression("#o = :order")
                .expressionAttributeValues(Map.of(":order", AttributeValue.builder().n(String.valueOf(order + 1)).build()))
                .expressionAttributeNames(Map.of("#o", "Order"))
                .build();

        QueryResponse queryResponse = dynamoDb.query(queryRequest);

        if (queryResponse.items().isEmpty()) return null;

        Map<String, AttributeValue> item = queryResponse.items().get(0);
        return new Question(
                item.get("PK").s(),
                item.get("QuestionText").s(),
                item.get("Category").s(),
                Integer.parseInt(item.get("Order").n())
        );
    }

    public Question getQuestion(String questionId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PK", AttributeValue.builder().s("Q#" + questionId).build()))
                .build();

        Map<String, AttributeValue> item = dynamoDb.getItem(request).item();
        return new Question(
                item.get("PK").s(),
                item.get("QuestionText").s(),
                item.get("Category").s(),
                Integer.parseInt(item.get("Order").n())
        );
    }

    public QuestionAssignment getLatestAnswered(long chatId) {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("PK = :pk AND AnswerId <> :nullAnswer")
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s("QA#" + chatId).build(),
                        ":nullAnswer", AttributeValue.builder().s("NULL").build()
                ))
                .limit(1)
                .scanIndexForward(false)
                .build();

        QueryResponse queryResponse = dynamoDb.query(queryRequest);
        if (queryResponse.items().isEmpty()) return null;

        Map<String, AttributeValue> item = queryResponse.items().get(0);
        return new QuestionAssignment(
                item.get("PK").s(),
                item.get("ChatId").s(),
                item.get("QuestionId").s(),
                item.get("AnswerId").s(),
                Integer.parseInt(item.get("Timestamp").n())
        );
    }

    protected Answer getAnswer(String answerId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PK", AttributeValue.builder().s("A#" + answerId).build()))
                .build();

        Map<String, AttributeValue> item = dynamoDb.getItem(request).item();
        return new Answer(
                item.get("PK").s(),
                item.get("ChatId").s(),
                item.get("AnswerText").s(),
                item.get("AnswerFile").s(),
                Integer.parseInt(item.get("Timestamp").n()),
                item.get("Deleted").bool()
        );
    }

    protected void deleteAnswer(Answer a) {
        DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("PK", AttributeValue.builder().s(a.pk).build()))
                .build();

        dynamoDb.deleteItem(deleteRequest);
    }

    protected void assignQuestionToChat(long chatId, Question q) {
        // Record the question assignment for the given chat ID
        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(Map.of(
                        "PK", AttributeValue.builder().s("QA#" + chatId).build(),
                        "QuestionId", AttributeValue.builder().s(q.pk).build(),
                        "Timestamp", AttributeValue.builder().n(String.valueOf(System.currentTimeMillis())).build()
                ))
                .build();

        dynamoDb.putItem(putRequest);
    }

}
