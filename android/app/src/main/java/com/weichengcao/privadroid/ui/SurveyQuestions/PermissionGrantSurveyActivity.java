package com.weichengcao.privadroid.ui.SurveyQuestions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.ExperimentEventFactory;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.database.PermissionGrantServerSurvey;
import com.weichengcao.privadroid.database.PermissionServerEvent;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_COLLECTION;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_GRANT_SURVEY_COLLECTION;

public class PermissionGrantSurveyActivity extends AppCompatActivity implements BaseSurveyActivity, View.OnClickListener {

    PermissionServerEvent currentPermissionServerEvent;
    PermissionGrantServerSurvey currentPermissionGrantServerSurvey;

    TextView mTitle;

    ImageView mBack;
    MaterialButton mSubmit;
    TextView mAnsweredOn;

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

        mWhy = findViewById(R.id.permission_grant_button_why);
        mWhy.setOnClickListener(this);
        mExpected = findViewById(R.id.permission_grant_button_expected);
        mExpected.setOnClickListener(this);
        mComfortable = findViewById(R.id.permission_grant_button_comfortable);
        mComfortable.setOnClickListener(this);
        mTemporarily = findViewById(R.id.permission_grant_button_temporary);
        mTemporarily.setOnClickListener(this);
        mReminder = findViewById(R.id.permission_grant_button_reminder);
        mReminder.setOnClickListener(this);
        mReminderCard = findViewById(R.id.permission_grant_reminder_card);
        showReminderCard = false;

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

            boolean forPermissionRevokeReminder = payload.getBoolean(EventUtil.FOR_PERMISSION_REVOKE_REMINDER);
            if (forPermissionRevokeReminder) {
                // record user's action
                FirestoreProvider firestoreProvider = new FirestoreProvider();
                firestoreProvider.sendRevokePermissionNotificationClickEvent(
                        ExperimentEventFactory.createRevokePermissionReminderNotificationClickEvent(Boolean.toString(true), eventServerId));

                Intent permissionIntent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(permissionIntent);
                finish();
                return;
            }

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
                        if (doc != null && doc.exists()) {
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
                                                        doc.getString(EventUtil.COMFORT_LEVEL), doc.getString(EventUtil.WANT_TEMPORARY_GRANT_ONLY),
                                                        doc.getString(EventUtil.WOULD_LIKE_A_NOTIFICATION)
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

                                            setUpAnswerBasedOnButtonId(R.id.permission_grant_button_why);
                                            setUpAnswerBasedOnButtonId(R.id.permission_grant_button_expected);
                                            setUpAnswerBasedOnButtonId(R.id.permission_grant_button_comfortable);
                                            setUpAnswerBasedOnButtonId(R.id.permission_grant_button_temporary);
                                            if (permissionGrantServerSurvey.getOnlyGrantTemporarily()) {
                                                showReminderCard = true;
                                                mReminderCard.setVisibility(View.VISIBLE);
                                                setUpAnswerBasedOnButtonId(R.id.permission_grant_button_reminder);
                                            }
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
    public void setUpAnswerBasedOnButtonId(int buttonId) {
        MaterialButton button = findViewById(buttonId);
        switch (buttonId) {
            case R.id.permission_grant_button_why:
                button.setText(currentPermissionGrantServerSurvey.getWhyGrant());
                break;
            case R.id.permission_grant_button_expected:
                button.setText(currentPermissionGrantServerSurvey.getExpectedPermissionRequest());
                break;
            case R.id.permission_grant_button_comfortable:
                button.setText(currentPermissionGrantServerSurvey.getComfortableLevelGranting());
                break;
            case R.id.permission_grant_button_temporary:
                button.setText(currentPermissionGrantServerSurvey.getOnlyGrantTemporarilyText());
                break;
            case R.id.permission_grant_button_reminder:
                button.setText(currentPermissionGrantServerSurvey.getWouldLikeReminderNotificationText());
                break;
            default:
                return;
        }
        button.setEnabled(false);
    }

    @Override
    public boolean validateAnswerBasedOnQuestionId(int questionId) {
        MaterialButton button;
        TextView question;
        switch (questionId) {
            case R.id.permission_grant_question_why:
                question = findViewById(R.id.permission_grant_question_why);
                button = findViewById(R.id.permission_grant_button_why);
                break;
            case R.id.permission_grant_question_expected:
                question = findViewById(R.id.permission_grant_question_expected);
                button = findViewById(R.id.permission_grant_button_expected);
                break;
            case R.id.permission_grant_question_comfortable:
                question = findViewById(R.id.permission_grant_question_comfortable);
                button = findViewById(R.id.permission_grant_button_comfortable);
                break;
            case R.id.permission_grant_question_temporary:
                question = findViewById(R.id.permission_grant_question_temporary);
                button = findViewById(R.id.permission_grant_button_temporary);
                break;
            case R.id.permission_grant_question_reminder:
                question = findViewById(R.id.permission_grant_question_reminder);
                button = findViewById(R.id.permission_grant_button_reminder);
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

    @Override
    public HashMap<String, String> gatherResponse() {
        MaterialButton whyButton = findViewById(R.id.permission_grant_button_why);
        String whyGrant = whyButton.getText().toString();

        MaterialButton expectedButton = findViewById(R.id.permission_grant_button_expected);
        String expected = expectedButton.getText().toString();

        MaterialButton comfortButton = findViewById(R.id.permission_grant_button_comfortable);
        String comfort = comfortButton.getText().toString();

        MaterialButton temporaryButton = findViewById(R.id.permission_grant_button_temporary);
        String temporary = temporaryButton.getText().toString();

        String reminder;
        if (showReminderCard) {
            MaterialButton reminderButton = findViewById(R.id.permission_grant_button_reminder);
            reminder = reminderButton.getText().toString();
        } else {
            reminder = getString(R.string.no);
        }

        String eventServerId = currentPermissionServerEvent.getServerId();

        return ExperimentEventFactory.createPermissionGrantSurveyEvent(whyGrant, expected, comfort,
                eventServerId, temporary, reminder);
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
                            !validateAnswerBasedOnQuestionId(R.id.permission_grant_question_comfortable) ||
                            !validateAnswerBasedOnQuestionId(R.id.permission_grant_question_temporary) ||
                            (showReminderCard && !validateAnswerBasedOnQuestionId(R.id.permission_grant_question_reminder))) {
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

    boolean[] whyChecked = new boolean[PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.permission_grant_options_why).length];
    HashSet<Integer> selectedWhyIndices = new HashSet<>();
    String[] whyOptions = EventUtil.randomizeSurveyQuestionOptions(PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.permission_grant_options_why), true, true);

    int selectedExpected = -1;
    int selectedComfortable = -1;
    int selectedTemporary = -1;
    int selectedReminder = -1;

    MaterialButton mWhy;
    MaterialButton mExpected;
    MaterialButton mComfortable;
    MaterialButton mTemporarily;
    MaterialButton mReminder;
    MaterialCardView mReminderCard;
    boolean showReminderCard;

    @Override
    public void onClick(View view) {
        if (view == mWhy) {
            showQuestionOptionsDialog(R.id.permission_grant_button_why);
        } else if (view == mExpected) {
            showQuestionOptionsDialog(R.id.permission_grant_button_expected);
        } else if (view == mComfortable) {
            showQuestionOptionsDialog(R.id.permission_grant_button_comfortable);
        } else if (view == mTemporarily) {
            showQuestionOptionsDialog(R.id.permission_grant_button_temporary);
        } else if (view == mReminder) {
            showQuestionOptionsDialog(R.id.permission_grant_button_reminder);
        }
    }

    @Override
    public void showQuestionOptionsDialog(int buttonId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        switch (buttonId) {
            case R.id.permission_grant_button_why:
                alertDialogBuilder.setTitle(R.string.select_multiple_allowed);
                alertDialogBuilder.setMultiChoiceItems(whyOptions, whyChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedWhyIndices.add(which);
                        } else {
                            selectedWhyIndices.remove(which);
                        }
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedWhyIndices.isEmpty()) {
                            mWhy.setText(R.string.select_multiple_allowed);
                            return;
                        }
                        ArrayList<String> whyTexts = new ArrayList<>();
                        for (int index : selectedWhyIndices) {
                            whyTexts.add(whyOptions[index]);
                        }
                        mWhy.setText(TextUtils.join(OPTION_DELIMITER, whyTexts));
                    }
                });
                break;
            case R.id.permission_grant_button_expected:
                alertDialogBuilder.setTitle(R.string.select_an_option);
                alertDialogBuilder.setSingleChoiceItems(R.array.permission_options_expect_request, selectedExpected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedExpected = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedExpected == -1) {
                            return;
                        }
                        mExpected.setText(getResources().getStringArray(R.array.permission_options_expect_request)[selectedExpected]);
                    }
                });
                break;
            case R.id.permission_grant_button_comfortable:
                alertDialogBuilder.setTitle(R.string.select_an_option);
                alertDialogBuilder.setSingleChoiceItems(R.array.permission_options_comfortable, selectedComfortable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedComfortable = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedComfortable == -1) {
                            return;
                        }
                        mComfortable.setText(getResources().getStringArray(R.array.permission_options_comfortable)[selectedComfortable]);
                    }
                });
                break;
            case R.id.permission_grant_button_temporary:
                alertDialogBuilder.setTitle(R.string.select_an_option);
                alertDialogBuilder.setSingleChoiceItems(R.array.permission_grant_options_temporary, selectedTemporary, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTemporary = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedTemporary == -1) {
                            mReminderCard.setVisibility(View.GONE);
                            showReminderCard = false;
                            return;
                        } else if (selectedTemporary == 0) {
                            // user clicks 'Yes' to grant temporarily
                            showReminderCard = true;
                            mReminderCard.setVisibility(View.VISIBLE);
                        } else {
                            // user clicks 'No' so we remove the reminder question
                            showReminderCard = false;
                            selectedReminder = -1;
                            mReminder.setText(PrivaDroidApplication.getAppContext().getText(R.string.select_an_option));
                            mReminderCard.setVisibility(View.GONE);
                        }
                        mTemporarily.setText(getResources().getStringArray(R.array.permission_grant_options_temporary)[selectedTemporary]);
                    }
                });
                break;
            case R.id.permission_grant_button_reminder:
                alertDialogBuilder.setTitle(R.string.select_an_option);
                alertDialogBuilder.setSingleChoiceItems(R.array.permission_grant_options_reminder, selectedReminder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedReminder = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedReminder == -1) {
                            return;
                        }
                        mReminder.setText(getResources().getStringArray(R.array.permission_grant_options_reminder)[selectedReminder]);
                    }
                });
                break;
        }

        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}