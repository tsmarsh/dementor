package com.tailoredshapes.dementor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.xray.AWSXRay;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.serverless.ApiGatewayResponse;
import com.tailoredshapes.dementor.aws.WebhookHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.HashMap;
import java.util.Map;

public class WebhookDelegate implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {


    private static final Logger LOG = LogManager.getLogger(WebhookHandler.class);
    private final TelegramBot telegramBot;
    private final String welcomeText;
    private final boolean enableTelegram;
    private Map<String, String> commandToQueue;

    public WebhookDelegate(TelegramBot telegramBot, String welcomeText, boolean enableTelegram, Map<String, String> commandToqueue, MessageSender messageSender) {
        this.telegramBot = telegramBot;
        this.welcomeText = welcomeText;
        this.enableTelegram = enableTelegram;
        this.commandToQueue = commandToqueue;
    }

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        HashMap<String, Boolean> response = new HashMap<>();
        System.out.println("The fuck?");
        try {
            String body = (String) input.get("body");
            Update update = BotUtils.parseUpdate(body);
            Chat chat = update.message().chat();

            String message = update.message().text();
            if (message.startsWith("/")) {
                String[] split = message.split(" ");
                String command = split[0];
                switch (command) {
                    case "/start":
                        start(body, chat.id().toString());
                        break;
                    case "/next":
                        forward(body, chat.id().toString(), commandToQueue.get("next"));
                        break;
                    case "undo":
                        forward(body, chat.id().toString(), commandToQueue.get("undo"));
                        break;
                    default:
                        forward(body, chat.id().toString(), commandToQueue.get("answer"));
                }
            }
            response.put("success", true);
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(response)
                    .build();
        } catch (Exception e) {
            response.put("success", false);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody(response)
                    .build();
        }
    }

    void start(String body, String chatId) {
        try (var ss = AWSXRay.beginSubsegment("Sending welcome text")) {
            SendMessage sendMessage = new SendMessage(chatId, welcomeText);
            if (enableTelegram) {
                try {
                    telegramBot.execute(sendMessage);
                } catch (Exception e) {
                    ss.addException(e);
                    LOG.error("Failed to send welcome", e);
                    throw (e);
                }
            } else {
                LOG.info(welcomeText);
            }
        }
        forward(body, chatId, commandToQueue.get("question"));
    }

    void forward(String body, String chatId, String queue) {
        try {

        } catch(Exception e){
            LOG.error("Error putting message on queue", e);
        }
    }
}
