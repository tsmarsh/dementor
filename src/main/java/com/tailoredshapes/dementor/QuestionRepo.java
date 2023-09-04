package com.tailoredshapes.dementor;

import java.io.File;

public interface QuestionRepo {
    String getNextQuestion(long chatId);
    String undoQuestion(long chatId);
    void answerQuestion(long chatId, String answer);
    void audiblyAnswerQuestion(long chatId, File audio);
}
