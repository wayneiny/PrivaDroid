package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.ui.TutorialCards.CardItem;
import com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter;
import com.weichengcao.privadroid.ui.TutorialCards.ShadowTransformer;
import com.weichengcao.privadroid.util.AccessibilityAppUsageUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import static com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter.ACCESSIBILITY_INDEX;
import static com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter.APP_USAGE_INDEX;
import static com.weichengcao.privadroid.ui.TutorialCards.CardPagerAdapter.HOW_TO_CARD_INDEX;
import static com.weichengcao.privadroid.util.EventConstants.JOIN_EVENT_COLLECTION;
import static com.weichengcao.privadroid.util.ExperimentEventFactory.createJoinEvent;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton mStartUsingAppButton;
    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter;

    private UserPreferences mUserPreferences;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mStartUsingAppButton = findViewById(R.id.start_using_app_button);
        mStartUsingAppButton.setOnClickListener(this);

        mViewPager = findViewById(R.id.tutorial_pager);

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
    public void onClick(View view) {
        if (view == mStartUsingAppButton) {
            if (!AccessibilityAppUsageUtil.isAccessibilitySettingsOn() || !AccessibilityAppUsageUtil.isAppUsageSettingsOn()) {
                Toast.makeText(this, "Enable Accessibility and App Usage services to use the app.", Toast.LENGTH_LONG).show();
                return;
            } else {
                Toast.makeText(this, "Congratulations!", Toast.LENGTH_SHORT).show();
            }

            // 1. Log a join event and send to FireStore
            mFirestore.collection(JOIN_EVENT_COLLECTION).add(createJoinEvent())
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                DocumentReference documentReference = task.getResult();
                                if (documentReference == null) {
                                    return;
                                }
                                mUserPreferences.setFirestoreJoinEventId(documentReference.getId());

                                // 2.1. Navigate to main screen
                                Intent intent = new Intent(PrivaDroidApplication.getAppContext(), MainScreenActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // 2.2 Display failed message
                                Toast.makeText(PrivaDroidApplication.getAppContext(), "Failed to join. Please Try again.", Toast.LENGTH_LONG).show();
                            }
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
