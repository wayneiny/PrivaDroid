package com.weichengcao.privadroid.ui.TutorialCards;

import android.view.View;

public class CardItem {

    private int titleStringResourceId;
    private int descriptionStringResourceId;
    private int buttonStringResourceId;
    private boolean setUpComplete;
    private View.OnClickListener buttonListener;

    public CardItem(int title, int description, int button, View.OnClickListener buttonListener) {
        this.titleStringResourceId = title;
        this.descriptionStringResourceId = description;
        this.buttonStringResourceId = button;
        this.buttonListener = buttonListener;
        this.setUpComplete = false;
    }

    public int getDescriptionStringResourceId() {
        return descriptionStringResourceId;
    }

    public int getTitleStringResourceId() {
        return titleStringResourceId;
    }

    public int getButtonStringResourceId() {
        return buttonStringResourceId;
    }

    public boolean isSetUpComplete() {
        return setUpComplete;
    }

    public void setButtonToComplete() {
        setUpComplete = true;
    }

    public View.OnClickListener getButtonListener() {
        return buttonListener;
    }
}
