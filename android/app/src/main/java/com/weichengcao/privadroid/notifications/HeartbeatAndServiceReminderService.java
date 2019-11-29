package com.weichengcao.privadroid.notifications;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.database.ExperimentEventFactory;
import com.weichengcao.privadroid.database.FirestoreProvider;
import com.weichengcao.privadroid.util.AccessibilityAppUsageUtil;
import com.weichengcao.privadroid.util.DatetimeUtil;
import com.weichengcao.privadroid.util.UserPreferences;

import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.getJobIdScheduled;
import static com.weichengcao.privadroid.notifications.DemographicReminderService.MAX_DELAY_OF_JOB_IN_MILLISECONDS;

public class HeartbeatAndServiceReminderService extends JobService {
    static final int ONE_DAY_IN_MILLISECONDS = 86400000;        // 1 day
    static final int HEARTBEAT_REMINDER_JOB_ID = 0;

    @Override
    public boolean onStartJob(JobParameters params) {
        // 1. Log heartbeat status
        boolean isAccessibilityOn = AccessibilityAppUsageUtil.isAccessibilitySettingsOn();
        boolean isAppUsageOn = AccessibilityAppUsageUtil.isAppUsageSettingsOn();
        FirestoreProvider firestoreProvider = new FirestoreProvider();
        firestoreProvider.sendHeartbeatEvent(ExperimentEventFactory.createHeartbeatEvent(
                Boolean.toString(isAccessibilityOn),
                Boolean.toString(isAppUsageOn)));

        // 2. Remind user to enable accessibility and usage access
        if (!isAccessibilityOn) {
            createAccessibilityAccessReminder();
        }
        if (!isAppUsageOn) {
            createAppUsageAccessReminder();
        }

        // 3. Re-schedule the job
        scheduleHeartbeatAndServiceReminderJob();

        // 4. Log the current reminder
        new UserPreferences(PrivaDroidApplication.getAppContext()).setLastHeartbeatReminder(DatetimeUtil.getCurrentIsoDatetime());

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public static void createAccessibilityAccessReminder() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            new QNotificationProvider(PrivaDroidApplication.getAppContext()).createAccessibilityAccessReminder();
        }
    }

    public static void createAppUsageAccessReminder() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            new QNotificationProvider(PrivaDroidApplication.getAppContext()).createAppUsageAccessReminder();
        }
    }

    public static void scheduleHeartbeatAndServiceReminderJob() {
        ComponentName serviceComponent = new ComponentName(PrivaDroidApplication.getAppContext(), HeartbeatAndServiceReminderService.class);

        JobInfo.Builder builder = new JobInfo.Builder(HEARTBEAT_REMINDER_JOB_ID, serviceComponent);
        builder.setMinimumLatency(ONE_DAY_IN_MILLISECONDS);
        builder.setOverrideDeadline(ONE_DAY_IN_MILLISECONDS + MAX_DELAY_OF_JOB_IN_MILLISECONDS);

        JobScheduler jobScheduler = PrivaDroidApplication.getAppContext().getSystemService(JobScheduler.class);
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    public static boolean isHeartbeatReminderJobScheduled() {
        return getCurrentHeartbeatReminderJobScheduled() != null;
    }

    public static JobInfo getCurrentHeartbeatReminderJobScheduled() {
        return getJobIdScheduled(HEARTBEAT_REMINDER_JOB_ID);
    }
}
