package com.tailoredshapes.dementor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class QuestionDelegateTest {

    @Mock
    private QuestionRepo repo;

    @Mock
    private MessageSender sender;

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private QuestionDelegate questionDelegate;

    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        questionDelegate = new QuestionDelegate(repo, sender, "questionTopic", telegramBot);
    }

    @Test
    void shouldSendNextQuestionIfAvailable() {
        String nextQuestion = "What is your favorite color?";
        when(repo.getNextQuestion(chatId)).thenReturn(nextQuestion);
        SendResponse sendResponse = mock(SendResponse.class);
        when(telegramBot.execute(any(SendMessage.class))).thenReturn(sendResponse);

        assertTrue(questionDelegate.handle(chatId));

        verify(telegramBot).execute(any(SendMessage.class));
        verify(sender, never()).sendMessage(eq(chatId), isNull(), anyString());
    }

    @Test
    void shouldSendMessageIfNoNextQuestion() {
        when(repo.getNextQuestion(chatId)).thenReturn(null);

        assertTrue(questionDelegate.handle(chatId));

        verify(sender).sendMessage(eq(chatId), isNull(), eq("questionTopic"));
        verify(telegramBot, never()).execute(any(SendMessage.class));
    }

    @Test
    void shouldReturnFalseOnException() {
        when(repo.getNextQuestion(chatId)).thenThrow(RuntimeException.class);

        assertFalse(questionDelegate.handle(chatId));

        verify(telegramBot, never()).execute(any(SendMessage.class));
        verify(sender, never()).sendMessage(eq(chatId), isNull(), anyString());
    }
}
