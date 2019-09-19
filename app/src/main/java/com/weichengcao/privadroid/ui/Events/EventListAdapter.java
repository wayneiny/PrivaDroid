package com.weichengcao.privadroid.ui.Events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.BaseServerEvent;
import com.weichengcao.privadroid.util.DatetimeUtil;

import java.util.ArrayList;

import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.ui.MainScreenActivity.APP_UNINSTALL_EVENT_TYPE;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListItemViewHolder> {

    public static class EventListItemViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;

        public EventListItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_list_item_card_title);
            description = itemView.findViewById(R.id.event_list_item_card_description);
        }

        void bind(final BaseServerEvent event, final Context context) {
            title.setText(event.getAppName());

            switch (event.getEventType()) {
                case APP_INSTALL_EVENT_TYPE:
                    description.setText(context.getString(R.string.why_install_app_on_date_list_item_description,
                            event.getAppName(), DatetimeUtil.convertIsoToReadableFormat(event.getLoggedTime())));
                    break;
                case APP_UNINSTALL_EVENT_TYPE:
                    description.setText(context.getString(R.string.why_uninstall_app_on_date_list_item_description,
                            event.getAppName(), DatetimeUtil.convertIsoToReadableFormat(event.getLoggedTime())));
                    break;
            }
        }
    }

    private Context mContext;
    private ArrayList<BaseServerEvent> mDataset;
    private int mEventType;

    public EventListAdapter(Context context, ArrayList<BaseServerEvent> events, int eventType) {
        mContext = context;
        mDataset = events;
        mEventType = eventType;
    }

    @Override
    public EventListAdapter.EventListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
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
