package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class RingerModeSettingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int hour = extras.getInt("hour");
        int minute = extras.getInt("minute");
        int ringerMode = extras.getInt("ringerMode");
        int requestCode = extras.getInt("requestCode");

        Log.d("Unsilencer", "received alarm for " +
                hour + "h " +
                minute + "m, request code " +
                requestCode + ", ringer mode " +
                ringerMode);

        NotificationManager notifManager = context.getSystemService(NotificationManager.class);
        AudioManager audioManager = context.getSystemService(AudioManager.class);

        if (!notifManager.isNotificationPolicyAccessGranted())
            return;

        // this entire project... just for this one line
        audioManager.setRingerMode(ringerMode);

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms())
            return;

        Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);
        ringerModeSettingIntent.putExtra("hour", hour);
        ringerModeSettingIntent.putExtra("minute", minute);
        ringerModeSettingIntent.putExtra("ringerMode", ringerMode);
        ringerModeSettingIntent.putExtra("requestCode", requestCode);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                ActionScheduler.findNextEpochTimeForAction(hour, minute),
                PendingIntent.getBroadcast(context, requestCode, ringerModeSettingIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
        );

        GregorianCalendar actionCalendar = new GregorianCalendar();
        actionCalendar.setTimeInMillis(ActionScheduler.findNextEpochTimeForAction(hour, minute));
        Log.d("Unsilencer", "set alarm for " +
                actionCalendar.get(Calendar.DAY_OF_MONTH) + "d " +
                actionCalendar.get(Calendar.HOUR_OF_DAY) + "h " +
                actionCalendar.get(Calendar.MINUTE) + "m, request code " +
                requestCode + ", ringer mode " +
                ringerMode);
    }

}