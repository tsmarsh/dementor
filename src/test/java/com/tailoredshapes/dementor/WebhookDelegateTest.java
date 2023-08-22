package com.tailoredshapes.dementor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.HashMap;
import java.util.Map;

import com.serverless.ApiGatewayResponse;
import com.tailoredshapes.dementor.aws.SQSMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class WebhookDelegateTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private Context context;

    @Mock
    private MessageSender sender;

    private WebhookDelegate webhookDelegate;
    private HashMap<String, String> commandToQueue;

    @BeforeEach
    void setUp() {
        commandToQueue = new HashMap<>();
        commandToQueue.put("question", "questionQueue");
        commandToQueue.put("next", "nextQueue");
        commandToQueue.put("answer", "answerQueue");
        commandToQueue.put("undo", "undoQueue");

        MockitoAnnotations.openMocks(this);
        webhookDelegate = new WebhookDelegate(telegramBot, "Live Long and Prosper", true, commandToQueue, sender);
    }

    @Test
    void testHandleRequestWithStartCommand() {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("body", telegramPayload);
        when(telegramBot.execute(any(SendMessage.class))).thenReturn(null);

        // When
        ApiGatewayResponse response = webhookDelegate.handleRequest(input, context);

        // Then
        assertEquals(200, response.getStatusCode());
        assertEquals("{\"success\":true}", response.getBody());
        verify(telegramBot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testHandleRequestWithError() {
        // Given
        String body = telegramPayload;
        Map<String, Object> input = new HashMap<>();
        input.put("body", body);
        when(telegramBot.execute(any(SendMessage.class))).thenThrow(new RuntimeException("Test Error"));

        // When
        ApiGatewayResponse response = webhookDelegate.handleRequest(input, context);

        // Then
        assertEquals(500, response.getStatusCode());
        assertEquals("{\"success\":false}", response.getBody());
        verify(telegramBot, times(1)).execute(any(SendMessage.class));
    }

    private final String telegramPayload = "{\n" +
                                           "  \"update_id\": 123456789,\n" +
                                           "  \"message\": {\n" +
                                           "    \"message_id\": 100,\n" +
                                           "    \"from\": {\n" +
                                           "      \"id\": 12345,\n" +
                                           "      \"first_name\": \"John\",\n" +
                                           "      \"last_name\": \"Doe\",\n" +
                                           "      \"username\": \"johndoe\",\n" +
                                           "      \"language_code\": \"en\"\n" +
                                           "    },\n" +
                                           "    \"chat\": {\n" +
                                           "      \"id\": 54321,\n" +
                                           "      \"type\": \"private\",\n" +
                                           "      \"title\": \"MyChat\",\n" +
                                           "      \"username\": \"my_chat\",\n" +
                                           "      \"first_name\": \"My\",\n" +
                                           "      \"last_name\": \"Chat\"\n" +
                                           "    },\n" +
                                           "    \"date\": 1609459200,\n" +
                                           "    \"text\": \"/start\"\n" +
                                           "  }\n" +
                                           "}\n";
}

