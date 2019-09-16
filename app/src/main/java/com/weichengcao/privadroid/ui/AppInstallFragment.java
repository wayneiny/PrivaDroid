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
import com.weichengcao.privadroid.ui.EventsPagers.SummaryFragment;
import com.weichengcao.privadroid.ui.EventsPagers.SurveyedFragment;
import com.weichengcao.privadroid.ui.EventsPagers.UnsurveyedFragment;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.weichengcao.privadroid.ui.EventsPagers.EventPagerAdapter.APP_INSTALL_EVENT_TYPE;

public class AppInstallFragment extends Fragment {

    TabLayout mTabLayout;
    ViewPager mViewPager;
    EventPagerAdapter mEventPagerAdapter;

    public AppInstallFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_install, container, false);

        mTabLayout = view.findViewById(R.id.app_install_tabs);
        mViewPager = view.findViewById(R.id.app_install_view_pager);

        mEventPagerAdapter = new EventPagerAdapter(getChildFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, APP_INSTALL_EVENT_TYPE);
        mEventPagerAdapter.add(new SurveyedFragment());
        mEventPagerAdapter.add(new UnsurveyedFragment());
        mEventPagerAdapter.add(new SummaryFragment());

        mViewPager.setAdapter(mEventPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }
}
