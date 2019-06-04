package com.weichengcao.privadroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Mobile Ad [START]
        MobileAds.initialize(this, "ca-app-pub-1063585474940344~2699806720");
        // Firebase Mobile Ad [END]
    }
}
