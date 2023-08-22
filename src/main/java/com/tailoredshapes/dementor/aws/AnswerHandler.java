package com.tailoredshapes.dementor.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.serverless.ApiGatewayResponse;
import com.serverless.Environment;
import com.tailoredshapes.dementor.AnswerDelegate;
import com.tailoredshapes.dementor.WebhookDelegate;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnswerHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard();
        AWSXRay.setGlobalRecorder(builder.build());
    }

    private final AnswerDelegate answerDelegate;

    public AnswerHandler() {
        String topic = Environment.getVariable("QUESTION_QUEUE");
        String tableName = Environment.getVariable("TABLE_NAME");


        var dynamoDb = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .overrideConfiguration(ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .build();

        var sqs = SqsClient.builder().overrideConfiguration(ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build()).build();

        SQSMessageSender sqsMessageSender = new SQSMessageSender(sqs);
        QuestionDynamoRepo repo = new QuestionDynamoRepo(tableName, dynamoDb);

        answerDelegate = new AnswerDelegate(repo, sqsMessageSender, topic);
    }

    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();

        event.getRecords().forEach((record) -> {
            String body = record.getBody();
            Update update = BotUtils.parseUpdate(body);
            Long chatId = update.message().chat().id();
            String answer = update.message().text();

            var success = answerDelegate.handle(chatId, answer);
            if(!success){
                SQSBatchResponse.BatchItemFailure failure = SQSBatchResponse.BatchItemFailure.builder().withItemIdentifier(record.getMessageId()).build();
                failures.add(failure);
            }
        });

        return new SQSBatchResponse(failures);
    }
}


