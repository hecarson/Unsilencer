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

public class RingerModeSettingScheduler {

    private final Context context;

    public RingerModeSettingScheduler(Context context) {
        this.context = context;
    }

    public void cancelRemovedRingerModeSettings(List<RingerModeSetting> removedRingerModeSettings) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);

        for (RingerModeSetting ringerModeSetting : removedRingerModeSettings) {
            PendingIntent ringerModeSettingPendingIntent =
                    PendingIntent.getBroadcast(context, ringerModeSetting.requestCode(), ringerModeSettingIntent,
                            PendingIntent.FLAG_IMMUTABLE);

            alarmManager.cancel(ringerModeSettingPendingIntent);

            Log.d(MainActivity.LOG_TAG, "cancelled alarm for hour " + ringerModeSetting.hour() + " minute " + ringerModeSetting.minute());
        }
    }

    public void addAlarmsForRingerModeSettings(List<RingerModeSetting> addedRingerModeSettings) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms())
            return;

        // make new alarms for each action
        for (RingerModeSetting ringerModeSetting : addedRingerModeSettings) {
            Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);
            ringerModeSettingIntent.putExtra("hour", ringerModeSetting.hour());
            ringerModeSettingIntent.putExtra("minute", ringerModeSetting.minute());
            ringerModeSettingIntent.putExtra("ringerMode", ringerModeSetting.ringerMode());
            ringerModeSettingIntent.putExtra("requestCode", ringerModeSetting.requestCode());

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    findNextEpochTimeForRingerModeSetting(ringerModeSetting.hour(), ringerModeSetting.minute()),
                    PendingIntent.getBroadcast(context, ringerModeSetting.requestCode(), ringerModeSettingIntent,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
            );

            Log.d(MainActivity.LOG_TAG, "set alarm for hour " + ringerModeSetting.hour() +
                    " minute " + ringerModeSetting.minute() + " ringerMode " + ringerModeSetting.ringerMode());
        }
    }

    public static long findNextEpochTimeForRingerModeSetting(int hour, int minute) {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setTimeInMillis(System.currentTimeMillis());
        nowCalendar.set(Calendar.SECOND, 0);
        GregorianCalendar settingCalendar = (GregorianCalendar)nowCalendar.clone();
        settingCalendar.set(Calendar.HOUR_OF_DAY, hour);
        settingCalendar.set(Calendar.MINUTE, minute);

        if (!nowCalendar.before(settingCalendar))
            settingCalendar.add(Calendar.DAY_OF_MONTH, 1);

        return settingCalendar.getTimeInMillis();
    }

}