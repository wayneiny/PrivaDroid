package com.weichengcao.privadroid.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.R;
import com.weichengcao.privadroid.util.UserPreferences;

import org.joda.time.DateTime;

import java.util.List;

public class BaseNotificationProvider {

    public final static String NOTIFICATION_INTENT_PAYLOAD = "NOTIFICATION_INTENT_PAYLOAD";

    public static final String CHANNEL_ID = "PrivaDroid Notification Channel";
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

        String tickerText = PrivaDroidApplication.getAppContext().getString(R.string.appName);

        // Adding bigText style to notification enabling larger messages to be read
        // in the notification pane
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.setBigContentTitle(tickerText);
        bigStyle.bigText(PrivaDroidApplication.getAppContext().getString(R.string.foreground_service_notification_text));

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(PrivaDroidApplication.getAppContext(), CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(tickerText)
                .setTicker(tickerText)
                .setContentText(PrivaDroidApplication.getAppContext().getString(R.string.foreground_service_notification_text))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)       // whether it's dismissible
                .setAutoCancel(false)    //whether it should disappear on user click
                .setStyle(bigStyle);

        int defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
        notificationBuilder.setDefaults(defaults);

        return notificationBuilder.build();
    }

    /**
     * Check if we should create a notification according to the last notification timestamp.
     */
    public static boolean shouldCreateNotification() {
        int NOTIFICATION_INTERVAL_IN_MINUTES = 5;

        UserPreferences userPreferences = new UserPreferences(PrivaDroidApplication.getAppContext());
        String lastNotificationTimestamp = userPreferences.getLastNotificationTimestamp();

        if (lastNotificationTimestamp.isEmpty()) {
            return true;
        }

        DateTime lastNotificationTime = DateTime.parse(lastNotificationTimestamp);
        DateTime now = DateTime.now();

        return now.minusMinutes(NOTIFICATION_INTERVAL_IN_MINUTES).isAfter(lastNotificationTime);
    }

    /**
     * Check if heartbeat and demographic reminder job has been scheduled.
     */
    public static boolean isJobIdScheduled(int jobId) {
        JobScheduler jobScheduler = PrivaDroidApplication.getAppContext().getSystemService(JobScheduler.class);
        if (jobScheduler == null) {
            return false;
        }
        List<JobInfo> allJobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : allJobs) {
            if (job != null) {
                if (job.getId() == jobId) {
                    return true;
                }
            }
        }
        return false;
    }
}
