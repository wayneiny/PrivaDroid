package com.weichengcao.privadroid.ui;

import android.content.DialogInterface;
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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.ExitSurveyServerEvent;
import com.weichengcao.privadroid.database.ExperimentEventFactory;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.ui.SurveyQuestions.BaseSurveyActivity;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.weichengcao.privadroid.database.FirestoreProvider.isNetworkAvailable;
import static com.weichengcao.privadroid.ui.ProfileFragment.REWARDS_DAYS;
import static com.weichengcao.privadroid.util.UserPreferences.UNKNOWN_DATE;

public class ExitSurveyActivity extends AppCompatActivity implements BaseSurveyActivity, View.OnClickListener {

    TextView mAnsweredOn;
    MaterialButton mSubmit;

    MaterialButton mControlOptionsOne, mControlOptionsTwo, mControlOptionsThree, mControlOptionsFour;
    MaterialButton mAwarenessOptionsOne, mAwarenessOptionsTwo, mAwarenessOptionsThree;
    MaterialButton mCollectionOptionsOne, mCollectionOptionsTwo, mCollectionOptionsThree, mCollectionOptionsFour;
    //    MaterialButton mErrorOptionsOne, mErrorOptionsTwo, mErrorOptionsThree, mErrorOptionsFour;
    MaterialButton mSecondaryUseOptionsOne, mSecondaryUseOptionsTwo, mSecondaryUseOptionsThree, mSecondaryUseOptionsFour;//, mSecondaryUseOptionsFive;
    //    MaterialButton mImproperOptionsOne, mImproperOptionsTwo, mImproperOptionsThree;
//    MaterialButton mGlobalOptionsOne, mGlobalOptionsTwo, mGlobalOptionsThree, mGlobalOptionsFour, mGlobalOptionsFive;
    MaterialButton mAdditionalOptionsFamiliar, mAdditionalOptionsDontUnderstand;

    ExitSurveyServerEvent exitSurveyServerEvent;

    MaterialCardView mJoinDate;
    TextView mJoinDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit_survey);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView mBack = findViewById(R.id.activity_exit_survey_back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAnsweredOn = findViewById(R.id.exit_survey_answered_on);
        mSubmit = findViewById(R.id.exit_survey_submit_button);

        mJoinDate = findViewById(R.id.exit_survey_days_left_card);
        mJoinDateText = findViewById(R.id.exit_survey_days_left_text);

        mControlOptionsOne = findViewById(R.id.exit_survey_control_button_one);
        mControlOptionsOne.setOnClickListener(this);
        mControlOptionsTwo = findViewById(R.id.exit_survey_control_button_two);
        mControlOptionsTwo.setOnClickListener(this);
        mControlOptionsThree = findViewById(R.id.exit_survey_control_button_three);
        mControlOptionsThree.setOnClickListener(this);
        mControlOptionsFour = findViewById(R.id.exit_survey_control_button_four);
        mControlOptionsFour.setOnClickListener(this);
        mAwarenessOptionsOne = findViewById(R.id.exit_survey_awareness_button_one);
        mAwarenessOptionsOne.setOnClickListener(this);
        mAwarenessOptionsTwo = findViewById(R.id.exit_survey_awareness_button_two);
        mAwarenessOptionsTwo.setOnClickListener(this);
        mAwarenessOptionsThree = findViewById(R.id.exit_survey_awareness_button_three);
        mAwarenessOptionsThree.setOnClickListener(this);
        mCollectionOptionsOne = findViewById(R.id.exit_survey_collection_button_one);
        mCollectionOptionsOne.setOnClickListener(this);
        mCollectionOptionsTwo = findViewById(R.id.exit_survey_collection_button_two);
        mCollectionOptionsTwo.setOnClickListener(this);
        mCollectionOptionsThree = findViewById(R.id.exit_survey_collection_button_three);
        mCollectionOptionsThree.setOnClickListener(this);
        mCollectionOptionsFour = findViewById(R.id.exit_survey_collection_button_four);
        mCollectionOptionsFour.setOnClickListener(this);
//        mErrorOptionsOne = findViewById(R.id.exit_survey_error_button_one);
//        mErrorOptionsOne.setOnClickListener(this);
//        mErrorOptionsTwo = findViewById(R.id.exit_survey_error_button_two);
//        mErrorOptionsTwo.setOnClickListener(this);
//        mErrorOptionsThree = findViewById(R.id.exit_survey_error_button_three);
//        mErrorOptionsThree.setOnClickListener(this);
//        mErrorOptionsFour = findViewById(R.id.exit_survey_error_button_four);
//        mErrorOptionsFour.setOnClickListener(this);
        mSecondaryUseOptionsOne = findViewById(R.id.exit_survey_secondary_use_button_one);
        mSecondaryUseOptionsOne.setOnClickListener(this);
        mSecondaryUseOptionsTwo = findViewById(R.id.exit_survey_secondary_use_button_two);
        mSecondaryUseOptionsTwo.setOnClickListener(this);
        mSecondaryUseOptionsThree = findViewById(R.id.exit_survey_secondary_use_button_three);
        mSecondaryUseOptionsThree.setOnClickListener(this);
        mSecondaryUseOptionsFour = findViewById(R.id.exit_survey_secondary_use_button_four);
        mSecondaryUseOptionsFour.setOnClickListener(this);
//        mSecondaryUseOptionsFive = findViewById(R.id.exit_survey_secondary_use_button_five);
//        mSecondaryUseOptionsFive.setOnClickListener(this);
//        mImproperOptionsOne = findViewById(R.id.exit_survey_improper_button_one);
//        mImproperOptionsOne.setOnClickListener(this);
//        mImproperOptionsTwo = findViewById(R.id.exit_survey_improper_button_two);
//        mImproperOptionsTwo.setOnClickListener(this);
//        mImproperOptionsThree = findViewById(R.id.exit_survey_improper_button_three);
//        mImproperOptionsThree.setOnClickListener(this);
//        mGlobalOptionsOne = findViewById(R.id.exit_survey_global_button_one);
//        mGlobalOptionsOne.setOnClickListener(this);
//        mGlobalOptionsTwo = findViewById(R.id.exit_survey_global_button_two);
//        mGlobalOptionsTwo.setOnClickListener(this);
//        mGlobalOptionsThree = findViewById(R.id.exit_survey_global_button_three);
//        mGlobalOptionsThree.setOnClickListener(this);
//        mGlobalOptionsFour = findViewById(R.id.exit_survey_global_button_four);
//        mGlobalOptionsFour.setOnClickListener(this);
//        mGlobalOptionsFive = findViewById(R.id.exit_survey_global_button_five);
//        mGlobalOptionsFive.setOnClickListener(this);
        mAdditionalOptionsFamiliar = findViewById(R.id.exit_survey_additional_button_familiar);
        mAdditionalOptionsFamiliar.setOnClickListener(this);
        mAdditionalOptionsDontUnderstand = findViewById(R.id.exit_survey_additional_button_dont_understand);
        mAdditionalOptionsDontUnderstand.setOnClickListener(this);

        final String joinDateText = new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate();
        if (!joinDateText.equals(UNKNOWN_DATE)) {
            DateTime joinDate = DateTime.parse(joinDateText);
            DateTime now = DateTime.now();

            mJoinDateText.setText(PrivaDroidApplication.getAppContext()
                    .getString(R.string.rewards_complete_join_date_text,
                            DatetimeUtil.convertIsoToReadableFormat(joinDateText)));

            boolean eligibleForExitSurvey = now.minusDays(REWARDS_DAYS).isAfter(joinDate);
            if (eligibleForExitSurvey) {
                /**
                 * Query if already filled survey.
                 */
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                CollectionReference rewardsColRef = firebaseFirestore.collection(EventUtil.EXIT_SURVEY_COLLECTION);
                Query query = rewardsColRef.whereEqualTo(EventUtil.USER_AD_ID, new UserPreferences(this).getAdvertisingId());
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                List<DocumentSnapshot> documentSnapshots = querySnapshot.getDocuments();
                                if (!documentSnapshots.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = documentSnapshots.get(0);

                                    exitSurveyServerEvent = new ExitSurveyServerEvent(documentSnapshot.getString(EventUtil.USER_AD_ID),
                                            documentSnapshot.getString(EventUtil.LOGGED_TIME),
                                            documentSnapshot.getString(EventUtil.CONTROL_ONE), documentSnapshot.getString(EventUtil.CONTROL_TWO), documentSnapshot.getString(EventUtil.CONTROL_THREE), documentSnapshot.getString(EventUtil.CONTROL_FOUR),
                                            documentSnapshot.getString(EventUtil.AWARENESS_ONE), documentSnapshot.getString(EventUtil.AWARENESS_TWO), documentSnapshot.getString(EventUtil.AWARENESS_THREE),
                                            documentSnapshot.getString(EventUtil.COLLECTION_ONE), documentSnapshot.getString(EventUtil.COLLECTION_TWO), documentSnapshot.getString(EventUtil.COLLECTION_THREE), documentSnapshot.getString(EventUtil.COLLECTION_FOUR),
                                            documentSnapshot.getString(EventUtil.ERROR_ONE), documentSnapshot.getString(EventUtil.ERROR_TWO), documentSnapshot.getString(EventUtil.ERROR_THREE), documentSnapshot.getString(EventUtil.ERROR_FOUR),
                                            documentSnapshot.getString(EventUtil.SECONDARY_USE_ONE), documentSnapshot.getString(EventUtil.SECONDARY_USE_TWO), documentSnapshot.getString(EventUtil.SECONDARY_USE_THREE), documentSnapshot.getString(EventUtil.SECONDARY_USE_FOUR), documentSnapshot.getString(EventUtil.SECONDARY_USE_FIVE),
                                            documentSnapshot.getString(EventUtil.IMPROPER_ONE), documentSnapshot.getString(EventUtil.IMPROPER_TWO), documentSnapshot.getString(EventUtil.IMPROPER_THREE),
                                            documentSnapshot.getString(EventUtil.GLOBAL_ONE), documentSnapshot.getString(EventUtil.GLOBAL_TWO), documentSnapshot.getString(EventUtil.GLOBAL_THREE), documentSnapshot.getString(EventUtil.GLOBAL_FOUR), documentSnapshot.getString(EventUtil.GLOBAL_FIVE),
                                            documentSnapshot.getString(EventUtil.FAMILIAR_WITH_ANDROID_PERMISSION), documentSnapshot.getString(EventUtil.PERMISSIONS_THAT_DONT_UNDERSTAND));

                                    mJoinDateText.setText(PrivaDroidApplication.getAppContext()
                                            .getString(R.string.exit_survey_completed_complete_join_date_text,
                                                    DatetimeUtil.convertIsoToReadableFormat(joinDateText)));
                                    mAnsweredOn.setVisibility(View.VISIBLE);
                                    mAnsweredOn.setText(String.format("%s %s", PrivaDroidApplication.getAppContext().getResources().getString(R.string.answered_on_prefix),
                                            DatetimeUtil.convertIsoToReadableFormat(exitSurveyServerEvent.getLoggedTime())));

                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_control_button_one);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_control_button_two);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_control_button_three);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_control_button_four);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_awareness_button_one);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_awareness_button_two);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_awareness_button_three);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_collection_button_one);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_collection_button_two);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_collection_button_three);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_collection_button_four);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_error_button_one);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_error_button_two);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_error_button_three);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_error_button_four);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_secondary_use_button_one);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_secondary_use_button_two);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_secondary_use_button_three);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_secondary_use_button_four);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_secondary_use_button_five);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_improper_button_one);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_improper_button_two);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_improper_button_three);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_global_button_one);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_global_button_two);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_global_button_three);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_global_button_four);
//                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_global_button_five);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_additional_button_familiar);
                                    setUpAnswerBasedOnButtonId(R.id.exit_survey_additional_button_dont_understand);

                                    mSubmit.setVisibility(View.GONE);
                                    return;
                                }
                            }
                        }

                        mAnsweredOn.setVisibility(View.GONE);
                        setUpSubmit();

                        mJoinDateText.setText(PrivaDroidApplication.getAppContext()
                                .getString(R.string.exit_survey_complete_join_date_text,
                                        DatetimeUtil.convertIsoToReadableFormat(joinDateText)));
                    }
                });
            } else {
                /**
                 * Hide cards and add join date card
                 */
                mSubmit.setVisibility(View.GONE);
                mAnsweredOn.setVisibility(View.GONE);

                findViewById(R.id.exit_survey_control_card).setVisibility(View.GONE);
                findViewById(R.id.exit_survey_secondary_use_card).setVisibility(View.GONE);
                findViewById(R.id.exit_survey_awareness_card).setVisibility(View.GONE);
//                findViewById(R.id.exit_survey_improper_card).setVisibility(View.GONE);
                findViewById(R.id.exit_survey_collection_card).setVisibility(View.GONE);
//                findViewById(R.id.exit_survey_global_card).setVisibility(View.GONE);
                findViewById(R.id.exit_survey_additional_card).setVisibility(View.GONE);
//                findViewById(R.id.exit_survey_error_card).setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(PrivaDroidApplication.getAppContext(), PrivaDroidApplication.getAppContext().getString(R.string.rewards_join_date_invalid_contact_team), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mControlOptionsOne) {
            showQuestionOptionsDialog(R.id.exit_survey_control_button_one);
        } else if (v == mControlOptionsTwo) {
            showQuestionOptionsDialog(R.id.exit_survey_control_button_two);
        } else if (v == mControlOptionsThree) {
            showQuestionOptionsDialog(R.id.exit_survey_control_button_three);
        } else if (v == mControlOptionsFour) {
            showQuestionOptionsDialog(R.id.exit_survey_control_button_four);
        } else if (v == mAwarenessOptionsOne) {
            showQuestionOptionsDialog(R.id.exit_survey_awareness_button_one);
        } else if (v == mAwarenessOptionsTwo) {
            showQuestionOptionsDialog(R.id.exit_survey_awareness_button_two);
        } else if (v == mAwarenessOptionsThree) {
            showQuestionOptionsDialog(R.id.exit_survey_awareness_button_three);
        } else if (v == mCollectionOptionsOne) {
            showQuestionOptionsDialog(R.id.exit_survey_collection_button_one);
        } else if (v == mCollectionOptionsTwo) {
            showQuestionOptionsDialog(R.id.exit_survey_collection_button_two);
        } else if (v == mCollectionOptionsThree) {
            showQuestionOptionsDialog(R.id.exit_survey_collection_button_three);
        } else if (v == mCollectionOptionsFour) {
            showQuestionOptionsDialog(R.id.exit_survey_collection_button_four);
//        } else if (v == mErrorOptionsOne) {
//            showQuestionOptionsDialog(R.id.exit_survey_error_button_one);
//        } else if (v == mErrorOptionsTwo) {
//            showQuestionOptionsDialog(R.id.exit_survey_error_button_two);
//        } else if (v == mErrorOptionsThree) {
//            showQuestionOptionsDialog(R.id.exit_survey_error_button_three);
//        } else if (v == mErrorOptionsFour) {
//            showQuestionOptionsDialog(R.id.exit_survey_error_button_four);
        } else if (v == mSecondaryUseOptionsOne) {
            showQuestionOptionsDialog(R.id.exit_survey_secondary_use_button_one);
        } else if (v == mSecondaryUseOptionsTwo) {
            showQuestionOptionsDialog(R.id.exit_survey_secondary_use_button_two);
        } else if (v == mSecondaryUseOptionsThree) {
            showQuestionOptionsDialog(R.id.exit_survey_secondary_use_button_three);
        } else if (v == mSecondaryUseOptionsFour) {
            showQuestionOptionsDialog(R.id.exit_survey_secondary_use_button_four);
//        } else if (v == mSecondaryUseOptionsFive) {
//            showQuestionOptionsDialog(R.id.exit_survey_secondary_use_button_five);
//        } else if (v == mImproperOptionsOne) {
//            showQuestionOptionsDialog(R.id.exit_survey_improper_button_one);
//        } else if (v == mImproperOptionsTwo) {
//            showQuestionOptionsDialog(R.id.exit_survey_improper_button_two);
//        } else if (v == mImproperOptionsThree) {
//            showQuestionOptionsDialog(R.id.exit_survey_improper_button_three);
//        } else if (v == mGlobalOptionsOne) {
//            showQuestionOptionsDialog(R.id.exit_survey_global_button_one);
//        } else if (v == mGlobalOptionsTwo) {
//            showQuestionOptionsDialog(R.id.exit_survey_global_button_two);
//        } else if (v == mGlobalOptionsThree) {
//            showQuestionOptionsDialog(R.id.exit_survey_global_button_three);
//        } else if (v == mGlobalOptionsFour) {
//            showQuestionOptionsDialog(R.id.exit_survey_global_button_four);
//        } else if (v == mGlobalOptionsFive) {
//            showQuestionOptionsDialog(R.id.exit_survey_global_button_five);
        } else if (v == mAdditionalOptionsFamiliar) {
            showQuestionOptionsDialog(R.id.exit_survey_additional_button_familiar);
        } else if (v == mAdditionalOptionsDontUnderstand) {
            showQuestionOptionsDialog(R.id.exit_survey_additional_button_dont_understand);
        }
    }

    @Override
    public void setUpSubmit() {
        mSubmit = findViewById(R.id.exit_survey_submit_button);
        if (new UserPreferences(this).getAnsweredExitSurvey()) {
            mSubmit.setVisibility(View.GONE);
            return;
        }
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(PrivaDroidApplication.getAppContext(), PrivaDroidApplication.getAppContext().getString(R.string.no_internet_connection_error), Toast.LENGTH_LONG).show();
                    return;
                }

                if (!validateAnswerBasedOnQuestionId(R.id.exit_survey_control_question_one) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_control_question_two) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_control_question_three) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_control_question_four) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_awareness_question_one) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_awareness_question_two) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_awareness_question_three) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_collection_question_one) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_collection_question_two) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_collection_question_three) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_collection_question_four) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_error_question_one) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_error_question_two) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_error_question_three) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_error_question_four) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_secondary_use_question_one) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_secondary_use_question_two) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_secondary_use_question_three) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_secondary_use_question_four) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_secondary_use_question_five) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_improper_question_one) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_improper_question_two) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_improper_question_three) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_global_question_one) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_global_question_two) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_global_question_three) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_global_question_four) ||
//                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_global_question_five) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_additional_question_familiar) ||
                        !validateAnswerBasedOnQuestionId(R.id.exit_survey_additional_question_dont_understand)) {
                    Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finish_all_event_survey_questions_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, String> response = gatherResponse();
                FirestoreProvider firestoreProvider = new FirestoreProvider();
                firestoreProvider.sendExitSurveyEvent(response);
                new UserPreferences(PrivaDroidApplication.getAppContext()).setAnsweredExitSurvey(true);

                Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finished_survey_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean validateAnswerBasedOnQuestionId(int questionId) {
        MaterialButton button;
        TextView question;
        switch (questionId) {
            case R.id.exit_survey_control_question_one:
                question = findViewById(R.id.exit_survey_control_question_one);
                button = findViewById(R.id.exit_survey_control_button_one);
                break;
            case R.id.exit_survey_control_question_two:
                question = findViewById(R.id.exit_survey_control_question_two);
                button = findViewById(R.id.exit_survey_control_button_two);
                break;
            case R.id.exit_survey_control_question_three:
                question = findViewById(R.id.exit_survey_control_question_three);
                button = findViewById(R.id.exit_survey_control_button_three);
                break;
            case R.id.exit_survey_control_question_four:
                question = findViewById(R.id.exit_survey_control_question_four);
                button = findViewById(R.id.exit_survey_control_button_four);
                break;
            case R.id.exit_survey_awareness_question_one:
                question = findViewById(R.id.exit_survey_awareness_question_one);
                button = findViewById(R.id.exit_survey_awareness_button_one);
                break;
            case R.id.exit_survey_awareness_question_two:
                question = findViewById(R.id.exit_survey_awareness_question_two);
                button = findViewById(R.id.exit_survey_awareness_button_two);
                break;
            case R.id.exit_survey_awareness_question_three:
                question = findViewById(R.id.exit_survey_awareness_question_three);
                button = findViewById(R.id.exit_survey_awareness_button_three);
                break;
            case R.id.exit_survey_collection_question_one:
                question = findViewById(R.id.exit_survey_collection_question_one);
                button = findViewById(R.id.exit_survey_collection_button_one);
                break;
            case R.id.exit_survey_collection_question_two:
                question = findViewById(R.id.exit_survey_collection_question_two);
                button = findViewById(R.id.exit_survey_collection_button_two);
                break;
            case R.id.exit_survey_collection_question_three:
                question = findViewById(R.id.exit_survey_collection_question_three);
                button = findViewById(R.id.exit_survey_collection_button_three);
                break;
            case R.id.exit_survey_collection_question_four:
                question = findViewById(R.id.exit_survey_collection_question_four);
                button = findViewById(R.id.exit_survey_collection_button_four);
                break;
//            case R.id.exit_survey_error_question_one:
//                question = findViewById(R.id.exit_survey_error_question_one);
//                button = findViewById(R.id.exit_survey_error_button_one);
//                break;
//            case R.id.exit_survey_error_question_two:
//                question = findViewById(R.id.exit_survey_error_question_two);
//                button = findViewById(R.id.exit_survey_error_button_two);
//                break;
//            case R.id.exit_survey_error_question_three:
//                question = findViewById(R.id.exit_survey_error_question_three);
//                button = findViewById(R.id.exit_survey_error_button_three);
//                break;
//            case R.id.exit_survey_error_question_four:
//                question = findViewById(R.id.exit_survey_error_question_four);
//                button = findViewById(R.id.exit_survey_error_button_four);
//                break;
            case R.id.exit_survey_secondary_use_question_one:
                question = findViewById(R.id.exit_survey_secondary_use_question_one);
                button = findViewById(R.id.exit_survey_secondary_use_button_one);
                break;
            case R.id.exit_survey_secondary_use_question_two:
                question = findViewById(R.id.exit_survey_secondary_use_question_two);
                button = findViewById(R.id.exit_survey_secondary_use_button_two);
                break;
            case R.id.exit_survey_secondary_use_question_three:
                question = findViewById(R.id.exit_survey_secondary_use_question_three);
                button = findViewById(R.id.exit_survey_secondary_use_button_three);
                break;
            case R.id.exit_survey_secondary_use_question_four:
                question = findViewById(R.id.exit_survey_secondary_use_question_four);
                button = findViewById(R.id.exit_survey_secondary_use_button_four);
                break;
//            case R.id.exit_survey_secondary_use_question_five:
//                question = findViewById(R.id.exit_survey_secondary_use_question_five);
//                button = findViewById(R.id.exit_survey_secondary_use_button_five);
//                break;
//            case R.id.exit_survey_improper_question_one:
//                question = findViewById(R.id.exit_survey_improper_question_one);
//                button = findViewById(R.id.exit_survey_improper_button_one);
//                break;
//            case R.id.exit_survey_improper_question_two:
//                question = findViewById(R.id.exit_survey_improper_question_two);
//                button = findViewById(R.id.exit_survey_improper_button_two);
//                break;
//            case R.id.exit_survey_improper_question_three:
//                question = findViewById(R.id.exit_survey_improper_question_three);
//                button = findViewById(R.id.exit_survey_improper_button_three);
//                break;
//            case R.id.exit_survey_global_question_one:
//                question = findViewById(R.id.exit_survey_global_question_one);
//                button = findViewById(R.id.exit_survey_global_button_one);
//                break;
//            case R.id.exit_survey_global_question_two:
//                question = findViewById(R.id.exit_survey_global_question_two);
//                button = findViewById(R.id.exit_survey_global_button_two);
//                break;
//            case R.id.exit_survey_global_question_three:
//                question = findViewById(R.id.exit_survey_global_question_three);
//                button = findViewById(R.id.exit_survey_global_button_three);
//                break;
//            case R.id.exit_survey_global_question_four:
//                question = findViewById(R.id.exit_survey_global_question_four);
//                button = findViewById(R.id.exit_survey_global_button_four);
//                break;
//            case R.id.exit_survey_global_question_five:
//                question = findViewById(R.id.exit_survey_global_question_five);
//                button = findViewById(R.id.exit_survey_global_button_five);
//                break;
            case R.id.exit_survey_additional_question_familiar:
                question = findViewById(R.id.exit_survey_additional_question_familiar);
                button = findViewById(R.id.exit_survey_additional_button_familiar);
                break;
            case R.id.exit_survey_additional_question_dont_understand:
                question = findViewById(R.id.exit_survey_additional_question_dont_understand);
                button = findViewById(R.id.exit_survey_additional_button_dont_understand);
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
    public void setUpAnswerBasedOnButtonId(int buttonId) {
        MaterialButton button = findViewById(buttonId);
        switch (buttonId) {
            case R.id.exit_survey_control_button_one:
                button.setText(exitSurveyServerEvent.getControlOne());
                break;
            case R.id.exit_survey_control_button_two:
                button.setText(exitSurveyServerEvent.getControlTwo());
                break;
            case R.id.exit_survey_control_button_three:
                button.setText(exitSurveyServerEvent.getControlThree());
                break;
            case R.id.exit_survey_control_button_four:
                button.setText(exitSurveyServerEvent.getControlFour());
                break;
            case R.id.exit_survey_awareness_button_one:
                button.setText(exitSurveyServerEvent.getAwarenessOne());
                break;
            case R.id.exit_survey_awareness_button_two:
                button.setText(exitSurveyServerEvent.getAwarenessTwo());
                break;
            case R.id.exit_survey_awareness_button_three:
                button.setText(exitSurveyServerEvent.getAwarenessThree());
                break;
            case R.id.exit_survey_collection_button_one:
                button.setText(exitSurveyServerEvent.getCollectionOne());
                break;
            case R.id.exit_survey_collection_button_two:
                button.setText(exitSurveyServerEvent.getCollectionTwo());
                break;
            case R.id.exit_survey_collection_button_three:
                button.setText(exitSurveyServerEvent.getCollectionThree());
                break;
            case R.id.exit_survey_collection_button_four:
                button.setText(exitSurveyServerEvent.getCollectionFour());
                break;
//            case R.id.exit_survey_error_button_one:
//                button.setText(exitSurveyServerEvent.getErrorOne());
//                break;
//            case R.id.exit_survey_error_button_two:
//                button.setText(exitSurveyServerEvent.getErrorTwo());
//                break;
//            case R.id.exit_survey_error_button_three:
//                button.setText(exitSurveyServerEvent.getErrorThree());
//                break;
//            case R.id.exit_survey_error_button_four:
//                button.setText(exitSurveyServerEvent.getErrorFour());
//                break;
            case R.id.exit_survey_secondary_use_button_one:
                button.setText(exitSurveyServerEvent.getSecondaryUseOne());
                break;
            case R.id.exit_survey_secondary_use_button_two:
                button.setText(exitSurveyServerEvent.getSecondaryUseTwo());
                break;
            case R.id.exit_survey_secondary_use_button_three:
                button.setText(exitSurveyServerEvent.getSecondaryUseThree());
                break;
            case R.id.exit_survey_secondary_use_button_four:
                button.setText(exitSurveyServerEvent.getSecondaryUseFour());
                break;
//            case R.id.exit_survey_secondary_use_button_five:
//                button.setText(exitSurveyServerEvent.getSecondaryUseFive());
//                break;
//            case R.id.exit_survey_improper_button_one:
//                button.setText(exitSurveyServerEvent.getImproperOne());
//                break;
//            case R.id.exit_survey_improper_button_two:
//                button.setText(exitSurveyServerEvent.getImproperTwo());
//                break;
//            case R.id.exit_survey_improper_button_three:
//                button.setText(exitSurveyServerEvent.getImproperThree());
//                break;
//            case R.id.exit_survey_global_button_one:
//                button.setText(exitSurveyServerEvent.getGlobalOne());
//                break;
//            case R.id.exit_survey_global_button_two:
//                button.setText(exitSurveyServerEvent.getGlobalTwo());
//                break;
//            case R.id.exit_survey_global_button_three:
//                button.setText(exitSurveyServerEvent.getGlobalThree());
//                break;
//            case R.id.exit_survey_global_button_four:
//                button.setText(exitSurveyServerEvent.getGlobalFour());
//                break;
//            case R.id.exit_survey_global_button_five:
//                button.setText(exitSurveyServerEvent.getGlobalFive());
//                break;
            case R.id.exit_survey_additional_button_familiar:
                button.setText(exitSurveyServerEvent.getFamiliar());
                break;
            case R.id.exit_survey_additional_button_dont_understand:
                button.setText(TextUtils.join(OPTION_DELIMITER, exitSurveyServerEvent.getDontKnowPermissions()));
                break;
            default:
                return;
        }
        button.setEnabled(false);
    }

//    @Override
//    public HashMap<String, String> gatherResponse() {
//        return ExperimentEventFactory.createExitSurveyEvent(
//                mControlOptionsOne.getText().toString(), mControlOptionsTwo.getText().toString(), mControlOptionsThree.getText().toString(),
//                mAwarenessOptionsOne.getText().toString(), mAwarenessOptionsTwo.getText().toString(), mAwarenessOptionsThree.getText().toString(),
//                mCollectionOptionsOne.getText().toString(), mCollectionOptionsTwo.getText().toString(), mCollectionOptionsThree.getText().toString(), mCollectionOptionsFour.getText().toString(),
//                mErrorOptionsOne.getText().toString(), mErrorOptionsTwo.getText().toString(), mErrorOptionsThree.getText().toString(), mErrorOptionsFour.getText().toString(),
//                mSecondaryUseOptionsOne.getText().toString(), mSecondaryUseOptionsTwo.getText().toString(), mSecondaryUseOptionsThree.getText().toString(), mSecondaryUseOptionsFour.getText().toString(), mSecondaryUseOptionsFive.getText().toString(),
//                mImproperOptionsOne.getText().toString(), mImproperOptionsTwo.getText().toString(), mImproperOptionsThree.getText().toString(),
//                mGlobalOptionsOne.getText().toString(), mGlobalOptionsTwo.getText().toString(), mGlobalOptionsThree.getText().toString(), mGlobalOptionsFour.getText().toString(), mGlobalOptionsFive.getText().toString(),
//                mAdditionalOptionsFamiliar.getText().toString(), mAdditionalOptionsDontUnderstand.getText().toString());
//    }

    @Override
    public HashMap<String, String> gatherResponse() {
        return ExperimentEventFactory.createExitSurveyEvent(
                mControlOptionsOne.getText().toString(), mControlOptionsTwo.getText().toString(), mControlOptionsThree.getText().toString(), mControlOptionsFour.getText().toString(),
                mAwarenessOptionsOne.getText().toString(), mAwarenessOptionsTwo.getText().toString(), mAwarenessOptionsThree.getText().toString(),
                mCollectionOptionsOne.getText().toString(), mCollectionOptionsTwo.getText().toString(), mCollectionOptionsThree.getText().toString(), mCollectionOptionsFour.getText().toString(),
                null, null, null, null,
                mSecondaryUseOptionsOne.getText().toString(), mSecondaryUseOptionsTwo.getText().toString(), mSecondaryUseOptionsThree.getText().toString(), mSecondaryUseOptionsFour.getText().toString(), null,
                null, null, null,
                null, null, null, null, null,
                mAdditionalOptionsFamiliar.getText().toString(), mAdditionalOptionsDontUnderstand.getText().toString());
    }

    @Override
    public void showQuestionOptionsDialog(int buttonId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        switch (buttonId) {
            case R.id.exit_survey_control_button_one:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedControlOne, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedControlOne = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedControlOne == -1) {
                            return;
                        }
                        mControlOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedControlOne]);
                    }
                });
                break;
            case R.id.exit_survey_control_button_two:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedControlTwo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedControlTwo = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedControlTwo == -1) {
                            return;
                        }
                        mControlOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedControlTwo]);
                    }
                });
                break;
            case R.id.exit_survey_control_button_three:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedControlThree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedControlThree = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedControlThree == -1) {
                            return;
                        }
                        mControlOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedControlThree]);
                    }
                });
                break;
            case R.id.exit_survey_control_button_four:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedControlFour, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedControlFour = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedControlFour == -1) {
                            return;
                        }
                        mControlOptionsFour.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedControlFour]);
                    }
                });
                break;
            case R.id.exit_survey_awareness_button_one:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedAwarenessOne, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedAwarenessOne = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedAwarenessOne == -1) {
                            return;
                        }
                        mAwarenessOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedAwarenessOne]);
                    }
                });
                break;
            case R.id.exit_survey_awareness_button_two:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedAwarenessTwo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedAwarenessTwo = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedAwarenessTwo == -1) {
                            return;
                        }
                        mAwarenessOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedAwarenessTwo]);
                    }
                });
                break;
            case R.id.exit_survey_awareness_button_three:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedAwarenessThree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedAwarenessThree = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedAwarenessThree == -1) {
                            return;
                        }
                        mAwarenessOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedAwarenessThree]);
                    }
                });
                break;
            case R.id.exit_survey_collection_button_one:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedCollectionOne, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCollectionOne = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedCollectionOne == -1) {
                            return;
                        }
                        mCollectionOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedCollectionOne]);
                    }
                });
                break;
            case R.id.exit_survey_collection_button_two:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedCollectionTwo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCollectionTwo = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedCollectionTwo == -1) {
                            return;
                        }
                        mCollectionOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedCollectionTwo]);
                    }
                });
                break;
            case R.id.exit_survey_collection_button_three:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedCollectionThree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCollectionThree = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedCollectionThree == -1) {
                            return;
                        }
                        mCollectionOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedCollectionThree]);
                    }
                });
                break;
            case R.id.exit_survey_collection_button_four:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedCollectionFour, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCollectionFour = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedCollectionFour == -1) {
                            return;
                        }
                        mCollectionOptionsFour.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedCollectionFour]);
                    }
                });
                break;
//            case R.id.exit_survey_error_button_one:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedErrorOne, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedErrorOne = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedErrorOne == -1) {
//                            return;
//                        }
//                        mErrorOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedErrorOne]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_error_button_two:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedErrorTwo, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedErrorTwo = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedErrorTwo == -1) {
//                            return;
//                        }
//                        mErrorOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedErrorTwo]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_error_button_three:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedErrorThree, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedErrorThree = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedErrorThree == -1) {
//                            return;
//                        }
//                        mErrorOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedErrorThree]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_error_button_four:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedErrorFour, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedErrorFour = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedErrorFour == -1) {
//                            return;
//                        }
//                        mErrorOptionsFour.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedErrorFour]);
//                    }
//                });
//                break;
            case R.id.exit_survey_secondary_use_button_one:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedSecondaryUseOne, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSecondaryUseOne = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedSecondaryUseOne == -1) {
                            return;
                        }
                        mSecondaryUseOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedSecondaryUseOne]);
                    }
                });
                break;
            case R.id.exit_survey_secondary_use_button_two:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedSecondaryUseTwo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSecondaryUseTwo = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedSecondaryUseTwo == -1) {
                            return;
                        }
                        mSecondaryUseOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedSecondaryUseTwo]);
                    }
                });
                break;
            case R.id.exit_survey_secondary_use_button_three:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedSecondaryUseThree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSecondaryUseThree = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedSecondaryUseThree == -1) {
                            return;
                        }
                        mSecondaryUseOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedSecondaryUseThree]);
                    }
                });
                break;
            case R.id.exit_survey_secondary_use_button_four:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedSecondaryUseFour, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSecondaryUseFour = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedSecondaryUseFour == -1) {
                            return;
                        }
                        mSecondaryUseOptionsFour.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedSecondaryUseFour]);
                    }
                });
                break;
//            case R.id.exit_survey_secondary_use_button_five:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedSecondaryUseFive, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedSecondaryUseFive = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedSecondaryUseFive == -1) {
//                            return;
//                        }
//                        mSecondaryUseOptionsFive.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedSecondaryUseFive]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_improper_button_one:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedImproperOne, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedImproperOne = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedImproperOne == -1) {
//                            return;
//                        }
//                        mImproperOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedImproperOne]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_improper_button_two:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedImproperTwo, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedImproperTwo = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedImproperTwo == -1) {
//                            return;
//                        }
//                        mImproperOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedImproperTwo]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_improper_button_three:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedImproperThree, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedImproperThree = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedImproperThree == -1) {
//                            return;
//                        }
//                        mImproperOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedImproperThree]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_global_button_one:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedGlobalOne, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedGlobalOne = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedGlobalOne == -1) {
//                            return;
//                        }
//                        mGlobalOptionsOne.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedGlobalOne]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_global_button_two:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedGlobalTwo, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedGlobalTwo = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedGlobalTwo == -1) {
//                            return;
//                        }
//                        mGlobalOptionsTwo.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedGlobalTwo]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_global_button_three:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedGlobalThree, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedGlobalThree = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedGlobalThree == -1) {
//                            return;
//                        }
//                        mGlobalOptionsThree.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedGlobalThree]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_global_button_four:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedGlobalFour, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedGlobalFour = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedGlobalFour == -1) {
//                            return;
//                        }
//                        mGlobalOptionsFour.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedGlobalFour]);
//                    }
//                });
//                break;
//            case R.id.exit_survey_global_button_five:
//                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
//                alertDialogBuilder.setSingleChoiceItems(R.array.uipc_question_options, selectedGlobalFive, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedGlobalFive = which;
//                    }
//                });
//                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (selectedGlobalFive == -1) {
//                            return;
//                        }
//                        mGlobalOptionsFive.setText(getResources().getStringArray(R.array.uipc_question_options)[selectedGlobalFive]);
//                    }
//                });
//                break;
            case R.id.exit_survey_additional_button_familiar:
                alertDialogBuilder.setTitle(getString(R.string.select_an_option));
                alertDialogBuilder.setSingleChoiceItems(R.array.exit_survey_additional_question_familiar_options, selectedFamiliar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedFamiliar = which;
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedFamiliar == -1) {
                            return;
                        }
                        mAdditionalOptionsFamiliar.setText(getResources().getStringArray(R.array.exit_survey_additional_question_familiar_options)[selectedFamiliar]);
                    }
                });
                break;
            case R.id.exit_survey_additional_button_dont_understand:
                alertDialogBuilder.setTitle(getString(R.string.select_multiple_allowed));
                alertDialogBuilder.setMultiChoiceItems(R.array.exit_survey_additional_question_dont_understand_options, dontKnowChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedDontKnow.add(which);
                        } else {
                            selectedDontKnow.remove(which);
                        }
                    }
                });
                alertDialogBuilder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (selectedDontKnow.isEmpty()) {
                                    mAdditionalOptionsDontUnderstand.setText(R.string.select_multiple_allowed);
                                    return;
                                }
                                ArrayList<String> dontKnowPermissionsTexts = new ArrayList<>();
                                for (int index : selectedDontKnow) {
                                    dontKnowPermissionsTexts.add(dontKnowOptions[index]);
                                }
                                mAdditionalOptionsDontUnderstand.setText(TextUtils.join(OPTION_DELIMITER, dontKnowPermissionsTexts));
                            }
                        });
                break;
            default:
                return;
        }

        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    boolean[] dontKnowChecked = new boolean[PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.exit_survey_additional_question_dont_understand_options).length];
    HashSet<Integer> selectedDontKnow = new HashSet<>();
    String[] dontKnowOptions = PrivaDroidApplication.getAppContext().getResources().getStringArray(R.array.exit_survey_additional_question_dont_understand_options);

    int selectedFamiliar = -1;
    //    int selectedGlobalOne = -1;
//    int selectedGlobalTwo = -1;
//    int selectedGlobalThree = -1;
//    int selectedGlobalFour = -1;
//    int selectedGlobalFive = -1;
//    int selectedImproperOne = -1;
//    int selectedImproperTwo = -1;
//    int selectedImproperThree = -1;
    int selectedSecondaryUseOne = -1;
    int selectedSecondaryUseTwo = -1;
    int selectedSecondaryUseThree = -1;
    int selectedSecondaryUseFour = -1;
    //    int selectedSecondaryUseFive = -1;
//    int selectedErrorOne = -1;
//    int selectedErrorTwo = -1;
//    int selectedErrorThree = -1;
//    int selectedErrorFour = -1;
    int selectedCollectionOne = -1;
    int selectedCollectionTwo = -1;
    int selectedCollectionThree = -1;
    int selectedCollectionFour = -1;
    int selectedControlOne = -1;
    int selectedControlTwo = -1;
    int selectedControlThree = -1;
    int selectedControlFour = -1;
    int selectedAwarenessOne = -1;
    int selectedAwarenessTwo = -1;
    int selectedAwarenessThree = -1;
}
