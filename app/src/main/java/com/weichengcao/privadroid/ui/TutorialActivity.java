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
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

import java.util.HashMap;

import static com.weichengcao.privadroid.util.FirestoreConstants.USER_AD_ID;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialButton mStartUsingAppButton;
    private UserPreferences mUserPreferences;
    private FirebaseFirestore mFirestore;

    private static final String JOIN_EVENT_COLLECTION = "JOIN_EVENT_COLLECTION";

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
                                mUserPreferences.setFirestoreJoinEventId(documentReference.getId());
                            } else {
                                Toast.makeText(TutorialActivity.this, "Failed to join. Please Try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            // 2. Navigate to main screen
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private HashMap<String, String> createJoinEvent() {
        HashMap<String, String> event = new HashMap<>();

        event.put(USER_AD_ID, mUserPreferences.getAdvertisingId());

        return event;
    }
}
