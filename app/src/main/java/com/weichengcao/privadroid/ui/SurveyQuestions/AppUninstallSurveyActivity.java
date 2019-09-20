package com.weichengcao.privadroid.ui.SurveyQuestions;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.weichengcao.privadroid.R;

public class AppUninstallSurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_uninstall_survey);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}