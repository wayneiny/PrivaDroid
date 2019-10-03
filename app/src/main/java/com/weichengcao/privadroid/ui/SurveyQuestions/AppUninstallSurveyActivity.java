package com.weichengcao.privadroid.ui.SurveyQuestions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.weichengcao.privadroid.database.AppUninstallServerEvent;
import com.weichengcao.privadroid.database.AppUninstallServerSurvey;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_SURVEY_COLLECTION;

public class AppUninstallSurveyActivity extends AppCompatActivity implements BaseSurveyActivity, View.OnClickListener {

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

        mWhy = findViewById(R.id.app_uninstall_button_why);
        mWhy.setOnClickListener(this);
        mRequestsRemembered = findViewById(R.id.app_uninstall_button_requests_remembered);
        mRequestsRemembered.setOnClickListener(this);

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
            CollectionReference appUninstallEventCollectionRef = FirebaseFirestore.getInstance().collection(APP_UNINSTALL_COLLECTION);
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
                                CollectionReference appUninstallSurveyCollectionRef = FirebaseFirestore.getInstance().collection(APP_UNINSTALL_SURVEY_COLLECTION);
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

                                            setUpAnswerBasedOnButtonId(R.id.app_uninstall_button_why);
                                            setUpAnswerBasedOnButtonId(R.id.app_uninstall_button_requests_remembered);
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
        MaterialButton button;
        TextView question;
        switch (questionId) {
            case R.id.app_uninstall_question_why:
                question = findViewById(R.id.app_uninstall_question_why);
                button = findViewById(R.id.app_uninstall_button_why);
                break;
            case R.id.app_uninstall_question_permission_remembered_requested:
                question = findViewById(R.id.app_uninstall_question_permission_remembered_requested);
                button = findViewById(R.id.app_uninstall_button_requests_remembered);
                break;
            default:
                return false;
        }
        String answer = button.getText().toString();
        if (answer.equals(getResources().getString(R.string.select_an_option)) || answer.equals(getString(R.string.select_multiple_allowed))) {
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
    public void setUpAnswerBasedOnButtonId(int buttonId) {
        MaterialButton button = findViewById(buttonId);
        switch (buttonId) {
            case R.id.app_uninstall_button_why:
                button.setText(currentAppUninstallServerSurvey.getWhy());
                break;
            case R.id.app_uninstall_button_requests_remembered:
                button.setText(TextUtils.join(OPTION_DELIMITER, currentAppUninstallServerSurvey.getPermissionsRequestedRemembered()));
                break;
            default:
                return;
        }
        button.setEnabled(false);
    }

    @Override
    public HashMap<String, String> gatherResponse() {
        MaterialButton whyButton = findViewById(R.id.app_uninstall_button_why);
        String why = whyButton.getText().toString();

        MaterialButton permissionRememberedButton = findViewById(R.id.app_uninstall_button_requests_remembered);
        String permissionRememberedRequested = permissionRememberedButton.getText().toString();

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

    boolean[] requestsRememberedChecked = new boolean[PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_uninstall_options_permission_remembered_requested).length];
    HashSet<Integer> selectedRequestsRemembered = new HashSet<>();
    int selectedWhy = -1;

    MaterialButton mWhy;
    MaterialButton mRequestsRemembered;

    @Override
    public void onClick(View view) {
        if (view == mWhy) {
            showQuestionOptionsDialog(R.id.app_uninstall_button_why);
        } else if (view == mRequestsRemembered) {
            showQuestionOptionsDialog(R.id.app_uninstall_button_requests_remembered);
        }
    }

    @Override
    public void showQuestionOptionsDialog(int buttonId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        switch (buttonId) {
            case R.id.app_uninstall_button_why:
                alertDialogBuilder.setTitle(R.string.select_an_option);
                alertDialogBuilder.setSingleChoiceItems(R.array.app_uninstall_options_why, selectedWhy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedWhy = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWhy.setText(getResources().getStringArray(R.array.app_uninstall_options_why)[selectedWhy]);
                    }
                });
                break;
            case R.id.app_uninstall_button_requests_remembered:
                alertDialogBuilder.setTitle(R.string.select_multiple_allowed);
                alertDialogBuilder.setMultiChoiceItems(R.array.app_uninstall_options_permission_remembered_requested, requestsRememberedChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedRequestsRemembered.add(which);
                        } else {
                            selectedRequestsRemembered.remove(which);
                        }
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedRequestsRemembered.isEmpty()) {
                            mRequestsRemembered.setText(R.string.select_multiple_allowed);
                            return;
                        }
                        String[] requestsRememberedOptions = getResources().getStringArray(R.array.app_uninstall_options_permission_remembered_requested);
                        ArrayList<String> requestsRememberedTexts = new ArrayList<>();
                        for (int index : selectedRequestsRemembered) {
                            requestsRememberedTexts.add(requestsRememberedOptions[index]);
                        }
                        mRequestsRemembered.setText(TextUtils.join(OPTION_DELIMITER, requestsRememberedTexts));
                    }
                });
                break;
        }

        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}