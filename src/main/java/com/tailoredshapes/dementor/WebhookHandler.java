package com.tailoredshapes.dementor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Voice;
import com.serverless.ApiGatewayResponse;
import com.serverless.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WebhookHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard();
        AWSXRay.setGlobalRecorder(builder.build());
    }

    private static final Logger LOG = LogManager.getLogger(WebhookHandler.class);

    private final DynamoDbClient dynamoDb;
    private final S3Client s3;

    private final String bucketName;

    public WebhookHandler() {
        dynamoDb = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .overrideConfiguration(ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build())
                .build();

        s3 = S3Client.builder().region(Region.US_EAST_1).overrideConfiguration(ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build()).build();

        bucketName = System.getenv("AUDIO_BUCKET");
    }

    public void handleTextAnswer(String questionId, String answerText) {

        HashMap<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("question_id", AttributeValue.builder().s(questionId).build());
        item.put("answer_text", AttributeValue.builder().s(answerText).build());

        var putRequest = PutItemRequest.builder().item(item).build();

        try {
            dynamoDb.putItem(putRequest);
        } catch (Exception e) {
            LOG.error("Failed to write item", e);
        }
    }

    public void handleAudioAnswer(String id, String questionId, File audioFile) {
        // Upload the audio file to S3
		String key = id + ".mp3";

		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        RequestBody body = RequestBody.fromFile(audioFile);

        s3.putObject(putObjectRequest, body);


        // S3 URL for the uploaded audio file
        String answerMp3File = "https://" + bucketName + ".s3.amazonaws.com/" + key;

        // Create an item for the answer
        Item item = new Item()
                .withPrimaryKey("id", id, "question_id", questionId)
                .withString("answer_mp3_file", answerMp3File);

        // Put the item in the DynamoDB table
        Table table = new DynamoDB(dynamoDBClient).getTable(tableName);
        table.putItem(item);
    }

    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        String body = (String) input.get("body");
        Update update = BotUtils.parseUpdate(body);
        Chat chat = update.message().chat();


        String text = update.message().text();
        Voice audio = update.message().voice();

        if (text != null) {

        }

        if (audio != null) {

        }

        Response responseBody = new Response("{\"success\": true}", input);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(responseBody)
                .build();
    }
}
