package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.weichengcao.privadroid.R;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton mStartUsingAppButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mStartUsingAppButton = findViewById(R.id.start_using_app_button);
        mStartUsingAppButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mStartUsingAppButton) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
