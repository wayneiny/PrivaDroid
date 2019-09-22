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
import com.weichengcao.privadroid.database.PermissionServerEvent;
import com.weichengcao.privadroid.notifications.BaseNotificationProvider;
import com.weichengcao.privadroid.ui.SurveyQuestions.AppInstallSurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.AppUninstallSurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.PermissionDenySurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.PermissionGrantSurveyActivity;
import com.weichengcao.privadroid.util.DatetimeUtil;

import java.util.ArrayList;

import static com.weichengcao.privadroid.util.EventUtil.APP_INSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.APP_UNINSTALL_EVENT_TYPE;
import static com.weichengcao.privadroid.util.EventUtil.EVENT_ID_INTENT_KEY;
import static com.weichengcao.privadroid.util.EventUtil.PERMISSION_EVENT_TYPE;

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
            switch (event.getEventType()) {
                case APP_INSTALL_EVENT_TYPE:
                    title.setText(event.getAppName());
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
                    title.setText(event.getAppName());
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
                case PERMISSION_EVENT_TYPE:
                    final PermissionServerEvent permissionServerEvent = (PermissionServerEvent) event;
                    String permissionName = permissionServerEvent.getPermissionName();
                    String appName = permissionServerEvent.getAppName();
                    final boolean granted = permissionServerEvent.isRequestedGranted();
                    title.setText(context.getString(R.string.permission_app_name_list_item_title,
                            permissionName, appName));
                    description.setText(context.getString(granted ?
                                    R.string.why_grant_permission_for_app_description :
                                    R.string.why_deny_permission_for_app_description,
                            permissionName, appName, DatetimeUtil.convertIsoToReadableFormat(permissionServerEvent.getLoggedTime())));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, granted ? PermissionGrantSurveyActivity.class : PermissionDenySurveyActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(EVENT_ID_INTENT_KEY, permissionServerEvent.getServerId());
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
