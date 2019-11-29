package com.weichengcao.privadroid.ui.SurveyQuestions;

import java.util.HashMap;

public interface BaseSurveyActivity {

    String OPTION_DELIMITER = ", ";

    void setUpSubmit();

    boolean validateAnswerBasedOnQuestionId(int questionId);

    void setUpAnswerBasedOnButtonId(int buttonId);

    HashMap<String, String> gatherResponse();

    void showQuestionOptionsDialog(int buttonId);
}
