package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.AppInstallServerEvent;
import com.weichengcao.privadroid.util.EventConstants;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.ArrayList;

import static com.weichengcao.privadroid.PrivaDroidApplication.appInstallServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.appInstallServerUnsurveyedEvents;
import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.createEventTypeFragmentBundle;
import static com.weichengcao.privadroid.util.EventConstants.APP_INSTALL_COLLECTION;

public class AppInstallFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_install, container, false);

        initializeSurveyedUnsurveyedFragments();

        return view;
    }

    private void initializeSurveyedUnsurveyedFragments() {
        final SurveyedCardFragment surveyedCardFragment = new SurveyedCardFragment();
        final UnsurveyedCardFragment unsurveyedCardFragment = new UnsurveyedCardFragment();

        /**
         * Pass event type to fragments.
         */
        surveyedCardFragment.setArguments(createEventTypeFragmentBundle(APP_INSTALL_EVENT_TYPE));
        unsurveyedCardFragment.setArguments(createEventTypeFragmentBundle(APP_INSTALL_EVENT_TYPE));

        /**
         * Query surveyed and unsurveyed app install events.
         */
        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(APP_INSTALL_COLLECTION);
        Query query = collectionReference.whereEqualTo(EventConstants.USER_AD_ID, userPreferences.getAdvertisingId());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    appInstallServerSurveyedEvents = new ArrayList<>();
                    appInstallServerUnsurveyedEvents = new ArrayList<>();

                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String adId = document.getString(EventConstants.USER_AD_ID);
                            String appName = document.getString(EventConstants.APP_NAME);
                            String appVersion = document.getString(EventConstants.APP_VERSION);
                            String loggedTime = document.getString(EventConstants.LOGGED_TIME);
                            String packageName = document.getString(EventConstants.PACKAGE_NAME);
                            String surveyed = document.getString(EventConstants.SURVEYED);
                            AppInstallServerEvent event = new AppInstallServerEvent(adId, appName, appVersion, loggedTime, packageName, surveyed);

                            if (event.isEventSurveyed()) {
                                appInstallServerSurveyedEvents.add(event);
                            } else {
                                appInstallServerUnsurveyedEvents.add(event);
                            }
                        }

                        if (surveyedCardFragment.getView() != null) {
                            int surveyedSize = appInstallServerSurveyedEvents.size();
                            ((TextView) surveyedCardFragment.getView()
                                    .findViewById(R.id.surveyed_card_description))
                                    .setText(getResources().getQuantityString(
                                            R.plurals.number_of_app_install_surveyed_events_card_description,
                                            surveyedSize,
                                            surveyedSize));
                        }
                        if (unsurveyedCardFragment.getView() != null) {
                            int unsurveyedSize = appInstallServerUnsurveyedEvents.size();
                            ((TextView) unsurveyedCardFragment.getView()
                                    .findViewById(R.id.unsurveyed_card_description))
                                    .setText(getResources().getQuantityString(
                                            R.plurals.number_of_app_install_unsurveyed_events_card_description,
                                            unsurveyedSize,
                                            unsurveyedSize));
                        }
                    }
                }
            }
        });

        /**
         * Perform transition.
         */
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.app_install_surveyed_fragment, surveyedCardFragment);
        ft.replace(R.id.app_install_unsurveyed_fragment, unsurveyedCardFragment);
        ft.commit();
    }
}
