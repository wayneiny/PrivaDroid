package com.weichengcao.privadroid.database;

public class DemographicEvent {

    private String adId;
    private String country;
    private String education;
    private String income;
    private String age;
    private String gender;
    private String status;
    private String industry;
    private String loggedTime;
    private String dailyUsage;

    public DemographicEvent(String adId, String country, String education, String income, String age,
                            String gender, String status, String industry, String loggedTime, String dailyUsage) {
        this.adId = adId;
        this.country = country;
        this.education = education;
        this.income = income;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.industry = industry;
        this.loggedTime = loggedTime;
        this.dailyUsage = dailyUsage;
    }

    public String getDailyUsage() {
        return dailyUsage;
    }

    public String getAdId() {
        return adId;
    }

    public String getAge() {
        return age;
    }

    public String getCountry() {
        return country;
    }

    public String getEducation() {
        return education;
    }

    public String getGender() {
        return gender;
    }

    public String getIncome() {
        return income;
    }

    public String getIndustry() {
        return industry;
    }

    public String getLoggedTime() {
        return loggedTime;
    }

    public String getStatus() {
        return status;
    }
}
