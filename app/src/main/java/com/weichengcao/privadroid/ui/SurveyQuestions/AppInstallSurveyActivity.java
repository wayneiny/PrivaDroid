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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.AppInstallServerEvent;
import com.weichengcao.privadroid.database.AppInstallServerSurvey;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.Arrays;
import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.KNOW_PERMISSION_REQUIRED;

public class AppInstallSurveyActivity extends AppCompatActivity implements BaseSurveyActivity {

    private final static String TAG = AppInstallSurveyActivity.class.getSimpleName();

    private AppInstallServerEvent currentAppInstallServerEvent;
    private AppInstallServerSurvey currentAppInstallServerSurvey;

    private TextView mTitle;

    private ImageView mBack;
    private MaterialButton mSubmit;
    private TextView mAnsweredOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_install_survey);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBack = findViewById(R.id.activity_app_install_survey_back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitle = findViewById(R.id.activity_app_install_survey_title);
        mAnsweredOn = findViewById(R.id.app_install_survey_answered_on);

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
            CollectionReference appInstallEventCollectionRef = FirebaseFirestore.getInstance().collection(APP_INSTALL_COLLECTION);
            DocumentReference eventDocRef = appInstallEventCollectionRef.document(eventServerId);
            eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null && doc.exists()) {
                            currentAppInstallServerEvent = new AppInstallServerEvent(doc.getId(),
                                    doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.APP_NAME),
                                    doc.getString(EventUtil.APP_VERSION), doc.getString(EventUtil.LOGGED_TIME),
                                    doc.getString(EventUtil.PACKAGE_NAME), doc.getString(EventUtil.SURVEY_ID),
                                    APP_INSTALL_EVENT_TYPE);

                            boolean surveyed = currentAppInstallServerEvent.isEventSurveyed();

                            /**
                             * Set up UI elements.
                             */
                            mTitle.setText(currentAppInstallServerEvent.getAppName());

                            /**
                             * Get survey from server if surveyed.
                             */
                            if (surveyed) {
                                CollectionReference appInstallSurveyCollectionRef = FirebaseFirestore.getInstance().collection(APP_INSTALL_SURVEY_COLLECTION);
                                Query query = appInstallSurveyCollectionRef.whereEqualTo(EventUtil.EVENT_SERVER_ID, currentAppInstallServerEvent.getServerId());
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            AppInstallServerSurvey appInstallServerSurvey = null;
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                appInstallServerSurvey = new AppInstallServerSurvey(
                                                        doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.LOGGED_TIME),
                                                        EventUtil.APP_INSTALL_EVENT_TYPE, doc.getString(EventUtil.WHY_INSTALL),
                                                        doc.getString(EventUtil.INSTALL_FACTORS), doc.getString(KNOW_PERMISSION_REQUIRED),
                                                        doc.getString(EventUtil.PERMISSIONS_THINK_REQUIRED), currentAppInstallServerEvent.getServerId(),
                                                        doc.getId());
                                            }
                                            if (appInstallServerSurvey == null) {
                                                return;
                                            }

                                            /**
                                             * Set the survey for the current app install event.
                                             */
                                            currentAppInstallServerSurvey = appInstallServerSurvey;
                                            PrivaDroidApplication.serverId2appInstallSurveys.put(appInstallServerSurvey.getServerId(), appInstallServerSurvey);

                                            /**
                                             * Populate answers and set up answered on.
                                             */
                                            mAnsweredOn.setVisibility(View.VISIBLE);
                                            mAnsweredOn.setText(String.format("%s %s", getResources().getString(R.string.answered_on_prefix),
                                                    DatetimeUtil.convertIsoToReadableFormat(appInstallServerSurvey.getLoggedTime())));

                                            setUpAnswerBasedOnSpinnerId(R.id.app_install_spinner_why);
                                            setUpAnswerBasedOnSpinnerId(R.id.app_install_spinner_factors);
                                            setUpAnswerBasedOnSpinnerId(R.id.app_install_spinner_know_permission);
                                            setUpAnswerBasedOnSpinnerId(R.id.app_install_spinner_which_permissions);
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
            case R.id.app_install_question_why:
                question = findViewById(R.id.app_install_question_why);
                spinner = findViewById(R.id.app_install_spinner_why);
                break;
            case R.id.app_install_question_factors:
                question = findViewById(R.id.app_install_question_factors);
                // TODO: change to multiple choice spinner
                spinner = findViewById(R.id.app_install_spinner_factors);
                break;
            case R.id.app_install_question_know_permission:
                question = findViewById(R.id.app_install_question_know_permission);
                spinner = findViewById(R.id.app_install_spinner_know_permission);
                break;
            case R.id.app_install_question_which_permissions:
                question = findViewById(R.id.app_install_question_which_permissions);
                // TODO: change to multiple choice spinner
                spinner = findViewById(R.id.app_install_spinner_which_permissions);
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
            case R.id.app_install_spinner_why:
                options = getResources().getStringArray(R.array.app_install_options_why);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentAppInstallServerSurvey.getWhy()));
                break;
            case R.id.app_install_spinner_factors:
                // TODO: change to multiple choice spinner
                options = getResources().getStringArray(R.array.app_install_options_factors);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentAppInstallServerSurvey.getFactors()[0]));
                break;
            case R.id.app_install_spinner_know_permission:
                options = getResources().getStringArray(R.array.app_install_options_know_permission);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentAppInstallServerSurvey.getKnowPermission()));
                break;
            case R.id.app_install_spinner_which_permissions:
                // TODO: change to multiple choice spinner
                options = getResources().getStringArray(R.array.app_install_options_which_permission_think);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentAppInstallServerSurvey.getThinkPermissions()[0]));
                break;
            default:
                return;
        }
        spinner.setEnabled(false);
    }

    @Override
    public HashMap<String, String> gatherResponse() {
        Spinner whySpinner = findViewById(R.id.app_install_spinner_why);
        String why = whySpinner.getSelectedItem().toString();

        // TODO: change to multiple choice
        Spinner factorsSpinner = findViewById(R.id.app_install_spinner_factors);
        String factors = factorsSpinner.getSelectedItem().toString();

        Spinner knowPermissionSpinner = findViewById(R.id.app_install_spinner_know_permission);
        String knowPermission = knowPermissionSpinner.getSelectedItem().toString();

        // TODO: change to multiple choice
        Spinner thinkPermissionsSpinner = findViewById(R.id.app_install_spinner_which_permissions);
        String thinkPermissions = thinkPermissionsSpinner.getSelectedItem().toString();

        String eventServerId = currentAppInstallServerEvent.getServerId();

        return ExperimentEventFactory.createAppInstallSurveyEvent(why, factors, knowPermission, thinkPermissions, eventServerId);
    }

    @Override
    public void setUpSubmit() {
        mSubmit = findViewById(R.id.app_install_survey_submit_button);
        if (currentAppInstallServerEvent.getSurveyId().isEmpty()) {
            mSubmit.setVisibility(View.VISIBLE);
            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!validateAnswerBasedOnQuestionId(R.id.app_install_question_why) ||
                            !validateAnswerBasedOnQuestionId(R.id.app_install_question_factors) ||
                            !validateAnswerBasedOnQuestionId(R.id.app_install_question_know_permission) ||
                            !validateAnswerBasedOnQuestionId(R.id.app_install_question_which_permissions)) {
                        Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finish_all_event_survey_questions_toast, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, String> response = gatherResponse();
                    FirestoreProvider firestoreProvider = new FirestoreProvider();
                    firestoreProvider.sendAppInstallSurveyEvent(response);

                    Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finished_survey_toast, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            mSubmit.setVisibility(View.GONE);
        }
    }
}
