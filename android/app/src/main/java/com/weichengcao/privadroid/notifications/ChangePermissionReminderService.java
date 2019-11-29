package com.weichengcao.privadroid.notifications;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;
import android.os.PersistableBundle;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.util.EventUtil;
import com.weichengcao.privadroid.util.RuntimePermissionAppUtil;

public class ChangePermissionReminderService extends JobService {

    public static final int PERMISSION_REVOKE_REMINDER_JOB_ID = 2;
    public static final int ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;   // 1 hour in milliseconds
    public static final int MAX_DELAY_OF_JOB_IN_MILLISECONDS = 600000;   // 10 minutes

    public static void createReminderNotificationToDisablePermission(String surveyServerDocId, String appName, String permissionName) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            new QNotificationProvider(PrivaDroidApplication.getAppContext()).createPermissionRevokeReminder(surveyServerDocId, appName, permissionName);
        }
    }

    static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void schedulePermissionRevokeReminder(String surveyServerDocId, String appName, String permissionName,
                                                        String packageName, String delayedHour) {
        ComponentName serviceComponent = new ComponentName(PrivaDroidApplication.getAppContext(), ChangePermissionReminderService.class);

        if (tryParseInt(delayedHour)) {
            int delayedMilliseconds = Integer.parseInt(delayedHour) * ONE_HOUR_IN_MILLISECONDS;
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString(EventUtil.EVENT_SERVER_ID, surveyServerDocId);
            bundle.putString(EventUtil.APP_NAME, appName);
            bundle.putString(EventUtil.PERMISSION_REQUESTED_NAME, permissionName);
            bundle.putString(EventUtil.PACKAGE_NAME, packageName);
            bundle.putString(EventUtil.REVOKE_DELAYED_HOUR, delayedHour);

            JobInfo.Builder builder = new JobInfo.Builder(PERMISSION_REVOKE_REMINDER_JOB_ID, serviceComponent).setExtras(bundle);
            builder.setMinimumLatency(delayedMilliseconds);
            builder.setOverrideDeadline(delayedMilliseconds + MAX_DELAY_OF_JOB_IN_MILLISECONDS);

            JobScheduler jobScheduler = PrivaDroidApplication.getAppContext().getSystemService(JobScheduler.class);
            if (jobScheduler != null) {
                jobScheduler.schedule(builder.build());
            }
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        String surveyServerDocId = bundle.getString(EventUtil.EVENT_SERVER_ID);
        String appName = bundle.getString(EventUtil.APP_NAME);
        String permissionName = bundle.getString(EventUtil.PERMISSION_REQUESTED_NAME);
        String packageName = bundle.getString(EventUtil.PACKAGE_NAME);
        String delayedHour = bundle.getString(EventUtil.REVOKE_DELAYED_HOUR);
        if (surveyServerDocId == null || surveyServerDocId.isEmpty() ||
                appName == null || appName.isEmpty() ||
                permissionName == null || permissionName.isEmpty() ||
                packageName == null || packageName.isEmpty() ||
                delayedHour == null || delayedHour.isEmpty()) {
            return false;
        }
        // if user is still using the app, reschedule the reminder
        if (packageName.equalsIgnoreCase(RuntimePermissionAppUtil.getLastActiveAppPackageName())) {
            schedulePermissionRevokeReminder(surveyServerDocId, appName, permissionName, packageName, delayedHour);
            return false;
        }
        createReminderNotificationToDisablePermission(surveyServerDocId, appName, permissionName);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
