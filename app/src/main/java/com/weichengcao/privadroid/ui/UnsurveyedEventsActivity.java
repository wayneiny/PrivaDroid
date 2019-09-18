package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.weichengcao.privadroid.R;

import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.EVENT_TYPE;

public class UnsurveyedEventsActivity extends AppCompatActivity {

    private int mCurrentEventType;
    private ImageView mBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsurveyed_events);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentEventType = intent.getIntExtra(EVENT_TYPE, APP_INSTALL_EVENT_TYPE);
        } else {
            Toast.makeText(this, "No event type. Exiting.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mBackButton = findViewById(R.id.activity_unsurveyed_events_back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
