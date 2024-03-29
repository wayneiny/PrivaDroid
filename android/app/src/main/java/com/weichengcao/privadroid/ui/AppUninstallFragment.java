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
import com.weichengcao.privadroid.database.AppUninstallServerEvent;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2appUninstallServerSurveyedEvents;
import static com.weichengcao.privadroid.PrivaDroidApplication.serverId2appUninstallServerUnsurveyedEvents;
import static com.weichengcao.privadroid.ui.MainScreenActivity.createEventTypeFragmentBundle;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;

public class AppUninstallFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_uninstall, container, false);

        initializeSurveyedUnsurveyedFragments();

        return view;
    }

    private void initializeSurveyedUnsurveyedFragments() {
        final SurveyedCardFragment surveyedCardFragment = new SurveyedCardFragment();
        final UnsurveyedCardFragment unsurveyedCardFragment = new UnsurveyedCardFragment();

        /**
         * Pass event type to fragments.
         */
        surveyedCardFragment.setArguments(createEventTypeFragmentBundle(APP_UNINSTALL_EVENT_TYPE));
        unsurveyedCardFragment.setArguments(createEventTypeFragmentBundle(APP_UNINSTALL_EVENT_TYPE));

        /**
         * Query surveyed and unsurveyed app install events.
         */
        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(APP_UNINSTALL_COLLECTION);
        Query query = collectionReference.whereEqualTo(EventUtil.USER_AD_ID, userPreferences.getAdvertisingId());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    serverId2appUninstallServerSurveyedEvents = new HashMap<>();
                    serverId2appUninstallServerUnsurveyedEvents = new HashMap<>();

                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String adId = document.getString(EventUtil.USER_AD_ID);
                            String appName = document.getString(EventUtil.APP_NAME);
                            String appVersion = document.getString(EventUtil.APP_VERSION);
                            String loggedTime = document.getString(EventUtil.LOGGED_TIME);
                            String packageName = document.getString(EventUtil.PACKAGE_NAME);
                            String surveyId = document.getString(EventUtil.SURVEY_ID);
                            String serverId = document.getId();
                            AppUninstallServerEvent event = new AppUninstallServerEvent(
                                    serverId, adId, appName, appVersion, loggedTime,
                                    packageName, surveyId, APP_UNINSTALL_EVENT_TYPE);

                            if (event.isEventSurveyed()) {
                                serverId2appUninstallServerSurveyedEvents.put(serverId, event);
                            } else {
                                serverId2appUninstallServerUnsurveyedEvents.put(serverId, event);
                            }
                        }

                        if (surveyedCardFragment.getView() != null) {
                            int surveyedSize = serverId2appUninstallServerSurveyedEvents.size();
                            ((TextView) surveyedCardFragment.getView()
                                    .findViewById(R.id.surveyed_card_description))
                                    .setText(getResources().getQuantityString(
                                            R.plurals.number_of_app_uninstall_surveyed_events_card_description,
                                            surveyedSize,
                                            surveyedSize));

                            if (serverId2appUninstallServerSurveyedEvents.isEmpty()) {
                                surveyedCardFragment.getView()
                                        .findViewById(R.id.surveyed_card_button)
                                        .setEnabled(false);
                            }
                        }
                        if (unsurveyedCardFragment.getView() != null) {
                            int unsurveyedSize = serverId2appUninstallServerUnsurveyedEvents.size();
                            ((TextView) unsurveyedCardFragment.getView()
                                    .findViewById(R.id.unsurveyed_card_description))
                                    .setText(getResources().getQuantityString(
                                            R.plurals.number_of_app_uninstall_unsurveyed_events_card_description,
                                            unsurveyedSize,
                                            unsurveyedSize));

                            if (serverId2appUninstallServerUnsurveyedEvents.isEmpty()) {
                                unsurveyedCardFragment.getView()
                                        .findViewById(R.id.unsurveyed_card_button)
                                        .setEnabled(false);
                            }
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
        ft.replace(R.id.app_uninstall_surveyed_fragment, surveyedCardFragment);
        ft.replace(R.id.app_uninstall_unsurveyed_fragment, unsurveyedCardFragment);
        ft.commit();
    }
}
