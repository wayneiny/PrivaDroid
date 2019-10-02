package com.weichengcao.privadroid.ui.SurveyQuestions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.AppUninstallServerEvent;
import com.weichengcao.privadroid.database.AppUninstallServerSurvey;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.Arrays;
import java.util.HashMap;

import static com.weichengcao.privadroid.PrivaDroidApplication.FIREBASE_PROJECT_ALIAS;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_SURVEY_COLLECTION;

public class AppUninstallSurveyActivity extends AppCompatActivity implements BaseSurveyActivity {

    private final static String TAG = AppInstallSurveyActivity.class.getSimpleName();

    private AppUninstallServerEvent currentAppUninstallServerEvent;
    private AppUninstallServerSurvey currentAppUninstallServerSurvey;

    private TextView mTitle;

    private ImageView mBack;
    private MaterialButton mSubmit;
    private TextView mAnsweredOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_uninstall_survey);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBack = findViewById(R.id.activity_app_uninstall_survey_back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitle = findViewById(R.id.activity_app_uninstall_survey_title);
        mAnsweredOn = findViewById(R.id.app_uninstall_survey_answered_on);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle payload = intent.getBundleExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD);
            if (payload == null) {
                return;
            }

            String eventServerId = payload.getString(EventUtil.EVENT_ID_INTENT_KEY);
            if (eventServerId == null) {
                return;
            }

            /**
             * Get proper event from Firestore.
             */
            FirebaseApp app = FirebaseApp.getInstance(FIREBASE_PROJECT_ALIAS);
            CollectionReference appUninstallEventCollectionRef = FirebaseFirestore.getInstance(app).collection(APP_UNINSTALL_COLLECTION);
            DocumentReference eventDocRef = appUninstallEventCollectionRef.document(eventServerId);
            eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            currentAppUninstallServerEvent = new AppUninstallServerEvent(doc.getId(),
                                    doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.APP_NAME),
                                    doc.getString(EventUtil.APP_VERSION), doc.getString(EventUtil.LOGGED_TIME),
                                    doc.getString(EventUtil.PACKAGE_NAME), doc.getString(EventUtil.SURVEY_ID),
                                    APP_UNINSTALL_EVENT_TYPE);

                            boolean surveyed = currentAppUninstallServerEvent.isEventSurveyed();

                            /**
                             * Set up UI elements.
                             */
                            mTitle.setText(currentAppUninstallServerEvent.getAppName());

                            /**
                             * Get survey from server if surveyed.
                             */
                            if (surveyed) {
                                FirebaseApp app = FirebaseApp.getInstance(FIREBASE_PROJECT_ALIAS);
                                CollectionReference appUninstallSurveyCollectionRef = FirebaseFirestore.getInstance(app).collection(APP_UNINSTALL_SURVEY_COLLECTION);
                                Query query = appUninstallSurveyCollectionRef.whereEqualTo(EventUtil.EVENT_SERVER_ID, currentAppUninstallServerEvent.getServerId());
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            AppUninstallServerSurvey appUninstallServerSurvey = null;
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                appUninstallServerSurvey = new AppUninstallServerSurvey(
                                                        doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.LOGGED_TIME),
                                                        EventUtil.APP_UNINSTALL_EVENT_TYPE, doc.getString(EventUtil.WHY_UNINSTALL),
                                                        doc.getString(EventUtil.PERMISSION_REMEMBERED_REQUESTED), currentAppUninstallServerEvent.getServerId(),
                                                        doc.getId());
                                            }
                                            if (appUninstallServerSurvey == null) {
                                                return;
                                            }

                                            /**
                                             * Set the survey for the current app uninstall event.
                                             */
                                            currentAppUninstallServerSurvey = appUninstallServerSurvey;
                                            PrivaDroidApplication.serverId2appUninstallSurveys.put(appUninstallServerSurvey.getServerId(), appUninstallServerSurvey);

                                            /**
                                             * Populate answers and set up answered on.
                                             */
                                            mAnsweredOn.setVisibility(View.VISIBLE);
                                            mAnsweredOn.setText(String.format("%s %s", getResources().getString(R.string.answered_on_prefix),
                                                    DatetimeUtil.convertIsoToReadableFormat(appUninstallServerSurvey.getLoggedTime())));

                                            setUpAnswerBasedOnSpinnerId(R.id.app_uninstall_spinner_why);
                                            setUpAnswerBasedOnSpinnerId(R.id.app_uninstall_spinner_permission_remembered_requested);
                                        }
                                    }
                                });
                            } else {
                                /**
                                 * Event not surveyed:
                                 * 1. Render submit button and hide answered on.
                                 * 2. Validate answers.
                                 * 3. Send to Firestore.
                                 */
                                mAnsweredOn.setVisibility(View.GONE);
                            }

                            setUpSubmit();
                        }
                    }
                }
            });
        }
    }

    /**
     * Validate answers.
     */
    @Override
    public boolean validateAnswerBasedOnQuestionId(int questionId) {
        Spinner spinner;
        TextView question;
        switch (questionId) {
            case R.id.app_uninstall_question_why:
                question = findViewById(R.id.app_uninstall_question_why);
                spinner = findViewById(R.id.app_uninstall_spinner_why);
                break;
            case R.id.app_uninstall_question_permission_remembered_requested:
                question = findViewById(R.id.app_uninstall_question_permission_remembered_requested);
                // TODO: change to multiple choice spinner
                spinner = findViewById(R.id.app_uninstall_spinner_permission_remembered_requested);
                break;
            default:
                return false;
        }
        String answer = spinner.getSelectedItem().toString();
        // TODO: change to multiple choice spinner
        if (answer.equals(getResources().getString(R.string.select_an_option))) {
            question.setTextColor(ContextCompat.getColor(this, R.color.colorAccentContrast));
            return false;
        }
        question.setTextColor(ContextCompat.getColor(this, R.color.black));
        return true;
    }

    /**
     * Set up answers from survey event.
     */
    @Override
    public void setUpAnswerBasedOnSpinnerId(int spinnerId) {
        Spinner spinner = findViewById(spinnerId);
        String[] options;
        switch (spinnerId) {
            case R.id.app_uninstall_spinner_why:
                options = getResources().getStringArray(R.array.app_uninstall_options_why);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentAppUninstallServerSurvey.getWhy()));
                break;
            case R.id.app_uninstall_spinner_permission_remembered_requested:
                // TODO: change to multiple choice spinner
                options = getResources().getStringArray(R.array.app_uninstall_options_permission_remembered_requested);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentAppUninstallServerSurvey.getPermissionsRequestedRemembered()[0]));
                break;
            default:
                return;
        }
        spinner.setEnabled(false);
    }

    @Override
    public HashMap<String, String> gatherResponse() {
        Spinner whySpinner = findViewById(R.id.app_uninstall_spinner_why);
        String why = whySpinner.getSelectedItem().toString();

        // TODO: change to multiple choice
        Spinner permissionRememberedRequestedSpinner = findViewById(R.id.app_uninstall_spinner_permission_remembered_requested);
        String permissionRememberedRequested = permissionRememberedRequestedSpinner.getSelectedItem().toString();

        String eventServerId = currentAppUninstallServerEvent.getServerId();

        return ExperimentEventFactory.createAppUninstallSurveyEvent(why, permissionRememberedRequested, eventServerId);
    }

    @Override
    public void setUpSubmit() {
        mSubmit = findViewById(R.id.app_uninstall_survey_submit_button);
        if (currentAppUninstallServerEvent.getSurveyId().isEmpty()) {
            mSubmit.setVisibility(View.VISIBLE);
            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!validateAnswerBasedOnQuestionId(R.id.app_uninstall_question_why) ||
                            !validateAnswerBasedOnQuestionId(R.id.app_uninstall_question_permission_remembered_requested)) {
                        Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finish_all_event_survey_questions_toast, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, String> response = gatherResponse();
                    FirestoreProvider firestoreProvider = new FirestoreProvider();
                    firestoreProvider.sendAppUninstallSurveyEvent(response);

                    Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finished_survey_toast, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            mSubmit.setVisibility(View.GONE);
        }
    }
}