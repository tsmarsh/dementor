package com.tailoredshapes.dementor;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public record UndoDelegate(QuestionRepo repo, TelegramBot bot) {

    public boolean handle(Long chatId){
        try{
            String currentQuestion = repo.undoQuestion(chatId);
            SendMessage sendMessage = new SendMessage(chatId, currentQuestion);
            bot.execute(sendMessage);
        }catch (Exception e) {
            return false;
        }
        return true;
    }
}
