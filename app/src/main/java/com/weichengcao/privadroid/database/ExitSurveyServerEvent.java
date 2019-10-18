package com.weichengcao.privadroid.database;

import static com.weichengcao.privadroid.ui.SurveyQuestions.BaseSurveyActivity.OPTION_DELIMITER;

public class ExitSurveyServerEvent {

    private String adId;
    private String loggedTime;

    private String controlOne;
    private String controlTwo;
    private String controlThree;

    public String getControlOne() {
        return controlOne;
    }

    public String getControlTwo() {
        return controlTwo;
    }

    public String getControlThree() {
        return controlThree;
    }

    private String awarenessOne;
    private String awarenessTwo;
    private String awarenessThree;

    public String getAwarenessOne() {
        return awarenessOne;
    }

    public String getAwarenessTwo() {
        return awarenessTwo;
    }

    public String getAwarenessThree() {
        return awarenessThree;
    }

    private String collectionOne;
    private String collectionTwo;
    private String collectionThree;

    public String getCollectionOne() {
        return collectionOne;
    }

    public String getCollectionTwo() {
        return collectionTwo;
    }

    public String getCollectionThree() {
        return collectionThree;
    }

    private String errorOne;
    private String errorTwo;
    private String errorThree;
    private String errorFour;

    public String getErrorOne() {
        return errorOne;
    }

    public String getErrorTwo() {
        return errorTwo;
    }

    public String getErrorThree() {
        return errorThree;
    }

    public String getErrorFour() {
        return errorFour;
    }

    private String secondaryUseOne;
    private String secondaryUseTwo;
    private String secondaryUseThree;
    private String secondaryUseFour;
    private String secondaryUseFive;

    public String getSecondaryUseOne() {
        return secondaryUseOne;
    }

    public String getSecondaryUseTwo() {
        return secondaryUseTwo;
    }

    public String getSecondaryUseThree() {
        return secondaryUseThree;
    }

    public String getSecondaryUseFour() {
        return secondaryUseFour;
    }

    public String getSecondaryUseFive() {
        return secondaryUseFive;
    }

    private String improperOne;
    private String improperTwo;
    private String improperThree;

    public String getImproperOne() {
        return improperOne;
    }

    public String getImproperTwo() {
        return improperTwo;
    }

    public String getImproperThree() {
        return improperThree;
    }

    private String globalOne;
    private String globalTwo;
    private String globalThree;
    private String globalFour;
    private String globalFive;

    public String getGlobalOne() {
        return globalOne;
    }

    public String getGlobalTwo() {
        return globalTwo;
    }

    public String getGlobalThree() {
        return globalThree;
    }

    public String getGlobalFour() {
        return globalFour;
    }

    public String getGlobalFive() {
        return globalFive;
    }

    private String familiar;
    private String dontKnowPermissions;

    public String getFamiliar() {
        return familiar;
    }

    public String[] getDontKnowPermissions() {
        return dontKnowPermissions.split(OPTION_DELIMITER);
    }

    public ExitSurveyServerEvent(String adId, String loggedTime,
                                 String controlOne, String controlTwo, String controlThree,
                                 String awarenessOne, String awarenessTwo, String awarenessThree,
                                 String collectionOne, String collectionTwo, String collectionThree,
                                 String errorOne, String errorTwo, String errorThree, String errorFour,
                                 String secondaryUseOne, String secondaryUseTwo, String secondaryUseThree, String secondaryUseFour, String secondaryUseFive,
                                 String improperOne, String improperTwo, String improperThree,
                                 String globalOne, String globalTwo, String globalThree, String globalFour, String globalFive,
                                 String familiar, String dontKnowPermissions) {
        this.adId = adId;
        this.loggedTime = loggedTime;
        this.controlOne = controlOne;
        this.controlTwo = controlTwo;
        this.controlThree = controlThree;
        this.awarenessOne = awarenessOne;
        this.awarenessTwo = awarenessTwo;
        this.awarenessThree = awarenessThree;
        this.collectionOne = collectionOne;
        this.collectionTwo = collectionTwo;
        this.collectionThree = collectionThree;
        this.errorOne = errorOne;
        this.errorTwo = errorTwo;
        this.errorThree = errorThree;
        this.errorFour = errorFour;
        this.secondaryUseOne = secondaryUseOne;
        this.secondaryUseTwo = secondaryUseTwo;
        this.secondaryUseThree = secondaryUseThree;
        this.secondaryUseFour = secondaryUseFour;
        this.secondaryUseFive = secondaryUseFive;
        this.improperOne = improperOne;
        this.improperTwo = improperTwo;
        this.improperThree = improperThree;
        this.globalOne = globalOne;
        this.globalTwo = globalTwo;
        this.globalThree = globalThree;
        this.globalFour = globalFour;
        this.globalFive = globalFive;
        this.familiar = familiar;
        this.dontKnowPermissions = dontKnowPermissions;
    }

    public String getAdId() {
        return adId;
    }

    public String getLoggedTime() {
        return loggedTime;
    }
}
