package com.tailoredshapes.dementor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnswerDelegateTest {

    private QuestionRepo repo;
    private MessageSender sender;
    private String topic;
    private AnswerDelegate delegate;

    @BeforeEach
    void setUp() {
        repo = mock(QuestionRepo.class);
        sender = mock(MessageSender.class);
        topic = "Test Topic";
        delegate = new AnswerDelegate(repo, sender, topic);
    }

    @Test
    void handle_success() {
        long chatId = 1L;
        String answer = "Answer";

        // Simulate successful behavior
        when(repo.answerQuestion(chatId, answer)).thenReturn("Not used"); // Assuming it returns a boolean or void

        boolean result = delegate.handle(chatId, answer);

        assertTrue(result);
        verify(repo).answerQuestion(chatId, answer);
        verify(sender).sendMessage(chatId, null, topic);
    }

    @Test
    void handle_failure() {
        long chatId = 1L;
        String answer = "Answer";

        // Simulate failure by throwing an exception
        doThrow(new RuntimeException("Test Exception")).when(repo).answerQuestion(chatId, answer);

        boolean result = delegate.handle(chatId, answer);

        assertFalse(result);
        verify(repo).answerQuestion(chatId, answer);
        verify(sender, never()).sendMessage(anyLong(), any(), anyString()); // Ensure that sendMessage is never called on failure
    }
}
