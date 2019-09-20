package com.weichengcao.privadroid.ui.Events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.BaseServerEvent;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.ui.SurveyQuestions.AppInstallSurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.AppUninstallSurveyActivity;
import com.weichengcao.privadroid.util.DatetimeUtil;

import java.util.ArrayList;

import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_ID_INTENT_KEY;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListItemViewHolder> {

    public static class EventListItemViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        public View view;

        EventListItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = itemView.findViewById(R.id.event_list_item_card_title);
            description = itemView.findViewById(R.id.event_list_item_card_description);
        }

        void bind(final BaseServerEvent event, final Context context) {
            title.setText(event.getAppName());

            switch (event.getEventType()) {
                case APP_INSTALL_EVENT_TYPE:
                    description.setText(context.getString(R.string.why_install_app_on_date_list_item_description,
                            event.getAppName(), DatetimeUtil.convertIsoToReadableFormat(event.getLoggedTime())));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, AppInstallSurveyActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(EVENT_ID_INTENT_KEY, event.getServerId());
                            intent.putExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD, bundle);
                            context.startActivity(intent);
                        }
                    });
                    break;
                case APP_UNINSTALL_EVENT_TYPE:
                    description.setText(context.getString(R.string.why_uninstall_app_on_date_list_item_description,
                            event.getAppName(), DatetimeUtil.convertIsoToReadableFormat(event.getLoggedTime())));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, AppUninstallSurveyActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(EVENT_ID_INTENT_KEY, event.getServerId());
                            intent.putExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD, bundle);
                            context.startActivity(intent);
                        }
                    });
                    break;
            }
        }
    }

    private Context mContext;
    private ArrayList<BaseServerEvent> mDataset;

    public EventListAdapter(Context context, ArrayList<BaseServerEvent> events) {
        mContext = context;
        mDataset = events;
    }

    @NonNull
    @Override
    public EventListAdapter.EventListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        return new EventListItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventListItemViewHolder holder, int position) {
        holder.bind(mDataset.get(position), mContext);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
