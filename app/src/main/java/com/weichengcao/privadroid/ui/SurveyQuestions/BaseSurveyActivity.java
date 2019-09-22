package com.weichengcao.privadroid.ui.SurveyQuestions;

import java.util.HashMap;

public interface BaseSurveyActivity {

    void setUpSubmit();

    boolean validateAnswerBasedOnQuestionId(int questionId);

    void setUpAnswerBasedOnSpinnerId(int spinnerId);

    HashMap<String, String> gatherResponse();
}
