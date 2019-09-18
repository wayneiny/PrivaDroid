package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.weichengcao.privadroid.R;

import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.createEventTypeFragmentBundle;

public class AppInstallFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_install, container, false);

        initializeSurveyedUnsurveyedFragments();

        /**
         * TODO: Query all app install events.
         */

        return view;
    }

    private void initializeSurveyedUnsurveyedFragments() {
        SurveyedCardFragment surveyedCardFragment = new SurveyedCardFragment();
        UnsurveyedCardFragment unsurveyedCardFragment = new UnsurveyedCardFragment();

        surveyedCardFragment.setArguments(createEventTypeFragmentBundle(APP_INSTALL_EVENT_TYPE));
        unsurveyedCardFragment.setArguments(createEventTypeFragmentBundle(APP_INSTALL_EVENT_TYPE));

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.app_install_surveyed_fragment, surveyedCardFragment);
        ft.replace(R.id.app_install_unsurveyed_fragment, unsurveyedCardFragment);
        ft.commit();
    }
}
