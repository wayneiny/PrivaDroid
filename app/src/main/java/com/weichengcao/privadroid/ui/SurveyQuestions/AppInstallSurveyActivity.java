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
import com.weichengcao.privadroid.database.AppInstallServerEvent;
import com.weichengcao.privadroid.database.AppInstallServerSurvey;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.ExperimentEventFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_SURVEY_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.KNOW_PERMISSION_REQUIRED;

public class AppInstallSurveyActivity extends AppCompatActivity implements BaseSurveyActivity, View.OnClickListener {

    AppInstallServerEvent currentAppInstallServerEvent;
    AppInstallServerSurvey currentAppInstallServerSurvey;

    TextView mTitle;

    ImageView mBack;
    MaterialButton mSubmit;
    TextView mAnsweredOn;

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

        mWhy = findViewById(R.id.app_install_button_why);
        mWhy.setOnClickListener(this);
        mFactors = findViewById(R.id.app_install_button_factors);
        mFactors.setOnClickListener(this);
        mKnowPermission = findViewById(R.id.app_install_button_know_permission);
        mKnowPermission.setOnClickListener(this);
        mWhichPermissions = findViewById(R.id.app_install_button_which_permissions);
        mWhichPermissions.setOnClickListener(this);

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

                                            setUpAnswerBasedOnButtonId(R.id.app_install_button_why);
                                            setUpAnswerBasedOnButtonId(R.id.app_install_button_factors);
                                            setUpAnswerBasedOnButtonId(R.id.app_install_button_know_permission);
                                            setUpAnswerBasedOnButtonId(R.id.app_install_button_which_permissions);
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
            case R.id.app_install_question_why:
                question = findViewById(R.id.app_install_question_why);
                button = findViewById(R.id.app_install_button_why);
                break;
            case R.id.app_install_question_factors:
                question = findViewById(R.id.app_install_question_factors);
                button = findViewById(R.id.app_install_button_factors);
                break;
            case R.id.app_install_question_know_permission:
                question = findViewById(R.id.app_install_question_know_permission);
                button = findViewById(R.id.app_install_button_know_permission);
                break;
            case R.id.app_install_question_which_permissions:
                question = findViewById(R.id.app_install_question_which_permissions);
                button = findViewById(R.id.app_install_button_which_permissions);
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
            case R.id.app_install_button_why:
                button.setText(currentAppInstallServerSurvey.getWhy());
                break;
            case R.id.app_install_button_factors:
                button.setText(TextUtils.join(OPTION_DELIMITER, currentAppInstallServerSurvey.getFactors()));
                break;
            case R.id.app_install_button_know_permission:
                button.setText(currentAppInstallServerSurvey.getKnowPermission());
                break;
            case R.id.app_install_button_which_permissions:
                button.setText(TextUtils.join(OPTION_DELIMITER, currentAppInstallServerSurvey.getThinkPermissions()));
                break;
            default:
                return;
        }
        button.setEnabled(false);
    }

    @Override
    public HashMap<String, String> gatherResponse() {
        MaterialButton whyButton = findViewById(R.id.app_install_button_why);
        String why = whyButton.getText().toString();

        MaterialButton factorsButton = findViewById(R.id.app_install_button_factors);
        String factors = factorsButton.getText().toString();

        MaterialButton knowPermissionButton = findViewById(R.id.app_install_button_know_permission);
        String knowPermission = knowPermissionButton.getText().toString();

        MaterialButton thinkPermissionsButton = findViewById(R.id.app_install_button_which_permissions);
        String thinkPermissions = thinkPermissionsButton.getText().toString();

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

    boolean[] factorsChecked = new boolean[PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_install_options_factors).length];
    HashSet<Integer> selectedFactors = new HashSet<>();
    String[] factorOptions = EventUtil.randomizeSurveyQuestionOptions(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_install_options_factors), true, true);

    boolean[] whichPermissionsChecked = new boolean[PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_install_options_which_permission_think).length];
    HashSet<Integer> selectedWhichPermissions = new HashSet<>();
    String[] whichPermissionsOptions = EventUtil.randomizeSurveyQuestionOptions(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_install_options_which_permission_think), true, true);

    boolean[] whyChecked = new boolean[PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_install_options_why).length];
    HashSet<Integer> selectedWhys = new HashSet<>();
    String[] whyOptions = EventUtil.randomizeSurveyQuestionOptions(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.app_install_options_why), true, true);

    int selectedKnowPermission = -1;

    MaterialButton mWhy;
    MaterialButton mFactors;
    MaterialButton mKnowPermission;
    MaterialButton mWhichPermissions;

    @Override
    public void onClick(View view) {
        if (view == mWhy) {
            showQuestionOptionsDialog(R.id.app_install_button_why);
        } else if (view == mFactors) {
            showQuestionOptionsDialog(R.id.app_install_button_factors);
        } else if (view == mKnowPermission) {
            showQuestionOptionsDialog(R.id.app_install_button_know_permission);
        } else if (view == mWhichPermissions) {
            showQuestionOptionsDialog(R.id.app_install_button_which_permissions);
        }
    }

    @Override
    public void showQuestionOptionsDialog(int buttonId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        switch (buttonId) {
            case R.id.app_install_button_why:
                alertDialogBuilder.setTitle(getString(R.string.select_multiple_allowed));
                alertDialogBuilder.setMultiChoiceItems(whyOptions, whyChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedWhys.add(which);
                        } else {
                            selectedWhys.remove(which);
                        }
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedWhys.isEmpty()) {
                                    mWhy.setText(R.string.select_multiple_allowed);
                                    return;
                                }
                                ArrayList<String> whyTexts = new ArrayList<>();
                                for (int index : selectedWhys) {
                                    whyTexts.add(whyOptions[index]);
                                }
                                mWhy.setText(TextUtils.join(OPTION_DELIMITER, whyTexts));
                            }
                        });
                break;
            case R.id.app_install_button_factors:
                alertDialogBuilder.setTitle(getString(R.string.select_multiple_allowed));
                alertDialogBuilder.setMultiChoiceItems(factorOptions, factorsChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedFactors.add(which);
                        } else {
                            selectedFactors.remove(which);
                        }
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedFactors.isEmpty()) {
                                    mFactors.setText(R.string.select_multiple_allowed);
                                    return;
                                }
                                ArrayList<String> factorTexts = new ArrayList<>();
                                for (int index : selectedFactors) {
                                    factorTexts.add(factorOptions[index]);
                                }
                                mFactors.setText(TextUtils.join(OPTION_DELIMITER, factorTexts));
                            }
                        });
                break;
            case R.id.app_install_button_know_permission:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.app_install_options_know_permission, selectedKnowPermission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedKnowPermission = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedKnowPermission == -1) {
                            return;
                        }
                        mKnowPermission.setText(getResources().getStringArray(R.array.app_install_options_know_permission)[selectedKnowPermission]);
                    }
                });
                break;
            case R.id.app_install_button_which_permissions:
                alertDialogBuilder.setTitle(getString(R.string.select_multiple_allowed));
                alertDialogBuilder.setMultiChoiceItems(whichPermissionsOptions, whichPermissionsChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedWhichPermissions.add(which);
                        } else {
                            selectedWhichPermissions.remove(which);
                        }
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedWhichPermissions.isEmpty()) {
                                    mWhichPermissions.setText(R.string.select_multiple_allowed);
                                    return;
                                }
                                ArrayList<String> factorTexts = new ArrayList<>();
                                for (int index : selectedWhichPermissions) {
                                    factorTexts.add(whichPermissionsOptions[index]);
                                }
                                mWhichPermissions.setText(TextUtils.join(OPTION_DELIMITER, factorTexts));
                            }
                        });
                break;
        }

        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
