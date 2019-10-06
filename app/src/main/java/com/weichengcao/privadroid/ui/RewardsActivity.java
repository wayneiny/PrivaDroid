package com.weichengcao.privadroid.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.database.RewardsServerEvent;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.ExperimentEventFactory;
import com.weichengcao.privadroid.util.UserPreferences;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

import static com.weichengcao.privadroid.database.FirestoreProvider.isNetworkAvailable;
import static com.weichengcao.privadroid.ui.ProfileFragment.REWARDS_DAYS;
import static com.weichengcao.privadroid.util.UserPreferences.UNKNOWN_DATE;

public class RewardsActivity extends AppCompatActivity {

    private static final String PAYPAL_METHOD = "PayPal";
    private static final String BITCOIN_METHOD = "Bitcoin";

    private ImageView mBack;
    private MaterialButton mSubmit;
    private TextInputEditText mRewardsMethodValue;
    private TextInputEditText mRewardsMethodConfirm;
    private TextInputLayout mRewardsMethodConfirmLayout;
    private MaterialCardView mRewardsDaysLeftCard;
    private TextView mDaysLeftText;
    private MaterialCardView mRewardsMethodCard;
    private TextView mCompleteJoinDateText;
    private TextView mRewardsAnsweredOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBack = findViewById(R.id.rewards_back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRewardsMethodValue = findViewById(R.id.rewards_method_value);
        mRewardsMethodConfirm = findViewById(R.id.rewards_method_confirm_value);
        mRewardsDaysLeftCard = findViewById(R.id.rewards_days_left_card);
        mRewardsMethodConfirmLayout = findViewById(R.id.rewards_method_confirm_layout);
        mDaysLeftText = findViewById(R.id.rewards_days_left_text);
        mRewardsMethodCard = findViewById(R.id.rewards_method_card);
        mCompleteJoinDateText = findViewById(R.id.rewards_complete_join_date_text);
        mRewardsAnsweredOn = findViewById(R.id.rewards_answered_on);
        mSubmit = findViewById(R.id.rewards_submit_button);

        /**
         * User has not entered the rewards before. Check eligibility and update UI.
         */
        String joinDateText = new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate();
        if (!joinDateText.equals(UNKNOWN_DATE)) {
            DateTime joinDate = DateTime.parse(joinDateText);
            DateTime now = DateTime.now();

            boolean eligibleForRewards = now.minusDays(REWARDS_DAYS).isAfter(joinDate);
            if (eligibleForRewards) {
                /**
                 * Query from Firebase the rewards event.
                 */
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                CollectionReference rewardsColRef = firebaseFirestore.collection(EventUtil.REWARDS_COLLECTION);
                Query query = rewardsColRef.whereEqualTo(EventUtil.USER_AD_ID, new UserPreferences(this).getAdvertisingId());
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                List<DocumentSnapshot> documentSnapshotList = querySnapshot.getDocuments();
                                if (!documentSnapshotList.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = documentSnapshotList.get(0);

                                    RewardsServerEvent rewardsServerEvent = new RewardsServerEvent(
                                            documentSnapshot.getString(EventUtil.USER_AD_ID),
                                            documentSnapshot.getString(EventUtil.LOGGED_TIME),
                                            documentSnapshot.getString(EventUtil.REWARDS_JOIN_DATE),
                                            documentSnapshot.getString(EventUtil.REWARDS_METHOD),
                                            documentSnapshot.getString(EventUtil.REWARDS_METHOD_VALUE));

                                    mRewardsDaysLeftCard.setVisibility(View.GONE);
                                    mCompleteJoinDateText.setText(PrivaDroidApplication.getAppContext()
                                            .getString(R.string.rewards_complete_join_date_text,
                                                    DatetimeUtil.convertIsoToReadableFormat(rewardsServerEvent.getJoinDate())));

                                    mRewardsMethodValue.setText(rewardsServerEvent.getMethodValue());
                                    mRewardsMethodValue.setEnabled(false);
                                    mRewardsMethodConfirm.setVisibility(View.GONE);

                                    mRewardsAnsweredOn.setText(PrivaDroidApplication.getAppContext()
                                            .getString(R.string.answered_on_prefix_new,
                                                    DatetimeUtil.convertIsoToReadableFormat(rewardsServerEvent.getLoggedTime())));

                                    mRewardsMethodConfirmLayout.setVisibility(View.GONE);

                                    mSubmit.setVisibility(View.GONE);
                                } else {
                                    mSubmit.setVisibility(View.VISIBLE);
                                    mCompleteJoinDateText.setText(PrivaDroidApplication.getAppContext()
                                            .getString(R.string.rewards_complete_join_date_text,
                                                    DatetimeUtil.convertIsoToReadableFormat(new UserPreferences(PrivaDroidApplication.getAppContext()).getJoinDate())));
                                    mRewardsDaysLeftCard.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
            } else {
                mSubmit.setVisibility(View.GONE);
                mRewardsMethodCard.setVisibility(View.GONE);

                String daysLeft = (REWARDS_DAYS - (int) new Duration(joinDate, now).getStandardDays()) + "";
                mDaysLeftText.setText(PrivaDroidApplication.getAppContext().getString(R.string.rewards_days_left_text, daysLeft));
            }
        } else {
            Toast.makeText(PrivaDroidApplication.getAppContext(), PrivaDroidApplication.getAppContext().getString(R.string.rewards_join_date_invalid_contact_team), Toast.LENGTH_LONG).show();
        }

        setUpSubmit();
    }

    private void setUpSubmit() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    Toast.makeText(PrivaDroidApplication.getAppContext(), PrivaDroidApplication.getAppContext().getString(R.string.no_internet_connection_error), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mRewardsMethodValue.getText() == null || mRewardsMethodValue.getText().toString().trim().isEmpty()) {
                    mRewardsMethodValue.setError(PrivaDroidApplication.getAppContext().getString(R.string.rewards_invalid_method));
                } else if (mRewardsMethodConfirm.getText() == null || mRewardsMethodConfirm.getText().toString().trim().isEmpty()) {
                    mRewardsMethodConfirm.setError(PrivaDroidApplication.getAppContext().getString(R.string.rewards_invalid_method));
                } else {
                    String methodValue = mRewardsMethodValue.getText().toString().trim();
                    String methodConfirmValue = mRewardsMethodConfirm.getText().toString().trim();

                    if (!methodValue.equals(methodConfirmValue)) {
                        mRewardsMethodConfirm.setError(PrivaDroidApplication.getAppContext().getString(R.string.rewards_method_not_match_error));
                        return;
                    }

                    String methodName = methodValue.contains("@") ? PAYPAL_METHOD : BITCOIN_METHOD;

                    FirestoreProvider firestoreProvider = new FirestoreProvider();
                    firestoreProvider.sendRewardsEvent(ExperimentEventFactory.createRewardsMethodEvent(methodName, methodValue));
                    Toast.makeText(PrivaDroidApplication.getAppContext(), PrivaDroidApplication.getAppContext().getString(R.string.congratulations), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
