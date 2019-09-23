package com.weichengcao.privadroid.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;

import static com.weichengcao.privadroid.sensors.SystemBroadcastForegroundService.PRIVADROID_APP_NAME;

public class BaseNotificationProvider {

    public final static String NOTIFICATION_INTENT_PAYLOAD = "NOTIFICATION_INTENT_PAYLOAD";

    public static final String CHANNEL_ID = "PRIVADROID_DEFAULT_CHANNEL";
    private static final String CHANNEL_DESC = "This is the default channel for PrivaDroid.";
    public static final int FOREGROUND_SERVICE_NOTIFICATION_ID = 1;

    /**
     * Create notification channel if >= Oreo.
     */
    public static void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(CHANNEL_DESC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = PrivaDroidApplication.getAppContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create notification for foreground service if >= Oreo.
     */
    public static Notification createAndroidNotificationForForegroundService() {
        int icon = R.drawable.logo_v2_transparent;

        String tickerText = PRIVADROID_APP_NAME;

        // Adding bigText style to notification enabling larger messages to be read
        // in the notification pane
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.setBigContentTitle(tickerText);
        bigStyle.bigText(PRIVADROID_APP_NAME + " is running.");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(PrivaDroidApplication.getAppContext(), CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(tickerText)
                .setTicker(tickerText)
                .setContentText(PRIVADROID_APP_NAME + " is running.")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)       // whether it's dismissible
                .setAutoCancel(false)    //whether it should disappear on user click
                .setStyle(bigStyle);

        int defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
        notificationBuilder.setDefaults(defaults);

        return notificationBuilder.build();
    }
}
