package com.weichengcao.privadroid.ui;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.DemographicEvent;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.database.ExperimentEventFactory;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.Arrays;
import java.util.HashMap;

import static com.weichengcao.privadroid.database.FirestoreProvider.isNetworkAvailable;
import static com.weichengcao.privadroid.util.EventUtil.DEMOGRAPHIC_COLLECTION;

public class DemographicActivity extends AppCompatActivity {

    UserPreferences mUserPreferences;

    ImageView mBack;

    Spinner mCountrySpinner, mAgeSpinner, mGenderSpinner, mStatusSpinner, mUsageSpinner, mIndustrySpinner, mEducationSpinner, mIncomeSpinner;

    TextView mAnsweredOn;
    MaterialButton mSubmit;

    DemographicEvent mDemographicEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographic);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBack = findViewById(R.id.demographic_back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mUserPreferences = new UserPreferences(this);
        if (mUserPreferences.getAnsweredDemographicSurvey()) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            CollectionReference demoCollection = firebaseFirestore.collection(DEMOGRAPHIC_COLLECTION);
            Query demoQuery = demoCollection.whereEqualTo(EventUtil.USER_AD_ID, mUserPreferences.getAdvertisingId());
            demoQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            mDemographicEvent = new DemographicEvent(
                                    doc.getString(EventUtil.USER_AD_ID), doc.getString(EventUtil.COUNTRY),
                                    doc.getString(EventUtil.EDUCATION), doc.getString(EventUtil.INCOME),
                                    doc.getString(EventUtil.AGE), doc.getString(EventUtil.GENDER),
                                    doc.getString(EventUtil.STATUS), doc.getString(EventUtil.INDUSTRY),
                                    doc.getString(EventUtil.LOGGED_TIME), doc.getString(EventUtil.DAILY_USAGE));
                        }

                        setUpAnsweredSpinner(R.id.demographic_spinner_age);
                        setUpAnsweredSpinner(R.id.demographic_spinner_country);
                        setUpAnsweredSpinner(R.id.demographic_spinner_education);
                        setUpAnsweredSpinner(R.id.demographic_spinner_gender);
                        setUpAnsweredSpinner(R.id.demographic_spinner_income);
                        setUpAnsweredSpinner(R.id.demographic_spinner_industry);
                        setUpAnsweredSpinner(R.id.demographic_spinner_status);
                        setUpAnsweredSpinner(R.id.demographic_spinner_usage);

                        mAnsweredOn = findViewById(R.id.demographic_answered_on);
                        mAnsweredOn.setVisibility(View.VISIBLE);
                        mAnsweredOn.setText(String.format("%s %s", getString(R.string.answered_on_prefix),
                                DatetimeUtil.convertIsoToReadableFormat(mDemographicEvent.getLoggedTime())));
                    }
                }
            });
        } else {
            mAnsweredOn = findViewById(R.id.demographic_answered_on);
            mAnsweredOn.setVisibility(View.GONE);
        }

        setUpSubmit();
    }

    /**
     * Validating answers and set errors. Gather responses.
     */
    boolean validateAnswerBasedOnQuestionId(int questionId) {
        Spinner spinner;
        TextView question;
        switch (questionId) {
            case R.id.demographic_question_age:
                question = findViewById(R.id.demographic_question_age);
                spinner = findViewById(R.id.demographic_spinner_age);
                break;
            case R.id.demographic_question_country:
                question = findViewById(R.id.demographic_question_country);
                spinner = findViewById(R.id.demographic_spinner_country);
                break;
            case R.id.demographic_question_education:
                question = findViewById(R.id.demographic_question_education);
                spinner = findViewById(R.id.demographic_spinner_education);
                break;
            case R.id.demographic_question_gender:
                question = findViewById(R.id.demographic_question_gender);
                spinner = findViewById(R.id.demographic_spinner_gender);
                break;
            case R.id.demographic_question_income:
                question = findViewById(R.id.demographic_question_income);
                spinner = findViewById(R.id.demographic_spinner_income);
                break;
            case R.id.demographic_question_industry:
                question = findViewById(R.id.demographic_question_industry);
                spinner = findViewById(R.id.demographic_spinner_industry);
                break;
            case R.id.demographic_question_status:
                question = findViewById(R.id.demographic_question_status);
                spinner = findViewById(R.id.demographic_spinner_status);
                break;
            case R.id.demographic_question_usage:
                question = findViewById(R.id.demographic_question_usage);
                spinner = findViewById(R.id.demographic_spinner_usage);
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

    HashMap<String, String> gatherResponse() {
        mAgeSpinner = findViewById(R.id.demographic_spinner_age);
        String age = mAgeSpinner.getSelectedItem().toString();

        mCountrySpinner = findViewById(R.id.demographic_spinner_country);
        String country = mCountrySpinner.getSelectedItem().toString();

        mEducationSpinner = findViewById(R.id.demographic_spinner_education);
        String education = mEducationSpinner.getSelectedItem().toString();

        mGenderSpinner = findViewById(R.id.demographic_spinner_gender);
        String gender = mGenderSpinner.getSelectedItem().toString();

        mIncomeSpinner = findViewById(R.id.demographic_spinner_income);
        String income = mIncomeSpinner.getSelectedItem().toString();

        mIndustrySpinner = findViewById(R.id.demographic_spinner_industry);
        String industry = mIndustrySpinner.getSelectedItem().toString();

        mStatusSpinner = findViewById(R.id.demographic_spinner_status);
        String status = mStatusSpinner.getSelectedItem().toString();

        mUsageSpinner = findViewById(R.id.demographic_spinner_usage);
        String usage = mUsageSpinner.getSelectedItem().toString();

        return ExperimentEventFactory.createDemographicEvent(age, country, industry, income, education,
                usage, status, gender);
    }

    /**
     * Set up spinners based on answers.
     */
    void setUpAnsweredSpinner(int spinnerId) {
        Spinner spinner = findViewById(spinnerId);
        String[] options;
        switch (spinnerId) {
            case R.id.demographic_spinner_age:
                options = getResources().getStringArray(R.array.demographic_age_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getAge()));
                break;
            case R.id.demographic_spinner_country:
                options = getResources().getStringArray(R.array.demographic_country_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getCountry()));
                break;
            case R.id.demographic_spinner_education:
                options = getResources().getStringArray(R.array.demographic_education_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getEducation()));
                break;
            case R.id.demographic_spinner_gender:
                options = getResources().getStringArray(R.array.demographic_gender_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getGender()));
                break;
            case R.id.demographic_spinner_income:
                options = getResources().getStringArray(R.array.demographic_income_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getIncome()));
                break;
            case R.id.demographic_spinner_industry:
                options = getResources().getStringArray(R.array.demographic_industry_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getIndustry()));
                break;
            case R.id.demographic_spinner_status:
                options = getResources().getStringArray(R.array.demographic_status_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getStatus()));
                break;
            case R.id.demographic_spinner_usage:
                options = getResources().getStringArray(R.array.demographic_usage_options);
                spinner.setSelection(Arrays.asList(options).lastIndexOf(mDemographicEvent.getDailyUsage()));
                break;
            default:
                return;
        }
        spinner.setEnabled(false);
    }

    void setUpSubmit() {
        mSubmit = findViewById(R.id.demographic_submit_button);
        if (mUserPreferences.getAnsweredDemographicSurvey()) {
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

                if (!validateAnswerBasedOnQuestionId(R.id.demographic_question_age) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_country) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_education) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_gender) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_income) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_industry) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_status) ||
                        !validateAnswerBasedOnQuestionId(R.id.demographic_question_usage)) {
                    Toast.makeText(PrivaDroidApplication.getAppContext(), R.string.finish_all_demographic_questions_toast, Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, String> response = gatherResponse();
                FirestoreProvider firestoreProvider = new FirestoreProvider();
                firestoreProvider.sendDemographicEvent(response);

                mUserPreferences.setAnsweredDemographicSurvey(true);
                finish();
            }
        });
    }
}
