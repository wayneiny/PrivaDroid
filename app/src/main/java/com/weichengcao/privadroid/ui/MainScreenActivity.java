package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.weichengcao.privadroid.R;

public class MainScreenActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectFragment(menuItem);
                return false;
            }
        });

        pushFragment(new AppInstallFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();

        removePaddingsOfBottomNavigationBar();
    }

    private void selectFragment(MenuItem menuItem) {
        menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.app_install:
                pushFragment(new AppInstallFragment());
                break;
            case R.id.app_uninstall:
                pushFragment(new AppUninstallFragment());
                break;
            case R.id.permission:
                pushFragment(new PermissionFragment());
                break;
            case R.id.global:
                pushFragment(new CommunityFragment());
                break;
            case R.id.profile:
                pushFragment(new ProfileFragment());
                break;
        }
    }

    /**
     * Method to push any fragment into given id.
     *
     * @param fragment An instance of Fragment to show into the given id.
     */
    protected void pushFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.main_screen_content_frame, fragment);
        ft.commit();
    }

    private void removePaddingsOfBottomNavigationBar() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
            View activeLabel = item.findViewById(R.id.largeLabel);
            if (activeLabel instanceof TextView) {
                activeLabel.setPadding(0, 0, 0, 0);
            }
        }
    }
}
