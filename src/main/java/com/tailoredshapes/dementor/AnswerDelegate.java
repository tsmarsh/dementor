package com.tailoredshapes.dementor;

public class AnswerDelegate {
    private QuestionRepo repo;
    private MessageSender sender;
    private String topic;

    public AnswerDelegate(QuestionRepo repo, MessageSender sender, String topic) {
        this.repo = repo;
        this.sender = sender;
        this.topic = topic;
    }

    public boolean handle(long chatId, String answer){
        try {
            repo.answerQuestion(chatId, answer);
            sender.sendMessage(chatId, null, topic);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
