package com.tailoredshapes.dementor.aws;

import com.tailoredshapes.dementor.QuestionRepo;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.File;

public class QuestionDynamoRepo implements QuestionRepo {

    private String tableName;
    private DynamoDbClient dynamoDb;

    public QuestionDynamoRepo(String tableName, DynamoDbClient dynamoDb){
        this.tableName = tableName;
        this.dynamoDb = dynamoDb;
    }

    @Override
    public String getNextQuestion(long chatId) {
        return null;
    }

    @Override
    public String undoQuestion(long chatId) {
        return null;
    }

    @Override
    public String answerQuestion(long chatId, String answer) {
        return null;
    }

    @Override
    public String audiblyAnswerQuestion(long chatId, File audio) {
        return null;
    }
}
