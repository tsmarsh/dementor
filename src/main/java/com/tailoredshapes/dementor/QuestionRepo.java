package com.tailoredshapes.dementor;

import java.io.File;

public interface QuestionRepo {
    String getNextQuestion(long chatId);
    String undoQuestion(long chatId);
    String answerQuestion(long chatId, String answer);
    String audiblyAnswerQuestion(long chatId, File audio);
}
