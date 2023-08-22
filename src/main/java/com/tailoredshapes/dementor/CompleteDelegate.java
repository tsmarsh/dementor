package com.tailoredshapes.dementor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public record CompleteDelegate(String completedMessage, TelegramBot bot) {

    public boolean handle(Long chatId){
        try {
            SendMessage sendMessage = new SendMessage(chatId, completedMessage);
            bot.execute(sendMessage);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
