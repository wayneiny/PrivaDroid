package com.weichengcao.privadroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import static com.weichengcao.privadroid.ui.MainScreenActivity.EVENT_TYPE;

public class SurveyedCardFragment extends Fragment {

    private Bundle args;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_surveyed, container, false);

        args = getArguments();

        MaterialButton viewData = view.findViewById(R.id.surveyed_card_button);
        viewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivaDroidApplication.getAppContext(), SurveyedEventsActivity.class);
                intent.putExtra(EVENT_TYPE, args.getInt(EVENT_TYPE));
                startActivity(intent);
            }
        });

        return view;
    }
}
