package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.material.button.MaterialButton;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

import java.lang.ref.WeakReference;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private UserPreferences userPreferences;
    private MaterialButton mStartAppButton;
    private CheckBox mAgreeToTermsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mStartAppButton = findViewById(R.id.start_using_app_button);
        mStartAppButton.setEnabled(false);
        mStartAppButton.setOnClickListener(this);

        mAgreeToTermsCheckBox = findViewById(R.id.agree_to_terms_checkbox);
        mAgreeToTermsCheckBox.setChecked(false);
        mAgreeToTermsCheckBox.setOnCheckedChangeListener(this);

        MobileAds.initialize(this, "ca-app-pub-1063585474940344~2699806720");

        userPreferences = new UserPreferences(this);
        if (userPreferences.getAdvertisingId().isEmpty()) {
            new GetGoogleAdvertisingIdTask(this).execute();
        } else {
            Log.d(TAG, "Read Google Advertising Id from UserPreferences to be " + userPreferences.getAdvertisingId());
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mStartAppButton) {
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
            mStartAppButton.setEnabled(b);
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
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(activityWeakReference.get().getApplicationContext());
            } catch (Exception e) {
                Log.e(TAG, "Unable to read Google Advertising Id: " + e.getLocalizedMessage());
                Toast.makeText(activityWeakReference.get().getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            return adInfo != null ? adInfo.getId() : "";
        }

        @Override
        protected void onPostExecute(String s) {
            activityWeakReference.get().userPreferences.setAdvertisingId(s);
            Toast.makeText(activityWeakReference.get().getApplicationContext(), activityWeakReference.get().userPreferences.getAdvertisingId(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Updated Google Advertising Id to be " + activityWeakReference.get().userPreferences.getAdvertisingId());
        }
    }
}
