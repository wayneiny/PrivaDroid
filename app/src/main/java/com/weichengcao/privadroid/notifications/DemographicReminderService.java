package com.weichengcao.privadroid.notifications;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;

import com.weichengcao.privadroid.PrivaDroidApplication;
import com.weichengcao.privadroid.util.UserPreferences;

import static com.weichengcao.privadroid.notifications.BaseNotificationProvider.isJobIdScheduled;

public class DemographicReminderService extends JobService {
    static final int DEMOGRAPHIC_INTERVAL_IN_MILLISECONDS = 198720000;                  // 2.3 days
    static final int MAX_DELAY_OF_JOB_IN_MILLISECONDS = 1000 * 1000;                    // 1000 seconds

    static final int DEMOGRAPHIC_REMINDER_JOB_ID = 1;

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!new UserPreferences(PrivaDroidApplication.getAppContext()).getAnsweredDemographicSurvey()) {
            createDemographicSurveyReminder();
            scheduleDemographicSurveyReminder();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public static void createDemographicSurveyReminder() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            new MarshmallowNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            new NougatNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            new NougatMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            new OreoNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            new OreoMR1NotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            new PieNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            new QNotificationProvider(PrivaDroidApplication.getAppContext()).createNotificationForDemographicSurveyReminder();
        }
    }

    public static void scheduleDemographicSurveyReminder() {
        ComponentName serviceComponent = new ComponentName(PrivaDroidApplication.getAppContext(), DemographicReminderService.class);

        JobInfo.Builder builder = new JobInfo.Builder(DEMOGRAPHIC_REMINDER_JOB_ID, serviceComponent);
        builder.setMinimumLatency(DEMOGRAPHIC_INTERVAL_IN_MILLISECONDS);
        builder.setOverrideDeadline(DEMOGRAPHIC_INTERVAL_IN_MILLISECONDS + MAX_DELAY_OF_JOB_IN_MILLISECONDS);

        JobScheduler jobScheduler = PrivaDroidApplication.getAppContext().getSystemService(JobScheduler.class);
        if (jobScheduler != null) {
            jobScheduler.schedule(builder.build());
        }
    }

    public static boolean isDemographicReminderJobScheduled() {
        return isJobIdScheduled(DEMOGRAPHIC_REMINDER_JOB_ID);
    }
}
