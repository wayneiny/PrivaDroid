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

import static com.weichengcao.privadroid.util.DatetimeUtil.convertIsoToReadableFormat;

public class ProfileFragment extends Fragment {

    private TextView mAdId;
    private TextView mJoinDate;
    private ImageView mAccessibilityIcon;
    private LinearLayout mAccessibilityLayout;
    private ImageView mUsageIcon;
    private LinearLayout mUsageLayout;
    private TextView mVersion;

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
        mJoinDate.setText(convertIsoToReadableFormat(userPreferences.getJoinDate()));

        /**
         * Set up version number
         */
        mVersion = view.findViewById(R.id.version_value);
        mVersion.setText(String.format("V %s", BuildConfig.VERSION_NAME));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * Set up accessibility and app usage icons and listeners
         */
        boolean isAccessibilityOn = AccessibilityAppUsageUtil.isAccessibilitySettingsOn();
        boolean isUsageOn = AccessibilityAppUsageUtil.isAppUsageSettingsOn();

        mAccessibilityIcon.setImageResource(isAccessibilityOn ? R.drawable.green_check : R.drawable.red_cross);
        mUsageIcon.setImageResource(isUsageOn ? R.drawable.green_check : R.drawable.red_cross);

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
    }

}
