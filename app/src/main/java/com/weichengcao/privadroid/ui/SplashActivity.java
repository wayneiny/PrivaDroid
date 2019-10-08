package com.weichengcao.privadroid.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.demographic.DemographicUtil;
import com.weichengcao.privadroid.demographic.SingleCountryUserStat;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.lang.ref.WeakReference;

import static com.weichengcao.privadroid.demographic.DemographicUtil.formatUserBaseStatFirebaseKey;
import static com.weichengcao.privadroid.util.EventUtil.ACTIVE_USER_COUNT;
import static com.weichengcao.privadroid.util.EventUtil.TARGET_USER_COUNT;
import static com.weichengcao.privadroid.util.EventUtil.TOTAL_USER_COUNT;

public class SplashActivity extends FragmentActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private UserPreferences userPreferences;
    private MaterialButton mContinueAppSettingButton;
    private CheckBox mAgreeToTermsCheckBox;

    private boolean userConfirmedCountryAndLanguage = false;
    private boolean userReachesLimitInThisCountry = false;

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

        MobileAds.initialize(this, getString(R.string.google_ad_id));

        /**
         * Get demographic summary to decide if we should let user join.
         */
        checkCountryEligibility();
        if (userReachesLimitInThisCountry) {
            return;
        }

        userPreferences = new UserPreferences(this);
        if (userPreferences.getAdvertisingId().isEmpty()) {
            new GetGoogleAdvertisingIdTask(this).execute();
        }

        if (!userPreferences.getFirestoreJoinEventId().isEmpty()) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
            finish();
        } else if (userPreferences.getConsentGranted()) {
            startTutorialActivity();
        }
    }

    private void checkCountryEligibility() {
        TelephonyManager manager = (TelephonyManager) PrivaDroidApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            String countryCode = manager.getNetworkCountryIso();
            final String countryName = DemographicUtil.countryCode2CountryNames.get(countryCode);
            if (countryName == null) {
                // do nothing, let user join
                return;
            }

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = firebaseFirestore.collection(EventUtil.RUNTIME_PARAMETERS_COLLECTION).document(formatUserBaseStatFirebaseKey(countryName));
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null) {
                            SingleCountryUserStat singleCountryUserStat = new SingleCountryUserStat(
                                    documentSnapshot.getString(ACTIVE_USER_COUNT),
                                    documentSnapshot.getString(TARGET_USER_COUNT),
                                    documentSnapshot.getString(TOTAL_USER_COUNT));
                            if (singleCountryUserStat.getActive() > singleCountryUserStat.getTarget()) {
                                userReachesLimitInThisCountry = true;
                                userPreferences.setConsentGranted(false);
                                return;
                            }
                        }
                    }

                    userReachesLimitInThisCountry = false;
                }
            });
        }
    }

    private void showConfirmLanguageAndCountryDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.confirm_language_and_country_restriction_awareness));
        alertDialogBuilder.setPositiveButton(getString(R.string.i_understand),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        userConfirmedCountryAndLanguage = true;

                        // write agree with terms in preferences
                        userPreferences.setConsentGranted(true);

                        startTutorialActivity();
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userConfirmedCountryAndLanguage = false;
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void startTutorialActivity() {
        Intent intent = new Intent(SplashActivity.this, TutorialActivity.class);
        startActivity(intent);
        finish();
    }

    public void showUserReachesLimitDialog() {
        Toast.makeText(this, R.string.user_in_one_country_reaches_limit_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        if (view == mContinueAppSettingButton) {
            if (userReachesLimitInThisCountry) {
                showUserReachesLimitDialog();
                return;
            }

            if (!userConfirmedCountryAndLanguage) {
                /**
                 * Create alert dialog to let user know that we only support certain countries and languages.
                 */
                showConfirmLanguageAndCountryDialog();
                return;
            }

            startTutorialActivity();
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
//                Toast.makeText(PrivaDroidApplication.getAppContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            return adInfo != null ? adInfo.getId() : "";
        }

        @Override
        protected void onPostExecute(String s) {
            activityWeakReference.get().userPreferences.setAdvertisingId(s);
        }
    }
}
