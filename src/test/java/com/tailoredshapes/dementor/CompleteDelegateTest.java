package com.tailoredshapes.dementor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompleteDelegateTest {

    private String completedMessage;
    private TelegramBot bot;
    private CompleteDelegate delegate;

    @BeforeEach
    void setUp() {
        completedMessage = "Test Completed Message";
        bot = mock(TelegramBot.class);
        delegate = new CompleteDelegate(completedMessage, bot);
    }

    @Test
    void handle_success() {
        Long chatId = 1L;

        // Assuming bot.execute returns SendResponse, you might need to modify this line
        SendResponse response = mock(SendResponse.class);
        when(bot.execute(any(SendMessage.class))).thenReturn(response);

        boolean result = delegate.handle(chatId);

        assertTrue(result);
        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    void handle_failure() {
        Long chatId = 1L;

        // Simulate failure by throwing an exception
        doThrow(new RuntimeException("Test Exception")).when(bot).execute(any(SendMessage.class));

        boolean result = delegate.handle(chatId);

        assertFalse(result);
        verify(bot).execute(any(SendMessage.class));
    }
}
