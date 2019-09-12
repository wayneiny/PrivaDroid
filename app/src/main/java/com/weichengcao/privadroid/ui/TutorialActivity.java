package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

import static com.weichengcao.privadroid.util.ExperimentEventFactory.createJoinEvent;
import static com.weichengcao.privadroid.util.EventConstants.JOIN_EVENT_COLLECTION;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton mStartUsingAppButton;
    private UserPreferences mUserPreferences;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mStartUsingAppButton = findViewById(R.id.start_using_app_button);
        mStartUsingAppButton.setOnClickListener(this);

        mUserPreferences = new UserPreferences(this);

        mFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {
        if (view == mStartUsingAppButton) {
            // 1. Log a join event and send to FireStore
            mFirestore.collection(JOIN_EVENT_COLLECTION).add(createJoinEvent())
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                DocumentReference documentReference = task.getResult();
                                if (documentReference == null) {
                                    return;
                                }
                                mUserPreferences.setFirestoreJoinEventId(documentReference.getId());

                                // 2.1. Navigate to main screen
                                Intent intent = new Intent(PrivaDroidApplication.getAppContext(), MainScreenActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // 2.2 Display failed message
                                Toast.makeText(PrivaDroidApplication.getAppContext(), "Failed to join. Please Try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}
