package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.ApplicationInfoPreferences;

public class MainScreenActivity extends FragmentActivity {

    private BottomNavigationView mBottomNavigationView;

    /**
     * Common to App Install/App Uninstall/Permission
     */
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final int APP_INSTALL_EVENT_TYPE = 0;
    public static final int APP_UNINSTALL_EVENT_TYPE = 1;
    public static final int PERMISSION_EVENT_TYPE = 2;

    public static Bundle createEventTypeFragmentBundle(int eventType) {
        Bundle res = new Bundle();
        res.putInt(EVENT_TYPE, eventType);
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectFragment(menuItem);
                return false;
            }
        });

        /**
         * Show the app install screen by default.
         */
        pushFragment(new AppInstallFragment());
        PrivaDroidApplication.setCurrentyHandledEventType(APP_INSTALL_EVENT_TYPE);

        /**
         * Cache app info if not already.
         */
        ApplicationInfoPreferences applicationInfoPreferences = new ApplicationInfoPreferences(this);
        if (!applicationInfoPreferences.getCachedAppInfo()) {
            applicationInfoPreferences.cacheAppInfo();
            applicationInfoPreferences.setCachedAppInfo(true);
        }
    }

    private void selectFragment(MenuItem menuItem) {
        menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.app_install:
                pushFragment(new AppInstallFragment());
                PrivaDroidApplication.setCurrentyHandledEventType(APP_INSTALL_EVENT_TYPE);
                break;
            case R.id.app_uninstall:
                pushFragment(new AppUninstallFragment());
                PrivaDroidApplication.setCurrentyHandledEventType(APP_UNINSTALL_EVENT_TYPE);
                break;
            case R.id.permission:
                pushFragment(new PermissionFragment());
                PrivaDroidApplication.setCurrentyHandledEventType(PERMISSION_EVENT_TYPE);
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
}
