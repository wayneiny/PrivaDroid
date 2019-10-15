package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.weichengcao.privadroid.BuildConfig;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.AccessibilityAppUsageUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import org.joda.time.DateTime;

import static com.weichengcao.privadroid.util.DatetimeUtil.convertIsoToReadableFormat;
import static com.weichengcao.privadroid.util.UserPreferences.UNKNOWN_DATE;

public class ProfileFragment extends Fragment {

    public static final int REWARDS_DAYS = 30;

    private TextView mAdId;
    private TextView mJoinDate;
    private ImageView mAccessibilityIcon;
    private LinearLayout mAccessibilityLayout;
    private ImageView mUsageIcon;
    private LinearLayout mUsageLayout;
    private LinearLayout mDemographicLayout;
    private ImageView mDemographicStatusIcon;
    private LinearLayout mRewardsLayout;
    private ImageView mRewardsStatusIcon;
    private TextView mVersion;
    private LinearLayout mExitSurveyLayout;
    private ImageView mExitSurveyStatusIcon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAdId = view.findViewById(R.id.user_ad_id_value);
        mJoinDate = view.findViewById(R.id.join_date_value);
        mAccessibilityIcon = view.findViewById(R.id.accessibility_status_icon);
        mAccessibilityLayout = view.findViewById(R.id.accessibility_status_container);
        mUsageIcon = view.findViewById(R.id.app_usage_status_icon);
        mUsageLayout = view.findViewById(R.id.app_usage_status_container);

        /**
         * Set up ad id
         */
        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        mAdId.setText(userPreferences.getAdvertisingId());
        mJoinDate.setText(userPreferences.getJoinDate().equals(UNKNOWN_DATE) ? UNKNOWN_DATE : convertIsoToReadableFormat(userPreferences.getJoinDate()));

        /**
         * Set up version number
         */
        mVersion = view.findViewById(R.id.version_value);
        mVersion.setText(String.format("V %s", BuildConfig.VERSION_NAME));

        /**
         * Set up demographic status
         */
        mDemographicLayout = view.findViewById(R.id.demographic_status_container);
        mDemographicStatusIcon = view.findViewById(R.id.demographic_status_icon);

        /**
         * Set up rewards status
         */
        mRewardsLayout = view.findViewById(R.id.rewards_status_container);
        if (userPreferences.getUserNotFromTargetCountry() || userPreferences.getUserLimitReached()) {
            view.findViewById(R.id.reward_status_container_above_divider).setVisibility(View.GONE);
            mRewardsLayout.setVisibility(View.GONE);
        } else {
            mRewardsStatusIcon = view.findViewById(R.id.rewards_status_icon);
            mRewardsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PrivaDroidApplication.getAppContext(), RewardsActivity.class);
                    startActivity(intent);
                }
            });
        }

        /**
         * Set up exit survey status
         */
        mExitSurveyLayout = view.findViewById(R.id.exit_survey_status_container);
        mExitSurveyStatusIcon = view.findViewById(R.id.exit_survey_status_icon);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());

        /**
         * Set up accessibility and app usage icons and listeners
         */
        boolean isAccessibilityOn = AccessibilityAppUsageUtil.isAccessibilitySettingsOn();
        boolean isUsageOn = AccessibilityAppUsageUtil.isAppUsageSettingsOn();

        mAccessibilityIcon.setImageResource(isAccessibilityOn ?
                R.drawable.ic_check_circle_accent_24dp :
                R.drawable.ic_cancel_accent_24dp);
        mUsageIcon.setImageResource(isUsageOn ?
                R.drawable.ic_check_circle_accent_24dp :
                R.drawable.ic_cancel_accent_24dp);

        mAccessibilityLayout.setOnClickListener(isAccessibilityOn ? null : new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accessibilityActivityIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibilityActivityIntent);
            }
        });

        mUsageLayout.setOnClickListener(isUsageOn ? null : new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });

        mDemographicStatusIcon.setImageResource(userPreferences.getAnsweredDemographicSurvey() ?
                R.drawable.ic_check_circle_accent_24dp :
                R.drawable.ic_cancel_accent_24dp);
        mDemographicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivaDroidApplication.getAppContext(), DemographicActivity.class);
                startActivity(intent);
            }
        });

        mExitSurveyStatusIcon.setImageResource(userPreferences.getAnsweredExitSurvey() ?
                R.drawable.ic_check_circle_accent_24dp :
                R.drawable.ic_cancel_accent_24dp);
        mExitSurveyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivaDroidApplication.getAppContext(), ExitSurveyActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Set up rewards status icon: if user is not eligible or has not stayed for a month, don't update icon.
         */
        if (!userPreferences.getUserLimitReached() && !userPreferences.getUserNotFromTargetCountry()) {
            String joinDateText = userPreferences.getJoinDate();
            if (!joinDateText.equals(UNKNOWN_DATE)) {
                DateTime joinDate = DateTime.parse(joinDateText);
                DateTime now = DateTime.now();

                mRewardsStatusIcon.setImageResource(now.minusDays(REWARDS_DAYS).isAfter(joinDate) ?
                        R.drawable.ic_check_circle_accent_24dp :
                        R.drawable.ic_cancel_accent_24dp);
            } else {
                mRewardsStatusIcon.setImageResource(R.drawable.ic_cancel_accent_24dp);
            }
        }
    }

}
