package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.ui.EventsPagers.EventPagerAdapter;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.weichengcao.privadroid.ui.EventsPagers.EventPagerAdapter.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.EventsPagers.EventPagerAdapter.TOTAL_TABS;

public class AppInstallFragment extends Fragment {

    TabLayout mTabLayout;
    ViewPager mViewPager;
    EventPagerAdapter mEventPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_install, container, false);

        mTabLayout = view.findViewById(R.id.app_install_tabs);
        mViewPager = view.findViewById(R.id.app_install_view_pager);

        mEventPagerAdapter = new EventPagerAdapter(getFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, TOTAL_TABS, APP_INSTALL_EVENT_TYPE);
        mViewPager.setAdapter(mEventPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }
}
