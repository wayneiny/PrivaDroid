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
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.PermissionServerEvent;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.PrivaDroidApplication.FIREBASE_PROJECT_ALIAS;
import static com.weichengcao.privadroid.ui.MainScreenActivity.createEventTypeFragmentBundle;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_EVENT_TYPE;

public class PermissionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission, container, false);

        initializeSurveyedUnsueveyedFragments();

        return view;
    }

    private void initializeSurveyedUnsueveyedFragments() {
        final SurveyedCardFragment surveyedCardFragment = new SurveyedCardFragment();
        final UnsurveyedCardFragment unsurveyedCardFragment = new UnsurveyedCardFragment();

        /**
         * Pass event type to fragments.
         */
        surveyedCardFragment.setArguments(createEventTypeFragmentBundle(PERMISSION_EVENT_TYPE));
        unsurveyedCardFragment.setArguments(createEventTypeFragmentBundle(PERMISSION_EVENT_TYPE));

        /**
         * Query surveyed and unsurveyed permission events.
         */
        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        FirebaseApp app = FirebaseApp.getInstance(FIREBASE_PROJECT_ALIAS);
        CollectionReference collectionReference = FirebaseFirestore.getInstance(app).collection(PERMISSION_COLLECTION);
        Query query = collectionReference.whereEqualTo(EventUtil.USER_AD_ID, userPreferences.getAdvertisingId());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    PrivaDroidApplication.serverId2permissionServerSurveyedEvents = new HashMap<>();
                    PrivaDroidApplication.serverId2permissionServerUnsurveyedEvents = new HashMap<>();

                    if (task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String adId = doc.getString(EventUtil.USER_AD_ID);
                            String appName = doc.getString(EventUtil.APP_NAME);
                            String appVersion = doc.getString(EventUtil.APP_VERSION);
                            String loggedTime = doc.getString(EventUtil.LOGGED_TIME);
                            String packageName = doc.getString(EventUtil.PACKAGE_NAME);
                            String surveyId = doc.getString(EventUtil.SURVEY_ID);
                            String serverId = doc.getId();
                            String granted = doc.getString(EventUtil.GRANTED);
                            String initiatedByUser = doc.getString(EventUtil.INITIATED_BY_USER);
                            String permissionName = doc.getString(EventUtil.PERMISSION_REQUESTED_NAME);
                            PermissionServerEvent permissionServerEvent = new PermissionServerEvent(serverId,
                                    adId, appName, appVersion, loggedTime, packageName, surveyId, PERMISSION_EVENT_TYPE,
                                    initiatedByUser, permissionName, granted);

                            if (permissionServerEvent.isEventSurveyed()) {
                                PrivaDroidApplication.serverId2permissionServerSurveyedEvents.put(serverId, permissionServerEvent);
                            } else {
                                PrivaDroidApplication.serverId2permissionServerUnsurveyedEvents.put(serverId, permissionServerEvent);
                            }
                        }

                        if (surveyedCardFragment.getView() != null) {
                            int surveyedSize = PrivaDroidApplication.serverId2permissionServerSurveyedEvents.size();
                            ((TextView) surveyedCardFragment.getView()
                                    .findViewById(R.id.surveyed_card_description))
                                    .setText(getResources().getQuantityString(
                                            R.plurals.number_of_permission_surveyed_events_card_description,
                                            surveyedSize,
                                            surveyedSize));

                            if (PrivaDroidApplication.serverId2permissionServerSurveyedEvents.isEmpty()) {
                                surveyedCardFragment.getView()
                                        .findViewById(R.id.surveyed_card_button)
                                        .setEnabled(false);
                            }
                        }
                        if (unsurveyedCardFragment.getView() != null) {
                            int unsurveyedSize = PrivaDroidApplication.serverId2permissionServerUnsurveyedEvents.size();
                            ((TextView) unsurveyedCardFragment.getView()
                                    .findViewById(R.id.unsurveyed_card_description))
                                    .setText(getResources().getQuantityString(
                                            R.plurals.number_of_permission_unsurveyed_events_card_description,
                                            unsurveyedSize,
                                            unsurveyedSize));

                            if (PrivaDroidApplication.serverId2permissionServerUnsurveyedEvents.isEmpty()) {
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
        ft.replace(R.id.permission_surveyed_fragment, surveyedCardFragment);
        ft.replace(R.id.permission_unsurveyed_fragment, unsurveyedCardFragment);
        ft.commit();
    }
}
