package com.tailoredshapes.dementor.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.interceptors.TracingInterceptor;
import com.pengrad.telegrambot.TelegramBot;
import com.serverless.ApiGatewayResponse;
import com.serverless.Environment;
import com.tailoredshapes.dementor.WebhookDelegate;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.HashMap;
import java.util.Map;

public class WebhookHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

    static {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard();
        AWSXRay.setGlobalRecorder(builder.build());
    }

    private final WebhookDelegate webhookDelegate;

    private final String welcomeText = "Welcome to Dementor. \n We're going to ask you hundreds of questions to help your bot become more like you.";

    public WebhookHandler() {
        String apiKey = Environment.getVariable("TELEGRAM_API_KEY");
        String welcomeText = Environment.getVariable("WELCOME_TEXT");

        boolean enableTelegram = Boolean.parseBoolean(Environment.getVariable("TELEGRAM_ENABLE"));

        var telegramBot = new TelegramBot(apiKey);
        HashMap<String, String> commandToQueue = new HashMap<>();
        commandToQueue.put("question", Environment.getVariable("QUESTION_QUEUE"));
        commandToQueue.put("next", Environment.getVariable("NEXT_QUEUE"));
        commandToQueue.put("answer", Environment.getVariable("ANSWER_QUEUE"));
        commandToQueue.put("undo", Environment.getVariable("UNDO_QUEUE"));

        var sqs = SqsClient.builder().overrideConfiguration(ClientOverrideConfiguration.builder().addExecutionInterceptor(new TracingInterceptor()).build()).build();

        SQSMessageSender sqsMessageSender = new SQSMessageSender(sqs);

        webhookDelegate = new WebhookDelegate(telegramBot, welcomeText, enableTelegram, commandToQueue, sqsMessageSender);
    }

    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        return webhookDelegate.handleRequest(input, context);
    }
}


