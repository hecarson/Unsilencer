package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ActionScheduler {

    private final Context context;

    public ActionScheduler(Context context) {
        this.context = context;
    }

    public void cancelRemovedActions(List<Action> removedActions) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);

        for (Action action : removedActions) {
            PendingIntent ringerModeSettingPendingIntent =
                    PendingIntent.getBroadcast(context, action.requestCode(), ringerModeSettingIntent,
                            PendingIntent.FLAG_IMMUTABLE);

            alarmManager.cancel(ringerModeSettingPendingIntent);

            Log.d("Unsilencer", "removed alarm for " +
                    action.hour() + "h " +
                    action.minute() + "m, request code " +
                    action.requestCode() + ", ringer mode " +
                    action.ringerMode());
        }
    }

    public void addAlarmsForActions(List<Action> addedActions) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms())
            return;

        // make new alarms for each action
        for (Action action : addedActions) {
            Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);
            ringerModeSettingIntent.putExtra("hour", action.hour());
            ringerModeSettingIntent.putExtra("minute", action.minute());
            ringerModeSettingIntent.putExtra("ringerMode", action.ringerMode());
            ringerModeSettingIntent.putExtra("requestCode", action.requestCode());

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    findNextEpochTimeForAction(action.hour(), action.minute()),
                    PendingIntent.getBroadcast(context, action.requestCode(), ringerModeSettingIntent,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
            );

            GregorianCalendar actionCalendar = new GregorianCalendar();
            actionCalendar.setTimeInMillis(findNextEpochTimeForAction(action.hour(), action.minute()));
            Log.d("Unsilencer", "set alarm for " +
                    actionCalendar.get(Calendar.DAY_OF_MONTH) + "d " +
                    actionCalendar.get(Calendar.HOUR_OF_DAY) + "h " +
                    actionCalendar.get(Calendar.MINUTE) + "m, request code " +
                    action.requestCode() + ", ringer mode " +
                    action.ringerMode());
        }
    }

    public static long findNextEpochTimeForAction(int hour, int minute) {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());
        nowCalendar.set(Calendar.SECOND, 0);
        GregorianCalendar actionCalendar = (GregorianCalendar)nowCalendar.clone();
        actionCalendar.set(Calendar.HOUR_OF_DAY, hour);
        actionCalendar.set(Calendar.MINUTE, minute);

        if (!nowCalendar.before(actionCalendar))
            actionCalendar.add(Calendar.DAY_OF_MONTH, 1);
//            actionCalendar.add(Calendar.SECOND, 10);

        return actionCalendar.getTimeInMillis();
    }

}