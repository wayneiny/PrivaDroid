package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.material.button.MaterialButton;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

import java.lang.ref.WeakReference;

import com.facebook.FacebookSdk;

public class SplashActivity extends FragmentActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private UserPreferences userPreferences;
    private MaterialButton mContinueAppSettingButton;
    private CheckBox mAgreeToTermsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContinueAppSettingButton = findViewById(R.id.continue_app_setting_button);
        mContinueAppSettingButton.setEnabled(false);
        mContinueAppSettingButton.setOnClickListener(this);

        mAgreeToTermsCheckBox = findViewById(R.id.agree_to_terms_checkbox);
        mAgreeToTermsCheckBox.setChecked(false);
        mAgreeToTermsCheckBox.setOnCheckedChangeListener(this);

        MobileAds.initialize(this, "ca-app-pub-1063585474940344~2699806720");

        userPreferences = new UserPreferences(this);
        if (userPreferences.getAdvertisingId().isEmpty()) {
            new GetGoogleAdvertisingIdTask(this).execute();
//        } else {
//            Log.d(TAG, "Read Google Advertising Id from UserPreferences to be " + userPreferences.getAdvertisingId());
        }

        if (!userPreferences.getFirestoreJoinEventId().isEmpty()) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mContinueAppSettingButton) {
            // write agree with terms in preferences
            userPreferences.setConsentGranted(true);

            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == mAgreeToTermsCheckBox) {
            mContinueAppSettingButton.setEnabled(b);
        }
    }

    private static class GetGoogleAdvertisingIdTask extends AsyncTask<String, Integer, String> {

        private WeakReference<SplashActivity> activityWeakReference;

        GetGoogleAdvertisingIdTask(SplashActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            AdvertisingIdClient.Info adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(PrivaDroidApplication.getAppContext());
            } catch (Exception e) {
//                Log.e(TAG, "Unable to read Google Advertising Id: " + e.getLocalizedMessage());
                Toast.makeText(PrivaDroidApplication.getAppContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            return adInfo != null ? adInfo.getId() : "";
        }

        @Override
        protected void onPostExecute(String s) {
            activityWeakReference.get().userPreferences.setAdvertisingId(s);
//            Log.d(TAG, "Updated Google Advertising Id to be " + activityWeakReference.get().userPreferences.getAdvertisingId());
        }
    }
}
