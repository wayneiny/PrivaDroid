package com.weichengcao.privadroid.ui.TutorialCards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    public static final int HOW_TO_CARD_INDEX = 0;
    public static final int ACCESSIBILITY_INDEX = 1;
    public static final int APP_USAGE_INDEX = 2;

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;

    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    public void setCardItemAt(int position, CardItem item) {
        mData.set(position, item);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.card_adapter, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = view.findViewById(R.id.tutorial_card);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(CardItem item, View view) {
        TextView title = view.findViewById(R.id.tutorial_card_title);
        TextView description = view.findViewById(R.id.tutorial_card_description);
        Button button = view.findViewById(R.id.tutorial_card_button);
        title.setText(item.getTitleStringResourceId());
        description.setText(item.getDescriptionStringResourceId());
        button.setText(item.getButtonStringResourceId());
        button.setOnClickListener(item.getButtonListener());
        if (item.isSetUpComplete()) {
            button.setText(R.string.done);
            button.setTextColor(ContextCompat.getColor(PrivaDroidApplication.getAppContext(), R.color.done_green));
        }
    }
}

