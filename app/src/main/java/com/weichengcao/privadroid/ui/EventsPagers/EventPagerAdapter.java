package com.weichengcao.privadroid.ui.EventsPagers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import java.util.ArrayList;

public class EventPagerAdapter extends FragmentPagerAdapter {

    public static final int APP_INSTALL_EVENT_TYPE = 0;
    public static final int APP_UNINSTALL_EVENT_TYPE = 1;
    public static final int PERMISSION_EVENT_TYPE = 2;

    private String[] TAB_TITLES = new String[]{
            PrivaDroidApplication.getAppContext().getString(R.string.surveyed),
            PrivaDroidApplication.getAppContext().getString(R.string.unsurveyed),
            PrivaDroidApplication.getAppContext().getString(R.string.summary)
    };

    private int mEventType;

    private ArrayList<Fragment> mFragments;

    private static final String EVENT_TYPE_BUNDLE_ARG = "EVENT_TYPE_BUNDLE_ARG";

    public EventPagerAdapter(@NonNull FragmentManager fm, int behavior, int eventType) {
        super(fm, behavior);

        mEventType = eventType;
        mFragments = new ArrayList<>();
    }

    public void add(Fragment fragment) {
        this.mFragments.add(fragment);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        fragment.setArguments(createEventTypeBundleArg());
        return fragment;
    }

    private Bundle createEventTypeBundleArg() {
        Bundle bundle = new Bundle();
        bundle.putInt(EVENT_TYPE_BUNDLE_ARG, mEventType);
        return bundle;
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }
}
