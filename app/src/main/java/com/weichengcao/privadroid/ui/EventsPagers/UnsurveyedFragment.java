package com.weichengcao.privadroid.ui.EventsPagers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.weichengcao.privadroid.R;

public class UnsurveyedFragment extends Fragment {

    public UnsurveyedFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unsurveyed, container, false);

        return view;
    }
}