package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.weichengcao.privadroid.R;

import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.EVENT_TYPE;

public class SurveyedEventsActivity extends FragmentActivity {

    private int mCurrentEventType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveyed_events);

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentEventType = intent.getIntExtra(EVENT_TYPE, APP_INSTALL_EVENT_TYPE);
        } else {
            Toast.makeText(this, "No event type. Exiting.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
}
