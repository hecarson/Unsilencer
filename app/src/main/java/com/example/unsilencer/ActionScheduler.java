package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ActionScheduler {

    private final Context context;

    public ActionScheduler(Context context) {
        this.context = context;
    }

    public void updateAlarmsForActions(List<Action> actions) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);
        PendingIntent ringerModeSettingPendingIntent = PendingIntent.getBroadcast(context, 0, ringerModeSettingIntent, PendingIntent.FLAG_IMMUTABLE);

        // cancel all action alarms
        alarmManager.cancel(ringerModeSettingPendingIntent);

        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms())
            return;

        // make new alarms for each action
        for (Action action : actions) {
            ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);
            ringerModeSettingIntent.putExtra("hour", action.hour);
            ringerModeSettingIntent.putExtra("minute", action.minute);
            ringerModeSettingIntent.putExtra("ringerMode", action.ringerMode);

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    findNextEpochTimeForAction(action),
                    PendingIntent.getBroadcast(context, 0, ringerModeSettingIntent, PendingIntent.FLAG_IMMUTABLE)
            );
        }
    }

    public static long findNextEpochTimeForAction(Action action) {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());
        nowCalendar.set(Calendar.SECOND, 0);
        GregorianCalendar actionCalendar = (GregorianCalendar)nowCalendar.clone();
        actionCalendar.set(Calendar.HOUR_OF_DAY, action.hour);
        actionCalendar.set(Calendar.MINUTE, action.minute);

        if (!nowCalendar.before(actionCalendar))
            actionCalendar.add(Calendar.DAY_OF_MONTH, 1);

        return actionCalendar.getTimeInMillis();
    }

}