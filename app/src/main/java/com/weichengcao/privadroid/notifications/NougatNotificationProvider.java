package com.weichengcao.privadroid.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.database.AppInstallServerEvent;
import com.weichengcao.privadroid.database.AppUninstallServerEvent;
import com.weichengcao.privadroid.database.PermissionServerEvent;
import com.weichengcao.privadroid.ui.SurveyQuestions.AppInstallSurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.AppUninstallSurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.PermissionDenySurveyActivity;
import com.weichengcao.privadroid.ui.SurveyQuestions.PermissionGrantSurveyActivity;
import com.weichengcao.privadroid.util.DatetimeUtil;

import static com.weichengcao.privadroid.util.EventUtil.EVENT_ID_INTENT_KEY;

public class NougatNotificationProvider extends BaseNotificationProvider {

    private Context mContext;

    private static final String DEFAULT_CHANNEL_ID = "DEFAULT_CHANNEL_ID";

    public NougatNotificationProvider(Context context) {
        mContext = context;
    }

    /**
     * App install event survey.
     */
    public void createNotificationForInstallEventSurvey(AppInstallServerEvent event) {
        Intent intent = new Intent(mContext, AppInstallSurveyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_ID_INTENT_KEY, event.getServerId());
        intent.putExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD, bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_v2_transparent)
                .setContentTitle(event.getAppName())
                .setContentText(mContext.getString(R.string.why_install_app_description, event.getAppName()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(DatetimeUtil.getIsoHash(event.getLoggedTime()), builder.build());
    }

    /**
     * App uninstall event survey.
     */
    public void createNotificationForUninstallEventSurvey(AppUninstallServerEvent event) {
        Intent intent = new Intent(mContext, AppUninstallSurveyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_ID_INTENT_KEY, event.getServerId());
        intent.putExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD, bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_v2_transparent)
                .setContentTitle(event.getAppName())
                .setContentText(mContext.getString(R.string.why_uninstall_app_description, event.getAppName()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(DatetimeUtil.getIsoHash(event.getLoggedTime()), builder.build());
    }

    /**
     * Permission event survey.
     */
    public void createNotificationForPermissionEventSurvey(PermissionServerEvent event) {
        boolean isPermissionGrant = event.isRequestedGranted();
        Intent intent = new Intent(mContext, isPermissionGrant ? PermissionGrantSurveyActivity.class : PermissionDenySurveyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EVENT_ID_INTENT_KEY, event.getServerId());
        intent.putExtra(BaseNotificationProvider.NOTIFICATION_INTENT_PAYLOAD, bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_v2_transparent)
                .setContentTitle(event.getAppName())
                .setContentText(mContext.getString(
                        isPermissionGrant ? R.string.why_grant_permission_for_app_description : R.string.why_deny_permission_for_app_description,
                        event.getPermissionName(), event.getAppName(), DatetimeUtil.convertIsoToReadableFormat(event.getLoggedTime())))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(DatetimeUtil.getIsoHash(event.getLoggedTime()), builder.build());
    }
}
