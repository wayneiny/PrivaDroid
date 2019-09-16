package com.weichengcao.privadroid.ui.EventsPagers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class EventPagerAdapter extends FragmentPagerAdapter {

    public static final int TOTAL_TABS = 3;
    public static final int SURVEYED_TAB_INDEX = 0;
    public static final int UNSURVEYED_TAB_INDEX = 1;
    public static final int SUMMARY_TAB_INDEX = 2;

    public static final int APP_INSTALL_EVENT_TYPE = 0;
    public static final int APP_UNINSTALL_EVENT_TYPE = 1;
    public static final int PERMISSION_EVENT_TYPE = 2;

    private int mTotalTabs;
    private int mEventType;

    private static final String EVENT_TYPE_BUNDLE_ARG = "EVENT_TYPE_BUNDLE_ARG";

    public EventPagerAdapter(@NonNull FragmentManager fm, int behavior, int totalTabs, int eventType) {
        super(fm, behavior);

        mTotalTabs = totalTabs;
        mEventType = eventType;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle args = createEventTypeBundleArg(mEventType);
        Fragment result;
        switch (position) {
            case UNSURVEYED_TAB_INDEX:
                result = new UnsurveyedFragment();
                break;
            case SUMMARY_TAB_INDEX:
                result = new SummaryFragment();
                break;
            case SURVEYED_TAB_INDEX:
            default:
                result = new SurveyedFragment();
        }

        result.setArguments(args);

        return result;
    }

    private Bundle createEventTypeBundleArg(int eventType) {
        Bundle bundle = new Bundle();
        bundle.putInt(EVENT_TYPE_BUNDLE_ARG, eventType);
        return bundle;
    }

    @Override
    public int getCount() {
        return mTotalTabs;
    }
}
