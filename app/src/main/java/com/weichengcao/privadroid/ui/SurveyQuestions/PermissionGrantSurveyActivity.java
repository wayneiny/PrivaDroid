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
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.database.PermissionGrantServerSurvey;
import com.weichengcao.privadroid.database.PermissionServerEvent;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.Arrays;
import java.util.HashMap;

import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_GRANT_SURVEY_COLLECTION;

public class PermissionGrantSurveyActivity extends AppCompatActivity implements BaseSurveyActivity {

    private PermissionServerEvent currentPermissionServerEvent;
    private PermissionGrantServerSurvey currentPermissionGrantServerSurvey;

    private TextView mTitle;

    private ImageView mBack;
    private MaterialButton mSubmit;
    private TextView mAnsweredOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_grant_survey);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBack = findViewById(R.id.activity_permission_grant_survey_back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitle = findViewById(R.id.activity_permission_grant_survey_title);
        mAnsweredOn = findViewById(R.id.permission_grant_survey_answered_on);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle payload = intent.getBundleExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD);
            String eventServerId = payload.getString(EventUtil.EVENT_ID_INTENT_KEY);

            /**
             * Get proper event from Firestore.
             */
            CollectionReference permissionEventCollectionRef = FirebaseFirestore.getInstance().collection(PERMISSION_COLLECTION);
            DocumentReference eventDocRef = permissionEventCollectionRef.document(eventServerId);
            eventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            currentPermissionServerEvent = new PermissionServerEvent(doc.getId(),
                                    doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.APP_NAME),
                                    doc.getString(EventUtil.APP_VERSION), doc.getString(EventUtil.LOGGED_TIME),
                                    doc.getString(EventUtil.PACKAGE_NAME), doc.getString(EventUtil.SURVEY_ID),
                                    PERMISSION_EVENT_TYPE, doc.getString(EventUtil.INITIATED_BY_USER),
                                    doc.getString(EventUtil.PERMISSION_REQUESTED_NAME),
                                    doc.getString(EventUtil.GRANTED));

                            boolean surveyed = currentPermissionServerEvent.isEventSurveyed();

                            /**
                             * Set up UI elements.
                             */
                            mTitle.setText(getResources().getString(R.string.permission_app_name_list_item_title,
                                    currentPermissionServerEvent.getPermissionName(), currentPermissionServerEvent.getAppName()));

                            /**
                             * Get survey from server if surveyed.
                             */
                            if (surveyed) {
                                CollectionReference permissionGrantSurveyCollectionRef = FirebaseFirestore.getInstance().collection(PERMISSION_GRANT_SURVEY_COLLECTION);
                                Query query = permissionGrantSurveyCollectionRef.whereEqualTo(EventUtil.EVENT_SERVER_ID, currentPermissionServerEvent.getServerId());
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            PermissionGrantServerSurvey permissionGrantServerSurvey = null;
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                permissionGrantServerSurvey = new PermissionGrantServerSurvey(
                                                        doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.LOGGED_TIME),
                                                        PERMISSION_EVENT_TYPE, currentPermissionServerEvent.getServerId(), doc.getId(),
                                                        doc.getString(EventUtil.WHY_GRANT), doc.getString(EventUtil.EXPECTED_PERMISSION_REQUEST),
                                                        doc.getString(EventUtil.COMFORT_LEVEL)
                                                );
                                            }
                                            if (permissionGrantServerSurvey == null) {
                                                return;
                                            }

                                            /**
                                             * Set the survey for the current permission grant event.
                                             */
                                            currentPermissionGrantServerSurvey = permissionGrantServerSurvey;
                                            PrivaDroidApplication.serverId2permissionSurveys.put(permissionGrantServerSurvey.getServerId(), permissionGrantServerSurvey);

                                            /**
                                             * Populate answers and set up answered on.
                                             */
                                            mAnsweredOn.setVisibility(View.VISIBLE);
                                            mAnsweredOn.setText(String.format("%s %s", getResources().getString(R.string.answered_on_prefix),
                                                    DatetimeUtil.convertIsoToReadableFormat(permissionGrantServerSurvey.getLoggedTime())));

                                            setUpAnswerBasedOnSpinnerId(R.id.permission_grant_spinner_why);
                                            setUpAnswerBasedOnSpinnerId(R.id.permission_grant_spinner_expected);
                                            setUpAnswerBasedOnSpinnerId(R.id.permission_grant_spinner_comfortable);
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

    @Override
    public void setUpAnswerBasedOnSpinnerId(int spinnerId) {
        Spinner spinner = findViewById(spinnerId);
        String[] options;
        switch (spinnerId) {
            case R.id.permission_grant_spinner_why:
                options = getResources().getStringArray(R.array.permission_grant_options_why);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentPermissionGrantServerSurvey.getWhyGrant()));
                break;
            case R.id.permission_grant_spinner_expected:
                options = getResources().getStringArray(R.array.permission_options_expect_request);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentPermissionGrantServerSurvey.getExpectedPermissionRequest()));
                break;
            case R.id.permission_grant_spinner_comfortable:
                options = getResources().getStringArray(R.array.permission_options_comfortable);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(currentPermissionGrantServerSurvey.getComfortableLevelGranting()));
                break;
            default:
                return;
        }
        spinner.setEnabled(false);
    }

    @Override
    public boolean validateAnswerBasedOnQuestionId(int questionId) {
        Spinner spinner;
        TextView question;
        switch (questionId) {
            case R.id.permission_grant_question_why:
                question = findViewById(R.id.permission_grant_question_why);
                spinner = findViewById(R.id.permission_grant_spinner_why);
                break;
            case R.id.permission_grant_question_expected:
                question = findViewById(R.id.permission_grant_question_expected);
                spinner = findViewById(R.id.permission_grant_spinner_expected);
                break;
            case R.id.permission_grant_question_comfortable:
                question = findViewById(R.id.permission_grant_question_comfortable);
                spinner = findViewById(R.id.permission_grant_spinner_comfortable);
                break;
            default:
                return false;
        }
        String answer = spinner.getSelectedItem().toString();
        if (answer.equals(getResources().getString(R.string.select_an_option))) {
            question.setTextColor(ContextCompat.getColor(this, R.color.colorAccentContrast));
            return false;
        }
        question.setTextColor(ContextCompat.getColor(this, R.color.black));
        return true;
    }

    @Override
    public HashMap<String, String> gatherResponse() {
        Spinner whySpinner = findViewById(R.id.permission_grant_spinner_why);
        String whyGrant = whySpinner.getSelectedItem().toString();

        Spinner expectedSpinner = findViewById(R.id.permission_grant_spinner_expected);
        String expected = expectedSpinner.getSelectedItem().toString();

        Spinner comfortSpinner = findViewById(R.id.permission_grant_spinner_comfortable);
        String comfort = comfortSpinner.getSelectedItem().toString();

        String eventServerId = currentPermissionServerEvent.getServerId();

        return ExperimentEventFactory.createPermissionGrantSurveyEvent(whyGrant, expected, comfort, eventServerId);
    }

    @Override
    public void setUpSubmit() {
        mSubmit = findViewById(R.id.permission_grant_survey_submit_button);
        if (currentPermissionServerEvent.getSurveyId().isEmpty()) {
            mSubmit.setVisibility(View.VISIBLE);
            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!validateAnswerBasedOnQuestionId(R.id.permission_grant_question_why) ||
                            !validateAnswerBasedOnQuestionId(R.id.permission_grant_question_expected) ||
                            !validateAnswerBasedOnQuestionId(R.id.permission_grant_question_comfortable)) {
                        Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finish_all_event_survey_questions_toast, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    HashMap<String, String> response = gatherResponse();
                    FirestoreProvider firestoreProvider = new FirestoreProvider();
                    firestoreProvider.sendPermissionServerSurveyEvent(response, true);

                    Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finished_survey_toast, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            mSubmit.setVisibility(View.GONE);
        }
    }
}