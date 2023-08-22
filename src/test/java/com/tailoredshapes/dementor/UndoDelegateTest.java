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

class UndoDelegateTest {

    @Mock
    private QuestionRepo repo;

    @Mock
    private TelegramBot bot;

    @InjectMocks
    private UndoDelegate undoDelegate;

    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        undoDelegate = new UndoDelegate(repo, bot);
    }

    @Test
    void shouldSendCurrentQuestionOnUndo() {
        String currentQuestion = "What is your favorite movie?";
        when(repo.undoQuestion(chatId)).thenReturn(currentQuestion);
        SendResponse sendResponse = mock(SendResponse.class);
        when(bot.execute(any(SendMessage.class))).thenReturn(sendResponse);

        assertTrue(undoDelegate.handle(chatId));

        verify(bot).execute(any(SendMessage.class));
    }

    @Test
    void shouldReturnFalseOnException() {
        when(repo.undoQuestion(chatId)).thenThrow(RuntimeException.class);

        assertFalse(undoDelegate.handle(chatId));

        verify(bot, never()).execute(any(SendMessage.class));
    }
}
