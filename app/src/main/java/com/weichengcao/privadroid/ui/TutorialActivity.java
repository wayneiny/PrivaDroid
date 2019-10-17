package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.notifications.DemographicReminderService;
import com.weichengcao.privadroid.notifications.HeartbeatAndServiceReminderService;
import com.weichengcao.privadroid.ui.TutorialCards.CardItem;
import com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter;
import com.weichengcao.privadroid.ui.TutorialCards.ShadowTransformer;
import com.weichengcao.privadroid.util.AccessibilityAppUsageUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.database.FirestoreProvider.isNetworkAvailable;
import static com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter.ACCESSIBILITY_INDEX;
import static com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter.APP_USAGE_INDEX;
import static com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter.HOW_TO_CARD_INDEX;
import static com.weichengcao.privadroid.util.EventUtil.JOIN_EVENT_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.LOGGED_TIME;
import static com.weichengcao.privadroid.database.ExperimentEventFactory.createJoinEvent;

public class TutorialActivity extends FragmentActivity implements View.OnClickListener {

    private MaterialButton mStartUsingAppButton;
    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter;
    private ProgressBar mJoinEventProgressBar;

    private UserPreferences mUserPreferences;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mStartUsingAppButton = findViewById(R.id.start_using_app_button);
        mStartUsingAppButton.setOnClickListener(this);

        mViewPager = findViewById(R.id.tutorial_pager);
        mJoinEventProgressBar = findViewById(R.id.join_event_progress_bar);

        mCardAdapter = new CardPagerAdapter();
        mCardAdapter.addCardItem(mHowToCard);
        mCardAdapter.addCardItem(mAccessibilityCard);
        mCardAdapter.addCardItem(mUsageCard);

        ShadowTransformer mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
        mCardShadowTransformer.enableScaling(true);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);

        mUserPreferences = new UserPreferences(this);

        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateAccessibilityAppUsageButtons();
    }

    public void updateAccessibilityAppUsageButtons() {
        /**
         * Update app usage button
         */
        if (AccessibilityAppUsageUtil.isAppUsageSettingsOn() && !mUsageCard.isSetUpComplete()) {
            mUsageCard.setButtonToComplete(true);
            mCardAdapter.setCardItemAt(APP_USAGE_INDEX, mUsageCard);
        } else if (!AccessibilityAppUsageUtil.isAppUsageSettingsOn()) {
            mUsageCard.setButtonToComplete(false);
        }

        /**
         * Update accessibility button
         */
        if (AccessibilityAppUsageUtil.isAccessibilitySettingsOn() && !mAccessibilityCard.isSetUpComplete()) {
            mAccessibilityCard.setButtonToComplete(true);
            mCardAdapter.setCardItemAt(ACCESSIBILITY_INDEX, mAccessibilityCard);
        } else if (!AccessibilityAppUsageUtil.isAccessibilitySettingsOn()) {
            mAccessibilityCard.setButtonToComplete(false);
        }

        /**
         * Update how to button
         */
        if (AccessibilityAppUsageUtil.readHowTo && !mHowToCard.isSetUpComplete()) {
            mHowToCard.setButtonToComplete(true);
            mCardAdapter.setCardItemAt(HOW_TO_CARD_INDEX, mHowToCard);
        }

        mViewPager.setAdapter(mCardAdapter);
    }

    @Override
    public void onClick(final View view) {
        if (view == mStartUsingAppButton) {
            view.setEnabled(false);
            if (!AccessibilityAppUsageUtil.isAccessibilitySettingsOn() || !AccessibilityAppUsageUtil.isAppUsageSettingsOn()) {
                Toast.makeText(this, R.string.finish_setting_up_accessibility_and_app_usage, Toast.LENGTH_LONG).show();
                view.setEnabled(true);
                return;
            }

            if (!isNetworkAvailable()) {
                Toast.makeText(this, this.getString(R.string.no_internet_connection_error), Toast.LENGTH_LONG).show();
                view.setEnabled(true);
                return;
            }

            // 1. Log a join event and send to FireStore
            final HashMap<String, String> joinEvent = createJoinEvent();
            mJoinEventProgressBar.setVisibility(View.VISIBLE);
            mFirestore.collection(JOIN_EVENT_COLLECTION).add(joinEvent)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                DocumentReference documentReference = task.getResult();
                                if (documentReference == null) {
                                    return;
                                }
                                mUserPreferences.setJoinDate(joinEvent.get(LOGGED_TIME));

                                // 2.1. Navigate to main screen
                                Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.congratulations, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PrivaDroidApplication.getAppContext(), MainScreenActivity.class);
                                startActivity(intent);
                                finish();

                                // 2.2. Schedule demographic survey reminder and heartbeat
                                DemographicReminderService.scheduleDemographicSurveyReminder();
                                HeartbeatAndServiceReminderService.scheduleHeartbeatAndServiceReminderJob();
                            } else {
                                // 2.3 Display failed message
                                Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.failed_to_join_make_sure_network, Toast.LENGTH_LONG).show();
                                view.setEnabled(true);
                            }

                            mJoinEventProgressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private CardItem mHowToCard = new CardItem(R.string.tutorial_how_title, R.string.tutorial_how_description, R.string.ok, new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AccessibilityAppUsageUtil.readHowTo = true;
            updateAccessibilityAppUsageButtons();
        }
    });

    private CardItem mAccessibilityCard = new CardItem(R.string.tutorial_accessibility_service_title, R.string.tutorial_accessibility_service_description, R.string.set_up, new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /**
             * 1. check if accessibility is enabled
             * 2. if not, start accessibility setting
             */
            if (AccessibilityAppUsageUtil.isAccessibilitySettingsOn()) {
                return;
            }

            Intent accessibilityActivityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(accessibilityActivityIntent);
        }
    });

    private CardItem mUsageCard = new CardItem(R.string.tutorial_app_usage_title, R.string.tutorial_app_usage_description, R.string.set_up, new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /**
             * 1. check if app usage is enabled
             * 2. if not, start usage setting
             */
            if (AccessibilityAppUsageUtil.isAppUsageSettingsOn()) {
                return;
            }

            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    });
}
