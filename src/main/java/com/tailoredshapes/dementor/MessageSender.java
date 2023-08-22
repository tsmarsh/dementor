package com.tailoredshapes.dementor;

public interface MessageSender {
    void sendMessage(Long chatId, String message, String topic);
}
