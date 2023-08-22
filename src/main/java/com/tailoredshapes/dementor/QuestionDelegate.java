package com.tailoredshapes.dementor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public record QuestionDelegate(QuestionRepo repo, MessageSender sender, String questionTopic, TelegramBot telegramBot) {

    public boolean handle(Long chatId) {
        try {
            String nextQuestion = repo.getNextQuestion(chatId);
            if (nextQuestion != null){
                SendMessage sendMessage = new SendMessage(chatId, nextQuestion);
                telegramBot.execute(sendMessage);
            }else {
                sender.sendMessage(chatId, null, questionTopic);
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
