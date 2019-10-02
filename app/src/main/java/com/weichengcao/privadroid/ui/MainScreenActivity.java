package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.OnDeviceStorageProvider;
import com.weichengcao.privadroid.util.ApplicationInfoPreferences;

import static com.weichengcao.privadroid.database.FirestoreProvider.isNetworkAvailable;
import static com.weichengcao.privadroid.sensors.SystemBroadcastForegroundService.startSystemBroadcastForegroundService;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_EVENT_TYPE;

public class MainScreenActivity extends FragmentActivity {

    private BottomNavigationView mBottomNavigationView;

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
        PrivaDroidApplication.setCurrentlyHandledEventType(APP_INSTALL_EVENT_TYPE);

        /**
         * Cache app info if not already.
         */
        ApplicationInfoPreferences applicationInfoPreferences = new ApplicationInfoPreferences(this);
        if (!applicationInfoPreferences.getCachedAppInfo()) {
            applicationInfoPreferences.cacheAppInfo();
            applicationInfoPreferences.setCachedAppInfo(true);
        }

        /**
         * Start SystemChangeEventReceiver if >= Oreo.
         */
        startSystemBroadcastForegroundService();

        /**
         * Sync on-device events to Firebase.
         */
        OnDeviceStorageProvider.syncAllOnDeviceEventsToFirebase();

        if (!isNetworkAvailable()) {
            Toast.makeText(PrivaDroidApplication.getAppContext(), PrivaDroidApplication.getAppContext().getString(R.string.no_internet_connection_error), Toast.LENGTH_LONG).show();
        }
    }

    private void selectFragment(MenuItem menuItem) {
        menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.app_install:
                pushFragment(new AppInstallFragment());
                PrivaDroidApplication.setCurrentlyHandledEventType(APP_INSTALL_EVENT_TYPE);
                break;
            case R.id.app_uninstall:
                pushFragment(new AppUninstallFragment());
                PrivaDroidApplication.setCurrentlyHandledEventType(APP_UNINSTALL_EVENT_TYPE);
                break;
            case R.id.permission:
                pushFragment(new PermissionFragment());
                PrivaDroidApplication.setCurrentlyHandledEventType(PERMISSION_EVENT_TYPE);
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
