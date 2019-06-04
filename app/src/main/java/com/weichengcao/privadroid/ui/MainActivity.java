package com.weichengcao.privadroid.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-1063585474940344~2699806720");

        userPreferences = new UserPreferences(this);
        if (userPreferences.getAdvertisingId().isEmpty()) {
            new GetGAIDTask().execute();
        } else {
            Log.d(TAG, "Read GAID from UserPreferences to be " + userPreferences.getAdvertisingId());
        }
    }

    private class GetGAIDTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            AdvertisingIdClient.Info adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this.getApplicationContext());
            } catch (Exception e) {
                Log.e(TAG, "Unable to read Google Advertising Id: " + e.getLocalizedMessage());
                Toast.makeText(MainActivity.this.getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            return adInfo != null ? adInfo.getId() : "";
        }

        @Override
        protected void onPostExecute(String s) {
            userPreferences.setAdvertisingId(s);
            Log.d(TAG, "Updated GAID to be " + userPreferences.getAdvertisingId());
        }
    }
}
