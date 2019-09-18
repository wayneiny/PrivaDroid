package com.weichengcao.privadroid.ui.Events;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.BaseServerEvent;
import com.weichengcao.privadroid.util.EventUtil;

import java.util.ArrayList;
import java.util.HashMap;

import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.UNSURVEYED_EVENT;
import static com.weichengcao.privadroid.util.EventUtil.getProperDataHashMap;

public class UnsurveyedEventsActivity extends AppCompatActivity {

    private int mCurrentEventType;
    private EventListAdapter mAdapter;

    private ImageView mBackButton;
    private RecyclerView mEventsView;

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

        mEventsView = findViewById(R.id.activity_unsurveyed_events_recycler_view);
        mEventsView.setHasFixedSize(true);
        mEventsView.setLayoutManager(new GridLayoutManager(this, 1));

        HashMap<String, BaseServerEvent> properEventsMap = getProperDataHashMap(mCurrentEventType, UNSURVEYED_EVENT);
        if (properEventsMap != null) {
            ArrayList<BaseServerEvent> sortedEvents = EventUtil.sortEventsBasedOnTime(properEventsMap, false);
            mAdapter = new EventListAdapter(this, sortedEvents, mCurrentEventType);
            mEventsView.setAdapter(mAdapter);
        }
    }
}
